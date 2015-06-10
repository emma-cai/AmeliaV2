package rte.answerextraction;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.experiments.GraphExtended;
import rte.graphmatching.NodeComparer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qingqingcai on 6/5/15.
 */
public class TrainingDataGeneration {

    private static List<String> FEALIST = new ArrayList<>();

    public static void main(String[] args) {

        run();
    }

    public static void run() {

        String rawXMLPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.xls";
        String trainXMLPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.txt";
        String trainArffPath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.train.arff";
        String sheetname = "MIT99-trek8";
        List<RTEData> dataList = new ArrayList<>();

        featureGeneration(rawXMLPath, sheetname, dataList);

        System.out.println("===========================================================");
        saveFeatureToFile(trainXMLPath, sheetname, dataList);
        writeFeatureToTxtFile(trainXMLPath, dataList, true);
        writeFeatureToArffFile(trainArffPath, dataList, false);
    }

    public static void saveFeatureToFile(
            String trainXMLPath, String sheetname, List<RTEData> dataList) {

        featureNormalization(dataList);
        for (RTEData data : dataList) {

            if (data.answer.isEmpty())
                continue;

            HashMap<String, String> feamap = data.feamap;
            data.sparsefeamap = new HashMap<>();
            System.out.println();
            System.out.println(data.id);
            System.out.println(data.question);
            System.out.println(data.positive);
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

    public static void featureGeneration(
            String xmlpath, String sheetname, List<RTEData> dataList) {

        readExcel(xmlpath, sheetname, dataList);
        for (RTEData data : dataList) {
            String id = data.id;
            String ques = data.question;
            String positive = data.positive;
            String answer = data.answer;
            String quesConllx = data.conllxQ;
            String textConllx = data.conllxP;
            Graph graphH = Graph.conllxToGraph(quesConllx);
            Graph graphP = Graph.conllxToGraph(textConllx);

            if (answer.isEmpty())
                continue;

            List<DNode> ansNodesList1 = GraphExtended.getNodeList(graphP, answer);
            String ansPosStr = GraphExtended.getFieldStr(ansNodesList, "pos");
            String ansDepStr = GraphExtended.getFieldStr(ansNodesList, "dep");
            String ansLemStr = GraphExtended.getFieldStr(ansNodesList, "lemma").replaceAll("_", " ");
            DNode whNode = graphH.getFirstNodeWithPosTag(NodeComparer.WhSet);

            if (whNode == null)
                continue;

            List<List<DNode>> answerDNodeCandidateList = Prediction.generateAnswerCandidates(ques, positive, graphH, graphP);
            for (String answerCandidate : answerCandidateList) {
                if (answerCandidate.contains(ansLemStr))
                    data.label = "1";
                else
                    data.label = "0";
            }

            String whForm = whNode.getForm();

            DNode lcaNode = graphP.getLowestCommonAncestor(ansNodesList);
            String lcaPosStr = GraphExtended.getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "pos");
            String lcaDepStr = GraphExtended.getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "dep");

            data.feamap.put("w_a_pos", "w_a_pos=" + whForm + "-" + ansPosStr);
            data.feamap.put("w_a_dep", "w_a_dep=" + whForm + "-" + ansDepStr);
            data.feamap.put("w_l_pos", "w_l_pos=" + whForm + "-" + lcaPosStr);
            data.feamap.put("w_l_dep", "w_l_dep=" + whForm + "-" + lcaDepStr);

            System.out.println(id);
            System.out.println(ques);
            System.out.println(positive);
            System.out.println(answer);
            System.out.println(whNode.getForm() + "-" + ansPosStr);
            System.out.println(whNode.getForm() + "-" + ansDepStr);
            System.out.println(whNode.getForm() + "-" + lcaPosStr);
            System.out.println(whNode.getForm() + "-" + lcaDepStr);
            System.out.println();
        }
    }

    public static void readExcel(String filepath, String sheetname, List<RTEData> dataList) {

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
                        RTEData data = new RTEData(id, ques, text, expAns, "");
                        data.setConllxQ(quesConllx);
                        data.setConllxP(textConllx);
                        dataList.add(data);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                    sb.append("1" + " ");
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

    /**
     * In Arff file, saveAsSparse is default as false;
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
}
