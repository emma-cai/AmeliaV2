package rte.graphmatching;

import rte.IRTestBase;
import rte.datastructure.Graph;
import rte.similarityflooding.NodePair;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static rte.graphmatching.DMatching.initNodeMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests on DMatching, including similarity flooding tests that use initialized values coming from DMatching.
 */
public class DMatchingTest extends IRTestBase {

    private void runTest(List<NodePair> actualList, List<List<String>> expectedNodePairValues) {
        for (int ct = 0; ct < actualList.size(); ct++)    {
            List<String> expectedList = expectedNodePairValues.get(ct);
            NodePair actualNodePair = actualList.get(ct);

            String failureString = "\niteration = " + ct + "; \nexpected = " + expectedList.get(0) + "; \n actual = " + actualNodePair.node1.toString();
            assertTrue(failureString, actualNodePair.node1.toString().contains(expectedList.get(0)));
            failureString = "\niteration = " + ct + "; \nexpected = " + actualNodePair.node1.toString() + "; \n actual = " + expectedList.get(1);
            assertTrue(failureString, actualNodePair.node2.toString().contains(expectedList.get(1)));
            Double val = new Double(expectedList.get(2));
            failureString = "\niteration = " + ct + "; \nval = " + val;
            assertEquals(failureString, val.doubleValue(), actualNodePair.sim, 0.1);
        }
    }

    @Test
    public void testDefaultBasicSimilarityFlooding()  {
        Graph dgraph1 = stringToDGraph("House Crickets can be found in a variety of habitats including woodlands, suburbs, " +
                "urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.");

        Graph dgraph2 = stringToDGraph("Where are house crickets found?");

        GraphComparer comparer = getTestGraphComparer();
        comparer.init(dgraph1, dgraph2, defaultSimFloodingConfiguration);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        String expectedOutput = "[NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=1.0; sim0=0.5; simN=4.9E-324; simN1=1.0, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.9982256416110027; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.9982256416110027, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.658679567457177; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.658679567457177, NodePair[node1=34\tfood\tfood\tNOUN\tNN\t_\t35\tnn\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.658679567457177; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.658679567457177, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.5713923800325079; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.5713923800325079, NodePair[node1=27\tanywhere\tanywhere\tADV\tRB\t_\t28\tadvmod\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.5713923800325079; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.5713923800325079, NodePair[node1=4\tbe\tbe\tAUX\tVB\t_\t5\tauxpass\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.5004435898518736; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5004435898518736, NodePair[node1=1\tHouse\tHouse\tPROPN\tNNP\t_\t2\tnn\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.49866923146287634; sim0=0.33181222783183384; simN=4.9E-324; simN1=0.49866923146287634]";
        List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);

        System.out.println(actualList.toString());

