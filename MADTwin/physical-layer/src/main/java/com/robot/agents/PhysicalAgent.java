package com.robot.agents;


import jade.core.Agent;

public class PhysicalAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Start: "+this.getName());
    }

}