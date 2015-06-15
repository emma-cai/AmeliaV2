package rte.answerextraction;

import rte.RteMessageHandler;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.LangLib;
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
    public static List<String> getFieldList(
            List<DNode> nodeList, String field, List<String> filterList) {

        List<String> AnsFieldList = new ArrayList<>();

        for (DNode dnode : nodeList) {
            switch (field) {
                case "pos":
                    String pos = dnode.getPOS();
                    if (!(filterList != null && filterList.contains(pos)))
                        AnsFieldList.add(pos);
                    break;
                case "dep":
                    String dep = dnode.getDepLabel();
                    if (!(filterList != null && !filterList.contains(dep)))
                        AnsFieldList.add(dep);
                    break;
                case "lemma":
                    String lemma = dnode.getLemma();
                    if (!(filterList != null && !filterList.contains(lemma)))
                        AnsFieldList.add(lemma);
                    break;
                case "form":
                    String form = dnode.getForm();
                    if (!(filterList != null && !filterList.contains(form)))
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
    public static String getFieldStr(List<DNode> nodeList, String field,
                                     List<String> filterList, String separate) {

        String AnsFieldStr = "";
        List<String> AnsFieldList = getFieldList(nodeList, field, filterList);
        for (String s : AnsFieldList) {
            AnsFieldStr += AnsFieldStr.isEmpty() ? s : (separate + s);
        }

        return AnsFieldStr;
    }

    /** **************************************************************
     * Collect specific field for each node int the nodeList, and then
     * convert the collection into string;
     */
    public static String listToString(List<String> list, String separate) {

        String AnsFieldStr = "";
        for (String s : list) {
            AnsFieldStr += AnsFieldStr.isEmpty() ? s : (separate + s);
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
     * How many lemmas in answer-candidate also appear in query?
     */
    public static int overlap(Graph graph, List<DNode> dnodeList) {

        int overlapN = 0;
        Set<Object> dnodeSetInQuery = new HashSet<>();
        dnodeSetInQuery.addAll(graph.vertexSet());
        for (DNode dnode : dnodeList) {
            String lemma = dnode.getLemma();
            for (Object dnodeInQuery : dnodeSetInQuery) {
                if (((DNode) dnodeInQuery).getLemma().equals(lemma)) {
                    overlapN++;
                }
            }
        }
        return overlapN;
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

//        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
//                = initNodeMatches(graphT, graphQ, config);
//        DMatching.computeMatchingCost(graphT, graphQ, nodeMatches);
//
//        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
//        return generateAnswerCandidates(whNode, nodeMatches);
//
//

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches
                = initNodeMatches(graphT, graphQ, config);

        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        return generateAnswerCandidates(whNode, nodeMatches);
    }

    public static List<TreeMap<Integer, DNode>> generateAnswerCandidates(
            DNode whNode, HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_sim_NodePairList) {

        List<TreeMap<Integer, DNode>> answerCandidateList = new ArrayList<>();
        Collection<List<NodePair>> tmp = nodeH_sim_NodePairList.get(whNode).values();

        for (List<NodePair> pairlist : tmp) {
            for (NodePair pair : pairlist) {
                DNode node_T = (DNode) pair.node1;
                TreeMap<Integer, DNode> answerCandidate = getSubtreeDNodeMap(node_T);

                if (answerCandidate.isEmpty())
                    continue;
                if (answerCandidate.size() == 1) {
                    String dep = answerCandidate.entrySet().iterator().next().getValue().getDepLabel();
                    if (!dep.contains("nn") && !dep.contains("subj") && !dep.contains("obj"))
                        continue;
                }

                answerCandidateList.add(answerCandidate);
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

    static boolean isNumeric(String s) {

        return s.startsWith("N:");
    }

    static boolean isCategorial(String s) {

        return s.startsWith("C:");
    }

    public static void main(String[] args) {

        String ques = "Who is the author of the book , `` The Iron Lady : A Biography of Margaret Thatcher '' ?";
        String text = "the IRON LADY ; A Biography of Margaret Thatcher by Hugo Young -LRB- Farrar , Straus & Giroux -RRB-";

//        ques = "When was London 's Docklands Light Railway constructed ?";
//        text = "As it turned out , when it opened in 1987 the Docklands Light Railway did not include any street running .";

//        ques = "What country is the biggest producer of tungsten ?";
//        text = "China dominates world tungsten production and has frequently been accused of dumping tungsten on western markets .";

        List<TreeMap<Integer, DNode>> listOfMap = generateAnswerCandidates(ques.toLowerCase(), text.toLowerCase());
        for(TreeMap id_dnode_map : listOfMap) {
            String ansCandStr = fromTreeMapToString(id_dnode_map);
            System.out.println(ansCandStr);
        }
    }


    private static HashSet<String> LessImportantPOSSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.POS_IN, LangLib.POS_DT, LangLib.POS_JJ, LangLib.POS_JJR,
                      LangLib.POS_JJS, LangLib.POS_TO}));
    private static HashSet<String> LessImportantDepSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.DEP_AUX, LangLib.DEP_AUXPASS, LangLib.DEP_ADVMOD,
                      LangLib.DEP_ACOMP,
                      LangLib.POS_JJS, LangLib.POS_TO}));
}
