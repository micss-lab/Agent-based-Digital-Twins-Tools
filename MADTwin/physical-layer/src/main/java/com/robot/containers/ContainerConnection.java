package com.robot.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class ContainerConnection {

    public static void connectToMainContainer() {
        try {
            Runtime runtime = Runtime.instance();
            String source = "192.168.0.112";
            String target = "192.168.0.138";
            ProfileImpl profileImp = new ProfileImpl(target, 1099, null, false);
            profileImp.setParameter(Profile.LOCAL_HOST, source);
            profileImp.setParameter(Profile.MAIN_PORT, "1099");
            profileImp.setParameter(Profile.CONTAINER_NAME, "Physical-Layer-Container");
            AgentContainer agentContainer = runtime.createAgentContainer(profileImp);

            AgentController physicalAgent = agentContainer.createNewAgent("PhysicalAgent",
                    "com.robot.agents.PhysicalAgent", new Object[]{});
            physicalAgent.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
