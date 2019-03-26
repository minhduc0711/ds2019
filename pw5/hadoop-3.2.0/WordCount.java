import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,NullWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, NullWritable.get());
        }
    }


    //    return set of String file header in folder
    public static List<String> listFilesForFolder(File folder) {
        List<String> files = new ArrayList<String>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getName());
            }
        }
        return  files;
    }
    //    Reading file
    public static List<String> ReadFile(File file){
        List<String> letter = new ArrayList<String>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String st;
            while((st = br.readLine())!=null){
                letter.add(st);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return letter;
    }
    //    Read file with "/" separator
    public static List<String> ReadFileInput(File file){
        List<String> letter = new ArrayList<String>();
        String delStr = "/";
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String st;
            while((st = br.readLine())!=null){
                st = st.replace(delStr," ");
                letter.add(st);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return letter;
    }
    //      Creating array of String to contain word in path
    public static String[] SubstringArray(String string){
        return string.split(" ");
    }

    //      Length of path to implement in find function
    public static Integer length(String path,List<String> check){
        String[] words = SubstringArray(path);
        Integer sum = 0;
        for (String word : words) {
            if(check.contains(word)){
                sum += 1;
            }
        }
        return sum;
    }
    //    Find the longest path
    public static String find(List<String> paths, List<String> letter){
        String longest_path = null;
        Integer max = 0;
        for(String path: paths ){
            Integer length = length(path,letter);
            if (length > max) {
                max = length;
                longest_path = path;
            }
        }
        return longest_path;
    }
    //  Append String to new file
    public static void appendStrtoFile(List<String> text, File Filename){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(Filename,true);
            BufferedWriter out =  new BufferedWriter(fileWriter);
            for(String texting : text) {
                out.write(texting + System.lineSeparator());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    Move all path to new file
    public static void MovePath(String root, List<String> files,File Transfer){
        ArrayList<List<String>> path = new ArrayList<>();
        for(int i = 0; i < files.size(); i++) {
            File file = new File(root + "/" + files.get(i));
            appendStrtoFile(ReadFileInput(file),Transfer);
        }
    }

    public static void main(String[] args) throws Exception {
        Path input = new Path("paths");
        Path output = new Path(args[1]);
//        Access input output file
        File input_folder = new File(args[0]);
//        Create a file contain all path
        File path_file = new File("paths");
//        Get all files from input folder
        List<String> files = listFilesForFolder(input_folder);
//        Move all path to new file
        MovePath(args[0],files,path_file);

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);
        String words = job.waitForCompletion(true) ? "OK" : "Not Ok";
//        System.exit(job.waitForCompletion(true) ? 0 : 1);
        System.out.println(words);

        File output_folder = new File("output/part-r-00000");
//        Get reduced word from map_reduce ouput
        List<String> letter = ReadFile(output_folder);
//        Read all path in new file
        List<String> paths = ReadFile(path_file);
//        Find the longest path from the files
        String longest_path = find(paths,letter);
        System.out.println("The longest path is:" + longest_path);
    }
}