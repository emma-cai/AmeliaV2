package rte.answerextraction;

import rte.datastructure.Graph;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by qingqingcai on 6/7/15.
 */
public class RTEData {
    String id;
    String query;
    String text;
 //   String negative;
    String answer;
    String answerid;
    String label;

    String shortAnswerCandidate;

    String conllxQ;
    String conllxT;
 //   String conllxN;

    Graph graphQ;
    Graph graphP;
    Graph graphN;

    HashMap<String, String> feamap = new HashMap<>();
    TreeMap<Integer, Integer> sparsefeamap = new TreeMap<>();
    // feature_index (fi) -> feature_value (fv)
    TreeMap<Integer, Double> numericfeamap = new TreeMap<>();   // feature_index to feature_value

    public void setId(String id) {
        this.id = id;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setShortAnswerCandidate(String shortAnswerCandidate) {

        this.shortAnswerCandidate = shortAnswerCandidate;
    }

    public void setConllxQ(String conllxQ) {

        this.conllxQ = conllxQ;
    }

    public void setConllxT(String conllxT) {

        this.conllxT = conllxT;
    }

    public void setFeaMap(HashMap<String, String> feamap) {
        this.feamap.putAll(feamap);
    }

    public void setNumericFeamap(HashMap<Integer, Double> numericFeamap) {
        this.numericfeamap.putAll(numericFeamap);
    }

    public RTEData() {

    }

//    public RTEData(String id, String query, String text, String answer) {
//
//        new RTEData(id, query, text, "", answer);
//    }
//
//    public RTEData(String id, String question, String positive, String answerid, String answer) {
//
//        this.id = id;
//        this.query = question;
//        this.text = positive;
//        this.answerid = answerid;
//        this.answer = answer;
//    }
//
//    public RTEData(String id, String label, String ques, String text, String answer,
//                   String quesConllx, String textConllx, HashMap<String, String> feamap) {
//
//        this.id = id;
//        this.label = label;
//        this.query = ques;
//        this.text = text;
//        this.answer = answer;
//        this.conllxQ = quesConllx;
//        this.conllxT = textConllx;
//        this.feamap.putAll(feamap);
//    }

    public RTEData(String id, String label, String query, String text, String answerid, String answer) {

        this.id = id;
        this.label = label;
        this.query = query;
        this.text = text;
        this.answerid = answerid;
        this.answer = answer;
    }

    public String getID() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getText() {
        return text;
    }

    public String getAnswer() {
        return answer;
    }

    public String getLabel() {
        return label;
    }

    public String getConllxQ() {
        return conllxQ;
    }

    public String getConllxT() {
        return conllxT;
    }

    public String getShortAnswerCandidate() {
        return shortAnswerCandidate;
    }

    public HashMap<String, String> getFeamap() {
        return feamap;
    }
}
