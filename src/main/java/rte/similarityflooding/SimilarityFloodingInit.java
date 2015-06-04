package rte.similarityflooding;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.datastructure.SimpleNode;
import rte.graphmatching.GraphComparer;
import rte.graphmatching.RteConfiguration;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import rte.similarityflooding.pcgraph.PCGraph;

import java.util.Map;
import java.util.Set;

/**
 * Implements graph matching based on a simplified version of Melnik, Molina, Rahm: "Similarity Flooding: A Versatile Graph Matching Algorithm",
 * aka "ICDE'02". old_compareGraphNodes( ) simply returns the initialized values.
  */
public class SimilarityFloodingInit implements GraphComparer {

    public static final int MIN_ITERATION_NUM = 5;
    // FIXME: When finished testing, make MAX_ITERATION_NUM static final.
    protected int MAX_ITERATION_NUM = 15;

    protected Graph leftGraph;
    protected Graph rightGraph;

    protected RteConfiguration rteConfiguration;

    protected PCGraph pcGraph;

    protected DirectedWeightedMultigraph ipGraph = new DirectedWeightedMultigraph(DefaultWeightedEdge.class);


    @Override
    public void init(Graph g1, Graph g2, RteConfiguration configuration) {

        leftGraph = g1;
        rightGraph = g2;

        rteConfiguration = configuration;

        pcGraph = RteConfiguration.PCGraphType.getPCGraph(configuration.pcGraph);
        MAX_ITERATION_NUM = configuration.nbrSimFloodingIterations;

        calcPCGraph();

        ipGraph = calcInducedPropagationGraph();
    }


    /**
     * For two graphs g1 and g2, score how similar each node of g1 is to each node of g2.
     *
     * @param initVals A set of values for initialization.
     * @return
     */
    @Override
    public Set<NodePair> compareGraphNodes(Map<NodePair, Double> initVals) {

        Map<NodePair, NodePair> fixpointVals = Maps.newHashMap();
        initializeCompareGraphNodes(fixpointVals, initVals);

        return fixpointVals.keySet();
    }

    @Override
    public PCGraph getPCGraph() {
        return pcGraph;
    }

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
            Set<String> ignoreList = Sets.newHashSet("punct", "root");
            if(ignoreList.contains(label))  {
                continue;
            }

            returnSet.add(new Edge(s, label, t));
        }

        return returnSet;
    }

    /**
     * Create the pairwise connectivity graph.
     * @return
     */
    private Graph calcPCGraph( ) {
        return pcGraph.calcPCGraph(leftGraph, rightGraph);
    }

    /**
     * FIXME: this is not a defensive copy
     * @return
     */
    DirectedWeightedMultigraph getInducedPropGraph() {
        return ipGraph;
    }

    /**
     * Return a copy of the nodes in the pairwise connectivity graph.
     * @return
     */
    @Override
    public Set<NodePair> getPCGraphNodes() {
        return Sets.newHashSet(pcGraph.getPCGraphNodes());
    }

    /**
     * For each of the given edges, return a set of source/target pairs.
     * @param edges
     * @return
     */
    public static Set<NodePair> getNodesInGraph(Set<Edge> edges) {
        Set<NodePair> returnSet = Sets.newHashSet();

        for (Edge edge : edges)   {
            returnSet.add((NodePair) edge.source);
            returnSet.add((NodePair) edge.target);
        }

        return returnSet;
    }

    protected void initializeCompareGraphNodes(Map<NodePair, NodePair> fixpointVals, Map<NodePair, Double> initVals) {
        if (initVals != null && ! initVals.isEmpty()) {
            for (Object objVertex : ipGraph.vertexSet()) {
                NodePair nodePair = (NodePair) objVertex;

                if(initVals.containsKey(nodePair))  {
                    nodePair.sim0 = initVals.get(nodePair);
                }
                else    {
                    nodePair.sim0 = 0.0;
                }
                nodePair.sim = nodePair.sim0;
                fixpointVals.put(nodePair, nodePair);
            }
        }
        else    {
            // If initVals is empty, we want to set all values to 1.0, not 0.0.
            for (Object objVertex : ipGraph.vertexSet()) {
                NodePair nodePair = (NodePair) objVertex;
                nodePair.sim0 = 1.0;
                nodePair.sim = nodePair.sim0;
                fixpointVals.put(nodePair, nodePair);
            }
        }
    }

    /**
     * Create the induced propagation graph.
     */
    private DirectedWeightedMultigraph calcInducedPropagationGraph() {

        DirectedWeightedMultigraph graph = new DirectedWeightedMultigraph(DefaultWeightedEdge.class);

        // For every edge, create an edge going in the opposite direction.
        Set<Edge> reversedEdges = Sets.newHashSet();
        for (Edge edge : pcGraph.getPCGraphEdges()) {
            NodePair sourcePair = (NodePair) edge.source;
            NodePair targetPair = (NodePair) edge.target;
            graph.addVertex(sourcePair);
            graph.addVertex(targetPair);
            graph.addEdge(sourcePair, targetPair);
            graph.addEdge(targetPair, sourcePair);
        }

        // Set weights.
        for (Object objVertex : graph.vertexSet()) {
            Set<Object> allEdgesOfSourceNode = graph.outgoingEdgesOf(objVertex);
            int nbrEdges = allEdgesOfSourceNode.size();
            for (Object objEdge : allEdgesOfSourceNode)  {
                graph.setEdgeWeight(objEdge, 1/(double) nbrEdges);
            }
        }

        return graph;
    }

    /**
     * Has the process converged?
     * FIXME: implement hasConverged( )
     * @return
     *      currently always returns false
     */
    protected boolean hasConverged() {
        return false;
    }

}
