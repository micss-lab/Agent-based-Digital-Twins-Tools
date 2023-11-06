from typing import Optional

import paho.mqtt.client as mqtt
from PyQt6.QtCore import QObject, pyqtSignal, pyqtProperty, pyqtSlot


class MQTTClient(QObject):
    Disconnected = 0
    Connecting = 1
    Connected = 2

    MQTT_3_1 = mqtt.MQTTv31
    MQTT_3_1_1 = mqtt.MQTTv311

    connected = pyqtSignal()
    disconnected = pyqtSignal()

    stateChanged = pyqtSignal(int)
    hostnameChanged = pyqtSignal(str)
    portChanged = pyqtSignal(int)
    keepAliveChanged = pyqtSignal(int)
    cleanSessionChanged = pyqtSignal(bool)
    protocolVersionChanged = pyqtSignal(int)
    messagepyqtSignal = pyqtSignal(str)

    def __init__(self, parent=None, host: Optional[str] = None, port: Optional[int] = None, topic: Optional[str] = None,
                 username: Optional[str] = None, password: Optional[str] = None, websockets: bool = False):

        super(MQTTClient, self).__init__(parent)
        self._state = MQTTClient.Disconnected
        self.m_cleanSession = True
        self.m_protocolVersion = MQTTClient.MQTT_3_1
        self.topic = topic

        if websockets:
            self._client = mqtt.Client(transport="websockets")
        else:
            self._client = mqtt.Client()

        self._client.on_connect = self.on_connect
        self._client.on_message = self.on_message
        self._client.on_disconnect = self.on_disconnect

        if username is not None and password is not None:
            self._client.tls_set()
            self.set_credentials(username, password)

        if host is not None and port is not None:
            self.connect_client(host, port)

    @pyqtProperty(int, notify=stateChanged)
    def state(self):
        return self._state

    @state.setter
    def state(self, state):
        if self._state == state:
            return
        self._state = state
        self.stateChanged.emit(state)

    def connect_client(self, host: str, port: int):
        self._client.connect(host, port)
        self.state = MQTTClient.Connecting
        self._client.loop_start()

    def set_credentials(self, username: str, password: str):
        self._client.username_pw_set(username, password)

    @pyqtSlot()
    def disconnectFromHost(self):
        self._client.disconnect()

    def subscribe(self, path):
        if self._state == MQTTClient.Connected:
            self._client.subscribe(path)

    def stop_receiving(self):
        self._client.loop_stop(True)

    #################################################################
    # callbacks
    def on_message(self, mqttc, obj, msg):
        string_msg = msg.payload.decode("ascii")
        self.messagepyqtSignal.emit(string_msg)
        # if self.topic == "tags": print("New Message: ", string_msg)

    def on_connect(self, *args):
        self.state = MQTTClient.Connected
        self._client.subscribe(self.topic)
        self.connected.emit()

    def on_disconnect(self, *args):
        self.state = MQTTClient.Disconnected
        self.disconnected.emit()
