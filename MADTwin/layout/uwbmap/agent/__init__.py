from typing import List

from py4j.java_gateway import JavaGateway


class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class NormalAgent:
    def __init__(self):
        self.gateway = JavaGateway()
        self._agent = self.gateway.entry_point

    def close_gateway(self) -> None:
        self.gateway.close()

    def send_start(self, guid: str, address: str) -> None:
        self._agent.sendStartMessage(guid, address)

    def send_stop(self, guid: str, address: str) -> None:
        self._agent.sendStopMessage(guid, address)

    def send_position(self, guid: str, address: str, x: int, y: int) -> None:
        self._agent.sendLocationMessage(guid, address, x, y)

    def send_charging_station(self, guid: str, address: str, x: int, y: int) -> None:
        self._agent.sendChargingStationMessage(guid, address, x, y)

    def send_low_battery(self, guid: str, address: str) -> None:
        self._agent.sendLowBatteryMessage(guid, address)

    def drop_charging_stations(self, guid: str, address: str) -> None:
        self._agent.sendDropChargingStationsMessage(guid, address)

    def create_dual_agent(self, guid: str, address: str, tag_id: str, x: int, y: int) -> None:
        self._agent.createDualAgent(guid, address, tag_id, x, y)

    def delete_dual_agent(self, guid: str, address: str) -> None:
        self._agent.deleteDualAgent(guid, address)

    def check_agent_messages(self) -> List[List[str]]:
        return self._agent.getNewMessages()

    def shutdown(self) -> None:
        self.close_gateway()


class Agent(NormalAgent, metaclass=Singleton):
    pass
