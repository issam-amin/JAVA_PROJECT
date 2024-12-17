package org.example.java_project.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.*;
import org.example.java_project.Service.DbConnection;
import org.example.java_project.Service.SparkSessionSingleton;
import org.example.java_project.Service.hadoopConf;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.example.java_project.Service.DbConnection.getAllClientNames;

public class ClientChurnController {

    private static FileSystem fs;

    @FXML
    private TableView<Map<String, String>> customerPredictionTableView;
    @FXML
    private TableColumn<Map<String, String>, String> customerIdColumn;
    @FXML
    private TableColumn<Map<String, String>, String> fullNameColumn;
    @FXML
    private TableColumn<Map<String, String>, String> predictionColumn;



    @FXML
    public void initialize() {
        customerIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("customerId")));
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("fullName")));
        predictionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get("prediction")));
        customerIdColumn.setCellFactory(this::defaultCellFactory);
        fullNameColumn.setCellFactory(this::defaultCellFactory);
        predictionColumn.setCellFactory(this::defaultCellFactory);

        // Add click event to the 'fullNameColumn'
        fullNameColumn.setCellFactory(tc -> {
            TableCell<Map<String, String>, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setOnMouseClicked(null);
                    } else {
                        setText(item);
                        setOnMouseClicked(event -> {
                            Map<String, String> selectedRow = getTableRow().getItem();
                            if (selectedRow != null) {
                                String customerId = selectedRow.get("customerId");
                                String fullName = selectedRow.get("fullName");
                                handleClientClick(customerId, fullName);
                            }
                        });
                    }
                }
            };
            return cell;
        });

        new Thread(this::runPredictionAndDisplay).start();
    }

    /**
     * Handles the click event for a client name.
     */
    private void handleClientClick(String customerId, String fullName) {
        try {
            boolean exportSuccess = DbConnection.export2("customer", "client_" + customerId + ".csv");
            if (exportSuccess) {
                System.out.println("Client data exported successfully for: " + fullName);
            } else {
                System.err.println("Failed to export client data for: " + fullName);
            }

            Map<String, String> clientDetails = getClientDetails(customerId);

            showClientPopup(fullName, clientDetails);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the client's details from the database.
     */
    private Map<String, String> getClientDetails(String customerId) throws SQLException, SQLException {
        Connection connection = DbConnection.getConnection();
        String sql = "SELECT * FROM customer WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, customerId);
        ResultSet resultSet = statement.executeQuery();

        Map<String, String> clientDetails = new HashMap<>();
        if (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);
                String value = resultSet.getString(i);
                clientDetails.put(columnName, value);
            }
        }
        return clientDetails;
    }
    TableCell<Map<String, String>, String> defaultCellFactory(TableColumn<Map<String, String>, String> column) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setText(null);
                    setStyle("-fx-opacity: 0;"); // Hides empty cells
                } else {
                    setText(item);
                    setStyle(""); // Reset style for non-empty cells
                }
            }
        };
    }


    /**
     * Displays the client's details in a popup.
     */
    private void showClientPopup(String fullName, Map<String, String> clientDetails) {
        StringBuilder detailsBuilder = new StringBuilder();
        clientDetails.forEach((key, value) -> detailsBuilder.append(key).append(": ").append(value).append("\n"));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Client Details");
        alert.setHeaderText("Details for: " + fullName);
        alert.setContentText(detailsBuilder.toString());
        alert.showAndWait();
    }


    public void runPredictionAndDisplay() {
        try {
            Dataset<Row> predictions = runPredictionAndGetResults();

            ObservableList<Map<String, String>> predictionItems = FXCollections.observableArrayList();
            Map<String, String> clientNames = getAllClientNames();

            // Process data and filter rows
            predictions.collectAsList().forEach(row -> {
                String customerId = row.getAs("customerID");
                String name = clientNames.getOrDefault(customerId, "Unknown");
                Double predictionValue = row.getAs("prediction");

                if (predictionValue == 1.0 && customerId != null && !customerId.trim().isEmpty()) {
                    Map<String, String> rowMap = new HashMap<>();
                    rowMap.put("customerId", customerId);
                    rowMap.put("fullName", name);
                    rowMap.put("prediction", "Probability of Leave");
                    predictionItems.add(rowMap);
                }
            });

            // Update TableView on the JavaFX thread
            javafx.application.Platform.runLater(() -> {
                customerPredictionTableView.getItems().clear(); // Clear existing data
                customerPredictionTableView.setItems(predictionItems);

                // Enhance the TableView style
                customerPredictionTableView.setStyle(
                        "-fx-font-family: 'Arial'; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-color: #f9f9f9; " +
                                "-fx-border-color: #dcdcdc; " +
                                "-fx-border-width: 1px;"
                );

                // Styling for fullName column
                fullNameColumn.setCellFactory(column -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
                        }
                    }
                });

                // Styling for prediction column
                predictionColumn.setCellFactory(column -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                        }
                    }
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopSparkSession();
        }
    }



    private void stopSparkSession() {
        SparkSession spark = SparkSessionSingleton.getInstance();
        if (spark != null && !spark.sparkContext().isStopped()) {
            spark.stop();
            SparkSessionSingleton.clearInstance();
            System.out.println("SparkSession has been stopped.");
        }
    }

    private Dataset<Row> runPredictionAndGetResults() {
        fetchDataFromDatabase();

        SparkSession spark = SparkSessionSingleton.getInstance();

        StructType schema = new StructType(new StructField[] {
                new StructField("customerID", DataTypes.StringType, true, Metadata.empty()),
                new StructField("Gender", DataTypes.StringType, true, Metadata.empty()),
                new StructField("SeniorCitizen", DataTypes.IntegerType, true, Metadata.empty()),
                new StructField("Partner", DataTypes.StringType, true, Metadata.empty()),
                new StructField("Dependents", DataTypes.StringType, true, Metadata.empty()),
                new StructField("Tenure", DataTypes.IntegerType, true, Metadata.empty()),
                new StructField("PhoneService", DataTypes.StringType, true, Metadata.empty()),
                new StructField("MultipleLines", DataTypes.StringType, true, Metadata.empty()),
                new StructField("InternetService", DataTypes.StringType, true, Metadata.empty()),
                new StructField("OnlineSecurity", DataTypes.StringType, true, Metadata.empty()),
                new StructField("OnlineBackup", DataTypes.StringType, true, Metadata.empty()),
                new StructField("DeviceProtection", DataTypes.StringType, true, Metadata.empty()),
                new StructField("TechSupport", DataTypes.StringType, true, Metadata.empty()),
                new StructField("StreamingTV", DataTypes.StringType, true, Metadata.empty()),
                new StructField("StreamingMovies", DataTypes.StringType, true, Metadata.empty()),
                new StructField("Contract", DataTypes.StringType, true, Metadata.empty()),
                new StructField("PaperlessBilling", DataTypes.StringType, true, Metadata.empty()),
                new StructField("PaymentMethod", DataTypes.StringType, true, Metadata.empty()),
                new StructField("MonthlyCharges", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("TotalCharges", DataTypes.StringType, true, Metadata.empty()),
        });

        Dataset<Row> testData = spark.read()
                .option("header", "false")
                .schema(schema)
                .csv("hdfs://localhost:9000/test/input/data_client.csv");

        testData = testData.filter("TotalCharges IS NOT NULL AND TotalCharges != ''")
                .withColumn("TotalCharges", testData.col("TotalCharges").cast(DataTypes.DoubleType))
                .na().drop();
        testData = testData.withColumn("Churn", functions.lit("No"));

        PipelineModel model = PipelineModel.load("hdfs://localhost:9000/test/churn_prediction_model");

        Dataset<Row> predictions = model.transform(testData)
                .select("customerID", "prediction"); // Select the relevant columns

        Dataset<Row> predictionsWithChurn = predictions.withColumn("Churn", functions.when(predictions.col("prediction").equalTo(1.0), "Leave").otherwise("Stay"));

        predictionsWithChurn.write()
                .mode("overwrite")
                .option("header", "true")
                .csv("hdfs://localhost:9000/test/churn_prediction");

        // Display predictions in the console
        predictionsWithChurn.show(10, false);

        return predictionsWithChurn;
    }


    // Method to fetch data from the database and upload to HDFS
    public void fetchDataFromDatabase() {
        try {
            String tableName = "customer"; // Example table name
            String relativeCsvPath = "src/main/resources/data_client.csv";
            String localCsvPath = "data_client.csv";
            String hdfsPath = "hdfs://localhost:9000/test/input/data_client.csv";

            boolean exportSuccess = DbConnection.export2(tableName, localCsvPath);

            if (exportSuccess) {
                // If export is successful, upload it to HDFS
                uploadFileToHdfs(relativeCsvPath, hdfsPath);
            } else {
                System.err.println("Failed to export data to CSV.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to upload CSV file to HDFS
    private void uploadFileToHdfs(String localFilePath, String hdfsFilePath) throws IOException {
        // Open connection to HDFS
        fs = hadoopConf.getFileSystem();
        Path localPath = new Path(localFilePath);
        Path hdfsPath = new Path(hdfsFilePath);

        // Ensure the target directory exists in HDFS
        Path parentDir = hdfsPath.getParent();
        if (!fs.exists(parentDir)) {
            fs.mkdirs(parentDir);
        }

        // Overwrite existing file in HDFS
        if (fs.exists(hdfsPath)) {
            fs.delete(hdfsPath, false);
        }

        // Copy file from local to HDFS
        fs.copyFromLocalFile(localPath, hdfsPath);
        System.out.println("File uploaded to HDFS: " + hdfsFilePath);
    }
}
