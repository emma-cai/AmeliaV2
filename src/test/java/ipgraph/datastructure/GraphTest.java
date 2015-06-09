package ipgraph.datastructure;

import org.junit.Test;
import rte.datastructure.Graph;

/**
 * Created by qingqingcai on 6/8/15.
 */
public class GraphTest {

    @Test
    public void testIsAncestorOf() {

        String text = "John is the director of the film Titanic.";
        Graph graph = Graph.stringToGraph(text);
        System.out.println("Graph = \n" + graph);
        int generation = graph.isAncestorOf(graph.getNodeById(0), graph.getNodeById(8));
        System.out.println("generation = " + generation);
    }
}
