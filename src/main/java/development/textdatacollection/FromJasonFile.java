package development.textdatacollection;

import bpn.utils.Buckets;
import bpn.utils.Fun;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by qingqingcai on 5/7/15.
 */
public class FromJasonFile {

    /** **************************************************************
     * Collect gold standard data from DA results with human correction
     * @param ifilepath: classification results from DA classifier_maochen with
     *                 manual correction
     * @param ofilepath: gold standard (labeled) data
     */
    public static void getTextDataFromJson(String ifilepath, String ofilepath) {

        ArrayList<String> gold_text = new ArrayList<>();
        JSONParser paser=new JSONParser();
        try {
            JSONArray array= (JSONArray) paser.parse(new FileReader(ifilepath));

            Iterator<JSONObject> iter=array.iterator();

            while(iter.hasNext()){
                JSONObject obj=iter.next();
                if (obj.get("gold").toString().isEmpty()) {
                    if (Buckets.FOCUSED_ON.contains(obj.get("tag"))) {
                        System.out.println(obj.get("tag") + "\t" + obj.get("sentence"));
                        gold_text.add(obj.get("tag") + "\t" + obj.get("sentence"));
                    }
                } else if (Buckets.FOCUSED_ON.contains(obj.get("gold"))) {
                    System.out.println(obj.get("gold") + "\t" + obj.get("sentence"));
                    gold_text.add(obj.get("gold") + "\t" + obj.get("sentence"));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Fun.saveToFile(ofilepath, gold_text);
    }

    public static void main(String[] args) {

        getTextDataFromJson("data/bpn/goldstandard/saytags.json", "data/bpn/goldstandard/gold_text.txt");
        //    convertToLibSVMData();
    }
}
