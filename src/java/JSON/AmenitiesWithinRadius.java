package src.java.JSON;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class AmenitiesWithinRadius {

    public static void main(String[] args) {
        try {
            // Read GeoJSON file
            String geoJsonContent = ClosestNodeFinder.readGeoJsonFile("C:\\Users\\alenq\\Downloads\\transitorartifact\\sourcecode\\bcs25-project-1-2\\bcs25-project-1-2\\src\\java\\JSON\\amenity.geojson");

            // Get user input for latitude and longitude
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter latitude: ");
            double userLat = scanner.nextDouble();
            System.out.print("Enter longitude: ");
            double userLon = scanner.nextDouble();

            // Find nodes within 2 kilometers
            double radius = 2000; // 2 kilometers in meters
            List<ClosestNodeFinder.Node> nodesWithinDistance = ClosestNodeFinder.findNodesWithinDistance(geoJsonContent, userLat, userLon, radius);

            // Print amenities of nodes within 2 kilometers
            if (!nodesWithinDistance.isEmpty()) {
                System.out.println("Amenities within 2 kilometers:");
                for (ClosestNodeFinder.Node node : nodesWithinDistance) {
                    System.out.println(node.amenities);
                }
            } else {
                System.out.println("No amenities found within 2 kilometers.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
