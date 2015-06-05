package rte.learning;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rte.datastructure.DNode;
import rte.datastructure.Graph;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 6/2/15.
 */
public class DataCollection {

    public static void main (String[] args) {

        String inputpath = "data/rte/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml";
        String outputpath = "data/rte/MIT99.xls";
        String sheetname = "MIT99-trek8";
        List<Data> dataList = readXML(inputpath);
        writeToExcel(outputpath, sheetname, dataList);

        readExcel(outputpath, sheetname, null);
    }

    public static void readExcel(String filepath, String sheetname, List<Data> data) {

        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filepath));
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workbook.getSheet(sheetname);
            HSSFRow row;
            HSSFCell cell;

            int rows = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    String ques = row.getCell(2).getStringCellValue();
                    String text = row.getCell(3).getStringCellValue();
                    Graph graph_Q = Graph.stringToGraph(ques);
                    Graph graph_T = Graph.stringToGraph(text);
//
//                    int cols = row.getPhysicalNumberOfCells();
//                    for (int j = 0; j < cols; j++) {
//                        cell = row.getCell(j);
//
//                    }

                    // check root
//                    System.out.println(ques);
//                    System.out.println(graph_Q.getNodeById(0).getChildren());
//
//                    System.out.println(text);
//                    System.out.println(graph_T.getNodeById(0).getChildren());
//                    System.out.println();

                    // check preposition
                    System.out.println(text);
                    for (Object node : graph_T.vertexSet()) {
                        String form = ((DNode) node).getForm();
                        if (form.equals("in") || form.equals("on")) {
                            System.out.println((DNode) node);
                        }
                    }
                    System.out.println("----------------------\n\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToJSON(String filepath, List<Data> dataList) {

    }

    public static void writeToExcel(String filepath, String sheetname, List<Data> dataList) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetname);
        int rownum = 0;
        for (Data data : dataList) {
            Row row = sheet.createRow(rownum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("1");

            cell = row.createCell(2);
            cell.setCellValue(data.question);

            cell = row.createCell(3);
            cell.setCellValue(data.positive);

            cell = row.createCell(4);
            cell.setCellValue(data.answer);

            row = sheet.createRow(rownum++);
            cell = row.createCell(0);
            cell.setCellValue(data.id);

            cell = row.createCell(1);
            cell.setCellValue("0");

            cell = row.createCell(2);
            cell.setCellValue(data.question);

            cell = row.createCell(3);
            cell.setCellValue(data.negative);
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

    public static List<Data> readXML(String filepath) {

        List<Data> dataList = new ArrayList<>();

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
            String negative = "";
            while ((line = br.readLine()) != null) {

                if (line.startsWith(pairStart)) {
                    boolean positivestarted = true;
                    boolean negativestarted = true;
                    if (!id.isEmpty()) {
                        Data data = new Data(id, question, positive, answer, negative);
                        dataList.add(data);
                    }

                    id = line.substring(pairStart.length()+1, line.length()-2);
                    while (!(line = br.readLine()).equals(pairEnd)) {
                        if (line.startsWith(questionTag)) {
                            question = br.readLine().trim().replaceAll("\t", " ");
                        }
                        if (line.startsWith(positiveStart) && positivestarted == true) {
                            positivestarted = false;
                            positive = br.readLine().trim().replaceAll("\t", " ");
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

            Data data = new Data(id, question, positive, answer, negative);
            dataList.add(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }
}

class Data {
    String id;
    String question;
    String positive;
    String negative;
    String answer;

    public Data(String id, String question, String positive, String answer, String negative) {

        this.id = id;
        this.question = question;
        this.positive = positive;
        this.answer = answer;
        this.negative = negative;
    }
}