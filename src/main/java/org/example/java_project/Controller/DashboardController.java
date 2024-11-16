package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import javafx.util.Duration;
import org.example.java_project.Service.Top3Job;


public class DashboardController {
    public BarChart<String, Number>  barChart;
    @FXML
    protected HBox pane;
    @FXML
    protected void onClickStart() {
        runjob("chart1");
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
            dataPoint.getNode().setStyle("-fx-bar-fill: #ff6347;"); // Change color on hover
            Tooltip tooltip = new Tooltip(dataPoint.getXValue() + ": " + dataPoint.getYValue() + "%");
            tooltip.setShowDelay(Duration.seconds(0.2));
            Tooltip.install(dataPoint.getNode(), tooltip); // Show tooltip on hover
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



}
