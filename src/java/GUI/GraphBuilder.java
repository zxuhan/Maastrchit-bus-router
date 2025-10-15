package src.java.GUI;

import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The GraphBuilder class is responsible for building the graph by retrieving data from the database.
 */
public class GraphBuilder {
    private final Graph g;

    public GraphBuilder(Graph g) {this.g = g;}

    /**
     * Retrieves bus stop data from the database and adds it to the graph.
     *
     * @throws SQLException If an SQL error occurs.
     */
    public void getBusStops() throws SQLException {
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon, trip_id, stop_sequence, arrival_time, departure_time, trip_headsign, shape_dist_traveled, shape_id " +
                "FROM maas_stops_time " +
                "WHERE stop_name LIKE '%Maastricht,%' AND trip_id NOT IN (SELECT trip_id FROM maas_stops_time mst WHERE arrival_time LIKE '24:%' OR arrival_time LIKE '25:%') " +
                "ORDER BY trip_id, stop_sequence; ";

        BusStop previousStop = null;
        int previousTripId = -1;
        Trip prevTrip = null;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int stopId = rs.getInt("stop_id");
                String stopName = rs.getString("stop_name");
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");
                int tripId = rs.getInt("trip_id");
                int stopSequence = rs.getInt("stop_sequence");
                String arriveTime = rs.getString("arrival_time");
                String departureTime = rs.getString("departure_time");
                String tripHeadSign = rs.getString("trip_headsign");
                int shapeDistTraveled = rs.getInt("shape_dist_traveled");
                int shapeId = rs.getInt("shape_id");

                BusStop currentStop = new BusStop(stopId, stopName, stopLat, stopLon);
                Trip currTrip = new Trip(tripId, convertToLocalTime(arriveTime), convertToLocalTime(departureTime), tripHeadSign, shapeDistTraveled, shapeId);
                g.addVertex(currentStop);

                if (previousStop != null && prevTrip != null && tripId == previousTripId) {
                    LocalTime aTime = convertToLocalTime(arriveTime);
                    LocalTime dTime = prevTrip.getDepartureTime();
                    int distance = shapeDistTraveled - prevTrip.getShapeDistTraveled();
                    Trip trip = new Trip(tripId, aTime, dTime, tripHeadSign, distance, shapeId);
                    g.addEdge(previousStop, currentStop, trip, tripHeadSign);
                }

                previousStop = currentStop;
                previousTripId = tripId;
                prevTrip = currTrip;
            }
        }
    }


    /**
     * Retrieves the shape data for a given shape ID from the database.
     *
     * @param shapeId The shape ID to retrieve data for.
     * @return A list of BusRouteShape objects representing the shape.
     * @throws SQLException If an SQL error occurs.
     */
    public static List<BusRouteShape> getBusRouteShapes(int shapeId) throws SQLException {
        String query = "SELECT shape_id, shape_pt_sequence, shape_pt_lat, shape_pt_lon, shape_dist_traveled " +
                "FROM shapes " +
                "WHERE shape_id = ?; ";

        List<BusRouteShape> shapes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, shapeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int shapeID = rs.getInt("shape_id");
                    int shapePtSequence = rs.getInt("shape_pt_sequence");
                    double shapePtLat = rs.getDouble("shape_pt_lat");
                    double shapePtLon = rs.getDouble("shape_pt_lon");
                    double shapeDistTraveled = rs.getDouble("shape_dist_traveled");

                    shapes.add(new BusRouteShape(shapeID, shapePtSequence, shapePtLat, shapePtLon, shapeDistTraveled));
                }
            }
        }

        return shapes;
    }

    /**
     * Converts a time string to a LocalTime object.
     *
     * @param timeStr The time string in "HH:mm:ss" format.
     * @return The corresponding LocalTime object.
     */
    public static LocalTime convertToLocalTime(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(timeStr, formatter);
    }

    /**
     * Calculates the difference in minutes between two LocalTime objects.
     *
     * @param startTime The start time.
     * @param endTime The end time.
     * @return The difference in minutes.
     */
    public static long calculateTimeDifference(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes();
    }

    public static void main(String[] args) throws SQLException {
        Graph g = new Graph();
        GraphBuilder graphBuilder = new GraphBuilder(g);
        graphBuilder.getBusStops();

        List<BusRouteShape> shapes = GraphBuilder.getBusRouteShapes(1131287);
        for (BusRouteShape shape : shapes) {
            System.out.println("(" + shape.shapePtLat + ", " + shape.shapePtLon + ")");
        }
    }

}