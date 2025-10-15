package src.java.GUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import src.java.Singletons.ExceptionManager;

public class UserInput {

    private static final String ZIP_CODE_PATTERN = "\\d{4}\\s[A-Z]{2}";
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        try {
            System.out.println("Enter zip codes in the format '1234 AA'.");
            String zipCode1 = getUserInput("First zip code: ");
            validateZipCode(zipCode1);
            String zipCode2 = getUserInput("Second zip code: ");
            validateZipCode(zipCode2);

            String transportChoice = getUserInput("Enter your mode of transportation (e.g., Walk, Bike, Car): ");
            System.out.printf("Zip Code 1: %s\nZip Code 2: %s\nTransportation Mode: %s\n", zipCode1, zipCode2, transportChoice);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String getUserInput(String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine().trim();
    }

    private static void validateZipCode(String zipCode) throws IllegalArgumentException {
        if (!zipCode.matches(ZIP_CODE_PATTERN)) {
            // ExceptionManager.showError("zipCode", "Problem", "Invalid zip code format");
            throw new IllegalArgumentException("Invalid zip code format.");
        }
    }
}
