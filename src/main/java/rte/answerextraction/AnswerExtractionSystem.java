package rte.answerextraction;

import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.linalg.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rte.RteMessageHandler;
import rte.classifier.ClassifierTrainer;
import rte.datacollection.SAEData;
import rte.datacollection.TrainData;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;
import rte.similarityflooding.NodePair;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static rte.answerextraction.FeatureExtractor.extractFeatures;
import static rte.answerextraction.FeatureExtractor.toNumericFeature;
import static rte.answerextraction.FeatureExtractorUtils.fromTreeMapToString;
import static rte.answerextraction.FeatureExtractorUtils.generateAnswerCandidates;
import static rte.graphmatching.DMatching.initNodeMatches;

/**
 * Created by qingqingcai on 6/18/15.
 */
public class AnswerExtractionSystem extends RteMessageHandler {

    private static boolean LOWERCASE = false;
    private static SVMModel svmModel = null;
    private static TreeMap<Integer, String> FITOFN = new TreeMap<>();

    public static void main(String[] args) {

        String trainName = "MIT99.smallexamples";
        String testName = "cmuwiki.smallexamples";
    //    String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train2393.cleanup.xml";
        String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml";
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

//        // Testing in json file
        String jsonPath = "data/rte/cmuWiki.json";
        runJSONTest(modelPath, feaPath, jsonPath, 30, 60);
    }

    public static void runSingleTest(String modelpath, String feapath) {

        // loda classifier and features
        svmModel = (SVMModel) sparkLibSVM.loadModel(modelpath);
        FITOFN = sparkLibSVM.loadFeature(feapath);

        String query = "What book did Rachel Carson write in 1962?";
        String text = "Rachel Carson's 1962 \"Silent Spring\" said dieldrin causes mania.";

        query = "What was the value of the monetary value of the Nobel Peace Prize in 1989?";
        text = "Each Novel Prize is worth $469,000.";

        query = "What are the similarities between beetles and grasshoppers?";
        text = "Beetles have mouthparts similar to those of grasshoppers.";

        query = "Please tell me about beetle anatomy";
        text = "The general anatomy of beetles is quite uniform, although specific organs and appendages may vary greatly in appearance and function between the many families in the order.";

        query = "Who was made commander of the Continental Army?";
        text = "He was made commander of the Continental Army on June 15, 1775";
        runQA(query, text);

    }

    public static void runJSONTest(String modelPath, String feaPath,
                                   String jsonPath, int start, int end) {

        svmModel = (SVMModel) sparkLibSVM.loadModel(modelPath);
        FITOFN = sparkLibSVM.loadFeature(feaPath);

        List<SAEData> dataList = new ArrayList<>();
        JSONParser paser = new JSONParser();
        try {
            JSONArray array = (JSONArray) paser.parse(new FileReader(jsonPath));

            Iterator<JSONObject> iter = array.iterator();

            int index = 0;
            while (iter.hasNext()) {
                index++;
                if (index < start || index > end)
                    continue;

                JSONObject obj = iter.next();
                String query = (String) obj.get("query");
                query = LOWERCASE ? query.toLowerCase() : query;
                String longanswerOri = (String) obj.get("answer");
                String[] longanswerArr = longanswerOri.split(" \\| ");
                String shortanswer = (String) obj.get("optimal_answer");
                if (shortanswer.toLowerCase().equals("yes")
                        || shortanswer.toLowerCase().equals("no"))
                    continue;

                for (String longanswer : longanswerArr) {
                    longanswer = longanswer.trim();
                    longanswer = LOWERCASE ? longanswer.toLowerCase() : longanswer;
                    NavigableMap<Double, List<String>> conf_candList = runQA(query, longanswer);
                    System.out.println("\n**************************************************");
                    System.out.println("query = " + query);
                    System.out.println("text = " + longanswer);
                    System.out.println("optimal_answer = " + shortanswer);
                    conf_candList.forEach((cand, conf) -> {
                        System.out.println("---\nlabel = " + conf);
                        System.out.println("candidate = " + cand);
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static NavigableMap<Double, List<String>> runQA(String query, String text) {

        Graph graphQ = GraphExtended.stringToGraph(query);
        Graph graphT = GraphExtended.stringToGraph(text);
        return runQA(query, text, graphQ, graphT);
    }

    public static NavigableMap<Double, List<String>> runQA(String query, String text, Graph graphQ, Graph graphT) {

        TreeMap<Double, List<String>> conf_candList = new TreeMap<>();

        // for each (query, text) pair, generate list of SAC (short_answer_candidate)
        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);
        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        whNode = (whNode==null) ? graphQ.getNodeById(1) : whNode;
        DNode whParent = whNode.getHead();
        whParent = (whParent==null || whParent.getId()==0) ? whNode : whParent;
        List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                = generateAnswerCandidates(whParent, nodeMatches);

        // for each SAC, generate features, predict the label (0/1)
        for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {
            List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
            HashMap<String, String> feaMap = extractFeatures(graphT, graphQ, ansCandNodeList, nodeMatches);
            Vector feaVector = toNumericFeature(FITOFN, feaMap);
            double label = svmModel.predict(feaVector);
            String ansCandStr = fromTreeMapToString(ansCandNodeMap);
            List<String> candList = new ArrayList<>();
            if (conf_candList.containsKey(label)) {
                candList = conf_candList.get(label);
            }
            candList.add(ansCandStr);
            conf_candList.put(label, candList);

            System.out.println("\nquery = " + query);
            System.out.println("text = " + text);
            System.out.println("candidate = " + fromTreeMapToString(ansCandNodeMap));
            System.out.println("feaMap = " + feaMap);
            System.out.println("feaVector = " + feaVector);
            System.out.println("label = " + label);
        }
        return conf_candList.descendingMap();
    }


   // short answer evaluation
    public static boolean evaluateForShortAnswer(final String expected, final String actual, final String actualInOriginalSentence)   {
        Pattern expectedPattern = Pattern.compile(expected.toLowerCase());

        // Split actual on short answer prefix, so that we are testing on just the short answer.
        // If the short answer prefix is not found, we expect to throw an index out of bounds exception.
        String actualShortAnswer = actual;

        boolean found = expectedPattern.matcher(actualShortAnswer.toLowerCase()).find();
        found |= expectedPattern.matcher(actualInOriginalSentence.toLowerCase()).find();

        return found;
    }
}
