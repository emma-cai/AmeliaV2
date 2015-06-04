package rte.experiments;

import rte.datastructure.DTree;
import rte.datastructure.Graph;
import rte.graphmatching.*;

import java.util.HashMap;
import java.util.Set;

import static rte.graphmatching.DMatching.initNodeMatches;

/**
* Created by qingqingcai on 5/18/15.
*/
public class DMatchingMain {

    public static final Set<String> postagSet = NodeComparer.postagSet;

    public static void main(String[] args) {

        test1();
    }

    public static void test1() {

    //    String T1 = "The collection of instructions is implemented as bit patterns, each one of which has a different meaning when loaded into the instruction register.";
    //    String T1 = "Technology shocks are events in a macroeconomic model, that change the production function.";
        String T1 = "House Crickets can be found in a variety of habitats including woodlands, suburbs, urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.";
        DTree dtree_T1 = DTree.buildTree(T1.toLowerCase());
        Graph graph_T1 = Graph.buildDGraph(dtree_T1)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_T1 = " + T1 +"\n" + graph_T1.toString());

    //    String T2 = "Even the incredibly simple microprocessor will have a fairly large set of instructions that it can perform.";
    //    String T2 = "The oil shocks that occurred in the late 1970s are examples of negative technology shocks.";
        String T2 = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        DTree dtree_T2 = DTree.buildTree(T2.toLowerCase());
        Graph graph_T2 = Graph.buildDGraph(dtree_T2)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_T2 = \n" + T2 + "\n" + graph_T2.toString());

     //   String Q1 = "How are the collection of instructions implemented?";    // will get correct answer if considering cross product mapping
     //   String Q1 = "What are technology shocks?";
        String Q1 = "Where are house crickets found?";
        DTree dtree_Q1 = DTree.buildTree(Q1.toLowerCase());
        Graph graph_Q1 = Graph.buildDGraph(dtree_Q1)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_Q1 = \n" + Q1 + "\n" + graph_Q1.toString());

        System.out.flush();

        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.IDENTICAL_LABELS_ONLY)
                .build();
        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(graph_T1, graph_Q1, config);
        HashMap nodeMatches = initNodeMatches(graph_T1, graph_Q1, null);
        double matchingCost = DMatching.computeMatchingCost(graph_T1, graph_Q1, nodeMatches);
        System.out.println("T1 to Q1: " + matchingCost);
        System.out.println("Best matched node using method2");
        String answer = AnswerExtraction.runAnswerExtraction(graph_T1, graph_Q1, nodeMatches);
        System.out.println(answer);

//        config = new RteConfiguration
//                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.IDENTICAL_LABELS_ONLY)
//                .build();
        comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(graph_T2, graph_Q1, config);
        nodeMatches = initNodeMatches(graph_T2, graph_Q1, null);
        matchingCost = DMatching.computeMatchingCost(graph_T2, graph_Q1, nodeMatches);
        System.out.println("T2 to Q1: " + matchingCost);
        System.out.println("Best matched node using method2");
        answer = AnswerExtraction.runAnswerExtraction(graph_T2, graph_Q1, nodeMatches);
        System.out.println(answer);
    }

    public static void test2() {
        //    String T1 = "The collection of instructions is implemented as bit patterns, each one of which has a different meaning when loaded into the instruction register.";
        //    String T1 = "Technology shocks are events in a macroeconomic model, that change the production function.";
        String T1 = "House Crickets can be found in a variety of habitats including woodlands, suburbs, urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.";
        DTree dtree_T1 = DTree.buildTree(T1.toLowerCase());
        Graph graph_T1 = Graph.buildDGraph(dtree_T1)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_T1 = " + T1 +"\n" + graph_T1.toString());

        //    String T2 = "Even the incredibly simple microprocessor will have a fairly large set of instructions that it can perform.";
        //    String T2 = "The oil shocks that occurred in the late 1970s are examples of negative technology shocks.";
        String T2 = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        DTree dtree_T2 = DTree.buildTree(T2.toLowerCase());
        Graph graph_T2 = Graph.buildDGraph(dtree_T2)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_T2 = \n" + T2 + "\n" + graph_T2.toString());

        //   String Q1 = "How are the collection of instructions implemented?";    // will get correct answer if considering cross product mapping
        //   String Q1 = "What are technology shocks?";
        String Q1 = "Where are house crickets found?";
        DTree dtree_Q1 = DTree.buildTree(Q1.toLowerCase());
        Graph graph_Q1 = Graph.buildDGraph(dtree_Q1)/**.getSubgraph(postagSet)**/;
        System.out.println("\nsubgraph_Q1 = \n" + Q1 + "\n" + graph_Q1.toString());
    }
}
