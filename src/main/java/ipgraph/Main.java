package ipgraph;

import ipgraph.datastructure.DGraph;
import ipgraph.datastructure.DNode;
import ipgraph.datastructure.DTree;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Subgraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class Main {

    private static Set<String> postagSet = new HashSet<>(Arrays.asList(new String[]{"NN", "NNS", "NNP", "NNPS", "WP"}));

    public static void main(String[] args) {

        DGraph DG = new DGraph();


        printSplitLine();
        String sentence = "Some proteins, such as those to be incorporated in membranes (known as membrane proteins), are transported into the RER during synthesis.";
        // build tree
        DTree dtree = DTree.buildTree(sentence);

        // build graph
        UndirectedGraph<Integer, DefaultEdge> dgraph = DG.buildDGraph(dtree);
        System.out.println("\ngraph = " + dgraph);

        // find the path between two nodes
        int start = 0; int end = 2;
        List<DefaultEdge> paths = DG.findShortestPath(dgraph, start, end);
        printPath(paths, dtree, dgraph, start, end);

        start = 0; end = 23;
        paths = DG.findShortestPath(dgraph, start, end);
        printPath(paths, dtree, dgraph, start, end);

        Set<Integer> VertexSet = new HashSet<>(Arrays.asList(new Integer[]{2, 15, 16, 23, 25}));
        Subgraph subgraph = DG.getSubgraph(dgraph, VertexSet);
        System.out.println("\nsubgraph = " + subgraph);


        Subgraph minidgraph = DG.getSubgraph(dgraph, dtree, postagSet);
        System.out.println("\nsubgraph_for_specific_postags = \n" + DG.toString(minidgraph, dtree));
        printSplitLine();






        printSplitLine();
        sentence = "What are transported into the RER during synthesis?";
        dtree = DTree.buildTree(sentence);
        dgraph = DG.buildDGraph(dtree);
        System.out.println("\ngraph = " + dgraph);

        // find the path between two nodes
        start = 0; end = 1;
        paths = DG.findShortestPath(dgraph, start, end);
        printPath(paths, dtree, dgraph, start, end);

        start = 0; end = 6;
        paths = DG.findShortestPath(dgraph, start, end);
        printPath(paths, dtree, dgraph, start, end);

        VertexSet = new HashSet<>(Arrays.asList(new Integer[]{1, 6, 8}));
        subgraph = DG.getSubgraph(dgraph, VertexSet);
        System.out.println("\nsubgraph = " + subgraph);

        minidgraph = DG.getSubgraph(dgraph, dtree, postagSet);
        System.out.println("\nsubgraph_for_specific_postags = \n" + DG.toString(minidgraph, dtree));
        printSplitLine();





        printSplitLine();
        sentence = "Which branch of biology studies cells?";
        dtree = DTree.buildTree(sentence);
        dgraph = DG.buildDGraph(dtree);
        VertexSet = new HashSet<>(Arrays.asList(new Integer[]{4, 6}));
        subgraph = DG.getSubgraph(dgraph, VertexSet);
        System.out.println("\nsubgraph = " + subgraph);

        minidgraph = DG.getSubgraph(dgraph, dtree, postagSet);
        System.out.println("\nsubgraph_for_specific_postags = \n" + DG.toString(minidgraph, dtree));
        printSplitLine();
    }

    public static void run(String sentence) {

        System.out.println("===============================================================");




//        // subgraph
//        ConnectivityInspector ci=new ConnectivityInspector(dgraph);
//        List<Set> ccs = ci.connectedSets();
//        System.out.println("subgraph = \n");
//        for (Set s : ccs)
//            System.out.println(s);
        System.out.println("===============================================================");
    }


    public static void printPath(List<DefaultEdge> paths,
                 DTree dtree, UndirectedGraph<Integer, DefaultEdge> dgraph, int start, int end) {
        System.out.println("\nshortest path from " + start + " to " + end + " = ");
        for (int i = 0; i < paths.size(); i++) {
            DefaultEdge p = paths.get(i);
            DNode s = dtree.getNodeById(dgraph.getEdgeSource(p));
            DNode t = dtree.getNodeById(dgraph.getEdgeTarget(p));
            System.out.println(s.getId() + ":" + s.getForm() + " --> " + t.getDepLabel() + " --> " + t.getId() + ":" + t.getForm());
        }
    }

    private static void printSplitLine() {
        System.out.println("===============================================================");
    }
}
