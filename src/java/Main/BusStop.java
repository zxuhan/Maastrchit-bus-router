package src.java.Main;

public class BusStop {
    private String stopId;
    private double lat;
    private double lon;
    private double distance;

    public BusStop(String stopId, double lat, double lon, double distance) {
        this.stopId = stopId;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
    }

    public String getStopId() {
        return this.stopId;
    }

    public double getDistance() {
        return this.distance;
    }

    public double getLat() {
        return this.lat;
    }
    public double getLon() {
        return this.lon;
    }
}