        runTest(actualList, expectedNodePairValues);
    }

    @Test
    public void testModifiedParamsBasicSimilarityFlooding()  {
        Graph dgraph1 = stringToDGraph("House Crickets can be found in a variety of habitats including woodlands, suburbs, " +
                "urban areas, buildings, ducts, siding, restaurants and anywhere else there happens to be a food supply and warm air.");

        Graph dgraph2 = stringToDGraph("Where are house crickets found?");

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();
        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(dgraph1, dgraph2, config);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        String expectedOutput = "[NodePair[node1=12\twoodlands\twoodlands\tNOUN\tNNS\t_\t11\tpobj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=1.0; sim0=0.27888482197713693; simN=4.9E-324; simN1=1.0, NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.6392370962309546; sim0=0.41338242108267; simN=4.9E-324; simN1=0.6392370962309546, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.5154068663501767; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.5154068663501767, NodePair[node1=12\twoodlands\twoodlands\tNOUN\tNNS\t_\t11\tpobj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.4237170405819626; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.4237170405819626, NodePair[node1=30\thappens\thappens\tVERB\tVBZ\t_\t0\troot\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.4061760784090138; sim0=0.36586440898919925; simN=4.9E-324; simN1=0.4061760784090138, NodePair[node1=32\tbe\tbe\tVERB\tVB\t_\t30\txcomp\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.2807994372395667; sim0=0.289050497374996; simN=4.9E-324; simN1=0.2807994372395667, NodePair[node1=8\tvariety\tvariety\tNOUN\tNN\t_\t6\tpobj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.271903626015908; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.271903626015908, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.2374364750566727; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.2374364750566727, NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.22321389857758647; sim0=0.299432857526027; simN=4.9E-324; simN1=0.22321389857758647, NodePair[node1=38\tair\tair\tNOUN\tNN\t_\t35\tconj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.15851981075109273; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15851981075109273, NodePair[node1=30\thappens\thappens\tVERB\tVBZ\t_\t0\troot\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.15851981075109273; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15851981075109273, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.15851981075109273; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15851981075109273, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.15851981075109273; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15851981075109273, NodePair[node1=17\tareas\tareas\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.15851981075109273; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15851981075109273, NodePair[node1=9\tof\tof\tADP\tIN\t_\t8\tprep\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.15137635629402782; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.15137635629402782, NodePair[node1=10\thabitats\thabitats\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.1504893471490584; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.1504893471490584, NodePair[node1=8\tvariety\tvariety\tNOUN\tNN\t_\t6\tpobj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.1476976704976062; sim0=0.289050497374996; simN=4.9E-324; simN1=0.1476976704976062, NodePair[node1=6\tin\tin\tADP\tIN\t_\t5\tprep\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.14637923219918628; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.14637923219918628, NodePair[node1=11\tincluding\tincluding\tVERB\tVBG\t_\t10\tprep\t_\t_; node2=5\tfound\tfound\tVERB\tVBN\t_\t0\troot\t_\t_; sim=0.14341004385479827; sim0=0.289050497374996; simN=4.9E-324; simN1=0.14341004385479827, NodePair[node1=32\tbe\tbe\tVERB\tVB\t_\t30\txcomp\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.13830696384488078; sim0=0.299432857526027; simN=4.9E-324; simN1=0.13830696384488078, NodePair[node1=6\tin\tin\tADP\tIN\t_\t5\tprep\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.10722524168203829; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.10722524168203829, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.10722524168203831; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.10722524168203831, NodePair[node1=10\thabitats\thabitats\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.10184520410457257; sim0=0.289050497374996; simN=4.9E-324; simN1=0.10184520410457257, NodePair[node1=11\tincluding\tincluding\tVERB\tVBG\t_\t10\tprep\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.10085406977715212; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.10085406977715212, NodePair[node1=9\tof\tof\tADP\tIN\t_\t8\tprep\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.09239338175346164; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.09239338175346164, NodePair[node1=38\tair\tair\tNOUN\tNN\t_\t35\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.08908883069986738; sim0=0.289050497374996; simN=4.9E-324; simN1=0.08908883069986738, NodePair[node1=17\tareas\tareas\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.08686850934565549; sim0=0.289050497374996; simN=4.9E-324; simN1=0.08686850934565549, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.08572439775459818; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.08572439775459818, NodePair[node1=8\tvariety\tvariety\tNOUN\tNN\t_\t6\tpobj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05376228096012412; sim0=0.289050497374996; simN=4.9E-324; simN1=0.05376228096012412, NodePair[node1=1\tHouse\tHouse\tPROPN\tNNP\t_\t2\tnn\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05376228096012413; sim0=0.289050497374996; simN=4.9E-324; simN1=0.05376228096012413, NodePair[node1=4\tbe\tbe\tAUX\tVB\t_\t5\tauxpass\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05323399521282364; sim0=0.36586440898919925; simN=4.9E-324; simN1=0.05323399521282364, NodePair[node1=6\tin\tin\tADP\tIN\t_\t5\tprep\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=6\tin\tin\tADP\tIN\t_\t5\tprep\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=4\tbe\tbe\tAUX\tVB\t_\t5\tauxpass\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=4\tbe\tbe\tAUX\tVB\t_\t5\tauxpass\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=3\tcan\tcan\tAUX\tMD\t_\t5\taux\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=3\tcan\tcan\tAUX\tMD\t_\t5\taux\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=3\tcan\tcan\tAUX\tMD\t_\t5\taux\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05323079773047805; sim0=0.3543436937742045; simN=4.9E-324; simN1=0.05323079773047805, NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=37\twarm\twarm\tADJ\tJJ\t_\t38\tamod\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=37\twarm\twarm\tADJ\tJJ\t_\t38\tamod\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=37\twarm\twarm\tADJ\tJJ\t_\t38\tamod\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=32\tbe\tbe\tVERB\tVB\t_\t30\txcomp\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=29\tthere\tthere\tDET\tEX\t_\t30\texpl\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=27\tanywhere\tanywhere\tADV\tRB\t_\t28\tadvmod\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=27\tanywhere\tanywhere\tADV\tRB\t_\t28\tadvmod\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=27\tanywhere\tanywhere\tADV\tRB\t_\t28\tadvmod\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=16\turban\turban\tADJ\tJJ\t_\t17\tamod\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=16\turban\turban\tADJ\tJJ\t_\t17\tamod\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=16\turban\turban\tADJ\tJJ\t_\t17\tamod\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=1\tHouse\tHouse\tPROPN\tNNP\t_\t2\tnn\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=1\tHouse\tHouse\tPROPN\tNNP\t_\t2\tnn\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=1\tHouse\tHouse\tPROPN\tNNP\t_\t2\tnn\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05283993691703091; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05283993691703091, NodePair[node1=11\tincluding\tincluding\tVERB\tVBG\t_\t10\tprep\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.05113787166842336; sim0=0.299432857526027; simN=4.9E-324; simN1=0.05113787166842336, NodePair[node1=12\twoodlands\twoodlands\tNOUN\tNNS\t_\t11\tpobj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.050549531819296174; sim0=0.289050497374996; simN=4.9E-324; simN1=0.050549531819296174, NodePair[node1=10\thabitats\thabitats\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.05033451192893931; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05033451192893931, NodePair[node1=10\thabitats\thabitats\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.05033451192893931; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.05033451192893931, NodePair[node1=11\tincluding\tincluding\tVERB\tVBG\t_\t10\tprep\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.050093815296543345; sim0=0.289050497374996; simN=4.9E-324; simN1=0.050093815296543345, NodePair[node1=11\tincluding\tincluding\tVERB\tVBG\t_\t10\tprep\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.050090993894659076; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.050090993894659076, NodePair[node1=9\tof\tof\tADP\tIN\t_\t8\tprep\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04934242760493948; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04934242760493948, NodePair[node1=7\ta\ta\tDET\tDT\t_\t8\tdet\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04934242760493948; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04934242760493948, NodePair[node1=8\tvariety\tvariety\tNOUN\tNN\t_\t6\tpobj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.048683208455729515; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.048683208455729515, NodePair[node1=8\tvariety\tvariety\tNOUN\tNN\t_\t6\tpobj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.048683208455729515; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.048683208455729515, NodePair[node1=12\twoodlands\twoodlands\tNOUN\tNNS\t_\t11\tpobj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04797127777762679; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04797127777762679, NodePair[node1=12\twoodlands\twoodlands\tNOUN\tNNS\t_\t11\tpobj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.04797127777762679; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04797127777762679, NodePair[node1=38\tair\tair\tNOUN\tNN\t_\t35\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04757381170669047; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04757381170669047, NodePair[node1=34\tfood\tfood\tNOUN\tNN\t_\t35\tnn\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04757381170669047; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04757381170669047, NodePair[node1=36\tand\tand\tCONJ\tCC\t_\t35\tcc\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04757087179827196; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04757087179827196, NodePair[node1=33\ta\ta\tDET\tDT\t_\t35\tdet\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04757087179827196; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04757087179827196, NodePair[node1=25\trestaurants\trestaurants\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=23\tsiding\tsiding\tNOUN\tNN\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=21\tducts\tducts\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=19\tbuildings\tbuildings\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=17\tareas\tareas\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=14\tsuburbs\tsuburbs\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04703192901240689; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04703192901240689, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.047028989103988374; sim0=0.299432857526027; simN=4.9E-324; simN1=0.047028989103988374, NodePair[node1=26\tand\tand\tCONJ\tCC\t_\t12\tcc\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.047028989103988374; sim0=0.299432857526027; simN=4.9E-324; simN1=0.047028989103988374, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.046730465838563766; sim0=0.299432857526027; simN=4.9E-324; simN1=0.046730465838563766, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.046730465838563766; sim0=0.299432857526027; simN=4.9E-324; simN1=0.046730465838563766, NodePair[node1=31\tto\tto\tPART\tTO\t_\t32\taux\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.046730465838563766; sim0=0.299432857526027; simN=4.9E-324; simN1=0.046730465838563766, NodePair[node1=31\tto\tto\tPART\tTO\t_\t32\taux\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.046730465838563766; sim0=0.299432857526027; simN=4.9E-324; simN1=0.046730465838563766, NodePair[node1=31\tto\tto\tPART\tTO\t_\t32\taux\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.046730465838563766; sim0=0.299432857526027; simN=4.9E-324; simN1=0.046730465838563766, NodePair[node1=10\thabitats\thabitats\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04658055325102172; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04658055325102172, NodePair[node1=35\tsupply\tsupply\tNOUN\tNN\t_\t32\txcomp\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04632589058721472; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04632589058721472, NodePair[node1=31\tto\tto\tPART\tTO\t_\t32\taux\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.046323069185330454; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.046323069185330454, NodePair[node1=37\twarm\twarm\tADJ\tJJ\t_\t38\tamod\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04522149356697717; sim0=0.299432857526027; simN=4.9E-324; simN1=0.04522149356697717, NodePair[node1=9\tof\tof\tADP\tIN\t_\t8\tprep\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04521815950269361; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04521815950269361, NodePair[node1=9\tof\tof\tADP\tIN\t_\t8\tprep\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.04521815950269361; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04521815950269361, NodePair[node1=7\ta\ta\tDET\tDT\t_\t8\tdet\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04521815950269361; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04521815950269361, NodePair[node1=7\ta\ta\tDET\tDT\t_\t8\tdet\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04521815950269361; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04521815950269361, NodePair[node1=7\ta\ta\tDET\tDT\t_\t8\tdet\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.04521815950269361; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04521815950269361, NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04509531358338452; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04509531358338452, NodePair[node1=32\tbe\tbe\tVERB\tVB\t_\t30\txcomp\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04509531358338452; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.04509531358338452, NodePair[node1=5\tfound\tfound\tVERB\tVBN\t_\t30\tdep\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.045092373674966; sim0=0.299432857526027; simN=4.9E-324; simN1=0.045092373674966, NodePair[node1=32\tbe\tbe\tVERB\tVB\t_\t30\txcomp\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.045092373674966; sim0=0.299432857526027; simN=4.9E-324; simN1=0.045092373674966, NodePair[node1=29\tthere\tthere\tDET\tEX\t_\t30\texpl\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.045092373674966; sim0=0.299432857526027; simN=4.9E-324; simN1=0.045092373674966, NodePair[node1=29\tthere\tthere\tDET\tEX\t_\t30\texpl\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.045092373674966; sim0=0.299432857526027; simN=4.9E-324; simN1=0.045092373674966, NodePair[node1=29\tthere\tthere\tDET\tEX\t_\t30\texpl\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.045092373674966; sim0=0.299432857526027; simN=4.9E-324; simN1=0.045092373674966, NodePair[node1=6\tin\tin\tADP\tIN\t_\t5\tprep\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.044587079945626856; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.044587079945626856, NodePair[node1=4\tbe\tbe\tAUX\tVB\t_\t5\tauxpass\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.044587079945626856; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.044587079945626856, NodePair[node1=3\tcan\tcan\tAUX\tMD\t_\t5\taux\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.044587079945626856; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.044587079945626856, NodePair[node1=2\tCrickets\tCrickets\tPROPN\tNNP\t_\t5\tnsubjpass\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.044587079945626856; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.044587079945626856, NodePair[node1=16\turban\turban\tADJ\tJJ\t_\t17\tamod\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.044182110860946235; sim0=0.299432857526027; simN=4.9E-324; simN1=0.044182110860946235, NodePair[node1=27\tanywhere\tanywhere\tADV\tRB\t_\t28\tadvmod\t_\t_; node2=3\thouse\thouse\tNOUN\tNN\t_\t4\tnn\t_\t_; sim=0.04337310177924797; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.04337310177924797, NodePair[node1=34\tfood\tfood\tNOUN\tNN\t_\t35\tnn\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.042869794839648104; sim0=0.289050497374996; simN=4.9E-324; simN1=0.042869794839648104, NodePair[node1=38\tair\tair\tNOUN\tNN\t_\t35\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=38\tair\tair\tNOUN\tNN\t_\t35\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=36\tand\tand\tCONJ\tCC\t_\t35\tcc\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=36\tand\tand\tCONJ\tCC\t_\t35\tcc\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=36\tand\tand\tCONJ\tCC\t_\t35\tcc\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=34\tfood\tfood\tNOUN\tNN\t_\t35\tnn\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=34\tfood\tfood\tNOUN\tNN\t_\t35\tnn\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=33\ta\ta\tDET\tDT\t_\t35\tdet\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=33\ta\ta\tDET\tDT\t_\t35\tdet\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=33\ta\ta\tDET\tDT\t_\t35\tdet\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.042866973437763835; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.042866973437763835, NodePair[node1=17\tareas\tareas\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.04159966456132894; sim0=0.320821300824607; simN=4.9E-324; simN1=0.04159966456132894, NodePair[node1=25\trestaurants\trestaurants\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04159084682898857; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04159084682898857, NodePair[node1=23\tsiding\tsiding\tNOUN\tNN\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04159084682898857; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04159084682898857, NodePair[node1=21\tducts\tducts\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04159084682898857; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04159084682898857, NodePair[node1=19\tbuildings\tbuildings\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04159084682898857; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04159084682898857, NodePair[node1=14\tsuburbs\tsuburbs\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.04159084682898857; sim0=0.289050497374996; simN=4.9E-324; simN1=0.04159084682898857, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=28\telse\telse\tADV\tRB\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=26\tand\tand\tCONJ\tCC\t_\t12\tcc\t_\t_; node2=4\tcrickets\tcrickets\tNOUN\tNNS\t_\t5\tnsubjpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=26\tand\tand\tCONJ\tCC\t_\t12\tcc\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=26\tand\tand\tCONJ\tCC\t_\t12\tcc\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=25\trestaurants\trestaurants\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=25\trestaurants\trestaurants\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=23\tsiding\tsiding\tNOUN\tNN\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=23\tsiding\tsiding\tNOUN\tNN\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=21\tducts\tducts\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=21\tducts\tducts\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=19\tbuildings\tbuildings\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=19\tbuildings\tbuildings\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=17\tareas\tareas\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=14\tsuburbs\tsuburbs\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=2\tare\tare\tAUX\tVBP\t_\t5\tauxpass\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304, NodePair[node1=14\tsuburbs\tsuburbs\tNOUN\tNNS\t_\t12\tconj\t_\t_; node2=1\tWhere\tWhere\tADV\tWRB\t_\t5\tadvmod\t_\t_; sim=0.041588025427104304; sim0=0.27888482197713693; simN=4.9E-324; simN1=0.041588025427104304]";
        List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);

        System.out.println(actualList.toString());

        runTest(actualList, expectedNodePairValues);

    }

    /**
     * Test on a QA pair that should have high similarity scores.
     */
    @Test
    public void testFrenchRevolutionGood1()  {
        Graph dgraph1 = stringToDGraph("The French Revolution was a period of radical social and political upheaval in France from 1789 to 1799 that had a fundamental " +
                "impact on French history and on modern history worldwide.");

        Graph dgraph2 = stringToDGraph("What was the French Revolution?");

        GraphComparer comparer = getTestGraphComparer();
        comparer.init(dgraph1, dgraph2, defaultSimFloodingConfiguration);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        //System.out.println(actualList.toString());

        /*
        String expectedOutput = "[NodePair[node1=3\tRevolution\tRevolution\tNOUN\tNN\t_\t4\tnsubj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=1.0; sim0=0.5; simN=4.9E-324; simN1=1.0, \n" +
                "NodePair[node1=23\timpact\timpact\tNOUN\tNN\t_\t20\tdobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.5138472065281067; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5138472065281067, \n" +
                "NodePair[node1=12\tupheaval\tupheaval\tNOUN\tNN\t_\t7\tpobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.5138472065281067; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5138472065281067, \n" +
                "NodePair[node1=26\thistory\thistory\tNOUN\tNN\t_\t24\tpobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.4475216014506906; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.4475216014506906, \n" +
                "NodePair[node1=25\tFrench\tFrench\tADJ\tJJ\t_\t26\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.4475216014506906; sim0=0.5; simN=4.9E-324; simN1=0.4475216014506906, \n" +
                "NodePair[node1=6\tperiod\tperiod\tNOUN\tNN\t_\t4\txcomp\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.34256480435207104; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.34256480435207104, \n" +
                "NodePair[node1=5\ta\ta\tDET\tDT\t_\t6\tdet\t_\t_; node2=3\tthe\tthe\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.34256480435207104; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.34256480435207104, \n" +
                "NodePair[node1=30\thistory\thistory\tNOUN\tNN\t_\t28\tpobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.34256480435207104; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.34256480435207104, \n" +
                "NodePair[node1=29\tmodern\tmodern\tADJ\tJJ\t_\t30\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.34256480435207104; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.34256480435207104, \n" +
                "NodePair[node1=4\twas\twas\tVERB\tVBD\t_\t0\troot\t_\t_; node2=2\twas\twas\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.3333354686848368; sim0=0.5; simN=4.9E-324; simN1=0.3333354686848368, \n" +
                "NodePair[node1=2\tFrench\tFrench\tADJ\tJJ\t_\t3\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.3333354686848368; sim0=0.5; simN=4.9E-324; simN1=0.3333354686848368, \n" +
                "NodePair[node1=1\tThe\tThe\tDET\tDT\t_\t3\tdet\t_\t_; node2=3\tthe\tthe\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.3333290626303264; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3333290626303264, \n" +
                "NodePair[node1=20\thad\thad\tVERB\tVBD\t_\t6\trcmod\t_\t_; node2=2\twas\twas\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.3198667279581056; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3198667279581056, \n" +
                "NodePair[node1=19\tthat\tthat\tDET\tWDT\t_\t20\tnsubj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.3198667279581056; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.3198667279581056, \n" +
                "NodePair[node1=18\t1799\t1799\tNUM\tCD\t_\t15\tpobj\t_\t_; node2=2\twas\twas\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.29716865156414013; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.29716865156414013, \n" +
                "NodePair[node1=17\tto\tto\tADP\tTO\t_\t18\tdep\t_\t_; node2=1\tWhat\tWhat\tPRON\tWP\t_\t2\tdep\t_\t_; sim=0.29716865156414013; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.29716865156414013, \n" +
                "NodePair[node1=9\tsocial\tsocial\tADJ\tJJ\t_\t12\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.25692360326405334; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.25692360326405334, \n" +
                "NodePair[node1=8\tradical\tradical\tADJ\tJJ\t_\t12\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.25692360326405334; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.25692360326405334, \n" +
                "NodePair[node1=22\tfundamental\tfundamental\tADJ\tJJ\t_\t23\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.25692360326405334; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.25692360326405334, \n" +
                "NodePair[node1=21\ta\ta\tDET\tDT\t_\t23\tdet\t_\t_; node2=3\tthe\tthe\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.25692360326405334; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.25692360326405334]";
        */
        //List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);
        //runTest(actualList, expectedNodePairValues);

        NodePair actual = actualList.get(0);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*Revolution.*nsubj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(1.0, actual.sim, 0);

        actual = actualList.get(1);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*impact.*dobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(0.51, actual.sim, 0.1);

        actual = actualList.get(2);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*upheaval.*pobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(0.51, actual.sim, 0.1);


    }

    /**
     * Test on a QA pair that should have low similarity scores.
     */
    @Ignore
    @Test
    public void testFrenchRevolutionBad1()  {
        Graph dgraph1 = stringToDGraph("The next few years were dominated by struggles between various liberal assemblies and right-wing supporters of the monarchy intent on thwarting major reforms.");

        Graph dgraph2 = stringToDGraph("What was the French Revolution?");

        GraphComparer comparer = getTestGraphComparer();
        comparer.init(dgraph1, dgraph2, defaultSimFloodingConfiguration);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        //System.out.println(actualList.toString());

        /*
        String expectedOutput = "[NodePair[node1=4\tyears\tyears\tNOUN\tNNS\t_\t6\tnsubjpass\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=1.0; sim0=0.3100255188723875; simN=4.9E-324; simN1=1.0, \n" +
                "NodePair[node1=12\tassemblies\tassemblies\tNOUN\tNNS\t_\t9\tpobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.75; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.75, \n" +
                "NodePair[node1=19\tintent\tintent\tNOUN\tNN\t_\t16\tpobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.6531926160615585; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.6531926160615585, \n" +
                "NodePair[node1=17\tthe\tthe\tDET\tDT\t_\t19\tdet\t_\t_; node2=3\tthe\tthe\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.6531926160615585; sim0=0.5; simN=4.9E-324; simN1=0.6531926160615585, \n" +
                "NodePair[node1=23\treforms\treforms\tNOUN\tNNS\t_\t21\tdobj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.5; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5, \n" +
                "NodePair[node1=22\tmajor\tmajor\tADJ\tJJ\t_\t23\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.5; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5, \n" +
                "NodePair[node1=15\tsupporters\tsupporters\tNOUN\tNNS\t_\t8\tconj\t_\t_; node2=5\tRevolution\tRevolution\tNOUN\tNN\t_\t2\tnsubj\t_\t_; sim=0.5; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5, \n" +
                "NodePair[node1=14\tright-wing\tright-wing\tADJ\tJJ\t_\t15\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.5; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5, \n" +
                "NodePair[node1=11\tliberal\tliberal\tADJ\tJJ\t_\t12\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.375; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.375, \n" +
                "NodePair[node1=10\tvarious\tvarious\tADJ\tJJ\t_\t12\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.375; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.375, \n" +
                "NodePair[node1=3\tfew\tfew\tADJ\tJJ\t_\t4\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.3333333333333333; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3333333333333333, \n" +
                "NodePair[node1=2\tnext\tnext\tADJ\tJJ\t_\t4\tamod\t_\t_; node2=4\tFrench\tFrench\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.3333333333333333; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3333333333333333, \n" +
                "NodePair[node1=1\tThe\tThe\tDET\tDT\t_\t4\tdet\t_\t_; node2=3\tthe\tthe\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.3333333333333333; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3333333333333333]";
                */
        //List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);
        //runTest(actualList, expectedNodePairValues);

        NodePair actual = actualList.get(0);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*years.*nsubjpass.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(1.0, actual.sim, 0);

        actual = actualList.get(1);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*assemblies.*pobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(0.75, actual.sim, 0.1);

        actual = actualList.get(2);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*intent.*pobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*Revolution.*nsubj.*"));
        assertEquals(0.65, actual.sim, 0.1);

    }

    /**
     * Test on a QA pair that should have high similarity scores.
     */
    @Test
    public void testFrenchRevolutionGood2()  {
        Graph dgraph1 = stringToDGraph("Experiencing an economic crisis exacerbated by the Seven Years War and the American Revolutionary War, the common people of France became " +
                "increasingly frustrated by the ineptitude of King Louis XVI and the continued decadence of the aristocracy.");

        Graph dgraph2 = stringToDGraph("What exacerbated an economic crisis?");

//        RteConfiguration config = new RteConfiguration
//                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
//                .nbrSimFloodingIterations(10)
//                .fixpointFormula(RteConfiguration.FixpointFormulaType.BASIC_FORMULA)
//                .build();
//        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);
//        comparer.init(dgraph1, dgraph2, config);

        GraphComparer comparer = getTestGraphComparer();
        comparer.init(dgraph1, dgraph2, defaultSimFloodingConfiguration);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

//        double simTotal = 0.0;
//        for(NodePair pair : actualList) {
//            simTotal += pair.sim;
//        }
//        System.out.println("Sim total = " + simTotal);


        System.out.println(actualList.toString());

        NodePair actual = actualList.get(0);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*crisis.*dobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(1.0, actual.sim, 0);

        actual = actualList.get(1);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*people.*nsubj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(0.51, actual.sim, 0.1);

        actual = actualList.get(2);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*decadence.*conj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches("..*crisis.*dobj.*"));
        assertEquals(0.51, actual.sim, 0.1);


        /*
        String expectedOutput = "[NodePair[node1=4\tcrisis\tcrisis\tNOUN\tNN\t_\t1\tdobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=1.0; sim0=0.5; simN=4.9E-324; simN1=1.0, \n" +
                "NodePair[node1=19\tpeople\tpeople\tNOUN\tNNS\t_\t22\tnsubj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.5138472065281068; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5138472065281068, \n" +
                "NodePair[node1=35\tdecadence\tdecadence\tNOUN\tNN\t_\t31\tconj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.4911491301341412; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.4911491301341412, \n" +
                "NodePair[node1=38\taristocracy\taristocracy\tNOUN\tNN\t_\t36\tpobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.3425648043520711; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3425648043520711, \n" +
                "NodePair[node1=37\tthe\tthe\tDET\tDT\t_\t38\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.3425648043520711; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3425648043520711, \n" +
                "NodePair[node1=27\tineptitude\tineptitude\tNOUN\tNN\t_\t25\tpobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.3425648043520711; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3425648043520711, \n" +
                "NodePair[node1=26\tthe\tthe\tDET\tDT\t_\t27\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.3425648043520711; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.3425648043520711, \n" +
                "NodePair[node1=3\teconomic\teconomic\tADJ\tJJ\t_\t4\tamod\t_\t_; node2=4\teconomic\teconomic\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.33333546868483677; sim0=0.5; simN=4.9E-324; simN1=0.33333546868483677, \n" +
                "NodePair[node1=2\tan\tan\tDET\tDT\t_\t4\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.33333546868483677; sim0=0.5; simN=4.9E-324; simN1=0.33333546868483677, \n" +
                "NodePair[node1=1\tExperiencing\tExperiencing\tVERB\tVBG\t_\t22\tvmod\t_\t_; node2=2\texacerbated\texacerbated\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.33332906263032636; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.33332906263032636, \n" +
                "NodePair[node1=7\tthe\tthe\tDET\tDT\t_\t10\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.31986672795810567; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=22\tbecame\tbecame\tVERB\tVBD\t_\t0\troot\t_\t_; node2=2\texacerbated\texacerbated\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.31986672795810567; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=19\tpeople\tpeople\tNOUN\tNNS\t_\t22\tnsubj\t_\t_; node2=1\tWhat\tWhat\tPRON\tWP\t_\t2\tnsubj\t_\t_; sim=0.31986672795810567; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=15\tWar\tWar\tPROPN\tNNP\t_\t10\tconj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.31986672795810567; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=12\tthe\tthe\tDET\tDT\t_\t15\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.31986672795810567; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=10\tWar\tWar\tPROPN\tNNP\t_\t6\tpobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.31986672795810567; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.31986672795810567, \n" +
                "NodePair[node1=18\tcommon\tcommon\tADJ\tJJ\t_\t19\tamod\t_\t_; node2=4\teconomic\teconomic\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.2569236032640534; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.2569236032640534, \n" +
                "NodePair[node1=17\tthe\tthe\tDET\tDT\t_\t19\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.2569236032640534; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.2569236032640534, \n" +
                "NodePair[node1=33\tthe\tthe\tDET\tDT\t_\t35\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.24557525775739023; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.24557525775739023, \n" +
                "NodePair[node1=34\tcontinued\tcontinued\tVERB\tVBN\t_\t35\tamod\t_\t_; node2=4\teconomic\teconomic\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.24557387237675096; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.24557387237675096]";*/

        //List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);
        //runTest(actualList, expectedNodePairValues);

    }

    /**
     * Test on a QA pair that should have low similarity scores.
     */
    @Ignore
    @Test
    public void testFrenchRevolutionBad2_1()  {
        Graph dgraph1 = stringToDGraph("Internally, popular agitation radicalized the Revolution significantly, culminating in the rise of Maximilien Robespierre and the Jacobins.");

        Graph dgraph2 = stringToDGraph("What exacerbated an economic crisis?");

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .fixpointFormula(RteConfiguration.FixpointFormulaType.BASIC_FORMULA)
                .build();
        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(dgraph1, dgraph2, config);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        System.out.println(actualList.toString());

        double simTotal = 0.0;
        for(NodePair pair : actualList) {
            simTotal += pair.sim;
        }
        System.out.println("Sim total = " + simTotal);

        NodePair actual = actualList.get(0);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*Revolution.*dobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(1.0, actual.sim, 0);

        actual = actualList.get(1);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*radicalized.*root.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*exacerbated.*root.*"));
        assertEquals(0.99, actual.sim, 0.1);

        actual = actualList.get(2);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*agitation.*nsubj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(0.77, actual.sim, 0.1);

        /*
        String expectedOutput = "[NodePair[node1=7\tRevolution\tRevolution\tNOUN\tNN\t_\t5\tdobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=1.0; sim0=0.3100255188723875; simN=4.9E-324; simN1=1.0, \n" +
                "NodePair[node1=5\tradicalized\tradicalized\tVERB\tVBD\t_\t0\troot\t_\t_; node2=2\texacerbated\texacerbated\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.999084626813639; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.999084626813639, \n" +
                "NodePair[node1=4\tagitation\tagitation\tNOUN\tNN\t_\t5\tnsubj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=3\tpopular\tpopular\tADJ\tJJ\t_\t4\tamod\t_\t_; node2=4\teconomic\teconomic\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=13\trise\trise\tNOUN\tNN\t_\t11\tpobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=12\tthe\tthe\tDET\tDT\t_\t13\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=19\tJacobins\tJacobins\tPROPN\tNNPS\t_\t16\tconj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7239699056827146; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.7239699056827146, \n" +
                "NodePair[node1=18\tthe\tthe\tDET\tDT\t_\t19\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.7239699056827146; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7239699056827146, \n" +
                "NodePair[node1=6\tthe\tthe\tDET\tDT\t_\t7\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.5002288436097923; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5002288436097923, \n" +
                "NodePair[node1=4\tagitation\tagitation\tNOUN\tNN\t_\t5\tnsubj\t_\t_; node2=1\tWhat\tWhat\tPRON\tWP\t_\t2\tnsubj\t_\t_; sim=0.4993134704234312; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.4993134704234312]";
        */

        //List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);
        //runTest(actualList, expectedNodePairValues);

    }

    /**
     * Test on a QA pair that should have low similarity scores.
     */
    @Ignore
    @Test
    public void testFrenchRevolutionBad2_2()  {
        Graph dgraph1 = stringToDGraph("The modern era has unfolded in the shadow of the French Revolution.");

        Graph dgraph2 = stringToDGraph("What exacerbated an economic crisis?");

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .fixpointFormula(RteConfiguration.FixpointFormulaType.BASIC_FORMULA)
                .build();
        GraphComparer comparer = RteConfiguration.GraphComparerType.getGraphComparer(config.graphComparer);

        comparer.init(dgraph1, dgraph2, config);

        // Get init parameter from DMatching.
        Set<NodePair> nodePairSet = comparer.getPCGraphNodes();
        Map<NodePair, Double> initMap = DMatching.computeGraphComparerInitValues(nodePairSet);

        Set<NodePair> actualSet = comparer.compareGraphNodes(initMap);
        List<NodePair> actualList = NodePair.sortOnSimilarity(actualSet);

        System.out.println(actualList.toString());

        double simTotal = 0.0;
        for(NodePair pair : actualList) {
            simTotal += pair.sim;
        }
        System.out.println("Sim total = " + simTotal);

        NodePair actual = actualList.get(0);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*Revolution.*dobj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(1.0, actual.sim, 0);

        actual = actualList.get(1);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*radicalized.*root.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*exacerbated.*root.*"));
        assertEquals(0.99, actual.sim, 0.1);

        actual = actualList.get(2);
        assertTrue("\n" + actual.node1.toString(), actual.node1.toString().matches(".*agitation.*nsubj.*"));
        assertTrue("\n" + actual.node2.toString(), actual.node2.toString().matches(".*crisis.*dobj.*"));
        assertEquals(0.77, actual.sim, 0.1);

        /*
        String expectedOutput = "[NodePair[node1=7\tRevolution\tRevolution\tNOUN\tNN\t_\t5\tdobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=1.0; sim0=0.3100255188723875; simN=4.9E-324; simN1=1.0, \n" +
                "NodePair[node1=5\tradicalized\tradicalized\tVERB\tVBD\t_\t0\troot\t_\t_; node2=2\texacerbated\texacerbated\tVERB\tVBD\t_\t0\troot\t_\t_; sim=0.999084626813639; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.999084626813639, \n" +
                "NodePair[node1=4\tagitation\tagitation\tNOUN\tNN\t_\t5\tnsubj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=3\tpopular\tpopular\tADJ\tJJ\t_\t4\tamod\t_\t_; node2=4\teconomic\teconomic\tADJ\tJJ\t_\t5\tamod\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=13\trise\trise\tNOUN\tNN\t_\t11\tpobj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=12\tthe\tthe\tDET\tDT\t_\t13\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.7753435647407161; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7753435647407161, \n" +
                "NodePair[node1=19\tJacobins\tJacobins\tPROPN\tNNPS\t_\t16\tconj\t_\t_; node2=5\tcrisis\tcrisis\tNOUN\tNN\t_\t2\tdobj\t_\t_; sim=0.7239699056827146; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.7239699056827146, \n" +
                "NodePair[node1=18\tthe\tthe\tDET\tDT\t_\t19\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.7239699056827146; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.7239699056827146, \n" +
                "NodePair[node1=6\tthe\tthe\tDET\tDT\t_\t7\tdet\t_\t_; node2=3\tan\tan\tDET\tDT\t_\t5\tdet\t_\t_; sim=0.5002288436097923; sim0=0.3100255188723875; simN=4.9E-324; simN1=0.5002288436097923, \n" +
                "NodePair[node1=4\tagitation\tagitation\tNOUN\tNN\t_\t5\tnsubj\t_\t_; node2=1\tWhat\tWhat\tPRON\tWP\t_\t2\tnsubj\t_\t_; sim=0.4993134704234312; sim0=0.2689414213699951; simN=4.9E-324; simN1=0.4993134704234312]";
        */

        //List<List<String>> expectedNodePairValues = parseNodePairSet(expectedOutput);
        //runTest(actualList, expectedNodePairValues);

    }

    @Test
    public void testComputeMatchingCost1() {

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("The female is told from the male by the presence of an ovipositor, a long extension " +
                "attached to the end of the abdomen, used for depositing eggs in the soil.");

        Graph graph_Q = stringToDGraph("What is ovipositor?");

        HashMap node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);
        double minMC_TP_Q = DMatching.computeMatchingCost(graph_T, graph_Q, node_SortedMapList_T_Q);
        assertEquals(0.58661757891733, minMC_TP_Q, 0);

    }

    @Test
    public void testComputeMatchingCost2() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("Experiencing an economic crisis exacerbated by the Seven Years War and the American Revolutionary War, the common people of France became " +
                "increasingly frustrated by the ineptitude of King Louis XVI and the continued decadence of the aristocracy.");

        Graph graph_Q = stringToDGraph("What exacerbated an economic crisis?");

        HashMap node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);
        double minMC_TP_Q = DMatching.computeMatchingCost(graph_T, graph_Q, node_SortedMapList_T_Q);
        System.out.println("MatchingCost = " + minMC_TP_Q);
        assertEquals(0.5619286995457285, minMC_TP_Q, 0);
    }

    @Test
    public void testComputeMatchingCost3() {

        DMatching.debugInComputeVertexCost = true;
        VertexSub.debug = false;

        RteConfiguration config = new RteConfiguration
                .RteConfigurationBuilder(RteConfiguration.GraphComparerType.SIMILARITY_FLOODING_INIT, RteConfiguration.PCGraphType.COMPLETE_CROSS_PRODUCT)
                .nbrSimFloodingIterations(10)
                .build();

        Graph graph_T = stringToDGraph("Internally, popular agitation radicalized the Revolution significantly, culminating in the rise of Maximilien Robespierre and the Jacobins.");

        Graph graph_Q = stringToDGraph("What exacerbated an economic crisis?");

        HashMap node_SortedMapList_T_Q = initNodeMatches(graph_T, graph_Q, config);
        double minMC_TP_Q = DMatching.computeMatchingCost(graph_T, graph_Q, node_SortedMapList_T_Q);
        System.out.println("MatchingCost = " + minMC_TP_Q);
        assertEquals(0.657759736829452, minMC_TP_Q, 0);
    }
}
