package bpn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


/**
 * Created by qingqingcai on 4/30/15.
 */
public class SparkKMeans {

    private static class ParsePoint implements Function<String, Vector> {
        private static final Pattern SPACE = Pattern.compile(" ");

        @Override
        public Vector call(String line) {
            String[] tok = SPACE.split(line);
            double[] point = new double[tok.length];
            for (int i = 0; i < tok.length; ++i) {
                point[i] = Double.parseDouble(tok[i]);
            }
            return Vectors.dense(point);
        }
    }

    /** **************************************************************
     * Start KMeans
     * @param inputFile One feature vector perline
     * @param k Number of clusters
     * @param iterations Number of iterations
     * @param runs ???
     * @return A hashmap where key is InstanceID, and value is CluserID
     */
    public static void run(String inputFile, int k, int iterations, int runs,
                           HashMap<Integer, Integer> InstanceID_ClusterID,
                           HashMap<Integer, ArrayList<Integer>> ClusterID_InstanceIDList) {

        if (runs == -1)
            runs = 1;

        SparkConf sparkConf = new SparkConf().setAppName("JavaKMeans").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = sc.textFile(inputFile);

        JavaRDD<Vector> points = lines.map(new ParsePoint());

        KMeansModel model = org.apache.spark.mllib.clustering.KMeans.train(points.rdd(), k, iterations, runs, org.apache.spark.mllib.clustering.KMeans.K_MEANS_PARALLEL());

        int i = 0;
        for(Vector v : points.collect()) {

            int clusterID = model.predict(v);
            InstanceID_ClusterID.put(i, clusterID);

            ArrayList<Integer> InstanceIDList = new ArrayList<>();
            if (ClusterID_InstanceIDList.containsKey(clusterID)) {
                InstanceIDList = ClusterID_InstanceIDList.get(clusterID);
            }
            InstanceIDList.add(i);
            ClusterID_InstanceIDList.put(clusterID, InstanceIDList);

            i++;
        }

        sc.stop();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println(
                    "Usage: JavaKMeans <input_file> <k> <max_iterations> [<runs>]");
        //    System.exit(1);
            args = new String[3];
            args[0] = "data/bpn/clustering/feature.txt";
            args[1] = "3";
            args[2] = "20";
        }
        String inputFile = args[0];
        int k = Integer.parseInt(args[1]);
        int iterations = Integer.parseInt(args[2]);
        int runs = 1;

        if (args.length >= 4) {
            runs = Integer.parseInt(args[3]);
        }
        SparkConf sparkConf = new SparkConf().setAppName("JavaKMeans").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = sc.textFile(inputFile);

        JavaRDD<Vector> points = lines.map(new ParsePoint());

        KMeansModel model = org.apache.spark.mllib.clustering.KMeans.train(points.rdd(), k, iterations, runs, org.apache.spark.mllib.clustering.KMeans.K_MEANS_PARALLEL());

        System.out.println("Cluster centers:");
        for (Vector center : model.clusterCenters()) {
            System.out.println(" " + center);
        }
        double cost = model.computeCost(points.rdd());
        System.out.println("Cost: " + cost);

        for(Vector v : points.collect()) {
            System.out.println("debug = " + model.predict(v));
        }

        sc.stop();
    }
}
