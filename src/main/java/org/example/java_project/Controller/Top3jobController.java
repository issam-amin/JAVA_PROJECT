package org.example.java_project.Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.hadoop.fs.Path;
import org.example.java_project.MainD;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.OldTop3job;
import org.example.java_project.Service.Top3Job;
import org.example.java_project.Service.hadoopConf;
import org.example.java_project.Util.Pair;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.java_project.MainD.*;


public class Top3jobController implements Initializable {

    BarChart<String, Number>  barChart;

    @FXML
    DatePicker date;
    @FXML
    VBox List;
    @FXML
    protected VBox pane;
    @FXML
    Text state;
    @FXML
    VBox pnItems ;
    @Override
    public void initialize(  URL location, ResourceBundle resources ) {
            date.setValue(LocalDate.now());
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
        job.setOnFailed(event->{
            Platform.runLater(() -> {
                state.setText("");

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
             showCustomPopup(MainD.getPrimaryStage());
             state.setText("");
            this.date.setValue(LocalDate.now());
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

                Parent parentNode = (Parent) node;

                ImageView image = (ImageView) parentNode.lookup("#image") ;


                if(pair.getValue() > some / 4){
                  image.setImage(new Image(getClass().getResource("../../images/redArrows.png").toExternalForm()));
                }else{
                  image.setImage(new Image(getClass().getResource("../../images/greenArrows.png").toExternalForm()));
                }

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

                final Button Action = (Button) parentNode.lookup("#ActionButton");
                Action.setDisable(true);
                Action.setVisible(false);                /*Action.setOnMouseClicked(event ->{
                    Stage newStage = new Stage();
                    newStage.setTitle("New Stage");
                    Parent newRoot = null;
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../Barchar/ComplaintsList.fxml"));
                        complaintsLIstController controller =  new complaintsLIstController();
                        controller.getinfo("sss");
                        loader.setController(controller);
                        newRoot = loader.load();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Scene newScene = new Scene(newRoot);
                    newStage.setScene(newScene);
                    newStage.show(); // Show the new stage
                });*/


                pnItems.getChildren().add(node);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private void showCustomPopup(Stage parentStage) {
        // Create a new Stage for the pop-up
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with parent stage
        popupStage.initOwner(parentStage); // Sets the owner for the pop-up

        // Design the content of the pop-up
        Text message = new Text("This is a custom pop-up!");
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popupStage.close());

        VBox popupLayout = new VBox(10, message, closeButton);
        popupLayout.setAlignment(Pos.CENTER);
        popupLayout.setStyle("-fx-padding: 20; -fx-background-color: lightblue; -fx-border-color: darkblue; -fx-border-width: 2;");

        Scene popupScene = new Scene(popupLayout, 300, 150);
        popupStage.setScene(popupScene);
        popupStage.setTitle("Pop-Up");
        popupStage.showAndWait(); // Makes it a blocking dialog
    }

    @FXML
    void exportJob(){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                Platform.runLater(() -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName("output_"+date.getValue()+".csv");
                    // Set an extension filter for text files
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("csv Files", "*.csv")
                    );
                    File file = fileChooser.showSaveDialog(getPrimaryStage());
                    System.out.println("1");

                    FileSystem fs = hadoopConf.getFileSystem();
                    String output = "/test/output_"+date.getValue()+ "_chart1/part-r-00000";

                    Path outputFile = new Path(output);
                    FSDataInputStream inputStream = null;
                    try {
                        inputStream = fs.open(outputFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    FileWriter csvWriter = null;
                    try {
                        csvWriter = new FileWriter(file.getAbsolutePath());
                        csvWriter.append("issues,count").append("\n");
                        while ((line = reader.readLine()) != null) {
                            String[] tokens = line.split("\t");
                            csvWriter.append(tokens[0]).append(",").append(tokens[1]).append("\n");;
                            System.out.println(line);
                        }
                        System.out.println("CSV file created successfully: " + file.getAbsolutePath());
                        csvWriter.flush();
                        csvWriter.close();
                        reader.close();
                        inputStream.close();
                    } catch (IOException e) {
                        System.out.println("couldn't save file: " + file.getAbsolutePath());

                    }
                });




                return null;
            }
        };
        task.setOnSucceeded(Event ->{
                System.out.println("finished running thr tread");
        });
        task.setOnRunning(Event ->{
                System.out.println("export started");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();



    }
}
