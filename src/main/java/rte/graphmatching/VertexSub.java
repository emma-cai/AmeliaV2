package rte.graphmatching;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.commons.math.stat.StatUtils;
import rte.datastructure.DNode;
import rte.datastructure.Graph;

import java.util.*;

/**
 * Created by qingqingcai on 5/14/15.
 */
public class VertexSub implements NodeComparer {

    public static final Set<Set<String>> equalPostagSet = NodeComparer.equalPostagSet;

    public static boolean debug = false;

    // TODO: Feature Weight Learning as Future Work
    public static Double[] weights = {0.3, 0.2, 0.05, 0.05, 0.1, 0.05, 0.1, 0.1};

    private static double ws4jThreshold = 0.5;  // threshold for WordNet similarity

    private static ILexicalDatabase db = new NictWordNet();

    private static RelatednessCalculator[] rcs = {
    //        new HirstStOnge(db),
    //        new LeacockChodorow(db),
    //        new Lesk(db),
    //        new WuPalmer(db),
    //        new Resnik(db),
    //        new JiangConrath(db),
    //        new Lin(db),
            new Path(db)
    };

    private final static Set<String> excludeFormList = new HashSet<>(Arrays.asList("^"));
    private final static Set<String> excludePOSList = new HashSet<>(Arrays.asList("PUNCT", "DET", "ADP", "WRB", "WP", "WDT", "IN"));
    private final static Set<String> excludeLemmaList = new HashSet<>(Arrays.asList("is", "am", "are", "a"));

    /** **************************************************************
     * Implement method getNodeSimilarity in NodeComparer: Calculcate
     * a value from 0 to 1 inclusive that indicates how similar the
     * two nodes are.
     * Version1: nodeSimilarity = 1 - nodeMatchingCost;
     */
    @Override
    public double getNodeSimilarity(DNode dnode_T, DNode dnode_H) {

        double nodeMatchingCost = getVertexSub(dnode_T, dnode_H);
        double nodeSimilarity = 1 - nodeMatchingCost;
        return nodeSimilarity;
    }

    /** **************************************************************
     * Compute VertexCost
     */
    public double getVertexSub(DNode dnode_T, DNode dnode_H) {

        Vector<Double> fie_vector = new Vector<>();

        double exactMatch_tag = 1.0;
        double stemMatch_tag = 1.0;
        double posMatch_tag = 1.0;
        double ws4jMatch_tag = 1.0;
        double exactMatch_tag_parent = 1.0;
        double stemMatch_tag_parent = 1.0;
        double posMatch_tag_parent = 1.0;
        double ws4jMatch_tag_parent = 1.0;

        if (dnode_T != null && dnode_H != null) {
            exactMatch_tag = doExactMatch(dnode_T, dnode_H);
            stemMatch_tag = doStemMatch(dnode_T, dnode_H);
            posMatch_tag = doPOSMatch(dnode_T, dnode_H);
            ws4jMatch_tag = doWS4JMatch(dnode_T, dnode_H);
            if (dnode_T.getHead() != null && dnode_H.getHead() != null) {
                exactMatch_tag_parent = doExactMatch(dnode_T.getHead(), dnode_H.getHead());
                stemMatch_tag_parent = doStemMatch(dnode_T.getHead(), dnode_H.getHead());
                posMatch_tag_parent = doPOSMatch(dnode_T.getHead(), dnode_H.getHead());
                ws4jMatch_tag_parent = doWS4JMatch(dnode_T.getHead(), dnode_H.getHead());
            }
        }

        fie_vector.add(exactMatch_tag);
        fie_vector.add(stemMatch_tag);
        fie_vector.add(posMatch_tag);
        fie_vector.add(ws4jMatch_tag);
        fie_vector.add(exactMatch_tag_parent);
        fie_vector.add(stemMatch_tag_parent);
        fie_vector.add(posMatch_tag_parent);
        fie_vector.add(ws4jMatch_tag_parent);

        Vector<Double> fie_weight_vector = new Vector();
        Collections.addAll(fie_weight_vector, weights);
//        Vector<Double> fie_weight_vector = new Vector<>(fie_vector.size());
//        for (int i = 0; i < fie_vector.size(); i++)
//            fie_weight_vector.add(1.0/fie_vector.size());

        double VertexSub = DMatching.computeExpFun(fie_weight_vector, fie_vector);

        if (debug) {
            System.out.println("\n----------------------------------------");
            System.out.println("dnode_H = " + dnode_H);
            System.out.println("dnode_T = " + dnode_T);
            System.out.print("fie_vector = ");
            for (double v : fie_vector) System.out.print(" \t" + v);
            System.out.println();
            System.out.print("fie_weight_vector = ");
            for (double v : fie_weight_vector) System.out.print(" \t" + v);
            System.out.println();
            System.out.println("VertexSub = " + VertexSub);
            System.out.println("----------------------------------------");
        }

//        System.out.println("\n----------------------------------------");
//        System.out.println("dnode_H = " + dnode_H);
//        System.out.println("dnode_T = " + dnode_T);
//        System.out.println("Vertex cost = " + VertexSub);
//        System.out.println("----------------------------------------");

        return VertexSub;
    }

