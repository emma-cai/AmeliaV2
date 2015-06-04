package rte.similarityflooding;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * A pair of nodes with similarity values.
 */
public class NodePair {
    public static final double NULL_SIMILARITY = Double.MIN_VALUE;

    public Object node1;
    public Object node2;

    // Final similarity
    public double sim = NULL_SIMILARITY;

    // Initialized value
    public double sim0 = NULL_SIMILARITY;

    // Current values.
    // FIXME: eliminate this field
    double simN = NULL_SIMILARITY;

    // Temporary value during flooding--"n + 1".
    public double simN1 = NULL_SIMILARITY;

    /**
     * Create a NodePair object from the given objects. The two objects are NOT interchangeable--a NodePair created with NodePair(g1, g2) is NOT
     * the same as one created with NodePair(g2, g1). For consistency, if comparing a number of candidate graphs to a ""hypothesis" graph,
     * the first parameter should be the candidate node, and the second the hypothesis node.
     * @param n1
     * @param n2
     */
    public NodePair(Object n1, Object n2) {
        node1 = n1;
        node2 = n2;
    }

    /**
     * For each of the given edges, return a set of source/target pairs.
     * @param edges
     * @return
     */
    public static Set<NodePair> getNodesInGraph(Set<Edge> edges) {
        Set<NodePair> returnSet = Sets.newHashSet();

        for (Edge edge : edges)   {
            returnSet.add((NodePair) edge.source);
            returnSet.add((NodePair) edge.target);
        }

        return returnSet;
    }

    @Override
    public String toString() {
        return "NodePair[" + "node1=" + node1 + "; node2=" + node2 + "; sim=" + sim + "; sim0=" + sim0 + "; simN=" + simN + "; simN1=" + simN1;
    }

    @Override
    public int hashCode()   {
        return new HashCodeBuilder(17, 31).
                append(node1).
                append(node2).
//                append(sim).
//                append(sim0).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NodePair))
            return false;
        if (obj == this)
            return true;

        NodePair rhs = (NodePair) obj;
        return new EqualsBuilder().
                append(node1, rhs.node1).
                append(node2, rhs.node2).
//                append(sim, rhs.sim).
//                append(sim0, rhs.sim0).
                isEquals();
    }

    /**
     * Sort NodePairs based first on sim field, then on toString().
     */
    public static final Comparator<NodePair> pairSimilarityComparator = new Comparator<NodePair>() {
        @Override
        public int compare(NodePair o1, NodePair o2) {
            // We need to round because for some reason the same input can result in minor differences in similarity.
            //int comp = Double.valueOf(o1.sim).compareTo(o2.sim);
            String str1 = String.valueOf(o1.sim);
            if(str1.length() > 10)  {
                str1 = str1.substring(0, 9);
            }
            String str2 = String.valueOf(o2.sim);
            if(str2.length() > 10)  {
                str2 = str2.substring(0, 9);
            }
            int comp = str1.compareTo(str2);
            if(comp != 0)    {
                return comp;
            }
            else    {
                return o1.toString().compareTo(o2.toString());
            }
        }
    };

    /**
     * Sort the given NodePair collection by similarity, in reverse order (highest first).
     * @param pairs
     * @return
     */
    public static List<NodePair> sortOnSimilarity(Collection<NodePair> pairs) {
        List<NodePair> list = Lists.newArrayList(pairs);
        Collections.sort(list, Collections.reverseOrder(NodePair.pairSimilarityComparator));
        return list;
    }
}
