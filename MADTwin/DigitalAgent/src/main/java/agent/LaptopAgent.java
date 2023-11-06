package agent;

import helpers.Delay;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import mqtt.TagIdMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;
import py4j.GatewayServer;
import robot.bdi.Robot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LaptopAgent extends Agent {
    Logger logger = Logger.getLogger(LaptopAgent.class.getName());
    private GatewayServer gatewayServer;
    private final HashMap<AID, AID> dualAgents = new HashMap<>();
    private final HashMap<AID, ArrayList<String>> controlledAgents = new HashMap<>();
    private final ArrayList<ArrayList<String>> new_messages = new ArrayList<>();
    ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

    static int x1, y1, x2, y2, x3, y3;
    static double safeDistance;
    static double COLLISION_DISTANCE=700;
    static boolean isStopped;
    static boolean isCollision;
    static String destinationLocation;
    static int collisionMessageCounter;

//    final TagIdMqtt tag_6823 = new TagIdMqtt( "6823");
    final TagIdMqtt tag_685C = new TagIdMqtt( "685c");
    final TagIdMqtt tag_6A75 = new TagIdMqtt( "6a75");
    final TagIdMqtt tag_682E = new TagIdMqtt( "682e");

    public LaptopAgent() throws MqttException {
    }

    protected void setup() {
        printLogMessage("Control Agent: " + getAID().getName() + " Initialized");
        printLogMessage("Agents Addresses: "+String.join(",\n", getAID().getAddressesArray()));
        par.addSubBehaviour(getTagCoordinates);
        par.addSubBehaviour(new ReceiveMessage());
        addBehaviour(par);
        gatewayServer = new GatewayServer(this);
        gatewayServer.start();
    }

    public void printLogMessage(String logMsg){
        logger.info("--------------------------------------\n" + logMsg + "\n--------------------------------------------");
    }

    public AID createAID(String GUID, String address) {
        AID aid = new AID(GUID, AID.ISGUID);
        aid.addAddresses(address);
        controlledAgents.put(aid, new ArrayList<>(Arrays.asList(GUID, address)));
        return aid;
    }

    public void sendMessage(String message, AID aid) {
        System.out.println("=========Sending Control Message=========");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(aid);
        msg.setConversationId("mission_control");
        // get the dual agent that is identical to the physical agent
        AID dual_aid = dualAgents.get(aid);
        if (dual_aid != null) {
            msg.addReceiver(dual_aid);
        }
        msg.setContent(message);
        send(msg);
        System.out.println("==> Agent: "+msg.getSender().getName()+" - Sent "+msg.getContent()+" ==> "+aid);
        System.out.println("=======End Sending Control Message=======");
    }

    /*
    Receive messages from physical agents (collision or obstacle messages)
     */
    private class ReceiveMessage extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("Agent: "+this.myAgent.getName()+" - Received Message From "+msg.getSender());
                if (controlledAgents.containsKey(msg.getSender())) {
                    ArrayList<String> agentList = (ArrayList<String>) controlledAgents.get(msg.getSender()).clone();
                    System.out.println("From Agent: "+agentList);
                    agentList.add(msg.getContent());
                    new_messages.add(agentList);
                    }
                }
            else {
                block();
            }
        }
    }

    /*
    add the collision detection functionality
     */
    TickerBehaviour getTagCoordinates=new TickerBehaviour(this, 20) {
        @Override
        protected void onTick() {
            if (!tag_685C.isNew_message()) {
                try {
                    tag_685C.getLock();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            } else {
                x1 = tag_685C.getLocation().x;
                y1 = tag_685C.getLocation().y;
//                System.out.println("Tag 685C: " + "[x1=" + x1 + ",y1=" + y1 + "]");
            }

            if (!tag_6A75.isNew_message()) {
                try {
                    tag_6A75.getLock();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            } else {
                x2 = tag_6A75.getLocation().x;
                y2 = tag_6A75.getLocation().y;
//                System.out.println("Tag 6823: " + "[x2=" + x2 + ",y2=" + y2 + "]");
            }
            if (!tag_682E.isNew_message()) {
                try {
                    tag_682E.getLock();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            } else {
                x3 = tag_682E.getLocation().x;
                y3 = tag_682E.getLocation().y;
//                System.out.println("Tag 682E: " + "[x3=" + x3 + ",y3=" + y3 + "]");
            }

            if (tag_685C.isNew_message() && tag_6A75.isNew_message()) {
                safeDistance = calculateDistance(new Point(x1, y1), new Point(x2, y2));
            } else if (tag_685C.isNew_message() && tag_682E.isNew_message())
            {
                safeDistance = calculateDistance(new Point(x1, y1), new Point(x3, y3));
            }
            else if (tag_6A75.isNew_message() && tag_682E.isNew_message()){
                safeDistance = calculateDistance(new Point(x2, y2), new Point(x3, y3));
            }
                if (safeDistance != 0)
                    if (safeDistance < COLLISION_DISTANCE && !isCollision) {
                        isCollision = true;
                        addBehaviour(collision);
                    }
                    //if the safeDistance is less than the threshold, and isCollision is true
                    else if (safeDistance < COLLISION_DISTANCE && isCollision) {
//                        isCollision = false;
                        Delay.msSleep(5000);
                        if (collisionMessageCounter==0) {
                            addBehaviour(noCollision);
                        }
                    } else if (safeDistance > COLLISION_DISTANCE && isCollision) {
                        System.out.println("SAFE");
                        Delay.msSleep(15000);
                        isCollision = false;
                        collisionMessageCounter=0;
                    }
            }
    };

    OneShotBehaviour collision = new OneShotBehaviour() {
        @Override
        public void action() {
            for (AID aid : controlledAgents.keySet()) {
                ACLMessage collision_message = new ACLMessage(ACLMessage.INFORM);
                collision_message.addReceiver(aid);
                collision_message.setConversationId("mission_control");
                collision_message.setContent("collision");
                send(collision_message);
            }
            if (!isStopped) {
                isStopped = true;
                System.out.println("STOP");
            }
            System.out.println("Collision");
                //block the getTagCoordinates behaviour based on the time required to stop and
        }
    };

    OneShotBehaviour noCollision = new OneShotBehaviour() {
        @Override
        public void action() {
            for (AID aid : controlledAgents.keySet()) {
                ACLMessage no_collision_message = new ACLMessage(ACLMessage.INFORM);
                no_collision_message.addReceiver(aid);
                no_collision_message.setConversationId("mission_control");
                no_collision_message.setContent("no_collision");
                send(no_collision_message);
                }
            System.out.println("Sent Move Message After Collision to: ");
            removeBehaviour(collision);
            addBehaviour(moveBehaviour);
            collisionMessageCounter++;
        }
    };

    OneShotBehaviour moveBehaviour = new OneShotBehaviour() {
        public void action() {
            for (AID aid : controlledAgents.keySet()) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                ACLMessage destinationMessage = new ACLMessage(ACLMessage.INFORM);
//                msg.addReceiver(aid);
                msg.setConversationId("mission_control");
                // get the dual agent that is identical to the physical agent
                AID dual_aid = dualAgents.get(aid);
                if (dual_aid != null) {
                    if (aid.getName().endsWith("Physical-Platform")) {
                        System.out.println("BEFORE");
                        Delay.msSleep(20000);
                        System.out.println("AFTER");
                        System.out.println("PHYSICAL AGENT");
                        msg.addReceiver(dual_aid);
                        msg.setContent("no_collision");
                        send(msg);
                    }
                    else if (aid.getName().endsWith("dual@Main-Platform")) {
                        System.out.println("DUAL AGENT");
                        destinationMessage.addReceiver(dual_aid);
                        send(destinationMessage);
                        destinationMessage.setContent(destinationLocation);
                    }
                }
            }
            System.out.println("MOVE");
        }
    };

    /*
    Functions called from the python side
     */
    public void sendStartMessage(String GUID, String address) {
        AID aid = createAID(GUID, address);
        sendMessage("start", aid);
    }

    public void sendStopMessage(String GUID, String address) {
        sendMessage("stop", createAID(GUID, address));
    }

    public void sendLocationMessage(String GUID, String address, Integer x, Integer y) {
        String location = "{location: {\"x\":" + x + ",\"y\":" + y + "}}";
        destinationLocation=location;
        sendMessage(location, createAID(GUID, address));
    }

    public void sendChargingStationMessage(String GUID, String address, Integer x, Integer y) {
        String station = "{charging_station: {\"x\":" + x + ",\"y\":" + y + "}}";
        sendMessage(station, createAID(GUID, address));
    }

    public void sendDropChargingStationsMessage(String GUID, String address) {
        sendMessage("drop_charging_stations", createAID(GUID, address));
    }

    public void sendLowBatteryMessage(String GUID, String address) {
        sendMessage("low_battery", createAID(GUID, address));
    }

    public void createDualAgent(String GUID, String address, String tagID, Integer x, Integer y) throws StaleProxyException {
        AID aid = createAID(GUID, address);
        if (!dualAgents.containsKey(aid)) {
            dualAgents.put(aid, createAID(GUID.split("@")[0] + "_dual@Main-Platform", "Laptop-Manager:7778"));
            AgentController ac = getContainerController().createNewAgent(GUID.split("@")[0] + "_dual", Robot.class.getName(), new Object[]{tagID+"_dual", x, y});
            ac.start();
            System.out.println("Create Digital Agent ==> " + aid.getLocalName()+ "_dual");
        }
    }

    public void deleteDualAgent(String GUID, String address) throws ControllerException {
        AID aid = createAID(GUID, address);
        if (dualAgents.containsKey(aid)) {
            // delete the name of the physical agent that has this digital agent
            dualAgents.remove(aid);
            getContainerController().getAgent(GUID.split("@")[0] + "_dual").kill();
            System.out.println("Delete Digital Agent ==> " + aid.getLocalName()+ "_dual");
        }
    }

    public ArrayList<ArrayList<String>> getNewMessages() {
        ArrayList<ArrayList<String>> messages = new ArrayList<>(new_messages);
        new_messages.clear();
        return messages;
    }

    public void shutdown() {
        gatewayServer.shutdown();
        Codec codec = new SLCodec();
        Ontology jmo = JADEManagementOntology.getInstance();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(jmo);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getAMS());
        msg.setLanguage(codec.getName());
        msg.setOntology(jmo.getName());
        try {
            getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
            send(msg);
        }
        catch (Exception ignored) {}
    }

    /*
    functions to calculate the distance between different tags
     */
    public static double calculateDistance (Point sourcePt, Point targetPt)
    {
        //convert the mm to cm, so we divide the result by 10
        double distance = Math.hypot(sourcePt.x - targetPt.x, sourcePt.y - targetPt.y);
//        System.out.println("CALCULATED DISTANCE  " + distance);
        return distance;
    }

}
