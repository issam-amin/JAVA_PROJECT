package org.example.java_project.Service;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
/*import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;*/
import org.apache.hadoop.mapreduce.Job;
/*import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;*/

/*import java.io.BufferedReader;*/
import java.io.IOException;
/*import java.io.InputStreamReader;
import java.util.*;*/

public class hadoopConf {

    public static Job getJob( String jobName) {
        try {
            return Job.getInstance(getConfiguration(), "wordcount");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static FileSystem getFileSystem() {
        try {
            return FileSystem.get(getConfiguration());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private Configuration getConfiguration() {
        final Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000/");
        return conf;
    }

}
