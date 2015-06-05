package rte.graphmatching;

import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.LangLib;
import rte.similarityflooding.NodePair;

import java.util.*;

/**
 * Created by qingqingcai on 5/21/15.
 */
public class AnswerExtraction {

    public static String runAnswerExtraction(Graph graph_T, Graph graph_H,
                                             HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_sim_NodePairList) {

        String answer = "";
        DNode bestMatchedNode = null;
        DNode parent = graph_H.getNodeById(1).getHead();    // the parent of Wh- words, assuming that Wh- word is the first word in H
        String parentLabel = graph_H.getNodeById(1).getDepLabel();

        if (!node_sim_NodePairList.containsKey(parent))
            return answer;

        bestMatchedNode = (DNode) node_sim_NodePairList.get(parent).entrySet().iterator().next().getValue().get(0).node1;
        if (bestMatchedNode == null)
            return answer;

        TreeMap<Integer, String> answerTokens = new TreeMap<>();
        List<DNode> childrenWithDepLabels = new ArrayList<>();
        if (parentLabel.equals("nsubj")
                || parentLabel.equals("nsubjpass")
                || parentLabel.equals("dep")) {
            childrenWithDepLabels = bestMatchedNode.getChildrenByDepLabels("nsubj");
            childrenWithDepLabels.addAll(bestMatchedNode.getChildrenByDepLabels("nsubjpass"));
        } else {
            childrenWithDepLabels = bestMatchedNode.getChildrenByDepLabels("dobj");
            childrenWithDepLabels.addAll(bestMatchedNode.getChildrenByDepLabels("pobj"));
            childrenWithDepLabels.addAll(bestMatchedNode.getChildrenByDepLabels("prep"));
        }

        if (childrenWithDepLabels.isEmpty())
            return answer;

        DNode startDNode = childrenWithDepLabels.get(0);
        answerTokens.put(startDNode.getId(), startDNode.getForm());

        Stack<DNode> stack = new Stack<>();
        stack.push(startDNode);

//        List<DNode> childrenOfBestMatched = bestMatchedNode[0].getChildren();
//        childrenOfBestMatched.forEach(c -> stack.push(c));
        while (!stack.empty()) {
            DNode top = stack.pop();
            answerTokens.put(top.getId(), top.getForm());
            List<DNode> children = top.getChildren();
            children.forEach(c -> stack.push(c));
        }

        for (Map.Entry entry : answerTokens.entrySet()) {
            answer += entry.getValue() + " ";   // entry.getValue() is the form
        }

        if (answer.isEmpty())
            return "I don't know.";
        return answer;
    }

    /** **************************************************************
     * Extract short answers by taking the most related dnode from T,
     * which is matched to wh-node in H;
     */
    public static String extractShortAnswers2(
            Graph graph_T, Graph graph_H, String ques,
            HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_sim_NodePairList) {

        String QTYPE = getQType(graph_H, ques);
        if (QTYPE.equals("YESNO"))
            return "yes";

        DNode whNode = graph_H.getFirstNodeWithPosTag(NodeComparer.WhSet);

        DNode bestMatchedNode_whNode = null;
        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeT_sim_NodePairList = toDNodeTMap(node_sim_NodePairList);
        List<NodePair> bestMatchedNodePairList_whNode = node_sim_NodePairList.get(whNode).entrySet().iterator().next().getValue();

        for (NodePair pair : bestMatchedNodePairList_whNode) {
            boolean finish = false;
            DNode node_T = (DNode) pair.node1;
            List<NodePair> reversedMatchedNodePairList = nodeT_sim_NodePairList.get(node_T).entrySet().iterator().next().getValue();
            for (NodePair reversedpair : reversedMatchedNodePairList) {
                if (reversedpair.node2 == whNode
                        && legalMapping((DNode) reversedpair.node1, whNode, QTYPE)) {
                    bestMatchedNode_whNode = node_T;
                    finish = true;
                    break;
                }
            }
            if (finish)
                break;
        }

        System.out.println("bestMatchedNode_whNode = " + bestMatchedNode_whNode);

        if (bestMatchedNode_whNode == null)
            return "I don't know.";

        Stack<DNode> stack = new Stack<>();
        stack.push(bestMatchedNode_whNode);

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

        if (answer.isEmpty())
            return "I don't know.";
        return answer;
    }

    public static String getQType(Graph graph_Q, String ques) {

        DNode firstNode = graph_Q.getNodeById(1);
        if (NodeComparer.VerbSet.contains(firstNode.getPOS()))
            return "YESNO";

        if (ques.matches("^(When|when|What time|what time) .*$"))
            return "TIME";

        if (ques.matches("^(Where|where) .*$"))
            return "LOCATION";

        return "OTHER";
    }

    /** **************************************************************
     * Convert nodeH_sim_NodePairList to nodeT_sim_NodePairList, sorted
     * by the sim score in descending;
     */
    private static HashMap<DNode, NavigableMap<Double, List<NodePair>>> toDNodeTMap(
            HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_sim_NodePairList) {

        HashMap<DNode, TreeMap<Double, List<NodePair>>> nodeT_sim_NodePairList = new HashMap<>();
        nodeH_sim_NodePairList.forEach((nodeH, sim_NodePairList) -> {
            List<NodePair> nodePairList = sim_NodePairList.entrySet().iterator().next().getValue();
            nodePairList.forEach(pair -> {
                DNode nodeT = (DNode) pair.node1;

                TreeMap<Double, List<NodePair>> sim_newNodePairList = new TreeMap<>();
                List<NodePair> newNodePairList = new ArrayList<>();
                if (nodeT_sim_NodePairList.containsKey(nodeT)) {
                    sim_newNodePairList = nodeT_sim_NodePairList.get(nodeT);
                    if (sim_newNodePairList.containsKey(pair.sim)) {
                        newNodePairList = sim_newNodePairList.get(pair.sim);
                    }
                }
                newNodePairList.add(pair);
                sim_newNodePairList.put(pair.sim, newNodePairList);
                nodeT_sim_NodePairList.put(nodeT, sim_newNodePairList);
            });
        });

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> result = new HashMap<>();
        for (DNode nodeH : nodeT_sim_NodePairList.keySet()) {
            NavigableMap sorted = (NavigableMap) nodeT_sim_NodePairList.get(nodeH).descendingMap();
            result.put(nodeH, sorted);
        }

        return result;
    }

    /** **************************************************************
     * Return true if node1 and node2 have equivalent dependency labels
     */
    private static boolean legalMapping(DNode nodeT, DNode whnode, String QType) {

        if (nodeT.getDepLabel().equals(whnode.getDepLabel()))
            return true;

        if (("LOCATION".equals(QType) || "TIME".equals(QType))
                && nodeT.getPOS().equals(LangLib.POS_IN))
            return true;

        if (whnode.getDepLabel().equals("dep")
                && ( NodeComparer.SubjSet.contains(nodeT.getDepLabel())
                || NodeComparer.ObjSet.contains(nodeT.getDepLabel())))
            return true;

        for (Set<String> strings : NodeComparer.equalDeplabelSet) {
            if (strings.contains(nodeT) && strings.contains(whnode))
                return true;
        }
        return false;
    }
}
