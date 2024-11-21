package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javafx.util.Duration;
import org.example.java_project.Service.RectypeJob;
import org.example.java_project.Service.Top3Job;

import java.util.HashMap;


public class DashboardController {
    public BarChart<String, Number>  barChart;
    @FXML
    protected HBox pane;
    @FXML
    protected HBox panepie;
    @FXML
    private BorderPane borderPane;

    @FXML
    protected void onClickStart() {
        runjob("chart1");
        runjob2("chart2");
    }

    protected void runjob(String JobName){

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("X-Axis");
        yAxis.setLabel("Y-Axis");

        XYChart.Series<String, Number> sampleSeries = new XYChart.Series<>();
        sampleSeries.setName("Loading...");
        barChart.getData().add(sampleSeries);
        pane.getChildren().add(barChart);
        Top3Job job = new Top3Job(JobName);

        job.setOnSucceeded(workerStateEvent -> {
            Platform.runLater(() -> {
            CategoryAxis xAxis2 = new CategoryAxis();
            NumberAxis yAxis2 = new NumberAxis(0, 100, 10); // from 0 to 100 with tick unit of 10
            yAxis2.setLabel("Percentage (%)");
            // Set a custom tick label formatter to show percentages
            yAxis2.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis2) {
                @Override
                public String toString(Number object) {
                    return String.format("%.0f%%", object.doubleValue()); // format as percentage
                }
            });
            barChart  = new BarChart<>(xAxis2, yAxis2);
            barChart.getData().clear();
            System.out.println("Successfully got the top 3 job");
            XYChart.Series<String, Number> updatedSeries = job.getValue();
            barChart.setCategoryGap(2);
            barChart.setBarGap(5);
            barChart.getData().add(updatedSeries);
                var data   =  barChart.getData().get(0);
                for ( var item : data.getData() ){
                     addDataPointInteractivity(item);
                }
            pane.getChildren().clear();
            pane.getChildren().add(barChart);
            });
        });

        Thread testJob = new Thread(job);
        testJob.setDaemon(true);
        testJob.start();

    }

    private void addDataPointInteractivity(XYChart.Data<String, Number> dataPoint) {
        // Add hover effect
        dataPoint.getNode().setOnMouseEntered(event -> {
            dataPoint.getNode().setStyle("-fx-bar-fill: black;"); // Change color on hover
            Tooltip tooltip = new Tooltip(dataPoint.getXValue() + ": " + dataPoint.getYValue().intValue() + "%");
            tooltip.setShowDelay(Duration.seconds(0.2));
            Tooltip.install(dataPoint.getNode(), tooltip);
        });
        dataPoint.getNode().setOnMouseExited(event -> {
            dataPoint.getNode().setStyle(""); // Reset color on exit
        });

        // Add click event
        dataPoint.getNode().setOnMouseClicked(event -> {
            System.out.println("Clicked on " + dataPoint.getXValue() + ": " + dataPoint.getYValue());
            // Perform additional actions, such as updating other parts of the UI
        });
    }

    private void runjob2(String jobName) {
        // Step 1: Show Loading Pie Chart
        showLoadingPieChart();

        // Step 2: Create the job
        RectypeJob job = new RectypeJob(jobName);

        // Step 3: Handle job success
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
    }

    private void showLoadingPieChart() {
        ObservableList<PieChart.Data> loadingData = FXCollections.observableArrayList(
                new PieChart.Data("Loading...", 1)
        );
        PieChart loadingChart = new PieChart(loadingData);
        loadingChart.setTitle("Loading...");
        loadingChart.setClockwise(true);
        loadingChart.setLabelsVisible(false);
        loadingChart.setLegendVisible(false);
        Platform.runLater(() -> {
            panepie.getChildren().clear(); // Clear existing content
            panepie.getChildren().add(loadingChart); // Add the loading chart
        });
    }

    private PieChart createPieChart(HashMap<String, Integer> jobResults) {
        int total = jobResults.values().stream().mapToInt(Integer::intValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", jobResults.getOrDefault("pending", 0)),
                new PieChart.Data("In Progress Critique", jobResults.getOrDefault("in progress critique", 0)),
                new PieChart.Data("Completed", jobResults.getOrDefault("completed", 0))
        );

        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Reclamations");
        pieChart.setClockwise(true);
        pieChart.setLabelLineLength(20);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setStartAngle(180);
        pieChart.setPrefSize(500, 500);

        pieChartData.forEach(data -> {
            String percentageLabel = getPercentage(data.getPieValue(), total);

            data.nameProperty().set(data.getName() + " " + percentageLabel);

            data.getNode().setOnMouseEntered(event -> {
                String absoluteLabel = data.getName() + ": " + (int) data.getPieValue();
                data.nameProperty().set(absoluteLabel);
            });

            data.getNode().setOnMouseExited(event -> {
                data.nameProperty().set(data.getName() + " " + percentageLabel);
            });
        });

        return pieChart;
    }

    private String getPercentage(double value, int total) {
        if (total == 0) return "0.00%";
        double percentage = (value / total) * 100;
        return String.format("%.2f%%", percentage);
    }




}
