package robot.reactive;

import bdi4jade.annotation.Belief;
import bdi4jade.annotation.Plan;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import bdi4jade.core.Capability;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.goal.Goal;
import bdi4jade.goal.SequentialGoal;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.reasoning.DefaultDeliberationFunction;
import bdi4jade.reasoning.DefaultOptionGenerationFunction;
import bdi4jade.reasoning.DefaultPlanSelectionStrategy;
import helpers.Movement;
import helpers.Point2D;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

import static bdi4jade.goal.GoalStatus.TRYING_TO_ACHIEVE;

public class MovementCapability extends Capability {

    private static final MessageTemplate missionTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchConversationId("mission_control"));
    @Belief
    static TransientBelief<String, Robot.Status> status;

    @Belief
    static TransientBelief<String, Movement> movement;

    @Belief
    static TransientBeliefSet<String, Point2D> charging_locations;

    @Belief
    static TransientBelief<String, Robot.BatteryLevel> battery_level;

    @Plan
    private final DefaultPlan movePlan = new DefaultPlan(DestinationGoal.class, MovePlanBody.class);

    @Plan
    private final DefaultPlan chargePlan = new DefaultPlan(ChargeGoal.class, ChargePlanBody.class);

    @Plan
    private final DefaultPlan stopPlan = new DefaultPlan(MessageTemplate.and(missionTemplate,
            MessageTemplate.MatchContent("stop")),
            MessagePlanBodies.StopMessagePlanBody.class);

    @Plan
    private final DefaultPlan startPlan = new DefaultPlan(MessageTemplate.and(missionTemplate,
            MessageTemplate.MatchContent("start")),
            MessagePlanBodies.StartMessagePlanBody.class);

    @Plan
    private final DefaultPlan dropChargingStationsPlan = new DefaultPlan(MessageTemplate.and(missionTemplate,
            MessageTemplate.MatchContent("drop_charging_stations")),
            MessagePlanBodies.DropChargingStationsMessagePlanBody.class);

    @Plan
    private final DefaultPlan lowBatteryPlan = new DefaultPlan(MessageTemplate.and(missionTemplate,
            MessageTemplate.MatchContent("low_battery")),
            MessagePlanBodies.LowBatteryMessagePlanBody.class);

    @Plan
    private final DefaultPlan jsonPlan = new DefaultPlan(missionTemplate,
            MessagePlanBodies.JSonMessagePlanBody.class);

    public MovementCapability(TransientBelief<String, Robot.Status> status, Movement movement) {
        MovementCapability.status = status;
        MovementCapability.movement = new TransientBelief<>("movement", movement);
        MovementCapability.charging_locations = new TransientBeliefSet<>("charging_locations", new HashSet<>());
        MovementCapability.battery_level = new TransientBelief<>("battery_level", Robot.BatteryLevel.HIGH);
        this.setDeliberationFunction(new Deliberation());
        this.setOptionGenerationFunction(new OptionGeneration());
        this.setPlanSelectionStrategy(new PlanSelection());
    }
    public static class OptionGeneration extends DefaultOptionGenerationFunction {
        @Override
        public void generateGoals(GoalUpdateSet goals) {
            if (status.getValue() != Robot.Status.STOP) {
                return;
            }
            for (GoalUpdateSet.GoalDescription goalDescription : goals.getCurrentGoals()) {
                goals.dropGoal(goalDescription);
                System.out.println("Dropped goal " + (goalDescription.getGoal()));
            }
        }
    }

    public static class PlanSelection extends DefaultPlanSelectionStrategy {
        @Override
        public bdi4jade.plan.Plan selectPlan(Goal goal, Set<bdi4jade.plan.Plan> candidatePlans) {
            if (candidatePlans.size() == 0) {
                return null;
            }
            if (candidatePlans.size() == 1) {
                return candidatePlans.iterator().next();
            }
            if (goal instanceof MessageGoal) {
                String most_specific_template = "";
                bdi4jade.plan.Plan most_specific_plan = null;
                for(bdi4jade.plan.Plan plan : candidatePlans) {
                    String messagetemplate = plan.toString().split(" :: ")[0];
                    if (messagetemplate.length() > most_specific_template.length()) {
                        most_specific_template = messagetemplate;
                        most_specific_plan = plan;
                    }
                }
                return most_specific_plan;
            }
            return candidatePlans.iterator().next();
        }
    }

    public static class Deliberation extends DefaultDeliberationFunction {
        @Override
        public Set<Goal> filter(Set<GoalUpdateSet.GoalDescription> set) {
            /*
            Return the closest goal if all goals are waiting,
            Otherwise return the goal that is already running.
             */
            HashSet<Goal> selected_goals = new HashSet<>();
            if (status.getValue() != Robot.Status.RUNNING) {
                return selected_goals;
            }
            Iterator<GoalUpdateSet.GoalDescription> iterator = set.iterator();
            Goal selected_goal = null;
            double shortest_distance = Double.MAX_VALUE;
            int goal_level_selected = 0;
            while (iterator.hasNext()) {
                GoalUpdateSet.GoalDescription goalDescription = iterator.next();
                if(goalDescription.getStatus() == TRYING_TO_ACHIEVE) {
                    selected_goal = goalDescription.getGoal();
                    goal_level_selected = 3;
                } if (goalDescription.getGoal() instanceof ChargeGoal && goal_level_selected < 3) {
                    selected_goal = goalDescription.getGoal();
                    goal_level_selected = 2;
                }
                if (goalDescription.getGoal() instanceof DestinationGoal && goal_level_selected < 2) {
                    DestinationGoal destinationGoal = (DestinationGoal) goalDescription.getGoal();
                    double distance = destinationGoal.getDestination().dist(movement.getValue().getLocation());
                    if (distance < shortest_distance) {
                        shortest_distance = distance;
                        selected_goal = goalDescription.getGoal();
                    }
                }
            }
            if (selected_goal != null) {
                selected_goals.add(selected_goal);
            }
            return selected_goals;
        }
    }

    public static class DestinationGoal implements Goal {
        private final Point2D destination;

        public DestinationGoal(Point2D destination) {
            this.destination = destination;
            System.out.println("DestinationGoal: " + destination);
        }

        public Point2D getDestination() {
            return destination;
        }
    }

    public static class ChargeGoal implements Goal {}

    public static class DestinationGoals extends SequentialGoal {
        public DestinationGoals(List<Goal> goals) {
            super(goals);
        }
    }
}
