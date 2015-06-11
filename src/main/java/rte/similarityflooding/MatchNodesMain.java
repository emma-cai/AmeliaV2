package rte.similarityflooding;

import rte.datastructure.DNode;
import rte.datastructure.DTree;
import rte.datastructure.Graph;
import rte.graphmatching.GraphComparer;
import rte.graphmatching.RteConfiguration;
import rte.graphmatching.VertexSub;

import java.util.Set;

/**
 * Created by qingqingcai on 5/19/15.
 */
public class MatchNodesMain {
    private static Graph stringToDGraph(String in)    {
        DTree dtree = Graph.buildTree(in);
        return Graph.buildDGraph(dtree);
    }

    public static void main(String[] args) {

        String text1 = "Data mining tools perform data analysis and may uncover important data patterns, contributing greatly to business strategies, knowledge bases, and scientific and medical research.";
        String text2 = "What do data mining tools perform?";
        Graph dgraph1 = stringToDGraph(text1);
        Graph dgraph2 = stringToDGraph(text2);

        System.out.println("\ndgraph1 = " + text1 + "\n");
        System.out.println(dgraph1);


        System.out.println("\ndgraph2 = " + text2 + "\n");
        System.out.println(dgraph2);

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.IDENTICAL_LABELS_ONLY)
                .nbrSimFloodingIterations(10).build();
        GraphComparer matcher = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        matcher.init(dgraph1, dgraph2, config);
        Set<NodePair> nodePairSet = matcher.getPCGraphNodes();

        for (NodePair pair : nodePairSet) {
            DNode dnode_T = (DNode) pair.node1;
            DNode dnode_H = (DNode) pair.node2;
            VertexSub sv = new VertexSub();
            double nodeSimilarity = sv.getNodeSimilarity(dnode_T, dnode_H);
            System.out.println("[" + dnode_T.getForm() + ", " + dnode_H.getForm() + "] = " + nodeSimilarity);
        }

        System.out.println("graph1_size = " + dgraph1.vertexSet().size());
        System.out.println("graph2_size = " + dgraph2.vertexSet().size());
        System.out.println("cross_product = " + nodePairSet.size());
    }

}
