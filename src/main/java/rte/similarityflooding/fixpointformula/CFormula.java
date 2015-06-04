package rte.similarityflooding.fixpointformula;

import rte.similarityflooding.NodePair;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;
import java.util.Set;

/**
 * Formula "C".
 */
public class CFormula implements FixpointFormula {

    @Override
    public void doFixpointFormula(Map<NodePair, NodePair> fixpointVals, DirectedWeightedMultigraph ipGraph) {
        double maxMappingVal = 0.0;

        // for every node in the graph
        for (Object objSource : ipGraph.vertexSet()) {
            NodePair source = (NodePair) objSource;
            double currSourceVal = fixpointVals.get(source).sim;
            double currSourceOriginalVal = fixpointVals.get(source).sim0;

            Set<Object> allEdgesOfSourceNode = ipGraph.outgoingEdgesOf(objSource);

            // Update the node based on its original value, its current value and all of its neighbors' original and current values and the edge between them
            double newVal = currSourceOriginalVal + currSourceVal;

            for (Object objEdge : allEdgesOfSourceNode)   {
                Object objNeighbor = ipGraph.getEdgeTarget(objEdge);

                // Get the weight of the edge leading from neighbor back to target.
                double coeff = ipGraph.getEdgeWeight(ipGraph.getEdge(objNeighbor, objSource));

                // Get the neighbor's value.
                NodePair neighbor = (NodePair) ipGraph.getEdgeTarget(objEdge);
                double currNeighborVal = fixpointVals.get(neighbor).sim;
                double currNeighborOriginalVal = fixpointVals.get(neighbor).sim0;

                newVal += coeff * (currNeighborOriginalVal + currNeighborVal);

                maxMappingVal = Double.max(maxMappingVal, newVal);
            }
            source.simN1 = newVal;
        }

        // Normalize
        for (NodePair mapPair : fixpointVals.keySet())   {
            double newVal = fixpointVals.get(mapPair).simN1 / maxMappingVal;
            mapPair.simN1 = newVal;
            mapPair.sim = newVal;
        }
    }
}
