package robot.bdi;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.MultipleCapabilityAgent;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalStatus;
import bdi4jade.message.MessageGoal;
import helpers.Movement;
import helpers.Point2D;
import mqtt.TagMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class Robot extends MultipleCapabilityAgent {
    Logger logger = Logger.getLogger(Robot.class.getName());

    TagMqtt tag;
    Movement movement;

    @Belief
    static TransientBelief<String, Status> status = new TransientBelief<>("status", Status.STOP);
    private MovementCapability.DestinationGoals destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
    private MovementCapability.DestinationGoals new_destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());

    public enum Status {
        IDLE,
        START,
        RUNNING,
        STOP,
    }

    public enum BatteryLevel {
        LOW,
        MEDIUM,
        HIGH,
    }

    protected void init() {
        logger.info("Digital Agent Initialized: " + getAID().getName());
        System.out.println("Agent addresses are " + String.join(",", getAID().getAddressesArray()));
        Object[] args = getArguments();
        try {
            System.out.println("args[0].toString() = " + args[0].toString());
            System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
            tag = new TagMqtt(args[0].toString());
            movement = new Movement(new Point2D(Integer.parseInt(args[1].toString()), Integer.parseInt(args[2].toString())), 0);
            //send the current location to the interface when the digital agent is created
            tag.sendLocation(movement.getLocation());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        addCapability(new MovementCapability(status, movement));
        addGoalListener(new DestinationGoalListener());
    }


    public class DestinationGoalListener implements GoalListener {
        @Override
        public void goalPerformed(GoalEvent goalEvent) {
            if (goalEvent.getGoal().getClass() == MovementCapability.DestinationGoal.class &&
                    goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                System.out.println("=========================================");
                System.out.println("==> Destination Goal Succeeded");
                System.out.println("=========================================");
                if (new_destinations.getGoals().size() > 0) {
                    destinations = new MovementCapability.DestinationGoals((List<Goal>) new_destinations.getGoals());
                    new_destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
                    addGoal(destinations);
                    System.out.println("==> New goals added: " + destinations.getGoals().size());
                } else {
                    // this will ensure that after reaching the destination, that stop message should be sent again
//                    status.setValue(Status.IDLE);
//                    System.out.println("Status to: "+status.getValue());
                    destinations.getGoals().clear();
                }
            } else if (goalEvent.getGoal().getClass() == MessageGoal.class &&
                    goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                System.out.println("=========================================");
                System.out.println("==> Message Goal Succeeded");
                System.out.println("=========================================");

            }
        }
    }


}
