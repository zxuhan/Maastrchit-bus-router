package src.java.Main;

import java.io.IOException;

import src.java.Transport.TransportMode;

public class TimeCalculator {
    
    private static int MinutesInHours = 60;

    public static long calculateAverageTimeTaken(String zipCode1, String zipCode2, TransportMode mode, boolean graphHopperEnabled) throws IOException {
        double distance = CalculateDistance.getDistance(zipCode1, zipCode2, graphHopperEnabled);
        double averageVelocity = mode.getVelocity();
        
        if (averageVelocity == 0) {
            throw new IllegalArgumentException("Average velocity cannot be zero.");
        }

        double averageTime = (distance / averageVelocity) * MinutesInHours;

        return Math.round(averageTime);
    }


    // ------------------TEST
    
    // /*  Main test code*/
    // public static void main(String[] args) {
    //      TimeCalculator calculator = new TimeCalculator();
        
    //      TransportMode mode = new Walk();
    //      // Change this to new Walk() or new Bike() to calculate for different modes
    //      String zipCode1 = "62";
    //      String zipCode2 = "6229EN";
    //      try {
    //          double averageTimeTaken = calculator.calculateAverageTimeTaken(zipCode1, zipCode2, mode);
            
    //          System.out.println("Average Time Taken: " + averageTimeTaken + " minutes");
    //      } catch (IOException e) {
    //          System.err.println("An error occurred while calculating the distance: " + e.getMessage());
    //          e.printStackTrace();
    //      }

     /**/
    }


