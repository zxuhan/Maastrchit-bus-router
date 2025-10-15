package src.java.GUI;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import src.java.API.RetrievePostalWithAPI;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Data {

    private static ArrayList<String> zipCodes = new ArrayList<String>();
    private static ArrayList<Double> latitudes = new ArrayList<Double>();
    private static ArrayList<Double> longitudes = new ArrayList<Double>();

    /**
     * Reads the data from the Excel file and stores it in the zipCodes and latitude arrays.
     */
    public static void getData() {
        boolean isFirstRow = true; // Flag to indicate the first row
        try (FileInputStream fis = new FileInputStream("src/java/Resources/relevantPostalCodes.xlsx");
        // try (FileInputStream fis = new FileInputStream("src/java/Resources/MassZipLatLon.xlsx");

             XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue; // Skip processing the first row
                }
                for (Cell cell : row) {
                    switch (cell.getColumnIndex()) {
                        case 0: // Zip Code Column
                            zipCodes.add(cell.getStringCellValue());
                            break;
                        case 1: // Latitude Column
                            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                latitudes.add(cell.getNumericCellValue());
                            } else {
                                throw new IllegalArgumentException("Latitude column must be numeric");
                            }
                            break;
                        case 2: // Longitude Column
                            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                longitudes.add(cell.getNumericCellValue());
                            } else {
                                throw new IllegalArgumentException("Longitude column must be numeric");
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            // Handle file IO error
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getZipCodes() {
        return zipCodes;
    }
    public static ArrayList<Double> getLatitudes() {
        return latitudes;
    }
    public static ArrayList<Double> getLongitudes() {
        return longitudes;
    }

    public static ArrayList<Double> getLatLong(String zipCode) throws IOException {
        int index = Collections.binarySearch(zipCodes, zipCode);
        ArrayList<Double> latLong = new ArrayList<Double>();
        if(index >= 0){
            latLong.add(latitudes.get(index));
            latLong.add(longitudes.get(index));
            return latLong;
        }
        else{
            System.out.println("attempting to get" + zipCode);
            RetrievePostalWithAPI api = new RetrievePostalWithAPI();
            latLong = api.getPCode(zipCode);
            if (latLong.size() > 0) {
                return latLong;
            }
        }
        return null;
    }

    public static String findClosestZipCode(double latitude, double longitude) {
        double minDistance = Double.MAX_VALUE;
        String closestZipCode = null;
        for (int i = 0; i < latitudes.size(); i++) {
            double lat = latitudes.get(i);
            double lon = longitudes.get(i);
            double distance = haversine(latitude, longitude, lat, lon);
            if (distance < minDistance) {
                minDistance = distance;
                closestZipCode = zipCodes.get(i);
            }
        }
        return closestZipCode;
    }

    /**
     * Calculates the Haversine distance between two points on the Earth specified by latitude and longitude.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in kilometers
    }

    public static void main(String[] args) {
        // Load the data
        getData();
        // Test the findClosestZipCode method
        double testLatitude = 42.3601;
        double testLongitude = -71.0589;
        String closestZipCode = findClosestZipCode(testLatitude, testLongitude);
        System.out.println("Closest Zip Code: " + closestZipCode);
    }


}
