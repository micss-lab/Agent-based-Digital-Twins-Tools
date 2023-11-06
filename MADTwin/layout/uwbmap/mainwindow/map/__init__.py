from typing import List

from PyQt6.QtCore import Qt, QPointF, QLineF, QRectF, pyqtSignal
from PyQt6.QtGui import QPen, QPainter, QImage, QPixmap
from PyQt6.QtWidgets import QGraphicsView, QGraphicsSceneMouseEvent, QGraphicsScene, \
    QWidget, QLabel, QGraphicsLineItem, QGraphicsPixmapItem, QGraphicsRectItem

from uwbmap.mainwindow.map.robot import Robot
from uwbmap.mainwindow.map.robot_dialog import RobotDialog


class ItemClickableGraphicsScene(QGraphicsScene):
    itemClicked = pyqtSignal(Robot)


class PozyxMap(QWidget):
    contextMenuRequested = pyqtSignal(QGraphicsSceneMouseEvent)

    def __init__(self, parent):
        super(PozyxMap, self).__init__(parent)
        self._zoom = None
        self.floorplan = None
        self.scene = None
        self.map: QGraphicsView = None
        self.coordinates: QLabel = None
        self.charging_stations: List[QGraphicsRectItem] = []

    def setup(self):
        self.scene = ItemClickableGraphicsScene()
        self.scene.itemClicked.connect(self.tagClicked)
        self.map.setRenderHint(QPainter.RenderHint.Antialiasing)
        self.floorplan = QGraphicsPixmapItem()
        self.scene.addItem(self.floorplan)
        self.map.setScene(self.scene)
        self.map.setTransformationAnchor(QGraphicsView.ViewportAnchor.AnchorUnderMouse)
        self.map.setResizeAnchor(QGraphicsView.ViewportAnchor.AnchorUnderMouse)
        self._zoom = 0
        self.map.setDragMode(QGraphicsView.DragMode.ScrollHandDrag)
        self.map.setMouseTracking(True)
        self.scene.mouseMoveEvent = self.mouseMoveEventScene
        self.scene.mousePressEvent = self.mousePressEventScene
        self.map.wheelEvent = self.wheelEvent
        self.floorplan.setPixmap(QPixmap("uwbmap/pictures/floor_plan.png").scaledToHeight(20046))
        self.floorplan.setPos(0, 0)
        self.floorplan.setZValue(-2)

    def add_tag(self, tag: Robot):
        self.scene.addItem(tag.graphics)

    def add_charging_station(self, pos: QPointF):
        # set the shape attributes of the charging stations
        self.charging_stations.append(QGraphicsRectItem(pos.x() - 150, pos.y() - 150, 200, 200))
        brush = Qt.GlobalColor.green
        self.charging_stations[-1].setBrush(brush)
        self.scene.addItem(self.charging_stations[-1])

    def remove_charging_stations(self):
        for station in self.charging_stations:
            self.scene.removeItem(station)
        self.charging_stations = []

    def update_tag_position(self, tag: Robot, new_center_loc: QPointF):
        cur_center_loc = tag.graphics.get_centerpos()
        if cur_center_loc != new_center_loc:
            line = QGraphicsLineItem(QLineF(cur_center_loc, new_center_loc))
            pen = QPen()
            pen.setBrush(tag.graphics.colour)
            pen.setWidth(60)
            line.setPen(pen)
            self.scene.addItem(line)
            tag.graphics.path.append(line)
            tag.graphics.set_centerpos(new_center_loc)

    def set_robot_destination(self, tag: Robot, pos: QPointF):
        line1 = QGraphicsLineItem(QLineF(pos.x() - 100, pos.y(), pos.x() + 100, pos.y()))
        line2 = QGraphicsLineItem(QLineF(pos.x(), pos.y() - 100, pos.x(), pos.y() + 100))
        pen = QPen()
        pen.setWidth(60)
        pen.setBrush(tag.graphics.colour)
        line1.setPen(pen)
        line2.setPen(pen)
        tag.graphics.destinations.append((line1, line2))
        line1.setZValue(+10)
        line2.setZValue(+10)
        self.scene.addItem(line1)
        self.scene.addItem(line2)

    def fitInView(self, scale=True):
        rect = QRectF(self.floorplan.pixmap().rect())
        if not rect.isNull():
            self.map.setSceneRect(rect)
            unity = self.map.transform().mapRect(QRectF(0, 0, 1, 1))
            self.map.scale(1 / unity.width(), 1 / unity.height())
            viewrect = self.map.viewport().rect()
            scenerect = self.map.transform().mapRect(rect)
            factor = min(viewrect.width() / scenerect.width(),
                         viewrect.height() / scenerect.height())
            self.map.scale(factor, factor)
            self._zoom = 0

    def wheelEvent(self, event):
        if event.angleDelta().y() > 0:
            factor = 1.1
            self._zoom += 1
        else:
            factor = 0.9
            self._zoom -= 1
        if self._zoom > 0:
            self.map.scale(factor, factor)
        elif self._zoom == 0:
            self.fitInView()
        else:
            self._zoom = 0

    def mouseMoveEventScene(self, event: QGraphicsSceneMouseEvent):
        self.coordinates.setText("x: " + str(event.scenePos().x()) + ", y:" + str(event.scenePos().y()))

    def mousePressEventScene(self, event: QGraphicsSceneMouseEvent):
        if event.button() == Qt.MouseButton.RightButton:
            self.contextMenuRequested.emit(event)
        elif event.button() == Qt.MouseButton.MiddleButton:
            img = QImage(self.floorplan.pixmap().rect().height(), self.floorplan.pixmap().rect().width(),
                         QImage.Format.Format_ARGB32_Premultiplied)
            painter = QPainter(img)
            self.scene.render(painter)
            painter.end()
            img.save("uwbmap/pictures/test.png")

    def tagClicked(self, tag: Robot):
        dialog = RobotDialog(tag)
        new_options = dialog.exec()
        if new_options is not None:
            tag.update_options(new_options)
