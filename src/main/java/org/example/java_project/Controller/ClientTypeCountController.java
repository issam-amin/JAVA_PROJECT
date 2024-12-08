package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import org.example.java_project.Service.clientTypeJob;
import org.example.java_project.Service.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.java_project.Service.DbConnection.getClient;
import static org.example.java_project.Service.DbConnection.getType;

public class ClientTypeCountController {

    @FXML
    private TableView<DataRecord> tableView;
    @FXML
    private TableColumn<DataRecord, String> customerColumn;
    @FXML
    private TableColumn<DataRecord, String> issueColumn;
    @FXML
    private TableColumn<DataRecord, Integer> scoreColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button refreshButton;

    private ObservableList<DataRecord> data;
    private FilteredList<DataRecord> filteredData;

    @FXML
    private void initialize() {
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        issueColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIssueType()));
        scoreColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getScore()).asObject());

        runJob("ClientTypeCount", JobType.NORMAL);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData(newValue));
    }

    @FXML
    private void refreshData(ActionEvent actionEvent) {
        runJob("ClientTypeCount", JobType.REFRESH);
    }

    private void runJob(String jobName, JobType jobType) {
        clientTypeJob job = new clientTypeJob(jobName, jobType);

        job.setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
            Map<String, Integer> jobResults = (Map<String, Integer>) job.getValue();
            List<DataRecord> processedData = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : jobResults.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();

                String[] parts = key.split("_");
                if (parts.length == 2) {
                    String clientId = parts[0];
                    String reclamationId = parts[1];

                    String clientName = getClient(clientId);
                    String reclamationType = getType(reclamationId);

                    processedData.add(new DataRecord(clientName, reclamationType, value));
                } else {
                    System.err.println("Invalid key format: " + key);
                }
            }

            data = FXCollections.observableArrayList(processedData);
            filteredData = new FilteredList<>(data, p -> true);
            tableView.setItems(filteredData);
        }));

        job.setOnFailed(workerStateEvent -> Platform.runLater(() -> {
            Throwable exception = job.getException();
            if (exception != null) {
                exception.printStackTrace();
            }
        }));

        Thread jobThread = new Thread(job);
        jobThread.setDaemon(true);
        jobThread.start();
    }

    private void filterData(String filter) {
        if (filter == null || filter.isEmpty()) {
            filteredData.setPredicate(record -> true);
        } else {
            String lowerCaseFilter = filter.toLowerCase();
            filteredData.setPredicate(record ->
                    record.getCustomerName().toLowerCase().contains(lowerCaseFilter) ||
                            record.getIssueType().toLowerCase().contains(lowerCaseFilter)
            );
        }
    }

    public static class DataRecord {
        private final String customerName;
        private final String issueType;
        private final int score;

        public DataRecord(String customerName, String issueType, int score) {
            this.customerName = customerName;
            this.issueType = issueType;
            this.score = score;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getIssueType() {
            return issueType;
        }

        public int getScore() {
            return score;
        }
    }
}
