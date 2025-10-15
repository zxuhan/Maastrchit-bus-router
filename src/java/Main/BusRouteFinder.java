package src.java.Main;

import java.util.List;

import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import src.java.GUI.Place;
import src.java.Database.DatabaseController;
import src.java.Singletons.ExceptionManager;

public class BusRouteFinder {
    public List<Place> getShapes(ArrayList<Double> depCoords, ArrayList<Double> desCoords, DatabaseController databaseController) throws Exception {
        ClosestBusStop stopFinder = new ClosestBusStop();
        ArrayList<BusStop> busStopsDep = new ArrayList<>();
        ArrayList<BusStop> busStopsDes = new ArrayList<>();
        BusRouteFinder tripFinder = new BusRouteFinder();
        ArrayList<Trip> result  =  new ArrayList<>();
        int tripId = -1;

        try {
            busStopsDep = stopFinder.findClosestBusStop(depCoords, databaseController);
            busStopsDes = stopFinder.findClosestBusStop(desCoords, databaseController);
            ArrayList<Trip> tripsList = tripFinder.getTripId(busStopsDes, busStopsDep, databaseController);
            result = tripFinder.calculateTripLength(tripsList, databaseController);
            tripId = result.get(0).tripId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (busStopsDep.isEmpty()) {
            throw new IllegalArgumentException("busStops array is empty");
        }

        if (tripId == -1) {
            ExceptionManager.showError("TripId error", "Problem", "Cannot find valid tripId, just walk", AlertType.ERROR);
            throw new IllegalArgumentException("tripId is null");
        }
        return getPlacesWithDeviation(tripId, result, databaseController);

        // List<Place> list = new ArrayList<>();
        // int shapeId = getShapeId(tripId, databaseController);
        // Trip trip = result.get(0);
        // ArrayList<String> shapes = getShapes(shapeId, databaseController);
        // ArrayList<BusStop> stops = getLatLonStop(trip.stopIdDep(), trip.stopIdDes(), databaseController);
        // for (String string: shapes) {
        //     double lon = Double.parseDouble(string.split(";")[1].split(":")[1].split(" ")[1]);
        //     double lat = Double.parseDouble(string.split(";")[2].split(":")[1].split(" ")[1]);
        //     Place place = new Place(lon, lat);
        //     list.add(place);
        // }
        // return list;
    }

    public List<Place> getPlacesWithDeviation(int tripId, ArrayList<Trip> result, DatabaseController databaseController) throws Exception {
        int shapeId = getShapeId(tripId, databaseController);
        Trip trip = result.get(0); // Assuming result is defined elsewhere
        ArrayList<String> shapes = getShapes(shapeId, databaseController);
        ArrayList<BusStop> stops = getLatLonStop(trip.stopIdDep(), trip.stopIdDes(), databaseController);

        List<Place> list = new ArrayList<>();
        boolean startCounting = false;
        double deviationMeters = 0.160; // Deviation in meters

        for (String string : shapes) {
            double lat = Double.parseDouble(string.split(";")[1].split(":")[1].split(" ")[1]);
            double lon = Double.parseDouble(string.split(";")[2].split(":")[1].split(" ")[1]);

            // Calculate distance between stop and shape
            double distance = CalculateDistance.distanceBetween(stops.get(0).getLat(), stops.get(0).getLon(), lat, lon);

            if (distance <= deviationMeters) {
                startCounting = true;
            }
            if (startCounting) {
                Place place = new Place(lat, lon);
                list.add(place);
            }

            distance = CalculateDistance.distanceBetween(stops.get(1).getLat(), stops.get(1).getLon(), lat, lon);

            if (distance <= deviationMeters) {
                break;
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        BusRouteFinder finder = new BusRouteFinder();
        // ArrayList<Double> departureCoords = new ArrayList<>();
        // ArrayList<Double> destinationCoords = new ArrayList<>();
        // departureCoords.add(5.6628118700314545);
        // departureCoords.add( 50.857339137628095);
        // destinationCoords.add(5.807836);
        // destinationCoords.add( 50.857729);
        DatabaseController databaseController = new DatabaseController();
        // finder.getShapes(departureCoords,destinationCoords, databaseController);
        ArrayList<Trip> trips = new ArrayList<>();

        // trips.add(new Trip(178415079, 2578159, 2578395, -1));
        trips.add(new Trip(178502463, 2578333, 2578133, -1));
        ArrayList<Trip> result = finder.calculateTripLength(trips, databaseController);
        System.out.println("");

    }

    public ArrayList<BusStop> getLatLonStop(int stopDep, int stopDes, DatabaseController databaseController) {
        String query = "SELECT stop_lat, stop_lon FROM stops WHERE stop_id = " + stopDep;
        ArrayList<String> list = new ArrayList<>();
        ArrayList<BusStop> stops = new ArrayList<>();
        try {
            list = databaseController.executeFetchQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] split = list.get(0).split(";");
        double latDep = Double.parseDouble(split[0].split(":")[1].trim());
        double lonDep = Double.parseDouble(split[1].split(":")[1].trim());
        BusStop stop = new BusStop("dep", latDep, lonDep, 0.0);
        stops.add(stop);
        query = "SELECT stop_lat, stop_lon FROM stops WHERE stop_id = " + stopDes;

        try {
            list = databaseController.executeFetchQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        split = list.get(0).split(";");
        Double latDes = Double.parseDouble(split[0].split(":")[1].trim());
        Double lonDes = Double.parseDouble(split[1].split(":")[1].trim());
        stop = new BusStop("des", latDes, lonDes, 0.0);
        stops.add(stop);
        return stops;
    }

    public ArrayList<String> getShapes(int shapeId, DatabaseController databaseController ) throws Exception {
        ArrayList<String> list = databaseController.executeFetchQuery(
        "SELECT shape_id, shape_pt_lat, shape_pt_lon FROM shapes WHERE shape_id = " + shapeId
        );
        return list;

    }

    public int getShapeId(int tripId, DatabaseController databaseController ) throws Exception{
        ArrayList<String> list = databaseController.executeFetchQuery(
        "SELECT shape_id FROM trips WHERE trip_id = " + tripId
        );

        return Integer.parseInt(list.get(0).split(":")[1].split(";")[0].split(" ")[1]);
    }

    public ArrayList<Trip> calculateTripLength(ArrayList<Trip> trips, DatabaseController databaseController) throws Exception {
        ArrayList<Trip> processedTrips = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME.withResolverStyle(ResolverStyle.LENIENT);

        for (Trip trip : trips) {
            long duration = 9999999;
            boolean startCounting = false;
            LocalTime timePrevDep = null;
            
            String query = (
                "SELECT stop_id, arrival_time, departure_time FROM stop_times " +
                "WHERE trip_id = " + trip.tripId() + " ORDER BY stop_sequence ASC"
            );
            ArrayList<String> list = databaseController.executeFetchQuery(query);
            
            for (String row : list) {
                String[] split = row.split(";");
                int stopId = Integer.parseInt(split[0].split(":")[1].trim());
                String arrTime = split[1].split("arrival_time:")[1].trim();
                String depTime = split[2].split("departure_time:")[1].trim();
                
                if (stopId == trip.stopIdDep()) {
                    duration = 0;
                    timePrevDep = LocalTime.parse(depTime, timeFormatter);
                    startCounting = true;
                }
                
                if (startCounting) {
                    LocalTime arrivalTime = LocalTime.parse(arrTime, timeFormatter);
                    LocalTime departureTime = LocalTime.parse(depTime, timeFormatter);
                    
                    if (timePrevDep != null) {
                        long durationBetweenStops = ChronoUnit.SECONDS.between(timePrevDep, arrivalTime);
                        duration += durationBetweenStops;
                    }
                    
                    long waitTime = ChronoUnit.SECONDS.between(arrivalTime, departureTime);
                    duration += waitTime;
                    
                    timePrevDep = departureTime;
                }
                
                if (stopId == trip.stopIdDes()) {
                    break;
                }
            }
            
            if (duration != 9999999){
                processedTrips.add(new Trip(trip.tripId(), trip.stopIdDep(), trip.stopIdDes(), duration));
            }
        }

        Collections.sort(processedTrips, new Comparator<Trip>() {
            @Override
            public int compare(Trip trip1, Trip trip2) {
                return Long.compare(trip1.duration(), trip2.duration());
            }
        });
    
    
        return processedTrips;
    }
    

    public ArrayList<Trip> getTripId(List<BusStop> busStopDep, List<BusStop> busStopDes,  DatabaseController databaseController) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        ArrayList<Trip> trips = new ArrayList<>();
        int stopIdDep = -1;
        int stopIdDes = -1;
        int depSize = busStopDep.size();
        int desSize = busStopDes.size();
        //Gets all overlapping trips between the bus stop lists
        for (int x = 0; x < depSize; x++) {
            stopIdDep = Integer.parseInt(busStopDep.get(x).getStopId().split(":")[1].split(" ")[1]);
            for (int y = 0; y < desSize; y++) {
                stopIdDes = Integer.parseInt(busStopDes.get(y).getStopId().split(":")[1].split(" ")[1]);
                String query = ( 
                    "SELECT DISTINCT s1.trip_id, s1.stop_id, s1.arrival_time, s1.departure_time " +
                    "FROM stop_times s1 " +
                    "JOIN stop_times s2 ON s1.trip_id = s2.trip_id " +
                    "WHERE s1.stop_id = " + stopIdDep +" AND s2.stop_id = " + stopIdDes
                );
                try {
                list = databaseController.executeFetchQuery(query);
                }
                catch (Exception e) {
                    System.out.println(query);
                }
                /* cast trip_id and stop_id to int and not to varchar, so that indexes actually make sense
                * Sort on trip_ids, then find the sortest one. Create function to calculate the length of it. Etc..
                */
                if (list.isEmpty()) continue;

                for (String string: list) {
                    String[] split = string.split(";");
                    int tripId = (Integer.parseInt(split[0].split(":")[1].split(" ")[1]));
                    // int stopId = (Integer.parseInt(split[1].split(":")[1].split(" ")[1]));
                    // String arrTime = split[2].split("arrival_time:")[1].split(" ")[1];
                    // String depTime = split[3].split("departure_time:")[1].split(" ")[1];
                    Trip trip = new Trip(tripId, stopIdDep, stopIdDes, -1);
                    trips.add(trip);
                }
            }
        }
        return trips;
    }
}
