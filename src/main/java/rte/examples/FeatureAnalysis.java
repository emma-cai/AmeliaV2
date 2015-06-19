package rte.examples;

import rte.datacollection.SAEData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class FeatureAnalysis {

    public static void main(String[] args) {

        String trainName = "MIT99.smallexamples";
        String testName = "cmuwiki.smallexamples";
        String trainExcelPath = "data/rte/" + trainName + ".train.xls";
        String testExcelPath = "data/rte/" + testName + ".test.xls";
        String trainSheetName = trainName;
        String testSheetName = testName;
        List<SAEData> trainDataWithFeaturesList = SVMSparkMLlib.readFromXML(trainExcelPath, trainSheetName);

        featureFrequency(trainDataWithFeaturesList);
    }

    public static void featureFrequency(List<SAEData> dataList) {

        HashMap<String, HashMap<String, List<String>>> fv_label_idList = new HashMap<>();
    //    String colTarget = "10";        // whlemma_0/1(contains CD or not)
        String colTarget = "8";        // whlemma_lcadep


        int idhelp = 0;
        for (SAEData data : dataList) {

            String id = data.getID() + "-" + Integer.toString(idhelp++);
            String label = data.getLabel();
            HashMap<String, String> feamap = data.getFeamap();
            String fv = feamap.get(colTarget);
            if (fv_label_idList.containsKey(fv)) {
                HashMap<String, List<String>> label_idList = fv_label_idList.get(fv);
                if (label_idList.containsKey(label)) {
                    List<String> idList = label_idList.get(label);
                    idList.add(id);
                    label_idList.put(label, idList);
                    fv_label_idList.put(fv, label_idList);
                } else {
                    label_idList.put(label, new ArrayList<>(Arrays.asList(id)));
                    fv_label_idList.put(fv, label_idList);
                }
            } else {
                HashMap<String, List<String>> label_idList = new HashMap<>();
                label_idList.put(label, new ArrayList<>(Arrays.asList(id)));
                fv_label_idList.put(fv, label_idList);
            }
        }

        fv_label_idList.forEach((fv, label_idList) -> {
            System.out.println(fv + "\t" + "0" + "\t" + (label_idList.containsKey("0") ? label_idList.get("0").size() : 0));
            System.out.println(fv + "\t" + "1" + "\t" + (label_idList.containsKey("1") ? label_idList.get("1").size() : 0));
        });
    }
}
