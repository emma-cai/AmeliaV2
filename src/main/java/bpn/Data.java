package bpn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qingqingcai on 4/29/15.
 */
public class Data {
    int id;
    String text;
    String type;
    String source;
    List<String> lemmas = new ArrayList<String>();
    HashMap<String, String> KMeansFeatures = new HashMap<String, String>();
    HashMap<String, String> LDAFeatures = new HashMap<>();

    public void print() {
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("source = " + this.source);
        System.out.println("id = " + this.id);
        System.out.println("type = " + this.type);
        System.out.println("text = " + this.text);
        System.out.println("lemmas = " + this.lemmas);
        System.out.println("features = " + this.KMeansFeatures.size() + "\t" + this.KMeansFeatures);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }
}
