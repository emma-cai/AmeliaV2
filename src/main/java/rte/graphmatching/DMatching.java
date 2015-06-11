package rte.graphmatching;

import com.google.common.collect.Maps;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.MatchedGraph;
import rte.similarityflooding.NodePair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

/**
 * Created by qingqingcai on 5/12/15.
 */
public class DMatching {

    public static boolean exclusiveCheck = true;
    public static boolean debugInComputeVertexCost = false;
    public static boolean debugInComputePathCost = false;
    public static final Set<String> postagSet = NodeComparer.postagSet;
    public static int MAXCP = 1;

    /** **************************************************************
     * Compute the cost of matching graph_T to graph_H. Basically, the
     * goal is to find the matching with lowest cost.
     */
    public static double computeMatchingCost(Graph graph_T, Graph graph_H, HashMap nodeMatches) {

        double minMC;
        List<MatchedGraph> bestMatchedGraphListInVC = computeVertexCost(graph_T, graph_H, nodeMatches, null);
        double minMCInVC = Double.MAX_VALUE;
        // Select bestMatchedGraphList by computing VertexCost
        if (!bestMatchedGraphListInVC.isEmpty())
            minMCInVC = bestMatchedGraphListInVC.get(0).getMatchingCost();
        minMC = minMCInVC;

//        List<MatchedGraph> bestMatchedGraphListByAddingPathCost = computePathCost(graph_T, graph_H, bestMatchedGraphListInVC);
//        double minimumMCByAddingPC = minMCInVC;
//        if (!bestMatchedGraphListByAddingPathCost.isEmpty())
//            minimumMCByAddingPC = bestMatchedGraphListByAddingPathCost.get(0).getMatchingCost();
//        System.out.println("After computing path cost, the final matchingCost = " + minimumMCByAddingPC);
//
//        minMC = minimumMCByAddingPC;

        return minMC;
    }

    /** **************************************************************
     * Given two graphs, get node pairs which are sorted by similarity
     * score (from initial value or similarity flooding algorithm)
     */
    public static HashMap<DNode, NavigableMap<Double, List<NodePair>>> initNodeMatches(
            Graph graph_T, Graph graph_H, RteConfiguration config) {

        if(config == null) {
            throw new IllegalArgumentException("RteConfiguration is null.");
        }

        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);
        comparer.init(graph_T, graph_H, config);

        Map<NodePair, Double> initVals = computeGraphComparerInitValues(comparer.getPCGraphNodes());

        Set<NodePair> actualSet = comparer.compareGraphNodes(initVals);

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_sim_NodePairList = groupOnDNodeAndSimilarity(actualSet);

