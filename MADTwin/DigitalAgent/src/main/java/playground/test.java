package playground;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.Random;


public class test {
    Timestamp timestamp;
    static File file;
    static FileWriter output;
    static CSVWriter writer;
    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();
        try (CSVReader reader = new CSVReader(new FileReader("c:\\temp\\DualAgentCoordinates2.csv\\"))) {
            String[] values;
            Random rand = new Random();

            int x = rand.nextInt(5);
            int y = rand.nextInt(6);

            Coordinate currentPosition=new Coordinate(x, y);

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

            LineString lineString1 = geometryFactory.createLineString(coordinatesArray);
//            System.out.println(lineString1);

            Geometry point = new GeometryFactory().createPoint(currentPosition);

            System.out.println(lineString1.distance(point));

//            for (Coordinate c : coordinatesArray) {
//                System.out.println(c.distance(currentPosition));
//            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        
    }
}
