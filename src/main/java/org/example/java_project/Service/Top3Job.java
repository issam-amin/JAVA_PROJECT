package org.example.java_project.Service;

import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FSDataInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.time.LocalDate;
import org.example.java_project.Util.Pair;
public class Top3Job extends Task<XYChart.Series<String, Number>>  {
    private String input ;
    private String output;
    private String JobName;
    private JobType jobType;
    private static ArrayList<Pair>  IssusCount   = new ArrayList<>();
    public static int  some  = 0;
    private static  FileSystem fs ;



    public Top3Job(String JobName , JobType jobType ){
        this.JobName = JobName;
        LocalDate currentDate = LocalDate.now();
        this.input = "/test/input/data.csv";
        this.output = "/test/output_"+currentDate.toString()+"_"+JobName;
        this.jobType = jobType;
    }

    @Override
    public XYChart.Series<String, Number> call() throws IOException {
        fs = hadoopConf.getFileSystem();
        boolean tuday_output = false;
        try {
             tuday_output = fs.exists(new Path(output));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        switch (jobType) {
            case NORMAL: {
                try {
                     if(tuday_output){
                        return FormatReturn();
                     }else {
                        return RunJob("src/main/resources/data.csv");
                     }
                } catch (Exception e) {
                    throw new IOException("Error while reading data from " + output);
                }
            }
            case REFRESH: {
                try {
                    DbConnection.export("rec");
                    return RunJob("src/main/resources/data.csv");
                } catch (Exception e) {
                    throw new IOException("error while refreshing");
                }
            }
    }
        return null;
    }

    protected  XYChart.Series<String, Number> RunJob(String localFilePath) throws Exception {

        /*String inputPath = args[0];*/
        Path outputPath = new Path(output);
        Path hdfsFilePath = new Path(input);
        Path localPath = new Path(localFilePath);

        Job job = hadoopConf.getJob(JobName);


        job.setJarByClass(Top3Job.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(SumCombiner.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);


        // Check if the HDFS path already exists
        if (fs.exists(hdfsFilePath)) {
            fs.delete(hdfsFilePath, true);
        }

        // Copy the file from local file system to HDFS
        fs.copyFromLocalFile(localPath, hdfsFilePath);
        System.out.println("File uploaded to HDFS: " + hdfsFilePath);

        if (fs.exists(outputPath)) {
            if (fs.delete(outputPath, true)) {
                System.out.println("File deleted successfully!");
            } else {
                System.out.println("File deletion failed. File may not exist.");
                System.exit(-1);
            }
        }
        FileInputFormat.addInputPath(job, hdfsFilePath);
        FileOutputFormat.setOutputPath(job, outputPath);


        boolean jobCompleted = job.waitForCompletion(true);
        if (!jobCompleted) {
            System.exit(1);
        }

        System.out.println("MapReduce Job Completed. Reading Output...");

        return FormatReturn();
    }


    public XYChart.Series<String, Number> FormatReturn() throws IOException {
        Path outputFile  = new Path(output+ "/part-r-00000");
        FSDataInputStream inputStream = fs.open(outputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        XYChart.Series<String, Number> updatedSeries = new XYChart.Series<>();
        ArrayList <XYChart.Data<String,Number>>  list  = new ArrayList<>();
        IssusCount.clear();
        some = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            some += Integer.parseInt(tokens[1]);
            System.out.println(line);
            list.add(new XYChart.Data<String,Number>(tokens[0], Integer.parseInt(tokens[1])));
            IssusCount.add(new Pair(tokens[0] , Integer.parseInt(tokens[1])));
        }
        reader.close();
        inputStream.close();

        System.out.println((list.get(0).getYValue().intValue()*1.0/some) * 100);
        list.get(0).setYValue((list.get(0).getYValue().intValue()*1.0/some) * 100);
        list.get(1).setYValue((list.get(1).getYValue().intValue()*1.0/some) * 100);
        list.get(2).setYValue((list.get(2).getYValue().intValue()*1.0/some) * 100);
        updatedSeries.getData().add(list.get(0));
        updatedSeries.getData().add(list.get(1));
        updatedSeries.getData().add(list.get(2));
        return updatedSeries;

    }

    public  static ArrayList<Pair> getlistComplaints(){
        return  IssusCount;
    }


    public  static   class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final  IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(","); // Split by whitespace

            word.set(tokens[2]);
            context.write(word, one);
        }
    }

    public static class SumCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);  // Do not modify the key in the combiner
        }
    }

    public  static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        private List<Pair> keyValueList = new ArrayList<>();

        // Custom Pair class for storing key-value pairs

        /*private HashMap<Text, IntWritable> CountMap = new HashMap<>();*/

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
             result.set(sum);

            /*CountMap.put(new Text(key), new IntWritable(sum));*/
            /*     Text customKey = new Text("movie with id " + key.toString()+ " hase been whatched : ");*/

            keyValueList.add(new Pair(key.toString(), sum));
        }


        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {
            // Sort the list by value in descending order
            Collections.sort(keyValueList, new Comparator<Pair>() {
                @Override
                public int compare(Pair p1, Pair p2) {
                    return Integer.compare(p2.getValue(), p1.getValue()); // Descending order
                }
            });

            for (Pair pair : keyValueList) {
                String Type = DbConnection.getType(pair.getKey());
                context.write(new Text(Type), new IntWritable(pair.getValue()));
            }
        }


    }

}
