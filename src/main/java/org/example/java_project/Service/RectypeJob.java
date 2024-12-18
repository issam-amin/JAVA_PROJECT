package org.example.java_project.Service;


import javafx.concurrent.Task;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RectypeJob extends Task<HashMap<String, Integer>> {
    private final String input;
    private final String output;
    private static String jobName;
    static FileSystem fs ;
    private final String currentDate;
    private LocalTime jobTime;
    private  JobType JobType;


    public RectypeJob(String jobName, JobType JobType) {
        this.JobType = JobType;
        this.jobName = jobName;
        this.jobTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.currentDate = LocalDate.now().format(formatter);
        this.input = "/test/input/dataJob.csv";
        this.output = "/test/output_" +currentDate+"_"+ jobName;
    }

    public HashMap<String, Integer> call() throws IOException {
        fs = hadoopConf.getFileSystem();
        boolean output_day = false;

        output_day = fs.exists(new Path(output));

        switch (JobType) {
            case NORMAL: {
                try {
                    if (output_day) {
                        System.out.println("kayn output");
                        return FormatReturn();

                    }else
                    {
                        return RunJob("src/main/resources/dataJob.csv", output, input, jobName);

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case REFRESH: {
                try {
                    System.out.println("issaa");
                    DbConnection.export("rec");
                    System.out.println("issaa2");

                    return RunJob("src/main/resources/dataJob.csv", output, input, jobName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return null;
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

    protected  HashMap<String, Integer> RunJob(String localFilePath, String outputPathStr, String hdfsFilePathStr,String JobName) throws Exception {
        Path outputPath = new Path(outputPathStr);
        Path hdfsFilePath = new Path(hdfsFilePathStr);
        Path localPath = new Path(localFilePath);

        Job job = hadoopConf.getJob(JobName);
        fs = hadoopConf.getFileSystem();


        job.setJarByClass(clientTypeJob.class);
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
        return FormatReturn();

    }
    private   HashMap<String, Integer> FormatReturn() throws IOException {
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






    public static List<String> getArchivesFromHDFS() throws IOException {
        List<String> archiveNames = new ArrayList<>();


       fs = hadoopConf.getFileSystem();


        Path archivesPath = new Path("/test");


        FileStatus[] fileStatuses = fs.listStatus(archivesPath);

     Pattern pattern = Pattern.compile("^output_\\d{4}-\\d{2}-\\d{2}_chart\\d+$");

      //  Pattern pattern = Pattern.compile("^output_\\d{4}-\\d{2}-\\d{2}_" + jobName + "\\d+$");

        if (fileStatuses == null || fileStatuses.length == 0) {
            System.out.println("No archives found in the specified directory.");
        } else {
            // Iterate through the files and add their names to the list
            for (FileStatus fileStatus : fileStatuses) {
                if (fileStatus.isDirectory()) {  // Only consider directories
                    String dirName = fileStatus.getPath().getName();

                    // Check if the directory name matches the regex pattern
                    Matcher matcher = pattern.matcher(dirName);
                    if (matcher.matches()) {
                        archiveNames.add(dirName);
                    }
                }
            }
        }

        fs.close();
        return archiveNames;
    }







}
