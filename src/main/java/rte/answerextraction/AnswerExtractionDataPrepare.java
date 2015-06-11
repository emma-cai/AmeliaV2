package rte.answerextraction;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;

import java.io.*;
import java.util.*;

import static rte.answerextraction.AnswerExtractionUtil.*;

/**
 * Created by qingqingcai on 6/10/15.
 */
public class AnswerExtractionDataPrepare {

    private static List<String> FEALIST = new ArrayList<>();

    public static void generateTrainingData() {

        String rawXMLPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.xls";
        String trainTxtPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.txt";
        String trainArffPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.arff";
        String sheetname = "MIT99-trek8";

        List<RTEData> dataList = new ArrayList<>();
        List<RTEData> dataWithFeatureList = featureGeneration(rawXMLPath, sheetname, dataList);
        featureFormatting(dataWithFeatureList);

        writeFeatureToTxtFile(trainTxtPath, dataWithFeatureList, true);
        writeFeatureToArffFile(trainArffPath, dataWithFeatureList, false);
    }

    /** **************************************************************
     * Format features based on usage using different learning tools
     */

    public static void featureFormatting(List<RTEData> dataList) {

        featureNormalization(dataList);
        for (RTEData data : dataList) {

            if (data.answer.isEmpty())
                continue;

            HashMap<String, String> feamap = data.feamap;
            data.sparsefeamap = new HashMap<>();
            System.out.println();
            System.out.println(data.id);
            System.out.println(data.query);
            System.out.println(data.text);
            System.out.println(data.answer);
            System.out.println(data.feamap);
            for (int i = 0; i < FEALIST.size(); i++) {
                String f = FEALIST.get(i);
                boolean found = false;
                for (String fn : feamap.keySet()) {
                    String fv = feamap.get(fn);
                    if (f.equals(fv)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    data.sparsefeamap.put(i, 1);
                    System.out.print(f + "=" + 1 + "\t");
                } else {
                    System.out.print(f + "=" + 0 + "\t");
                }
            }
            System.out.println();

            System.out.println("=====final result");
            data.sparsefeamap.forEach((index, value) -> {
                System.out.println(index + " --> " + value);
            });
        }
    }

    /** **************************************************************
     * Collect all feature values appeared in training data, and save
     * the feature values in FEALIST;
     */
    private static void featureNormalization(List<RTEData> dataList) {

        for (RTEData data : dataList) {
            HashMap<String, String> feamap = data.feamap;
            feamap.forEach((fn, fv) -> {
                if (!FEALIST.contains(fv)) {
                    FEALIST.add(fv);
                }
            });
        }
    }

    /** **************************************************************
     * Generate features for each instance in dataList;
     */
    public static List<RTEData> featureGeneration(
            String xmlpath, String sheetname, List<RTEData> dataList) {

        List<RTEData> dataWithFeatureList = new ArrayList<>();

        readExcel(xmlpath, sheetname, dataList);
        for (RTEData data : dataList) {
            String id = data.id;
            String ques = data.query;
            String text = data.text;
            String answer = data.answer;
            String quesConllx = data.conllxQ;
            String textConllx = data.conllxT;
            Graph graphQ = Graph.conllxToGraph(quesConllx);
            Graph graphT = Graph.conllxToGraph(textConllx);

            if (answer.isEmpty())
                continue;

//            List<DNode> labeledAnsNodeList = GraphExtended.getNodeList(graphT, answer);
//            String ansPosStr = GraphExtended.getFieldStr(labeledAnsNodeList, "pos");
//            String ansDepStr = GraphExtended.getFieldStr(labeledAnsNodeList, "dep");
//            String ansFormStr = GraphExtended.getFieldStr(labeledAnsNodeList, "form").replaceAll("_", " ");

            List<DNode> labeledAnsNodeList = getNodeList(graphT, answer);
            String labeledAnsFormStr = getFieldStr(labeledAnsNodeList, "form").replaceAll("_", " ").toLowerCase();

            DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);

            if (whNode == null)
                continue;

            String whForm = whNode.getForm();

            List<TreeMap<Integer, DNode>> ListOfAnsCandNodeMap
                    = generateAnswerCandidates(graphQ, graphT);

            for (TreeMap<Integer, DNode> ansCandNodeMap : ListOfAnsCandNodeMap) {
                String ansCandStr = fromTreeMapToString(ansCandNodeMap).toLowerCase();
                List<DNode> ansCandNodeList = new ArrayList(ansCandNodeMap.values());
                String label = "0";
                if (ansCandStr.contains(labeledAnsFormStr))
                    label = "1";

                HashMap<String, String> feamap = featureGeneration(graphT, graphQ, ansCandNodeList);

                RTEData dataWithFeature = new RTEData(id, label, ques, text, answer, quesConllx, textConllx, feamap);
                dataWithFeatureList.add(dataWithFeature);
                System.out.println("id = " + data.id);
                System.out.println("label = " + data.label);
                System.out.println("ques = " + data.query);
                System.out.println("text = " + data.text);
                System.out.println("answer = " + data.answer);
                System.out.println("ansCandStr = " + ansCandStr);
//                data.feamap.forEach((fn, fv) -> {
//                    System.out.println(fn + ": " + fv);
//                });
                System.out.println();
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

        String ansPosStr = getFieldStr(ansCandNodeList, "pos");
        String ansDepStr = getFieldStr(ansCandNodeList, "dep");
        String ansLemStr = getFieldStr(ansCandNodeList, "lemma").replaceAll("_", " ");
        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);

        if (whNode == null) {
            System.out.println("WARNING: No Wh-words were found!");
            whNode = graphQ.getNodeById(1);
        }

        String whForm = whNode.getForm();

        DNode lcaNode = graphT.getLowestCommonAncestor(ansCandNodeList);
        String lcaPosStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "pos");
        String lcaDepStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "dep");

