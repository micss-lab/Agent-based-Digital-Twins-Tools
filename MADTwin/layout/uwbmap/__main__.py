import os
import sys
from PyQt6 import QtWidgets
from uwbmap.mainwindow import MainWindow


def main():
    # Get the current working directory
    cwd = os.getcwd()
    # Print the current working directory
    print("Current working directory: {0}".format(cwd))

    main_application = QtWidgets.QApplication(sys.argv)
    main_window = MainWindow()
    main_window.show()
    sys.exit(main_application.exec())


if __name__ == '__main__':
    main()
