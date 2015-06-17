package rte.answerextraction_tmp;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;

import java.io.*;
import java.util.*;

import static rte.answerextraction_tmp.AnswerExtractionUtil.*;

/**
 * Created by qingqingcai on 6/10/15.
 */
public class AnswerExtractionDataPrepare {

//    private static List<String> FEALIST = new ArrayList<>();
    // feature_index (fi) -> feature_name (fn)
    private static HashMap<Integer, String> FITOFN = new HashMap<>();
    private static List<String> POSFILTERLIST = new ArrayList<>(Arrays.asList(",", "", "``", "''"));
    private static int POSNUM = 0;
    private static int NEGNUM = 0;

    public static void generateTrainingData() {

        // For training data
        String trainName = "MIT99.smallexamples";
        String rawXMLPath = "data/rte/" + trainName + ".xls";
        String trainExcelPath = "data/rte/" + trainName + ".train.xls";
        String trainTxtPath = "data/rte/" + trainName + ".train.txt";
        String trainSparkPath = "data/rte/" + trainName + ".train.spark.txt";
        String trainArffPath = "data/rte/" + trainName + ".train.arff";
        String trainSheetName = trainName;

        List<RTEData> trainDataList = readFromXML(rawXMLPath, trainSheetName, true);
        List<RTEData> trainDataWithFeatureList = generateFeatures(trainDataList, true);
        featureNormalization(trainDataWithFeatureList);
        toNumericFeature(trainDataWithFeatureList);

        writeFeatureToExcelFile(trainExcelPath, trainSheetName, trainDataWithFeatureList, true);
        writeFeatureToTxtFile(trainTxtPath, trainDataWithFeatureList, true);
        writeFeatureToArffFile(trainArffPath, trainDataWithFeatureList, false);
        writeFeatureToSparkLabeledPoint(trainSparkPath, trainDataWithFeatureList, false);

        // for testing data
        String testName = "cmuwiki.smallexamples";
        String testPath = "data/rte/" + testName + ".xls";
        String testSparkPath = "data/rte/" + testName + ".test.spark.txt";
        String testExcelPath = "data/rte/" + testName + ".test.xls";
        String testSheetName = testName;

        List<RTEData> testDataList = readFromXML(testPath, testSheetName, false);
        List<RTEData> testDataWithFeatureList = generateFeatures(testDataList, false);
        toNumericFeature(testDataWithFeatureList);

        writeFeatureToExcelFile(testExcelPath, testSheetName, testDataWithFeatureList, true);
        writeFeatureToSparkLabeledPoint(testSparkPath, testDataWithFeatureList, false);
    }

