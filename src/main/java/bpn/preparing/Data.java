package bpn.preparing;

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

    public String getText() { return text; }

    public String getType() { return type; }

    public String getSource() { return source; }

    public List<String> getLemmas() { return lemmas; }

    public HashMap<String, String> getKMeansFeatures() { return KMeansFeatures; }

    public HashMap<String, String> getLDAFeatures() { return LDAFeatures; }

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
