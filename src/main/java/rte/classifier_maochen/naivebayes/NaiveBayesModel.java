package rte.classifier_maochen.naivebayes;

import java.util.Map;

/**
 * Created by qingqingcai on 6/11/15.
 */
public class NaiveBayesModel {

    double[][] meanVectors;
    double[][] varianceVectors;

    LabelIndexer labelIndexer;

    Map<Integer, Double> labelPrior;

    /** **************************************************************
     *
     */
    public void persist(String filename) {

    }

    /** **************************************************************
     * Load training data
     * @param filename
     */
    public void load(String filename) {


    }
}
