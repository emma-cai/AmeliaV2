package rte.graphmatching;

import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.MatchedGraph;
import rte.similarityflooding.NodePair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

/**
 * Created by qingqingcai on 5/20/15.
 */
public class PathSub {

    public boolean PathSubDebug = true;
    public Double[] PathSubWeights = {0.3, 0.2, 0.2, 0.3};

    /** **************************************************************
     * TODO: compute the importance weight for edge_H
     */
    public static double Importance(DefaultWeightedEdge edge) {
        return 1.0;
    }

    /** **************************************************************
     * Compute PathSub
     */
    public double getPathSub(Graph graph_T, Graph graph_H, DefaultWeightedEdge edge_H, MatchedGraph mg) {

        double pathSubValue = 0.0;

        // get source and target node given edge_H
        DNode sourceV = (DNode) graph_H.getEdgeSource(edge_H);
        DNode targetV = (DNode) graph_H.getEdgeTarget(edge_H);

        // get the best mapping node source_T for source_H; get the best mapping node target_T for target_H;
        List<NodePair> sourceVMappedPairList = mg.getNodePair(sourceV, true);
        List<NodePair> targetVMappedPairList = mg.getNodePair(targetV, true);

        if (!sourceVMappedPairList.isEmpty() && !targetVMappedPairList.isEmpty()) {
            NodePair sourceVMappedPair = sourceVMappedPairList.get(0);
            NodePair targetVMappedPair = targetVMappedPairList.get(0);
            DNode sourceVMapped = (DNode) sourceVMappedPair.node1;
            DNode targetVMapped = (DNode) targetVMappedPair.node1;

            List pathLabel_T = new ArrayList<>();
            List path_T = graph_T.findShortestPath(sourceVMapped, targetVMapped);
            for (int i = 0; i < path_T.size(); i++) {
                pathLabel_T.add(((DNode) graph_T.getEdgeTarget((DefaultWeightedEdge) path_T.get(i))).getDepLabel());
            }


            pathSubValue = getPathSub(graph_T, graph_H, sourceV, targetV, sourceVMapped, targetVMapped, path_T);

            if (PathSubDebug) {
                System.out.println("--------------------------------------------------");
                System.out.println("edge_H: " + sourceV.getForm()+"-"+sourceV.getId() + "\t" + targetV.getForm()+"-"+targetV.getId()/** + "\t" + pathLabel_H.toString()**/);
                System.out.println("edge_T: " + sourceVMapped.getForm()+"-"+sourceVMapped.getId() + "\t" + targetVMapped.getForm()+"-"+targetVMapped.getId() + "\t" + pathLabel_T.toString());
                System.out.println("sourceV: " + sourceV);
                System.out.println("targetV: " + targetV);
                System.out.println("sourceVMappedInT: " + sourceVMapped);
                System.out.println("targetVMappedInT: " + targetVMapped);
                System.out.println("pathSubValue = " + pathSubValue);
                System.out.println("--------------------------------------------------\n");
            }
        } else {
            if (PathSubDebug) {
                System.out.println("--------------------------------------------------");
                System.out.println("sourceV: " + sourceV);
                System.out.println("targetV: " + targetV);
                System.out.println("sourceVMappedInT: " + null);
                System.out.println("targetVMappedInT: " + null);
                System.out.println("pathSubValue = " + pathSubValue);
                System.out.println("--------------------------------------------------\n");
            }
        }

        return pathSubValue;
    }

    private double getPathSub(
            Graph graph_T, Graph graph_H, DNode sourceV, DNode targetV,
            DNode sourceVMapped, DNode targetVMapped, List path_T) {

        Vector<Double> fie_vector = getPathSubFeatureVector(
                graph_T, graph_H, sourceV, targetV, sourceVMapped, targetVMapped, path_T);

        if (PathSubDebug) {
            System.out.println("*************Feature Vector**************");
            System.out.println("exactMatch_tag = " + fie_vector.get(0));
            System.out.println("partialMatch_tag = " + fie_vector.get(1));
            System.out.println("ancestorMatch_tag = " + fie_vector.get(2));
            System.out.println("kinkedMatch_tag = " + fie_vector.get(3));
            System.out.println("***************************");
        }

        Vector<Double> fie_weight_vector = new Vector();
        Collections.addAll(fie_weight_vector, PathSubWeights);

        double PathSub = DMatching.computeExpFun(fie_weight_vector, fie_vector);

        return PathSub;
    }

    private Vector<Double> getPathSubFeatureVector(
            Graph graph_T, Graph graph_H, DNode sourceV, DNode targetV,
            DNode sourceVMapped, DNode targetVMapped, List path_T) {

        Vector<Double> fie_vector = new Vector<>();

        // TODO: Should implement these methods
        double exactMatch_tag = doExactMatch(targetV, targetVMapped, path_T);
        double partialMatch_tag = doPartialMatch(graph_H, targetVMapped, path_T);
        double ancestorMatch_tag = doAncestorMatch(graph_T, sourceVMapped, targetVMapped);
        double kinkedMatch_tag = doKinkedMatch(graph_T, sourceVMapped, targetVMapped);

        fie_vector.add(exactMatch_tag);
        fie_vector.add(partialMatch_tag);
        fie_vector.add(ancestorMatch_tag);
        fie_vector.add(kinkedMatch_tag);

        return fie_vector;
    }

    private static double doExactMatch(DNode targetV, DNode targetVMapped, List path_T) {

        if (path_T == null || path_T.isEmpty() || path_T.size() > 1)
            return 1.0;
        if (targetV.getDepLabel().equals(targetVMapped.getDepLabel()))
            return 0.0;
        return 1.0;
    }

    private static double doPartialMatch(Graph graph_H, DNode targetVMapped, List path_T) {

        if (path_T == null || path_T.isEmpty() || path_T.size() > 1)
            return 1.0;
        for (Object object : graph_H.vertexSet()) {
            DNode nodeH = (DNode) object;
            if (targetVMapped.getDepLabel().equals(nodeH.getDepLabel()))
                return 0.0;
        }
        return 1.0;
    }

    private static double doAncestorMatch(Graph graph_T, DNode sourceVMapped, DNode targetVMapped) {

        int generation = graph_T.isAncestorOf(sourceVMapped, targetVMapped);
        if ( generation < 1)
            return 1.0;
        double exp = Math.exp(generation);
        return exp / (1.0 + exp);
    }

    private static double doKinkedMatch(Graph graph_T, DNode sourceVMapped, DNode targetVMapped) {

        DNode node = graph_T.getLowestCommonAncestor(new ArrayList<>(Arrays.asList(sourceVMapped, targetVMapped)));
        if (node == null)
            return 1.0;
        double exp = Math.exp(graph_T.findShortestPath(node, graph_T.getNodeById(0)).size());
        return exp / (1.0 + exp);
    }

    private static Map.Entry<NodePair, Double> getBestMappingNodePair(List<Map.Entry<NodePair, Double>> matchedList) {

        if (matchedList == null)
            return null;
        return matchedList.get(0);
    }


}
