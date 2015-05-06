package bpn.preparing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by qingqingcai on 4/29/15.
 */
public class FeatureExtraction {

    protected StanfordCoreNLP pipeline;
    public static List<String> lemmaList = new ArrayList<String>();

    public FeatureExtraction() {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    /** **************************************************************
     * Preprocess the original text data;
     * Generate features for different models;
     */
    public static void run(ArrayList<Data> sayList) {

        preprocess(sayList);

        generateKMeansFeatures(sayList);

        generateLDAFeatures(sayList);
    }

    /** **************************************************************
     * For each data, do preprocessing before generating features
     * (1) remove unnecessary characters using regular expression;
     * (2) convert text to lemma based on StanfordCoreNLP;
     */
    public static void preprocess(ArrayList<Data> sayList) {

        FeatureExtraction feaClass = new FeatureExtraction();
        for (Data data : sayList) {
            // preprocessing of text
            String text = data.text;
            text = regExp(text);

            // convert word to lemma
            List<String> lemmas = feaClass.lemmatize(text);
            data.lemmas = lemmas;
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
    public ArrayList<String> lemmatize(String text) {

        ArrayList<String> lemmas = new ArrayList<String>();
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);
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
    public static void generateKMeansFeatures(ArrayList<Data> sayList) {

        for (Data data : sayList) {

            data.KMeansFeatures.put("text_length", Integer.toString(data.lemmas.size()));
            for (String l : lemmaList) {
                if (data.lemmas.contains(l))
                    data.KMeansFeatures.put(l, "1");
                else
                    data.KMeansFeatures.put(l, "0");
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
                int occurences = Collections.frequency(data.lemmas, l);
                data.LDAFeatures.put(l, Integer.toString(occurences));
            }
        }
    }
}
