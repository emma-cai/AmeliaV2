package rte.answerextraction_tmp;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import rte.datastructure.DNode;
import rte.datastructure.Graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by qingqingcai on 6/12/15.
 */
public class SmallExperiments {

    public static void main(String[] args) {

        String filepath = "/Users/qingqingcai/Documents/IntellijWorkspace/AmeliaV2/data/rte/MIT99.xls";
        String sheetname = "MIT99-trek8";
        readExcel(filepath, sheetname, null);
    }

    /** **************************************************************
     * Read RTE data from excel file
     */
    public static void readExcel(String filepath, String sheetname, List<RTEData> data) {

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
                    System.out.println("----------------------");
                    String ques = row.getCell(2).getStringCellValue();
                    String text = row.getCell(3).getStringCellValue();
                    String quesConllx = row.getCell(5).getStringCellValue();
                    String textConllx = row.getCell(6).getStringCellValue();
                    Graph graph_Q = Graph.conllxToGraph(quesConllx);
                    Graph graph_T = Graph.conllxToGraph(textConllx);

                    // check preposition
                    System.out.println(text);
                    for (Object node : graph_T.vertexSet()) {
                        String form = ((DNode) node).getForm();
                        String pos = ((DNode) node).getPOS();
                        System.out.println(form + "\t" + pos);
                    }
                    System.out.println("----------------------\n\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