        HashMap<String, String> feamap = new HashMap<>();

        feamap.put("w_a_pos", "w_a_pos=" + whForm + "-" + ansPosStr);
        feamap.put("w_a_dep", "w_a_dep=" + whForm + "-" + ansDepStr);
        feamap.put("w_l_pos", "w_l_pos=" + whForm + "-" + lcaPosStr);
        feamap.put("w_l_dep", "w_l_dep=" + whForm + "-" + lcaDepStr);

//        System.out.println(whNode.getForm() + "-" + ansPosStr);
//        System.out.println(whNode.getForm() + "-" + ansDepStr);
//        System.out.println(whNode.getForm() + "-" + lcaPosStr);
//        System.out.println(whNode.getForm() + "-" + lcaDepStr);
//        System.out.println();
        return feamap;
    }

    /** **************************************************************
     * Read data information from prepared XML file; store as RETData
     */
    public static void readExcel(
            String filepath, String sheetname, List<RTEData> dataList) {

        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filepath));
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workbook.getSheet(sheetname);
            HSSFRow row;

            int rows = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    String id = row.getCell(0).getStringCellValue().toLowerCase();
                    String ques = row.getCell(2).getStringCellValue().toLowerCase();
                    String text = row.getCell(3).getStringCellValue().toLowerCase();
                    String expAns = row.getCell(4) == null? null : row.getCell(4).getStringCellValue().toLowerCase();
                    String quesConllx = row.getCell(5).getStringCellValue();
                    String textConllx = row.getCell(6).getStringCellValue();

                    if (expAns != null) {
                        RTEData data = new RTEData(id, ques, text, expAns);
                        data.setConllxQ(quesConllx);
                        data.setConllxT(textConllx);
                        dataList.add(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Write training data with features to an xml file
     */
    public static void writeFeatureToXMLFile(
            String filepath, String sheetname,
            List<RTEData> dataList, boolean saveAsSparse) {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (RTEData data : dataList) {

            Row row = sheet.createRow(rownum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(data.id);
            cell = row.createCell(1);
            cell.setCellValue("1");           // label as 1; positive examples
            int colNum = FEALIST.size();
            for (int i = 0; i < colNum; i++) {
                cell = row.createCell(i+2);     // IMPORTANT
                if (data.sparsefeamap.containsKey(i))
                    cell.setCellValue(data.sparsefeamap.get(i));
                else
                    cell.setCellValue(0);
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
                    HashMap sparsefea = data.sparsefeamap;
                    StringBuilder sb = new StringBuilder();
                    sb.append(data.label + " ");
                    sparsefea.forEach((findex, fvalue) -> {
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
            for (String fea : FEALIST) {
                bw.write("@attribute " + fea + " {'0','1'}");
                bw.newLine();
            }
            bw.write("@attribute 'Class' {'0','1'}");
            bw.newLine();
            bw.write("@data");
            bw.newLine();

            for (RTEData data : dataList) {

                if (data.answer.isEmpty())
                    continue;

                HashMap sparsefea = data.sparsefeamap;
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < FEALIST.size(); i++) {
                    if (sparsefea.containsKey(i))
                        sb.append("'1',");
                    else
                        sb.append("'0',");
                }
                sb.append("'1'");
                String row = sb.toString().trim();
                bw.write(row);
                bw.newLine();
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
