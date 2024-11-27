package org.example.java_project.Service;

import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataInputStream;
import org.example.java_project.Util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.time.LocalDate;


public class OldTop3job extends Task<XYChart.Series<String, Number>> {

    private String output;
    private String JobName;
    private static  FileSystem fs ;
    private static ArrayList<Pair>  IssusCount   = new ArrayList<>();
    public static int  some  = 0;


    public OldTop3job(String jobName  , LocalDate date){
        this.JobName = jobName;
        this.output = "/test/output_"+date.toString()+"_"+JobName;
    }

    @Override
    public XYChart.Series<String, Number> call() throws IOException {
        fs = hadoopConf.getFileSystem();
        boolean outputExist = fs.exists(new Path(output));

        if(outputExist) {
            return FormatReturn();
        }else{
            throw new IOException();
        }

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
}
