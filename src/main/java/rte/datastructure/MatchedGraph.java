package rte.datastructure;

import rte.similarityflooding.NodePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 5/27/15.
 */
public class MatchedGraph {

    private static final double vertexMatchWeightPct = 1.0;

    private List<NodePair> matchedNodes;

    private double vertexCost;

    private double pathCost;

    private double matchingCost;

    public MatchedGraph() {
        matchedNodes = new ArrayList<>();
        vertexCost = Double.MAX_VALUE;
        pathCost = Double.MAX_VALUE;
        matchingCost = Double.MAX_VALUE;
    }

    public MatchedGraph(List<NodePair> nodepairs) {

        matchedNodes = new ArrayList<>();
        matchedNodes.addAll(nodepairs);
        vertexCost = Double.MAX_VALUE;
        pathCost = Double.MAX_VALUE;
        matchingCost = Double.MAX_VALUE;
    }

    // Get values from MatchedGraph
    public double getVertexCost() { return vertexCost; }

    public double getPathCost() { return pathCost; }

    public double getMatchingCost() { return matchingCost; }

    public List<NodePair> getMatchedNodes() { return matchedNodes; }

    // Set values to MatchedGraph
    public void setVertexCost(double vertexCost) {
        this.vertexCost =  vertexCost;
    }

    public void setPathCost(double pathCost) {
        this.pathCost = pathCost;
    }

    public void setMatchingCost(double matchingCost) {
        this.matchingCost = matchingCost;
    }

    public void addNodePair(NodePair nodepair) {
        matchedNodes.add(nodepair);
    }

    // Compute matchingCost based on vertexCost and pathCost
    public void computeMatchingCost() {
        matchingCost = vertexMatchWeightPct * vertexCost + (1 - vertexMatchWeightPct) * pathCost;
    }

    public void computeMatchingCost(double vertexCostWeight) {
        matchingCost = vertexCostWeight * vertexCost + (1 - vertexCostWeight) * pathCost;
    }

    /**
     * @param node
     * @param inverse True if node is the second one in the pair; False otherwise;
     * @return
     */
    public List<NodePair> getNodePair(DNode node, boolean inverse) {

        List<NodePair> nodepairs = new ArrayList<>();
        for (NodePair nodepair : matchedNodes) {
            if (inverse) {          // searching by the second_node in the pair
                if (((DNode) nodepair.node2).getId() == node.getId()) {
                    nodepairs.add(nodepair);
                    return nodepairs;
                }
            } else {                // searching by the first_node in the pair
                if (((DNode) nodepair.node1).getId() == node.getId()) {
                    nodepairs.add(nodepair);
                }
            }
        }
        return nodepairs;
    }

    // static methods
    public static List<MatchedGraph> getBestMatchedGraphList(List<MatchedGraph> MatchedGraphList) {
        double minMatchingCost = Double.MAX_VALUE;
        List<MatchedGraph> bestMatchedGraphList = new ArrayList<>();
        for (MatchedGraph mg : MatchedGraphList) {
            double matchingCost = mg.getMatchingCost();

            if (Double.compare(matchingCost, minMatchingCost) < 0.0) {
                minMatchingCost = matchingCost;
                bestMatchedGraphList = new ArrayList<>();
                bestMatchedGraphList.add(mg);
            } else if (Double.compare(matchingCost, minMatchingCost) == 0.0) {
                bestMatchedGraphList.add(mg);
            }
        }

        return bestMatchedGraphList;
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        matchedNodes.forEach(pair -> {
            sb.append(pair.node1 + " ||| " + pair.node2 + " ||| " + pair.sim + "\n");
        });
        sb.append("vertexCost = " + vertexCost + "\n");
        sb.append("pathCost = " + pathCost + "\n");
        sb.append("matchingCost = " + matchingCost + "\n");

        return sb.toString();
    }
}
