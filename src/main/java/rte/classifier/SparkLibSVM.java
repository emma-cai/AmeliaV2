package rte.classifier;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.*;
import java.util.HashMap;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class SparkLibSVM<L, F> implements SAEClassifier<L, F> {

    private static final Logger LOG = LoggerFactory.getLogger(SparkLibSVM.class);

    static SparkContext sc;
    static SVMModel svmModel;
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
    public void saveModel(String modelpath) {
        svmModel.save(sc, modelpath);
    }

    @Override
    public Object loadModel(String modelpath) {
        return svmModel.load(sc, modelpath);
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

    public static void trainModel(String trainpath) {
        JavaRDD<LabeledPoint> training = MLUtils.loadLabeledData(sc, trainpath).toJavaRDD().cache();
        if (threshold == Double.MIN_VALUE || threshold == Double.MAX_VALUE)
            svmModel = SVMWithSGD.train(training.rdd(), numIterations).clearThreshold();
        else
            svmModel = SVMWithSGD.train(training.rdd(), numIterations).setThreshold(threshold);
    }

    public static HashMap<String, Object> testModel(String testpath) {

        HashMap<String, Object> evalMetrix = new HashMap<>();
        if (svmModel == null)
            throw new IllegalStateException("SVMModel is NULL!");
        JavaRDD<LabeledPoint> test = MLUtils.loadLabeledData(sc, testpath).toJavaRDD().cache();
        System.out.println("debug: " + test.collect().size());

        // Tuple2._1() is the predictive score = W^T * X
        // Tuple2._2() is the golden standard label
        // By default (if call clearThreshold()), if (W^T * X) > 0, then output is positive
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
        double auPR = metrics.areaUnderPR();
        JavaRDD<Tuple2<Object, Object>> pr = metrics.pr().toJavaRDD().cache();  // return the precision-recall curve, which is an RDD of (recall, precision)
        evalMetrix.put(AREAUNDERROC, auROC);
        evalMetrix.put(AREAUNDERPR, auPR);
        evalMetrix.put(PRCURVE, pr);

        return evalMetrix;
    }

    public static double predict(Vector example) {

        if (svmModel == null)
            throw new IllegalStateException("SVMModel is NULL!");
        Double score = svmModel.predict(example);
        return score;
    }
}
