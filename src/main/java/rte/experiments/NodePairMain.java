package rte.experiments;

import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DNode;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DTree;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.Graph;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.GraphComparer;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.RteConfiguration;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.similarityflooding.NodePair;

import java.text.DecimalFormat;
import java.util.*;

import static net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.DMatching.computeGraphComparerInitValues;

/**
 * Created by qingqingcai on 5/26/15.
 */
public class NodePairMain {

    public static void main(String[] args) {

        String T = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        DTree dtree_T = DTree.buildTree(T);
        Graph graph_T = Graph.buildDGraph(dtree_T);
        System.out.println("\ngraph_T = \n" + graph_T.toString());

        String Q = "What has pairs of wings?";
        DTree dtree_Q = DTree.buildTree(Q);
        Graph graph_Q = Graph.buildDGraph(dtree_Q);
        System.out.println("\ngraph_Q = \n" + graph_Q.toString());

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .build();
        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(graph_T, graph_Q, config);

        Map<NodePair, Double> initVals = computeGraphComparerInitValues(comparer.getPCGraphNodes());

        Set<NodePair> actualSet = comparer.compareGraphNodes(initVals);

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_Sim_PairList = groupOnDNodeAndSimilarity(actualSet);

        for (DNode node_H : nodeH_Sim_PairList.keySet()) {
            System.out.println("\nnodeH = " + node_H);
            NavigableMap<Double, List<NodePair>> sim_NodePairWithSameSim = nodeH_Sim_PairList.get(node_H);
            for (Double sim : sim_NodePairWithSameSim.keySet()) {
                for (NodePair pair : sim_NodePairWithSameSim.get(sim)) {
                    System.out.println("\t" + sim + "\t" + pair);
                }
            }
        }
    }

    public static HashMap<DNode, NavigableMap<Double, List<NodePair>>> groupOnDNodeAndSimilarity(Set<NodePair> actualSet) {

        // Key is the node in Question, value is the list of nodes paired with key
        HashMap<DNode, List<NodePair>> nodeH_paired = new HashMap<>();
        for (NodePair key : actualSet) {
            DNode dnode_H = ((DNode) key.node2);
            List<NodePair> paired = new ArrayList<>();
            if (nodeH_paired.containsKey(dnode_H)) {
                paired = nodeH_paired.get(dnode_H);
                paired.add(key);
            } else {
                paired.add(key);
            }
            nodeH_paired.put(dnode_H, paired);
        }

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> nodeH_Sim_PairList = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.####");
        for (DNode node_H : nodeH_paired.keySet()) {
            TreeMap<Double, List<NodePair>> sim_pairedWithSameSim = new TreeMap<>();
            for (NodePair pair : nodeH_paired.get(node_H)) {
                Double sim = Double.valueOf(df.format(pair.sim));
                List<NodePair> pairWithSameSim = new ArrayList<>();
                if (sim_pairedWithSameSim.containsKey(sim)) {
                    pairWithSameSim = sim_pairedWithSameSim.get(sim);
                    pairWithSameSim.add(pair);
                } else {
                    pairWithSameSim.add(pair);
                }
                //    List<NodePair> sorted = NodePair.sortOnSimilarity(pairWithSameSim);
                sim_pairedWithSameSim.put(sim, pairWithSameSim);
            }
            NavigableMap sorted = (NavigableMap) sim_pairedWithSameSim.descendingMap();
            nodeH_Sim_PairList.put(node_H, sorted);
        }

        return nodeH_Sim_PairList;
    }

}
