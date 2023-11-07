package milo.opcua.server;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;

public class RobotAgent extends Agent {

    protected void setup() {
        System.out.println("Agent " + getLocalName() + " started.");
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(receiverBehaviour);
        addBehaviour(parallelBehaviour);
    }

    TickerBehaviour receiverBehaviour = new TickerBehaviour(this, 500) {
        @Override
        public void onStart() {
            super.onStart();
            System.out.println("Destination = " + CustomNamespace.destinationPoint.getValue().getValue().getValue());
        }

        @Override
        public void onTick() {
            System.out.println("myAgent = " + myAgent.getLocalName());
            System.out.println("X = " + CustomNamespace.positionX.getValue().getValue().getValue());
            System.out.println("Y = " + CustomNamespace.positionY.getValue().getValue().getValue());
        }
    };
}