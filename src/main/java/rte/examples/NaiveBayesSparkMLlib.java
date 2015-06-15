package rte.examples;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import scala.Tuple2;

/**
 * Created by qingqingcai on 6/15/15.
 */
public class NaiveBayesSparkMLlib {

    public static void main(String[] args) {

        String trainpath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.spark.txt";
        String testpath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.spark.txt";
        SparkConf conf = new SparkConf().setAppName("Naive Bayes Classifier")
                .setMaster("local[4]");
        SparkContext sc = new SparkContext(conf);

        JavaRDD<LabeledPoint> data = MLUtils.loadLabeledData(sc, trainpath).toJavaRDD().cache();
        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] {0.6, 0.4}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> test = splits[1].cache();

        final NaiveBayesModel nbModel = NaiveBayes.train(training.rdd(), 1.0);

        JavaPairRDD<Double, Double> predictionAndLabel = test.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
            @Override public Tuple2<Double, Double> call(LabeledPoint p) {
                return new Tuple2<Double, Double>(nbModel.predict(p.features()), p.label());
            }
        });

        double accuracy = predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            @Override public Boolean call(Tuple2<Double, Double> pl) {
                return pl._1().equals(pl._2());
            }
        }).count() / (double) test.count();

        predictionAndLabel.collect().forEach(tuple2 -> {
            System.out.println(tuple2._2() + "\t" + tuple2._1());
        });

        System.out.println("accuracy = " + accuracy);
    }
}
