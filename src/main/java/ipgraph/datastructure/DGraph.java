package ipgraph.datastructure;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class DGraph extends SimpleGraph<Integer, DefaultEdge> {

    public DGraph(Class<? extends DefaultEdge> aClass) {
        super(aClass);
    }

    /** **************************************************************
     * Build an undirected graph from dependency tree
     */
    public static DGraph buildDGraph(DTree dtree) {

        DGraph dgraph = new DGraph(DefaultEdge.class);

        // Initialize vertex set
        Set<Integer> processed = new HashSet<>();
        for (DNode n : dtree) {
            if (!processed.contains(n.getId())
                    && !n.getDepLabel().equals("erased")) {
                dgraph.addVertex(n.getId());
                processed.add(n.getId());
            }
        }

        // Initialize edge set
        for (DNode n : dtree) {
            List<DNode> children = n.getChildren();
            for (DNode c : children) {
                if (!c.getDepLabel().equals("erased")) {
                    dgraph.addEdge(n.getId(), c.getId());
                }
            }
        }
        return dgraph;
    }

    /** **************************************************************
     * Print Vertex and Edge set
     */
    public String toString(DTree dtree) {

        StringBuilder builder = new StringBuilder();
        Set<DefaultEdge> edges = this.edgeSet();
        for (DefaultEdge edge : edges) {
            DNode node = dtree.get((Integer) this.getEdgeTarget(edge));

            builder.append(node.getId()).append("\t");
            builder.append(node.getForm()).append("\t");
            builder.append(node.getLemma()).append("\t");
            builder.append(node.getPOS()).append("\t");
            builder.append(node.getHead().getId()).append("\t");
            builder.append(node.getDepLabel());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    public static String toString(Subgraph dgraph, DTree dtree) {

        StringBuilder builder = new StringBuilder();
        Set<DefaultEdge> edges = dgraph.edgeSet();
        for (DefaultEdge edge : edges) {
            DNode node = dtree.get((Integer) dgraph.getEdgeTarget(edge));

            builder.append(node.getId()).append("\t");
            builder.append(node.getForm()).append("\t");
            builder.append(node.getLemma()).append("\t");
            builder.append(node.getPOS()).append("\t");
            builder.append(node.getHead().getId()).append("\t");
            builder.append(node.getDepLabel());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /** **************************************************************
     * Find the shortest path from "from" to "to"
     */
    public List findShortestPath(int from, int to) {

        List path = new ArrayList();
        if (!this.vertexSet().contains(from)) {
            System.out.println("ERROR: " + from + " is not in the graph!");
            return path;
        } else if (!this.vertexSet().contains(to)) {
            System.out.println("ERROR: " + to + " is not in the graph!");
            return path;
        }

        return DijkstraShortestPath.findPathBetween(this, from, to);
    }

    /** **************************************************************
     * Return the subtree/subgraph containing all nodes in containNodes
     */
    public Subgraph getSubgraph(UndirectedGraph<Integer, DefaultEdge> dgraph, Set<Integer> containNodes) {

        Set edgeSubset = new HashSet<>();
        Set vertexSubset = new HashSet<>();
        for (int nid : containNodes) {
            List<DefaultEdge> path = findShortestPath(0, nid);
            edgeSubset.addAll(path);
            for (DefaultEdge p : path) {
                int sid = dgraph.getEdgeSource(p);
                int tid = dgraph.getEdgeTarget(p);
                vertexSubset.add(sid);
                vertexSubset.add(tid);
            }
        }
        Subgraph subgraph = new Subgraph(dgraph, vertexSubset, edgeSubset);
        return subgraph;
    }

    /** **************************************************************
     * Build an undirected graph from dtree, where all nodes whose POS
     * tag is NN, NNS, NNP, NNPS
     */
    public Subgraph getSubgraph(DTree dtree, Set<String> posTags) {

        Set edgeSubset = new HashSet<>();
        Set vertexSubset = new HashSet<>();
        for (Integer vid : this.vertexSet()) {
            DNode v = dtree.getNodeById(vid);
            if (posTags.contains(v.getPOS())) {
                vertexSubset.add(v);
                List<DefaultEdge> path = findShortestPath(0, vid);
                edgeSubset.addAll(path);
                for (DefaultEdge p : path) {
                    int sid = this.getEdgeSource(p);
                    int tid = this.getEdgeTarget(p);
                    vertexSubset.add(sid);
                    vertexSubset.add(tid);
                }
            }
        }
        Subgraph subgraph = new Subgraph(this, vertexSubset, edgeSubset);
        return subgraph;
    }
}
