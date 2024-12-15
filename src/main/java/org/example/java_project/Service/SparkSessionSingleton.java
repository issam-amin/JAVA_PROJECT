package org.example.java_project.Service;

import org.apache.spark.sql.SparkSession;

public class SparkSessionSingleton {
    private static SparkSession instance = null;

    private SparkSessionSingleton() {}

    public static SparkSession getInstance() {
        if (instance == null) {
            instance = SparkSession.builder()
                    .appName("ChurnPredictionApp")
                    .master("local[*]") // Replace with your cluster master if needed
                    .config("spark.executor.memory", "2g") // Example config
                    .getOrCreate();
        }
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }
}
