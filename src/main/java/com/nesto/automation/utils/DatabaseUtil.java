package com.nesto.automation.utils;

import java.sql.*;

public class DatabaseUtil {
    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/nesto_db";
    private static final String USER = "postgres"; // Replace with your PG username
    private static final String PASS = "fathima619"; // Replace with your PG password

    public static String getSingleValue(String query) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getString(1); // Returns the first column of the first row
            }
        } catch (SQLException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
        }
        return "DB_ERROR";
    }
}
