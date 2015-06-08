package development.textdatacollection;

import bpn.utils.Buckets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static bpn.utils.Fun.saveToFile;

/**
 * Created by qingqingcai on 5/12/15.
 */
public class Performance {

    public static void main(String[] args) {

        String golddatapath = "data/bpn/text/bpn_test.txt";
    //    String testrespath = "data/bpn/result/libSVM/bpn_test_libsvm.res";
        String testrespath = "data/bpn/result/DAClassifier/bpn_test_da.res";

        HashMap<String, Double> perform = new HashMap<>();
        double precision = computePerformance(golddatapath, testrespath);
    }

    public static double computePerformance(String golddatapath, String testrespath) {

        double precision = 0.0;

        try {
            List<String> goldLines = Files.readAllLines(Paths.get(golddatapath), StandardCharsets.UTF_8);
            List<String> testLines = Files.readAllLines(Paths.get(testrespath), StandardCharsets.UTF_8);
            HashMap<String, String> instance_goldLabel = new HashMap<>();
            HashMap<String, String> instance_testLabel = new HashMap<>();
            goldLines.forEach(l -> {
                int index = l.indexOf("\t");
                String label = l.substring(0, index);
                String instance = l.substring(index+1);
                instance_goldLabel.put(instance, label);
            });

            testLines.forEach(l -> {
                int index = l.indexOf("\t");
                String label = l.substring(0, index);
                String instance = l.substring(index+1);
                instance_testLabel.put(instance, label);
            });

            final int[] TOTAL = {0};
            final int[] TP = {0};
            int FP = 0;
            int TN = 0;
            int FN = 0;
            instance_goldLabel.forEach((ins, gl) -> {
                if (Buckets.FOCUSED_ON.contains(gl)) {
                    String tl = instance_testLabel.get(ins);
                    System.out.println(TOTAL[0] + "\t" + gl + "\t\t" + tl + "\t\t" + ins);
                    if (gl.contains(tl)) {
                        TP[0]++;
                    }
                    TOTAL[0]++;
                }
            });
            precision = (TP[0] * 1.0) / (TOTAL[0] * 1.0);
            System.out.println("TP = " + TP[0]);
            System.out.println("TOTAL = " + TOTAL[0]);
            System.out.println("precision = " + precision);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return precision;
    }

    public static void test() {

        String ifilepath = "data/bpn/result/DAClassifier/bpn_test_tags.original.txt";
        String ofilepath = "data/bpn/result/DAClassifier/bpn_test_da.res";
        try {
            List<String> originalList = Files.readAllLines(Paths.get(ifilepath), StandardCharsets.UTF_8);
            ArrayList<String> newformatList = new ArrayList<>();
            originalList.forEach(l -> {
                int index = l.indexOf(":");
                String label = l.substring(0, index);
                String stmt = l.substring(index+1, l.length());
                String newformat = label + "\t" + stmt;
                if (!newformatList.contains(newformat))
                    newformatList.add(newformat);
            });
            saveToFile(ofilepath, newformatList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
