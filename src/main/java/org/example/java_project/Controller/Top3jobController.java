package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.Top3Job;
import java.net.URL;
import java.util.ResourceBundle;



public class Top3jobController implements Initializable {

    BarChart<String, Number>  barChart;

    @FXML
    protected HBox pane;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        runjob("chart1" , JobType.NORMAL);
    }

    @FXML
    protected void onClickStart() {
        runjob("chart1" , JobType.REFRESH);
    }

    protected void runjob(String JobName ,JobType jobType){
        pane.getChildren().clear();
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("X-Axis");
        yAxis.setLabel("Y-Axis");
        xAxis.setStyle("-fx-tick-label-fill: white; " +
                "-fx-tick-label-font-size: 14px; " +
                "-fx-tick-label-font-weight: bold;"); // X-axis text color
        yAxis.setStyle("-fx-tick-label-fill: white; " +
                "-fx-tick-label-font-size: 14px; " +
                "-fx-tick-label-font-weight: bold;");
        XYChart.Series<String, Number> sampleSeries = new XYChart.Series<>();
        sampleSeries.setName("Loading...");
        pane.getChildren().add(barChart);
        barChart.getData().add(sampleSeries);


      Top3Job job = new Top3Job(JobName, jobType);
        job.setOnSucceeded(workerStateEvent -> {
            Platform.runLater(() -> {
                CategoryAxis xAxis2 = new CategoryAxis();
                NumberAxis yAxis2 = new NumberAxis(0, 100, 10); // from 0 to 100 with tick unit of 10
                yAxis2.setLabel("Percentage (%)");
                xAxis2.setStyle("-fx-tick-label-fill: white; " +
                        "-fx-tick-label-font-size: 14px; " +
                        "-fx-tick-label-font-weight: bold;"); // X-axis text color
                yAxis2.setStyle("-fx-tick-label-fill: white; " +
                        "-fx-tick-label-font-size: 14px; " +
                        "-fx-tick-label-font-weight: bold;");

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
                yAxis2.setTickMarkVisible(false);
                yAxis2.setTickLabelsVisible(true); // Optional: Keep axis labels
                yAxis2.setMinorTickVisible(false);
                barChart.setBarGap(5);
                barChart.getData().add(updatedSeries);
                var data   =  barChart.getData().get(0);
                for ( var item : data.getData() ){
                    addDataPointInteractivity(item);
                }
                barChart.setLegendVisible(false);
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
            dataPoint.getNode().setStyle("-fx-bar-fill: white;"); // Change color on hover
            Tooltip tooltip = new Tooltip(dataPoint.getXValue() + ": " + dataPoint.getYValue().intValue() + "%");
            tooltip.setShowDelay(Duration.seconds(0.2));
            tooltip.fontProperty().set(new Font("arial", 24));
            tooltip.setStyle("-fx-text-fill: white;");
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


}
