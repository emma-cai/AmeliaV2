package rte.classifier.naivebayes;

import rte.classifier.Tuple;

import java.util.List;

/**
 * Created by qingqingcai on 6/11/15.
 */
public class NBTrainingEngine {

    private List<Tuple> trainingData;
    private NaiveBayesModel model;
    private int[] count;    // sum the training data by label

    private void calculateMean() {

    }

    private void calculateVariance() {

    }

    public void calculateLabelPrior() {

    }

    public NaiveBayesModel train() {

        calculateMean();
        calculateVariance();
        calculateLabelPrior();
        return model;
    }

    public NBTrainingEngine(List<Tuple> trainingData) {

        
    }
}
