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

    public boolean PathSubDebug = false;

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
        PathSub ps = new PathSub();

        // get source and target node given edge_H
        DNode source_H = (DNode) graph_H.getEdgeSource(edge_H);
        DNode target_H = (DNode) graph_H.getEdgeTarget(edge_H);

        // get the best mapping node source_T for source_H; get the best mapping node target_T for target_H;
        NodePair source_pair = mg.getNodePair(source_H, true).get(0);
        NodePair target_pair = mg.getNodePair(target_H, true).get(0);

        if (source_pair != null && target_pair != null) {
            DNode source_T = (DNode) source_pair.node1;
            DNode target_T = (DNode) target_pair.node1;

            List pathLabel_H = new ArrayList<>();
            pathLabel_H.add(((DNode) graph_H.getEdgeTarget(edge_H)).getDepLabel());

            List pathLabel_T = new ArrayList<>();
            List path_T = graph_T.findShortestPath(source_T, target_T);
            for (int i = 0; i < path_T.size(); i++) {
                pathLabel_T.add(((DNode) graph_T.getEdgeTarget((DefaultWeightedEdge) path_T.get(i))).getDepLabel());
            }

            pathSubValue = getPathSub(graph_T, graph_H, source_T, target_T, source_H, target_H);

            if (PathSubDebug) {
                System.out.println("\n--------------------------------------------------");
                System.out.println("edge_H: " + source_H.getForm()+"-"+source_H.getId() + "\t" + target_H.getForm()+"-"+target_H.getId() + "\t" + pathLabel_H.toString());
                System.out.println("edge_T: " + source_T.getForm()+"-"+source_T.getId() + "\t" + target_T.getForm()+"-"+target_T.getId() + "\t" + pathLabel_T.toString());
                System.out.println("source_H: " + source_H);
                System.out.println("target_H: " + target_H);
                System.out.println("source_T: " + source_T);
                System.out.println("target_T: " + target_T);
                System.out.println("pathSubValue = " + pathSubValue);
                System.out.println("--------------------------------------------------");
            }
        } else {
            if (PathSubDebug) {
                System.out.println("\n--------------------------------------------------");
                System.out.println("source_H: " + source_H);
                System.out.println("target_H: " + target_H);
                System.out.println("source_T: " + null);
                System.out.println("target_T: " + null);
                System.out.println("pathSubValue = " + pathSubValue);
                System.out.println("--------------------------------------------------");
            }
        }

        return pathSubValue;
    }

    private static double getPathSub(Graph graph_T, Graph graph_H, DNode source_T,
                                     DNode target_T, DNode source_H, DNode target_H) {

        Vector<Double> fie_vector = new Vector<>();

        // TODO: Should implement these methods
        double exactMatch_tag = doExactMatch(target_T, target_H);
        double partialMatch_tag = doPartialMatch( );
        double ancestorMatch_tag = doAncestorMatch(source_T, target_T);
        double kinkedMatch_tag = doKinkedMatch( );

        fie_vector.add(exactMatch_tag);
        fie_vector.add(partialMatch_tag);
        fie_vector.add(ancestorMatch_tag);
        fie_vector.add(kinkedMatch_tag);

        Double[] weights = {0.3, 0.2, 0.2, 0.3};
        Vector<Double> fie_weight_vector = new Vector();
        Collections.addAll(fie_weight_vector, weights);

        double PathSub = DMatching.computeExpFun(fie_weight_vector, fie_vector);

        return PathSub;
    }

    private static double doExactMatch(DNode target_T, DNode target_H) {

        if (target_H.getDepLabel().equals(target_T.getDepLabel()))
            return 0.0;
        return 1.0;
    }

    private static double doPartialMatch() {
        return 0.0;
    }

    private static double doAncestorMatch(DNode source_T, DNode target_T) {

        return 0.0;
    }

    private static double doKinkedMatch() {
        return 0.0;
    }

    private static Map.Entry<NodePair, Double> getBestMappingNodePair(List<Map.Entry<NodePair, Double>> matchedList) {

        if (matchedList == null)
            return null;
        return matchedList.get(0);
    }


}
