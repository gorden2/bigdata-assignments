/*
 * Cloud9: A Hadoop toolkit for working with big data
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.umd.gorden2;

import java.io.IOException;
import java.util.*;
import java.io.*;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils; 
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tl.lin.data.pair.PairOfStrings;

/**
 * <p>
 * Implementation of the "pairs" algorithm for computing co-occurrence matrices from a large text
 * collection. This algorithm is described in Chapter 3 of "Data-Intensive Text Processing with 
 * MapReduce" by Lin &amp; Dyer, as well as the following paper:
 * </p>
 *
 * <blockquote>Jimmy Lin. <b>Scalable Language Processing Algorithms for the Masses: A Case Study in
 * Computing Word Co-occurrence Matrices with MapReduce.</b> <i>Proceedings of the 2008 Conference
 * on Empirical Methods in Natural Language Processing (EMNLP 2008)</i>, pages 419-428.</blockquote>
 *
 * @author Jimmy Lin
 */
public class PairsPMI extends Configured implements Tool {
  private static final Logger LOG = Logger.getLogger(PairsPMI.class);

  private static class MyMapper extends Mapper<LongWritable, Text, PairOfStrings, DoubleWritable> {
    private static final PairOfStrings PAIR = new PairOfStrings();
    private static final DoubleWritable ONE = new DoubleWritable(1.0d);

    @Override
    public void map(LongWritable key, Text line, Context context)
        throws IOException, InterruptedException {
      String text = line.toString();
      String[] s = text.trim().split("\\s+");

	  
	  // eliminate the dup words
	  Set<String> ss = new HashSet<String>(Arrays.asList(s));
	  String[] terms = ss.toArray(new String[ss.size()]);
	  
      for (int i = 0; i < terms.length; i++) {
        String term = terms[i];

        // skip empty tokens
        if (term.length() == 0)
          continue;
		
		for (int j=0; j < terms.length; j++){
			  if (i!=j){
				  PAIR.set(terms[i], terms[j]);
				  context.write(PAIR, ONE);
				  }
			  }
        
        }
      }
    }
    
  // WordCount Mapper
  private static class MyMapper2 extends Mapper<LongWritable, Text, Text, IntWritable> {

    // Reuse objects to save overhead of object creation.
    private final static IntWritable ONE = new IntWritable(1);
    private final static Text WORD = new Text();

    @Override
    public void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
      String line = ((Text) value).toString();
      String[] s = line.trim().split("\\s+");
      
      // eliminate the dup words
	  Set<String> ss = new HashSet<String>(Arrays.asList(s));
	  String[] terms = ss.toArray(new String[ss.size()]);
	  
