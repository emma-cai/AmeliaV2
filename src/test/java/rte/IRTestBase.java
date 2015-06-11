package rte;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import rte.datastructure.DNode;
import rte.datastructure.DTree;
import rte.datastructure.Graph;
import rte.graphmatching.GraphComparer;
import rte.graphmatching.RteConfiguration;
import rte.similarityflooding.NodePair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Set;

//import net.ipsoft.eliza.framework.util.Pair;

/**
 * Base class for IR unit tests.
 */
public class IRTestBase {

    protected final RteConfiguration defaultSimFloodingConfiguration = new RteConfiguration
            .RteConfigurationBuilder(RteConfiguration.GraphComparerType.BASIC_SIMILARITY_FLOODING, RteConfiguration.PCGraphType.IDENTICAL_LABELS_ONLY)
            .nbrSimFloodingIterations(10)
            .build();

    protected GraphComparer getTestGraphComparer()    {
        return RteConfiguration.GraphComparerType.getGraphComparer(defaultSimFloodingConfiguration.graphComparer);
    }


    /**
     * Given output from a call to toString() on a List<NodePair>, returns a List<List<String>> where:
     * (1) the outer list corresponds with each node pair, and (2) the inner list has three parts: (a) the
     * values of the first node of the node pair; (b) the values of the second node of the node pair; and
     * (c) the similarity of the two nodes in the node pair.
     * @param expectedOutput
     * @return
     */
    protected List<List<String>> parseNodePairSet(String expectedOutput) {
        List<List<String>> returnList = Lists.newArrayList();

        String[] nodePairSplit = expectedOutput.split("NodePair");
        for(String nodePair : nodePairSplit)    {
            List<String> nodePairList = Lists.newArrayList();

            if(! nodePair.contains(";"))    {
                // First item of split, contains nothing of interest.
                continue;
            }

            String[] semicolonSplit = nodePair.split(";");
            nodePairList.add(semicolonSplit[0].split("=")[1]);
            nodePairList.add(semicolonSplit[1].split("=")[1]);

            String[] equalsSplit = semicolonSplit[2].split("=");
            String simValue = equalsSplit[1];
            nodePairList.add(simValue);

            returnList.add(nodePairList);
        }

        return returnList;
    }

    protected Graph stringToDGraph(String in)    {
        DTree dtree = Graph.buildTree(in);
        return Graph.buildDGraph(dtree);
    }

    /**
     * From the given node pairs, get all the forms for the various nodes.
     * FIXME: Currently this method expects NodePairs of type DNode.
     * @param pairs
     * @return
     */
    public Multiset<String> getFormsFromNodePairs(Collection<NodePair> pairs)  {
        Multiset<String> returnSet = HashMultiset.create();

        for (NodePair pair : pairs) {
            if (pair.node1 instanceof DNode && pair.node2 instanceof DNode) {
                String form1 = ((DNode) pair.node1).getForm();
                String form2 = ((DNode) pair.node2).getForm();
                returnSet.addAll(Lists.newArrayList(form1, form2));
            }
            else    {
                // Assume none of the nodes in the NodePair collection are DNodes.
                return returnSet;
            }
        }
        return returnSet;
    }

    public Multiset<Pair<String, String>> getFormPairsFromNodePairs(Set<NodePair> pairs) {
        Multiset<Pair<String, String>> returnSet = HashMultiset.create();

        for (NodePair pair : pairs) {
            if (pair.node1 instanceof DNode && pair.node2 instanceof DNode) {
                String form1 = ((DNode) pair.node1).getForm();
                String form2 = ((DNode) pair.node2).getForm();
                returnSet.add(new ImmutablePair<String, String>(form1, form2));
            }
            else    {
                // Assume none of the nodes in the NodePair collection are DNodes.
                return returnSet;
            }
        }

        return returnSet;

    }


}
