package agent;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Initializer {
    /**
     * IP (or host) of the main container
     */
    private static String MAIN_CONTAINER_IP; // localhost
    static {
        try {
            MAIN_CONTAINER_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    /**
     * Port to use to contact the AMS
     */
    private static int PLATFORM_PORT=1099;
    /**
     * ID (name) of the platform instance
     */
    private static String PLATFORM_ID="Main-Platform";
    /**
     * Name of the container
     */
    private static String CONTAINER_NAME="Laptop-Container";
    /**
     * MTPS Protocol address
     */
    private static String MTPS_PROTOCOL=String.format("jade.mtp.http.MessageTransportProtocol(http://%s:7778)", MAIN_CONTAINER_IP);

    public static void main(String[] args) throws UnknownHostException {


        Runtime runtime = Runtime.instance();
        Properties properties = new ExtendedProperties();
        properties.setProperty(Profile.GUI, "true");
        properties.setProperty(Profile.MAIN, "true");
        Profile profile = new ProfileImpl(properties);
        profile.setParameter(Profile.PLATFORM_ID,PLATFORM_ID);
        profile.setParameter(Profile.MAIN_HOST,MAIN_CONTAINER_IP);
        profile.setParameter(Profile.CONTAINER_NAME,CONTAINER_NAME);
        profile.setParameter(Profile.MTPS,MTPS_PROTOCOL);

        ContainerController containerController = runtime.createMainContainer(profile);

        if (containerController != null) {
            AgentController agentController;
            try {
                agentController = containerController.createNewAgent("laptop", LaptopAgent.class.getName(), null);
                agentController.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
