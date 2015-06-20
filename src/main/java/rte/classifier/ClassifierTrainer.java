package rte.classifier;

import org.apache.spark.mllib.classification.SVMModel;
import rte.RteMessageHandler;

import java.util.TreeMap;

/**
 * Created by qingqingcai on 6/18/15.
 */
public class ClassifierTrainer extends RteMessageHandler {

    public static void runClassifierTrainer(String[] args, TreeMap<Integer, String> fitofn) {

        String trainName, modelPath, feaPath;
        if (args == null || args.length != 3) {
            trainName = "MIT99";
            modelPath = "data/rte/SVMSPARK.model";
            feaPath = "data/rte/FITOFN.ser";
        } else {
            trainName = args[0].isEmpty() ? "MIT99" : args[0];
            modelPath = args[1].isEmpty() ? "data/rte/SVMSPARK.model" : args[1];
            feaPath = args[2].isEmpty() ? "data/rte/FITOFN.ser" : args[2];
        }
        TreeMap<Integer, String> FITOFN = new TreeMap<>();
        FITOFN.putAll(fitofn);

        // Classifier trainer; and FITOFN and classifier serialization
        String trainPath = "data/rte/" + trainName + ".train.spark.txt";
        SVMModel svmModel = (SVMModel) sparkLibSVM.trainModel(trainPath);
        sparkLibSVM.saveModel(modelPath, svmModel);
        sparkLibSVM.saveFeature(feaPath, FITOFN);
    }
}
