import configparser
import csv
import json
import logging
import os
import socket
from typing import Dict, List
from PyQt6 import uic, QtCore
from PyQt6.QtCore import pyqtSlot, QPointF, Qt
from PyQt6.QtGui import QColor, QPixmap, QIcon
from PyQt6.QtWidgets import QMainWindow, QTextBrowser, QGraphicsView, QLabel, QMessageBox, QVBoxLayout, \
    QGraphicsSceneMouseEvent, QMenu, QSplashScreen, QPushButton, QFileDialog
from py4j.protocol import Py4JNetworkError, Py4JError
from uwbmap.agent import Agent
from uwbmap.config import read_config, save_config
from uwbmap.mainwindow.agent_summary import AgentSummary
from uwbmap.mainwindow.map import PozyxMap, Robot
from uwbmap.mainwindow.map.robot import RobotOptions
from uwbmap.mqttclient import MQTTClient
from uwbmap.pathsimilarity.similarityresults import ResultTable


class MainWindow(QMainWindow):
    def __init__(self, *args, **kwargs):
        super(MainWindow, self).__init__(*args, **kwargs)
        self.connection_dialog_shown = None
        self.textMQTTLog: QTextBrowser = None
        self.textAgentMessages: QTextBrowser = None
        self.pozyx_map: PozyxMap = None
        self.map: QGraphicsView = None
        self.coordinates: QLabel = None
        self.client: MQTTClient = None
        self.client_local: MQTTClient = None
        self.layoutTabAgents: QVBoxLayout = None
        self.buttonReset: QPushButton = None
        self.buttonResendCharging: QPushButton = None
        self.buttonRemoveCharging: QPushButton = None
        self.MQTTReset: QPushButton = None
        self.MQTTStatus: QLabel = None
        self.localMQTTStatus: QLabel = None
        self.localMQTTReset: QPushButton = None
        self.javaAgentStatus: QLabel = None
        self.javaAgentReset: QPushButton = None
        self.button_results: QPushButton = None
        self.button_file1: QPushButton = None
        self.button_file2: QPushButton = None
        self.physical_agent_file = None
        self.digital_agent_file = None

        self.tags: Dict[str: Robot] = {}
        self.agents: Dict[str: RobotOptions] = {}
        self.charging_stations: List[QPointF] = []
        self.cached_dict: Dict[List[str], str] = {}

        pixmap = QPixmap("uwbmap/pictures/splash.jpg")
        splash = QSplashScreen(pixmap)
        splash.show()
        splash.showMessage("Loading UI...", Qt.AlignmentFlag.AlignBottom | Qt.AlignmentFlag.AlignCenter,
                           QColor("#000000"))
        self.load_ui()
        splash.showMessage("Loading Configuration...", Qt.AlignmentFlag.AlignBottom | Qt.AlignmentFlag.AlignCenter,
                           QColor("#000000"))

        self.config = read_config("config.ini")
        self.smoothing = self.config["GENERAL"].getint("smoothing", fallback=1)
        self.update_window = self.config["GENERAL"].getint("window", fallback=1)
        self.load_agents_from_config()
        splash.showMessage("Connecting to MQTT Broker...",
                           Qt.AlignmentFlag.AlignBottom | Qt.AlignmentFlag.AlignCenter, QColor("#000000"))
        self.client = self.try_connect_client(local=False)
        if self.config.has_section("MQTT_LOCAL") and self.config["MQTT"] != self.config["MQTT_LOCAL"]:
            splash.showMessage("Connecting to local MQTT Broker...",
                               Qt.AlignmentFlag.AlignBottom | Qt.AlignmentFlag.AlignCenter, QColor("#000000"))
            self.client_local = self.try_connect_client(local=True)
        splash.finish(self)

        # create a variable that has a timeout which is executed every specific time interval
        self.check_agent_message_timer = QtCore.QTimer(self)
        self.check_agent_message_timer.setInterval(1000)  # 1 seconds
        self.check_agent_message_timer.timeout.connect(self.check_agent_messages)
        self.check_agent_message_timer.start()

    def try_connect_client_local(self) -> None:
        self.client_local = self.try_connect_client(local=True)

    def try_connect_client_main(self) -> None:
        self.client = self.try_connect_client(local=False)

    def try_connect_client(self, local: bool) -> MQTTClient:
        if local:
            config = self.config["MQTT_LOCAL"]
            status = self.localMQTTStatus
            connecting = "local"
            self.localMQTTReset.clicked.connect(self.try_connect_client_local)
        else:
            config = self.config["MQTT"]
            status = self.MQTTStatus
            connecting = "local"
            self.MQTTReset.clicked.connect(self.try_connect_client_main)

        try:
            client = self.connect_client(config)
            status.setText("Connected")
            return client
        except (ConnectionRefusedError, socket.gaierror):
            QMessageBox.critical(self, "Error", "Could not connect to the " + connecting +
                                 " MQTT broker.\nPlease check your settings.")
        except socket.timeout:
            QMessageBox.critical(self, "Error", "Could not connect to the MQTT broker.\nYou are offline.")
        status.setText("Not connected")

    def load_agents_from_config(self):
        for section in self.config.sections():
            if section.startswith("Tag:"):
                options = RobotOptions(self.config[section]["address"], self.config[section]["guid"],
                                       QColor(self.config[section]["colour"]))
                self.agents[section[len("Tag:"):]] = options

    def load_ui(self) -> None:
        uic.loadUi("mainwindow.ui", self)
        # load the map into the window
        self.pozyx_map.map = self.map
        self.pozyx_map.coordinates = self.coordinates
        self.pozyx_map.setup()
        self.pozyx_map.contextMenuRequested.connect(self.generate_context_menu)
        self.setWindowIcon(QIcon("uwbmap/pictures/icon.jpg"))
        self.buttonReset.clicked.connect(self.reset_map)
        self.buttonResendCharging.clicked.connect(self.resend_charging_stations)
        self.buttonRemoveCharging.clicked.connect(self.remove_charging_stations)

        # open the two files of the physical and digital agents
        self.button_file1.clicked.connect(self.get_physical_agent_file)
        self.button_file2.clicked.connect(self.get_digital_agent_file)
        # when the results button is clicked, the display results method is called
        self.button_results.clicked.connect(self.display_results)

    def generate_context_menu(self, event: QGraphicsSceneMouseEvent):
        # add either charging station, or destination point
        menu = QMenu()
        action = menu.addAction("Add charging station")
        action.setIcon(QIcon('uwbmap/pictures/charging.png'))
        action.triggered.connect(lambda: self.send_charging_station(event.scenePos()))
        menu.addSection("Send destination to agent")
        for tag in self.tags.values():
            if tag.guid != "" and not tag.dual_agent:
                action = menu.addAction(tag.guid)
                action.setIcon(QIcon('uwbmap/pictures/destination.svg'))
                action.triggered.connect(lambda chosen, tag_=tag: self.send_destination_save(tag_, event.scenePos()))
        menu.exec(event.screenPos())

    def reset_map(self):
        for tag in self.tags.values():
            tag.reset()

    def resend_charging_stations(self):
        for station in self.charging_stations:
            self.send_charging_station(station, True)

    def remove_charging_stations(self):
        for agent in [tag for tag in self.tags.values() if not tag.dual_agent]:
            agent.drop_charging_stations()
        self.charging_stations = []
        self.pozyx_map.remove_charging_stations()

    def send_destination_save(self, tag: Robot, destination: QPointF) -> None:
        try:
            tag.send_destination(destination)
            self.pozyx_map.set_robot_destination(tag, destination)
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", "Local JADE agent container is not running. Please start it first.")

    def send_charging_station(self, station: QPointF, resend=False) -> None:
        try:
            for tag in [tag for tag in self.tags.values() if not tag.dual_agent]:
                tag.send_charging_station(station)
            if not resend:
                self.pozyx_map.add_charging_station(station)
                self.charging_stations.append(station)
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", "Local JADE agent container is not running. Please start it first.")

    def connect_client(self, config: configparser.SectionProxy) -> MQTTClient:
        client = MQTTClient(self, host=config["host"], port=config.getint("port"), topic=config["topic"],
                            username=config.get("username", None), password=config.get("password", None),
                            websockets=config.getboolean("websockets", False))
        client.stateChanged.connect(self.on_stateChanged, )
        client.messagepyqtSignal.connect(self.on_messagepyqtSignal, )
        client.connected.connect(self.on_connect, )
        return client

    @pyqtSlot()
    def on_connect(self):
        self.textMQTTLog.append("Client Connected")
        # create the csv, and it's header
        # file_header = ['timestamp', 'x', 'y']
        # self.write_csv_header(file_header)

    @pyqtSlot(int)
    def on_stateChanged(self, state):
        pass

    @pyqtSlot(str)
    def on_messagepyqtSignal(self, msg):
        self.textMQTTLog.append(msg)
        for message in json.loads(msg):
            try:
                tag = message["tagId"]
                x_coordinate = round(message["data"]["coordinates"]["x"] / self.smoothing) * self.smoothing
                y_coordinate = round(message["data"]["coordinates"]["y"] / self.smoothing) * self.smoothing
                # self.write_csv([datetime.now().time(), x_coordinate, y_coordinate])
                # if tag == "27253":
                #     print(message["tagId"], x_coordinate, y_coordinate)
                if tag in self.tags:
                    self.pozyx_map.update_tag_position(self.tags[tag], QPointF(x_coordinate, y_coordinate))
                else:
                    new_tag = Robot(QPointF(x_coordinate, y_coordinate), tag, dual_agent=tag[-5:] == "_dual")
                    self.tags[tag] = new_tag
                    self.pozyx_map.add_tag(new_tag)
                    self.layoutTabAgents.addWidget(AgentSummary(new_tag, self.layoutTabAgents))
                    new_tag.dualAgentDeleted.connect(self.on_dual_agent_deleted)
                    if tag in self.agents:
                        print("Agent Exists: ", tag)
                        new_tag.update_options(self.agents[tag])
            except KeyError:
                pass

    @pyqtSlot(str)
    def on_dual_agent_deleted(self, tag_id: str):
        dual_agent = self.tags[tag_id + "_dual"]
        dual_agent.delete()
        del self.tags[tag_id + "_dual"]

    def closeEvent(self, event):
        if self.client is not None:
            self.client.stop_receiving()
            self.client.disconnectFromHost()
        if self.client_local is not None:
            self.client_local.stop_receiving()
            self.client_local.disconnectFromHost()
        for tag in self.tags.values():
            if tag.has_dual_agent:
                try:
                    Agent().delete_dual_agent(tag.guid, tag.address)
                except (Py4JNetworkError, Py4JError):
                    pass
        event.accept()
        save_config("config.ini", self.config, self.tags)
        event.accept()

    # function to check the message received from the physical agents
    def check_agent_messages(self):
        try:
            new_messages = Agent().check_agent_messages()
            # if the connection with JADE is lost and restored, then show this message
            if self.connection_dialog_shown: self.connection_restored()
            self.connection_dialog_shown = False
        except Py4JNetworkError:
            # if the connection with JADE is lost, then show this message just once
            if not self.connection_dialog_shown: self.connection_lost()
            self.connection_dialog_shown = True

            return
        for new_message in new_messages:
            # guid_addr = new_message[:2]
            # message = new_message[2]
            # agent_name = ""
            # if guid_addr in self.cached_dict:
            #     agent_name = self.cached_dict[guid_addr]
            # else:
            #     for tag in self.tags.values():
            #         print(str([tag.guid, tag.address])+str(guid_addr))
            #         if [tag.guid, tag.address] == guid_addr:
            #             agent_name = tag.name
            #             self.cached_dict[guid_addr] = agent_name
            #             break
            # if agent_name != "":
            self.textAgentMessages.append(new_message[0] + ": " + new_message[2])

    def connection_lost(self):
        connection_lost_dialog_msg = QMessageBox(self)
        connection_lost_dialog_msg.setWindowTitle("Connection Error")
        connection_lost_dialog_msg.setText("There is no connection with JADE platform!")
        connection_lost_dialog_msg.exec()

    def connection_restored(self):
        connection_restored_dialog_msg = QMessageBox(self)
        connection_restored_dialog_msg.setWindowTitle("Connect")
        connection_restored_dialog_msg.setText("Connection with JADE platform has been restored!")
        connection_restored_dialog_msg.exec()

    def display_results(self):
        if self.physical_agent_file and self.digital_agent_file:
            ResultTable.calculate_similarity(self, self.physical_agent_file, self.digital_agent_file)
            ResultTable.display_table(self)
        else:
            error_dialog_msg = QMessageBox(self)
            error_dialog_msg.setWindowTitle("Empty Files")
            error_dialog_msg.setText("Physical and Digital Files Are Empty, Please choose the files and then click on "
                                     "the results button!")
            error_dialog_msg.exec()

    def get_physical_agent_file(self):
        filename = QFileDialog.getOpenFileName(self, 'Open Physical Agent Coordinates File', 'c:\\temp',
                                               "CSV files (*.csv *.txt)")
        if filename:
            # get the first part of the string
            filename = str(filename[0])
            self.physical_agent_file = filename
        print(self.physical_agent_file)

    def get_digital_agent_file(self):
        filename = QFileDialog.getOpenFileName(self, 'Open Digital Agent Coordinates File', 'c:\\temp',
                                               "CSV files (*.csv *.txt)")
        if filename:
            # get the first part of the string
            filename = str(filename[0])
            self.digital_agent_file = filename
        print(self.digital_agent_file)

    def write_csv_file(self, data):
        with open('coordinates.csv', 'a') as outfile:
            writer = csv.writer(outfile)
            writer.writerow(data)

    def write_header_csv_file(self, header):
        with open('coordinates.csv', 'a') as outfile:
            writer = csv.writer(outfile)
            writer.writerow(header)
