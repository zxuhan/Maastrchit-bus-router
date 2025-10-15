package src.java.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LogController {

    private static String path = "src/java/Database/log.txt";

    public LogController() {
        createLogFile();
    }
    public static void createLogFile() {
        try {
            File logFile = new File(path); // Specify the file name

            // Check if the file exists
            if (!logFile.exists()) {
                // Attempt to create the file
                if (logFile.createNewFile()) {
                    System.out.println("Log file created successfully: " + logFile.getAbsolutePath());
                    addToLog("Log file created at: " + getCurrentDateTime());
                } else {
                    throw new RuntimeException("Unable to create log file: " + logFile.getAbsolutePath());
                }
            } else {
                logFile.delete();
                logFile.createNewFile();
                System.out.println("Log file created successfully: " + logFile.getAbsolutePath());
                addToLog("Log file created at: " + getCurrentDateTime());
            }
        } catch (IOException e) {
            // Print any error messages to standard output
            throw new RuntimeException("An error occurred while creating the log file: " + e.getMessage());
        }
    }

    public static void addToLog(String log) {
        // Use try-with-resources to ensure the FileWriter and BufferedWriter are closed after use
        try (FileWriter fw = new FileWriter(path, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(log);  // Write the text to the file
            bw.newLine();    // Add a new line
            bw.flush();      // Ensure data is written to the file
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the log file: " + e.getMessage());
        }
    }

    public static void logQuery(String query) {
        addToLog("Query: " + query + " at: " + getCurrentDateTime());
    }

    public static void logResult() {
        addToLog("Results received at: " + getCurrentDateTime());
    }

    public static void logConnection(Boolean connecting) {
        if (connecting) {
            addToLog("Connected at: " + getCurrentDateTime());
        } else {
            addToLog("Disconnected at: " + getCurrentDateTime());
        }
    }

    public static void logCredentials(Boolean encrypt) {
        if (encrypt) {
            addToLog("Credentials encrypted at: " + getCurrentDateTime());
        } else {
            addToLog("Credentials decrypted at: " + getCurrentDateTime());
        }
    }

    public static void logError(String error){
        addToLog("Error: " + error + " at: " + getCurrentDateTime());
    }


    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter); // Formats the date and time up to minutes
    }

}
