package rte.datacollection;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;
import rte.similarityflooding.NodePair;

import java.io.*;
import java.util.*;

import static rte.answerextraction.FeatureExtractor.extractFeatures;
import static rte.answerextraction.FeatureExtractorUtils.*;
import static rte.graphmatching.DMatching.initNodeMatches;

/**
 * Created by qingqingcai on 6/18/15.
 */
public class TrainData {

    public static HashMap<Integer, String> FITOFN = new HashMap<>();
    private static List<String> POSFILTERLIST = new ArrayList<>(Arrays.asList(",", "", "``", "''"));
    private static int POSNUM = 0;
    private static int NEGNUM = 0;

    public static void runTrainData(String[] args) {

        String trainName, testName;
        if (args == null || args.length == 0) {
            trainName = "MIT99";
            testName = "cmuwiki";
        } else {
            trainName = args[0].isEmpty() ? "MIT99" : args[0];
            testName = args[1].isEmpty() ? "cmuwiki" : args[1];
        }

        // For training data
        String rawXMLPath = "data/rte/" + trainName + ".xls";
        String trainExcelPath = "data/rte/" + trainName + ".train.xls";
        String trainTxtPath = "data/rte/" + trainName + ".train.txt";
        String trainSparkPath = "data/rte/" + trainName + ".train.spark.txt";
        String trainArffPath = "data/rte/" + trainName + ".train.arff";
        String trainSheetName = trainName;

        List<SAEData> trainDataList = readRawFromXML(rawXMLPath, trainSheetName, true);
        List<SAEData> trainDataWithFeatureList = generateFeatures(trainDataList, true);
        featureNormalization(trainDataWithFeatureList);
        toNumericFeature(trainDataWithFeatureList);

        writeFeatureToExcelFile(trainExcelPath, trainSheetName, trainDataWithFeatureList, true);
        writeFeatureToTxtFile(trainTxtPath, trainDataWithFeatureList, true);
        writeFeatureToArffFile(trainArffPath, trainDataWithFeatureList, false);
        writeFeatureToSparkLabeledPoint(trainSparkPath, trainDataWithFeatureList, false);

        // for testing data
        String testPath = "data/rte/" + testName + ".xls";
        String testSparkPath = "data/rte/" + testName + ".test.spark.txt";
        String testExcelPath = "data/rte/" + testName + ".test.xls";
        String testSheetName = testName;

        List<SAEData> testDataList = readRawFromXML(testPath, testSheetName, false);
        List<SAEData> testDataWithFeatureList = generateFeatures(testDataList, false);
        toNumericFeature(testDataWithFeatureList);

        writeFeatureToExcelFile(testExcelPath, testSheetName, testDataWithFeatureList, true);
        writeFeatureToSparkLabeledPoint(testSparkPath, testDataWithFeatureList, false);
    }

    /** **************************************************************
     * Generate features for each instance in dataList;
     */
    public static List<SAEData> generateFeatures(List<SAEData> dataList, boolean forTrainData) {

        List<SAEData> dataWithFeatureList = new ArrayList<>();

        for (SAEData data : dataList) {
            String id = data.id;
            String ques = data.query;
            String text = data.text;
            String answerid = data.answerid;
            String answer = data.answer;

            String quesConllx = data.conllxQ;
            String textConllx = data.conllxT;

            if (text.toLowerCase().equals("This helps explain why Peruvian fishermen dubbed the phenomenon El Nino , which means Christ child in Spanish .".toLowerCase()))
                System.out.println("qingqing debug ...");


            Graph graphQ = (quesConllx == null || quesConllx.isEmpty())
                    ? Graph.stringToGraph(ques) : Graph.conllxToGraph(quesConllx);
            Graph graphT = (textConllx == null || textConllx.isEmpty())
                    ? Graph.stringToGraph(text) : Graph.conllxToGraph(textConllx);


            DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);

            if (whNode == null)
                continue;

            String whForm = whNode.getForm();

            HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                    = initNodeMatches(graphT, graphQ, config);
            List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                    = generateAnswerCandidates(whNode, nodeMatches);

            for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {

                String ansCandStr = fromTreeMapToString(ansCandNodeMap);
                List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
                String label = "0";


                boolean isPositiveCandidate = isPositive(data, graphT, ansCandStr, forTrainData);

                boolean addtofile = false;
                if (forTrainData) {
                    if (isPositiveCandidate || (!isPositiveCandidate && NEGNUM <= POSNUM)) {
                        if (isPositiveCandidate) {
                            label = "1";
                            POSNUM++;
                        } else {
                            NEGNUM++;
                        }

                        addtofile = true;
                    }
                } else {
                    if (isPositiveCandidate) {
                        label = "1";
                    }
                    addtofile = true;
                }
                if (addtofile) {
                    HashMap<String, String> feamap = extractFeatures(graphT, graphQ, ansCandNodeList, nodeMatches);

                    SAEData dataWithFeature = new SAEData(id, label, ques, text, answerid, answer);
                    dataWithFeature.setFeaMap(feamap);
                    dataWithFeature.setShortAnswerCandidate(ansCandStr);
                    dataWithFeatureList.add(dataWithFeature);
                    System.out.println("id = " + data.id);
                    System.out.println("label = " + label);
                    System.out.println("ques = " + data.query);
                    System.out.println("text = " + data.text);
                    System.out.println("answerid = " + data.answerid);
                    System.out.println("answer = " + data.answer);
                    System.out.println("ansCandStr = " + dataWithFeature.shortAnswerCandidate);
                    System.out.println("feature = " + dataWithFeature.feamap);
                    System.out.println();
                }
            }
        }

