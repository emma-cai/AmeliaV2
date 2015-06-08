package rte.experiments;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rte.RteMessageHandler;
import rte.datastructure.DTree;
import rte.datastructure.Graph;
import rte.graphmatching.AnswerExtraction;
import rte.graphmatching.DMatching;
import rte.graphmatching.NodeComparer;
import rte.utils.LangTools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by qingqingcai on 5/18/15.
 */
public class ExperimentsOnWiki {
    public static final Set<String> postagSet = NodeComparer.postagSet;

  //  private static String baseDir = "eliza-ir/src/test/resources/wiki";
    private static String baseDir = "eliza-corpus-test/src/test/resources/srugm/corpus/wiki";

    private static String textDir = baseDir;

    private static String conllxDir = "/Users/qingqingcai/Documents/workspace/AmeliaData";

    private static HashMap<String, List<String>> file_texts = new HashMap<>();

    private static HashMap<String, List<Graph>> file_graphs = new HashMap<>();

    private static int TESTNUM = 0;

    private static int CORRECTNUM = 0;

    private static double PRECISION = 0.0;

    private static ArrayList<String> errInstances = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        String jsonpath = "/Users/qingqingcai/Documents/workspace/amelia/eliza-corpus-test/src/test/resources/srugm/testfile/wiki/IRtests.json";

     //   runTextToConllx(textDir, conllxDir);

        runPerformanceTest(jsonpath, true);        // Set false if reading

        for (String errMsg : errInstances) {
            System.out.println("\n--------------------------------------------------");
            System.out.println(errMsg);
            System.out.println("--------------------------------------------------");
        }

        System.out.println("Precision of the system = " + PRECISION);
    }

    /** **************************************************************
     * Read plain text, convert the text to conllx (which will be used
     * to build a tree quickly)
     * @param textDir Path to plain text
     * @param conllxDir Path to conllx
     * @throws java.io.IOException
     */
    public static void runTextToConllx(String textDir, String conllxDir) throws IOException {

        File inFile = null;
        try {
            File folder = new File(textDir);
            File[] listOfFiles = folder.listFiles();
            for (File f : listOfFiles) {
                System.out.println("Processing: " + f.getName());
                if (f.isFile() && f.getName().endsWith(".txt")) {
                    String conllxPath = conllxDir + File.separator + f.getName().substring(0, f.getName().indexOf(".txt")) + ".conllx";
                    HashMap<String, String> text_conllx = new HashMap<>();

                    inFile = new File(f.getAbsolutePath());
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        if (line.trim().isEmpty())
                            continue;
                        DTree dtree = DTree.buildTree(line.toLowerCase());
//                        SemanticHeadFinder headFinder = new SemanticHeadFinder(false); // keep copula verbs as head
//                        GrammaticalStructure egs = new EnglishGrammaticalStructure(dtree, string -> true, headFinder, true);
//                        System.out.println("current = " + line);
//                        if (egs != null) {
//                            String conllx = EnglishGrammaticalStructure.dependenciesToString(egs, egs.typedDependencies(), tree, true, true);
//                            text_conllx.put(line, conllx);
//                        }

                        String conllx = dtree.toString();
                        text_conllx.put(line, conllx);
                    }

                    File outFile = new File(conllxPath);
                    if (!outFile.exists())
                        outFile.createNewFile();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
                    text_conllx.forEach((text, conllx) -> {
                        try {
                            //    bw.write("\"text:\"" + text + "\"conllx:\"" + conllx);
                            bw.write(text + "\n" + conllx);
                            bw.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Couldn't read document: " + inFile + ". Exiting");
            throw e;
        }
    }

    /** **************************************************************
     * Compute the QA performance: read json file firstly and decide
     * which test file should be read;
     * @param jsonpath Path to json file (containing questions)
     * @param fromConllx Set fromConllx = true if you want to read from
     *                   conllx (will be faster); Set fromConllx = false
     *                   if you want to read from plain text (takes time
     *                   since it firstly run Stanford dependency tree)
     */
    public static void runPerformanceTest(String jsonpath, boolean fromConllx) {

        File jsonTestFile = null;
        try {
            jsonTestFile = new File(jsonpath);
            String filename = jsonTestFile.getAbsolutePath();
            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new FileReader(filename));
            JSONArray jsonObject = (JSONArray) obj;
            ListIterator<JSONObject> li = jsonObject.listIterator();
            while (li.hasNext()) {
                JSONObject jo = li.next();
                String fname = (String) jo.get("file");
                String query = (String) jo.get("query");
                String answer = (String) jo.get("answer");

                runQA(fname.substring(0, fname.indexOf(".txt")), query, answer, fromConllx);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): File not found: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FileNotFoundException");
        }
        catch (IOException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): IO exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            throw new RuntimeException("IOException");
        }
        catch (ParseException e) {
            System.out.println("Error in InferenceWikiTest.prepare(): Parse exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            throw new RuntimeException("ParseException");
        }
        catch (Exception e) {
            System.out.println("Error in InferenceWikiTest.prepare(): Parse exception reading: " + jsonTestFile);
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Exception");
        }
    }

    /** **************************************************************
     * For each "query", find the best answer from file "fname"
     * @param fname Path to text file containing statements
     * @param query Query
     * @param answer Expected answer
     * @param fromConllx True if read conllex; False if read plain text
     * @throws Exception
     */
    public static void runQA(String fname, String query, String answer, boolean fromConllx) throws Exception {

        File inFile = null;

        if(!file_texts.containsKey(fname)) {
            //reading the lines
            try {
                if (fromConllx == false) {
                    inFile = new File(textDir + File.separator + fname + ".txt");
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> texts = Files.readAllLines(path);
                    List<Graph> graphs = new ArrayList<>();
                    for (String text : texts) {
                        if (text.trim().isEmpty())
                            continue;
                        DTree dtree = DTree.buildTree(text);
                        Graph dgraph = Graph.buildDGraph(dtree);
                        graphs.add(dgraph);
                    }
                    file_texts.put(fname, texts);
                    file_graphs.put(fname, graphs);
                } else {
                    inFile = new File(conllxDir + File.separator + fname + ".conllx");
                    Path path = Paths.get(inFile.getAbsolutePath());
                    List<String> lines = Files.readAllLines(path);
                    boolean afterText = false;

                    List<String> texts = new ArrayList<>();
                    List<Graph> graphs = new ArrayList<>();
                    String text = "";
                    String conllx = "";
                    for (String line : lines) {
                        if (afterText == false) {
                            text = line;
                            conllx = "";
                            afterText = true;
                        } else if (line.trim().isEmpty()) {
                            System.out.println("test = " + conllx);
                            DTree dtree = LangTools.getDTreeFromCoNLLXString(conllx, true);
                            Graph dgraph = Graph.buildDGraph(dtree);
                            texts.add(text);
                            graphs.add(dgraph);
                            afterText = false;
                        } else {
                            conllx += line + "\n";
                        }
                    }
                    file_texts.put(fname, texts);
                    file_graphs.put(fname, graphs);
                }

            } catch (Exception e) {
                System.out.println("Couldn't read document: " + inFile + ". Exiting");
                throw e;
            }
        }

        List<Graph> graphs = file_graphs.get(fname);
        List<String> texts = file_texts.get(fname);

        DTree DTree_T = DTree.buildTree(query);
        Graph Graph_H = Graph.buildDGraph(DTree_T);

        double minimumCost = Double.MAX_VALUE;
        Graph minimumTGraph = null;
        HashMap minimumNodeMatches = new HashMap<>();

        String actual = null;
        String shortAnswer = null;
        for(int i = 0; i < graphs.size(); i++)  {
            Graph Graph_T = graphs.get(i);
            // These calls are performed in initNodeMatches( ).
            //GraphComparer comparer = RteMessageHandler.defaultRteConfiguration.graphComparer;
            //comparer.init(graph_T, graph_H);
            HashMap nodeH_sim_NodePairList = DMatching.initNodeMatches(Graph_T, Graph_H, RteMessageHandler.config);
            double graphSimilarity = DMatching.computeMatchingCost(Graph_T, Graph_H, nodeH_sim_NodePairList);

            if (Double.compare(graphSimilarity, minimumCost) < 0) {
                minimumCost = graphSimilarity;
                actual = texts.get(i);
                minimumTGraph = Graph_T;
                minimumNodeMatches = nodeH_sim_NodePairList;
            }
        }
        shortAnswer = AnswerExtraction.runAnswerExtraction(minimumTGraph, Graph_H, minimumNodeMatches);

        TESTNUM++;
        boolean correctAnswer = judgeAnswer(answer, actual, "");
        if (correctAnswer)
            CORRECTNUM++;

        if (!correctAnswer) {

//            String errMsg = "fname = " + fname
//                    + "\nquery = " + query
//                    + "\nexpected = " + answer
//                    + "\nactual = " + actual
//                    + "\nminimumCost = " + minimumCost
//                    + "\nisCorrect = " + correctAnswer
//                    + "\nPrecision = " + (CORRECTNUM * 1.0) / (TESTNUM * 1.0);
//            errInstances.add(errMsg);
        }

        PRECISION = (CORRECTNUM * 1.0) / (TESTNUM * 1.0);
        System.out.println("\n--------------------------------------------------");
        System.out.println("fname = " + fname);
        System.out.println("query = " + query);
        System.out.println("expected = " + answer);
        System.out.println("actual = " + actual);
        System.out.println("short answer = " + shortAnswer);
        System.out.println("minimumCost = " + minimumCost);
        System.out.println("isCorrect = " + correctAnswer);
        System.out.println("Precision = " + CORRECTNUM + "/" + TESTNUM + " = " + PRECISION);
        System.out.println("--------------------------------------------------");
    }

    /** **************************************************************
     * Compare the actual answer with expected answer
     * @param expected Expected answer in json file
     * @param actual Actual answer returned by GraphMatching
     * @return
     */
    public static boolean judgeAnswer(String expected, String actual, String actualInOriginalSentence) {

        boolean isFind = expected.equalsIgnoreCase(actualInOriginalSentence);
        if (!isFind) {
            try {
                Pattern expectedPattern = Pattern.compile(expected.toLowerCase());
                isFind |= expectedPattern.matcher(actual.toLowerCase()).find();
                isFind |= expectedPattern.matcher(actualInOriginalSentence.toLowerCase()).find();
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
            }
        }

        return isFind;
    }
}
