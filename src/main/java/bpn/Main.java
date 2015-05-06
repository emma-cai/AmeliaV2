package bpn;

import bpn.clustering.SparkKMeans;
import bpn.clustering.SparkLDA;
import bpn.preparing.Data;
import bpn.preparing.FeatureExtraction;
import bpn.preparing.XMLExtraction;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qingqingcai on 4/29/15.
 */
public class Main {

    public static void main(String[] args) {

        String directory = "data/bpn/raw";
        ArrayList<Data> sayList = new ArrayList<Data>();

        // collect data and do preprocessing
        XMLExtraction.run(directory, sayList);
        FeatureExtraction.preprocess(sayList);

        /**
        // both KMeans and LDA
         FeatureExtraction.generateKMeansFeatures(sayList);
         FeatureExtraction.generateLDAFeatures(sayList);
        FeatureExtraction.run(sayList);

        // only for KMeans
         FeatureExtraction.generateKMeansFeatures(sayList);
        runKMeans(sayList);
         **/

        // only for LDA
        FeatureExtraction.generateLDAFeatures(sayList);
        runLDA(sayList);
    }

    /** **************************************************************
     * Save features to text file, one feature per line
     * The type of features is defined in algorithm: "kmeans", "lda"
     * 0.0 0.0 0.0
     * 0.1 0.1 0.1
     * 0.2 0.2 0.2
     * 9.0 9.0 9.0
     * 9.1 9.1 9.1
     * 9.2 9.2 9.2
     */
    public static void saveFeatureToFile(String filepath, ArrayList<Data> sayList, String algorithm) {
        try {
            // open BufferedWriter for feature
            OutputStreamWriter featureosw = new OutputStreamWriter(
                    new FileOutputStream(new File(filepath)));
            BufferedWriter feabw = new BufferedWriter(featureosw);

            for (Data data : sayList) {
                HashMap<String, String> features = new HashMap<>();
                if (algorithm.equals("kmeans"))
                    features = data.getKMeansFeatures();
                else if (algorithm.equals("lda"))
                    features = data.getLDAFeatures();
                else {
                    System.err.println("Wrong Algorithm! Only support kmeans and lda!");
                    System.exit(-1);
                }
                for (String key : features.keySet()) {
                    String val = features.get(key);
                    feabw.write(val + " ");
                }
                feabw.newLine();
            }

            // close BufferedWriter
            feabw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Save original text, one text one line
     * How can I help you?
     * You are welcome.
     */
    public static void saveTextToFile(String filepath, ArrayList<Data> sayList) {
        try {
            // open BufferedWriter for text
            OutputStreamWriter textosw = new OutputStreamWriter(
                    new FileOutputStream(new File(filepath)));
            BufferedWriter textbw = new BufferedWriter(textosw);

             for (Data data : sayList) {
                textbw.write(data.getText());
                textbw.newLine();
            }

            // close BufferedWriter
            textbw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Save KMeans output to file
     * ClusterID\tInstanceID\tText
     */
    public static void saveKMeansOut(String filepath, ArrayList<Data> sayList,
                       HashMap<Integer, ArrayList<Integer>> ClusterID_InstanceIDList) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(new File(filepath)));
            BufferedWriter bw = new BufferedWriter(osw);

            for (int clusterID : ClusterID_InstanceIDList.keySet()) {
                ArrayList<Integer> InstanceIDList = ClusterID_InstanceIDList.get(clusterID);
                for (int InstanceID : InstanceIDList) {
                    bw.write(clusterID + "\t" + InstanceID + "\t" + sayList.get(InstanceID).getText());
                    bw.newLine();
                }
            }

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Generate KMeans features, run KMeans algorithm and save data;
     */
    public static void runKMeans(ArrayList<Data> sayList) {

        // collect data (features) for KMeans
        //String textpath = "data/bpn/clustering/text.txt";
        String kmeansfeapath = "data/bpn/clustering/kmeans_feature.txt";
        //saveTextToFile(featurepath, sayList);
        saveFeatureToFile(kmeansfeapath, sayList, "kmeans");

        // run KMeans
        HashMap<Integer, Integer> InstanceID_ClusterID = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> ClusterID_InstanceIDList = new HashMap<>();
        SparkKMeans.run(kmeansfeapath, 10, 20, 1,
                InstanceID_ClusterID, ClusterID_InstanceIDList);

        // save KMeans output
        saveKMeansOut("data/bpn/clustering/kmeans.output", sayList, ClusterID_InstanceIDList);
    }

    /** **************************************************************
     * Generate LDA features, run LDA algorithm and save data;
     */
    public static void runLDA(ArrayList<Data> sayList) {

        // collect data (features) for lda
        String ldafeapath = "data/bpn/lda/lda_feature.txt";
        saveFeatureToFile(ldafeapath, sayList, "lda");

        // run LDA
        int topicN = 10;
        int topTopicN = 1;
        int topicWordsN = 5;

        HashMap<Integer, Integer> InstanceID_TopicID = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> TopicID_InstanceIDList = new HashMap<>();
        SparkLDA.run(ldafeapath, topicN, topTopicN, topicWordsN,
                InstanceID_TopicID, TopicID_InstanceIDList);

        // save LDA output
        saveKMeansOut("data/bpn/lda/lda.output", sayList, TopicID_InstanceIDList);
    }

    /** **************************************************************
     * Save KMeans output to file
     * ClusterID\tInstanceID\tText
     */
    public static void saveLDAOut(String filepath, ArrayList<Data> sayList,
                  HashMap<Integer, ArrayList<Integer>> TopicID_InstanceIDList) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(new File(filepath)));
            BufferedWriter bw = new BufferedWriter(osw);

            for (int clusterID : TopicID_InstanceIDList.keySet()) {
                ArrayList<Integer> InstanceIDList = TopicID_InstanceIDList.get(clusterID);
                for (int InstanceID : InstanceIDList) {
                    bw.write(clusterID + "\t" + InstanceID + "\t" + sayList.get(InstanceID).getText());
                    bw.newLine();
                }
            }

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
