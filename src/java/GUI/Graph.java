package src.java.GUI;

import java.io.Serializable;
import java.util.*;

/**
 * The Graph class represents a graph data structure with vertices (places) and edges (connections between places).
 */
public class Graph implements Serializable{
    private Map<Place, Set<Edge>> adjList = new HashMap<>();

    /**
     * Adds a vertex to the graph.
     *
     * @param vertex The vertex (place) to add.
     */
    public void addVertex(Place vertex) {
        adjList.putIfAbsent(vertex, new HashSet<>());
    }

    /**
     * Adds an edge between two places with a trip and trip head sign.
     *
     * @param from The starting place.
     * @param to The destination place.
     * @param trip The trip associated with the edge.
     * @param tripHeadsign The head sign of the trip.
     */
    public void addEdge(Place from, Place to, Trip trip, String tripHeadsign) {
        Edge newEdge = new Edge(from, to, tripHeadsign);

        // Check if an equivalent edge already exists and add the trip to it
        for (Edge e : adjList.get(from)) {
            if (e.equals(newEdge)) {
                e.addTrip(trip);
                return;
            }
        }

        // Add a new edge if no equivalent edge exists
        newEdge.addTrip(trip);
        adjList.get(from).add(newEdge);
    }

    /**
     * Adds an edge to the graph.
     *
     * @param edge The edge to add.
     */
    public void addEdge(Edge edge) {
        Place from = edge.getFrom();
        adjList.get(from).add(edge);
    }

    /**
     * Gets all edges connected to a given vertex (place).
     *
     * @param vertex The vertex to get edges for.
     * @return A set of edges connected to the vertex.
     */
    public Set<Edge> getEdges(Place vertex) {
        return adjList.get(vertex);
    }

    /**
     * Gets all vertices (places) in the graph.
     *
     * @return A set of all vertices in the graph.
     */
    public Set<Place> getVertices() {
        return adjList.keySet();
    }

    /**
     * Finds the N nearest bus stops to given coordinates.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param stops The number of nearest bus stops to find.
     * @return A list of the N nearest bus stops.
     */
    public List<BusStop> findNearestBusStops(double lat, double lon, int stops) {
        PriorityQueue<BusStop> nearestStops = new PriorityQueue<>(Comparator.comparingDouble(busStop -> -busStop.distanceTo(lat, lon)));

        for (Place place : adjList.keySet()) {
            if (place instanceof BusStop) {
                nearestStops.offer((BusStop) place);
                if (nearestStops.size() > stops) {
                    nearestStops.poll(); // Remove the farthest bus stop if we have more than N
                }
            }
        }

        // Convert the priority queue to a list and return
        List<BusStop> result = new ArrayList<>();
        while (!nearestStops.isEmpty()) {
            result.add(nearestStops.poll());
        }
        Collections.reverse(result);
        return result;
    }

}
