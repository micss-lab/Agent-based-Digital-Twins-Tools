package mqtt;

import helpers.Point2D;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import robot.bdi.Robot;

import java.util.logging.Logger;

public class TagMqtt {
    Logger logger = Logger.getLogger(TagMqtt.class.getName());

//    Address of online pozyx broker
//    private final String host = "wss://mqtt.cloud.pozyxlabs.com:443";
    private final String username = "61d730870295a7f3798fdb31";
    private final String password = "1d761f94-6fe7-4549-aaa5-73a4ffecc2ee";
//    private final String topic = "61d730870295a7f3798fdb31";

    // address of pozyx broker local
    private final String host = "tcp://localhost:1883";
    private final String topic = "tags";


    private final MqttClient client;
    private final String tagId;

    public TagMqtt(String tagId) throws MqttException {
        this.tagId = tagId;
        client = create_client();
    }

    public void shutdown() throws MqttException {
        client.disconnect();
        logger.warning("Disconnected");
        client.close();
    }

    private MqttClient create_client() throws MqttException {
        return create_client(host, topic, username, password);
    }

    private MqttClient create_client(String host, String topic, String username, String password) throws MqttException {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            MqttClient client = new MqttClient(host, MqttClient.generateClientId(), persistence);

            // MQTT connection option
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            // retain session
            connOpts.setCleanSession(true);

            // establish a connection
            logger.info("Connecting to broker: " + host);
            client.connect(connOpts);
            logger.info("Connected...");
            client.subscribe(topic);
            logger.info("Subscribed to topic: " + topic);

            return client;
        } catch (MqttException me) {
            print_exception(me);
            throw me;
        }
    }

    private void print_exception(MqttException me) throws MqttException {
        System.out.println("reason " + me.getReasonCode());
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("exception " + me);
    }

    public void sendLocation(Point2D location) throws MqttException {
        JSONObject message = new JSONObject();
        message.put("tagId", tagId);
        message.put("success", true);
        JSONObject data = new JSONObject();
        JSONObject coordinates = new JSONObject();
        coordinates.put("x", location.x);
        coordinates.put("y", location.y);
        data.put("coordinates", coordinates);
        message.put("data", data);
        String msg = "[" + message + "]";
//        System.out.println("Sending message: " + msg);
        client.publish(topic, msg.getBytes(), 0, false);
    }
}
