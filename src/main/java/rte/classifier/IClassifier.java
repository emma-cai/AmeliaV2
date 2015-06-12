package rte.classifier;

import java.util.List;
import java.util.Map;

/**
 * Created by qingqingcai on 6/11/15.
 */
public interface IClassifier {

    IClassifier train(List<Tuple> trainingData);

    Map<String, Double> predict(Tuple predict);

    void setParameter(Map<String, String> paraMap);
}
