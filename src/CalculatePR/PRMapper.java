package pageRank;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.*;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.net.URI; 
import java.io.*;
import java.math.BigDecimal;

public class PRMapper extends Mapper<Text, Text, Text, InfoText> {
	
	private Text K = new Text();
	private InfoText V = new InfoText();
	private int N;
	private double alpha;
	private double dangling;
	
	protected void setup(Context context) throws IOException, InterruptedException {
        N = context.getConfiguration().getInt("N", PageRank.N);
		alpha = context.getConfiguration().getDouble("alpha", PageRank.alpha);
		dangling = context.getConfiguration().getDouble("dangling", PageRank.danglingValue);
    }
	
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}
	
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException{		
		String[] arr = value.toString().split("#"); // PR, outDegree, out1, ...
		
		double curPR = Double.parseDouble(arr[0]);
		int outDegree = Integer.parseInt(arr[1]);
		double send = alpha * curPR/outDegree;
		
		K.set(key.toString());
		V.setType(2);
		V.setValue(curPR);
		context.write(K, V);
		
		for(int i=0 ; i<outDegree ; ++i){
			K.set(arr[2+i]);
			V.setType(1);
			V.setValue(send);
			context.write(K, V);
			
			K.set(key.toString());
			V.setType(0);
			V.setInfo(arr[2+i]);
			context.write(K, V);
		}
		
		double remain = add(alpha*dangling, (1-alpha)/N);
		K.set(key.toString());
		V.setType(1);
		V.setValue(remain);
		context.write(K, V);
	}
}