        return nodeH_sim_NodePairList;
    }

    /** **************************************************************
     * Return the desired initial values for the given NodePair set.
     * @param nodePairSet
     * @return
     */
    public static Map<NodePair, Double> computeGraphComparerInitValues(Set<NodePair> nodePairSet)    {
        Map<NodePair, Double> initVals = Maps.newHashMap();
        VertexSub vs = new VertexSub();
        for (NodePair pair : nodePairSet) {
            DNode node1 = (DNode) pair.node1;
            DNode node2 = (DNode) pair.node2;
            double nodesim = vs.getNodeSimilarity(node1, node2);
            initVals.put(new NodePair(node1, node2), nodesim);
        }
        return initVals;
    }

    /** **************************************************************
     * For each key_node, return list of a map<Double, NodePair>, where the
     * key is similarity_score, value is a node_pair; If inverse = true,
     * key_node is on the right side of node_pair, otherwise, key_node
     * is on the left side of node_pair;
     */
    public static HashMap<DNode, NavigableMap<Double, List<NodePair>>> groupOnDNodeAndSimilarity(Set<NodePair> actualSet) {

        HashMap<DNode, TreeMap<Double, List<NodePair>>> nodeH_sim_nodePairList = new HashMap<>();
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);
        actualList.forEach(nodePair -> {
            DNode node2 = (DNode) nodePair.node2;
            Double sim = nodePair.sim;
            TreeMap<Double, List<NodePair>> sim_nodePairList = new TreeMap<>();
            List<NodePair> nodePairList = new ArrayList<>();
            if (nodeH_sim_nodePairList.containsKey(node2)) {
                sim_nodePairList = nodeH_sim_nodePairList.get(node2);
                if (sim_nodePairList.containsKey(sim)) {
                    nodePairList = sim_nodePairList.get(sim);
                }
            }
            nodePairList.add(nodePair);
            sim_nodePairList.put(sim, nodePairList);
            nodeH_sim_nodePairList.put(node2, sim_nodePairList);
        });

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> result = new HashMap<>();
        for (DNode nodeH : nodeH_sim_nodePairList.keySet()) {
            NavigableMap sorted = (NavigableMap) nodeH_sim_nodePairList.get(nodeH).descendingMap();
            result.put(nodeH, sorted);
        }
        return result;
    }

    /** **************************************************************
     * Compute vertex substitution cost to match graph_T to graph_H,
     * and node_SortedMapList helps to find the best matching node with
     * highest similarity score;
     */
    public static List<MatchedGraph> computeVertexCost(Graph graph_T, Graph graph_H,
                                                       HashMap nodeH_sim_NodePairList,
                                                       List<MatchedGraph> MatchedGraphList) {

        if (debugInComputeVertexCost)
            System.out.println("\n-------ComputVertexCost Started--------");

        List<MatchedGraph> bestMatchedGraphList = new ArrayList<>();
        double VertexCost = 0.0;
        double normalizationConstant = 0.0;
        boolean started = true;
        for (Object object : graph_H.vertexSet()) {
            DNode dnode_H = (DNode) object;

//            if (exclusiveCheck && VertexSub.excludeNodes(dnode_H))
//                continue;

            // compute the importance of the node in H
            double dnode_weight = VertexSub.Importance(graph_H, dnode_H);
            NavigableMap<Double, List<NodePair>> sim_NodePairList =
                    (NavigableMap<Double, List<NodePair>>) nodeH_sim_NodePairList.get(dnode_H);

            if (sim_NodePairList != null) {

                // compute minimum VertexCost
                List<NodePair> matchedNodePairList = sim_NodePairList.entrySet().iterator().next().getValue();
                boolean stop = false;
                for (int index = 0; index < matchedNodePairList.size() && stop == false; index++) {
                    NodePair matchednodepair = matchedNodePairList.get(index);
                    DNode dnode_T = (DNode) matchednodepair.node1;

//                    if (exclusiveCheck && VertexSub.excludeNodes(dnode_T))
//                        continue;

                    normalizationConstant += dnode_weight;
                    double vertexSubValue = 1.0 - matchednodepair.sim;
                    VertexCost += dnode_weight * vertexSubValue;

                    if (debugInComputeVertexCost) {
                        System.out.println("node_in_H = " + dnode_H.getForm() + "-" + dnode_H.getId() + "\n\t"
                                + "best_matched_node_in_T = " + dnode_T.getForm() + "-" + dnode_T.getId() + "\n\t"
                                + "nodeSimilarity = " + matchednodepair.sim + "\n\t"
                                + "vertexSubValue = " + vertexSubValue + "\n\t"
                                + "dnode_weight = " + dnode_weight + "\n\t"
                                + "vertexSubValue*dnode_weight = " + dnode_weight*vertexSubValue + "\n\t"
                                + "VertextCost (no normalization) = " + VertexCost);
                    }

                    // collect all matching candidates with the same minimum VertexCost
                    if (started) {
                        for (int i = 0; i < MAXCP && i < matchedNodePairList.size(); i++) {
                            NodePair nodepair = matchedNodePairList.get(i);
                            MatchedGraph mg = new MatchedGraph();
                            mg.addNodePair(nodepair);               // add nodepair
                            bestMatchedGraphList.add(mg);
                        }
                        started = false;
                    } else {
                        List<MatchedGraph> tmp = new ArrayList<>();

                        for (int i = 0; i < bestMatchedGraphList.size(); i++) {
                            MatchedGraph currentMG = bestMatchedGraphList.get(i);
                            MatchedGraph mg = new MatchedGraph(currentMG.getMatchedNodes());

                            for (int m = 0; m < MAXCP && m < matchedNodePairList.size(); m++) {
                                NodePair nodepair = matchedNodePairList.get(m);
                                mg.addNodePair(nodepair);
                                tmp.add(mg);
                                mg = new MatchedGraph(currentMG.getMatchedNodes());
                            }
                        }

                        bestMatchedGraphList = new ArrayList<>();
                        bestMatchedGraphList.addAll(tmp);
                    }

                    stop = true;
                }
            }
        }

        if (debugInComputeVertexCost)
            System.out.println("-------ComputVertexCost Finished-------\n");

        for (MatchedGraph bmg : bestMatchedGraphList) {
            bmg.setVertexCost(VertexCost / normalizationConstant);
            bmg.computeMatchingCost(1.0);
        }
        return bestMatchedGraphList;
    }

    /** **************************************************************
     * Compute path substitution cost to match graph_T to graph_H
     */
    public static List<MatchedGraph> computePathCost(
            Graph graph_T, Graph graph_H, List<MatchedGraph> matchedGraphList) {

        if (debugInComputePathCost)
            System.out.println("\n-------ComputePathCost Started-------");

        List<MatchedGraph> bestMatchedGraphList = new ArrayList<>();
        PathSub ps = new PathSub();

        for (MatchedGraph mg : matchedGraphList) {

            double PathCost = 0.0;
            double normalizationConstant = 0.0;

            for (DefaultWeightedEdge edge_H : graph_H.edgeSet()) {
                double edge_weight = 1.0;
                normalizationConstant += PathSub.Importance(edge_H);
                double pathSubValue = ps.getPathSub(graph_T, graph_H, edge_H, mg);
                PathCost += edge_weight * pathSubValue;
            }

            mg.setPathCost(PathCost / normalizationConstant);
            mg.computeMatchingCost(0.8);   // TODO: manually change the weight
        }

        bestMatchedGraphList = MatchedGraph.getBestMatchedGraphList(matchedGraphList);

        if (debugInComputePathCost)
            System.out.println("-------ComputePathCost Finished-------\n");

        return bestMatchedGraphList;
    }

    /** **************************************************************
     * Compute f = (exp(weightVector * valueVector))
     *              / (1.0 + exp(weightVector * valueVector))
     * @param weightVector Weight vector
     * @param feaVector Feature vector
     * @return
     */
    public static double computeExpFun(Vector<Double> weightVector, Vector<Double> feaVector) {

        if (weightVector.size() != feaVector.size()) {
            System.err.println("Feature dimension and weight dimenstion are NOT identical!");
            System.err.println("Feature = " + feaVector);
            System.err.println("FeatureWeight = " + weightVector);
            System.exit(-1);
        }

        double tmp = 0.0;
        for (int i = 0; i < feaVector.size(); i++) {
            tmp += (feaVector.get(i) * weightVector.get(i));
        }

        return Math.exp(tmp) / (1 + Math.exp(tmp));
    }

    /**  **************************************************************
     * Fixme: this method should be removed when similarity flooding
     * algorithm is implemented;
     */
    private static Map<NodePair, NodePair> compareGraphNodesWithoutSF(Map<NodePair, Double> initVals)  {

        // Using map for fast lookup.
        Map<NodePair, NodePair> fixpointVals = Maps.newHashMap();
        initVals.forEach((pair, sim) -> {
            pair.sim = sim;
            fixpointVals.put(pair, pair);
        });

        return fixpointVals;
    }
}
