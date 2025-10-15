package src.java.GUI;

/**
 * The BusRouteShape class represents a point on a bus route shape.
 */
public class BusRouteShape {
    protected int shapeId; // The ID of the shape
    protected int shapePtSequence; // The sequence of the point in the shape
    protected double shapePtLat; // The latitude of the shape point
    protected double shapePtLon; // The longitude of the shape point
    protected double shapeDistTraveled; // The distance traveled along the shape to this point

    /**
     * Constructor to initialize a BusRouteShape object.
     *
     * @param shapeId The ID of the shape.
     * @param shapePtSequence The sequence of the point in the shape.
     * @param shapePtLat The latitude of the shape point.
     * @param shapePtLon The longitude of the shape point.
     * @param shapeDistTraveled The distance traveled along the shape to this point.
     */
    public BusRouteShape(int shapeId, int shapePtSequence, double shapePtLat, double shapePtLon, double shapeDistTraveled) {
        this.shapeId = shapeId;
        this.shapePtSequence = shapePtSequence;
        this.shapePtLat = shapePtLat;
        this.shapePtLon = shapePtLon;
        this.shapeDistTraveled = shapeDistTraveled;
    }
}
