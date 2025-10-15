package src.java.GUI;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * The Trip class represents a bus trip with its associated details.
 */
public class Trip implements Comparable<Trip>, Serializable {
    private int tripId;
    private String tripHeadSign;
    private int shapeDistTraveled;

    private LocalTime arriveTime;
    private LocalTime departureTime;

    private int shapeId;

    /**
     * Constructor to initialize a Trip object.
     *
     * @param tripId The ID of the trip.
     * @param arriveTime The arrival time of the trip.
     * @param departureTime The departure time of the trip.
     * @param tripHeadSign The head sign of the trip.
     * @param shapeDistTraveled The distance traveled along the shape.
     * @param shapeId The ID of the shape.
     */
    public Trip(int tripId,  LocalTime arriveTime, LocalTime departureTime, String tripHeadSign, int shapeDistTraveled, int shapeId) {
        this.tripId = tripId;
        this.tripHeadSign = tripHeadSign;
        this.shapeDistTraveled = shapeDistTraveled;
        this.arriveTime = arriveTime;
        this.departureTime = departureTime;
        this.shapeId = shapeId;
    }

    public int getShapeDistTraveled() {return shapeDistTraveled;}

    public LocalTime getArriveTime() {return arriveTime;}

    public LocalTime getDepartureTime() {return departureTime;}

    public int getShapeId() {return shapeId;}

    public String getTripHeadSign() {return tripHeadSign;}

    @Override
    public int compareTo(Trip that) {
        return this.arriveTime.compareTo(that.arriveTime);
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId=" + tripId +
                ", tripHeadSign='" + tripHeadSign + '\'' +
                ", shapeDistTraveled=" + shapeDistTraveled +
                ", arriveTime=" + arriveTime +
                ", departureTime=" + departureTime +
                ", shapeId=" + shapeId +
                '}';
    }
}
