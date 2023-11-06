import copy
import random
from typing import Optional, List, Tuple

from PyQt6.QtCore import Qt, QPointF, pyqtSignal, QObject
from PyQt6.QtGui import QBrush, QColor, QPen
from PyQt6.QtWidgets import QGraphicsEllipseItem, QGraphicsItem, QGraphicsObject

from uwbmap.agent import Agent


class RobotOptions:
    def __init__(self, address: str, guid: str, colour: QColor):
        self.address = address
        self.guid = guid
        self.colour = colour


class Robot(QGraphicsObject):
    optionsChanged = pyqtSignal()
    dualAgentDeleted = pyqtSignal(str)
    agentDeleted = pyqtSignal()

    def __init__(self, loc: QPointF, tag_id: str, colour: Optional[QColor] = None, size: int = 250,
                 dual_agent: bool = False):
        super().__init__()
        QObject.__init__(self, None)
        self.tag_id = tag_id
        if not dual_agent:
            self.name = str(hex(int(tag_id))) + "(" + str(tag_id) + ")"
        else:
            self.name = tag_id
        self.graphics = RobotGraphics(self, loc, colour, size)
        self.dual_agent = dual_agent
        self.guid = ""
        self.address = ""
        if self.dual_agent:
            self.guid = "Dual Agent"
            self.address = "Dual Agent"
        self.has_dual_agent = False

    def update_options(self, options: RobotOptions):
        self.address = options.address
        self.guid = options.guid
        self.graphics.colour = options.colour
        for line in self.graphics.path + list(sum(self.graphics.destinations, ())):
            pen = line.pen()
            pen.setColor(options.colour)
            line.setPen(pen)
        self.graphics.setBrush(QBrush(self.graphics.colour))
        self.optionsChanged.emit()

    def start(self):
        Agent().send_start(self.guid, self.address)

    def stop(self):
        Agent().send_stop(self.guid, self.address)
        self.graphics.clear_destinations()

    def reset(self):
        self.graphics.reset()

    def delete(self):
        self.graphics.delete()
        self.agentDeleted.emit()

    def create_dual_agent(self):
        Agent().create_dual_agent(self.guid, self.address, self.tag_id,
                                  int(self.graphics.get_centerpos().x()), int(self.graphics.get_centerpos().y()))
        self.has_dual_agent = True

    def delete_dual_agent(self):
        Agent().delete_dual_agent(self.guid, self.address)
        self.has_dual_agent = False
        self.dualAgentDeleted.emit(self.tag_id)

    def send_destination(self, destination: QPointF):
        Agent().send_position(self.guid, self.address, int(destination.x()), int(destination.y()))

    def send_charging_station(self, station: QPointF):
        Agent().send_charging_station(self.guid, self.address, int(station.x()), int(station.y()))

    def drop_charging_stations(self):
        Agent().drop_charging_stations(self.guid, self.address)

    def send_low_battery(self):
        Agent().send_low_battery(self.guid, self.address)


class RobotGraphics(QGraphicsEllipseItem):
    def __init__(self, robot, loc: QPointF, colour: Optional[QColor] = None, size: int = 125):
        QGraphicsEllipseItem.__init__(self, 0, 0, size, size)
        self.robot = robot
        self.setPos(loc.x() - size / 2, loc.y() - size / 2)
        self.size = size
        if colour is None:
            colour = random.choice(list(QColor.colorNames()))
        self.colour = QColor(colour)
        self.setBrush(QBrush(self.colour))
        self.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIsSelectable, True)
        self.path = []
        self.setZValue(10)
        self.destinations: List[Tuple] = []

    def get_centerpos(self) -> QPointF:
        return QPointF(self.pos().x() + self.size / 2, self.pos().y() + self.size / 2)

    def set_centerpos(self, pos: QPointF):
        self.setPos(self.compute_realpos(pos))

    def compute_centerpos(self, pos: QPointF):
        return QPointF(pos.x() + self.size / 2, pos.y() + self.size / 2)

    def compute_realpos(self, pos: QPointF):
        return QPointF(pos.x() - self.size / 2, pos.y() - self.size / 2)

    def getRGB_string(self) -> str:
        return "rgb(" + ", ".join([str(value) for value in self.colour.getRgb()[:-1]]) + ")"

    def mouseDoubleClickEvent(self, event: 'QGraphicsSceneMouseEvent') -> None:
        self.scene().itemClicked.emit(self.robot)

    def clear_destinations(self):
        for line in list(sum(self.destinations, ())):
            self.scene().removeItem(line)
        self.destinations = []

    def clear_path(self):
        for line in self.path:
            self.scene().removeItem(line)
        self.path = []

    def reset(self):
        self.clear_path()
        self.clear_destinations()

    def delete(self):
        self.reset()
        self.scene().removeItem(self)
