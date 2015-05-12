package bpn.utils;

import development.textdatacollection.Data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qingqingcai on 5/7/15.
 */
public class Fun {


    public static void saveToFile(String filepath, ArrayList<String> data) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (String row : data) {
                bw.write(row);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToFile(String filepath, ArrayList<Data> data, String field) {

        File file = new File(filepath);
        if (!file.exists())
            try {
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                for (Data row : data) {
                    bw.write(row.get(field));
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public static void saveToFile(String filepath,
           HashMap<Integer, String> id_label, HashMap<Integer, String> id_fea) {

        File file = new File(filepath);
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            id_label.forEach((id, label) -> {
                try {
                    bw.write(id_label.get(id) + " " + id_fea.get(id));
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
