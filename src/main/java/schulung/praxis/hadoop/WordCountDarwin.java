package schulung.praxis.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class WordCountDarwin extends Configured implements Tool {

	// TODO: Adjust Map function to count words that include "evolution".
	public static class Map extends MapReduceBase implements
			org.apache.hadoop.mapred.Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				word.set(tokenizer.nextToken());
				if(word.find("evolution") > -1) output.collect(word, one);
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new WordCountDarwin(),
				args);
		System.exit(res);
	}

	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.printf("Usage: %s [generic options] <input_file> <output_path>\n",
					getClass().getSimpleName());
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}
		
		JobConf jConf = new JobConf(WordCountDarwin.class);
		jConf.setJobName("wordcount-darwin");

		//jConf.setJarByClass(WordCountDarwin.class);
		
		jConf.setOutputKeyClass(Text.class);
		jConf.setOutputValueClass(IntWritable.class);

		jConf.setMapperClass(Map.class);
		jConf.setCombinerClass(Reduce.class);
		jConf.setReducerClass(Reduce.class);

		jConf.setInputFormat(TextInputFormat.class);
		jConf.setOutputFormat(TextOutputFormat.class);

		if(new File(args[0]).isDirectory())
			for(File ifile : new File(args[0]).listFiles())
				FileInputFormat.addInputPath(jConf, new Path(ifile.getPath()));
		FileOutputFormat.setOutputPath(jConf, new Path(args[1]));

		
		jConf.setNumMapTasks(10);
		jConf.setNumReduceTasks(3);
		jConf.setSpeculativeExecution(true);
		
		RunningJob job = JobClient.runJob(jConf);

		
		//Chained Jobs from here ...
		
		return 0;
	}

}
