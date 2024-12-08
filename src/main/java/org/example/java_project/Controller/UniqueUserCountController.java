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

    @FXML
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
    }
    @FXML
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
}
