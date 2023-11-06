package playground;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Agent2 extends Agent {

    LineString sentLineString;
    TickerBehaviour sendMsg = new TickerBehaviour(this, 10000) {
        @Override
        public void onTick() {
            sendPathLine();
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Alice", AID.ISLOCALNAME));
            msg.setOntology("Sent Line String");
            msg.setConversationId("LineString");
            System.out.println("Sending "+sentLineString);
            msg.setContent(sentLineString.toString());
            send(msg);

        }
    };

        public void sendPathLine() {
            GeometryFactory geometryFactory = new GeometryFactory();
            try (CSVReader reader = new CSVReader(new FileReader("c:\\temp\\DualAgentCoordinates2.csv\\"))) {
                String[] values;

                List<Coordinate> coordinateList = new ArrayList<>();
                // read every arrow in the CSV file
                while ((values = reader.readNext()) != null) {
                    try {
                        coordinateList.add(new Coordinate(Double.valueOf(values[1]), Double.valueOf(values[2])));
                    } catch (NumberFormatException e) {
                        System.out.println(String.format("There are string values: %s, %s",values[1], values[2]));
                    }
                }
                Coordinate[] coordinatesArray = new Coordinate[coordinateList.size()];
                coordinatesArray = coordinateList.toArray(coordinatesArray);
                sentLineString = geometryFactory.createLineString(coordinatesArray);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }

        }



    @Override
    protected void setup() {
        // Register this agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Communicating");
        sd.setName(getLocalName()+"-Communicating");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(sendMsg);
    }
}
