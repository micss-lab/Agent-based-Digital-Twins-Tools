## Multi-agent Digital Twin Platform (MADTwin)

### JAVA Digital Agent
1. The easiest way is to import the **Digital Agent** project into IntelliJ, then build and run it with Gradle.
2. Or, you can run it directly from PowerShell and using the Gradle building tool by running the following commands:
``` shell
.\gradlew build  
```
Followed by running the project:
``` shell
.\gradlew run
```

### Installing the UI
0. Change the directory to layout (based on the OS you are using)
```shell
cd .\MADTwin\layout\
```
1. Install Poetry
```shell
pip install poetry
```
2. Spawn a new Poetry virtual environment
```shell
poetry shell
```
3. Install the UI
```shell
poetry install
```
4. If you are using PyCharm, make sure you set up the interpreter by adding a poetry environment. Also, keep in mind the current working directory, as it might cause some issues with the files' paths.
5. Install the open-source MQTT broker (Eclipse Mosquitto, [Download Page](https://mosquitto.org/download/)) according to the OS (Windows/Linux/Mac).
6. If you import the project in Pycharm, make sure to use Python version 3.10 and install all the necessary packages.
7. Finally, change the working directory of the project from the menu: Run => Edit Configuration => Working Directory to the ``.\MADTwin\layout``
### Usage
1. Check if the MQTT brokers are configured correctly in the config file and if the mosquitto local host is running (Windows/Linux/Mac). For Windows, you can check **Windows Services**. Then open the CMD as administrator and change the directory to the mosquitto, and run the following command:
``` shell
.\mosquitto.exe -v
```
2. Check if the localhost with port:1883 is open by running the following command:
``` shell
netstat -a
```
3. Start the UI
```shell
python -m uwbmap
```
3. After you initialize the UI, you need to use `Ctrl+scroll wheel` to zoom in and zoom out so the floorplan appears.

4. Demonstration video: [Link](https://youtu.be/btcVBjFgKCQ)

This work is licensed under a
[Creative Commons Attribution-ShareAlike 4.0 International License][cc-by-sa].

[![CC BY-SA 4.0][cc-by-sa-image]][cc-by-sa]

[cc-by-sa]: http://creativecommons.org/licenses/by-sa/4.0/
[cc-by-sa-image]: https://licensebuttons.net/l/by-sa/4.0/88x31.png
[cc-by-sa-shield]: https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg
