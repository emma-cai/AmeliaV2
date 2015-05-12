package bpn.feature;

import bpn.utils.Buckets;
import bpn.utils.Fun;
import development.textdatacollection.Data;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by qingqingcai on 5/7/15.
 */
public class Feature {

    protected static StanfordCoreNLP pipeline;
    public static List<String> lemmaList = new ArrayList<>();
    private static boolean debug = false;

    /** **************************************************************
     * Test Feature Extraction
     * @param args
     */
    public static void main(String[] args) {

        init();

        String traintextpath = "data/bpn/text/da_train.txt";
        String trainfeapath = "data/bpn/feature/da_train.fea";
        String testtextpath = "data/bpn/text/bpn_test.txt";
        String testfeapath = "data/bpn/feature/bpn_test.fea";

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
        runFeatureExtraction(traintextpath, trainfeapath, testtextpath, testfeapath);
    }

    /** **************************************************************
     * Initialize Stanford pipeline
     */
    public static void init() {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    /** **************************************************************
     * Collect all lemmas from training and testing file, then compute
     * training and testing features;
     * @param traintextpath Path to training text
     * @param trainfeapath Path to training features
     * @param testtextpath Path to testing text
     * @param testfeapath Path to testing features
     */
    public static void runFeatureExtraction(String traintextpath, String trainfeapath,
                           String testtextpath, String testfeapath) {

        // for training: read training data, and get lemmas
        HashMap<Integer, String> id_label_train = new HashMap<>();
        HashMap<Integer, String> id_text_train = new HashMap<>();
        HashMap<Integer, double[]> id_dense_train = new HashMap<>();
        HashMap<Integer, String> id_sparse_train = new HashMap<>();
        HashMap<Integer, ArrayList<String>> id_lemmas_train = new HashMap<>();
        readTextData(traintextpath, id_label_train, id_text_train);

        id_text_train.forEach((id, text) -> {
            String textprocessed = regExp(text);
            ArrayList<String> lemmas = lemmatize(textprocessed); // convert to lemma
            id_lemmas_train.put(id, lemmas);
        });

        // for testing: read testing data, and get lemmas
        HashMap<Integer, String> id_label_test = new HashMap<>();
        HashMap<Integer, String> id_text_test = new HashMap<>();
        HashMap<Integer, double[]> id_dense_test = new HashMap<>();
        HashMap<Integer, String> id_sparse_test = new HashMap<>();
        HashMap<Integer, ArrayList<String>> id_lemmas_test = new HashMap<>();
        readTextData(testtextpath, id_label_test, id_text_test);

        id_text_test.forEach((id, text) -> {
            String textprocessed = regExp(text);
            ArrayList<String> lemmas = lemmatize(textprocessed); // convert to lemma
            id_lemmas_test.put(id, lemmas);
        });


        // compute training features
        id_text_train.forEach((id, text) -> {
            double[] denseArray_train = generateBOWFeatures(id_lemmas_train.get(id));
            id_dense_train.put(id, denseArray_train);
            if (debug) System.out.println("training" + "\t" + id + "\t" + id_label_train.get(id) + "\t" + id_text_train.get(id) + "\t" + denseArray_train.length);
        });
        convertDenseToSparse(id_dense_train, id_sparse_train);
        Fun.saveToFile(trainfeapath, id_label_train, id_sparse_train);

        // comute testing features
        id_text_test.forEach((id, text) -> {
            double[] denseArray_test = generateBOWFeatures(id_lemmas_test.get(id));
            id_dense_test.put(id, denseArray_test);
            if (debug) System.out.println("testing" + "\t" + id + "\t" + id_label_test.get(id) + "\t" + id_text_test.get(id) + "\t" + denseArray_test.length);
        });
        convertDenseToSparse(id_dense_test, id_sparse_test);
        Fun.saveToFile(testfeapath, id_label_test, id_sparse_test);
    }

    /** **************************************************************
     * Read original file, separate into id_label/text
     * @param textpath one data one line, with two columns [label text]
     * @param id_label HashMap: key = id, value = label
     * @param id_text HashMap: key = id, value = text
     */
    public static void readTextData(String textpath,
           HashMap<Integer, String> id_label, HashMap<Integer, String> id_text) {

        try {
            Files.lines(Paths.get(textpath), StandardCharsets.UTF_8).forEach(
                    line -> {
                        String[] arr = line.split("\t");
                    //    id_label.put(id_label.size(), arr[0]);

                        String label = Integer.toString(Buckets.FOCUSED_ON.size());

                        if (Buckets.FOCUSED_ON.indexOf(arr[0]) != -1) {
                            label = Integer.toString(Buckets.FOCUSED_ON.indexOf(arr[0]));
                            id_label.put(id_label.size(), label);
                            id_text.put(id_text.size(), arr[1]);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Convert dense feature vector to sparse feature vector
     * For example,
     * dense feature vector:    [1.0, 0.0, 3.0]
     * sparse feature vector:   (3, [0, 2], [1.0, 3.0])
     */
    public static void convertDenseToSparse(HashMap<Integer, double[]> id_dense,
                                           HashMap<Integer, String> id_sparse) {

        StringBuilder sb = new StringBuilder();

        for (Integer id : id_dense.keySet()) {
            double[] dense = id_dense.get(id);
            for (int i = 0; i < dense.length; i++) {
                if (i == 0) {
                    sb = new StringBuilder();
            //        sb.append(dense[i]);
                }
                if (Double.compare(dense[i], 0.0) != 0) {
                    if (!sb.toString().isEmpty())
                        sb.append(" ");
                    sb.append(i+1 + ":" + dense[i]);
                }

//                if (i == dense.length - 1)
//                    id_sparse.put(id, sb.toString());
            }
            // Add a dummy entry, to make sure that the feature dimensions in training and testing are identical
            int tmp = dense.length+1;
            sb.append(" " + tmp + ":" + "1.0");
            id_sparse.put(id, sb.toString());
        }
    }

    /** **************************************************************
     * Given a string, remove unnecessary characters
     * TODO: need to think about which characters should be removedd
     */
    public static String regExp(String str) {

        String result = str;

        // convert upper-characters into lower-characters
        result = result.toLowerCase();

        // remove ${.*};
        // input = Ticket Number: ${radarTicketId}
        // output = Ticket Number
        result = result.replaceAll("\\$\\{", "");
        result = result.replaceAll("\\}", "");

        // convert "*'m", etc to "* 'm", etc
        result = result.replaceAll("'m", " 'm");
        result = result.replaceAll("'ve", " 've");
        result = result.replaceAll("'s", " 's");

        // remove punctuations???
        // result = result.replaceAll("[:|,|.]", "");

        // trim
        result = result.trim();
        return result;
    }

    /** **************************************************************
     * convert text into lemmas_list
     */
    public static ArrayList<String> lemmatize(String text) {

        ArrayList<String> lemmas = new ArrayList<String>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                lemmas.add(lemma);
                if (!lemmaList.contains(lemma))
                    lemmaList.add(lemma);
            }
        }
        return lemmas;
    }

    /** **************************************************************
     * Generate KMeans features
     * (1) length of string;
     * (2) bag-of-words features;
     */
    public static double[] generateBOWFeatures(ArrayList<String> lemmasInText) {

        HashMap<String, Double> bowFeatures = new HashMap<>();
        for (String l : lemmaList) {
            if (lemmasInText.contains(l))
                bowFeatures.put(l, 1.0);
            else
                bowFeatures.put(l, 0.0);
        }
        double[] valArray = new double[bowFeatures.size()];
        int i = 0;
        for(double val : bowFeatures.values()) {
            valArray[i] = val;
            i++;
        }
        return valArray;
    }

    /** **************************************************************
     * For each data, do preprocessing before generating features
     * (1) remove unnecessary characters using regular expression;
     * (2) convert text to lemma based on StanfordCoreNLP;
     */
    public static void preprocess(ArrayList<Data> sayList) {

      //  FeatureExtraction feaClass = new FeatureExtraction();
        for (Data data : sayList) {
            // preprocessing of text
            String text = data.getText();
            text = regExp(text);

            // convert word to lemma
            List<String> lemmas = lemmatize(text);
            data.setLemmas(lemmas);
        }
    }

    /** **************************************************************
     * Generate KMeans features
     * (1) length of string;
     * (2) bag-of-words features;
     */
    public static void generateKMeansFeatures(ArrayList<Data> sayList) {

        for (Data data : sayList) {
            data.addKMeansFeatures("text_length", Integer.toString(data.getLemmas().size()));
            //    data.KMeansFeatures.put("text_length", Integer.toString(data.lemmas.size()));
            for (String l : lemmaList) {
                if (data.getLemmas().contains(l))
                    data.addKMeansFeatures(l, "1");
                else
                    data.addKMeansFeatures(l, "0");
            }
        }
    }

    /** **************************************************************
     * Generate LDA features
     * feature = occurrences for each element
     */
    public static void generateLDAFeatures(ArrayList<Data> sayList) {

        for (Data data : sayList) {
            for (String l : lemmaList) {
                int occurences = Collections.frequency(data.getLemmas(), l);
                data.addLDAFeatures(l, Integer.toString(occurences));
            }
        }
    }
}
