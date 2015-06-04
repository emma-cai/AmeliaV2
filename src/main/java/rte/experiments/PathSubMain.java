package rte.experiments;

import net.ipsoft.eliza.ir.rte.sf.RteMessageHandler;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DNode;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DTree;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.Graph;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.MatchedGraph;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.DMatching;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.PathSub;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.similarityflooding.NodePair;

import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;

/**
* Created by qingqingcai on 5/26/15.
*/
public class PathSubMain {

    public static void main(String[] args) {

        PathSub ps = new PathSub();
        ps.PathSubDebug = true;

        String T = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        DTree dtree_T = DTree.buildTree(T);
        Graph graph_T = Graph.buildDGraph(dtree_T);
        System.out.println("\ngraph_T = \n" + graph_T.toString());

        String Q = "What has pairs of wings?";
        DTree dtree_Q = DTree.buildTree(Q);
        Graph graph_Q = Graph.buildDGraph(dtree_Q);
        System.out.println("\ngraph_Q = \n" + graph_Q.toString());

        // These calls are performed in initNodeMatches( ).
        //GraphComparer comparer = RteMessageHandler.defaultRteConfiguration.graphComparer;
        //comparer.init(graph_T, graph_H);
        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeMatches = DMatching.initNodeMatches(graph_T, graph_Q, RteMessageHandler.defaultRteConfiguration);

        // Select bestMatchedGraphList by computing VertexCost
        List<MatchedGraph> bestMatchedGraphListByVertexCost = DMatching.computeVertexCost(graph_T, graph_Q, nodeMatches, null);
        double minimumMCByVC = Double.MAX_VALUE;
        if (!bestMatchedGraphListByVertexCost.isEmpty())
            minimumMCByVC = bestMatchedGraphListByVertexCost.get(0).getMatchingCost();
        System.out.println("After computing vertex cost, the matchingCost = " + minimumMCByVC);

        // Further select bestMatchedGraphList by integrating PathCost into VertexCost
        List<MatchedGraph> bestMatchedGraphListByAddingPathCost = DMatching.computePathCost(graph_T, graph_Q, bestMatchedGraphListByVertexCost);
        double minimumMCByAddingPC = Double.MAX_VALUE;
        if (!bestMatchedGraphListByAddingPathCost.isEmpty())
            minimumMCByAddingPC = bestMatchedGraphListByAddingPathCost.get(0).getMatchingCost();
        System.out.println("After computing path cost, the final matchingCost = " + minimumMCByAddingPC);
    }
}
