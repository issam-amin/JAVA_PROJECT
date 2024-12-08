package org.example.java_project.Service;

import javafx.concurrent.Task;
import org.apache.hadoop.conf.Configuration;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.example.java_project.Service.DbConnection.getClient;
import static org.example.java_project.Service.DbConnection.getType;

public class clientTypeJob extends Task<Map<String, Integer>> {
    private final String input;
    private final String output;
    private static String jobName;
    private static FileSystem fs;
    private final String currentDate;
    private LocalTime jobTime;
    private static JobType jobType;
    private static Configuration hadoopConf;

    public clientTypeJob(String jobName, JobType jobType) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.jobTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.currentDate = LocalDate.now().format(formatter);
        this.input = "/test/input/data.csv";
        this.output = "/test/output_" + currentDate + "_" + jobName;
    }

    @Override
    protected Map<String, Integer> call() throws Exception {
        hadoopConf = new Configuration();
        fs = FileSystem.get(hadoopConf);
        boolean outputExists = fs.exists(new Path(output));

        if (outputExists && jobType == JobType.NORMAL) {
            System.out.println("Output already exists for normal job. Reading existing output.");
            return formatReturn();
        } else {
            if (outputExists) {
                fs.delete(new Path(output), true);
                System.out.println("Old output deleted for job: " + jobName);
            }
            return runJob("src/main/resources/data.csv", output, input, jobName);
        }
    }

    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(",");
            if (tokens.length > 1) {
                String clientId = tokens[1].trim();
                // String client = getClient(clientId);
                String reclamationId = tokens[2].trim();
                //String reclamationType = getType(reclamationId);
                //String pair = client + "_" + reclamationType;
                String pair2 = clientId + "_" + reclamationId;
                word.set(pair2);
                context.write(word, one);
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

    protected Map<String, Integer> runJob(String localFilePath, String outputPathStr, String hdfsFilePathStr, String jobName) throws Exception {
        Path outputPath = new Path(outputPathStr);
        Path hdfsFilePath = new Path(hdfsFilePathStr);
        Path localPath = new Path(localFilePath);

        Job job = Job.getInstance(hadoopConf, jobName);
        fs = FileSystem.get(hadoopConf);

        job.setJarByClass(clientTypeJob.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setNumReduceTasks(10);  // Increase the number of reducers

        if (fs.exists(hdfsFilePath)) {
            fs.delete(hdfsFilePath, true);
            System.out.println("File deleted successfully!");
        }

        fs.copyFromLocalFile(localPath, hdfsFilePath);
        System.out.println("File uploaded to HDFS: " + hdfsFilePath);

        if (fs.exists(outputPath)) {
            if (fs.delete(outputPath, true)) {
                System.out.println("Output path deleted successfully!");
            } else {
                System.out.println("Output path deletion failed. Exiting.");
                System.exit(-1);
            }
        }

        FileInputFormat.addInputPath(job, hdfsFilePath);
        FileOutputFormat.setOutputPath(job, outputPath);

        // Run the job and wait for completion
        long startTime = System.currentTimeMillis();  // Start timer
        boolean jobCompleted = job.waitForCompletion(true);
        long endTime = System.currentTimeMillis();  // End timer

        if (!jobCompleted) {
            System.exit(1);
        }

        System.out.println("MapReduce Job Completed. Time taken: " + (endTime - startTime) + " ms");
        System.out.println("Reading Output...");
        return formatReturn();
    }

    public Map<String, Integer> formatReturn() throws IOException {
        Path outputFile = new Path(output + "/part-r-00000");
        System.out.println(output);
        if (!fs.exists(outputFile)) {
            System.err.println("Output file not found!");
            System.exit(-1);
        }
        FSDataInputStream inputStream = fs.open(outputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        Map<String, Integer> statistics = new HashMap<>();
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
