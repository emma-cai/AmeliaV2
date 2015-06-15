package rte.examples;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import scala.Tuple2;

/**
 * Created by qingqingcai on 6/15/15.
 */
public class SVMSparkMLlib {

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


        // (1) How to return probability/confidence instead of actual class
        //     Answer: remove thresholds by calling clearThreshold(); then treat the predictions
        //             as probability from 0..1 inclusive; (clearthreshold() returns the raw
        //             predicted scores, instead of translating it into a positive/negative class);
        int numIterations = 50;
        final SVMModel svmModel = SVMWithSGD.train(training.rdd(), numIterations).clearThreshold();

        JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint p) {
                        Double score = svmModel.predict(p.features());
                        return new Tuple2<Object, Object>(score, p.label());
                    }
                }
        );

        BinaryClassificationMetrics metrics =
                new BinaryClassificationMetrics(JavaRDD.toRDD(scoreAndLabels));
        double auROC = metrics.areaUnderROC();
        double auPR =metrics.areaUnderPR();

        // print out: labeled <- probability
        final int[] instanceIndex = {0};
        scoreAndLabels.collect().forEach(tuple2 -> {

            System.out.println("instance " + (instanceIndex[0]++) + " = " + tuple2._2() + "\t" + tuple2._1());
        });



        // print out: performance using ROC
        System.out.println("Area under ROC = " + auROC);
        System.out.println("Area under PR = " + auPR);
    }
}
