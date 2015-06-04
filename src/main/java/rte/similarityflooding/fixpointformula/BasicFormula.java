package rte.similarityflooding.fixpointformula;

import rte.similarityflooding.NodePair;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.Map;
import java.util.Set;

/**
 * The "Basic" formula.
 */
public class BasicFormula implements FixpointFormula {

    @Override
    public void doFixpointFormula(Map<NodePair, NodePair> fixpointVals, DirectedWeightedMultigraph ipGraph) {
        double maxMappingVal = 0.0;

        // for every node in the graph
        for (Object objSource : ipGraph.vertexSet()) {
            NodePair source = (NodePair) objSource;
            double currSourceVal = fixpointVals.get(source).sim;

            Set<Object> allEdgesOfSourceNode = ipGraph.outgoingEdgesOf(objSource);

            // Update the node based on its current value and all of its neighbors' values and the edge between them
            double newVal = currSourceVal;

            for (Object objEdge : allEdgesOfSourceNode)   {
                Object objNeighbor = ipGraph.getEdgeTarget(objEdge);

                // Get the weight of the edge leading from neighbor back to target.
                double coeff = ipGraph.getEdgeWeight(ipGraph.getEdge(objNeighbor, objSource));

                // Get the neighbor's value.
                NodePair neighbor = (NodePair) ipGraph.getEdgeTarget(objEdge);
                double currNeighborVal = fixpointVals.get(neighbor).sim;

                newVal += coeff * currNeighborVal;

                maxMappingVal = Double.max(maxMappingVal, newVal);
            }
            source.simN1 = newVal;
            // The put is unnecessary
            //fixpointVals.put(source, source);
        }

        // Normalize
        for (NodePair mapPair : fixpointVals.keySet())   {
            double newVal = fixpointVals.get(mapPair).simN1 / maxMappingVal;
            mapPair.simN1 = newVal;
            mapPair.sim = newVal;
            // The put is unnecessary
            //fixpointVals.put(mapPair, mapPair);
        }
    }
}
