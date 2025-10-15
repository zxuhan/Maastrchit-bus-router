package src.java.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import src.java.GUI.Data;
import src.java.GUI.Place;

public class CalculateDistance {
    private static final int EARTH_RADIUS = 6371000; // meters

    /**
     * Calculates the distance between two zip codes using the Haversine formula.
     * If the zip codes are not present in the data, an API call is made to retrieve
     * the coordinates.
     * 
     * @param p1 the first zip code
     * @param p2 the second zip code
     * @return the distance between the two zip codes in kilometers or meters,
     *         depending on the distance
     */
    public static double getDistance(String p1, String p2, boolean grassHopperEnabled) throws IOException {
        // Initialize data
        Data data = new Data();
        data.getData();

        double distance = 0;
        // Get LatLong Arrays from data class
        ArrayList<Double> latLong1 = data.getLatLong(p1);
        ArrayList<Double> latLong2 = data.getLatLong(p2);
        if (!(grassHopperEnabled)) {
            // Calculate distance between
            distance = distanceBetween(latLong1.get(0), latLong1.get(1), latLong2.get(0), latLong2.get(1));
            // Format to two decimal places

            DecimalFormat df = new DecimalFormat("#.#"); // Adjusted to remove comma formatting
            String formattedDistance = df.format(distance);
            formattedDistance = formattedDistance.replace(',', '.');
            distance = Double.parseDouble(formattedDistance);
        }

        else {
            distance = getDistanceWithGraphHopper(latLong1, latLong2);
        }

        return distance;

    }

    /**
     * Calculates the distance between two points on the earth.
     *
     * @param lat1 the latitude of the first point
     * @param lon1 the longitude of the first point
     * @param lat2 the latitude of the second point
     * @param lon2 the longitude of the second point
     * @return the distance between the two points in kilometers
     */
    public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the change in coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Calculate the distance using the Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * Calculates the midpoint between two points on the earth.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the midpoint between the two points as an ArrayList of two Doubles,
     *         where the first element is the latitude and the second element is the
     *         longitude
     * @throws IOException if there is an error retrieving the coordinates from the
     *                     data file or making an API call
     */
    public static ArrayList<Double> findMidpoint(Place p1, Place p2) throws IOException {
        ArrayList<Double> midpoint = new ArrayList<>();

        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();

        double dLon = Math.toRadians(lon2 - lon1);

        // Convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        // Intermediate point
        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2),
                Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        // Convert back to degrees
        midpoint.add(Math.toDegrees(lat3));
        midpoint.add(Math.toDegrees(lon3));

        return midpoint;
    }

    public static String printDistance(String p1, String p2, Boolean graphHopperEnabled) throws IOException {
        double distance = getDistance(p1, p2, graphHopperEnabled);
        if (distance >= 1) {
            return distance + " Kilometers";
        } else {
            return distance * 1000 + " Meters";
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(distanceBetween(50.835239, 5.713166, 50.837101, 5.712526));

    }

    /**
     * Launches the GraphHopper server using a subprocess.
     *
     * @return the subprocess that was launched
     * @throws IOException if there is an error launching the subprocess
     */
    public static Process launchGraphHopper() throws IOException {
        Process process = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                // cmd for windows
                String someCommand = "java -Xms1g -Xmx1g -server -Ddw.graphhopper.datareader.file=src/java/graphhopper/Maastricht.osm.pbf -cp src/java/graphhopper/graphhopper.jar com.graphhopper.application.GraphHopperApplication server src\\java\\graphhopper\\config.yml";
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c",
                        "start cmd.exe /k cmd /c " + someCommand);
                process = processBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                // MacOS
                // Get the current directory path
                String currentDirectory = System.getProperty("user.dir");

                // Construct the command to change directory and then execute your Java command
                String command = "cd " + currentDirectory
                        + " && java -Xms1g -Xmx1g -server -Ddw.graphhopper.datareader.file=src/java/graphhopper/Maastricht.osm.pbf -cp src/java/graphhopper/graphhopper.jar com.graphhopper.application.GraphHopperApplication server src/java/graphhopper/config.yml";

                // Use ProcessBuilder to execute the command
                ProcessBuilder processBuilder = new ProcessBuilder("osascript", "-e",
                        "tell application \"Terminal\" to do script \"" + command + "\"");
                processBuilder.inheritIO(); // This makes the terminal inherit the IO of the parent process
                process = processBuilder.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return process;
    }

    public static double getDistanceWithGraphHopper(ArrayList<Double> latLong1, ArrayList<Double> latLong2)
            throws MalformedURLException, IOException {

        double distance = 0;
        // String lat1 = "50.86635198292194";
        // String long1 = "5.704404406327205";
        // String lat2 = "50.8339399220579";
        // String long2 = "5.6609128633869";

        @SuppressWarnings("deprecation")
        URL obj = new URL("http://localhost:8989/route?point=" + latLong1.get(0) + "%2C" + latLong1.get(1) + "&point="
                + latLong2.get(0) + "%2C" + latLong2.get(1) + "&profile=car");
        System.out.println(obj);
        URLConnection yc = obj.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        in.close();
        String jsonString = stringBuilder.toString();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray paths = jsonObject.getJSONArray("paths");
            JSONObject firstPath = paths.getJSONObject(0);
            distance = firstPath.getDouble("distance");
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        String distanceString = Double.toString(distance);
        distanceString = distanceString.replaceAll(",", ".");
        distance = Double.parseDouble(distanceString) / 1000;
        DecimalFormat df = new DecimalFormat("#.#");
        distance = Double.parseDouble(df.format(distance));
        return distance;
    }

}
