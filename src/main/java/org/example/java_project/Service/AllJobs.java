package org.example.java_project.Service;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;


public class AllJobs  extends Task<HashMap<String, Integer>> {
    private String output ;
    private String jobName;
    static FileSystem fs ;


    public HashMap<String, Integer> call() {
        try {
            return FormatReturn(output, jobName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Integer> FormatReturn(String date, String jobName) throws IOException {
        Path baseOutputDir = new Path("/test/output_" + date +"_"+ jobName);
        fs = hadoopConf.getFileSystem();


        FileStatus[] jobDirs = fs.listStatus(baseOutputDir);
/*
        System.out.println(Arrays.toString(jobDirs));
*/
        if (!fs.exists(baseOutputDir)) {
            System.out.println("No output directory found for the selected date: " + date);
            return getPreviousData(jobName);
        }
        HashMap<String, Integer> aggregatedStatistics = new HashMap<>();
        for (FileStatus jobDir : jobDirs) {
            String dirName = jobDir.getPath().getName();
            if (dirName.endsWith("part-r-00000")) {
                Path outputFile = jobDir.getPath();

                if (!fs.exists(outputFile)) {
                    System.err.println("Output file not found for " + dirName);
                    continue;
                }

                FSDataInputStream inputStream = fs.open(outputFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] lineSplit = line.split("\\t");
                    String key = lineSplit[0];
                    int value = Integer.parseInt(lineSplit[1]);
                    aggregatedStatistics.put(key, aggregatedStatistics.getOrDefault(key, 0) + value);
                }

                reader.close();
                inputStream.close();
            }
        }
        //System.out.println(aggregatedStatistics);
        return aggregatedStatistics;
    }
    public HashMap<String, Integer> getPreviousData(String jobName) throws IOException {
        Path baseDir = new Path("/test");
        FileStatus[] allDirs = fs.listStatus(baseDir);

        String latestDate = null;
        for (FileStatus dir : allDirs) {
            String dirName = dir.getPath().getName();
            if (dirName.startsWith("output_") && dirName.endsWith("_" + jobName)) {
                String datePart = dirName.split("_")[1];
                if (latestDate == null || datePart.compareTo(latestDate) > 0) {
                    latestDate = datePart;
                }
            }
        }
        System.out.println(latestDate);

        if (latestDate != null) {
            System.out.println("Using data from the latest available date: " + latestDate);
            return FormatReturn(latestDate, jobName);
        } else {
            System.out.println("No previous data available.");
            return new HashMap<>();
        }
    }

}
