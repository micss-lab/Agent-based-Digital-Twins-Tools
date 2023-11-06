package robot.reactive;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import com.opencsv.CSVWriter;
import helpers.Movement;
import helpers.Point2D;
import jade.util.Logger;
import mqtt.TagMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

public class MovePlanBody extends AbstractPlanBody {
    private Logger log = Logger.getMyLogger(getClass().getName());
    protected Movement movement;
    protected Point2D destination;
    protected Point2D prev_location;
    protected int current_angle;
    static Timestamp timestamp;
    static File file;
    static FileWriter output;
    static CSVWriter writer;

    @Belief
    static TransientBelief<String, Robot.Status> status;

    @Override
    public void onStart() {
        super.onStart();
        movement = ((Robot) myAgent).movement;
        log.info("Printing all goals");
        destination = ((MovementCapability.DestinationGoal) getGoal()).getDestination();
        System.out.println("Start Moving to: " + destination);
        // create the csv file to store the coordinates
        file = new File("c:\\temp\\DualAgentCoordinates.csv\\");
        try {
            output = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer = new CSVWriter(output);
        String[] header = {"Timestamp", "X1", "Y1"};
        writer.writeNext(header);
    }

    @Override
    public void action() {
        Point2D cur_location = movement.getLocation();
        if (destination == null) {
            System.out.println("No destination found");
            setEndState(Plan.EndState.FAILED);
            return;
        }
        if (status.getValue() == Robot.Status.STOP) {
            System.out.println("Movement stopped to destination: " + destination);
            setEndState(Plan.EndState.FAILED);
            return;
        }
        if (prev_location != null && status.getValue() != Robot.Status.STOP) {
            current_angle= (int) calculateAngle(cur_location, destination);
//            System.out.println("==> Current Angle: " + current_angle);
//            System.out.println("==> Current Position: " + movement.getLocation());
            movement.setAngle(current_angle);
            // write the coordinates x and y to the csv file
            try {
                timestamp = new Timestamp(System.currentTimeMillis());
                String[] data = {String.valueOf(timestamp), String.valueOf(movement.getLocation().x), String.valueOf(movement.getLocation().y)};
                writer.writeNext(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        movement.forward(15);
        try {
            TagMqtt tag = ((Robot) myAgent).tag;
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
            System.out.println("==> Simulation agent at:  " + destination + " Distance: " + destination.dist(movement.getLocation()));
            System.out.println("==> Calculated Angle: " + current_angle);
            System.out.println("==> Target Destination: " + movement.getLocation());
            setEndState(Plan.EndState.SUCCESSFUL);
            //close the cvs file writer
            writer.close();
        }
        block(100);
    }

    public static double calculateAngle(Point2D sourcePt, Point2D targetPt) {
        // default direction of the vector is towards the +X axis
        // calculate the angle theta from the deltaY and deltaX values
        // the rotation depends on the environment, anyway the rotation always counter-clockwise
        // if we have not used the Math.PI
        double radian = Math.atan2(targetPt.y - sourcePt.y, targetPt.x - sourcePt.x);
        double calculatedAngle = radian * 180 / Math.PI;
        return calculatedAngle;
    }


}
