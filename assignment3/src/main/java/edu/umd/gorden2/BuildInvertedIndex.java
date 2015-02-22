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
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tl.lin.data.array.ArrayListWritable;
import tl.lin.data.fd.Object2IntFrequencyDistribution;
import tl.lin.data.fd.Object2IntFrequencyDistributionEntry;
import tl.lin.data.pair.PairOfInts;
import tl.lin.data.pair.PairOfObjectInt;
import tl.lin.data.pair.PairOfWritables;
import tl.lin.data.pair.PairOfStringInt;

public class BuildInvertedIndex extends Configured implements Tool {
  private static final Logger LOG = Logger.getLogger(BuildInvertedIndex.class);

  private static class MyMapper extends Mapper<LongWritable, Text, PairOfStringInt, IntWritable> {
	private static final PairOfStringInt PAIR = new PairOfStringInt();
    private static final HashMap<String,Integer> MAP = new HashMap<String,Integer>();
	private static final IntWritable COUNT= new IntWritable();
	
	
    @Override
    public void map(LongWritable docno, Text doc, Context context)
        throws IOException, InterruptedException {
      String text = doc.toString();
      MAP.clear();
      String[] terms = text.split("\\s+");
      
      for (String term:terms){
		  if (term == null || term.length() == 0) {continue;}
		  if (MAP.containsKey(term)) {MAP.put(term, MAP.get(term)+1);}
		  else {MAP.put(term,1);}
		  }

	  Iterator<String> iterkeys = MAP.keySet().iterator();
	  while (iterkeys.hasNext()){
		  String iter = iterkeys.next();
		  PAIR.set(iter, (int)docno.get());
		  COUNT.set(MAP.get(iter));
		  context.write(PAIR,COUNT);
		  }

    }
  }

  private static class MyReducer extends
      Reducer<PairOfStringInt, IntWritable, Text, PairOfWritables<IntWritable, ArrayListWritable<PairOfInts>>> {
    private final static IntWritable DF = new IntWritable();
	private final static Text TERM = new Text();
	ArrayListWritable<PairOfInts> postings = new ArrayListWritable<PairOfInts>();
	String pre = "";
	
    @Override
    public void reduce(PairOfStringInt key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      Iterator<IntWritable> iter = values.iterator();
      PairOfInts post = new PairOfInts();
      
	  int sum = 0;
      while (iter.hasNext()) {
        sum += iter.next().get();
      }
	  System.out.println("initial:");
	  System.out.println(pre);
	  
	  if (!key.getLeftElement().equals(pre) && pre!=""){
		  System.out.println("new:");
		  TERM.set(pre); 
		  DF.set(postings.size());
		  context.write(TERM, new PairOfWritables<IntWritable, ArrayListWritable<PairOfInts>>(DF, postings));
		  postings.clear();
		  }
	  post.set(key.getRightElement(),sum);
	  postings.add(post);
	  pre = key.getLeftElement();
	  System.out.println("pre:");
	  System.out.println(pre);
    }
	
  }


  // basic partitioner
  protected static class MyPartitioner extends Partitioner<PairOfStringInt, IntWritable> {
    @Override
    public int getPartition(PairOfStringInt key, IntWritable value, int numReduceTasks) {
      return (key.getLeftElement().hashCode() & Integer.MAX_VALUE) % numReduceTasks;
    }
  }



  private BuildInvertedIndex() {}

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

    LOG.info("Tool name: " + BuildInvertedIndex.class.getSimpleName());
    LOG.info(" - input path: " + inputPath);
    LOG.info(" - output path: " + outputPath);
    LOG.info(" - num reducers: " + reduceTasks);

    Job job = Job.getInstance(getConf());
    job.setJobName(BuildInvertedIndex.class.getSimpleName());
    job.setJarByClass(BuildInvertedIndex.class);

    job.setNumReduceTasks(reduceTasks);

    FileInputFormat.setInputPaths(job, new Path(inputPath));
    FileOutputFormat.setOutputPath(job, new Path(outputPath));

    job.setMapOutputKeyClass(PairOfStringInt.class);
    job.setMapOutputValueClass(IntWritable.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(PairOfWritables.class);
    //job.setOutputFormatClass(MapFileOutputFormat.class);

    job.setMapperClass(MyMapper.class);
    job.setReducerClass(MyReducer.class);
	job.setPartitionerClass(MyPartitioner.class);
	
    // Delete the output directory if it exists already.
    Path outputDir = new Path(outputPath);
    FileSystem.get(getConf()).delete(outputDir, true);

    long startTime = System.currentTimeMillis();
    job.waitForCompletion(true);
    System.out.println("Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");

    return 0;
  }

  /**
   * Dispatches command-line arguments to the tool via the {@code ToolRunner}.
   */
  public static void main(String[] args) throws Exception {
    ToolRunner.run(new BuildInvertedIndex(), args);
  }
}
