package rte.answerextraction;

import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.linalg.Vector;
import rte.classifier.SAEClassifier;
import rte.classifier.SparkLibSVM;
import rte.datastructure.DNode;
import rte.datastructure.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static rte.answerextraction.FeatureExtractor.extractFeatures;
import static rte.answerextraction.FeatureExtractor.toNumericFeature;
import static rte.answerextraction.FeatureExtractorUtils.generateAnswerCandidates;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class FeatureExtractorMain {

    private final static HashMap<String, Object> context = new HashMap() {
        {
            put(SAEClassifier.ITERATION, 50);
            put(SAEClassifier.THRESHOLD, Double.MAX_VALUE);
        }
    };
    private static SVMModel svmModel = null;
    private static HashMap<Integer, String> FITOFN = null;

    public static void main(String[] args) {

        SparkLibSVM sparkLibSVM = new SparkLibSVM();
        sparkLibSVM.init();
        sparkLibSVM.setConf(context);

        String feapath = "data/rte/FITOFN.ser";
        String modelpath = "data/rte/SPARKLIBSVM.model";

        // loda classifier and features
        svmModel = (SVMModel) sparkLibSVM.loadModel(modelpath);
        FITOFN = sparkLibSVM.loadFeature(feapath);

        String query = "Who is Obama?";
        String text = "Obama is the president.";

        runQA(query, text);
    }

    public static void runQA(String query, String text) {

        Graph graphQ = GraphExtended.stringToGraph(query);
        Graph graphT = GraphExtended.stringToGraph(text);
        runQA(query, text, graphQ, graphT);
    }

    public static void runQA(String query, String text, Graph graphQ, Graph graphT) {

        // for each (query, text) pair, generate list of SAC (short_answer_candidate)
        List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                = generateAnswerCandidates(graphQ, graphT);

        // for each SAC, generate features, predict the label (0/1)
        for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {
            List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
            HashMap<String, String> feaMap = extractFeatures(graphT, graphQ, ansCandNodeList);
            Vector feaVector = toNumericFeature(FITOFN, feaMap);
            double label = svmModel.predict(feaVector);
            System.out.println("\nquery = " + query);
            System.out.println("text = " + text);
            System.out.println("feaMap = " + feaMap);
            System.out.println("feaVector = " + feaVector);
            System.out.println("label = " + label);
        }
    }
}
