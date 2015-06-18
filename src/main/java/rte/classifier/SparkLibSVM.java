package rte.classifier;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class SparkLibSVM<L, F> implements SAEClassifier<L, F> {

    private static final Logger LOG = LoggerFactory.getLogger(SparkLibSVM.class);

    static SparkContext sc;
    static int numIterations = 0;
    static double threshold = 0;

    @Override
    public void init() {

        SparkConf conf = new SparkConf().setAppName("SVM Classifier")
                .setMaster("local[4]");
        sc = new SparkContext(conf);
    }

    @Override
    public void setConf(HashMap<String, Object> contextMap) {
        numIterations = (int) contextMap.get(ITERATION);
        threshold = (double) contextMap.get(THRESHOLD);
    }

    @Override
    public void saveModel(String modelpath, SVMModel svmModel) {
        svmModel.save(sc, modelpath);
    }

    @Override
    public Object loadModel(String modelpath) {

        return SVMModel.load(sc, modelpath);
    }

    @Override
    public void saveFeature(String path, HashMap<Integer, String> map) {

        LOG.info("Serialized feature map ...");
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<Integer, String> loadFeature(String path) {

        LOG.info("Deserialized feature map ...");
        HashMap<Integer, String> map = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Object trainModel(String trainpath) {

        SVMModel svmModel = null;
        JavaRDD<LabeledPoint> training = MLUtils.loadLabeledData(sc, trainpath).toJavaRDD().cache();
        if (threshold == Double.MIN_VALUE || threshold == Double.MAX_VALUE)
            svmModel = SVMWithSGD.train(training.rdd(), numIterations).clearThreshold();
        else
            svmModel = SVMWithSGD.train(training.rdd(), numIterations).setThreshold(threshold);
        return svmModel;
    }

    public static double predict(SVMModel svmModel, Vector example) {

        if (svmModel == null)
            throw new IllegalStateException("SVMModel is NULL!");
        Double score = svmModel.predict(example);
        return score;
    }

    public SparkContext getSparkContext() {

        return sc;
    }
}
