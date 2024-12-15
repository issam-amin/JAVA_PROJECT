package org.example.java_project.Controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        new Thread(this::runPredictionAndDisplay).start();
    }

    public void runPredictionAndDisplay() {
        try {
            Dataset<Row> predictions = runPredictionAndGetResults();

            ObservableList<Map<String, String>> predictionItems = FXCollections.observableArrayList();

            // Fetch client names from the database
            Map<String, String> clientNames = getAllClientNames();

            // Process the predictions and format them for the TableView
            predictions.collectAsList().forEach(row -> {
                String customerId = row.getAs("customerID");
                String name = clientNames.getOrDefault(customerId, "Unknown");
                Double predictionValue = row.getAs("prediction");

                // Prepare the row for display
                Map<String, String> rowMap = new HashMap<>();
                rowMap.put("customerId", customerId);
                rowMap.put("fullName", name);
                rowMap.put("prediction", (predictionValue == 1.0) ? "Leave" : "Stay");

                // Add the row map to the list
                predictionItems.add(rowMap);
            });

            // Set the TableView items
            javafx.application.Platform.runLater(() -> customerPredictionTableView.setItems(predictionItems));

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
                .csv("hdfs://localhost:9000/amine/input/data_client.csv");

        testData = testData.filter("TotalCharges IS NOT NULL AND TotalCharges != ''")
                .withColumn("TotalCharges", testData.col("TotalCharges").cast(DataTypes.DoubleType))
                .na().drop();
        testData = testData.withColumn("Churn", functions.lit("No"));

        PipelineModel model = PipelineModel.load("hdfs://localhost:9000/amine/churn_prediction_model");

        Dataset<Row> predictions = model.transform(testData)
                .select("customerID", "prediction"); // Select the relevant columns

        Dataset<Row> predictionsWithChurn = predictions.withColumn("Churn", functions.when(predictions.col("prediction").equalTo(1.0), "Leave").otherwise("Stay"));

        predictionsWithChurn.write()
                .mode("overwrite")
                .option("header", "true")
                .csv("hdfs://localhost:9000/amine/churn_prediction");

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
            String hdfsPath = "hdfs://localhost:9000/amine/input/data_client.csv";

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
