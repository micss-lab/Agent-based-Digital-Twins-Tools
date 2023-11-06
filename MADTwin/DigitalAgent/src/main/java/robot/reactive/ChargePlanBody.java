package robot.reactive;

import bdi4jade.annotation.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import helpers.Point2D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ChargePlanBody extends MovePlanBody {
    @Belief
    static TransientBelief<String, Robot.Status> status;

    @Belief
    static TransientBeliefSet<String, Point2D> charging_locations;

    @Belief
    static TransientBelief<String, Robot.BatteryLevel> battery_level;

    @Belief
    static TransientBelief<String, AID> control_center_aid;

    @Override
    public void onStart() {
        movement = ((Robot) myAgent).movement;
        destination = this.select_shortest_charge_point();
        if (destination == null) {
            System.out.println("No charging point found");
        }
        else {
            System.out.println("Starting to move to charging station: " + destination);
        }
        if(control_center_aid.getValue() != null) {
            send_battery_message();
        }
    }

    @Override
    public int onEnd() {
        System.out.println("Charging");
        block(10000);
        battery_level.setValue(Robot.BatteryLevel.HIGH);
        System.out.println("Charged");
        if(control_center_aid.getValue() != null) {
            send_battery_message();
        }
        return super.onEnd();
    }

    private void send_battery_message() {
        if (battery_level.getValue() == Robot.BatteryLevel.LOW) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(control_center_aid.getValue());
            switch (battery_level.getValue()) {
                case LOW:
                    msg.setContent("low_battery");
                    break;
                case MEDIUM:
                    msg.setContent("medium_battery");
                    break;
                case HIGH:
                    msg.setContent("high_battery");
                    break;
            }
            myAgent.send(msg);
        }
    }

    private Point2D select_shortest_charge_point() {
        Point2D shortest_point = null;
        double shortest_distance = Double.MAX_VALUE;
        for (Point2D point : charging_locations.getValue()) {
            double distance = movement.getLocation().dist(point);
            if (distance < shortest_distance) {
                shortest_distance = distance;
                shortest_point = point;
            }
        }
        return shortest_point;
    }
}
