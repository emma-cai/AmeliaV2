package rte.answerextraction;

import rte.datastructure.Graph;

import java.util.HashMap;

/**
 * Created by qingqingcai on 6/7/15.
 */
public class RTEData {
    String id;
    String question;
    String positive;
    String negative;
    String answer;
    String label;

    String conllxQ;
    String conllxP;
    String conllxN;

    Graph graphQ;
    Graph graphP;
    Graph graphN;

    HashMap<String, String> feamap = new HashMap<>();
    HashMap<Integer, Integer> sparsefeamap = new HashMap<>();

    public void setConllxQ(String conllxQ) {

        this.conllxQ = conllxQ;
    }

    public void setConllxP(String conllxP) {

        this.conllxP = conllxP;
    }

    public void setConllxN(String conllxN) {

        this.conllxN = conllxN;
    }

    public RTEData(String id, String question, String positive, String answer, String negative) {

        this.id = id;
        this.question = question;
        this.positive = positive;
        this.answer = answer;
        this.negative = negative;
    }
}
