package rte.classifier;

import org.apache.spark.mllib.classification.SVMModel;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by qingqingcai on 6/17/15.
 */
public interface SAEClassifier<L, F> {

    String AREAUNDERROC = "AREAUNDERROC";
    String AREAUNDERPR = "AREAUNDERPR";
    String PRCURVE = "PRCURVE";

    String ITERATION = "ITERATION";
    String THRESHOLD = "THRESHOLD";


    public void init();

    public void setSparkConfig(HashMap<String, Object> contextMap);

    public void saveModel(String modelpath, SVMModel svmModel);

    public Object loadModel(String modelpath);

    public void saveFeature(String path, TreeMap<Integer, String> map);

    public TreeMap<Integer, String> loadFeature(String path);

    public default Object trainModel(String trainpath) {
        return null;
    }
}
