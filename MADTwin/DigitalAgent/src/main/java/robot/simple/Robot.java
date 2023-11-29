package robot.simple;

import helpers.Movement;
import helpers.Point2D;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import mqtt.TagMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class Robot extends Agent {
    private Logger log = Logger.getMyLogger(getClass().getName());
    TagMqtt tag;
    Movement movement;

    protected void setup() {
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
        addBehaviour(receive_message);
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
                    System.out.println("content = " + content);

                } else if (content.equals("stop")) {
                    System.out.println("content = " + content);
                }
                else if (content.equals("collision")) {
                    System.out.println("content = " + content);
                }
                else if (content.equals("no_collision")) {
                    System.out.println("content = " + content);

                }
                else {
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        JSONObject location = jsonObject.getJSONObject("location");
                        int x = location.getInt("x");
                        int y = location.getInt("y");
                        Point2D destination = new Point2D(x, y);
                        System.out.println("Destination: " + destination);
                        System.out.println("======================================\n");
                    } catch (JSONException ignored) {
                    }
                }
                block();
            }
        }
    };


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


}
