package rte.datastructure;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A node class that lets each node have more than one incoming edge.
 */
public class SimpleNode {
    String name;

    Map<SimpleNode, String> depLabels = Maps.newHashMap();

    public SimpleNode(String label)  {
        name = label;
    }

    @Override public String toString()  {
        return name;
    }

    /**
     * Label the edge leading from the given source node to this node.
     * @param source
     * @param name
     */
    public void setDepLabel(SimpleNode source, String name)  {
        depLabels.put(source, name);
    }


    public String getDepLabel(SimpleNode nodeSource) {
        if (depLabels.containsKey(nodeSource))  {
            return depLabels.get(nodeSource);
        }
        return "";
    }
}

