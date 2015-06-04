package rte.graphmatching;

import rte.datastructure.Graph;
import rte.datastructure.LangLib;
import rte.similarityflooding.NodePair;
import rte.similarityflooding.pcgraph.PCGraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compare two graphs. Comparison may take place at the low node level or a high level.
 * An implementation of GraphComparer is expected to have some "knowledge" of two existing graphs.
 * Known implementations: SimpleGraphComparer, MatchNodes
 */
public interface GraphComparer {

    public static final Set<String> postagSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN,
                    LangLib.POS_NNS,
                    LangLib.POS_NNP,
                    LangLib.POS_NNPS,
                    LangLib.POS_WP}));


    /**
     * Initialize with the given graphs.
     * @param g1
     * @param g2
     * @param configuration
     */
    void init(Graph g1, Graph g2, RteConfiguration configuration);

    /**
     * Return the nodes that are found in a pairwise connectivity graph of the two graphs of this GraphComparer.
     * @return
     */
    Set<NodePair> getPCGraphNodes();

    /**
     * For two graphs g1 and g2, score how similar each node of g1 is to each node of g2.
     * @param initVals
     *      A set of values for initialization.
     * @return
     */
    public Set<NodePair> compareGraphNodes(Map<NodePair, Double> initVals);

    // FIXME: not sure if we want to keep this just for the tests; current implementations do not return defensive copies
    public PCGraph getPCGraph();

}
