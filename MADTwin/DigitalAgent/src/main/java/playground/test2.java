package playground;

import bdi4jade.belief.TransientBeliefSet;
import helpers.Point2D;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class test2 {
    static ArrayList<Point2D> charging_locations = new ArrayList<>();
    static Point2D destination;
    public static void main(String[] args) throws UnknownHostException {

        Point2D p1, p2 , p3, p4;
        destination= new Point2D(15, 17);
        p1 = new Point2D(3, 5);
        p2 = new Point2D(8, 14);
        p3 = new Point2D(16, 20);
        p4 = new Point2D(25, 30);

        charging_locations.add(p1);
        charging_locations.add(p2);
        charging_locations.add(p3);
        charging_locations.add(p4);

        System.out.println(selectingShortestChargingStation());

    }

    private static Point2D selectingShortestChargingStation() {
        Point2D shortest_point = null;
        double shortest_distance = Double.MAX_VALUE;
        for (Point2D point : charging_locations) {
            double distance = destination.dist(point);
            if (distance < shortest_distance) {
                shortest_distance = distance;
                shortest_point = point;
            }
        }
        return shortest_point;
    }

}
