package src.java.DisplayerAccessibility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.java.Singletons.FileManager;

//Data for postcode polygons from: https://service.pdok.nl/cbs/postcode6/atom/postcode6_volledige_postcode.xml
public class RDToWGS84 {
    Map<String, List<List<Double[]>>> polygonsMap = new HashMap<>();
    //Full credits to: https://github.com/glenndehaan/rd-to-wgs84/blob/master/src/core.js for convertRDToWGS84 function
    public double[] convertRDToWGS84(double x, double y) {
        double dX = (x - 155000) * Math.pow(10, -5);
        double dY = (y - 463000) * Math.pow(10, -5);

        double sumN = (3235.65389 * dY) + (-32.58297 * Math.pow(dX, 2)) + (-0.2475 * Math.pow(dY, 2)) + 
                      (-0.84978 * Math.pow(dX, 2) * dY) + (-0.0655 * Math.pow(dY, 3)) + 
                      (-0.01709 * Math.pow(dX, 2) * Math.pow(dY, 2)) + (-0.00738 * dX) + 
                      (0.0053 * Math.pow(dX, 4)) + (-0.00039 * Math.pow(dX, 2) * Math.pow(dY, 3)) + 
                      (0.00033 * Math.pow(dX, 4) * dY) + (-0.00012 * dX * dY);

        double sumE = (5260.52916 * dX) + (105.94684 * dX * dY) + (2.45656 * dX * Math.pow(dY, 2)) + 
                      (-0.81885 * Math.pow(dX, 3)) + (0.05594 * dX * Math.pow(dY, 3)) + 
                      (-0.05607 * Math.pow(dX, 3) * dY) + (0.01199 * dY) + 
                      (-0.00256 * Math.pow(dX, 3) * Math.pow(dY, 2)) + (0.00128 * dX * Math.pow(dY, 4)) + 
                      (0.00022 * Math.pow(dY, 2)) + (-0.00022 * Math.pow(dX, 2)) + 
                      (0.00026 * Math.pow(dX, 5));

        double latitude = 52.15517 + (sumN / 3600);
        double longitude = 5.387206 + (sumE / 3600);

        return new double[]{latitude, longitude};
    }

    public static void main(String[] args) {
        // RDToWGS84 parser = new RDToWGS84();
        // parser.getPolyGon();
        // ArrayList<String> file = parser.readFileLineByLine("./polygons.csv");
        // for (String string: file) {
        //     parser.parsePolygon(string.substring(7), string.split(",")[0]);
        // }
        // FileManager.getInstance().serializeObject(parser.polygonsMap, "polygonsMap", "polygonsMap.ser");
        String filePath = "/home/tristanko/Stiahnuté/scores.txt";
        
        try {
            HashMap<String, Integer> hashMap = convertFileToHashMap(filePath);
            System.out.println("");
            FileManager.getInstance().serializeObject(hashMap, "scores", "scores.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> convertFileToHashMap(String filePath) throws IOException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        
        while ((line = reader.readLine()) != null) {
            String[] keyValue = line.replace("\"", "").split(":");
            if (keyValue.length == 2) {
                try {
                    String key = keyValue[0];
                    Integer value = Integer.parseInt(keyValue[1]);
                    hashMap.put(key, value);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line due to invalid number format: " + line);
                }
            }
        }
        
        reader.close();
        return hashMap;
    }

    public ArrayList<ArrayList<Double[]>> getPolyGon() {
        if (!this.polygonsMap.isEmpty()) {
            // return polygons;
        }
        ArrayList<ArrayList<Double[]>> polygons = new ArrayList<>();
        ArrayList<String> lines = readFileLineByLine("./coordinates.csv");
        
        for (String line : lines) {
            ArrayList<Double[]> polygon = new ArrayList<>();
            String[] parts = line.split("-");
            
            if (parts.length != 2) {
                polygon.add(new Double[]{-1.0, -1.0});
            } else {
                String coordinatesString = parts[1];
                String[] coordinatePairs = coordinatesString.split(";");
                
                for (String pair : coordinatePairs) {
                    String[] latLng = pair.split(",");
                    if (latLng.length != 2) {
                        continue;
                    }
                    Double latitude = Double.parseDouble(latLng[0]);
                    Double longitude = Double.parseDouble(latLng[1]);
                    polygon.add(new Double[]{latitude, longitude});
                }
            }
            polygons.add(polygon);
        }
        
        return polygons;
    }

    public void parsePolygon(String polygonString, String postalCode) {
        List<List<Double[]>> polygonsList = new ArrayList<>();

        polygonString = polygonString.replace("MULTIPOLYGON(((", "").replace(")))", "").replace("POLYGON", "");
        String[] polygons = polygonString.split("\\)\\s*,\\s*\\(");
        
        for (String polygon : polygons) {
            polygon = polygon.replace("(", "").replace(")", "");
            String[] coordinatePairs = polygon.split("\\s*,\\s*");
            List<Double[]> coordinates = new ArrayList<>();

            for (String pair : coordinatePairs) {
                if (pair.isEmpty()) {
                    continue;
                }
                pair = pair.replaceAll("\"", "");
                String[] coords = pair.split("\\s+");
                try {
                    double rdX = Double.parseDouble(coords[0]);
                    double rdY = Double.parseDouble(coords[1]);
                    double[] wgs84 = convertRDToWGS84(rdX, rdY);
                    coordinates.add(new Double[]{wgs84[0], wgs84[1]});
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing coordinate pair: " + pair);
                    e.printStackTrace();
                }
            }

            polygonsList.add(coordinates);
        }

        this.polygonsMap.put(postalCode, polygonsList);
    }

    public void writeCoordinatesToCSV(String postalCode, ArrayList<Double[]> coordinates, String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        sb.append(postalCode).append("-");
        if ((coordinates != null)) {
            for (Double[] coord : coordinates) {
                sb.append(coord[0]).append(",").append(coord[1]).append(";");
            }
        }

        sb.deleteCharAt(sb.length() - 1).append("\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(sb.toString());
        }
    }

    public ArrayList<String> readFileLineByLine(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine())!= null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}