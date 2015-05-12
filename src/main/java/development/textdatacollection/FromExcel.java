package development.textdatacollection;

import bpn.utils.Buckets;
import bpn.utils.Fun;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

//import org.apache.poi.hssf.model.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Created by qingqingcai on 5/11/15.
 */
public class FromExcel {

    public static void main(String[] args) {

        String excelpath = "data/bpn/text/bpn_data_in_processing.xlsx";
        String textpath = "data/bpn/text/bpn_test.txt";

        System.out.println("Data collection started ...");
        extractDataFromExcel(excelpath, textpath);
        System.out.println("Data collection finished ...");
    }

    /** **************************************************************
     * Collect data from an excel file which is still under development
     * @param ifilepath: bpn data under development
     * @param ofilepath: testing data in text format: LABEL\tTEXT
     */
    public static void extractDataFromExcel(String ifilepath, String ofilepath) {

        ArrayList<String> label_text_list = new ArrayList<>();
        try {

            FileInputStream fis = new FileInputStream(ifilepath);
            Workbook workbook = new XSSFWorkbook(fis);
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Iterator rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = (Row) rowIterator.next();
                    Cell label = row.getCell(0);
                    Cell text = row.getCell(1);
                    if (label != null && text != null) {
                        if (Buckets.FOCUSED_ON.contains(label.toString())) {
                            String labeltext = label.toString() + "\t" + text.toString();
                            if (!label_text_list.contains(labeltext))
                                label_text_list.add(labeltext);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Fun.saveToFile(ofilepath, label_text_list);
    }
}
