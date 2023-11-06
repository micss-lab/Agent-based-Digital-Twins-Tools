package robot.bdi;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import helpers.CSVFile;
import helpers.Delay;
import helpers.Movement;
import helpers.Point2D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class ChargePlanBody extends MovePlanBody {
    @Belief
    static TransientBeliefSet<String, Point2D> charging_locations;

    @Belief
    static TransientBelief<String, Robot.BatteryLevel> battery_level;

    @Override
    public void onStart() {
        movement = ((Robot) myAgent).movement;
        destination = this.shortestDistanceChargingStation();
        if (destination == null) {
            System.out.println("No charging point found");
        } else {
            System.out.println("==> Start moving to charging station");
            printMission(movement.getLocation(), destination, movement.getAngle());
            sendChargingMissionToPhysicalAgent(destination);
            CSVFile.createCSVFile(myAgent.getName());
        }
    }

    @Override
    public int onEnd() {
        System.out.println("Charging");
        Delay.msSleep(10000);
        battery_level.setValue(Robot.BatteryLevel.HIGH);
        System.out.println("Charged");
        sendingBatteryMessage();
        return super.onEnd();
    }

    private void sendChargingMissionToPhysicalAgent(Point2D destinationPoint) {
        // retrieve the movement belief of the robot
        System.out.println("=========================================");
        Movement movement = (Movement) getBeliefBase().getBelief("current_movement").getValue();
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        String physicalAgentName = myAgent.getName().split("_dual")[0];
        msg.addReceiver(new AID(physicalAgentName, AID.ISLOCALNAME));
        msg.setOntology("source_target_line_string");
        msg.setConversationId("line_string");
        // sending path from current location to charging destination line string
        Coordinate sourceCoordinate = new Coordinate(movement.getLocation().x, movement.getLocation().y);
        Coordinate targetCoordinate = new Coordinate(destinationPoint.x, destinationPoint.y);
        Coordinate[] sourceTargetCoordinates = new Coordinate[]{sourceCoordinate, targetCoordinate};
        LineString pathLineString = new GeometryFactory().createLineString(sourceTargetCoordinates);
        System.out.println("Sending Path [C]: " + pathLineString);
        System.out.println("=========================================");
        msg.setContent(pathLineString.toString());
        myAgent.send(msg);
    }

    private void sendingBatteryMessage() {
        if (battery_level.getValue() == Robot.BatteryLevel.LOW) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//            msg.addReceiver(control_center_aid.getValue());
            msg.addReceiver(myAgent.getAID());
            switch (battery_level.getValue()) {
                case LOW:
                    msg.setContent("low_battery");
                    break;
                case MEDIUM:
                    msg.setContent("medium_battery");
                    break;
                case HIGH:
                    msg.setContent("high_battery");
                    break;
            }
            myAgent.send(msg);
        }
    }

    private Point2D shortestDistanceChargingStation() {
        Point2D shortest_point = null;
        double shortest_distance = Double.MAX_VALUE;
        for (Point2D point : charging_locations.getValue()) {
            double distance = movement.getLocation().dist(point);
            if (distance < shortest_distance) {
                shortest_distance = distance;
                shortest_point = point;
            }
        }
        return shortest_point;
    }

    public static void printMission(Point2D source, Point2D destination, int angle) {
        //Updating the robot's pose, and mission
        System.out.println("=========================================");
        System.out.println("==> Current Angle: " + angle);
        System.out.println("==> Robot Source: " + "[X=" + source.x + ", Y=" + source.y + "]");
        System.out.println("==> Robot Destination: " + "[X=" + destination.x + ", Y=" + destination.y + "]");
        System.out.println("==> Distance to Destination: " + destination.dist(source));
        System.out.println("=========================================");

    }

}
