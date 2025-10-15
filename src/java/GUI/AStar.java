package src.java.GUI;

import src.java.Main.CalculateDistance;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * AStar class implements the A* search algorithm for finding the shortest path between two places.
 */
public class AStar implements Serializable {
    private final int AVGWALKINGTIME = 70; // Average walking speed in meters/min
    private final Graph graph;
    private List<String> directions;

    public AStar(Graph graph) {
        this.graph = graph;
        directions = new ArrayList<>();
    }

    public List<String> getDirections() {
        return this.directions;
    }

    public void setDirections(List<String> directions) {
        this.directions = directions;
    }

    /**
     * Main method to find the shortest path between two places.
     *
     * @param startPlace The starting place.
     * @param endPlace The ending place.
     * @return A list of places representing the shortest path.
     * @throws Exception If an error occurs during the search.
     */
    public List<Place> findShortestPath(Place startPlace, Place endPlace) throws Exception {
        connectBusStops();

        connectPlaceToGraph(startPlace, 10);
        connectPlaceToGraph(endPlace, 10);

        // Get the current local time
        LocalTime currentTime = LocalTime.now();

        // Round down the seconds by setting them to zero
        LocalTime roundedTime = currentTime.withSecond(0).withNano(0);

        // Define a formatter to ensure output is in HH:mm:ss format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Format the rounded time
        String formattedTime = roundedTime.format(formatter);
        String[] time= formattedTime.split(":");

        return aStarSearch(startPlace, endPlace, LocalTime.of(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2])));
    }

    /**
     * Method to connect a place to the nearest bus stops.
     *
     * @param place The place to connect.
     * @param stops The number of nearest bus stops to connect.
     * @throws Exception If an error occurs while getting the real distance.
     */
    private void connectPlaceToGraph(Place place, int stops) throws Exception {
        List<BusStop> nearestBusStops = graph.findNearestBusStops(place.getLatitude(), place.getLongitude(), stops);
        graph.addVertex(place);
        for (BusStop busStop : nearestBusStops) {
            int walkingDist = getRealDistance(place, busStop);
            long walkingTime = Math.ceilDiv(walkingDist, AVGWALKINGTIME);

            // Add edges for walking between the place and the bus stop
            Edge walkEdge = new Edge(place, busStop, walkingTime, walkingDist, "walk");
            graph.addEdge(walkEdge);

            walkEdge = new Edge(busStop, place, walkingTime, walkingDist, "walk");
            graph.addEdge(walkEdge);
        }
    }

    /**
     * Method to connect all bus stops within 100 meters of each other.
     */
    private void connectBusStops() {
        for (Place p1 : graph.getVertices()) {
             for (Place p2 : graph.getVertices()) {
                if (p2.equals(p1)) {
                    continue;
                }
                int walkingDist = (int) Math.round(CalculateDistance.distanceBetween(p1.lat, p1.lon, p2.lat, p2.lon));
                if (walkingDist <= 100) {
                    long walkingTime = Math.ceilDiv(walkingDist, AVGWALKINGTIME);

                    Edge walkEdge = new Edge(p1, p2, walkingTime, walkingDist, "walk");
                    graph.addEdge(walkEdge);

                    walkEdge = new Edge(p2, p1, walkingTime, walkingDist, "walk");
                    graph.addEdge(walkEdge);
                }
            }
        }
    }


    /**
     * A* search algorithm to find the shortest path between the start and goal places.
     *
     * @param start The starting place.
     * @param goal The goal place.
     * @param startTime The starting time.
     * @return A list of places representing the shortest path.
     */
    private List<Place> aStarSearch(Place start, Place goal, LocalTime startTime) {
        PriorityQueue<SearchNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(searchNode -> searchNode.f));
        Map<Place, Long> priorityMap = new HashMap<>();

        for (Place p : graph.getVertices()) {
            priorityMap.putIfAbsent(p, Long.MAX_VALUE);
        }

        SearchNode startSearchNode = new SearchNode(start, 0, heuristic(start, goal), startTime, 0, null, null);
        priorityMap.put(start, startSearchNode.f);
        openSet.add(startSearchNode);

        while (!openSet.isEmpty()) {
            SearchNode currentSearchNode = openSet.poll();
            if (priorityMap.get(currentSearchNode.place) != currentSearchNode.f) {
                continue;
            }

            if (currentSearchNode.place.equals(goal)) {
                return reconstructPath(currentSearchNode);
            }

            // Loop through all edges of the current node
            for (Edge edge : graph.getEdges(currentSearchNode.place)) {
                if (currentSearchNode.cameFrom != null && edge.getTo().equals(currentSearchNode.cameFrom.place)) {
                    continue;
                }

                long timeCost = 0;
                int distCost = 0;
                String edgeHeadSign = edge.getTripHeadSign();

                if (edgeHeadSign.equals("walk")) {

                    timeCost = currentSearchNode.g + edge.getWalkingTime();
                    distCost = currentSearchNode.dist + edge.getWalkingDist();
                    LocalTime time = currentSearchNode.time.plusMinutes(edge.getWalkingTime());
                    SearchNode searchNode = new SearchNode(edge.getTo(), timeCost, heuristic(edge.getTo(), goal), time, distCost, currentSearchNode, null);
                    if (searchNode.f < priorityMap.get(searchNode.place)) {
                        priorityMap.put(searchNode.place, searchNode.f);
                    }
                    openSet.add(searchNode);
                    continue;
                }

                List<Trip> trips = edge.getTrips();
                Collections.sort(trips);

                // Check all possible trips in one edge
                for (Trip trip : trips) {

                    if (trip.getDepartureTime().compareTo(currentSearchNode.time) >= 0) {
                        timeCost = currentSearchNode.g + GraphBuilder.calculateTimeDifference(currentSearchNode.time, trip.getArriveTime());
                        distCost = currentSearchNode.dist + trip.getShapeDistTraveled();
                        LocalTime time = trip.getArriveTime();
                        SearchNode searchNode = new SearchNode(edge.getTo(), timeCost, heuristic(edge.getTo(), goal), time, distCost, currentSearchNode, trip);
                        if (searchNode.f < priorityMap.get(searchNode.place)) {
                            priorityMap.put(searchNode.place, searchNode.f);
                        }
                        openSet.add(searchNode);
                        break;
                    }
                }
            }
        }

        return Collections.emptyList();
    }


    /**
     * Heuristic function to estimate the distance between the current place and the goal.
     *
     * @param place The current place.
     * @param goal The goal place.
     * @return The estimated distance.
     */
    private long heuristic(Place place, Place goal) {
        double dist = CalculateDistance.distanceBetween(place.getLatitude(), place.getLongitude(), goal.getLatitude(), goal.getLongitude());
        return Math.round(dist / AVGWALKINGTIME);
    }


    /**
     * Method to reconstruct the path from the goal to the start.
     *
     * @param currentSearchNode The goal search node.
     * @return A list of places representing the shortest path.
     */
    private List<Place> reconstructPath(SearchNode currentSearchNode) {
        List<SearchNode> pathNodes = new ArrayList<>();

        while (currentSearchNode != null) {
            pathNodes.add(currentSearchNode);
            currentSearchNode = currentSearchNode.cameFrom;
        }
        Collections.reverse(pathNodes);

        constructDirections(pathNodes);
        return constructShapes(pathNodes);
    }

    /**
     * Method to construct the directions from the path nodes.
     *
     * @param pathNodes The list of search nodes representing the path.
     */
    private void constructDirections(List<SearchNode> pathNodes) {
        int size = pathNodes.size();
        String headSign = "";
        for (int i = 0; i < size; i += 1) {
            StringBuilder sb = new StringBuilder();
            SearchNode node = pathNodes.get(i);
            if (i == 0) {
                sb.append(node.time).append("--").append(node.place.getZipCode());
                directions.add(sb.toString());
                continue;
            }
            if (node.trip == null) {
                sb.append("Walk about ")
                        .append(node.g -  pathNodes.get(i - 1).g)
                        .append(" min, ")
                        .append(node.dist - pathNodes.get(i - 1).dist)
                        .append(" m");
                directions.add(sb.toString());
                sb = new StringBuilder();
            } else if (!node.trip.getTripHeadSign().equals(headSign)) {
                sb.append("Transfer to ").append(node.trip.getTripHeadSign()).append(", departs at ").append(node.trip.getDepartureTime()).append(", ").append(pathNodes.get(i - 1).place);
                directions.add(sb.toString());
                headSign = node.trip.getTripHeadSign();
                sb = new StringBuilder();
            }
            if (i == size - 1) {
                sb.append(node.time).append("--").append(node.place.getZipCode());
                directions.add(sb.toString());
                long hours = node.g / 60;
                long minutes = node.g % 60;
                if (hours == 0) {
                    directions.add("Total time cost: " + node.g + " minutes");
                } else {
                    directions.add("Total time cost: " + hours + " hours, " + minutes + " minutes");
                }
                continue;
            }
            sb.append(node.time).append("--").append(node.place);
            directions.add(sb.toString());
        }
    }

    /**
     * Method to construct the shapes from the path nodes.
     *
     * @param pathNodes The list of search nodes representing the path.
     * @return A list of places representing the shapes.
     */
    private List<Place> constructShapes(List<SearchNode> pathNodes) {
        List<Place> stops = new ArrayList<>();
        stops.add(pathNodes.get(0).place);
        List<Integer> shapeIDs = new ArrayList<>();

        for (SearchNode node : pathNodes) {
            if (node.trip == null) {
                shapeIDs.add(0);
            } else {
                shapeIDs.add(node.trip.getShapeId());
            }
        }

        Place start = null;
        Place end = null;
        int prev = 0;

        for (int i = 1; i < shapeIDs.size(); i += 1) {
            int curr = shapeIDs.get(i);

            if (curr == 0) {

                if (prev != 0) {
                    end = pathNodes.get(i - 1).place;

                    try {
                        List<BusRouteShape> shapes = GraphBuilder.getBusRouteShapes(prev);
                        stops.addAll(extractShapeSegment(start, end, shapes));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                stops.add(pathNodes.get(i).place);

            } else {
                if (prev == 0) {
                    start = pathNodes.get(i - 1).place;
                } else if (prev != curr) {
                    end = pathNodes.get(i - 1).place;

                    try {
                        List<BusRouteShape> shapes = GraphBuilder.getBusRouteShapes(prev);
                        stops.addAll(extractShapeSegment(start, end, shapes));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    start = pathNodes.get(i).place;
                }
            }

            prev = curr;
        }

        return stops;
    }

    /**
     * Method to extract the shape segment between the start and end places.
     *
     * @param start The starting place.
     * @param end The ending place.
     * @param shapes The list of bus route shapes.
     * @return A list of places representing the shape segment.
     */
    private List<Place> extractShapeSegment(Place start, Place end, List<BusRouteShape> shapes) {
        List<Place> segment = new ArrayList<>();
        int startIndex = findClosestShapePoint(start, shapes);
        int endIndex = findClosestShapePoint(end, shapes);
        List<BusRouteShape> temp;

        if (startIndex > endIndex) {
            temp = shapes.subList(endIndex, startIndex + 1);
        } else {
            temp = shapes.subList(startIndex, endIndex + 1);
        }

        for (BusRouteShape shape : temp) {
            segment.add(new Place(shape.shapePtLat, shape.shapePtLon));
        }

        return segment;
    }


    /**
     * Method to find the closest shape point to a place.
     *
     * @param place The place to find the closest shape point.
     * @param shapes The list of bus route shapes.
     * @return The index of the closest shape point.
     */
    private int findClosestShapePoint(Place place, List<BusRouteShape> shapes) {
        int index = -1;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < shapes.size(); i += 1) {
            BusRouteShape shape = shapes.get(i);
            double distance = CalculateDistance.distanceBetween(place.lat, place.lon, shape.shapePtLat, shape.shapePtLon);
            if (distance < minDistance) {
                minDistance = distance;
                index = i;
            }
        }

        return index;
    }

    /**
     * Method to get the real walking distance between two places using Google Maps API.
     *
     * @param origin The origin place.
     * @param destination The destination place.
     * @return The real walking distance in meters.
     * @throws Exception If an error occurs while getting the distance.
     */
    public static int getRealDistance(Place origin, Place destination) throws Exception {
        String apiKey = "AIzaSyAZwfzWK71qIgXSleA-02n-oXfo5OjOhhU";
        String originStr = origin.lat + "," + origin.lon;
        String destinationStr = destination.lat + "," + destination.lon;
        String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + originStr +
                "&destinations=" + destinationStr + "&mode=walking&key=" + apiKey;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(content.toString());
        JSONArray rows = jsonResponse.getJSONArray("rows");
        JSONObject elements = rows.getJSONObject(0);
        JSONArray element = elements.getJSONArray("elements");
        JSONObject distance = element.getJSONObject(0).getJSONObject("distance");

        return distance.getInt("value");
    }

    public static void main(String[] args) throws Exception {
        Graph graph = new Graph();
        GraphBuilder graphBuilder = new GraphBuilder(graph);
        graphBuilder.getBusStops();
        Place startPlace  = new Place(50.8419194317073, 5.64520881707317);
        Place endPlace = new Place(50.8864, 5.7145);

        AStar aStar = new AStar(graph);
        List<Place> path = aStar.findShortestPath(startPlace, endPlace);

        if (path.isEmpty()) {
            System.out.println("No path found.");
        } else {
            System.out.println("Path found: ");
            for (String s : aStar.directions) {
                System.out.println(s);
            }

        }
    }
}
