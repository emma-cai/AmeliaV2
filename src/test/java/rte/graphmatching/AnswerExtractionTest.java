package rte.graphmatching;

import rte.IRTestBase;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.similarityflooding.NodePair;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;

import static rte.graphmatching.DMatching.initNodeMatches;
import static org.junit.Assert.assertEquals;

/**
 * Created by qingqingcai on 6/3/15.
 */
public class AnswerExtractionTest extends IRTestBase {

    @Test
    public void testRunAnswerExtration1() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = true;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("The adult House Cricket is about two centimeters in length.");

        Graph graph_Q = stringToDGraph("What is two centimeters in length?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("the adult house cricket", actual);

        graph_Q = stringToDGraph("What is House cricket?");
        actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("about two centimeters in length", actual);
    }

    @Test
    public void testRunAnswerExtration2() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("The body is usually light brown with black markings about the head and thorax.");

        Graph graph_Q = stringToDGraph("What is usually light brown with black markings about the head and thorax?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
         assertEquals("the body", actual);
    }

    @Test
    public void testRunAnswerExtration3() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.");
        Graph graph_Q = stringToDGraph("What has pairs of wings?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("house crickets", actual);

        graph_Q = stringToDGraph("What has conspicuous jumping legs?");
        actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("house crickets", actual);

        graph_Q = stringToDGraph("What is used for flying?");
        actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("the back pair", actual);
    }

    @Test
    public void testRunAnswerExtration4() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("House Crickets can be found in a variety of habitats including woodlands, suburbs, urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.");
        Graph graph_Q = stringToDGraph("Where are house crickets found?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("in a variety of habitats including woodlands , suburbs , urban areas , buildings , ducts , siding , restaurants and anywhere else", actual);
    }

    @Test
    public void testRunAnswerExtration5() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("Celsius was born in Uppsala in Sweden.");
        Graph graph_Q = stringToDGraph("Where was Celsius born");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("in sweden", actual);
    }

    @Test
    public void testRunAnswerExtration6() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("He dropped John from his name upon graduating from college.");
        Graph graph_Q = stringToDGraph("When did he drop John from his name?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("upon graduating from college", actual);
    }

    @Test
    public void testRunAnswerExtration7() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("In 1905 Coolidge met and married Grace Anna Goodhue, a local schoolteacher and fellow Vermonter.");
        Graph graph_Q = stringToDGraph("When did Coolidge meet and marry Grace Anna Goodhue?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("in 1905", actual);
    }

    @Test
    public void testRunAnswerExtration8() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("John Calvin Coolidge Jr. was born in Plymouth, Windsor County, Vermont, on July 4 1872, the only U.S. President to be born on the fourth of July.");
        Graph graph_Q = stringToDGraph("When was Coolidge born?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("in plymouth , windsor county , vermont , on july 4", actual);
    }

    @Test
    public void testRunAnswerExtration9() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("They had two sons; John Coolidge, born in 1906, and Calvin Jr., born in 1908.");
        Graph graph_Q = stringToDGraph("Is Calvin Jr. older than John Coolidge?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("yes", actual);
    }

    @Ignore
    @Test
    public void testRunAnswerExtration10() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("With his savings and a small inheritance from his grandfather, Coolidge was able to open his own law office in Northampton in 1898, where he practiced transactional law, believing that he served his clients best by staying out of court.");
        Graph graph_Q = stringToDGraph("What year did Coolidge open his own law office?");
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("in 1898", actual);
    }


    @Ignore
    @Test
    public void testRunAnswerExtrationFail() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("The female is told from the male by the presence of an ovipositor, a long extension attached to the end of the abdomen, used for depositing eggs in the soil.");

        // Cannot be answered
        // What is ovipositor?
        // What is attached to the end of the abdomen?
        // What is used for depositing eggs in the soil?
        Graph graph_Q = stringToDGraph("What is ovipositor?");

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);
        String actual = testAnswerExtraction(graph_T, graph_Q, config);
        assertEquals("the body", actual);
    }


    private String testAnswerExtraction(Graph graph_T, Graph graph_Q, RteConfiguration config) {

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);

        DMatching.computeMatchingCost(graph_T, graph_Q, node_SortedMapList_T_Q);

        String actual = AnswerExtraction.extractShortAnswers2(graph_T, graph_Q, node_SortedMapList_T_Q).trim().toLowerCase();

        System.out.println("answer = " + actual);

        return actual;
    }
}
