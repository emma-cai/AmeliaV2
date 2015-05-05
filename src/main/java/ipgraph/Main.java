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
        String sentence = "What are transported into the RER during synthesis?";

        // build tree
        DTree dtree = DTree.buildTree(sentence);

        // build graph
        DGraph dgraph = DGraph.buildDGraph(dtree);
        System.out.println("\ngraph = \n" + dgraph.toString(dtree));

        // find the path between two nodes
        int sid = 1; int tid = 6;
        List<DefaultEdge> paths = dgraph.findShortestPath(sid, tid);
        System.out.println("\npath between " + sid + " and " + tid + " = " + paths);

        // build subgraph
        Subgraph subgraph_NOUN = dgraph.getSubgraph(dtree, postagSet);
        System.out.println("\nsubgraph_for_NOUN = \n" + DGraph.toString(subgraph_NOUN, dtree));
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
