package rte.examples;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import rte.answerextraction_tmp.RTEData;
import rte.classifier.SAEClassifier;
import rte.classifier.SparkLibSVM;

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

//        SparkConf conf = new SparkConf().setAppName("SVM Classifier")
//                .setMaster("local[4]");
//        SparkContext sc = new SparkContext(conf);
        SparkLibSVM sparkLibSVM = new SparkLibSVM();
        sparkLibSVM.init();

        String trainName = "MIT99";
        String testName = "cmuwiki";
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
        sparkLibSVM.trainModel(trainpath);

//        HashMap<String, Object> evalMetrix = sparkLibSVM.testModel(testpath, svmModel);
//        System.out.println(evalMetrix.get(SAEClassifier.AREAUNDERROC + " = " + evalMetrix.get(SAEClassifier.AREAUNDERROC)));
//        System.out.println(evalMetrix.get(SAEClassifier.AREAUNDERPR + " = " + evalMetrix.get(SAEClassifier.AREAUNDERPR)));
//        System.out.println(evalMetrix.get(SAEClassifier.PRCURVE + " = " + evalMetrix.get(SAEClassifier.PRCURVE)));

        // feature vector, no label included
        double[] features = {0, 5.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Vector featureVector = Vectors.dense(features);
        double score = sparkLibSVM.predict(featureVector);
        System.out.println("score = " + score);
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
