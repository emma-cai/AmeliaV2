package rte.answerextraction;

import org.apache.spark.mllib.classification.SVMModel;
import rte.classifier.SAEClassifier;

import java.util.HashMap;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class QAMain {

    private final static HashMap<String, Object> context = new HashMap() {
        {
            put(SAEClassifier.ITERATION, 50);
            put(SAEClassifier.THRESHOLD, Double.MAX_VALUE);
        }
    };


    public static void main(String[] args) {


    }


}
