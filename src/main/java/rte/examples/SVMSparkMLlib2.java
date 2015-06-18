package rte.examples;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import rte.answerextraction_tmp.RTEData;
import rte.classifier.SAEClassifier;
import rte.classifier.SparkLibSVM;
import scala.Tuple2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qingqingcai on 6/15/15.
 */
public class SVMSparkMLlib2 {

    public static void main(String[] args) {

        SparkLibSVM sparkLibSVM = new SparkLibSVM();
        sparkLibSVM.init();

        String trainName = "MIT99.smallexamples";
        String testName = "cmuwiki.smallexamples";
        String trainpath = "data/rte/" + trainName + ".train.spark.txt";
        String testpath = "data/rte/" + testName + ".test.spark.txt";
        String testExcelPath = "data/rte/" + testName + ".test.xls";
        String testSheetName = testName;
        List<RTEData> testDataList = readFromXML(testExcelPath, testSheetName);

        HashMap<String, Object> context = new HashMap() {
            {
                put(SAEClassifier.ITERATION, 50);
                put(SAEClassifier.THRESHOLD, Double.MAX_VALUE);
            }
        };
        sparkLibSVM.setConf(context);
        SVMModel svmModel = (SVMModel) sparkLibSVM.trainModel(trainpath);

        HashMap<String, Object> evalMetrix = testModel(testpath, testDataList, sparkLibSVM.getSparkContext(), svmModel);
        System.out.println(SAEClassifier.AREAUNDERROC + " = " + evalMetrix.get(SAEClassifier.AREAUNDERROC));
        System.out.println(SAEClassifier.AREAUNDERPR + " = " + evalMetrix.get(SAEClassifier.AREAUNDERPR));
        System.out.println(SAEClassifier.PRCURVE + " = " + evalMetrix.get(SAEClassifier.PRCURVE));

        // feature vector, no label included
//        double[] features = {0, 5.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        Vector featureVector = Vectors.dense(features);
//        double score = sparkLibSVM.predict(svmModel, featureVector);
//        System.out.println("score = " + score);
    }

    public static HashMap<String, Object> testModel(
            String testpath, List<RTEData> testDataList, SparkContext sc, SVMModel svmModel) {

        HashMap<String, Object> evalMetrix = new HashMap<>();
        if (svmModel == null)
            throw new IllegalStateException("SVMModel is NULL!");
        JavaRDD<LabeledPoint> test = MLUtils.loadLabeledData(sc, testpath).toJavaRDD().cache();
        System.out.println("debug: " + test.collect().size());

        // Tuple2._1() is the predictive score = W^T * X
        // Tuple2._2() is the golden standard label
        // By default (if call clearThreshold()), if (W^T * X) > 0, then output is positive
        JavaRDD<Tuple2<Object, Object>> scoreAndLabels = test.map(
                new Function<LabeledPoint, Tuple2<Object, Object>>() {
                    public Tuple2<Object, Object> call(LabeledPoint p) {
                        Double score = svmModel.predict(p.features());
                        return new Tuple2<Object, Object>(score, p.label());
                    }
                }
        );
        BinaryClassificationMetrics metrics =
                new BinaryClassificationMetrics(JavaRDD.toRDD(scoreAndLabels));
        double auROC = metrics.areaUnderROC();
        double auPR = metrics.areaUnderPR();
        JavaRDD<Tuple2<Object, Object>> pr = metrics.pr().toJavaRDD().cache();  // return the precision-recall curve, which is an RDD of (recall, precision)
        evalMetrix.put(SAEClassifier.AREAUNDERROC, auROC);
        evalMetrix.put(SAEClassifier.AREAUNDERPR, auPR);
        evalMetrix.put(SAEClassifier.PRCURVE, pr);

        final int[] instanceIndex = {0};
        final int[] TP = {0};
        final int[] FP = {0};
        final int[] TN = {0};
        final int[] FN = {0};
        final int[] testID = {0};
        scoreAndLabels.collect().forEach(tuple2 -> {

            RTEData data = testDataList.get(testID[0]);
            System.out.println("\nid = " + data.getID() + "\n"
                    + "query = " + data.getQuery() + "\n"
                    + "answer = " + data.getAnswer() + "\n"
                    + "shcand = " + data.getShortAnswerCandidate() + "\n"
                    + "label = " + data.getLabel() + "\n"
                    + "feamap = " + data.getFeamap());


            String label = Double.toString((Double) tuple2._2());
            Double confidence = (Double) tuple2._1();
            if (label.equals("1.0") && data.getLabel().equals("1")) {

                System.out.println("TP:\t" + label + "\t" + confidence);
                TP[0]++;
            } else if (label.equals("1.0") && data.getLabel().equals("0")) {
                System.out.println("FP:\t" + label + "\t" + confidence);
                FP[0]++;
            } else if (label.equals("0.0") && data.getLabel().equals("0")) {
                System.out.println("TN:\t" + label + "\t" + confidence);
                TN[0]++;
            } else if (label.equals("0.0") && data.getLabel().equals("1")) {
                System.out.println("FN:\t" + label + "\t" + confidence);
                FN[0]++;
            }

            testID[0]++;
        });


        return evalMetrix;
    }

    /** **************************************************************
     * Link the prediction to original testing instance/data;
     */
    public static List<RTEData> readFromXML(String filepath, String sheetname) {

        List<RTEData> dataList = new ArrayList<>();

        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filepath));
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workbook.getSheet(sheetname);
            HSSFRow row;

            int rows = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < rows; i++) {

                row = sheet.getRow(i);
                if (row != null) {
                    int colIndex = 0;
                    String id = row.getCell(colIndex++).getStringCellValue();
                    String label = row.getCell(colIndex++).getStringCellValue();
                    String ques = row.getCell(colIndex++).getStringCellValue();
                    String text = row.getCell(colIndex++).getStringCellValue();
                    String answerid = row.getCell(colIndex++).getStringCellValue();
                    String answer = row.getCell(colIndex++).getStringCellValue();
                    String saCand = row.getCell(colIndex++).getStringCellValue();

                    int feaStart = 6;
                    int feaTotal = row.getPhysicalNumberOfCells();
                    HashMap<String, String> feamap = new HashMap<>();
                    for (int feaIndex = feaStart; feaIndex < feaTotal; feaIndex++) {
                        String feaValue = row.getCell(feaIndex).getStringCellValue();
                        feamap.put(Integer.toString(feaIndex), feaValue);
                    }
                    String quesConllx = row.getCell(5).getStringCellValue();
                    String textConllx = row.getCell(6).getStringCellValue();

                    RTEData data = new RTEData(id, label, ques, text, answerid, answer);
                    data.setShortAnswerCandidate(saCand);
                    data.setFeaMap(feamap);
                    data.setLabel(label);

                    dataList.add(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
