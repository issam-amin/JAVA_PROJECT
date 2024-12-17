package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import java.util.HashSet;
import java.util.Set;

import static org.example.java_project.Service.ComplaintService.getComplainte;

public class Controller {
    static FileSystem fs;
    public HBox itemC1;
    public HBox itemC;
    public TextField search_bar;
    public TextField limitField;

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
    private final Set<String> addedComplaints = new HashSet<>();
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
        listComplaints(0);

        search_bar.textProperty().addListener((observable, oldValue, newValue) -> listComplaints(0));
    }

    public void getAllIssues() {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                int sum = 0;
                try {
                    fs = hadoopConf.getFileSystem();  // Ensure fs is opened once here
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String currentDate = LocalDate.now().format(formatter);
                    String outputFilePath = "/test/output_" + currentDate + "_chart1/part-r-00000";
                    Path path = new Path(outputFilePath);

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
                        reader.close();
                        inputStream.close();
                    } else {
                        System.out.println("Output file for the current day does not exist.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return sum;
            }
        };

        task.setOnSucceeded(event -> IssueValue.set(task.getValue().toString()));
        new Thread(task).start();
    }

    private void getAllClients() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String clientCount = "0";
                Connection connection = DbConnection.getConnection();
                try {
                    String sql = "SELECT COUNT(*) FROM client";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        clientCount = resultSet.getString("COUNT(*)");
                    }
                } catch (SQLException ignored) {
                }
                return clientCount;
            }
        };

        task.setOnSucceeded(event -> ClientValue.set(task.getValue()));
        new Thread(task).start();
    }

    public void getStatus(String status) {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                int sum = 0;
                try {
                    fs = hadoopConf.getFileSystem(); // Ensure fs is opened once here
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String currentDate = LocalDate.now().format(formatter);
                    String outputFilePath = "/test/output_" + currentDate + "_chart2/part-r-00000";
                    Path path = new Path(outputFilePath);

                    if (fs.exists(path)) {
                        FSDataInputStream inputStream = fs.open(path);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split("\t");
                            if (parts.length == 2 && parts[0].equals(status)) {
                                try {
                                    sum += Double.parseDouble(parts[1]);
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid number: " + parts[1]);
                                }
                            }
                        }
                        reader.close();
                    } else {
                        System.out.println("Output file for the current day does not exist.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return sum;
            }
        };

        task.setOnSucceeded(event -> {
            if (status.equals("completed")) {
                CompletedIssuesValue.set(task.getValue().toString());
            } else {
                PendingIssuesValue.set(task.getValue().toString());
            }
        });
        new Thread(task).start();
    }


    // Track added complaints across multiple calls to listComplaints


    void listComplaints(int limit) {
        pnItems.getChildren().clear();  // Clear the displayed items.
        addedComplaints.clear();        // Reset the duplicate tracking set.

        if (limit <= 0) {
            limit = 20; // Default limit if no value is specified
        }

        int finalLimit = limit;  // Capture limit for use inside the task

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    ResultSet complaints = getComplainte();
                    String searchTerm = search_bar.getText().toLowerCase();
                    int count = 0;  // Counter to track the number of complaints displayed

                    while (complaints.next() && count < finalLimit) {
                        String client = complaints.getString("Client").toLowerCase();
                        String recText = complaints.getString("Rec_Text").toLowerCase();
                        String dateReclamation = complaints.getString("date_Reclamation").toLowerCase();
                        String status = complaints.getString("status_Rec");
                        String idClient = complaints.getString("id_C");
                        String idReclamation = complaints.getString("id_R"); // Fetch ID

                        String complaintKey = client + recText + dateReclamation;

                        // Filter complaints based on search term
                        if ((client.contains(searchTerm) || recText.contains(searchTerm) || dateReclamation.contains(searchTerm))
                                && addedComplaints.add(complaintKey)) {

                            count++;  // Increment the counter as we add a complaint

                            Node node = FXMLLoader.load(getClass().getResource("../MyItems.fxml"));
                            Platform.runLater(() -> {
                                try {
                                    Label issueLabel = (Label) ((Parent) node).lookup("#issueNumber");
                                    if (issueLabel != null) {
                                        issueLabel.setText(idReclamation); // Display ID
                                    }

                                    Label label = (Label) ((Parent) node).lookup("#Client");
                                    if (label != null) label.setText(client);

                                    label = (Label) ((Parent) node).lookup("#Rec_Text");
                                    if (label != null) label.setText(recText);

                                    label = (Label) ((Parent) node).lookup("#date_Reclamation");
                                    if (label != null) label.setText(dateReclamation);

                                    Button activeButton = (Button) ((Parent) node).lookup("#activeButton");
                                    if (activeButton != null) {
                                        activeButton.setOnAction(event -> openPopupWindow(client, recText, dateReclamation, status, idClient));
                                    }

                                    pnItems.getChildren().add(node);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }






    // Open a popup window with complaint details
    private void openPopupWindow(String client, String recText, String dateReclamation, String status, String idClient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Popup.fxml"));
            Parent root = loader.load();

            PopupController controller = loader.getController();
            controller.setMessage(client, recText, dateReclamation, status, idClient);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Complaint Details");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void applyLimitFilter() {
        String limitText = limitField.getText();
        int limit = -1;
        try {
            if (!limitText.isEmpty()) {
                limit = Integer.parseInt(limitText);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid limit value: " + limitText);
        }
        listComplaints(limit); // Apply the limit filter to the complaint list
    }
}
