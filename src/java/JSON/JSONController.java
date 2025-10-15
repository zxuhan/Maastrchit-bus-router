package src.java.JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import src.java.GUI.Data;
import src.java.GUI.Place;
import src.java.Singletons.FileManager;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class JSONController {
    private static final String NAME_API_URL_TEMPLATE = "https://geocode.maps.co/reverse?lat=%f&lon=%f&api_key=6666d2ba4331d378476246ngzcb1fcc";
    private static final String API_URL_TEMPLATE = "https://api.geoapify.com/v1/geocode/reverse?lat=%f&lon=%f&apiKey=c9a518e5482a431f8c1dbd0d894ef95e";

    private static final String AMENITY_PATH = "src/java/JSON/amenity.geojson";
    private static final String SHOP_PATH = "src/java/JSON/shop.geojson";
    private static final String TOURISM_PATH = "src/java/JSON/tourism.geojson";

    private static final String SCHOOL_AMENITY = "school";
    private static final String ATM_AMENITY = "atm";
    private static final String FUEL_AMENITY = "fuel";
    private static final String WASTE_AMENITY = "waste_basket";

    private static final String SUPERMARKET_SHOP = "supermarket";

    private static final double EARTH_RADIUS = 6371e3;


    public static String[] getPostalCode(double latitude, double longitude) {
        String[] data = new String[2];
        String apiUrl = String.format(API_URL_TEMPLATE, latitude, longitude);

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());
            JsonNode featuresNode = rootNode.get("features");

            if (featuresNode != null && featuresNode.isArray() && featuresNode.size() > 0) {
                JsonNode firstFeature = featuresNode.get(0);
                JsonNode propertiesNode = firstFeature.get("properties");

                if (propertiesNode != null) {
                    JsonNode postcodeNode = propertiesNode.get("postcode");
                    JsonNode nameNode = propertiesNode.get("name");

                    if (postcodeNode != null) {
                        data[0] = postcodeNode.asText();
                        if(nameNode == null){
                            data[1] = getLocationName(latitude, longitude);
                        }
                        //System.out.println(data[1]);
                        return data;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocationName(double latitude, double longitude) {
        String apiUrl = String.format(NAME_API_URL_TEMPLATE, latitude, longitude);
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.toString());
            JsonNode displayNameNode = rootNode.get("display_name");

            if (displayNameNode != null) {
                return displayNameNode.asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of places from the GEOJSON files provided
     * @param isAmenity specifies whether the palces are amenities or shops
     * @param type specifies the type of places to be returned eg. supermarket, shop, school, etc.
     * @param useAPI Whether the method is to use the API to supplement any missing information (should be left false because it is very slow)
     * @return Returns a list of places that the user searched for
     * @throws IOException
     * @throws InterruptedException
     */
    public List<Place> getPlacesFromGeoJSON(Boolean isAmenity, String type, boolean useAPI) throws IOException, InterruptedException {
        String filePath;
        if(isAmenity){
            filePath = AMENITY_PATH;
        }else{
            filePath = SHOP_PATH;
        }
        // Data.getData();
        List<Place> places = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(filePath));

        JsonNode features = rootNode.get("features");
        if (features != null && features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode properties = feature.get("properties");
                if(isAmenity) {
                    if (properties != null) {
                        JsonNode amenityNode = properties.get("amenity");
                        if (amenityNode != null && type.equals(amenityNode.asText())) {
                            // Exclude waste baskets with waste type dog_excrement
                            if ("waste_basket".equals(type)) {
                                JsonNode wasteNode = properties.get("waste");
                                if (wasteNode != null && "dog_excrement".equals(wasteNode.asText())) {
                                    continue;  // Skip this feature
                                }
                            }

                            String name = properties.has("name") ? properties.get("name").asText() : "Unnamed " + capitalizeFirstLetter(type);
                            JsonNode geometry = feature.get("geometry");
                            if (geometry != null && "Point".equals(geometry.get("type").asText())) {
                                JsonNode coordinates = geometry.get("coordinates");
                                if (coordinates != null && coordinates.isArray() && coordinates.size() == 2) {
                                    double longitude = coordinates.get(0).asDouble();
                                    double latitude = coordinates.get(1).asDouble();
                                    if (useAPI) {
                                        String[] data = getPostalCode(latitude, longitude);
                                        String postcode = data[0];
                                        if (name.equals("Unnamed " + capitalizeFirstLetter(type)) && data[1] != null) {
                                            name = data[1];
                                        }
                                        places.add(new Place(name, postcode, latitude, longitude));
                                        Thread.sleep(200);
                                    } else {
                                        String postcode = Data.findClosestZipCode(latitude, longitude);
                                        places.add(new Place(name, postcode, latitude, longitude));
                                    }

                                }
                            }
                        }
                    }
                }else{
                    JsonNode shopNode = properties.get("shop");
                    if (shopNode != null && type.equals(shopNode.asText())) {

                        String name = properties.has("name") ? properties.get("name").asText() : "Unnamed " + capitalizeFirstLetter(type);
                        JsonNode geometry = feature.get("geometry");
                        if (geometry != null && "Point".equals(geometry.get("type").asText())) {
                            JsonNode coordinates = geometry.get("coordinates");
                            if (coordinates != null && coordinates.isArray() && coordinates.size() == 2) {
                                double longitude = coordinates.get(0).asDouble();
                                double latitude = coordinates.get(1).asDouble();
                                if (useAPI) {
                                    String[] data = getPostalCode(latitude, longitude);
                                    String postcode = data[0];
                                    if (name.equals("Unnamed " + capitalizeFirstLetter(type)) && data[1] != null) {
                                        name = data[1];
                                    }
                                    places.add(new Place(name, postcode, latitude, longitude));
                                    Thread.sleep(200);
                                } else {
                                    String postcode = Data.findClosestZipCode(latitude, longitude);
                                    places.add(new Place(name, postcode, latitude, longitude));
                                }

                            }
                        }
                    }
                }
            }
        }
        return places;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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

    public List<Place> getNearbyAmenities(String filePath, String postCode, double radius) throws IOException {
        // Data.getData();
        ArrayList<Double> LatLon = Data.getLatLong(postCode);
        Double latitude = LatLon.get(0);
        Double longitude = LatLon.get(1);
        List<Place> places = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(filePath));

        JsonNode features = rootNode.get("features");
        if (features != null && features.isArray()) {
            for (JsonNode feature : features) {
                JsonNode properties = feature.get("properties");
                if (properties != null) {
                    JsonNode amenityNode = properties.get("amenity");
                    if (amenityNode != null) {
                        // Exclude waste baskets with waste type dog_excrement
                        if ("waste_basket".equals(amenityNode.asText())) {
                            JsonNode wasteNode = properties.get("waste");
                            if (wasteNode != null && "dog_excrement".equals(wasteNode.asText())) {
                                continue;  // Skip this feature
                            }
                        }

                        JsonNode geometry = feature.get("geometry");
                        if (geometry != null && "Point".equals(geometry.get("type").asText())) {
                            JsonNode coordinates = geometry.get("coordinates");
                            if (coordinates != null && coordinates.isArray() && coordinates.size() == 2) {
                                double placeLongitude = coordinates.get(0).asDouble();
                                double placeLatitude = coordinates.get(1).asDouble();

                                // Calculate the distance
                                double distance = vincentyDistance(latitude, longitude, placeLatitude, placeLongitude);

                                // Check if the distance is within the specified radius
                                if (distance <= radius) {
                                    String name = properties.has("name") ? properties.get("name").asText() : "Unnamed " + capitalizeFirstLetter(amenityNode.asText());
                                    String postcode = Data.findClosestZipCode(placeLatitude, placeLongitude);
                                    Place place = new Place(name, postcode, placeLatitude, placeLongitude);
                                    place.setType(amenityNode.asText());
                                    places.add(place);
                                }
                            }
                        }
                    }
                }
            }
        }
        return places;
    }

    /**
     * Returns the amenity score for the given post code within a radius
     * @param filePath Path to the geojson file (Usually amenities.geojson)
     * @param postCode Post code to calculate the amenity score for
     * @param radius Radius of search in meters
     * @return Returns the integer amenity score for the post code
     * @throws IOException
     */
    public int calculateTotalScore(String filePath, String postCode, double radius) throws IOException {
        List<Place> nearbyAmenities = getNearbyAmenities(filePath, postCode, radius);
        int totalScore = 0;

        for (Place place : nearbyAmenities) {
            Amenity amenity = Amenity.fromString(place.getType());
            if (amenity != null) {
                totalScore += amenity.getPoints();
            }
        }

        return totalScore;
    }


    public static HashMap<String, Integer> calculateScores(int radius) {
        Data data = new Data();
        data.getData();
        JSONController controller = new JSONController();
        HashMap<String, Integer> scores = new HashMap<>();
        ArrayList<String> zipCodes = new ArrayList<>(data.getZipCodes()); // Create a copy of the list

        for (String zipCode : zipCodes) {
            try {
                int score = controller.calculateTotalScore(AMENITY_PATH, zipCode, radius);
                //FileManager.saveStringToFile(String.valueOf(score), "scores.txt");
                System.out.println(zipCode + ": " + score);
                scores.put(zipCode, score);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error calculating score for " + zipCode);
                scores.put(zipCode, 0); // Add a default value or handle the error as needed
            }
        }
        scores = sortByValues(scores);
        return scores;
    }

    public static LinkedHashMap<String, Integer> sortByValues(HashMap<String, Integer> map) {
        return map.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static void numberScores(String readFilePath, String storeFilePath) throws IOException {
        JSONController controller = new JSONController();
        // Example usage
        HashMap<String, Integer> loadedScores = FileManager.readLinkedHashMapFromFile(readFilePath);
        Set<String> keySet = loadedScores.keySet();
        String[] keys = keySet.toArray(new String[0]);

        // Create a new HashMap to assign numbers to each post code
        LinkedHashMap<String, Integer> numberedZipCodes = new LinkedHashMap<>();
        for(int i = 0; i < keys.length; i++){
            numberedZipCodes.put(keys[i], i+1);
        }

        FileManager.saveHashMapToFile(numberedZipCodes, storeFilePath);
    }


    public static void main(String[] args) throws IOException {
         numberScores("scores-1000.txt", "numbered-scores-1000.txt");
    }


}