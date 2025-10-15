package src.java.GUI;

import java.time.LocalTime;
import java.util.Objects;

/**
 * The SearchNode class represents a node in the search graph used by the A* algorithm.
 */
public class SearchNode {
    Place place; // The place associated with this search node
    long g; // Cost from the start node to this node
    long h; // Heuristic cost from this node to the goal
    long f; // Total cost (g + h)
    LocalTime time; // The time associated with reaching this node
    int dist; // The distance traveled to reach this node
    SearchNode cameFrom; // The previous node in the path
    Trip trip; // The trip associated with this node, if any

    /**
     * Constructor to initialize a SearchNode object.
     *
     * @param place The place associated with this search node.
     * @param g The cost from the start node to this node.
     * @param h The heuristic cost from this node to the goal.
     * @param time The time associated with reaching this node.
     * @param dist The distance traveled to reach this node.
     * @param cameFrom The previous node in the path.
     * @param trip The trip associated with this node, if any.
     */
    SearchNode(Place place, long g, long h, LocalTime time, int dist, SearchNode cameFrom, Trip trip) {
        this.place = place;
        this.g = g;
        this.h = h;
        this.f = g + h;
        this.cameFrom = cameFrom;
        this.trip = trip;
        this.dist = dist;
        this.time = time;
    }

    public long getG() {return g;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchNode searchNode = (SearchNode) o;
        return place.equals(searchNode.place);
    }

    @Override
    public int hashCode() {
        return Objects.hash(place.hashCode(), g, h);
    }
}
