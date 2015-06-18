package rte.answerextraction;

import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.linalg.Vector;
import rte.RteMessageHandler;
import rte.classifier.ClassifierTrainer;
import rte.datacollection.TrainData;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;
import rte.similarityflooding.NodePair;

import java.util.*;

import static rte.answerextraction.FeatureExtractor.extractFeatures;
import static rte.answerextraction.FeatureExtractor.toNumericFeature;
import static rte.answerextraction.FeatureExtractorUtils.fromTreeMapToString;
import static rte.answerextraction.FeatureExtractorUtils.generateAnswerCandidates;
import static rte.graphmatching.DMatching.initNodeMatches;

/**
 * Created by qingqingcai on 6/18/15.
 */
public class AnswerExtractionSystem extends RteMessageHandler {

    private static SVMModel svmModel = null;
    private static HashMap<Integer, String> FITOFN = new HashMap<>();

    public static void main(String[] args) {

        String trainName = "MIT99";
        String testName = "cmuwiki";
        String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train2393.cleanup.xml";
        String modelPath = "data/rte/SVMSPARK." + trainName + ".model";
        String feaPath = "data/rte/FITOFN." + trainName + ".ser";

//        // Raw data precessing
//        RawData.runRawData(new String[]{trainName, testName, trainInputPath});

        // Generate features and prepare the training / testing data
        TrainData.runTrainData(new String[]{trainName, testName});
        FITOFN.putAll(TrainData.FITOFN);

        // Run classifier
        ClassifierTrainer.runClassifierTrainer(
                new String[]{trainName, modelPath, feaPath}, FITOFN);

        // Testing for single instance
        runSingleTest(modelPath, feaPath);
    }

    public static void runSingleTest(String modelpath, String feapath) {

        // loda classifier and features
        svmModel = (SVMModel) sparkLibSVM.loadModel(modelpath);
        FITOFN = sparkLibSVM.loadFeature(feapath);

        String query = "What book did Rachel Carson write in 1962?";
        String text = "Rachel Carson's 1962 \"Silent Spring\" said dieldrin causes mania.";

        runQA(query, text);

    }

    public static void runQA(String query, String text) {

        Graph graphQ = GraphExtended.stringToGraph(query);
        Graph graphT = GraphExtended.stringToGraph(text);
        runQA(query, text, graphQ, graphT);
    }

    public static void runQA(String query, String text, Graph graphQ, Graph graphT) {

        // for each (query, text) pair, generate list of SAC (short_answer_candidate)
        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);
        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                = generateAnswerCandidates(whNode, nodeMatches);

        // for each SAC, generate features, predict the label (0/1)
        for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {
            List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
            HashMap<String, String> feaMap = extractFeatures(graphT, graphQ, ansCandNodeList, nodeMatches);
            Vector feaVector = toNumericFeature(FITOFN, feaMap);
            double label = svmModel.predict(feaVector);
            System.out.println("\nquery = " + query);
            System.out.println("text = " + text);
            System.out.println("candidate = " + fromTreeMapToString(ansCandNodeMap));
            System.out.println("feaMap = " + feaMap);
            System.out.println("feaVector = " + feaVector);
            System.out.println("label = " + label);
        }
    }
}
