package rte.answerextraction;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rte.datastructure.Graph;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 6/2/15.
 */
public class DataCollection {

    public static boolean LOWERCASE = true;
    public static void main (String[] args) {

        String inputpath = "data/rte/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml";
        String outputpath = "data/rte/MIT99.xls";
        String sheetname = "MIT99-trek8";
        List<RTEData> dataList = readXML(inputpath);
        writeToExcel(outputpath, sheetname, dataList);
    }

    /** **************************************************************
     * Write extracted data to Excel
     */
    public static void writeToExcel(String filepath, String sheetname, List<RTEData> dataList) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (RTEData data : dataList) {

            // only save the (query, text) pairs with correct answers
            Row row = sheet.createRow(rownum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("1");

            cell = row.createCell(2);
            cell.setCellValue(data.query);

            cell = row.createCell(3);
            cell.setCellValue(data.text);

            cell = row.createCell(4);
            cell.setCellValue(data.answer);

            cell = row.createCell(5);
            cell.setCellValue(data.conllxQ);

            cell = row.createCell(6);
            cell.setCellValue(data.conllxT);

            /**
            // for negative instances
            row = sheet.createRow(rownum++);
            cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("0");

            cell = row.createCell(2);
            cell.setCellValue(data.question);

            cell = row.createCell(3);
            cell.setCellValue(data.negative);

            cell = row.createCell(4);
            cell.setCellValue("");

            cell = row.createCell(5);
            cell.setCellValue(data.conllxQ);

            cell = row.createCell(6);
            cell.setCellValue(data.conllxN);
             **/
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
     * Read Trec data (download from Xuchen's 2013 ACL paper)
     */
    public static List<RTEData> readXML(String filepath) {

        List<RTEData> dataList = new ArrayList<>();

        String pairStart = "<QApairs id=";
        String pairEnd = "</QApairs>";
        String questionTag = "<question>";
        String positiveStart = "<positive>";
        String positiveEnd = "</positive>";
        String negativeStart = "<negative>";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line = null;
            String id = "";
            String question = "";
            String positive = "";
            String answer = "";
            String negative = "";           // NOT USED IN CURRENT VERSION
            while ((line = br.readLine()) != null) {

                if (line.startsWith(pairStart)) {
                    boolean positivestarted = true;
                    boolean negativestarted = true;
                    if (!id.isEmpty()) {
                        RTEData data = new RTEData(id, question, positive, answer);
                        data.setConllxQ(Graph.textToConllx(question));
                        data.setConllxT(Graph.textToConllx(positive));
                //        data.setConllxN(Graph.textToConllx(negative));
                        dataList.add(data);
                    }

                    id = line.substring(pairStart.length()+1, line.length()-2);
                    while (!(line = br.readLine()).equals(pairEnd)) {
                        if (line.startsWith(questionTag)) {
                            question = br.readLine().trim().replaceAll("\t", " ");
                            question = LOWERCASE ? question.toLowerCase() : question;
                        }
                        if (line.startsWith(positiveStart) && positivestarted == true) {
                            positivestarted = false;
                            positive = br.readLine().trim().replaceAll("\t", " ");
                            positive = LOWERCASE ? positive.toLowerCase() : positive;
                            String prev = line;
                            while (!(line = br.readLine()).equals(positiveEnd)) {
                                prev = line;
                            }
                            answer = prev.trim().replaceAll("\t", " ");
                        }
                        if (line.startsWith(negativeStart) && negativestarted == true) {
                            negativestarted = false;
                            negative = br.readLine().trim().replaceAll("\t", " ");
                        }
                    }
                }
            }

            RTEData data = new RTEData(id, question, positive, answer);
            data.setConllxQ(Graph.textToConllx(question));
            data.setConllxT(Graph.textToConllx(positive));
        //    data.setConllxN(Graph.textToConllx(negative));
            dataList.add(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
