package rte.graphmatching;

import rte.similarityflooding.BasicSimilarityFlooding;
import rte.similarityflooding.SimilarityFloodingInit;
import rte.similarityflooding.pcgraph.CompleteCrossProduct;
import rte.similarityflooding.pcgraph.IdenticalLabelsOnly;
import rte.similarityflooding.pcgraph.PCGraph;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by jkurlandski on 6/1/15.
 */
public class RteConfigurationTest {

    @Test
    public void testSimple1() {
        RteConfiguration config = new RteConfiguration(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT);

        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);
        assertTrue(comparer instanceof SimilarityFloodingInit);     // true because of subclassing
        assertTrue(comparer instanceof BasicSimilarityFlooding);

        PCGraph pcGraph = RteConfiguration.PCGraphType.getPCGraph(config.pcGraph);
        assertTrue(pcGraph instanceof CompleteCrossProduct);
    }

    @Test
    public void testSimple2() {
        RteConfiguration config = new RteConfiguration(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.IDENTICAL_LABELS_ONLY);

        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);
        assertTrue(comparer instanceof SimilarityFloodingInit);

        PCGraph pcGraph = RteConfiguration.PCGraphType.getPCGraph(config.pcGraph);
        assertTrue(pcGraph instanceof IdenticalLabelsOnly);
    }
}
