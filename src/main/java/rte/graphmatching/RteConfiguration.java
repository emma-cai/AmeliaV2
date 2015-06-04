package rte.graphmatching;

import rte.similarityflooding.fixpointformula.BasicFormula;
import rte.similarityflooding.fixpointformula.CFormula;
import rte.similarityflooding.fixpointformula.FixpointFormula;
import rte.similarityflooding.BasicSimilarityFlooding;
import rte.similarityflooding.SimilarityFloodingInit;
import rte.similarityflooding.pcgraph.*;

/**
 * Used to control the various parameters of RTE and Similarity Flooding.
 */
public class RteConfiguration {

    // FIXME: Try to find a way to implement these that don't require user updating the enums everytime there's a new implementation
    public enum GraphComparerType{
        SIMILARITY_FLOODING_INIT, BASIC_SIMILARITY_FLOODING;

        public static GraphComparer getGraphComparer(GraphComparerType type) {
            if (type.equals(GraphComparerType.SIMILARITY_FLOODING_INIT))    {
                return new SimilarityFloodingInit();
            }
            else if (type.equals(GraphComparerType.BASIC_SIMILARITY_FLOODING))    {
                return new BasicSimilarityFlooding();
            }
            else    {
                throw new IllegalStateException("Unknown GraphComparerType");
            }
        }
    }

    public enum PCGraphType {
        COMPLETE_CROSS_PRODUCT, IDENTICAL_LABELS_ONLY, LOOSELY_MATCHED_LABELS_ONLY, STRING_NODE_MATCH_ONLY;

        public static PCGraph getPCGraph(PCGraphType type) {
            if (type.equals(PCGraphType.COMPLETE_CROSS_PRODUCT)) {
                return new CompleteCrossProduct();
            } else if (type.equals(PCGraphType.IDENTICAL_LABELS_ONLY)) {
                return new IdenticalLabelsOnly();
            } else if (type.equals(PCGraphType.LOOSELY_MATCHED_LABELS_ONLY)) {
                return new LooselyMatchedLabelsOnly();
            } else if (type.equals(PCGraphType.STRING_NODE_MATCH_ONLY)) {
                return new StringNodeMatchOnly();
            } else {
                throw new IllegalStateException("Unknown PCGraphType");
            }
        }
    }

    public enum FixpointFormulaType {
        BASIC_FORMULA, C_FORMULA;

        public static FixpointFormula getFixpointFormula(FixpointFormulaType type) {
            if (type.equals(FixpointFormulaType.BASIC_FORMULA)) {
                return new BasicFormula();
            } else if (type.equals(FixpointFormulaType.C_FORMULA)) {
                return new CFormula();
            } else {
                throw new IllegalStateException("Unknown FixpointFormulaType");
            }
        }
    }

    /**
     * Builder class for RteConfiguration.
     * Sample usage:
     * RteConfiguration config = new RteConfiguration
     *      .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
     *      .nbrSimFloodingIterations(10)
     *      .build();
     */
    public static class RteConfigurationBuilder   {
        private final GraphComparerType graphComparer;
        private final PCGraphType pcGraph;

        private FixpointFormulaType fixpointFormula = FIXPOINT_FORMULA_DEFAULT;
        private int nbrSimFloodingIterations = NBR_SIM_FLOODING_ITERATIONS_DEFAULT;

        public RteConfigurationBuilder(GraphComparerType gc, PCGraphType pcg)   {
            graphComparer = gc;
            pcGraph = pcg;
        }

        public RteConfigurationBuilder fixpointFormula(FixpointFormulaType fft)    {
            fixpointFormula = fft;
            return this;
        }

        public RteConfigurationBuilder nbrSimFloodingIterations(int its)    {
            nbrSimFloodingIterations = its;
            return this;
        }

        public RteConfiguration build() {
            return new RteConfiguration(graphComparer, pcGraph, fixpointFormula, nbrSimFloodingIterations);
        }
    }

    public final GraphComparerType graphComparer;

    public final PCGraphType pcGraph;

    public final FixpointFormulaType fixpointFormula;
    public static final FixpointFormulaType FIXPOINT_FORMULA_DEFAULT = FixpointFormulaType.BASIC_FORMULA;

    public final int nbrSimFloodingIterations;
    public static final int NBR_SIM_FLOODING_ITERATIONS_DEFAULT = 10;

    /**
     * Create an RteConfiguration with the provided parameters using default values for the remaining fields.
     * @param gc
     * @param pcg
     */
    public RteConfiguration(GraphComparerType gc, PCGraphType pcg) {
        this(gc, pcg, FIXPOINT_FORMULA_DEFAULT, NBR_SIM_FLOODING_ITERATIONS_DEFAULT);
    }


    /**
     * Create an RteConfiguration by specifying all the fields. Alternatively, create a RteConfigurationBuilder by calling
     * "new RteConfiguration.RteConfigurationBuilder()" appropriately.
     * @param gc
     * @param pcg
     * @param iterations
     */
    public RteConfiguration(GraphComparerType gc, PCGraphType pcg, FixpointFormulaType fpf, int iterations)  {
        graphComparer = gc;
        pcGraph = pcg;
        fixpointFormula = fpf;
        nbrSimFloodingIterations = iterations;
    }


}
