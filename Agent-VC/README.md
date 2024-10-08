# Agent-VC

This documentation provides an overview of a project that combines remote Java with an OPC UA server and Python program for a Visual Components simulation. The project includes code files that control the behavior of a simulated vehicle and manage OPC UA data.

You can download the Visual Components V4.6 from the following link: [Download](https://download.visualcomponents.net/installers/VisualComponents/4.6.0/VisualComponentsPremiumSetup_64.exe)

## Project Structure

The project is organized into three main components:

1. **Python Script for Simulated Vehicle Control**
   - **File**: The Python script inside the VC modeling file.
   - **Description**: This Python script simulates the movement of a vehicle within a virtual environment. It updates the vehicle's position, sets destinations, and provides methods to control its behavior.

2. **Java-Agent OPC UA Server Component**
   - **Files**:
     - `Container.java`
     - `CustomNamespace.java`
     - `RobotAgent.java`
     - `Server.java`
   - **Description**: This Java component creates an OPC UA server with a custom namespace. It registers variables related to destination points and vehicle positions. It also includes logic for coordinating movements within the environment and communicates via OPC UA. The function of the agent is to update the status of the robot regularly by using the ``TickerBehaviour``.


## Integration with Visual Components and Java Dependency Installation

To successfully run this project, follow these additional setup steps:

### 1. Connect Visual Components to OPC UA Server

To enable communication between Visual Components and the OPC UA server, use the Connectivity Plugin provided by Visual Components (Connectivity -> OPC UA). Ensure that you have configured the plugin to establish a connection to the OPC UA server created in the Java server.

### 2. Install Java Dependencies with Maven

To install the Java dependencies required for the Java OPC UA server component, use Maven. Navigate to the Java project directory in your terminal and run the following command:
```bash
cd .\Agent-VC\opcua\
```
Then:
```bash
mvn clean install
```
Finally, you can run the project with the following command:
```bash
mvn -f pom.xml exec:java@ServerMain
```
Also, you can import the project into IntelliJ and run the main class `Server`

This work is licensed under a
[Creative Commons Attribution-ShareAlike 4.0 International License][cc-by-sa].

[![CC BY-SA 4.0][cc-by-sa-image]][cc-by-sa]

[cc-by-sa]: http://creativecommons.org/licenses/by-sa/4.0/
[cc-by-sa-image]: https://licensebuttons.net/l/by-sa/4.0/88x31.png
[cc-by-sa-shield]: https://img.shields.io/badge/License-CC%20BY--SA%204.0-lightgrey.svg
