package rte.answerextraction;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import rte.datastructure.Graph;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by qingqingcai on 6/2/15.
 */
public class DataCollection {

    public static boolean LOWERCASE = false;
    public static void main (String[] args) {

        String trainName = "MIT99";
        String trainInputPath = "data/rte/jacana-qa-naacl2013-data-results/train2393.cleanup.xml";
    //    String trainInputPath = "data/rte/original-trec-smallexamples.xml";
        String trainOutputPath = "data/rte/" + trainName + ".xls";
        String trainSheetName = trainName;
        List<RTEData> trainDataList = readXML(trainInputPath);
        writeToExcel(trainOutputPath, trainSheetName, trainDataList);

        String testName = "cmuwiki";
        String testInputPath = "data/rte/" + testName + ".json";
        String testOutputPath = "data/rte/" + testName + ".xls";
        String testSheetName = testName;
        List<RTEData> testDataList = readJSON(testInputPath);
        writeToExcel(testOutputPath, testSheetName, testDataList);
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

            // id \t label \t query \t text \t answerid \t answer \t conllxQ \t conllxT
            int colIndex = 0;
            Cell cell = row.createCell(colIndex++);
            cell.setCellValue(data.id);

            cell = row.createCell(colIndex++);
            cell.setCellValue("1");

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.query);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.text);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.answerid);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.answer);

            cell = row.createCell(colIndex++);
            cell.setCellValue(data.conllxQ);

            cell = row.createCell(colIndex++);
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
            String answerid = "";
            String negative = "";           // NOT USED IN CURRENT VERSION
            while ((line = br.readLine()) != null) {

                if (line.startsWith(pairStart)) {
                    boolean positivestarted = true;
                    boolean negativestarted = true;
                    if (!id.isEmpty()) {
                        RTEData data = new RTEData(id, "", question, positive, answerid, answer);
                        data.setConllxQ(Graph.textToConllx(question));
                        data.setConllxT(Graph.textToConllx(positive));
                //        data.setConllxN(Graph.textToConllx(negative));
                        System.out.println(data.id + "\n" + data.query + "\n" + data.answer + "\n");
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
                            String twoStepPrev = line;
                            String prev = line;
                            while (!(line = br.readLine()).equals(positiveEnd)) {
                                twoStepPrev = prev;
                                prev = line;
                            }
                            answer = twoStepPrev.trim().replaceAll("\t", " ");
                            int indexOfSplit = answer.indexOf("#");
                            answer = (indexOfSplit == -1) ? answer : answer.substring(0, indexOfSplit).trim();

                            answerid = prev.trim().replaceAll("\t", " ");
                            indexOfSplit = answerid.indexOf("#");
                            answerid = (indexOfSplit == -1) ? answerid : answerid.substring(0, indexOfSplit).trim();
                        }
                        if (line.startsWith(negativeStart) && negativestarted == true) {
                            negativestarted = false;
                            negative = br.readLine().trim().replaceAll("\t", " ");
                        }
                    }
                }
            }

            RTEData data = new RTEData(id, "", question, positive, answerid, answer);
            data.setConllxQ(Graph.textToConllx(question));
            data.setConllxT(Graph.textToConllx(positive));
        //    data.setConllxN(Graph.textToConllx(negative));
            System.out.println(data.id + "\n" + data.query + "\n" + data.answerid + "\n");
            dataList.add(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    /** **************************************************************
     * Read testing data from IPSoft corpus test; Store graph;
     */
    public static List<RTEData> readJSON(String filepath) {

        List<RTEData> dataList = new ArrayList<>();
        JSONParser paser = new JSONParser();
        try {
            JSONArray array = (JSONArray) paser.parse(new FileReader(filepath));

            Iterator<JSONObject> iter = array.iterator();

            int id = 1;
            while (iter.hasNext()) {
                JSONObject obj = iter.next();
                String query = (String) obj.get("query");
                query = LOWERCASE ? query.toLowerCase() : query;
                String longanswer = (String) obj.get("answer");
                longanswer = LOWERCASE ? longanswer.toLowerCase() : longanswer;
                String shortanswer = (String) obj.get("optimal_answer");

                RTEData data = new RTEData(Integer.toString(id++), "", query, longanswer, "", shortanswer);
                data.setConllxQ(Graph.textToConllx(query));
                data.setConllxT(Graph.textToConllx(longanswer));

                dataList.add(data);

                System.out.println("\nid = " + data.id);
                System.out.println("query = " + data.query);
                System.out.println("longanswer = " + data.text);
                System.out.println("shortanswer = " + data.answer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}
