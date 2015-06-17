package rte.classifier;

import java.util.HashMap;

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

    public void setConf(HashMap<String, Object> contextMap);

    public void saveModel(String modelpath);

    public Object loadModel(String modelpath);

    public void saveFeature(String path, HashMap<Integer, String> map);

    public HashMap<Integer, String> loadFeature(String path);

    public static void trainModel(String trainpath) {
        return;
    }

    public static HashMap<String, Object> testModel(String testpath) {
        return null;
    }
}
