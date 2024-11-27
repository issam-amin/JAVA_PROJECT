package org.example.java_project.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.java_project.Service.DbConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataInputStream;
import org.example.java_project.Service.hadoopConf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class clientStatsController {

    @FXML
    private Label IssueTotal;
    @FXML
    private Label ClientTotal;
    static FileSystem fs ;
    private final StringProperty ClientValue = new SimpleStringProperty("Makhrajch wallo");
    private final StringProperty IssueValue = new SimpleStringProperty("Makhrajch wallo");



    @FXML
    private void initialize() {
        getAllClients();
        ClientTotal.textProperty().bind(ClientValue);
        getAllIssues();
        IssueTotal.textProperty().bind(IssueValue);
    }
    private void getAllClients() {
        Connection connection = DbConnection.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM client"; // Query to extract table data
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                ClientValue.set(resultSet.getString("COUNT(*)"));
            }
        } catch (SQLException ignored) {
        }

    }
    private void getAllIssues() {
        Connection connection = DbConnection.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM rec"; // Query to extract table data
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                IssueValue.set(resultSet.getString("COUNT(*)"));
            }
        } catch (SQLException ignored) {
        }
    }



    public void getAllIssuesFromHDFS() {
        try {

            fs = hadoopConf.getFileSystem();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentDate = LocalDate.now().format(formatter);
            String outputFilePath = "/test/output_" + currentDate + "_chart1/part-r-00000";
            Path path = new Path(outputFilePath);

            double sum = 0;

            if (fs.exists(path)) {
                FSDataInputStream inputStream = fs.open(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t");
                    if (parts.length == 2) {
                        try {
                            sum += Double.parseDouble(parts[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number: " + parts[1]);
                        }
                    }
                }
                System.out.println(sum);
                reader.close();
                inputStream.close();
            } else {
                System.out.println("Output file for the current day does not exist.");
            }

            fs.close();
            IssueValue.set(String.valueOf(sum));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getStatus(String Status) {
        try {
            fs = hadoopConf.getFileSystem();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentDate = LocalDate.now().format(formatter);
            String outputFilePath = "/test/output_" + currentDate + "_chart2/part-r-00000";
            System.out.println(outputFilePath);
            Path path = new Path(outputFilePath);

            double sum = 0;

            if (fs.exists(path)) {
                FSDataInputStream inputStream = fs.open(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    String[] parts = line.split("\t");
                //    System.out.println( parts[0]);
                    if (parts.length == 2 && parts[0].equals(Status)) {
                        try {
                            sum += Double.parseDouble(parts[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number: " + parts[1]);
                        }
                    }
                }

                reader.close();
                inputStream.close();
            } else {
                System.out.println("Output file for the current day does not exist.");
            }

            fs.close();
            IssueValue.set(String.valueOf(sum));
            System.out.println(sum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getREC() {
        Connection connection = DbConnection.getConnection();
        try {

            String sql = "SELECT CONCAT(client.Nom, ' ', client.Prenom) AS Client, client.CNE, rec.Rec_Text, rec.date_Reclamation, typerec.NomType " +
                    "FROM client " +
                    "JOIN rec ON client.id = rec.id_C " +
                    "JOIN typerec ON typerec.id = rec.id_T " +
                    "ORDER BY rec.date_Reclamation DESC " +
                    "LIMIT 20";

            System.out.println("Executing SQL: " + sql);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println(resultSet);
            while (resultSet.next()) {
                String client = resultSet.getString("Client");
                String cne = resultSet.getString("CNE");
                String recText = resultSet.getString("Rec_Text");
                String dateRec = resultSet.getString("date_Reclamation");
                String type = resultSet.getString("NomType");

                // Print each row
                System.out.printf("%-20s %-15s %-30s %-20s %-20s%n", client, cne, recText, dateRec, type);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        }
    }






