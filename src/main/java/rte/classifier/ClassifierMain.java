package rte.classifier;

import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.HashMap;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class ClassifierMain {

    public static void main(String[] args) {
        SparkLibSVM sparkLibSVM = new SparkLibSVM();
        sparkLibSVM.init();

        String trainName = "MIT99";
        String testName = "cmuwiki";
        String trainpath = "data/rte/" + trainName + ".train.spark.txt";
        String testpath = "data/rte/" + testName + ".test.spark.txt";
        String testExcelPath = "data/rte/" + testName + ".test.xls";
        String testSheetName = testName;

        HashMap<String, Object> context = new HashMap() {
            {
                put(SAEClassifier.ITERATION, 50);
                put(SAEClassifier.THRESHOLD, Double.MAX_VALUE);
            }
        };
        sparkLibSVM.setConf(context);
        SVMModel svmModel = (SVMModel) sparkLibSVM.trainModel(trainpath);
        // feature vector, no label included
        double[] features = {0, 5.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Vector featureVector = Vectors.dense(features);
        double score = sparkLibSVM.predict(svmModel, featureVector);
        System.out.println("score = " + score);
    }
}
