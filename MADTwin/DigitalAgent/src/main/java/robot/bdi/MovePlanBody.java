package robot.bdi;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import helpers.CSVFile;
import helpers.Delay;
import helpers.Movement;
import helpers.Point2D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import mqtt.TagMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import java.io.IOException;
import java.sql.Timestamp;

public class MovePlanBody extends AbstractPlanBody {
    private Logger log = Logger.getMyLogger(getClass().getName());
    protected Movement movement;
    protected Point2D destination;
    protected Point2D prev_location;
    protected int current_angle;
    protected int target_angle;
    protected Timestamp timestamp;

    @Belief
    static TransientBelief<String, Robot.Status> status;
    static Point2D cur_location;
    @Override
    public void onStart() {
        super.onStart();
        movement = ((Robot) myAgent).movement;
        destination = ((MovementCapability.DestinationGoal) getGoal()).getDestination();
        System.out.println("=========================================");
        System.out.println("==> Start moving to mission station");
        target_angle= (int) calculateAngle(movement.getLocation(), destination);
        printMission(movement.getLocation(), destination, movement.getAngle(), target_angle);
        sendMissionPhysicalAgent(destination);
        CSVFile.createCSVFile(myAgent.getName());
        // wait time for physical robot finishes its rotation
        Delay.msSleep(3000);
    }

    @Override
    public void action() {
         cur_location = movement.getLocation();
         //update the robots' movement belief
         getBeliefBase().updateBelief("current_movement", movement);
        if (destination == null) {
            System.out.println("No destination found");
            setEndState(Plan.EndState.FAILED);
            return;
        }
        if (status.getValue() == Robot.Status.STOP) {
            System.out.println("Movement stopped, destination: " + destination);
            setEndState(Plan.EndState.FAILED);
            return;
        }
        if (prev_location != null && status.getValue() != Robot.Status.STOP) {
            current_angle= (int) calculateAngle(cur_location, destination);
            movement.setAngle(current_angle);
            timestamp = new Timestamp(System.currentTimeMillis());
            String[] missionInformation = {String.valueOf(timestamp), String.valueOf(movement.getLocation().x), String.valueOf(movement.getLocation().y)};
            try {
                CSVFile.updateCSV(missionInformation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        movement.forward(10);
        try {
            TagMqtt tag = ((Robot) myAgent).tag;
            // send current location updates of the digital agent to the python side
            tag.sendLocation(movement.getLocation());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            checkDone();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prev_location = cur_location;
    }

    protected void checkDone() throws IOException {
        if (destination.dist(movement.getLocation()) < 10) {
            System.out.println("==> Simulation agent arrived to the destination: " + movement.getLocation() + " Distance: " + destination.dist(movement.getLocation()));
            System.out.println("==> Current robot angle: " + current_angle);
            setEndState(Plan.EndState.SUCCESSFUL);
            CSVFile.closeCSVFile();
        }
        block(100);
    }


    private void sendMissionPhysicalAgent(Point2D destinationPoint){
        // retrieve the movement belief of the robot
        Movement movement= (Movement) getBeliefBase().getBelief("current_movement").getValue();
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        String physicalAgentName=myAgent.getName().split("_dual")[0];
        msg.addReceiver(new AID(physicalAgentName, AID.ISLOCALNAME));
        msg.setOntology("source_target_line_string");
        msg.setConversationId("line_string");
        // sending path from current location to charging destination line string
        Coordinate sourceCoordinate= new Coordinate(movement.getLocation().x, movement.getLocation().y);
        Coordinate targetCoordinate= new Coordinate(destinationPoint.x, destinationPoint.y);
        Coordinate[] sourceTargetCoordinates = new Coordinate[] {sourceCoordinate, targetCoordinate};
        LineString pathLineString = new GeometryFactory().createLineString(sourceTargetCoordinates);
        System.out.println("Sending Path [D]: " + pathLineString);
        msg.setContent(pathLineString.toString());
        myAgent.send(msg);
    }

    public static double calculateAngle(Point2D sourcePt, Point2D targetPt) {
        // default direction of the vector is towards the +X axis
        // calculate the angle theta from the deltaY and deltaX values
        // The initialization of the robot should be always towards the X axis
        double radian = Math.atan2(targetPt.y - sourcePt.y, targetPt.x - sourcePt.x);
        double calculatedAngle = radian * 180 / Math.PI;
        return calculatedAngle;
    }

    public static void printMission(Point2D source, Point2D destination, int currentAngle, int targetAngle) {
        //Prints the robot's pose, and the mission
        System.out.println("==============Print Mission==============");
        System.out.println("==> Current Angle: " + currentAngle);
        System.out.println("==> Target Angle: " + targetAngle);
        System.out.println("==> Robot Source: " + "[X=" + source.x + ", Y=" + source.y + "]");
        System.out.println("==> Robot Destination: " + "[X=" + destination.x + ", Y=" + destination.y + "]");
        System.out.println("==> Distance to Destination: " + destination.dist(source));
        System.out.println("=========================================\n");

    }

}
