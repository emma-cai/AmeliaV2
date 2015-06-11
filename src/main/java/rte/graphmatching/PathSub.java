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

    public static boolean PathSubDebug = false;
    public Double[] PathSubWeights = {0.1, 0.5, 0.0, 0.4};

    /** **************************************************************
     * Compute path substitution cost. This gives cost for substituting
     * the path relationship between two paths in one graph_H for that
     * in another graph_T;
     */
    public double getPathSub(Graph graph_T, Graph graph_H, DefaultWeightedEdge edge_H, MatchedGraph mg) {

        double pathSubValue = 0.0;

        // get v(sourceV) and v'(targetV) given edge_H
        DNode sourceV = (DNode) graph_H.getEdgeSource(edge_H);
        DNode targetV = (DNode) graph_H.getEdgeTarget(edge_H);

        // get the best mapped nodes for v and v'
        List<NodePair> sourceVMappedPairList = mg.getNodePair(sourceV, true);
        List<NodePair> targetVMappedPairList = mg.getNodePair(targetV, true);
        DNode sourceVMapped = null;
        DNode targetVMapped = null;
        List<String> pathLabel_T = new ArrayList<>();
        List path_T = new ArrayList<>();
        if (!sourceVMappedPairList.isEmpty() && !targetVMappedPairList.isEmpty()) {
            NodePair sourceVMappedPair = sourceVMappedPairList.get(0);
            NodePair targetVMappedPair = targetVMappedPairList.get(0);
            sourceVMapped = (DNode) sourceVMappedPair.node1;
            targetVMapped = (DNode) targetVMappedPair.node1;
            pathLabel_T = new ArrayList<>();
            path_T = graph_T.findShortestPath(sourceVMapped, targetVMapped);
            for (int i = 0; i < path_T.size(); i++) {
                pathLabel_T.add(((DNode) graph_T.getEdgeTarget((DefaultWeightedEdge) path_T.get(i))).getDepLabel());
            }
        }

        pathSubValue = getPathSub(graph_T, graph_H, sourceV, targetV, sourceVMapped, targetVMapped, path_T, pathLabel_T);

        if (PathSubDebug) {
            System.out.println("--------------------------------------------------");
            System.out.println("edge_H: " + sourceV.getForm()+"-"+sourceV.getId() + "\t"
                    + targetV.getForm()+"-"+targetV.getId() + "\t" + targetV.getDepLabel());
            if (sourceVMapped != null)
                System.out.print("edge_T: " + sourceVMapped.getForm() + "-" + sourceVMapped.getId() + "\t");
            else
                System.out.print("edge_T: " + null + "-" + null + "\t");
            if (targetVMapped != null)
                System.out.println(targetVMapped.getForm() + "-" + targetVMapped.getId() + "\t" + pathLabel_T.toString());
            else
                System.out.println(null + "-" + null + pathLabel_T.toString());
            System.out.println("sourceV: " + sourceV);
            System.out.println("targetV: " + targetV);
            if (sourceVMapped != null)
                System.out.println("sourceVMappedInT: " + sourceVMapped);
            else
                System.out.println("sourceVMappedInT: " + null);
            if (targetVMapped != null)
                System.out.println("targetVMappedInT: " + targetVMapped);
            else
                System.out.println("targetVMappedInT: " + null);
            System.out.println("pathSubValue = " + pathSubValue);
            System.out.println("--------------------------------------------------\n");
        }

        return pathSubValue;
    }

    /** **************************************************************
     * Compute path substitution cost for each give node pair.
     */
    private double getPathSub(
            Graph graph_T, Graph graph_H, DNode sourceV, DNode targetV,
            DNode sourceVMapped, DNode targetVMapped, List path_T, List<String> pathLabel_T) {

        Vector<Double> fie_vector = getPathSubFeatureVector(
                graph_T, graph_H, sourceV, targetV, sourceVMapped, targetVMapped, path_T, pathLabel_T);

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

    /** **************************************************************
     * Compute path substitution binary feature vector: ExactMatch,
     * PartialMatch, AncestorMatch and KinkedMatch;
     */
    private Vector<Double> getPathSubFeatureVector(
            Graph graph_T, Graph graph_H, DNode sourceV, DNode targetV,
            DNode sourceVMapped, DNode targetVMapped, List path_T, List<String> pathLabel_T) {

        Vector<Double> fie_vector = new Vector<>();

        double exactMatch_tag = doExactMatch(targetV, targetVMapped, path_T);
        double partialMatch_tag = doPartialMatch(targetV, pathLabel_T);
        double ancestorMatch_tag = doAncestorMatch(graph_T, sourceVMapped, targetVMapped);
        double kinkedMatch_tag = doKinkedMatch(graph_T, sourceVMapped, targetVMapped);

        fie_vector.add(exactMatch_tag);
        fie_vector.add(partialMatch_tag);
        fie_vector.add(ancestorMatch_tag);
        fie_vector.add(kinkedMatch_tag);

        return fie_vector;
    }

    /** **************************************************************
     * Check if M(v) -> M(v') is an edge in T with the same label as
     * edge v -> v' in H;
     */
    private static double doExactMatch(DNode targetV, DNode targetVMapped, List path_T) {

        if (path_T == null || path_T.isEmpty() || path_T.size() > 1)
            return 1.0;
        if (targetV.getDepLabel().equals(targetVMapped.getDepLabel()))
            return 0.0;
        return 1.0;
    }

    /** **************************************************************
     * Check if M(v) -> M(v') contains an equivalent relationship as
     * the dependency label for v;
     */
    private static double doPartialMatch(DNode targetV, List<String> pathLabel_T) {

        for (String label : pathLabel_T) {
            for (Set<String> equalLabelSet : NodeComparer.equalDeplabelSet) {
                if (equalLabelSet.contains(label)
                        && equalLabelSet.contains(targetV.getDepLabel()))
                    return 0.0;
            }
        }
        return 1.0;
    }

    /** **************************************************************
     * Check if M(v) is an ancestor of M(v'); Use an exponentially
     * increasing cost based on the longer distance relationships;
     */
    private static double doAncestorMatch(Graph graph_T, DNode sourceVMapped, DNode targetVMapped) {

        if (sourceVMapped ==null || targetVMapped == null)
            return 1.0;
        int generation = graph_T.isAncestorOf(sourceVMapped, targetVMapped);
        if ( generation < 1)
            return 1.0;
        double exp = Math.exp(generation);
        return exp / (1.0 + exp);
    }

    /** **************************************************************
     * Check if M(v) and M(v') share a common parent or ancestor in T;
     * Use an exponentially increasing cost based on the minimum of the
     * node's distance to their least common ancestor in T;
     */
    private static double doKinkedMatch(Graph graph_T, DNode sourceVMapped, DNode targetVMapped) {

        DNode node = graph_T.getLowestCommonAncestor
                (new ArrayList<>(Arrays.asList(sourceVMapped, targetVMapped)));
        if (node == null)
            return 1.0;
        double exp = Math.exp(graph_T.findShortestPath(node, sourceVMapped).size());
        return exp / (1.0 + exp);
    }


    /** **************************************************************
     * TODO: compute the importance weight for edge_H
     * Compute the importance of edge in H; Intuitively, subject and
     * object relations are more important to match than other;
     */
    public static double Importance(DefaultWeightedEdge edge) {
        return 1.0;
    }

}