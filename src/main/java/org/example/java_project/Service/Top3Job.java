package org.example.java_project.Service;

import io.netty.handler.codec.string.LineSeparator;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import org.apache.hadoop.conf.Configuration;
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
import java.util.ArrayList;
import java.util.Comparator;

public class Top3Job extends Task<XYChart.Series<String, Number>> {
    private String input ;
    private String output;
    private String JobName;

    public Top3Job(String JobName ){
        this.JobName = JobName;
        this.input = "/test/input_"+JobName+"/data.csv";
        this.output = "/test/output_"+JobName;
    }

    @Override
    public XYChart.Series<String, Number> call() {
        /*boolean exportRes = DbConnection.export("rec");*/

       if(true) {
        try {
           return RunJob("src/main/resources/data.csv",output,input ,JobName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
       }else {
           System.out.println("sorry !!!!");
       }
        return null;
    }

    protected  XYChart.Series<String, Number> RunJob(String localFilePath, String outputPathStr, String hdfsFilePathStr ,String JobName) throws Exception {

        /*String inputPath = args[0];*/
        Path outputPath = new Path(outputPathStr);
        Path hdfsFilePath = new Path(hdfsFilePathStr);
        Path localPath = new Path(localFilePath);

        Job job = hadoopConf.getJob(JobName);
        FileSystem fs = hadoopConf.getFileSystem();


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

        // Read and print the output
        System.out.println("MapReduce Job Completed. Reading Output...");
        Path outputFile = new Path(outputPathStr + "/part-r-00000"); // Default output file

        // Check if the file exists
        if (!fs.exists(outputFile)) {
            System.err.println("Output file not found!");
            System.exit(-1);
        }

        // Read the output file content
        FSDataInputStream inputStream = fs.open(outputFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        XYChart.Series<String, Number> updatedSeries = new XYChart.Series<>();
        ArrayList <XYChart.Data<String,Number>>  list  = new ArrayList<>();
        int some = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            System.out.println(line);
            some += Integer.parseInt(tokens[1]);
            list.add(new XYChart.Data<String,Number>(DbConnection.getType(tokens[0]), Integer.parseInt(tokens[1])));

        }
        reader.close();
        inputStream.close();
        list.sort(new Comparator<XYChart.Data<String,Number>>() {
            @Override
            public int compare(XYChart.Data<String, Number> o1, XYChart.Data<String, Number> o2) {
                return Integer.compare(o2.getYValue().intValue(),o1.getYValue().intValue());
            }
        });
        System.out.println((list.get(0).getYValue().intValue()*1.0/some) * 100);
        list.get(0).setYValue((list.get(0).getYValue().intValue()*1.0/some) * 100);
        list.get(1).setYValue((list.get(1).getYValue().intValue()*1.0/some) * 100);
        list.get(2).setYValue((list.get(2).getYValue().intValue()*1.0/some) * 100);
        updatedSeries.getData().add(list.get(0));
        updatedSeries.getData().add(list.get(1));
        updatedSeries.getData().add(list.get(2));
        return updatedSeries;
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
        /*private HashMap<Text, IntWritable> CountMap = new HashMap<>();*/

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
             result.set(sum);

            /*CountMap.put(new Text(key), new IntWritable(sum));*/
            /*     Text customKey = new Text("movie with id " + key.toString()+ " hase been whatched : ");*/

             context.write(key, result);
        }
    }

}
