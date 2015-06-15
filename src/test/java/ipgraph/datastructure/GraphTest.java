package ipgraph.datastructure;

import org.junit.Test;
import rte.datastructure.Graph;

import static org.junit.Assert.assertEquals;

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
        assertEquals(4, generation);

        generation = graph.isAncestorOf(graph.getNodeById(2), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(3, generation);

        generation = graph.isAncestorOf(graph.getNodeById(4), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(2, generation);

        generation = graph.isAncestorOf(graph.getNodeById(5), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(1, generation);

        generation = graph.isAncestorOf(graph.getNodeById(8), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(0, generation);

        generation = graph.isAncestorOf(graph.getNodeById(3), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(-1, generation);

        generation = graph.isAncestorOf(graph.getNodeById(1), graph.getNodeById(7));
        System.out.println("generation = " + generation);
        assertEquals(-1, generation);

        generation = graph.isAncestorOf(graph.getNodeById(7), graph.getNodeById(8));
        System.out.println("generation = " + generation);
        assertEquals(-1, generation);

        generation = graph.isAncestorOf(graph.getNodeById(4), graph.getNodeById(3));
        System.out.println("generation = " + generation);
        assertEquals(1, generation);
    }

    @Test
    public void testStringToGraph() {

        String text = "Who has a vehicle?";
        Graph graph = Graph.stringToGraph(text);
        System.out.println("graph = " + graph);
    }
}
