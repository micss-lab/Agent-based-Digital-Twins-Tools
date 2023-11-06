package playground;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
public class BasicExample
{
    public static void main(String[] args)
            throws Exception
    {
        // read a geometry from a WKT string (using the default geometry factory)
        Geometry g1 = new WKTReader().read("LINESTRING (0 0, 2 2, 4 4)");
        System.out.println("Geometry 1: " + g1);

        // create a geometry by specifying the coordinates directly
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(0, 0),
                new Coordinate(10, 10), new Coordinate(20, 20)};
        // use the default factory, which gives full double-precision
        Geometry g2 = new GeometryFactory().createLineString(coordinates);
        System.out.println("Geometry 2: " + g2);

        // compute the intersection of the two geometries
        Geometry g3 = g1.intersection(g2);
        System.out.println("G1 intersection G2: " + g3);

        // create a point
        Geometry point = new GeometryFactory().createPoint(new Coordinate(1,1));
        System.out.println("Point Geometry: " + point);

        // compute whether point is on g1
        System.out.println("Point within g1: " + g1.contains(point));

        System.out.println("g1 = " + g1.getNumPoints());

        Coordinate c1 = new Coordinate(-10,5);
        Coordinate c2 = new Coordinate(10,5);

        System.out.println("hhh"+c1.distance(c2));

        System.out.println("etet"+Angle.toDegrees(Angle.angle(c1, c2)));
        Angle current = null;
        System.out.println("etet"+current.toDegrees(Angle.angle(c1, c2)));


        Coordinate[] coordinates1 = new Coordinate[] {c1, c2};
        LineString closestPtLine = new GeometryFactory().createLineString(coordinates1);

        System.out.println("closestPtLine = " + closestPtLine.getStartPoint());
        System.out.println("closestPtLine = " + closestPtLine.getEndPoint());
        System.out.println("closestPtLine = " + closestPtLine);


    }
}