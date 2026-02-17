package com.nesto.automation.utils;

import java.sql.*;

public class DatabaseUtil {
    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/nesto_db";
    private static final String USER = "postgres";
    private static final String PASS = "fathima619";

    /**
     * Used for SELECT queries (Fetching data for Verification)
     */
    public static String getSingleValue(String query) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                Object val = rs.getObject(1);
                return val != null ? val.toString() : "NULL";
            }
        } catch (SQLException e) {
            System.err.println("❌ Database Query Error: " + e.getMessage());
        }
        return "DB_ERROR";
    }

    /**
     * Used for DELETE, INSERT, or UPDATE queries (Data Cleanup)
     */
    public static void executeUpdate(String query) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(query);
            System.out.println("✅ DB Update Success: " + rowsAffected + " rows affected.");

        } catch (SQLException e) {
            System.err.println("❌ Database Execution Error: " + e.getMessage());
            throw new RuntimeException("❌ DB Execution Failed: " + e.getMessage());
        }
    }
}