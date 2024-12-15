package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.example.java_project.Service.AllJobs;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.RectypeJob;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RectypeController implements Initializable {
    @FXML
    private ComboBox<String> Archives;
    @FXML
    private HBox panepie;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label state;

    private Random random = new Random();
    private PieChart loadingPieChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadArchives();
        runJob("chart2", JobType.NORMAL);
    }

    private void loadArchives() {
        try {
            List<String> archives = RectypeJob.getArchivesFromHDFS();
            List<String> filteredArchives = archives.stream()
                    .filter(archive -> archive.contains("chart2"))  // Filter for "chart2" only
                    .map(this::extractFormattedDate)  // Map each archive to the formatted date
                    .collect(Collectors.toList());

            if (filteredArchives.isEmpty()) {
                updateState("No archives found for chart2 in HDFS.", "red");
            } else {
                Platform.runLater(() -> Archives.getItems().addAll(filteredArchives));  // Use filteredArchives
                Archives.setOnAction(this::handleArchiveSelection);
            }
        } catch (IOException e) {
            updateState("Failed to load archives from HDFS.", "#ffffff");
        }
    }

    private String extractFormattedDate(String archiveName) {
        String datePart = archiveName.split("_")[1];

        return datePart;
    }


    // Action when refreshing the pie chart
    @FXML
    public void refresh_Piechart(ActionEvent actionEvent) {
        runJob("chart2", JobType.REFRESH);
    }

    private void runJob(String jobName, JobType jobType) {
        showLoadingPieChart();

        RectypeJob job = new RectypeJob(jobName, jobType);
        job.setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
            HashMap<String, Integer> jobResults = job.getValue();
            updatePieChartData(jobResults);
        }));

        job.setOnFailed(workerStateEvent -> Platform.runLater(() -> {
            panepie.getChildren().clear();
            panepie.getChildren().add(new Label("Failed to load data."));
            Throwable error = job.getException();
            error.printStackTrace();
        }));

        Thread jobThread = new Thread(job);
        jobThread.setDaemon(true);
        jobThread.start();
    }
    private void showLoadingPieChart() {
        ObservableList<PieChart.Data> randomData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", random.nextInt(4000) + 1000),
                new PieChart.Data("In Progress Critique", random.nextInt(4000) + 1000),
                new PieChart.Data("Completed", random.nextInt(4000) + 1000)
        );

        loadingPieChart = new PieChart(randomData);
        loadingPieChart.setClockwise(true);
        loadingPieChart.setLabelsVisible(true);
        loadingPieChart.setLegendVisible(false);
        loadingPieChart.setStartAngle(180);
        loadingPieChart.setPrefSize(1000, 1000);

        Scene scene = panepie.getScene();
        if (scene != null) {
            URL cssResource = getClass().getResource("../Style/pieChart.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Error: CSS resource not found.");
            }
        } else {
            System.err.println("Error: Scene is null.");
        }
        panepie.getChildren().clear();
        panepie.getChildren().add(loadingPieChart);
    }


    private void updatePieChartData(HashMap<String, Integer> jobResults) {
        int total = jobResults.values().stream().mapToInt(Integer::intValue).sum();
        loadingPieChart.getData().clear();

        for (String key : jobResults.keySet()) {
            int value = jobResults.getOrDefault(key, 0);
            String percentageLabel = getPercentage(value, total);
            String initialLabel = key + " " + percentageLabel;
            String absoluteLabel = key + ": " + value;

            PieChart.Data data = new PieChart.Data(initialLabel, value);
            loadingPieChart.getData().add(data);

            Platform.runLater(() -> addPieChartEffects(data, absoluteLabel));
        }

        Platform.runLater(() -> {
            panepie.getChildren().clear();
            panepie.getChildren().add(loadingPieChart);
        });
    }

    private void addPieChartEffects(PieChart.Data data, String absoluteLabel) {
        Node node = data.getNode();
        if (node != null) {
            Tooltip tooltip = new Tooltip(absoluteLabel);
            tooltip.setShowDelay(Duration.seconds(0.2));
            Tooltip.install(node, tooltip);

            node.setOnMouseEntered(event -> node.setStyle("-fx-effect: dropshadow(gaussian, #080808, 10, 0.5, 0, 0);"));
            node.setOnMouseExited(event -> node.setStyle(""));

            node.setOnMouseClicked(event -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pie Slice Clicked");
                alert.setHeaderText(null);
                alert.setContentText("Slice: " + data.getName() + "\nValue: " + (int) data.getPieValue());
                alert.showAndWait();
            });
        }
    }

    private String getPercentage(double value, int total) {
        if (total == 0) return "0%";
        double percentage = (value / total) * 100;
        return String.format("%.1f%%", percentage);
    }


    public void getOutputForDate(ActionEvent actionEvent) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "No date selected!", Alert.AlertType.ERROR);
            return;
        }

        String formattedDate = selectedDate.toString();
        System.out.println("Selected Date: " + formattedDate);

        AllJobs allJobs = new AllJobs();
        try {
            HashMap<String, Integer> results = allJobs.FormatReturn(formattedDate, "chart2");

            if (results.isEmpty()) {
                results = allJobs.getPreviousData("chart2");

                if (results.isEmpty()) {
                    updateState("No data available for the selected date or previous dates.", "red");
                    return;
                }
            }

            results.forEach((key, value) -> System.out.println(key + ": " + value));
            HashMap<String, Integer> finalResults = results;
            Platform.runLater(() -> {
                updatePieChartData(finalResults);
                updateState("Data loaded successfully!", "green");
            });

        } catch (FileNotFoundException e) {
            showAlert("Error", "File not found: " + e.getMessage() + "\nCheck if the file exists in the archive.", Alert.AlertType.ERROR);
        } catch (IOException e) {
            updateState("An error occurred while fetching data.", "red");
            e.printStackTrace();
        }
    }

    // Method to show an alert
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private void handleArchiveSelection(ActionEvent event) {
        String selectedArchive = Archives.getValue();
        if (selectedArchive != null && !selectedArchive.isEmpty()) {
            System.out.println("Selected Archive: " + selectedArchive);
            showLoadingPieChart();

            RectypeJob job = new RectypeJob("chart2", JobType.NORMAL);
            job.setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
                HashMap<String, Integer> jobResults = job.getValue();
                updatePieChartData(jobResults);
            }));

            job.setOnFailed(workerStateEvent -> Platform.runLater(() -> {
                panepie.getChildren().clear();
                panepie.getChildren().add(new Label("Failed to load data for the selected archive."));
                Throwable error = job.getException();
                error.printStackTrace();
            }));

            Thread jobThread = new Thread(job);
            jobThread.setDaemon(true);
            jobThread.start();
        }
    }

    private void updateState(String message, String color) {
        Platform.runLater(() -> {
            if (state == null) {
                System.out.println("state is null");
            }
            state.setText(message);
            state.setStyle("-fx-text-fill: " + color + ";");
        });
    }



}
