package src.java.GUI;

import java.sql.*;
public class DatabaseConnection {
      static String URL = "";
      static String USER = "";
      static String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Establish connection and return it
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        String query = "SELECT* FROM shapes LIMIT 10";
        try (Statement stmt = getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            boolean hasData = false;

            // Process the result set
            while (rs.next()) {
                hasData = true;
                int shapeId = rs.getInt("shape_id");
                int shapePtSequence = rs.getInt("shape_pt_sequence");
                double shapePtLat = rs.getDouble("shape_pt_lat");
                double shapePtLon = rs.getDouble("shape_pt_lon");
                double shapeDistTraveled = rs.getDouble("shape_dist_traveled");
                System.out.println("Shape ID: " + shapeId +
                        ", Shape Point Sequence: " + shapePtSequence +
                        ", Latitude: " + shapePtLat +
                        ", Longitude: " + shapePtLon +
                        ", Distance Traveled: " + shapeDistTraveled);
            }
            if (!hasData) {
                System.out.println("No data found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
