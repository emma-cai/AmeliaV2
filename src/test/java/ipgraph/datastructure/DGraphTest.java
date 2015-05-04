package ipgraph.datastructure;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.BeforeClass;

/**
 * Created by qingqingcai on 5/4/15.
 */
public class DGraphTest {


    @BeforeClass
    public static void init() {
        DGraph DG = new DGraph();
        String sentence = "";
        DTree dtree = DTree.buildTree(sentence);
        UndirectedGraph<Integer, DefaultEdge> dgraph = DG.buildDGraph(dtree);
    }
}
