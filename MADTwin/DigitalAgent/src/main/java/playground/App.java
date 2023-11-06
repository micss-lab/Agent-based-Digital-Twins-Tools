package playground;

import jade.core.*;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.io.IOException;

public class App {
    public static final String AGENT_1 = "Alice";
    public static final String AGENT_2 = "Bob";

    public static void main(String[] args) throws IOException {

        try {
            Runtime runtime = Runtime.instance();
            Properties properties = new ExtendedProperties();
            properties.setProperty(Profile.GUI, "true");
            properties.setProperty(Profile.MAIN, "true");
            Profile profile = new ProfileImpl(properties);
            profile.setParameter(profile.MAIN_HOST, "localhost");

            AgentContainer agentContainer = runtime.createMainContainer(profile);
//            AgentController agent = agentContainer.createNewAgent(AGENT_1,
//                    "playground.Agent1", new Object[]{});
//            agent.start();
//
//            AgentController agent2 = agentContainer.createNewAgent(AGENT_2,
//                    "playground.Agent2", new Object[]{});
//            agent2.start();

//            AgentController agent3 = agentContainer.createNewAgent(AGENT_1,
//                    ContainersRetrieverAgent.class.getName(), new Object[]{});
//            agent3.start();
            AgentController agent4 = agentContainer.createNewAgent(Agent3.class.getName(),
                    Agent3.class.getName(), new Object[]{});
            agent4.start();

            System.out.println("Agents Created");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
