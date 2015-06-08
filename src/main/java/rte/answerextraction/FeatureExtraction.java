//package rte.answerextraction;
//
//import rte.datastructure.DNode;
//import rte.datastructure.DTree;
//import rte.datastructure.Graph;
//import rte.experiments.GraphExtended;
//import rte.graphmatching.NodeComparer;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by qingqingcai on 5/29/15.
// */
//public class FeatureExtraction {
//
//    private static Set<String> WhSet = NodeComparer.WhSet;
//    private static String ANSPos = "ANSPos";
//    private static String ANSDep = "ANSDep";
//    private static String WH_ANSPos = "WH_ANSPos";
//    private static String WH_ANSDep = "WH_ANSDep";
//    private static String WHDep_ANSDep = "WHDep_ANSDep";
//    private static boolean flagGroupPos = true;
//    private static boolean flagGroupDep = true;
//
//
//    public HashMap<String, String> extractFeatuers(Graph graph_Q, Graph graph_T, List<Integer> ANSIDList) {
//
//        HashMap<String, String> result = new HashMap<>();
//        String WH_ANSPosValue = "";
//        String WH_ANSDepValue = "";
//        String WHDep_ANSDepValue = "";
//        DNode whNode = graph_Q.getFirstNodeWithPosTag(WhSet);
//        if (whNode == null) {
//            System.err.println("NO QUESTION WORDS WERE FOUND!");
//            return result;
//        }
//        List<DNode> ANSDNodeList = GraphExtended.getNodeList(graph_T, ANSIDList);
//        HashMap<String, String> feaName_feaValue = computeWHAnsPos(graph_T, ANSDNodeList);
//        WH_ANSPosValue = whNode.getForm() + "#" + feaName_feaValue.get(ANSPos);
//        WH_ANSDepValue = whNode.getForm() + "#" + feaName_feaValue.get(ANSDep);
//        WHDep_ANSDepValue = whNode.getForm() + "_" + whNode.getDepLabel() + "#" + feaName_feaValue.get(ANSDep);
//
//        result.put(WH_ANSPos, WH_ANSPosValue);
//        result.put(WH_ANSDep, WH_ANSDepValue);
//        result.put(WHDep_ANSDep, WHDep_ANSDepValue);
//
////        System.out.println("First dnode with postag WP = " + whNode);
////        System.out.println("WH_ANSPos = " + WH_ANSPos);
////        System.out.println("WH_ANSDep = " + WH_ANSDep);
//        return result;
//    }
//
//    public static void main(String[] args) {
//
//        List<RTEData> dataList = readXML("data/rte/jacana-qa-naacl2013-data-results/train-less-than-40.manual-edit.xml");
//        HashMap<String, List<String>> wh_anspos_features = new HashMap<>();
//        HashMap<String, List<String>> wh_ansdep_features = new HashMap<>();
//        HashMap<String, List<String>> whdep_ansdep_features = new HashMap<>();
//        for (RTEData data : dataList) {
//
//            System.out.println();
//            System.out.println("id = " + data.id);
//            System.out.println("question = " + data.question);
//            System.out.println("positive = " + data.positive);
//            System.out.println("answer = " + data.answer);
//
//
//            Graph graph_Q = Graph.buildDGraph(DTree.buildTree(data.question.toLowerCase()));
//            Graph graph_P = Graph.buildDGraph(DTree.buildTree(data.positive.toLowerCase()));
//            List<Integer> ANSIDList = new ArrayList<>();
//            String[] idArr = data.answer.split(" ");
//            for (String id : idArr) {
//                if (!id.equals("#"))
//                    ANSIDList.add(Integer.parseInt(id));
//                else
//                    break;
//            }
//
//            for (int id : ANSIDList) {
//                DNode node = graph_P.getNodeById(id);
//                System.out.print(node.getForm() + "-" + node.getId() + " ");
//            }
//            System.out.println();
//
//            FeatureExtraction fe = new FeatureExtraction();
//            HashMap<String, String> feaname_feavalue = fe.extractFeatuers(graph_Q, graph_P, ANSIDList);
//            String wh_ansposvalue = feaname_feavalue.get(WH_ANSPos);
//            String wh_ansdepvalue = feaname_feavalue.get(WH_ANSDep);
//            String whdep_ansdepvalue = feaname_feavalue.get(WHDep_ANSDep);
//
//            System.out.println("negative = " + data.negative);
//            System.out.println("wh_ansposvalue = " + wh_ansposvalue);
//            System.out.println("wh_ansdepvalue = " + wh_ansdepvalue);
//
//            List<String> instances = new ArrayList<>();
//            if (wh_anspos_features.containsKey(wh_ansposvalue)) {
//                instances = wh_anspos_features.get(wh_ansposvalue);
//            } else {
//                instances = new ArrayList<>();
//            }
//            instances.add(data.id);
//            wh_anspos_features.put(wh_ansposvalue, instances);
//
//            instances = new ArrayList<>();
//            if (wh_ansdep_features.containsKey(wh_ansdepvalue)) {
//                instances = wh_ansdep_features.get(wh_ansdepvalue);
//            } else {
//                instances = new ArrayList<>();
//            }
//            instances.add(data.id);
//            wh_ansdep_features.put(wh_ansdepvalue, instances);
//
//            instances = new ArrayList<>();
//            if (whdep_ansdep_features.containsKey(whdep_ansdepvalue)) {
//                instances = whdep_ansdep_features.get(whdep_ansdepvalue);
//            } else {
//                instances = new ArrayList<>();
//            }
//            instances.add(data.id);
//            whdep_ansdep_features.put(whdep_ansdepvalue, instances);
//        }
//
////        // print whWords_ansdep -> [1,2,3,....]
////        printfeatures(wh_ansdep_features);
////
////        // print whWords_anspos -> [1,2,3,...]
////        printfeatures(wh_anspos_features);
//
//        // print whdep_ansdep -> [1,2,3,...]
//        printfeatures(whdep_ansdep_features);
//
//
//
//
////        String T = "Among them was Christa McAulife, the first private citizen to fly in space.";
////        DTree dtree_T = DTree.buildTree(T.toLowerCase());
////        Graph graph_T = Graph.buildDGraph(dtree_T);
////        System.out.println("\nsubgraph_T1 = " + T +"\n" + graph_T.toString());
////
////        String H = "Name the first private citizen to fly in space.";
////        DTree dtree_H = DTree.buildTree(H.toLowerCase());
////        Graph graph_H = Graph.buildDGraph(dtree_H);
////        System.out.println("\nsubgraph_Q1 = \n" + H + "\n" + graph_H.toString());
////
////        List<Integer> ANSIDList = new ArrayList<>(Arrays.asList(new Integer[]{Integer.parseInt("1"), Integer.parseInt("5")}));
////        List<DNode> ANSDNodeList = getAnsDNodeList(graph_T, ANSIDList);
////        DNode closestAncestor = graph_T.getMostClosestAncestor(ANSDNodeList);
////        System.out.println("closestAncestor = " + closestAncestor);
////
////        FeatureExtraction fe = new FeatureExtraction();
////        fe.extractFeatuers(graph_H, graph_T, ANSIDList);
//
//    }
//
//    public static HashMap<String, String> computeWHAnsPos(Graph graph, List<DNode> ANSDNodeList) {
//
//        HashMap<String, String> feaName_feaValue = new HashMap<>();
//        String ANSPosValue = "";
//        String ANSDepValue = "";
////        for (DNode dnode : ANSDNodeList) {
////            String pos = dnode.getPOS();
////            String dep = dnode.getDepLabel();
////
////            if (flagGroupPos)
////                pos = groupPos(pos);
////            if (flagGroupDep)
////                dep = groupDep(dep);
////
////            ANSPosValue += ANSPosValue.isEmpty() ? pos : ("_" + pos);
////            ANSDepValue += ANSDepValue.isEmpty() ? dep : ("_" + dep);
////            feaName_feaValue.put(ANSPos, ANSPosValue);
////            feaName_feaValue.put(ANSDep, ANSDepValue);
////        }
//        DNode head = graph.getLowestCommonAncestor(ANSDNodeList);
//        String pos = head.getPOS();
//        String dep = head.getDepLabel();
//
//        if (flagGroupPos)
//            pos = groupPos(pos);
//        if (flagGroupDep)
//            dep = groupDep(dep);
//
//        ANSPosValue += ANSPosValue.isEmpty() ? pos : ("_" + pos);
//        ANSDepValue += ANSDepValue.isEmpty() ? dep : ("_" + dep);
//        feaName_feaValue.put(ANSPos, ANSPosValue);
//        feaName_feaValue.put(ANSDep, ANSDepValue);
//        return feaName_feaValue;
//    }
//
//    public static List<RTEData> readXML(String filepath) {
//
//        List<String> questionList = new ArrayList<>();
//        List<String> positiveList = new ArrayList<>();
//        List<String> answerList = new ArrayList<>();
//        List<RTEData> dataList = new ArrayList<>();
//
//        String pairStart = "<QApairs id=";
//        String pairEnd = "</QApairs>";
//        String questionTag = "<question>";
//        String positiveStart = "<positive>";
//        String positiveEnd = "</positive>";
//        String negativeStart = "<negative>";
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(filepath));
//            String line = null;
//            String id = "";
//            String question = "";
//            String positive = "";
//            String answer = "";
//            String negative = "";
//            while ((line = br.readLine()) != null) {
//
//                if (line.startsWith(pairStart)) {
//                    boolean positivestarted = true;
//                    boolean negativestarted = true;
////                    System.out.println();
////                    System.out.println("id = " + id);
////                    System.out.println("question = " + question);
////                    System.out.println("positive = " + positive);
////                    System.out.println("answer = " + answer);
//                    if (!id.isEmpty()) {
//                        RTEData data = new RTEData(id, question, positive, answer, negative);
//                        dataList.add(data);
////                        questionList.add(question);
////                        positiveList.add(positive);
////                        answerList.add(answer);
//                    }
//
//                    id = line.substring(pairStart.length()+1, line.length()-2);
//                    while (!(line = br.readLine()).equals(pairEnd)) {
//                        if (line.startsWith(questionTag)) {
//                            question = br.readLine().trim().replaceAll("\t", " ");
//                        }
//                        if (line.startsWith(positiveStart) && positivestarted == true) {
//                            positivestarted = false;
//                            positive = br.readLine().trim().replaceAll("\t", " ");
//                            String prev = line;
//                            while (!(line = br.readLine()).equals(positiveEnd)) {
//                                prev = line;
//                            }
//                            answer = prev.trim().replaceAll("\t", " ");
//                        }
//                        if (line.startsWith(negativeStart) && negativestarted == true) {
//                            negativestarted = false;
//                            negative = br.readLine().trim().replaceAll("\t", " ");
//                        }
//                    }
//                }
//            }
//
////            System.out.println();
////            System.out.println("id = " + id);
////            System.out.println("question = " + question);
////            System.out.println("positive = " + positive);
////            System.out.println("answer = " + answer);
////            questionList.add(question);
////            positiveList.add(positive);
////            answerList.add(answer);
//            RTEData data = new RTEData(id, question, positive, answer, negative);
//            dataList.add(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return dataList;
//    }
//
//    private static void printfeatures(HashMap<String, List<String>> features) {
//
//        features.forEach((feavalue, ids) -> {
//            System.out.println(feavalue + " = " + ids.size() + " \t " + ids);
//        });
//    }
//
//    private static String groupPos(String pos) {
//
//        return pos;
//    }
//
//    private static String groupDep(String dep) {
//
//        if (NodeComparer.SubjSet.contains(dep))
//            return "subj";
//        else if (NodeComparer.ObjSet.contains(dep))
//            return "obj";
//        else if (NodeComparer.AdjSet.contains(dep))
//            return "adj";
//        else
//            return "other";
//    }
//}