        return dataWithFeatureList;
    }

    private static boolean isPositive(SAEData data, Graph graphT, String ansCandStr, boolean forTraining) {

        String answer = data.answer;

        if (forTraining) {
            String answerid = data.answerid;
            String[] answeridArr = answerid.split(" ");
            String[] answerArr = answer.split(" ");

            // check if answer and answerid are matched
            if (answeridArr.length != answerArr.length)
                return false;
            for (int i = 0; i < answeridArr.length; i++) {
                String aid = answeridArr[i];
                String a = answerArr[i];
                DNode anode = graphT.getNodeById(Integer.parseInt(aid));
                if (anode == null)
                    return false;
                if (!anode.getForm().toLowerCase().equals(a.toLowerCase())) {
                    System.out.println("ERROR: answerid and answer are not matched for " + data.id);
                    return false;
                }
            }
        }

        // check if the ansCandStr contains expected-answer
        if (answer.toLowerCase().equals(ansCandStr.toLowerCase()) ||
                (ansCandStr.toLowerCase().contains(answer.toLowerCase())
                        && (ansCandStr.split(" ").length <= answer.split(" ").length+5)))
            return true;

        return false;
    }

    public static void toNumericFeature(List<SAEData> dataList) {

        for (SAEData data : dataList) {
            HashMap<String, String> feamap = data.feamap;
            data.numericfeamap = new TreeMap<>();

            for (Integer fi : FITOFN.keySet()) {
                String normalizedFN = FITOFN.get(fi);
                if (isNumeric(normalizedFN)) {
                    String fv = feamap.get(normalizedFN);
                    data.numericfeamap.put(fi, Double.parseDouble(fv));
                } else {
                    for (String fntmp : feamap.keySet()) {
                        String fv = feamap.get(fntmp);

                        if (isCategorial(fntmp)) {
                            String newfn = fntmp + ":" + fv;
                            if (normalizedFN.equals(newfn))
                                data.numericfeamap.put(fi, 1.0);
                            else
                                data.numericfeamap.put(fi, 0.0);
                            break;
                        }
                    }
                }

            }
        }
    }

    /** **************************************************************
     * Read training data from prepared XML file; stored as RETData;
     */
    public static List<SAEData> readRawFromXML(String filepath, String sheetname, boolean forTrainData) {

        List<SAEData> dataList = new ArrayList<>();

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
                    String id = readCellValue(row, colIndex++);
                    String label = readCellValue(row, colIndex++);
                    String ques = readCellValue(row, colIndex++);
                    String text = readCellValue(row, colIndex++);
                    String answerid = readCellValue(row, colIndex++);
                    String answer = readCellValue(row, colIndex++);
                    String quesConllx = readCellValue(row, colIndex++);
                    String textConllx = readCellValue(row, colIndex++);

                    boolean yesorno = (answer.toLowerCase().equals("yes"))
                            || (answer.toLowerCase().equals("no"));
                    if ((forTrainData && answer != null)
                            || (!forTrainData && !yesorno)) {
                        SAEData data = new SAEData(id, label, ques, text, answerid, answer);
                        data.setConllxQ(quesConllx);
                        data.setConllxT(textConllx);
                        dataList.add(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public static String readCellValue(HSSFRow row, int colIndex) {

        String value = "";
        Cell cell = row.getCell(colIndex);
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                value = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                value = Double.toString(cell.getNumericCellValue());
                break;
        }
        return value;
    }

    /** **************************************************************
     * Collect all feature values appeared in training data, and save
     * the feature values in FEALIST;
     */
    private static void featureNormalization(List<SAEData> dataList) {

        List<String> fntmp = new ArrayList<>();
        for (SAEData data : dataList) {
            HashMap<String, String> FNTOFV = data.feamap;
            FNTOFV.forEach((fn, fv) -> {
                String newfn = fn;          // for numeric feature, newfn = fn, e.g.
                if (isCategorial(fn))
                    newfn += ":" + fv;
                if (!fntmp.contains(newfn)) {
                    fntmp.add(newfn);
                    FITOFN.put(fntmp.size(), newfn);
                }
            });
        }
    }

    /** **************************************************************
     * Write training data with features to a txt file
     */
    public static void writeFeatureToTxtFile(
            String filepath, List<SAEData> dataList, boolean saveAsSparse) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (SAEData data : dataList) {

                if (data.answer.isEmpty())
                    continue;

                if (saveAsSparse) {
                    TreeMap numericfea = data.numericfeamap;
                    StringBuilder sb = new StringBuilder();
                    sb.append(data.label + " ");
                    numericfea.forEach((findex, fvalue) -> {
                        sb.append(findex + ":" + fvalue + " ");
                    });
                    String row = sb.toString().trim();
                    bw.write(row);
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Write training data with features to a labeled point file which
     * will be used in apache mllib
     */
    public static void writeFeatureToSparkLabeledPoint(
            String filepath, List<SAEData> dataList, boolean saveAsSparse) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (SAEData data : dataList) {

                TreeMap<Integer, Double> numericfea = data.numericfeamap;
                StringBuilder sb = new StringBuilder();
                sb.append(data.label).append(",");
                numericfea.forEach((findex, fvalue) -> {
                    if (Double.compare(fvalue, 0.0) == 0)
                        sb.append("0").append(" ");
                    else if (Double.compare(fvalue, 1.0) == 0)
                        sb.append("1").append(" ");
                    else
                        sb.append(fvalue).append(" ");
                });
                String row = sb.toString().trim();
                bw.write(row);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Write training data with features to a txt file
     */
    public static void writeFeatureToExcelFile(
            String filepath, String sheetname, List<SAEData> dataList, boolean saveAsSparse) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (SAEData data : dataList) {

            // only save the (query, text) pairs with correct answers
            Row row = sheet.createRow(rownum++);

            int colIndex = 0;
            Cell cell = row.createCell(colIndex++);
            cell.setCellValue(data.id);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.label);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.query);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.text);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.answerid);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.answer);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.shortAnswerCandidate);

            for (String fn : data.feamap.keySet()) {
                cell = row.createCell(colIndex++);
                cell.setCellValue(data.feamap.get(fn));
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(new File(filepath));
            workbook.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Write training data with features to an arff file; this will be
     * used in weka package;
     */
    public static void writeFeatureToArffFile(
            String filepath, List<SAEData> dataList, boolean saveAsSparse) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("@relation breast-cancer");
            bw.newLine();
            for (Integer fi : FITOFN.keySet()) {
                String fn = FITOFN.get(fi);
                if (isNumeric(fn)) {
                    bw.write("@attribute " + fn + " numeric");
                } else if (isCategorial(fn)) {
                    bw.write("@attribute " + fn + " {0,1}");
                }
                bw.newLine();
            }
            bw.write("@attribute 'Class' {0,1}");
            bw.newLine();
            bw.write("@data");
            bw.newLine();

            int negnum = 0;
            for (SAEData data : dataList) {

                if ((data.label.equals("0") && negnum <= POSNUM)
                        || data.label.equals("1")) {

                    if (data.label.equals("0"))
                        negnum++;
                    TreeMap<Integer, Double> numericfea = data.numericfeamap;
                    StringBuilder sb = new StringBuilder();

                    for (Integer fi : numericfea.keySet()) {
                        Double fv = numericfea.get(fi);
                        if (Double.compare(fv, 0.0) == 0)
                            sb.append("0").append(",");
                        else if (Double.compare(fv, 1.0) == 0)
                            sb.append("1").append(",");
                        else
                            sb.append(fv).append(",");
                    }
                    sb.append(data.label);
                    String row = sb.toString().trim();
                    bw.write(row);
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
