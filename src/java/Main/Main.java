package src.java.Main;
//Main Will Be Instancing of GUI

import static src.java.Main.CalculateDistance.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            //Not an elegant solutions, fix later
            launchGraphHopper().waitFor(1, TimeUnit.MINUTES);
            Thread.sleep(8000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(printDistance("6217HG", "6267EC", false));
        // System.out.println(printDistance("6229EN", "6211LC", false));
        System.out.println(printDistance("6217HG", "6211LC", true));


    }
}