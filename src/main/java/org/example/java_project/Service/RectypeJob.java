package org.example.java_project.Service;


import javafx.concurrent.Task;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class RectypeJob extends Task<HashMap<String, Integer>> {
    private final String input;
    private final String output;
    private static String jobName;

    private final String currentDate;
    private LocalTime jobTime;


    public RectypeJob(String jobName) {
        this.jobName = jobName;
        this.jobTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.currentDate = LocalDate.now().format(formatter);
       // System.out.println(currentDate);
        this.input = "/test/input_" + jobName + "/data.csv";
        this.output = "/test/output_" +currentDate+"_"+ jobName;
    }

    public HashMap<String, Integer> call() {

        boolean exportRes = DbConnection.export("rec");
      //  System.out.println(exportRes);
        try {

            return RunJob("src/main/resources/data.csv", output, input, jobName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Map and Reduce classes remain unchanged
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();
        DateTimeFormatter formatter6 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatter4 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(",");

            if (tokens.length > 6) {
                String status = tokens[5].trim().toLowerCase();
                String startDateStr = tokens[4].trim();
                String endDateStr = tokens[6].trim();

                if (status.equals("completed")) {
                    word.set("completed");
                    context.write(word, one);
                } else if (status.equals("pending")) {
                    try {
                        LocalDateTime startDate = LocalDate.parse(startDateStr, formatter4).atStartOfDay();
                        LocalDateTime endDate = LocalDate.parse(endDateStr, formatter6).atStartOfDay();

                        long daysDifference = ChronoUnit.DAYS.between(startDate, endDate);
                        word.set(daysDifference > 7 ? "in progress critique" : "pending");
                        context.write(word, one);
                    } catch (DateTimeParseException e) {
                        System.err.println("Error parsing date: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private final IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    protected static HashMap<String, Integer> RunJob(String localFilePath, String outputPathStr, String hdfsFilePathStr,String JobName) throws Exception {
        Path outputPath = new Path(outputPathStr);
        Path hdfsFilePath = new Path(hdfsFilePathStr);
        Path localPath = new Path(localFilePath);

        Job job = hadoopConf.getJob(JobName);
        FileSystem fs = hadoopConf.getFileSystem();


        job.setJarByClass(RectypeJob.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        if (fs.exists(hdfsFilePath)) {
            fs.delete(hdfsFilePath, true);
            System.out.println("File deleted successfully!");
        }

        fs.copyFromLocalFile(localPath, hdfsFilePath);
        System.out.println("File uploaded to HDFS: " + hdfsFilePath);

        // Check if the output path exists, and delete if it does
        if (fs.exists(outputPath)) {
            if (fs.delete(outputPath, true)) {
                System.out.println("Output path deleted successfully!");
            } else {
                System.out.println("Output path deletion failed. Exiting.");
                System.exit(-1);
            }
        }

        // Set input and output paths for the job
        FileInputFormat.addInputPath(job, hdfsFilePath);
        FileOutputFormat.setOutputPath(job, outputPath);

        // Run the job and wait for completion
        boolean jobCompleted = job.waitForCompletion(true);
        if (!jobCompleted) {
            System.exit(1);
        }

        System.out.println("MapReduce Job Completed. Reading Output...");
        Path outputFile = new Path(outputPathStr + "/part-r-00000");


        if (!fs.exists(outputFile)) {
            System.err.println("Output file not found!");
            System.exit(-1);
        }

        // Read the output file content
        FSDataInputStream inputStream = fs.open(outputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        HashMap<String, Integer> statistics = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String[] lineSplit = line.split("\\t");
            statistics.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
            System.out.println(line);
        }

        reader.close();
        inputStream.close();
        return statistics;
    }





}
