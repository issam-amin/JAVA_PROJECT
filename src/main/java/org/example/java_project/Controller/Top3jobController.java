package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.apache.hadoop.fs.FileSystem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.OldTop3job;
import org.example.java_project.Service.Top3Job;
import org.example.java_project.Util.Pair;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;



public class Top3jobController implements Initializable {

    BarChart<String, Number>  barChart;
    FileSystem fileSystem ;

    @FXML
    DatePicker date;

    @FXML
    protected VBox pane;
    @FXML
    Text state;
    @FXML
    VBox pnItems ;
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
        barChart.setLegendVisible(false);
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
                state.setText("");
                pane.getChildren().add(barChart);
                listComplaints( Top3Job.getlistComplaints() , LocalDate.now() ,Top3Job.some);
            });
        });

        job.setOnRunning(workerStateEvent -> {
            Platform.runLater(() -> {
                state.setText("Loading...");
                state.setStyle("-fx-text-fill: white;");
                System.out.println("dds");
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

    

    public void GetDate(ActionEvent actionEvent) {
        getOldJob("chart1" , date.getValue());
    }
    
    
    void getOldJob(String jobName, LocalDate  date){


        OldTop3job job = new OldTop3job(jobName , date);
        job.setOnSucceeded(workerStateEvent -> {
            
            Platform.runLater(() -> {
                XYChart.Series<String, Number> updatedSeries = job.getValue();
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
                state.setText("");
                pane.getChildren().clear();
                pane.getChildren().add(barChart);
                listComplaints( OldTop3job.getlistComplaints() , date ,OldTop3job.some);
            });
        });

        job.setOnFailed(workerStateEvent -> {
            System.out.println("1111");
        });


        job.setOnRunning(workerStateEvent -> {
            Platform.runLater(() -> {
                state.setText("Loading...");
                state.setStyle("-fx-text-fill: white;");
                System.out.println("dds");
            });
        });
        Thread testJob = new Thread(job);
        testJob.setDaemon(true);
        testJob.start();
    }




    void listComplaints(  ArrayList<Pair> IssusCount,  LocalDate date  , int some){
        pnItems.getChildren().clear();
        for (Pair pair : IssusCount) {
            try {
                Node node =  FXMLLoader.load(getClass().getResource("../Item.fxml"));
                node.setOnMouseEntered(event -> {
                    node.setStyle("-fx-background-color : #0A0E3F");
                });
                node.setOnMouseExited(event -> {
                    node.setStyle("-fx-background-color : #02030A");
                });

                Parent parentNode = (Parent) node;

                Label label = (Label) parentNode.lookup("#id");
                if (label != null) {
                    label.setText(pair.getKey());
                }
                label = (Label) parentNode.lookup("#count");
                if (label != null) {
                    label.setText(pair.getValue()+"");
                }

                label = (Label) parentNode.lookup("#countDate");
                if (label != null) {
                    label.setText(date.toString());
                }
                label = (Label) parentNode.lookup("#percentage");
                if (label != null) {
                    label.setText(String.format("%.2f%%", pair.getValue() * 1.0 /some * 100));
                }

                pnItems.getChildren().add(node);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        /*Node[] nodes = new Node[10];


        for (int i = 0; i < nodes.length; i++) {

            try {
                final int j = i;
                nodes[i] = FXMLLoader.load(getClass().getResource("../Item.fxml"));

                //give the items some effect
                nodes[i].setOnMouseEntered(event -> {
                    nodes[j].setStyle("-fx-background-color : #0A0E3F");
                });
                nodes[i].setOnMouseExited(event -> {
                    nodes[j].setStyle("-fx-background-color : #02030A");
                });

                if (nodes[i] instanceof Parent) {
                    Parent parentNode = (Parent) nodes[i];
                    // Access all children
                    ObservableList<Node> children = parentNode.getChildrenUnmodifiable();
                    for (Node child : children) {
                        System.out.println("Child: " + child);
                    }

                    // Lookup specific child nodes by fx:id
                    Label label = (Label) parentNode.lookup("#id");
                    if (label != null) {
                        label.setText("Label " + i);
                    }

                   *//* Button button = (Button) parentNode.lookup("#buttonId");
                    if (button != null) {
                        button.setOnAction(event -> System.out.println("Button " + i + " clicked!"));
                    }*//*
                }

                pnItems.getChildren().add(nodes[i]);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/






    }
}
