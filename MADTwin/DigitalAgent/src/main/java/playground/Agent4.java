package playground;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

public class Agent4 extends Agent {
    Vector agents = new Vector();
    static Queue<LineString> lineStringForDestination = new LinkedList<>();
    static Geometry receivedLineString;


    CyclicBehaviour receiveMsg = new CyclicBehaviour() {
        private MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("line_string");
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
                msgTx.setConversationId("line_string");
                send(msgTx);
            } else {
                block();
            }
        }
    };

    public static void receivePathLine(String s) throws ParseException {
        receivedLineString = new WKTReader().read(s);
        lineStringForDestination.add((LineString) receivedLineString);
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

    @Override
    protected void setup() {
        System.out.println("Printed");
        addBehaviour(receiveMsg);

    }
}
