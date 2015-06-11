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

        String text = "The adult House Cricket is about two centimeters in length.";
        String ques = "What is two centimeters in length?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);
        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
        assertEquals("the adult house cricket", actual);

        ques = "What is House cricket?";
        graph_Q = stringToDGraph(ques);
        actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "The body is usually light brown with black markings about the head and thorax.";
        String ques = "What is usually light brown with black markings about the head and thorax?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        String ques = "What has pairs of wings?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
        assertEquals("house crickets", actual);

        ques = "What has conspicuous jumping legs?";
        graph_Q = stringToDGraph(ques);
        actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
        assertEquals("house crickets", actual);

        ques = "What is used for flying?";
        graph_Q = stringToDGraph(ques);
        actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "House Crickets can be found in a variety of habitats including woodlands, suburbs, urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.";
        String ques = "Where are house crickets found?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "Celsius was born in Uppsala in Sweden.";
        String ques = "Where was Celsius born";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);
        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "He dropped John from his name upon graduating from college.";
        String ques = "When did he drop John from his name?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "In 1905 Coolidge met and married Grace Anna Goodhue, a local schoolteacher and fellow Vermonter.";
        String ques = "When did Coolidge meet and marry Grace Anna Goodhue?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "John Calvin Coolidge Jr. was born in Plymouth, Windsor County, Vermont, on July 4 1872, the only U.S. President to be born on the fourth of July.";
        String ques = "When was Coolidge born?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "They had two sons; John Coolidge, born in 1906, and Calvin Jr., born in 1908.";
        String ques = "Is Calvin Jr. older than John Coolidge?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        String text = "With his savings and a small inheritance from his grandfather, Coolidge was able to open his own law office in Northampton in 1898, where he practiced transactional law, believing that he served his clients best by staying out of court.";
        String ques = "What year did Coolidge open his own law office?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);

        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
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

        // Cannot be answered
        // What is ovipositor?
        // What is attached to the end of the abdomen?
        // What is used for depositing eggs in the soil?

        String text = "The female is told from the male by the presence of an ovipositor, a long extension attached to the end of the abdomen, used for depositing eggs in the soil.";
        String ques = "What is ovipositor?";
        Graph graph_T = stringToDGraph(text);
        Graph graph_Q = stringToDGraph(ques);


        HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);
        String actual = testAnswerExtraction(graph_T, graph_Q, ques, config);
        assertEquals("the body", actual);
    }


    private String testAnswerExtraction(Graph graph_T, Graph graph_Q, String ques, RteConfiguration config) {

        HashMap<DNode, NavigableMap<Double, List<NodePair>>> node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);

        DMatching.computeMatchingCost(graph_T, graph_Q, node_SortedMapList_T_Q);

        String actual = AnswerExtraction.extractShortAnswers(graph_T, graph_Q, ques, node_SortedMapList_T_Q).trim().toLowerCase();

        System.out.println("answer = " + actual);

        return actual;
    }
}
