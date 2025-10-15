package src.java.JSON;
import java.io.*;
import java.util.*;

public class ClosestNodeFinder {

    public static void main(String[] args) {
        try {
            // Read GeoJSON file
            String geoJsonContent = readGeoJsonFile("./src/java/JSON/amenity.geojson");

            // Get user input for latitude and longitude
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter latitude: ");
            double userLat = scanner.nextDouble();
            System.out.print("Enter longitude: ");
            double userLon = scanner.nextDouble();

            // Find closest node
            Node closestNode = findClosestNode(geoJsonContent, userLat, userLon);

            // Print amenities of closest node
            if (closestNode != null) {
                System.out.println("Closest node amenities: " + closestNode.amenities);
            } else {
                System.out.println("No nodes found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }//50,8544730689126, 5,670415204500935
    }

    public static String readGeoJsonFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        br.close();
        return content.toString();
    }

    public static Node findClosestNode(String geoJsonContent, double userLat, double userLon) {
        int featuresStartIndex = geoJsonContent.indexOf("\"features\": [");
        int featuresEndIndex = geoJsonContent.lastIndexOf("]");

        if (featuresStartIndex == -1 || featuresEndIndex == -1) {
            return null;
        }

        String featuresContent = geoJsonContent.substring(featuresStartIndex + 12, featuresEndIndex + 1);
        List<String> features = parseFeatures(featuresContent);

        double minDistance = Double.MAX_VALUE;
        Node closestNode = null;

        for (String feature : features) {
            if (feature.contains("\"type\": \"Point\"")) {
                int coordsStartIndex = feature.indexOf("\"coordinates\": [");
                int coordsEndIndex = feature.indexOf("]", coordsStartIndex);
                String coordsContent = feature.substring(coordsStartIndex + 15, coordsEndIndex).trim();
                coordsContent = coordsContent.replace("[", "").replace("]", ""); // Clean the string
                String[] coords = coordsContent.split(",");

                double lon = Double.parseDouble(coords[0].trim());
                double lat = Double.parseDouble(coords[1].trim());

                double distance = vincentyDistance(userLat, userLon, lat, lon);

                if (distance == 0) {
                    int propsStartIndex = feature.indexOf("\"properties\": {");
                    int propsEndIndex = feature.lastIndexOf("}");
                    String propertiesContent = feature.substring(propsStartIndex + 15, propsEndIndex + 1);
                    return new Node(lat, lon, propertiesContent);
                }

                if (distance < minDistance) {
                    minDistance = distance;
                    int propsStartIndex = feature.indexOf("\"properties\": {");
                    int propsEndIndex = feature.lastIndexOf("}");
                    String propertiesContent = feature.substring(propsStartIndex + 15, propsEndIndex + 1);
                    closestNode = new Node(lat, lon, propertiesContent);
                }
            }
        }
        return closestNode;
    }

    public static List<Node> findNodesWithinDistance(String geoJsonContent, double userLat, double userLon, double distanceLimit) {
        int featuresStartIndex = geoJsonContent.indexOf("\"features\": [");
        int featuresEndIndex = geoJsonContent.lastIndexOf("]");

        if (featuresStartIndex == -1 || featuresEndIndex == -1) {
            return Collections.emptyList();
        }

        String featuresContent = geoJsonContent.substring(featuresStartIndex + 12, featuresEndIndex + 1);
        List<String> features = parseFeatures(featuresContent);

        List<Node> nodesWithinDistance = new ArrayList<>();

        for (String feature : features) {
            if (feature.contains("\"type\": \"Point\"")) {
                int coordsStartIndex = feature.indexOf("\"coordinates\": [");
                int coordsEndIndex = feature.indexOf("]", coordsStartIndex);
                String coordsContent = feature.substring(coordsStartIndex + 15, coordsEndIndex).trim();
                coordsContent = coordsContent.replace("[", "").replace("]", ""); // Clean the string
                String[] coords = coordsContent.split(",");

                double lon = Double.parseDouble(coords[0].trim());
                double lat = Double.parseDouble(coords[1].trim());

                double distance = vincentyDistance(userLat, userLon, lat, lon);

                if (distance <= distanceLimit) {
                    int propsStartIndex = feature.indexOf("\"properties\": {");
                    int propsEndIndex = feature.lastIndexOf("}");
                    String propertiesContent = feature.substring(propsStartIndex + 15, propsEndIndex + 1);
                    nodesWithinDistance.add(new Node(lat, lon, propertiesContent));
                }
            }
        }
        return nodesWithinDistance;
    }

    public static double vincentyDistance(double lat1, double lon1, double lat2, double lon2) {
        final double a = 6378137.0; // WGS-84 semi-major axis constant in meters
        final double f = 1 / 298.257223563; // WGS-84 flattening factor
        final double b = 6356752.314245; // WGS-84 semi-minor axis

        double L = Math.toRadians(lon2 - lon1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double cosSigma, sigma, sinSigma, cos2SigmaM, sinAlpha;
        double lambda = L, lambdaP, iterLimit = 100;
        do {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) {
                return 0; // co-incident points
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / (1 - sinAlpha * sinAlpha);
            double C = f / 16 * (1 - sinAlpha * sinAlpha) * (4 + f * (4 - 3 * (1 - sinAlpha * sinAlpha)));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0) {
            return Double.POSITIVE_INFINITY; // formula failed to converge
        }

        double uSquared = (1 - sinAlpha * sinAlpha) * (a * a - b * b) / (b * b);
        double A = 1 + uSquared / 16384 * (4096 + uSquared * (-768 + uSquared * (320 - 175 * uSquared)));
        double B = uSquared / 1024 * (256 + uSquared * (-128 + uSquared * (74 - 47 * uSquared)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        return b * A * (sigma - deltaSigma);
    }

    static List<String> parseFeatures(String featuresContent) {
        List<String> features = new ArrayList<>();
        int start = 0;
        int braceCount = 0;
        for (int i = 0; i < featuresContent.length(); i++) {
            char c = featuresContent.charAt(i);
            if (c == '{') {
                if (braceCount == 0) {
                    start = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    features.add(featuresContent.substring(start, i + 1));
                }
            }
        }
        return features;
    }

    static class Node {
        double lat;
        double lon;
        String amenities;

        Node(double lat, double lon, String amenities) {
            this.lat = lat;
            this.lon = lon;
            this.amenities = amenities;
        }
    }
}
