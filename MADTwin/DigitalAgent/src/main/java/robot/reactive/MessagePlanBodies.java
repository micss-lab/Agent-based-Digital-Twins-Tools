package robot.reactive;

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
            System.out.println("==========Start Mission Message==========");
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
            System.out.println("DROP_CHARGING_STATIONS");
            charging_locations.getValue().clear();
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }

    public static class LowBatteryMessagePlanBody extends MessagePlanBody {
        @Override
        public void action() {
            battery_level.setValue(Robot.BatteryLevel.LOW);
            System.out.println("Battery level: LOW");
            dispatchGoal(new MovementCapability.ChargeGoal());
            setEndState(Plan.EndState.SUCCESSFUL);
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
                    System.out.println("Destination: " + destination);
                    dispatchGoal(new MovementCapability.DestinationGoal(destination));
                    checkStart();
                } else if (jsonObject.has("charging_station")) {
                    Point2D station = jsonToPoint(jsonObject.getJSONObject("charging_station"));
                    System.out.println("station: " + station);
                    charging_locations.addValue(station);
                }
            } catch (JSONException ignored) {
            }
            setEndState(Plan.EndState.SUCCESSFUL);
        }
    }
}
