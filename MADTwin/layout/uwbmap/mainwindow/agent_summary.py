from PyQt6.QtWidgets import QWidget, QVBoxLayout, QLabel, QFormLayout
from PyQt6.QtCore import Qt
from PyQt6.QtGui import QMouseEvent

from uwbmap.mainwindow.map import Robot, RobotDialog


class AgentSummary(QWidget):
    def __init__(self, agent: Robot, layout, parent=None):
        super(AgentSummary, self).__init__(parent)
        self.agent = agent
        vertical_layout = QVBoxLayout()
        self.tagIDLabel = QLabel(agent.name)
        self.tagIDLabel.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.tagIDLabel.setStyleSheet("background-color: " + self.agent.graphics.getRGB_string())
        self.GUIDLabel = QLabel(self.agent.guid)
        self.addressLabel = QLabel(self.agent.address)
        vertical_layout.addWidget(self.tagIDLabel)
        form_layout = QFormLayout()
        form_layout.addRow("GUID:", self.GUIDLabel)
        form_layout.addRow("Address:", self.addressLabel)
        vertical_layout.addLayout(form_layout)
        self.setLayout(vertical_layout)
        self.agent.optionsChanged.connect(self.update_labels)
        self.agent.agentDeleted.connect(self.delete)
        self.layout = layout

    def mouseDoubleClickEvent(self, a0: QMouseEvent) -> None:
        dialog = RobotDialog(self.agent)
        new_options = dialog.exec()
        if new_options is not None:
            self.agent.update_options(new_options)

    def update_labels(self):
        self.tagIDLabel.setText(self.agent.name)
        self.tagIDLabel.setStyleSheet("background-color: " + self.agent.graphics.getRGB_string())
        self.GUIDLabel.setText(self.agent.guid)
        self.addressLabel.setText(self.agent.address)

    def delete(self):
        self.layout.removeWidget(self)
        self.deleteLater()
        self.hide()
