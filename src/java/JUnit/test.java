package src.java.JUnit;

import org.junit.Test;

import src.java.GUI.AStar;
import src.java.GUI.Graph;
import src.java.GUI.GraphBuilder;
import src.java.GUI.Place;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;


//Caching of astar graph needs to be implemented to improve runtime
public class test {
    @Test
    public void test() throws Exception {
        assertShortestPath(50.86369838159479, 5.6714126544592185, 50.854054496231, 5.6842872583488715, 12);
        assertShortestPath(50.84156897226049, 5.668216667130091, 50.848235002298914, 5.711818657797253, 25);
        assertShortestPath(50.85361432181733, 5.6652170625378915, 50.86981895328774, 5.7150183433174435, 38);
    }

    //Real travel time is now irrelevant because astar algorithm does not use actual bus travel time
    private void assertShortestPath(double latitude, double longitude, double latitude2, double longitude2, int realTravelTime) throws Exception {
        Place endPlace = new Place(latitude, longitude);
        Place startPlace = new Place(latitude2, longitude2);
        int travelTime = getShortestTravelTime(startPlace, endPlace);
        boolean asserter = (realTravelTime + 5 >= travelTime && realTravelTime - 5 <= travelTime);
        assertEquals(asserter, true);
    }

    public static int getShortestTravelTime(Place departure, Place destination) throws Exception {
        Graph graph = new Graph();
        GraphBuilder graphBuilder = new GraphBuilder(graph);
        try {
            graphBuilder.getBusStops();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        AStar aStar = new AStar(graph);
        List<Place> path = aStar.findShortestPath(departure, destination);
        List<String> directions = aStar.getDirections();
        return Integer.parseInt(directions.get(directions.size() -1).split(":")[1].split(" ")[1]);
    }
}
