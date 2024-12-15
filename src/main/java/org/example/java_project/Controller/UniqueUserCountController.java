package org.example.java_project.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.UniqueUserCount;

import java.util.HashMap;

import static org.example.java_project.Service.DbConnection.getType;

public class UniqueUserCountController {

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private TableView<Result> tableView;

    @FXML
    private TableColumn<Result, String> typeColumn;

    @FXML
    private TableColumn<Result, Integer> countColumn;

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

   /* @FXML
    private void initialize(ActionEvent actionEvent) {
        try {
            UniqueUserCount uniqueUserCount = new UniqueUserCount("UniqueUserCount", JobType.NORMAL);
            HashMap<String, Integer> result = uniqueUserCount.call();


            XYChart.Series<String, Number> series = new XYChart.Series<>();
            result.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
            lineChart.getData().add(series);

            // Populate TableView
            ObservableList<Result> data = FXCollections.observableArrayList();
            result.forEach((type, count) -> data.add(new Result(type, count)));

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
            tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    @FXML
    private void initialize() {
        // Initialize table columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));

        // Load initial data
        loadData();
    }
    @FXML
    private void loadData() {
        try {
            // Clear previous data
            lineChart.getData().clear();
            tableView.getItems().clear();

            // Fetch new data
            UniqueUserCount uniqueUserCount = new UniqueUserCount("UniqueUserCount", JobType.NORMAL);
            HashMap<String, Integer> result = uniqueUserCount.call(); // This gets the raw data with id and count

            // Populate LineChart and TableView with type fetched from the database
            ObservableList<Result> data = FXCollections.observableArrayList();
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (String id : result.keySet()) {
                String type = getType(id);  // Fetch type from the database based on the id
                Integer count = result.get(id);

                if (type != null) {  // If type is found, add it to the chart and table
                    // Add to the chart
                    series.getData().add(new XYChart.Data<>(type, count));

                    // Add to the table
                    data.add(new Result(type, count));
                }
            }

            // Set chart data
            lineChart.getData().add(series);

            // Set table data
            tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
/*
    private void handleShowResults(ActionEvent actionEvent) {
        try {
            UniqueUserCount uniqueUserCount = new UniqueUserCount("UniqueUserCount", JobType.NORMAL);
            HashMap<String, Integer> result = uniqueUserCount.call();

            // Populate LineChart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            result.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
            lineChart.getData().add(series);

            // Populate TableView
            ObservableList<Result> data = FXCollections.observableArrayList();
            result.forEach((type, count) -> data.add(new Result(type, count)));

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
            tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    private void handleShowResults(ActionEvent actionEvent) {
        // Reload data on button click
        loadData();
    }
}
