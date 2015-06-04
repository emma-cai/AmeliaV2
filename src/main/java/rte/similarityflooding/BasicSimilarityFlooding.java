package rte.similarityflooding;

import com.google.common.collect.Maps;
import rte.graphmatching.RteConfiguration;
import rte.similarityflooding.fixpointformula.FixpointFormula;

import java.util.Map;
import java.util.Set;

/**
 * Implements graph matching based on Melnik, Molina, Rahm: "Similarity Flooding: A Versatile Graph Matching Algorithm",
 * aka "ICDE'02".
 */
public class BasicSimilarityFlooding extends SimilarityFloodingInit {
    public BasicSimilarityFlooding() {

    }

    /**
     * For two graphs g1 and g2, score how similar each node of g1 is to each node of g2.
     *
     * @param initVals A set of values for initialization.
     * @return
     */
    @Override
    public Set<NodePair> compareGraphNodes(Map<NodePair, Double> initVals) {

        Map<NodePair, NodePair> fixpointVals = Maps.newHashMap();

        initializeCompareGraphNodes(fixpointVals, initVals);

        FixpointFormula formula = RteConfiguration.FixpointFormulaType.getFixpointFormula(rteConfiguration.fixpointFormula);

        int itCnt = 1;
        while (itCnt <=  MAX_ITERATION_NUM)  {
            itCnt++;

            formula.doFixpointFormula(fixpointVals, ipGraph);

            // Early exit if converged.
            if (hasConverged() && itCnt >= MIN_ITERATION_NUM)    {
                break;
            }
        }

        return fixpointVals.keySet();
    }



}

