package bpn.classification;

import bpn.feature.Feature;
import bpn.utils.Buckets;
import development.textdatacollection.Performance;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import scala.Tuple2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static bpn.utils.Fun.saveToFile;

/**
 * Created by qingqingcai on 5/7/15.
 */
public class SparkMultinomialLogisticRegression {

    public static ArrayList<String> feaList = new ArrayList<>();
    public static ArrayList<Double> lableList = new ArrayList<>();
    public static HashMap<Integer, Double> lineIndex_label = new HashMap<>();

    /** **************************************************************
     * Test BPN classifier based on apache spark libSVM
     * Training/Testing input: LABEL\tPLAIN_TEXT
     * Training/Testing feature: spark feature vector
     * Model path: Path to store trained classifier model
     */
    public static void main(String[] args) {

        String modelpath = "data/bpn/logisticregression/MultinomialLogisticRegress.model";
        String resultpath = "data/bpn/result/libSVM/bpn_test_libsvm.res";
        String traintextpath = "data/bpn/text/da_train.txt";
        String trainfeapath = "data/bpn/feature/da_train.fea";
        String testtextpath = "data/bpn/text/bpn_test.txt";
        String testfeapath = "data/bpn/feature/bpn_test.fea";

        Feature.init();

        if (args.length != 0) {
            if (args.length == 4) {
                traintextpath = args[0];
                trainfeapath = args[1];
                testtextpath = args[2];
                testfeapath = args[3];
            } else {
                System.out.println("bpn.feature.Feature <path_to_train_text> <path_to_train_feature> <path_to_test_text> <path_to_test_fea>");
                System.exit(-1);
            }
        }
        Feature.runFeatureExtraction(traintextpath, trainfeapath, testtextpath, testfeapath);   // extract features
        runMultinomialLogisticRegression(trainfeapath, testfeapath, modelpath, false);          // run classifier
        printResults(testfeapath, testtextpath, resultpath);                                    // save classification result
        double precision = Performance.computePerformance(testtextpath, resultpath);            // compute performance (precision)
        System.out.println("Performance (precision) = " + precision);
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
        JavaRDD<LabeledPoint> training = MLUtils.loadLibSVMFile(sc, trainpath).toJavaRDD().cache();
        JavaRDD<LabeledPoint> testing = MLUtils.loadLibSVMFile(sc, testpath).toJavaRDD().cache();


        // Split data into 60% for training and the rest for testing (60% for training and 40 for testing)
//        JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, testpath).toJavaRDD().cache();
//        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] {0.6, 0.4}, 11L);
//        JavaRDD<LabeledPoint> training = splits[0].cache();
//        JavaRDD<LabeledPoint> testing = splits[1].cache();

        // Run training algorithm to build the model
        int numClasses = Buckets.FOCUSED_ON.size();
        final LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
                .setNumClasses(numClasses)
                .run(training.rdd());

        JavaRDD<Tuple2<Object, Object>> predictionAndLabels = testing.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint p) {
                        Double prediction = 0.0;
                        try {
                            prediction = model.predict(p.features());

                            ArrayList<Integer> indices = new ArrayList<Integer>();
                            for (int i = 0; i < p.features().toArray().length; i++) {
                                double val = p.features().apply(i);
                                if (Double.compare(val, 0.0) != 0)
                                    indices.add(i);
                            }

                            feaList.add(indices.toString());
                            lableList.add(prediction);
                        } catch (Exception ex) {
                            System.out.println("Catch exception ... ");
                            ex.printStackTrace();
                        }

                        return new Tuple2<Object, Object>(prediction, p.label());
                    }
                }
        );

        // Get evaluation metrics
        MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
        double precision = metrics.precision();
        System.out.println("precision: " + precision);

        // Save and load model
        if (saveModel)
            model.save(sc, modelpath);
    //    LogisticRegressionModel sameModel = LogisticRegressionModel.load(sc, modelpath);
    }

    /** **************************************************************
     * Print out the predicated labels for each test instance
     * @param testfeapath Path to test features
     * @param testtextpath Path to original test text
     * @param resultpath Path to classification result
     */
    public static void printResults(String testfeapath, String testtextpath, String resultpath) {
        System.out.println("\n\n\nFINAL RESULTS!");
        try {
            final int[] lineIndex = {0};
            Files.lines(Paths.get(testfeapath), StandardCharsets.UTF_8).forEach(
                    line -> {
                        ArrayList<Integer> originalIndices = new ArrayList<>();
                        String[] arr = line.split(" ");
                        for (int i = 1; i < arr.length; i++) {
                            int index = Integer.parseInt(arr[i].split(":")[0]);
                            originalIndices.add(index-1);
                        }
                        if (feaList.contains(originalIndices.toString())) {
                            lineIndex_label.put(lineIndex[0]++, lableList.get(feaList.indexOf(originalIndices.toString())));
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print out labeling details
        ArrayList<String> resultList = new ArrayList<>();
        try {
            List<String> tmp = (List<String>) Files.readAllLines(Paths.get(testtextpath), StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<>();

            tmp.forEach(p -> {
                if (Buckets.FOCUSED_ON.contains(p.split("\t")[0])) {
                    lines.add(p);
                }
            });

            for (int lineIndex : lineIndex_label.keySet()) {
                int labelIndex = (int) Math.round(lineIndex_label.get(lineIndex));
            //    System.out.println(lines.get(lineIndex) + "\t" + /**labelIndex + "\t" + **/Buckets.FOCUSED_ON.get(labelIndex));
                String predictedLabel = Buckets.FOCUSED_ON.get(labelIndex);
                String predictedInstance = lines.get(lineIndex);
                String predictedSentence = predictedInstance.substring(predictedInstance.indexOf("\t")+1, predictedInstance.length());
                String result = predictedLabel + "\t" + predictedSentence;
                System.out.println(result);
                if (!resultList.contains(result)) {
                    resultList.add(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // save classification result
        saveToFile(resultpath, resultList);
    }

    public static void testByReadingFeatureFiles(String[] args) {

        String modelpath = "data/bpn/logisticregression/MultinomialLogisticRegress.model";
        String resultpath = "data/bpn/result/bpn_test_libsvm.res";
        String trainpath = "data/bpn/feature/da_train.fea";
        String testpath = "data/bpn/feature/bpn_test.fea";
        String text_testpath = "data/bpn/text/bpn_test.txt";

        runMultinomialLogisticRegression(trainpath, testpath, modelpath, false);

        printResults(testpath, text_testpath, resultpath);
    }
}
