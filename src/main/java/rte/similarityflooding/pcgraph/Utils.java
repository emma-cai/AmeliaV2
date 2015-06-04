package rte.similarityflooding.pcgraph;

import com.google.common.collect.Sets;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.LangLib;
import rte.datastructure.SimpleNode;
import org.jgrapht.graph.DefaultWeightedEdge;
import rte.similarityflooding.Edge;

import java.util.Set;

/**
 * Utility class.
 */
public class Utils {
    /**
     * Given a graph, return the edges in that graph.
     * @param graph
     * @return
     */
    public static Set<Edge> getEdges(Graph graph) {
        Set<Edge> returnSet = Sets.newHashSet();

        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            Object s = graph.getEdgeSource(edge);
            Object t = graph.getEdgeTarget(edge);

            String label = "";

            // FIXME: behavior depends on type of node
            if (t instanceof DNode) {
                DNode node = (DNode) t;
                label = node.getDepLabel();
            }
            else if (t instanceof SimpleNode) {
                SimpleNode nodeSource = (SimpleNode) s;
                SimpleNode nodeTarget = (SimpleNode) t;
                label = nodeTarget.getDepLabel(nodeSource);
            }
            else {
                label = edge.toString();
            }

            // Ignore punctuation and root.
            Set<String> ignoreList = Sets.newHashSet(LangLib.DEP_PUNCT, LangLib.DEP_ROOT);
            if(ignoreList.contains(label))  {
                continue;
            }

            returnSet.add(new Edge(s, label, t));
        }

        return returnSet;
    }


    /**
     * Do the edges qualify as matching?
     * FIXME: requires DNode; the method is therefore not general
     * @param source1
     * @param source2
     * @return
     */
    public static boolean nodesMatch(Object source1, Object source2) {
        if (! (source1 instanceof DNode) || ! (source2 instanceof DNode) )   {
            return false;
        }
        DNode node1 = (DNode) source1;
        DNode node2 = (DNode) source2;
        return node1.getLemma().equals(node2.getLemma());
    }

}
