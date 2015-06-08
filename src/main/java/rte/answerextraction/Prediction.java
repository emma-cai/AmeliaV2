package rte.answerextraction;

import rte.RteMessageHandler;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.DMatching;
import rte.graphmatching.NodeComparer;
import rte.similarityflooding.NodePair;

import java.util.*;

import static rte.graphmatching.DMatching.initNodeMatches;

/**
 * Created by qingqingcai on 6/7/15.
 */
public class Prediction extends RteMessageHandler {

    public static void main(String[] args) {

        String ques = "Who is the author of the book , `` The Iron Lady : A Biography of Margaret Thatcher '' ?";
        String text = "the IRON LADY ; A Biography of Margaret Thatcher by Hugo Young -LRB- Farrar , Straus & Giroux -RRB-";

//        ques = "When was London 's Docklands Light Railway constructed ?";
//        text = "As it turned out , when it opened in 1987 the Docklands Light Railway did not include any street running .";

        ques = "What country is the biggest producer of tungsten ?";
        text = "China dominates world tungsten production and has frequently been accused of dumping tungsten on western markets .";

        generateAnswerCandidates(ques.toLowerCase(), text.toLowerCase());
    }

    public static String generateAnswerCandidates(String ques, String text) {

        Graph graphT = Graph.stringToGraph(text);
        Graph graphQ = Graph.stringToGraph(ques);

        return generateAnswerCandidates(ques, text, graphQ, graphT);
    }

    public static String generateAnswerCandidates(
            String ques, String text, Graph graphQ, Graph graphT) {

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);
        DMatching.computeMatchingCost(graphT, graphQ, nodeMatches);

        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        return generateAnswerCandidates(nodeMatches, whNode);

//        DNode whNodeParent = whNode.getHead();
//        return generateAnswerCandidates(nodeMatches, whNode);
    }

    public static String generateAnswerCandidates(
            HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_sim_NodePairList,
            DNode whNode) {

        List<NodePair> bestMatchedNodePairList_whNode = new ArrayList<>();
        Collection<List<NodePair>> tmp = nodeH_sim_NodePairList.get(whNode).values();

        for (List<NodePair> pairlist : tmp) {
            for (NodePair pair : pairlist) {
                DNode node_T = (DNode) pair.node1;
                String answerCandidate = getSubtreeString(node_T);
            //    if (answerCandidate.split(" ").length > 1) {
                    System.out.println("\n" + pair.sim + "\t" + whNode);
                    System.out.println( pair.sim + "\t" + node_T);
                    System.out.println("answer candidate = " + answerCandidate);
            //    }
            }
        }

//        for (NodePair pair : bestMatchedNodePairList_whNode) {
//            DNode node_T = (DNode) pair.node1;
//            String answerCandidate = getSubtreeString(node_T);
//            System.out.println("\n" + pair.sim + "\t" + whNode);
//            System.out.println( pair.sim + "\t" + node_T);
//            System.out.println("answer candidate = " + answerCandidate);
//        }

        return null;
    }

    public static String getSubtreeString(DNode startDNode) {

        Stack<DNode> stack = new Stack<>();
        stack.push(startDNode);

        String answer = "";
        TreeMap<Integer, String> answerTokens = new TreeMap<>();
        while (!stack.empty()) {
            DNode top = stack.pop();
            answerTokens.put(top.getId(), top.getForm());
            List<DNode> children = top.getChildren();
            children.forEach(c -> stack.push(c));
        }

        for (Map.Entry entry : answerTokens.entrySet()) {
            answer += entry.getValue() + " ";   // entry.getValue() is the form
        }

        return answer;
    }
}
