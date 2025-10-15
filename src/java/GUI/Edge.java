package src.java.GUI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Edge class represents an edge in the graph, connecting two places.
 * It can represent a walking path or a bus route between two places.
 */
public class Edge implements Serializable {
    private Place from; // The starting place of the edge
    private Place to; // The destination place of the edge
    private String tripHeadSign; // The head sign of the trip for this edge
    private List<Trip> trips; // The list of trips associated with this edge
    private long walkingTime; // The walking time in minutes
    private int walkingDist; // The walking distance in meters

    /**
     * Constructor to initialize an Edge object with a trip head sign.
     *
     * @param from The starting place.
     * @param to The destination place.
     * @param tripHeadSign The head sign of the trip.
     */
    public Edge(Place from, Place to, String tripHeadSign) {
        this.from = from;
        this.to = to;
        this.tripHeadSign = tripHeadSign;
        this.trips = new ArrayList<>();
    }

    /**
     * Constructor to initialize an Edge object with walking time and distance.
     *
     * @param from The starting place.
     * @param to The destination place.
     * @param walkingTime The walking time in minutes.
     * @param walkingDist The walking distance in meters.
     * @param tripHeadSign The head sign of the trip.
     */
    public Edge(Place from, Place to, long walkingTime, int walkingDist, String tripHeadSign) {
        this.from = from;
        this.to = to;
        this.tripHeadSign = tripHeadSign;
        this.walkingTime = walkingTime;
        this.walkingDist = walkingDist;
        this.trips = new ArrayList<>();
    }

    public void addTrip(Trip trip) {
        trips.add(trip);
    }

    public List<Trip> getTrips() {
        return trips;
    }
    public Place getFrom() {
        return from;
    }

    public Place getTo() {
        return to;
    }

    public String getTripHeadSign() {
        return tripHeadSign;
    }

    public long getWalkingTime() {return walkingTime;}

    public int getWalkingDist() {return walkingDist;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return from.equals(edge.from) &&
                to.equals(edge.to) &&
                tripHeadSign.equals(edge.tripHeadSign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, tripHeadSign, trips.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edge{")
                .append("from=").append(from)
                .append(", to=").append(to)
                .append(", tripHeadSign='").append(tripHeadSign).append('\'')
                .append(", trips=[");
        for (Trip trip : trips) {
            sb.append("\n    ").append(trip.toString());
        }
        sb.append("\n  ]}");
        return sb.toString();
    }
}
