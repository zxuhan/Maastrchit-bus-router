package src.java.GUI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The BusStop class represents a bus stop with a specific ID, name, latitude, and longitude.
 * It extends the Place class.
 */
public class BusStop extends Place implements Serializable {
    private int stopId;
    private String name;

    /**
     * Constructor to initialize a BusStop object.
     *
     * @param stopId The ID of the bus stop.
     * @param name The name of the bus stop.
     * @param lat The latitude of the bus stop.
     * @param lon The longitude of the bus stop.
     */
    public BusStop(int stopId, String name, double lat, double lon) {
        super(lat, lon);
        this.stopId = stopId;
        this.name = name;
    }

    public int getStopId() {
        return stopId;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return  name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStop busStop = (BusStop) o;
        return Double.compare(busStop.lat, lat) == 0 &&
                Double.compare(busStop.lon, lon)  == 0 &&
                busStop.stopId == stopId &&
                name.equals(busStop.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, name, lat, lon);
    }

}
