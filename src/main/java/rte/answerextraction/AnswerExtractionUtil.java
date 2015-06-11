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
 * Created by qingqingcai on 6/10/15.
 */
public class AnswerExtractionUtil extends RteMessageHandler {

    /** **************************************************************
     * Generate token string from <node-id, node> map
     */
    public static String fromTreeMapToString(TreeMap<Integer, DNode> map) {

        StringBuilder sb = new StringBuilder();
        map.forEach((id, node) -> {
            sb.append(node.getForm() + " ");
        });
        return sb.toString().trim();
    }

    /** **************************************************************
     * Get list of DNodes, whose IDs are defined in the IDList
     */
    public static List<DNode> getNodeList(Graph graph, String s) {

        return getNodeList(graph, stringToList(s));
    }

    /** **************************************************************
     * Get list of DNodes, whose IDs are defined in the IDList
     */
    public static List<DNode> getNodeList(Graph graph, List<Integer> IDList) {

        List<DNode> DNodeList = new ArrayList<>();
        IDList.forEach(nodeID -> {
            DNodeList.add(graph.getNodeById(nodeID));
        });

        return DNodeList;
    }

    /** **************************************************************
     * Collect specific field for each node in the nodeList;
     */
    public static List<String> getFieldList(List<DNode> nodeList, String field) {

        List<String> AnsFieldList = new ArrayList<>();

        for (DNode dnode : nodeList) {
            switch (field) {
                case "pos":
                    String pos = dnode.getPOS();
                    AnsFieldList.add(pos);
                    break;
                case "dep":
                    String dep = dnode.getDepLabel();
                    AnsFieldList.add(dep);
                    break;
                case "lemma":
                    String lemma = dnode.getLemma();
                    AnsFieldList.add(lemma);
                    break;
                case "form":
                    String form = dnode.getForm();
                    AnsFieldList.add(form);
                    break;
            }

        }
        return AnsFieldList;
    }

    /** **************************************************************
     * Collect specific field for each node int the nodeList, and then
     * convert the collection into string;
     */
    public static String getFieldStr(List<DNode> nodeList, String field) {

        String AnsFieldStr = "";
        List<String> AnsFieldList = getFieldList(nodeList, field);
        for (String s : AnsFieldList) {
            AnsFieldStr += AnsFieldStr.isEmpty() ? s : ("_" + s);
        }

        return AnsFieldStr;
    }

    /** **************************************************************
     * Example input s = "11 12", return list of integers {11, 12};
     */
    public static List<Integer> stringToList(String s) {

        List<Integer> list= new ArrayList<>();
        String[] idArr = s.split(" ");
        for (String id : idArr) {
            if (!id.equals("#"))
                list.add(Integer.parseInt(id));
            else
                break;
        }

        return list;
    }

    /** **************************************************************
     * For the given question, extract (short) answer candidates from
     * the text; Return a list of <DNodeID, DNode> for the short answer;
     */
    public static List<TreeMap<Integer, DNode>> generateAnswerCandidates(String ques, String text) {

        Graph graphT = Graph.stringToGraph(text);
        Graph graphQ = Graph.stringToGraph(ques);

        return generateAnswerCandidates(graphQ, graphT);
    }

    /** **************************************************************
     * For the given question, extract (short) answer candidates from
     * the text; Return a list of <DNodeID, DNode> for the short answer;
     */
    public static List<TreeMap<Integer, DNode>> generateAnswerCandidates(
            Graph graphQ, Graph graphT) {

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);
        DMatching.computeMatchingCost(graphT, graphQ, nodeMatches);

        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        return generateAnswerCandidates(whNode, nodeMatches);
    }

    public static List<TreeMap<Integer, DNode>> generateAnswerCandidates(
            DNode whNode, HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_sim_NodePairList) {

        List<TreeMap<Integer, DNode>> answerCandidateList = new ArrayList<>();
        List<NodePair> bestMatchedNodePairList_whNode = new ArrayList<>();
        Collection<List<NodePair>> tmp = nodeH_sim_NodePairList.get(whNode).values();

        for (List<NodePair> pairlist : tmp) {
            for (NodePair pair : pairlist) {
                DNode node_T = (DNode) pair.node1;
                TreeMap<Integer, DNode> answerCandidate = getSubtreeDNodeMap(node_T);
                answerCandidateList.add(answerCandidate);
                //    if (answerCandidate.split(" ").length > 1) {
                System.out.println("\n" + pair.sim + "\t" + whNode);
                System.out.println( pair.sim + "\t" + node_T);
                System.out.println("answer candidate = " + answerCandidate);
                //    }
            }
        }

        return answerCandidateList;
    }

    /** **************************************************************
     * Get the subtree starting from the input DNode "startDNode";
     */
    public static String getSubtreeString(DNode startDNode) {

        TreeMap<Integer, DNode> idDNodeMap = getSubtreeDNodeMap(startDNode);
        List<DNode> DNodeList = new ArrayList<>(idDNodeMap.values());
        return fromTreeMapToString(idDNodeMap);
    }

    /** **************************************************************
     * Get the subtree starting from the input DNode "startDNode";
     */
    public static TreeMap<Integer, DNode> getSubtreeDNodeMap(DNode startDNode) {

        TreeMap<Integer, DNode> subtreeDNodeSorted = new TreeMap<>();

        Stack<DNode> stack = new Stack<>();
        stack.push(startDNode);

        while (!stack.empty()) {
            DNode top = stack.pop();
            subtreeDNodeSorted.put(top.getId(), top);
            List<DNode> children = top.getChildren();
            children.forEach(c -> stack.push(c));
        }

        return subtreeDNodeSorted;
    }

    public static void main(String[] args) {

        String ques = "Who is the author of the book , `` The Iron Lady : A Biography of Margaret Thatcher '' ?";
        String text = "the IRON LADY ; A Biography of Margaret Thatcher by Hugo Young -LRB- Farrar , Straus & Giroux -RRB-";

//        ques = "When was London 's Docklands Light Railway constructed ?";
//        text = "As it turned out , when it opened in 1987 the Docklands Light Railway did not include any street running .";

        ques = "What country is the biggest producer of tungsten ?";
        text = "China dominates world tungsten production and has frequently been accused of dumping tungsten on western markets .";

        generateAnswerCandidates(ques.toLowerCase(), text.toLowerCase());
    }
}
