package rte.similarityflooding;

import rte.graphmatching.NodeComparer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * An edge connect two objects, a source and a target. We expect the object to be either two DNodes or two NodePairs.
 */
public class Edge extends DefaultWeightedEdge {
    public final Object source;
    public final Object target;

    public final String label;

    public Edge(Object n1, String name, Object n2)    {
        source = n1;
        target = n2;
        label = name;
    }

    @Override
    public int hashCode()   {
        return new HashCodeBuilder(17, 31).
                append(source).
                append(label).
                append(target).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge))
            return false;
        if (obj == this)
            return true;

        Edge rhs = (Edge) obj;
        return new EqualsBuilder().
                append(source, rhs.source).
                append(label, rhs.label).
                append(target, rhs.target).
                isEquals();
    }

    @Override
    public String toString()    {
        return label;
    }
//    public String toString()    {
//        return "[source: " + source + "; label: " + label + "; target: " + target + "]";
//    }

    /**
     * Do the two edges match exactly?
     * @param edge
     * @return
     */
    public boolean matches(Edge edge) {
        return this.label.equals(edge.label);
    }

    /**
     * Do the two edges match sufficiently that their sources and targets should be paired in the pairwise connectivity graph?
     * Also returns true if the two edges match exactly.
     * @param edge
     * @return
     */
    public boolean matchesLoosely(Edge edge) {

        if (this.label.matches(edge.label))
            return true;

        // If either edge label maps to the other's edge label...
        if(NodeComparer.equalDeplabelMap.get(this.label).contains(edge.label) || NodeComparer.equalDeplabelMap.get(edge.label).contains(this.label))
            return true;

        return false;
    }
}
