package org.example.java_project.Controller;
import javafx.scene.Scene;
import org.example.java_project.Service.RectypeJob;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.ResourceBundle;

public class RectypeController implements Initializable {
    @FXML
    protected HBox panepie;
    private Random random = new Random();
    private PieChart loadingPieChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        runjob2("chart2");
    }
    public void refresh_Piechart(ActionEvent actionEvent) {
        runjob2("chart2");
    }


    /*protected void runjob2(String jobName) {
        showLoadingPieChart();

        RectypeJob job = new RectypeJob(jobName);

        job.setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
            HashMap<String, Integer> jobResults = job.getValue();
            PieChart pieChart = createPieChart(jobResults);
            panepie.getChildren().clear(); // Clear existing content in panepie
            panepie.getChildren().add(pieChart); // Add the new PieChart
        }));

        // Step 4: Handle job failure
        job.setOnFailed(workerStateEvent -> Platform.runLater(() -> {
            panepie.getChildren().clear(); // Clear existing content in panepie
            panepie.getChildren().add(new Label("Failed to load data.")); // Display error message
            Throwable error = job.getException();
            error.printStackTrace();
        }));

        // Step 5: Start the job
        Thread jobThread = new Thread(job);
        jobThread.setDaemon(true);
        jobThread.start();
    }*/
    protected void runjob2(String jobName) {
        showLoadingPieChart();

        RectypeJob job = new RectypeJob(jobName);

        job.setOnSucceeded(workerStateEvent -> Platform.runLater(() -> {
            HashMap<String, Integer> jobResults = job.getValue();
            updatePieChartData(jobResults);
        }));

        job.setOnFailed(workerStateEvent -> Platform.runLater(() -> {
            panepie.getChildren().clear(); // Clear existing content in panepie
            panepie.getChildren().add(new Label("Failed to load data.")); // Display error message
            Throwable error = job.getException();
            error.printStackTrace();
        }));

        Thread jobThread = new Thread(job);
        jobThread.setDaemon(true);
        jobThread.start();
    }



    private void showLoadingPieChart() {
        Random random = new Random();
        ObservableList<PieChart.Data> randomData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", random.nextInt(5000 - 1000) + 1000),
                new PieChart.Data("In Progress Critique", random.nextInt(5000 - 1000) + 1000),
                new PieChart.Data("Completed", random.nextInt(5000 - 1000) + 1000)
        );

        loadingPieChart = new PieChart(randomData);
        loadingPieChart.setTitle("Reclamations");
        loadingPieChart.setClockwise(true);
        loadingPieChart.setLabelLineLength(50);
        loadingPieChart.setLabelsVisible(true);
        loadingPieChart.setLegendVisible(false);
        loadingPieChart.setStartAngle(180);
        Scene scene = panepie.getScene();
        if (scene != null) {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("../Style/pieChart.css")).toExternalForm());
        }
       /* for (PieChart.Data data : loadingPieChart.getData()) {
            switch (data.getName()) {
                case "Pending":
                    data.getNode().setStyle("-fx-pie-color: #47f0ff;");
                    break;
                case "In Progress Critique":
                    data.getNode().setStyle("-fx-pie-color: #0077ff;");
                    break;
                case "Completed":
                    data.getNode().setStyle("-fx-pie-color: #32CD32;");
                    break;
            }
        }*/
        panepie.getChildren().clear();
        panepie.getChildren().add(loadingPieChart);
    }
    private void updatePieChartData(HashMap<String, Integer> jobResults) {
        int total = jobResults.values().stream().mapToInt(Integer::intValue).sum();

        for (PieChart.Data data : loadingPieChart.getData()) {
            switch (data.getName()) {
                case "Pending":
                    data.setPieValue(jobResults.getOrDefault("pending", 0));
                    break;
                case "In Progress Critique":
                    data.setPieValue(jobResults.getOrDefault("in progress critique", 0));
                    break;
                case "Completed":
                    data.setPieValue(jobResults.getOrDefault("completed", 0));
                    break;
            }

            String percentageLabel = getPercentage(data.getPieValue(), total);
            String initialLabel = data.getName() + " " + percentageLabel;
            String absoluteLabel = data.getName() + ": " + (int) data.getPieValue();
            data.nameProperty().set(initialLabel);
            Tooltip tooltip = new Tooltip(absoluteLabel);
            tooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(data.getNode(), tooltip);
/*
            for (PieChart.Data data1 : loadingPieChart.getData()) {
                switch (data1.getName()) {
                    case "Pending":
                        data1.getNode().setStyle("-fx-pie-color: #47f0ff;");
                        break;
                    case "In Progress Critique":
                        data1.getNode().setStyle("-fx-pie-color: #ff0026;");
                        break;
                    case "Completed":
                        data1.getNode().setStyle("-fx-pie-color: #cd9732;");
                        break;
                }
            }
*/

        }
    }

    private String getPercentage(double value, int total) {
        if (total == 0) return "0%";
        double percentage = (value / total) * 100;
        return String.format("%.1f%%", percentage);
    }



}
