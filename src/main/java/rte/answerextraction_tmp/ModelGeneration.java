package rte.answerextraction_tmp;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;

import java.util.ArrayList;

/**
 * Created by qingqingcai on 6/7/15.
 */
public class ModelGeneration {

    public static ArrayList<String> feaList = new ArrayList<>();
    public static ArrayList<Double> lableList = new ArrayList<>();


    public static void main(String[] args) {

        run();
    }

    public static void run() {
        String trainfeapath = "data/rte/MIT99.train.txt";
        String testfeapath = "data/rte/MIT99.train.txt";
        String modelpath = "data/rte/MIT99.classifier_maochen";
        runMultinomialLogisticRegression(trainfeapath, testfeapath, modelpath, false);          // run classifier_maochen
    }

    /** **************************************************************
     * Train a multinomial logistic regression model based on labeled
     * training data; Test the multinomial logistic regression model on
     * new testing data;
     * @param trainpath path to labeled training data
     * @param testpath path to unlabeled testing data
     * @param modelpath path to multinomial logistic regression model
     * @param saveModel true if you want to save the model to local file
     */
    public static void runMultinomialLogisticRegression(String trainpath, String testpath, String modelpath, boolean saveModel) {

        // Prepare configuration and data
        //
        SparkConf conf = new SparkConf().setAppName("Multinomial Logistic Regression")
                .setMaster("local[4]");
        SparkContext sc = new SparkContext(conf);
//        JavaRDD<LabeledPoint> training = MLUtils.loadLibSVMFile(sc, trainpath).toJavaRDD().cache();
//        JavaRDD<LabeledPoint> testing = MLUtils.loadLibSVMFile(sc, testpath).toJavaRDD().cache();


        // Split data into 60% for training and the rest for testing (60% for training and 40 for testing)
        JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, testpath).toJavaRDD().cache();
        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] {0.6, 0.4}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> testing = splits[1].cache();



//        // Save and load model
//        if (saveModel)
//            model.save(sc, modelpath);
//        //    LogisticRegressionModel sameModel = LogisticRegressionModel.load(sc, modelpath);
    }
}
