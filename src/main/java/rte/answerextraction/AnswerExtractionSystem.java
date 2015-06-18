package rte.answerextraction;

import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.linalg.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rte.RteMessageHandler;
import rte.datacollection.SAEData;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;
import rte.similarityflooding.NodePair;

import java.io.FileReader;
import java.io.IOException;
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

    private static boolean LOWERCASE = false;
    private static SVMModel svmModel = null;
    private static HashMap<Integer, String> FITOFN = new HashMap<>();

    public static void main(String[] args) {

        String trainName = "MIT99.smallexamples";
        String testName = "cmuwiki.smallexamples";
    //    String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train2393.cleanup.xml";
        String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml";
        String modelPath = "data/rte/SVMSPARK." + trainName + ".model";
        String feaPath = "data/rte/FITOFN." + trainName + ".ser";

//        // Raw data precessing
//        RawData.runRawData(new String[]{trainName, testName, trainInputPath});

//        // Generate features and prepare the training / testing data
//        TrainData.runTrainData(new String[]{trainName, testName});
//        FITOFN.putAll(TrainData.FITOFN);

//        // Run classifier
//        ClassifierTrainer.runClassifierTrainer(
//                new String[]{trainName, modelPath, feaPath}, FITOFN);

//        // Testing for single instance
//        runSingleTest(modelPath, feaPath);

        // Testing in json file
        String jsonPath = "data/rte/cmuWiki.json";
        runJSONTest(modelPath, feaPath, jsonPath, 10);
    }

    public static void runSingleTest(String modelpath, String feapath) {

        // loda classifier and features
        svmModel = (SVMModel) sparkLibSVM.loadModel(modelpath);
        FITOFN = sparkLibSVM.loadFeature(feapath);

        String query = "What book did Rachel Carson write in 1962?";
        String text = "Rachel Carson's 1962 \"Silent Spring\" said dieldrin causes mania.";

        runQA(query, text);

    }

    public static void runJSONTest(String modelPath, String feaPath, String jsonPath, int top) {

        svmModel = (SVMModel) sparkLibSVM.loadModel(modelPath);
        FITOFN = sparkLibSVM.loadFeature(feaPath);

        List<SAEData> dataList = new ArrayList<>();
        JSONParser paser = new JSONParser();
        try {
            JSONArray array = (JSONArray) paser.parse(new FileReader(jsonPath));

            Iterator<JSONObject> iter = array.iterator();

            int index = 1;
            while (iter.hasNext() && index <= top) {
                JSONObject obj = iter.next();
                String query = (String) obj.get("query");
                query = LOWERCASE ? query.toLowerCase() : query;
                String longanswer = (String) obj.get("answer");
                longanswer = LOWERCASE ? longanswer.toLowerCase() : longanswer;
                String shortanswer = (String) obj.get("optimal_answer");
                if (shortanswer.toLowerCase().equals("yes")
                        || shortanswer.toLowerCase().equals("no"))
                    continue;

                TreeMap<String, Double> cand_confidence = runQA(query, longanswer);
                System.out.println("\nquery = " + query);
                System.out.println("text = " + longanswer);
                cand_confidence.forEach((cand, conf) -> {
                    System.out.println("candidate = " + cand);
                    System.out.println("label = " + conf);
                });

//                // short answer evaluation
//                public static boolean evaluateForShortAnswer(final String expected, final String actual, final String actualInOriginalSentence)   {
//                    Pattern expectedPattern = Pattern.compile(expected.toLowerCase());
//
//                    // Split actual on short answer prefix, so that we are testing on just the short answer.
//                    // If the short answer prefix is not found, we expect to throw an index out of bounds exception.
//                    String actualShortAnswer = actual.split(IrUtils.SHORT_ANSWER_PREFIX)[1];
//
//                    boolean found = expectedPattern.matcher(actualShortAnswer.toLowerCase()).find();
//                    found |= expectedPattern.matcher(actualInOriginalSentence.toLowerCase()).find();
//
//                    return found;
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static TreeMap<String, Double> runQA(String query, String text) {

        Graph graphQ = GraphExtended.stringToGraph(query);
        Graph graphT = GraphExtended.stringToGraph(text);
        return runQA(query, text, graphQ, graphT);
    }

    public static TreeMap<String, Double> runQA(String query, String text, Graph graphQ, Graph graphT) {

        HashMap<String, Double> cand_confidence = new HashMap<>();

        // for each (query, text) pair, generate list of SAC (short_answer_candidate)
        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);
        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        whNode = whNode==null ? graphQ.getNodeById(1) : whNode;
        List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                = generateAnswerCandidates(whNode, nodeMatches);

        // for each SAC, generate features, predict the label (0/1)
        for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {
            List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
            HashMap<String, String> feaMap = extractFeatures(graphT, graphQ, ansCandNodeList, nodeMatches);
            Vector feaVector = toNumericFeature(FITOFN, feaMap);
            double label = svmModel.predict(feaVector);
            cand_confidence.put(fromTreeMapToString(ansCandNodeMap), label);
//            System.out.println("\nquery = " + query);
//            System.out.println("text = " + text);
//            System.out.println("candidate = " + fromTreeMapToString(ansCandNodeMap));
//            System.out.println("feaMap = " + feaMap);
//            System.out.println("feaVector = " + feaVector);
//            System.out.println("label = " + label);
        }

        ValueComparator bvc =  new ValueComparator(cand_confidence);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);

        return sorted_map;
    }
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(String a, String b) {
        if (base.get(a) <= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
