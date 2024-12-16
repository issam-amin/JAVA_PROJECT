package org.example.java_project.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DbConnection {
    static final Connection connection;
    static {
        try {
            connection =  DriverManager.getConnection("jdbc:mysql://localhost:3306/claims", "root", "PHW#84#jeor");
        }catch  (SQLException e) {
            throw new RuntimeException(e);
        }
    }
/*    public static Connection getConnection() {
        return connection;
    }*/
public static Connection getConnection() {
    /*Connection conn = null;
    try {
        if (conn == null || conn.isClosed()) {
            // Establish a new connection if the old one is closed
           conn  =  DriverManager.getConnection("jdbc:mysql://localhost:3306/claims", "root", "PHW#84#jeor");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }*/
    return connection;
}



    public static boolean export(String table){
        String csvFilePath = "src/main/resources/data.csv";
        System.out.println("CSV file path: " + new File(csvFilePath).getAbsolutePath());

        // System.out.println(csvFilePath);
        try {
            Connection connection = getConnection();
            String sql = "SELECT * FROM "+ table; // Query to extract table data
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            // Write CSV file
            FileWriter csvWriter = new FileWriter(csvFilePath);
            // Get column names and write as header row in CSV
            int columnCount = resultSet.getMetaData().getColumnCount();
            // Write data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    csvWriter.append(resultSet.getString(i));
                    if (i < columnCount) csvWriter.append(","); // Add comma if not the last column
                }
                csvWriter.append("\n"); // New line after each row
            }
            csvWriter.flush();
            csvWriter.close();
            System.out.println("CSV file created successfully: " + csvFilePath);
            return  true;

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            return false;
        }catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean export2(String table,String csvname){
        String csvFilePath = "src/main/resources/"+csvname;
        System.out.println("CSV file path: " + new File(csvFilePath).getAbsolutePath());

        // System.out.println(csvFilePath);
        try {
            Connection connection = getConnection();
            String sql = "SELECT id ,Gender, SeniorCitizen, Partner, Dependents,tenure, " +
                    "PhoneService, MultipleLines, InternetService, OnlineSecurity, OnlineBackup, DeviceProtection, " +
                    "TechSupport, StreamingTV, StreamingMovies, Contract, PaperlessBilling, PaymentMethod, " +
                    "MonthlyCharges, TotalCharges " +
                    "FROM " + table + ";";


            // Query to extract table data
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            // Write CSV file
            FileWriter csvWriter = new FileWriter(csvFilePath);
            // Get column names and write as header row in CSV
            int columnCount = resultSet.getMetaData().getColumnCount();
            // Write data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    csvWriter.append(resultSet.getString(i));
                    if (i < columnCount) csvWriter.append(","); // Add comma if not the last column
                }
                csvWriter.append("\n"); // New line after each row
            }
            csvWriter.flush();
            csvWriter.close();
            System.out.println("CSV file created successfully: " + csvFilePath);
            return  true;

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            return false;
        }catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public  static String getType(String id ){

        Connection connection = getConnection();
        HashMap<String, String> typerec = new HashMap<>();

            String sql = "SELECT * FROM typerec WHERE  id = "+id ; // Query to extract table data
            Statement statement = null;
            try {
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                   return resultSet.getString("NomType");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
    }
    public static String getClient(String id ){
        Connection connection = getConnection();
        HashMap<String, String> client = new HashMap<>();

        String sql = "SELECT * FROM client WHERE  id = "+id ; // Query to extract table data
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                    String nom = resultSet.getString("Nom");
                    String prenom = resultSet.getString("Prenom");
                    // Return concatenated result
                    return nom + " " + prenom;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    public static Map<String, String> getAllClientNames() {
        Map<String, String> clientNames = new HashMap<>();
        String sql = "SELECT id, CONCAT(firstName, ' ', lastName) AS name FROM customer";

        try (var connection = DbConnection.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                clientNames.put(resultSet.getString("id"), resultSet.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clientNames;
    }
    public static boolean validateLogin(String username, String password) {
        Connection connection = getConnection();
        String query = "SELECT * FROM admin_user WHERE username = ? AND password = ?";
        try (
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public  static void close() {
        Connection con = getConnection();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
