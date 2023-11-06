package robot.bdi;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import helpers.Point2D;
import jade.lang.acl.ACLMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public abstract class MessagePlanBodies {
    public abstract static class MessagePlanBody extends AbstractPlanBody {
        @Belief
        static TransientBelief<String, Robot.Status> status;

        @Belief
        static TransientBeliefSet<String, Point2D> charging_locations;

        @Belief
        static TransientBelief<String, Robot.BatteryLevel> battery_level;

        @Override
        public void onStart() {
            super.onStart();
            System.out.println("=============Mission Message=============");
        }

        @Override
        public int onEnd() {
            System.out.println("===========End Mission Message===========");

            return super.onEnd();
        }

        protected void checkStart() {
            if (status.getValue() == Robot.Status.START) {
                status.setValue(Robot.Status.RUNNING);
            } else if (status.getValue() == Robot.Status.STOP) {
                status.setValue(Robot.Status.IDLE);
                System.out.println("Status to: IDLE");
            }
        }

        protected Point2D jsonToPoint(JSONObject jsonObject) {
            int x = jsonObject.getInt("x");
            int y = jsonObject.getInt("y");
            return new Point2D(x, y);
        }
    }

    public static class StartMessagePlanBody extends MessagePlanBody {
        @Override
        public void action() {
            status.setValue(Robot.Status.START);
            System.out.println("Status to: START");
            checkStart();
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static class StopMessagePlanBody extends MessagePlanBody {
        @Override
        public void action() {
            status.setValue(Robot.Status.STOP);
            System.out.println("Status to: STOP");
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static class DropChargingStationsMessagePlanBody extends MessagePlanBody {
        @Override
        public void action() {
            System.out.println("Drop Charging Station");
            charging_locations.getValue().clear();
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static class LowBatteryMessagePlanBody extends MessagePlanBody {
        @Override
        public void action() {
            battery_level.setValue(Robot.BatteryLevel.LOW);
            System.out.println("Battery Level: LOW");
            dispatchGoal(new MovementCapability.ChargeGoal());
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static class lineStingMessagePlanBody extends MessagePlanBody {
        Geometry receivedLineString;

        @Override
        public void action() {
            ACLMessage msgRx = ((MessageGoal) getGoal()).getMessage();
            if (msgRx != null) {
                try {
                    receivedLineString = new WKTReader().read(msgRx.getContent());
                    System.out.println("lineString = " + ((LineString) receivedLineString));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Point point = ((LineString) receivedLineString).getEndPoint();
                Point2D destination = new Point2D((int) point.getX(), (int) point.getY());
                dispatchGoal(new MovementCapability.DestinationGoal(destination));
                setEndState(Plan.EndState.SUCCESSFUL);
                ACLMessage msgTx = msgRx.createReply();
                msgTx.setOntology("Received Line String");
                msgTx.setConversationId("LineString");
                myAgent.send(msgTx);
            } else {
                block();
            }
        }
    }

    public static class JSonMessagePlanBody extends MessagePlanBody {
        public void action() {
            ACLMessage msg = ((MessageGoal) getGoal()).getMessage();
            String content = msg.getContent();
            try {
                JSONObject jsonObject = new JSONObject(content);
                if (jsonObject.has("location")) {
                    Point2D destination = jsonToPoint(jsonObject.getJSONObject("location"));
                    System.out.println("JSon Destination Message: " + ((MessageGoal) getGoal()).getMessage().getContent());
                    dispatchGoal(new MovementCapability.DestinationGoal(destination));
                    checkStart();
                } else if (jsonObject.has("charging_station")) {
                    Point2D station = jsonToPoint(jsonObject.getJSONObject("charging_station"));
                    System.out.println("Charging Station: " + station);
                    charging_locations.addValue(station);
                }
            } catch (JSONException ignored) {
            }
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static void receivePathLine(String s) throws ParseException {
        Geometry receivedLineString;
        receivedLineString = new WKTReader().read(s);
        System.out.println("Received: " + receivedLineString);
        System.out.println("lineString.getEndPoint() = " + ((LineString) receivedLineString).getEndPoint());

        java.awt.geom.Point2D point2D = new java.awt.geom.Point2D.Double(2, 3);
        Random rand = new Random();
        int x = rand.nextInt(5);
        int y = rand.nextInt(6);
        Coordinate currentPosition = new Coordinate(x, y);
        Geometry currentPoint = new GeometryFactory().createPoint(currentPosition);
        DistanceOp distOp = new DistanceOp(currentPoint, receivedLineString);
        Coordinate[] closestPt = distOp.nearestPoints();
        LineString closestPtLine = new GeometryFactory().createLineString(closestPt);
        System.out.println("From Current Position " + closestPtLine.getStartPoint() + ", The Closest Is " + closestPtLine.getEndPoint()
                + " (Distance = " + closestPtLine.getLength() + ")");

    }

}
