package playground;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Vector;

public class Agent1 extends Agent {
    Vector agents = new Vector();
    static Geometry receivedLineString;
    ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
    static boolean isSimpleBehaviour;
    CyclicBehaviour receiveMsg = new CyclicBehaviour() {
        private MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("LineString");
        @Override
        public void action() {
            ACLMessage msgRx = receive(messageTemplate);
            if (msgRx != null) {
                try {
                    receivePathLine(msgRx.getContent());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ACLMessage msgTx = msgRx.createReply();
                msgTx.setOntology("Received Line String");
                msgTx.setConversationId("LineString");
                send(msgTx);

            } else {
                block(3000);
            }
        }
    };

    public static void receivePathLine(String s) throws ParseException {
        receivedLineString = new WKTReader().read(s);
        System.out.println("Received: "+receivedLineString);
        System.out.println("lineString.getEndPoint() = " + ((LineString) receivedLineString).getEndPoint());
        Point2D point2D =new Point2D.Double(2, 3);
        Random rand = new Random();
        int x = rand.nextInt(5);
        int y = rand.nextInt(6);
        Coordinate currentPosition=new Coordinate(x, y);
        Geometry currentPoint = new GeometryFactory().createPoint(currentPosition);
        DistanceOp distOp = new DistanceOp(currentPoint, receivedLineString);
        Coordinate[] closestPt = distOp.nearestPoints();
        LineString closestPtLine = new GeometryFactory().createLineString(closestPt);
        System.out.println("From Current Position " + closestPtLine.getStartPoint()+", The Closest Is "+closestPtLine.getEndPoint()
                + " (Distance = " + closestPtLine.getLength() + ")");

    }


    TickerBehaviour update = new TickerBehaviour(this, 5000) {
        protected void onTick() {
           // Update the list of agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Communicating");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                agents.clear();
                for (int i = 0; i < result.length; ++i) {
                    agents.addElement(result[i].getName());
                    System.out.println(result[i].getName());
                }
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }
    };

    SimpleBehaviour simpleBehaviour = new SimpleBehaviour() {
        int i;
        @Override
        public void onStart() {
            isSimpleBehaviour=true;
        }

        @Override
        public void action() {
            System.out.println("simple");
            System.out.println(isSimpleBehaviour);
            block(5000);
            i++;
        }

        @Override
        public boolean done() {
            if (i>4)
                return true;
                else
                return false;
        }

        @Override
        public int onEnd() {
            isSimpleBehaviour = false;
            System.out.println("end"+isSimpleBehaviour);
            return super.onEnd();
        }
    };

    @Override
    protected void setup() {
        System.out.println("Printed");
        ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
        addBehaviour(update);
        addBehaviour(receiveMsg);
        addBehaviour(simpleBehaviour);
        addBehaviour(parallelBehaviour);

    }
}
