//package rte;
//
//import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.Graph;
//import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.AnswerExtraction;
//import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.DMatching;
//import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.RteConfiguration;
//import net.ipsoft.eliza.nlp.ElizaNLPService;
//
//import java.util.HashMap;
//import java.util.List;
//
///**
// * Created by qingqingcai on 5/19/15.
// */
//public class RteSfEvaluator {
//
//    public static String getMinimumCostMatch(ElizaNLPService elizaNLPService, String question, Graph graph_Q,
//                                             List<String> statements, List<Graph> graphs, RteConfiguration config) {
//
//        double minimumCost = Double.MAX_VALUE;
//        Graph bestGraph_T = null;
//        HashMap bestNodeMatches = null;
//        String actual = "Sorry, I don't know the answer.";
//
//        if (graph_Q == null) {
//            graph_Q = Graph.stringToGraph(elizaNLPService, question);
//        }
//
//        for(int i = 0; i < statements.size(); i++)  {
//            String stmt = statements.get(i);
//            Graph graph_T = graphs.get(i);
//
//            HashMap nodeH_sim_NodePairList = DMatching.initNodeMatches(graph_T, graph_Q, config);
//
//            double graphSimilarity = DMatching.computeMatchingCost(graph_T, graph_Q, nodeH_sim_NodePairList);
//
//            if (Double.compare(graphSimilarity, minimumCost) < 0) {
//                minimumCost = graphSimilarity;
//                actual = stmt;
//                bestGraph_T = graph_T;
//                bestNodeMatches = nodeH_sim_NodePairList;
//            }
//        }
//
//        if (bestGraph_T != null && bestNodeMatches != null) {
//            String shortAnswer = AnswerExtraction.runAnswerExtraction(bestGraph_T, graph_Q, bestNodeMatches);
//            if (!shortAnswer.isEmpty()) {
//                actual += "\n Briefly, the answer is " + shortAnswer;
//            }
//        }
//
//        return actual;
//    }
//}