    /** **************************************************************
     * If v and M(v) are identical words; return 0 (cost value) if they
     * are, otherwise return 1;
     */
    private static double doExactMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getForm().equals(dnode_H.getForm())
                || doStemMatch(dnode_T, dnode_H) ==  0.0)
            return 0.0;             // if exactly matched, the cost is 0
        else
            return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same lemma; return 0 (cost value) if they
     * match, otherwise return 1;
     */
    private static double doLemmaMatch(DNode dnode_T, DNode dnode_H) {
        if (dnode_T.getLemma().equals(dnode_H.getLemma()))
            return 0.0;
        else
            return 1.0;
    }

    /** **************************************************************
     * A naive method (suggested by Peigen You) to check if two words
     * have the same stemming
     */
    public static double doStemMatch(DNode dnode_T, DNode dnode_H) {

        String form_T = dnode_T.getForm();
        String form_H = dnode_H.getForm();

        if (form_T.equals(form_H))
            return 0.0;

//        if ((form_T.equals("flight") && form_H.equals("flying")) ||
//                (form_H.equals("flight") && form_T.equals("flying")) )
//            return 0.0;

        if(Math.abs(form_H.length()-form_T.length())<3 && Math.abs(form_H.length()-form_T.length())>0){
            String longForm = form_H.length() > form_T.length() ? form_H : form_T;
            String shortForm = longForm == form_H ? form_T : form_H;

            if(longForm.startsWith(shortForm) && (longForm.endsWith("s")||longForm.endsWith("ed"))){
                return 0.0;
            }
        }
        return 1.0;
    }

    /** **************************************************************
     * If v and M(v) have the same part of speech; return 0 (cost value)
     * if they match, otherwise return 1;
     */
    private static double doPOSMatch(DNode dnode_T, DNode dnode_H) {

        if (dnode_T.getPOS().equals(dnode_H.getPOS()))
            return 0.0;             // if the pos tags are matched, the cost is 0
        else {
            for (Set<String> equalPostag : equalPostagSet) {
                if (equalPostag.contains(dnode_T.getPOS()) && equalPostag.contains(dnode_H.getPOS()))
                    return 0.0;
            }
        }

        return 1.0;
    }

    /** **************************************************************
     * If WordNet similarity > ws4jThreshold, WS4JCost = 0;
     * Otherwise, WS4JCost = 1.0;
     */
    private static double doWS4JMatch(DNode dnode_T, DNode dnode_H) {

        double[] ws4jSimilarities = computeWS4JSimilarity(dnode_T, dnode_H);
        double average = StatUtils.sum(ws4jSimilarities) / (ws4jSimilarities.length * 1.0);
        if (Double.compare(average, ws4jThreshold) > 0)
            return 0.0;
        else
            return 1.0;
    }

    /** **************************************************************
     * Compute WordNet similarity for two words by using cmu-ws4j
     */
    private static double[] computeWS4JSimilarity(DNode dNode_T, DNode dNode_H) {

        double[] ws4jSimilarities = new double[rcs.length];
        WS4JConfiguration.getInstance().setMFS(true);

        int index = 0;
        for ( RelatednessCalculator rc : rcs ) {
            double s = rc.calcRelatednessOfWords(dNode_T.getForm(), dNode_H.getForm());
            ws4jSimilarities[index++] = s;
        }
        return ws4jSimilarities;
    }

    /** **************************************************************
     * In VertexSub computation, exclude nodes if:
     * (1) the node is the root, which is labeled as "^";
     * (2) the cPOSTag of the node is "PUNCT", "DET;
     */
    public static boolean excludeNodes(DNode node) {

        // A list of forms/word_strings which will not be considered in VertexSub
        if (excludeFormList.contains(node.getForm()))
            return true;

        // A list of pos-tags which will not be considered in VertexSub
        if (excludePOSList.contains(node.getPOS()))
            return true;

        // A list of lemma which will not be considered in VertexSub
        if (excludeLemmaList.contains(node.getLemma()))
            return true;

        return false;
    }

    /** **************************************************************
     * TODO: compute the importance weight dnode_H in our algorithm
     * @param dnode
     * @return
     */
    public static double Importance(DNode dnode) {

        return 1.0;
    }

    public static double Importance(Graph graph_H, DNode dnode_H) {

        String POSTag = dnode_H.getPOS();
        double tag = 0.0;
        if (NodeComparer.VerbSet.contains(POSTag)) {
            tag += 0.0;
        } else if (NodeComparer.NounSet.contains(POSTag)) {
            tag += 1.0;
        } else {
            tag += 2.0;
        }
        tag += (graph_H.findShortestPath(graph_H.getNodeById(0), dnode_H)).size();

        return 1.0 / ( tag * 1.0 );
    }
}
