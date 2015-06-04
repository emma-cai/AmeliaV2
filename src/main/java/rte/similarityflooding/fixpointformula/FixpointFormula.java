package rte.similarityflooding.fixpointformula;

import rte.similarityflooding.NodePair;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;

/**
 * For similarity flooding, allows different ways of calculating the next iterations's node-pair values.
 */
public interface FixpointFormula {

    public void doFixpointFormula(Map<NodePair, NodePair> fixpointVals, DirectedWeightedMultigraph ipGraph);
}
