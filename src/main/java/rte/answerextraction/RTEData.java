package rte.answerextraction;

import rte.datastructure.Graph;

import java.util.HashMap;

/**
 * Created by qingqingcai on 6/7/15.
 */
public class RTEData {
    String id;
    String query;
    String text;
 //   String negative;
    String answer;
    String label;

    String conllxQ;
    String conllxT;
 //   String conllxN;

    Graph graphQ;
    Graph graphP;
    Graph graphN;

    HashMap<String, String> feamap = new HashMap<>();
    HashMap<Integer, Integer> sparsefeamap = new HashMap<>();
    HashMap<String, Double> numericfeamap = new HashMap<>();

    public void setConllxQ(String conllxQ) {

        this.conllxQ = conllxQ;
    }

    public void setConllxT(String conllxT) {

        this.conllxT = conllxT;
    }

//    public void setConllxN(String conllxN) {
//
//        this.conllxN = conllxN;
//    }

    public RTEData(String id, String question, String positive, String answer) {

        this.id = id;
        this.query = question;
        this.text = positive;
        this.answer = answer;
    //    this.negative = negative;
    }

    public RTEData(String id, String label, String ques, String text, String answer,
                   String quesConllx, String textConllx, HashMap<String, String> feamap) {

        this.id = id;
        this.label = label;
        this.query = ques;
        this.text = text;
        this.answer = answer;
        this.conllxQ = quesConllx;
        this.conllxT = textConllx;
        this.feamap.putAll(feamap);
    }
}
