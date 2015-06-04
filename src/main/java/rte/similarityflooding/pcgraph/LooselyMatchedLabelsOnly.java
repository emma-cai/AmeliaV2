package rte.similarityflooding.pcgraph;

import com.google.common.collect.Sets;
import rte.datastructure.Graph;
import rte.similarityflooding.Edge;
import rte.similarityflooding.NodePair;

import java.util.Set;

import static rte.similarityflooding.NodePair.getNodesInGraph;

/**
 * Creates a PCGraph where the labels between the nodes in the two graphs must loosely or identically match for them to be included in this
 * PC Graph.
 */
// FIXME: not finished
public class LooselyMatchedLabelsOnly implements PCGraph {
    // FIXME: Perhaps all PCGraph implementations should share these fields via a base class?
    // FIXME: we need Edge objects to verify edge labels; we need DefaultWeightedEdge objects in the Graph object
    private Set<Edge> pcGraphEdges = Sets.newHashSet();

    private Set<NodePair> pcGraphNodes = Sets.newHashSet();

    /**
     * Create a pairwise connectivity graph on the two input graphs.
     *
     * @param graph1
     * @param graph2
     * @return
     */
    @Override
    public Graph calcPCGraph(Graph graph1, Graph graph2) {

        Set<Edge> leftGraphEdges = Utils.getEdges(graph1);
        Set<Edge> rightGraphEdges = Utils.getEdges(graph2);

        for (Edge edgeL : leftGraphEdges) {
            for (Edge edgeR : rightGraphEdges) {
                if (edgeL.matchesLoosely(edgeR)) {
                    NodePair pairL = new NodePair(edgeL.source, edgeR.source);
                    NodePair pairR = new NodePair(edgeL.target, edgeR.target);
                    pcGraphEdges.add(new Edge(pairL, edgeL.label, pairR));
                }
            }
        }

        // Now get a permanent copy of the nodes in the pcGraph.
        pcGraphNodes.addAll(getNodesInGraph(pcGraphEdges));

        return Graph.buildGraph(pcGraphEdges);
    }

    @Override
    public Set<NodePair> getPCGraphNodes() {

        return pcGraphNodes;
    }

    @Override
    public Set<Edge> getPCGraphEdges() {

        return Sets.newHashSet(pcGraphEdges);
    }
}