      for (int i =0; i < terms.length; i++){
	   String term = terms[i];
	   if (term.length() == 0)
				continue;
        WORD.set(term);
        context.write(WORD, ONE);
      }
    }
  }

  // WordCount Reducer
  private static class MyReducer2 extends Reducer<Text, IntWritable, Text, IntWritable> {

    // Reuse objects.
    private final static IntWritable SUM = new IntWritable();

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      // Sum up values.
      Iterator<IntWritable> iter = values.iterator();
      int sum = 0;
      while (iter.hasNext()) {
        sum += iter.next().get();
      }
      SUM.set(sum);
      context.write(key, SUM);
    }
  }
  
  // Pair Combiner
    private static class MyCombiner extends
      Reducer<PairOfStrings, DoubleWritable, PairOfStrings, DoubleWritable> {
	  private final static DoubleWritable SUM = new DoubleWritable();
	  
    @Override
    public void reduce(PairOfStrings key, Iterable<DoubleWritable> values, Context context)
        throws IOException, InterruptedException {
      Iterator<DoubleWritable> iter = values.iterator();
      double sum = 0.0d;
      while (iter.hasNext()) {
        sum += iter.next().get();
      }
      SUM.set(sum);
      context.write(key,SUM);

    }
  }
  
  // Pair Reducer
  private static class MyReducer extends
      Reducer<PairOfStrings, DoubleWritable, PairOfStrings, DoubleWritable> {
    private final static DoubleWritable PMI = new DoubleWritable();
	HashMap<String, Double> sidemap = new HashMap<String, Double>();
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		if (context.getCacheFiles() != null && context.getCacheFiles().length > 0) {
			URI mappingFileUri = context.getCacheFiles()[0];
			if (mappingFileUri != null) {
				String side_file = FileUtils.readFileToString(new File("./part-r-00000"));
				String[] token = side_file.split("\\s+");
				
				for (int i=0; i<=token.length-2;i+=2){
						sidemap.put(token[i], Double.parseDouble(token[i+1]));
						}
				} 
			else {System.out.println(">>>>>> NO MAPPING FILE");}
			} 
		else {System.out.println(">>>>>> NO CACHE FILES AT ALL");}
	}
	
    @Override
    public void reduce(PairOfStrings key, Iterable<DoubleWritable> values, Context context)
        throws IOException, InterruptedException {
      Iterator<DoubleWritable> iter = values.iterator();
      double sum = 0.0d;
      while (iter.hasNext()) {
        sum += iter.next().get();
      }
      
	  Double pmix = sidemap.get(key.getLeftElement());
	  Double pmiy = sidemap.get(key.getRightElement());
	  Double pmixy = sum;
	  if (pmixy>=10){
			Double pmi = pmixy / (pmix * pmiy);
			Double logpmi = Math.log10(pmi);
			PMI.set(logpmi);
			context.write(key, PMI);
		}
    }
  }

  // Currently useless
  protected static class MyPartitioner extends Partitioner<PairOfStrings, DoubleWritable> {
    @Override
    public int getPartition(PairOfStrings key, DoubleWritable value, int numReduceTasks) {
      return (key.getLeftElement().hashCode() & Integer.MAX_VALUE) % numReduceTasks;
    }
  }

  /**
   * Creates an instance of this tool.
   */
  public PairsPMI() {}

  private static final String INPUT = "input";
  private static final String OUTPUT = "output";
  private static final String NUM_REDUCERS = "numReducers";

  /**
   * Runs this tool.
   */
  @SuppressWarnings({ "static-access" })
  public int run(String[] args) throws Exception {
    Options options = new Options();

    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("input path").create(INPUT));
    options.addOption(OptionBuilder.withArgName("path").hasArg()
        .withDescription("output path").create(OUTPUT));
    options.addOption(OptionBuilder.withArgName("num").hasArg()
        .withDescription("number of reducers").create(NUM_REDUCERS));

    CommandLine cmdline;
    CommandLineParser parser = new GnuParser();

    try {
      cmdline = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("Error parsing command line: " + exp.getMessage());
      return -1;
    }

    if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
      System.out.println("args: " + Arrays.toString(args));
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(120);
      formatter.printHelp(this.getClass().getName(), options);
      ToolRunner.printGenericCommandUsage(System.out);
      return -1;
    }

    String inputPath = cmdline.getOptionValue(INPUT);
    String outputPath = cmdline.getOptionValue(OUTPUT);
    int reduceTasks = cmdline.hasOption(NUM_REDUCERS) ?
        Integer.parseInt(cmdline.getOptionValue(NUM_REDUCERS)) : 1;


    LOG.info("Tool: " + PairsPMI.class.getSimpleName());
    LOG.info(" - input path: " + inputPath);
    LOG.info(" - output path: " + outputPath);
    LOG.info(" - number of reducers: " + reduceTasks);
	
	//
	// Pair job
    Job job = Job.getInstance(getConf());
    job.setJobName(PairsPMI.class.getSimpleName());
    job.setJarByClass(PairsPMI.class);

    // Delete the output directory if it exists already.
    Path outputDir = new Path(outputPath);
    FileSystem.get(getConf()).delete(outputDir, true);

    job.setNumReduceTasks(reduceTasks);

    FileInputFormat.setInputPaths(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    job.setMapOutputKeyClass(PairOfStrings.class);
    job.setMapOutputValueClass(DoubleWritable.class);
    job.setOutputKeyClass(PairOfStrings.class);
    job.setOutputValueClass(DoubleWritable.class);

    job.setMapperClass(MyMapper.class);
    job.setCombinerClass(MyCombiner.class);
    job.setReducerClass(MyReducer.class);
    //job.setPartitionerClass(MyPartitioner.class);
	
	job.addCacheFile(new URI("wc/part-r-00000"));
	
    long startTime = System.currentTimeMillis();
    
    //
    // wordcount job
    Job job2 = Job.getInstance(getConf());
    job2.setJobName("Wordcount");
    job2.setJarByClass(PairsPMI.class);
	String outputPath2 = "wc";
	
    // Delete the output directory if it exists already.
    Path outputDir2 = new Path(outputPath2);
    FileSystem.get(getConf()).delete(outputDir2, true);

    job2.setNumReduceTasks(1);

    FileInputFormat.setInputPaths(job2, new Path(inputPath));
    FileOutputFormat.setOutputPath(job2, new Path(outputPath2));

    job2.setMapOutputKeyClass(Text.class);
    job2.setMapOutputValueClass(IntWritable.class);
    job2.setOutputKeyClass(Text.class);
    job2.setOutputValueClass(IntWritable.class);

    job2.setMapperClass(MyMapper2.class);
    job2.setCombinerClass(MyReducer2.class);
    job2.setReducerClass(MyReducer2.class);

	// add side file to job1
	job.addCacheFile(new URI("wc/part-r-00000"));
    
    job2.waitForCompletion(true);
    job.waitForCompletion(true);
    System.out.println("Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");

    return 0;
  }

  /**
   * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
   */
  public static void main(String[] args) throws Exception {
    ToolRunner.run(new PairsPMI(), args);
  }
}
