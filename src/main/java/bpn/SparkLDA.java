package bpn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by qingqingcai on 5/1/15.
 */
public class SparkLDA {

    public static void main(String[] args) {
        test();
    }
    public static void test() {
        SparkConf conf = new SparkConf().setAppName("LDA Example").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load and parse the data
        String path = "data/mllib/sample_lda_data.txt";
     //   String path = "data/bpn/lda/lda_feature.txt";
        JavaRDD<String> data = sc.textFile(path);
        JavaRDD<Vector> parsedData = data.map(
                new Function<String, Vector>() {
                    public Vector call(String s) {
                        String[] sarray = s.trim().split(" ");
                        double[] values = new double[sarray.length];
                        for (int i = 0; i < sarray.length; i++)
                            values[i] = Double.parseDouble(sarray[i]);
                        return Vectors.dense(values);
                    }
                }
        );
        // Index documents with unique IDs
        JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
                new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
                    public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
                        return doc_id.swap();
                    }
                }
        ));
        corpus.cache();

        // Cluster the documents into three topics using LDA
        DistributedLDAModel ldaModel = new LDA().setK(3).run(corpus);

        // Output topics. Each is a distribution over words (matching word count vectors)
        System.out.println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize()
                + " words):");
        Matrix topics = ldaModel.topicsMatrix();
        for (int topic = 0; topic < 3; topic++) {
            System.out.print("Topic " + topic + ":");
            for (int word = 0; word < ldaModel.vocabSize(); word++) {
                System.out.print(" " + topics.apply(word, topic));
            }
            System.out.println();
        }

        System.out.println("\n\n\n");
        Tuple2<int[], double[]>[] tuples = ldaModel.describeTopics(3);
        for (int i = 0; i < tuples.length; i++) {
            Tuple2 tuple = tuples[i];
            int[] element1 = (int[]) tuple._1;
            double[] element2 = (double[]) tuple._2;
            for (int m = 0; m < element1.length; m++) {
                System.out.println(element1[m] + " : " + element2[m]);
            }
            System.out.println();
        }
    }

    public static void run(String inputFile, int topicN, int topTopicN, int topicWordsN,
                  HashMap<Integer, Integer> InstanceID_TopicID,
                  HashMap<Integer, ArrayList<Integer>> TopicID_InstanceIDList) {

        SparkConf conf = new SparkConf().setAppName("LDA").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load and parse the data
        JavaRDD<String> data = sc.textFile(inputFile);
        JavaRDD<Vector> parsedData = data.map(
                new Function<String, Vector>() {
                    public Vector call(String s) {
                        String[] sarray = s.trim().split(" ");
                        double[] values = new double[sarray.length];
                        for (int i = 0; i < sarray.length; i++)
                            values[i] = Double.parseDouble(sarray[i]);
                        return Vectors.dense(values);
                    }
                }
        );
        // Index documents with unique IDs
        JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
                new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
                    public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
                        return doc_id.swap();
                    }
                }
        ));
        corpus.cache();

        // Cluster the documents into "topicN" topics using LDA
        DistributedLDAModel ldaModel = new LDA().setK(topicN).run(corpus);

        // topicDis: <DocumentID, topicDistribution>, index of topicDistribution is the topicID
        RDD<Tuple2<Object, Vector>> topicDis = ldaModel.topicDistributions();

        // describeTopics: <wordIndexArray, distributionArray_for_each_word>
        Tuple2<int[], double[]>[] describeTopics = ldaModel.describeTopics(topicWordsN);

        // topicIndex_topics: <topicId, topic_original_words_list>
        HashMap<Integer, ArrayList<String>> topicIndex_topics = new HashMap<>();
        for (int topicIndex = 0; topicIndex < describeTopics.length; topicIndex++) {
            Tuple2 tuple = describeTopics[topicIndex];
            int[] wordIndexArray = (int[]) tuple._1;
            for (int m = 0; m < wordIndexArray.length && m < topicWordsN; m++) {
                ArrayList<String> topics = new ArrayList<>();
                if (topicIndex_topics.containsKey(topicIndex)) {
                    topics = topicIndex_topics.get(topicIndex);
                }
                topics.add(FeatureExtraction.lemmaList.get(wordIndexArray[m]));
                topicIndex_topics.put(topicIndex, topics);
            }
        }

        // For each document, find the most top topics and the most important words to describe the topic
//        for (Tuple2 tuple : topicDis.toJavaRDD().collect()) {
//            String InstanceID = tuple._1.toString();
//            double[] topicstmp = ((Vector) tuple._2).toArray();
//            Double[] topics = new Double[topicstmp.length];
//            for (int i = 0; i < topicstmp.length; i++) {
//                topics[i] = (Double) topicstmp[i];
//            }
//            ArrayIndexComparator comparator = new ArrayIndexComparator(topics);
//            Integer[] indexes = comparator.createIndexArray();
//            Arrays.sort(indexes, comparator);
//
//            System.out.print("\t" + tuple._1.toString() + "\t");
//            int count = 1;
//            for (int i = indexes.length-1; i >= 0 && count <= topTopicN; i--) {
//                System.out.println(topics[indexes[i]] + " | " + topicIndex_topics.get(i));
//                count++;
//            }
//            System.out.println();
//        }

        int k = 0;
        for (Tuple2 tuple : topicDis.toJavaRDD().collect()) {
            String InstanceID = tuple._1.toString();
            double[] topicstmp = ((Vector) tuple._2).toArray();
            Double[] topics = new Double[topicstmp.length];
            for (int i = 0; i < topicstmp.length; i++) {
                topics[i] = (Double) topicstmp[i];
            }
            ArrayIndexComparator comparator = new ArrayIndexComparator(topics);
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);


            int topicID = indexes[indexes.length-1];
            InstanceID_TopicID.put(k, topicID);

            ArrayList<Integer> InstanceIDList = new ArrayList<>();
            if (TopicID_InstanceIDList.containsKey(topicID)) {
                InstanceIDList = TopicID_InstanceIDList.get(topicID);
            }
            InstanceIDList.add(k);
            TopicID_InstanceIDList.put(topicID, InstanceIDList);

            k++;
        }
    }
}
