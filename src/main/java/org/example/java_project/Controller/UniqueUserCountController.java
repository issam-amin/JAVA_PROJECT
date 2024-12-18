package org.example.java_project.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.UniqueUserCount;

import java.util.HashMap;

import static org.example.java_project.Service.DbConnection.getType;

public class UniqueUserCountController {

    @FXML
    private TableView<Result> tableView;

    @FXML
    private TableColumn<Result, String> typeColumn;

    @FXML
    private TableColumn<Result, Integer> countColumn;

    @FXML
    private Button loadButton;

    @FXML
    private Button chartButton;

    public static class Result {
        private final String type;
        private final Integer count;

        public Result(String type, Integer count) {
            this.type = type;
            this.count = count;
        }

        public String getType() {
            return type;
        }

        public Integer getCount() {
            return count;
        }
    }

    @FXML
    private void initialize() {
        // Initialize table columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));

        // Set button actions
        loadButton.setOnAction(event -> loadData(JobType.REFRESH));
        chartButton.setOnAction(event -> showChart());

        // Load initial data
        loadData(JobType.NORMAL);
    }

    @FXML
    private void loadData( JobType jobType) {
        try {
            // Clear previous data
            tableView.getItems().clear();

            // Fetch new data
            UniqueUserCount uniqueUserCount = new UniqueUserCount("UniqueUserCount", jobType);
            HashMap<String, Integer> result = uniqueUserCount.call();

            // Populate TableView
            ObservableList<Result> data = FXCollections.observableArrayList();

            for (String id : result.keySet()) {
                String type = getType(id); // Fetch type from the database
                Integer count = result.get(id);

                if (type != null) {
                    data.add(new Result(type, count));
                }
            }

            tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showChart() {
        try {
            UniqueUserCount uniqueUserCount = new UniqueUserCount("UniqueUserCount", JobType.NORMAL);
            HashMap<String, Integer> result = uniqueUserCount.call();

            LineChart<String, Number> lineChart = new LineChart<>(new CategoryAxis(), new NumberAxis());
            lineChart.setTitle("Unique User Count");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (String id : result.keySet()) {
                String type = getType(id); // Fetch type from the database
                Integer count = result.get(id);
                if (type != null) {
                    series.getData().add(new XYChart.Data<>(type, count));
                }
            }
            lineChart.getData().add(series);

            Stage chartStage = new Stage();
            VBox chartLayout = new VBox(lineChart);
            chartLayout.setPadding(new javafx.geometry.Insets(10));

            Scene scene = new Scene(chartLayout, 800, 600);
            chartStage.setTitle("Line Chart");
            chartStage.setScene(scene);
            chartStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
