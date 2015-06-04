package rte.similarityflooding.pcgraph;

import rte.datastructure.Graph;
import rte.similarityflooding.Edge;
import rte.similarityflooding.NodePair;

import java.util.Set;

/**
 * Different implementations of a pairwise connectivity graph--a graph merging the nodes of two separate graphs.
 */
public interface PCGraph {

    /**
     * Create a pairwise connectivity graph on the two input graphs.
     * @param graph1
     * @param graph2
     * @return
     */
    public Graph calcPCGraph(Graph graph1, Graph graph2);

    /**
     * Get the node pairs of this pairwise connectivity graph.
     * @return
     */
    Set<NodePair> getPCGraphNodes();

    /**
     * Return the edges of the pairwise connectivity graph.
     * @return
     */
    Set<Edge> getPCGraphEdges();
}
