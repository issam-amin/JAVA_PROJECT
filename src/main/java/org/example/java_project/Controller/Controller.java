package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.example.java_project.Service.AllJobs;
import org.example.java_project.Service.ComplaintService;
import org.example.java_project.Service.DbConnection;
import org.example.java_project.Service.hadoopConf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.example.java_project.Service.ComplaintService.getComplainte;

public class Controller {
    static FileSystem fs ;
    public HBox itemC1;
    public HBox itemC;

    @FXML
    private Button activeButton;

    @FXML
    private Label CompletedIssues;
    @FXML
    private Label PendingIssues;
    @FXML
    private Label IssueTotal;
    @FXML
    private VBox pnItems;
    @FXML
    private Label ClientTotal;
    private final StringProperty ClientValue = new SimpleStringProperty("None");
    private final StringProperty IssueValue = new SimpleStringProperty("None");
    private final StringProperty CompletedIssuesValue = new SimpleStringProperty("None");
    private final StringProperty PendingIssuesValue = new SimpleStringProperty("None");
    @FXML
    private void initialize() {
        getAllIssues();
        IssueTotal.textProperty().bind(IssueValue);
        getAllClients();
        ClientTotal.textProperty().bind(ClientValue);
        getStatus("completed");
        CompletedIssues.textProperty().bind(CompletedIssuesValue);
        getStatus("pending");
        PendingIssues.textProperty().bind(PendingIssuesValue);
        listComplaints();
    }
    public void getAllIssues() {
        try {

            fs = hadoopConf.getFileSystem();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentDate = LocalDate.now().format(formatter);
            String outputFilePath = "/test/output_" + currentDate + "_chart1/part-r-00000";

            Path path = new Path(outputFilePath);

            int sum = 0;

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
                //System.out.println(sum);
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

    private void getAllClients() {
        Connection connection = DbConnection.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM client";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                ClientValue.set(resultSet.getString("COUNT(*)"));
            }
        } catch (SQLException ignored) {
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

            int sum = 0;

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
            if (Status.equals("completed"))
                CompletedIssuesValue.set(String.valueOf(sum));
            else
                PendingIssuesValue.set(String.valueOf(sum));
            System.out.println(sum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*public List<HashMap<String, String>> getComplaints() {
        List<HashMap<String, String>> complaintsList = new ArrayList<>();
        try {
            fs = hadoopConf.getFileSystem();
            String inputFile = "/test/input/data.csv";
            Path path = new Path(inputFile);

            if (fs.exists(path)) {
                FSDataInputStream inputStream = fs.open(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");

                    if (parts.length >= 5) { // Ensure the line has at least 5 fields
                        String field1 = parts[1]; // "7"
                        String field2 = parts[2]; // "20"
                        String field3 = parts[3]; // "Sample reclamation text for..."
                        String field4 = parts[4].split(" ")[0]; // Extract only the date from "2024-09-20 17:18:48"

                        String type = DbConnection.getType(field1); // Lookup type
                        String client = DbConnection.getClient(field2); // Lookup client

                        // Create a dictionary for the current complaint
                        HashMap<String, String> complaint = new HashMap<>();
                        complaint.put("Type", type);
                        complaint.put("Client", client);
                        complaint.put("Text", field3);
                        complaint.put("Date", field4);

                        // Add to the list
                        complaintsList.add(complaint);
                    }
                }

                reader.close();
                inputStream.close();
            } else {
                System.out.println("Input file does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return complaintsList; // Return the list of complaints
    }*/

    void listComplaints() {
        pnItems.getChildren().clear();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ResultSet complaints = getComplainte();
                    while (complaints.next()) {
                        Node node = FXMLLoader.load(getClass().getResource("../MyItems.fxml"));

                        node.setOnMouseEntered(event -> node.setStyle("-fx-background-color: #0A0E3F"));
                        node.setOnMouseExited(event -> node.setStyle("-fx-background-color: #02030A"));

                        // Extract data from the ResultSet
                        String client = complaints.getString("Client");
                        String recText = complaints.getString("Rec_Text");
                        String dateReclamation = complaints.getString("date_Reclamation");

                        // Update the UI on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            try {
                                Label label = (Label) ((Parent) node).lookup("#Client");
                                if (label != null) {
                                    label.setText(client);
                                }

                                label = (Label) ((Parent) node).lookup("#Rec_Text");
                                if (label != null) {
                                    label.setText(recText);
                                }

                                label = (Label) ((Parent) node).lookup("#date_Reclamation");
                                if (label != null) {
                                    label.setText(dateReclamation);
                                }

                                Button activeButton = (Button) ((Parent) node).lookup("#activeButton");
                                if (activeButton != null) {
                                    activeButton.setOnAction(event -> openPopupWindow(client, recText, dateReclamation));
                                }

                                // Add the node to the panel
                                pnItems.getChildren().add(node);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        // Run the task in a new thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // Ensure the thread stops when the application exits
        thread.start();
    }


    private void openPopupWindow(String client, String recText, String dateReclamation) {
        try {
            // Load the FXML for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Popup.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the data
            PopupController controller = loader.getController();
            controller.setMessage(client, recText, dateReclamation);

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            popupStage.setTitle("Complaint Details");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


