package rte.classifier.naivebayes;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rte.classifier.IClassifier;
import rte.classifier.Tuple;

import java.util.List;
import java.util.Map;

/**
 * Created by qingqingcai on 6/11/15.
 */
public class NaiveBayesClassifier implements IClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesClassifier.class);
    private NaiveBayesModel model;

    @Override
    public IClassifier train(List<Tuple> trainingData) {
        model = new NBTrainingEngine(trainingData).train();
        return this;
    }

    @Override
    public Map<String, Double> predict(Tuple predict) {
        return null;
    }

    @Override
    public void setParameter(Map<String, String> paraMap) {
        throw new NotImplementedException("Method setParameter has not been implemented.");
    }
}
