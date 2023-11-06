from typing import Optional

from PyQt6 import uic
from PyQt6.QtGui import QIcon
from PyQt6.QtWidgets import QDialog, QLineEdit, QPushButton, QColorDialog, QMessageBox
from py4j.protocol import Py4JNetworkError

from uwbmap.mainwindow.map.robot import Robot, RobotOptions


class RobotDialog(QDialog):
    CONTAINERERROR = "Local JADE agent container is not running. Please start it first."

    def __init__(self, tag: "Robot", parent=None):
        self.editAddress: QLineEdit = None
        self.editGUID: QLineEdit = None
        self.buttonColour: QPushButton = None
        self.buttonStart: QPushButton = None
        self.buttonStop: QPushButton = None
        self.buttonDualAgent: QPushButton = None
        self.buttonReset: QPushButton = None
        self.buttonLowBattery: QPushButton = None

        self.tag: Robot = tag
        self.chosen_options = RobotOptions(tag.address, tag.guid, tag.graphics.colour)

        super(RobotDialog, self).__init__(parent)
        uic.loadUi("tagdialog.ui", self)
        self.setWindowTitle("Tag: " + tag.name)
        self.editAddress.setText(str(tag.address))
        self.editAddress.textEdited.connect(self.on_address_edited)
        self.editGUID.setText(str(tag.guid))
        self.editGUID.textEdited.connect(self.on_guid_edited)
        rgb_colour = "rgb(" + ", ".join([str(value) for value in tag.graphics.colour.getRgb()[:-1]]) + ")"
        self.buttonColour.setStyleSheet("background-color: " + rgb_colour)
        self.buttonColour.clicked.connect(self.on_colour_clicked)
        self.buttonStart.clicked.connect(self.start_tag)
        self.buttonStart.setIcon(QIcon('uwbmap/pictures/start.png'))
        self.buttonStop.clicked.connect(self.stop_tag)
        self.buttonStop.setIcon(QIcon('uwbmap/pictures/stop.png'))
        self.buttonReset.clicked.connect(self.reset_tag)
        self.buttonReset.setIcon(QIcon('uwbmap/pictures/reset.png'))
        self.buttonLowBattery.clicked.connect(self.send_low_battery)
        self.buttonLowBattery.setIcon(QIcon('uwbmap/pictures/battery.svg'))
        if not tag.has_dual_agent:
            # add digital agent
            self.buttonDualAgent.clicked.connect(self.create_dual_agent)
            self.buttonDualAgent.setIcon(QIcon('uwbmap/pictures/create.svg'))
        else:
            # remove digital agent
            self.buttonDualAgent.setText("Remove dual agent")
            self.buttonDualAgent.clicked.connect(self.delete_dual_agent)
            self.buttonDualAgent.setIcon(QIcon('uwbmap/pictures/remove.svg'))

        if tag.dual_agent:
            self.buttonStart.hide()
            self.buttonStop.hide()
            self.editAddress.setDisabled(True)
            self.editGUID.setDisabled(True)
            self.buttonDualAgent.hide()
            self.buttonLowBattery.hide()

    def start_tag(self):
        try:
            self.tag.start()
            print("Start ==> ", self.tag.guid)
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", RobotDialog.CONTAINERERROR)

    def stop_tag(self):
        try:
            self.tag.stop()
            print("Stop ==> ", self.tag.guid)
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", RobotDialog.CONTAINERERROR)

    def reset_tag(self):
        self.tag.reset()

    def create_dual_agent(self):
        try:
            self.tag.create_dual_agent()
            print("Create Digital Agent ==> ", self.tag.guid+"_dual")
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", RobotDialog.CONTAINERERROR)

    def delete_dual_agent(self):
        try:
            self.tag.delete_dual_agent()
            print("Remove Digital Agent ==> ", self.tag.guid+"_dual")
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", RobotDialog.CONTAINERERROR)

    def send_low_battery(self):
        try:
            self.tag.send_low_battery()
            print("Sending low battery message ==> ", self.tag.guid)
        except Py4JNetworkError:
            QMessageBox.critical(self, "Error", RobotDialog.CONTAINERERROR)

    def on_colour_clicked(self):
        dialog = QColorDialog()
        dialog.colorSelected.connect(self.on_colour_selected)
        dialog.exec()

    def on_address_edited(self, text):
        self.chosen_options.address = text

    def on_guid_edited(self, text):
        self.chosen_options.guid = text

    def on_colour_selected(self, colour):
        rgb_colour = "rgb(" + ", ".join([str(value) for value in colour.getRgb()[:-1]]) + ")"
        self.buttonColour.setStyleSheet("background-color: " + rgb_colour)
        self.chosen_options.colour = colour

    def exec(self) -> Optional[RobotOptions]:
        if super(RobotDialog, self).exec():
            return self.chosen_options
        else:
            return None
