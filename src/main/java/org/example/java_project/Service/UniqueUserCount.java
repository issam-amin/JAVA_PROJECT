package org.example.java_project.Service;

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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

public class UniqueUserCount {
    private final String input;
    private final String output;
    private static String jobName;
    static FileSystem fs;
    private final String currentDate;
    private JobType jobType;

    public UniqueUserCount(String jobName, JobType jobType) {
        this.jobType = jobType;
        UniqueUserCount.jobName = jobName;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.currentDate = LocalDate.now().format(formatter);
        this.input = "/test/input/data.csv";
        this.output = "/test/output_" + currentDate + "_" + jobName;
    }

    public HashMap<String, Integer> call() throws IOException {
        fs = hadoopConf.getFileSystem();
        boolean outputExists = fs.exists(new Path(output));

        switch (jobType) {
            case NORMAL: {
                try {
                    if (outputExists) {
                        System.out.println("Output already exists.");
                        return formatReturn();
                    } else {
                        return runJob("src/main/resources/data.csv", output, input, jobName);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case REFRESH: {
                try {
                    return runJob("src/main/resources/data.csv", output, input, jobName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {
        private Text typeUserKey = new Text();
        private Text userId = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // Split the CSV line by commas
            String[] fields = value.toString().split(",");
            if (fields.length >= 3) {
                String idType = fields[2];
                String idUser = fields[1];
                // Emit (id_type, id_user) as key, id_user as value
                typeUserKey.set(idType);
                userId.set(idUser);
                context.write(typeUserKey, userId);
            }
        }
    }

    public static class UniqueUserReducer extends Reducer<Text, Text, Text, IntWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            HashSet<String> uniqueUsers = new HashSet<>();
            // Add each user ID to the HashSet
            for (Text userId : values) {
                uniqueUsers.add(userId.toString());
            }
            // Emit id_type and the size of the HashSet
            context.write(key, new IntWritable(uniqueUsers.size()));
        }
    }

    private HashMap<String, Integer> runJob(String localFilePath, String outputPathStr, String hdfsFilePathStr, String jobName) throws Exception {
        Path outputPath = new Path(outputPathStr);
        Path hdfsFilePath = new Path(hdfsFilePathStr);
        Path localPath = new Path(localFilePath);


        Job job = hadoopConf.getJob(jobName);
        fs = hadoopConf.getFileSystem();

        job.setJarByClass(UniqueUserCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(UniqueUserReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

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
        boolean jobCompleted = job.waitForCompletion(true);
        if (!jobCompleted) {
            System.exit(1);
        }

        System.out.println("MapReduce Job Completed. Reading Output...");
        return formatReturn();
    }

    private HashMap<String, Integer> formatReturn() throws IOException {
        Path outputFile = new Path(output + "/part-r-00000");

        if (!fs.exists(outputFile)) {
            System.err.println("Output file not found!");
            System.exit(-1);
        }

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

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: UniqueUserCount <input path> <output path>");
            System.exit(-1);
        }

        String inputPath = args[0];
        String outputPath = args[1];

        // Set up the Hadoop configuration and job
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "uniqueUserCount");
        job.setJarByClass(UniqueUserCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(UniqueUserReducer.class);
        job.setReducerClass(UniqueUserReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        // Wait for the job to complete and exit based on job success
        boolean jobCompleted = job.waitForCompletion(true);

        if (jobCompleted) {
            // Display the results
            FileSystem fs = FileSystem.get(conf);
            Path outputFile = new Path(outputPath + "/part-r-00000");
            FSDataInputStream inputStream = fs.open(outputFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            System.out.println("Job Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            reader.close();
            inputStream.close();
        } else {
            System.exit(1);
        }
    }
}