    public static void toNumericFeature(List<RTEData> dataList) {

        for (RTEData data : dataList) {
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

//    /** **************************************************************
//     * Format features based on usage using different learning tools
//     */
//    public static void toSparseFeature(List<RTEData> dataList) {
//
//        for (RTEData data : dataList) {
//
//            HashMap<String, String> feamap = data.feamap;
//            data.sparsefeamap = new HashMap<>();
////            System.out.println();
////            System.out.println(data.id);
////            System.out.println(data.query);
////            System.out.println(data.text);
////            System.out.println(data.answer);
////            System.out.println(data.feamap);
//            for (int i = 0; i < FEALIST.size(); i++) {
//                String f = FEALIST.get(i);
//                boolean found = false;
//                for (String fn : feamap.keySet()) {
//                    String fv = feamap.get(fn);
//                    if (f.equals(fv)) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (found) {
//                    data.sparsefeamap.put(i, 1);
//            //        System.out.print(f + "=" + 1 + "\t");
//                } else {
//            //        System.out.print(f + "=" + 0 + "\t");
//                }
//            }
////            System.out.println();
////
////            System.out.println("=====final result");
////            data.sparsefeamap.forEach((index, value) -> {
////                System.out.println(index + " --> " + value);
////            });
//        }
//    }

    /** **************************************************************
     * Collect all feature values appeared in training data, and save
     * the feature values in FEALIST;
     */
    private static void featureNormalization(List<RTEData> dataList) {

        List<String> fntmp = new ArrayList<>();
        for (RTEData data : dataList) {
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
     * Generate features for each instance in dataList;
     */
    public static List<RTEData> generateFeatures(List<RTEData> dataList, boolean forTrainData) {

        List<RTEData> dataWithFeatureList = new ArrayList<>();

        for (RTEData data : dataList) {
            String id = data.id;
            String ques = data.query;
            String text = data.text;
            String answerid = data.answerid;
            String answer = data.answer;

            String quesConllx = data.conllxQ;
            String textConllx = data.conllxT;
            Graph graphQ = (quesConllx == null || quesConllx.isEmpty())
                    ? Graph.stringToGraph(ques) : Graph.conllxToGraph(quesConllx);
            Graph graphT = (textConllx == null || textConllx.isEmpty())
                    ? Graph.stringToGraph(text) : Graph.conllxToGraph(textConllx);

//            List<DNode> labeledAnsNodeList = GraphExtended.getNodeList(graphT, answer);
//            String ansPosStr = GraphExtended.getFieldStr(labeledAnsNodeList, "pos");
//            String ansDepStr = GraphExtended.getFieldStr(labeledAnsNodeList, "dep");
//            String ansFormStr = GraphExtended.getFieldStr(labeledAnsNodeList, "form").replaceAll("_", " ");

            String labeledAnsFormStr1 = answer;
//            if (forTrainData) {
//                List<DNode> labeledAnsNodeList = getNodeList(graphT, answer);
//                labeledAnsFormStr = getFieldStr(labeledAnsNodeList, "form", null, "_").replaceAll("_", " ");
//            }

            DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);

            if (whNode == null)
                continue;

            String whForm = whNode.getForm();

            List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                    = generateAnswerCandidates(graphQ, graphT);

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
                    HashMap<String, String> feamap = featureGeneration(graphT, graphQ, ansCandNodeList);

                    RTEData dataWithFeature = new RTEData(id, label, ques, text, answerid, answer);
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

    /** **************************************************************
     * Collect all feature values appeared in training data, and save
     * the feature values in FEALIST;
     */
    public static HashMap<String, String> featureGeneration(
            Graph graphT, Graph graphQ, String ansCandStr) {

        List<DNode> ansCandNodeList = getNodeList(graphT, ansCandStr);
        return featureGeneration(graphT, graphQ, ansCandNodeList);
    }

    public static HashMap<String, String> featureGeneration(
            Graph graphT, Graph graphQ, List<DNode> ansCandNodeList) {

        List<String> ansPosList = getFieldList(ansCandNodeList, "pos", POSFILTERLIST);
        List<String> ansDepList = getFieldList(ansCandNodeList, "dep", null);

//        String ansPosStr = getFieldStr(ansCandNodeList, "pos", POSFILTERLIST, "_");
//        String ansDepStr = getFieldStr(ansCandNodeList, "dep", null, "_");
//        String ansLemStr = getFieldStr(ansCandNodeList, "lemma", null, "_").replaceAll("_", " ");

        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);

        if (whNode == null) {
            System.out.println("WARNING: No Wh-words were found!");
            whNode = graphQ.getNodeById(1);
        }

        String whForm = whNode.getForm();
        String whLemma = whNode.getLemma();

        DNode lcaNode = graphT.getLowestCommonAncestor(ansCandNodeList);
        String lcaPosStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "pos", null, "_");
        String lcaDepStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "dep", null, "_");

        HashMap<String, String> feamap = new HashMap<>();

        // # of tokens in answer-candidate
        String fv = Integer.toString(ansCandNodeList.size());
        feamap.put("N:a_TN", fv);

        // overlap of QUERY_LEMMA and TEXT_LEMMA
        fv = Integer.toString(overlap(graphQ, ansCandNodeList));
        feamap.put("N:overlapN", fv);

        // (wh-word-form, 0/1); 0/1 -> Does answer-candidate contain number/CD?
        fv = ansPosList.contains("CD") ? "1" : "0";
        feamap.put("C:whLemma_a_hasCD", whLemma + "-" + fv);

        // (wh-word-lemma, lca-dep)
        feamap.put("C:whLemma_lcaDep", whLemma + "-" + lcaDepStr);



//        // wh-words and lowest-common-ancestor's pos
//        feamap.put("C:w_l_pos", whForm + "-" + lcaPosStr);
//
//        feamap.put("w_a_pos", "w_a_pos=" + whForm + "-" + ansPosStr);
//        feamap.put("w_a_dep", "w_a_dep=" + whForm + "-" + ansDepStr);
//        feamap.put("w_l_pos", "w_l_pos=" + whForm + "-" + lcaPosStr);
//        feamap.put("w_l_dep", "w_l_dep=" + whForm + "-" + lcaDepStr);

        return feamap;
    }

    /**
     *
     * @param data
     * @param graphT
     * @param ansCandStr
     * @return
     */
    private static boolean isPositive(RTEData data, Graph graphT, String ansCandStr, boolean forTraining) {

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

    /** **************************************************************
     * Read testing data from IPSoft corpus test; stored as RTEData;
     */
    public static List<RTEData> readTestData(String filepath) {

        List<RTEData> dataList = new ArrayList<>();
        JSONParser paser = new JSONParser();
        try {
            JSONArray array= (JSONArray) paser.parse(new FileReader(filepath));

            Iterator<JSONObject> iter = array.iterator();

            int id = 0;
            while(iter.hasNext()){
                JSONObject obj = iter.next();
                String query = (String) obj.get("query");
                String longanswer = (String) obj.get("answer");
                String shortanswer = (String) obj.get("optimal_answer");

                boolean yesorno = (shortanswer.toLowerCase().equals("yes"))
                        || (shortanswer.toLowerCase().equals("no"));
                if (!yesorno) {

                    RTEData data = new RTEData(Integer.toString(id++), "", query, longanswer, "", shortanswer);
                    dataList.add(data);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    /** **************************************************************
     * Read training data from prepared XML file; stored as RETData;
     */
    public static List<RTEData> readFromXML(String filepath, String sheetname, boolean forTrainData) {

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
                        RTEData data = new RTEData(id, label, ques, text, answerid, answer);
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
     * Write training data with features to a txt file
     */
    public static void writeFeatureToTxtFile(
            String filepath, List<RTEData> dataList, boolean saveAsSparse) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (RTEData data : dataList) {

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
            String filepath, List<RTEData> dataList, boolean saveAsSparse) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (RTEData data : dataList) {

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
            String filepath, String sheetname, List<RTEData> dataList, boolean saveAsSparse) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (RTEData data : dataList) {

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
            String filepath, List<RTEData> dataList, boolean saveAsSparse) {

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
            for (RTEData data : dataList) {

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

    public static void main(String[] args) {

        generateTrainingData();
    }
}
