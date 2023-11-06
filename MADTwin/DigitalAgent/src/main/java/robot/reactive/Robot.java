package robot.reactive;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.core.MultipleCapabilityAgent;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.Goal;
import bdi4jade.goal.GoalStatus;
import helpers.Point2D;
import helpers.Movement;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import mqtt.TagMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Robot extends MultipleCapabilityAgent {
    private Logger log = Logger.getMyLogger(getClass().getName());
    TagMqtt tag;
    Movement movement;

    @Belief
    static TransientBelief<String, Status> status = new TransientBelief<>("status", Status.STOP);;

    private MovementCapability.DestinationGoals destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
    private MovementCapability.DestinationGoals new_destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());

    protected void init() {
        System.out.println("Core Agent Controller, GUID Is: " + getAID().getName());
        System.out.println("Agent addresses are " + String.join(",", getAID().getAddressesArray()));
        System.out.println("I'm Ready");
        Object[] args = getArguments();
        try {
            System.out.println("args[0].toString() = " + args[0].toString());
            System.out.println("Arrays.toString(args) = " + Arrays.toString(args));
            tag = new TagMqtt(args[0].toString());
            movement = new Movement(new Point2D(Integer.parseInt(args[1].toString()), Integer.parseInt(args[2].toString())), 0);
            tag.sendLocation(movement.getLocation());
        } catch (MqttException e) {
            e.printStackTrace();
        }
//        addBehaviour(receive_message);
        addCapability(new MovementCapability(status, movement));
        addGoalListener(new DestinationGoalListener());
    }

    CyclicBehaviour receive_message = new CyclicBehaviour() {
        @Override
        public void action() {
            MessageTemplate missionTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("mission_control"));
            ACLMessage msg = receive(missionTemplate);
            if (msg != null) {
                String content = msg.getContent();
                if (content.equals("start")) {
                    checkStart();
                } else if (content.equals("stop")) {
                    dropGoal(destinations);
                    status.setValue(Status.STOP);
                    destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
                    new_destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
                    System.out.println("Status to: STOP");
                }
                else if (content.equals("collision")) {
                    status.setValue(Status.STOP);
                    System.out.println("Status to: STOP");
                }
                else if (content.equals("no_collision")) {
                    checkStart();
                    status.setValue(Status.START);
                    System.out.println("Status to: START");

                }
                else {
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        JSONObject location = jsonObject.getJSONObject("location");
                        int x = location.getInt("x");
                        int y = location.getInt("y");
                        Point2D destination = new Point2D(x, y);
                        System.out.println("Destination: " + destination);
                        if (status.getValue() != Status.RUNNING) {
                            destinations.getGoals().add(new MovementCapability.DestinationGoal(destination));
                        } else {
                            new_destinations.getGoals().add(new MovementCapability.DestinationGoal(destination));
                        }
                        checkStart();
                        System.out.println("======================================\n");
                    } catch (JSONException ignored) {
                    }
                }
                block();
            }
        }
    };

    private void checkStart() {
        if (status.getValue() == Status.START) {
            status.setValue(Status.RUNNING);
            addGoal(destinations);
            System.out.println("Status to: RUNNING, goals: " + destinations.getGoals().size());
        } else if (status.getValue() == Status.STOP) {
            status.setValue(Status.IDLE);
            System.out.println("Status to: IDLE");
        }
    }

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

    public class DestinationGoalListener implements GoalListener {
        @Override
        public void goalPerformed(GoalEvent goalEvent) {
            if (goalEvent.getGoal().getClass() == MovementCapability.DestinationGoals.class &&
                goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                System.out.println("Goal succeeded");
                if(new_destinations.getGoals().size() > 0) {
                    destinations = new MovementCapability.DestinationGoals((List<Goal>) new_destinations.getGoals());
                    new_destinations = new MovementCapability.DestinationGoals(new ArrayList<Goal>());
                    addGoal(destinations);
                    System.out.println("New goals added: " + destinations.getGoals().size());
                }
                else {
                    status.setValue(Status.IDLE);
                    destinations.getGoals().clear();
                    System.out.println("Status to: IDLE");
                }
            }
        }
    }


}
