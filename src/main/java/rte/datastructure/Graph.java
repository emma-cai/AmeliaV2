package rte.datastructure;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import rte.graphmatching.NodeComparer;
import rte.parser.pcfg.StanfordPCFGParser;
import rte.similarityflooding.Edge;
import rte.utils.LangTools;

import java.util.*;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class Graph extends SimpleGraph<Object, DefaultWeightedEdge> {

    private static String modelFile = "";
    private static String posTaggerModel = "";

    private static StanfordPCFGParser pcfgParser = new StanfordPCFGParser(modelFile, posTaggerModel, true);

    public static final Set<String> postagSet = NodeComparer.postagSet;

    public Graph(Class<? extends DefaultWeightedEdge> aClass) {
        super(aClass);
    }

    public Graph(Class<? extends DefaultWeightedEdge> aClass,
                 Set<DNode> vertexSubset, Set<DefaultWeightedEdge> edgeSubset) {

        super(aClass);

        for (DNode v : vertexSubset)
            this.addVertex(v);

        for (DefaultWeightedEdge e : edgeSubset)
            this.addEdge(this.getEdgeSource(e), this.getEdgeTarget(e));
    }


    /** **************************************************************
     * Build a Graph from a set of edges.
     * @param edges
     * @return
     */
    public static Graph buildGraph(Set<Edge> edges) {
        Graph graph = new Graph(DefaultWeightedEdge.class);

        // Initialize DefaultWeightedEdge set
        for (Edge e : edges) {
            Object source = e.source;
            Object target = e.target;
            graph.addVertex(source);
            graph.addVertex(target);
            graph.addEdge(source, target);
        }

        return graph;
    }

    /** **************************************************************
     * Build an undirected graph from dependency tree
     */
    public static Graph buildDGraph(DTree dtree) {

        Graph dgraph = new Graph(DefaultWeightedEdge.class);

        // Initialize vertex set
        Set<Integer> processed = new HashSet<>();
        for (DNode n : dtree) {
            if (!processed.contains(n.getId())
                    && !n.getDepLabel().equals("erased")) {
                dgraph.addVertex(n);
                processed.add(n.getId());
            }
        }

        // Initialize edge set
        for (DNode n : dtree) {
            List<DNode> children = n.getChildren();
            for (DNode c : children) {
                if (!c.getDepLabel().equals("erased")) {
                    dgraph.addEdge(n, c);
                }
            }
        }

        return dgraph;
    }

//    /** **************************************************************
//     * Convert plain text to graph using parser in ElizaNLPService
//     */
//    public static Graph stringToGraph(ElizaNLPService elizaNLPService, String text) {
//
//        Graph graph;
//        if (elizaNLPService != null) {
//            SRUAnalysis sruAnalysis= elizaNLPService.parseSentence(text.toLowerCase());
//            DEPTree deptree = sruAnalysis.getDepTree();
//            String conllx = deptree.toStringCoNLL().replaceAll("\n", "\t_\t_\n") + "\t_\t_";
//            DTree dtree = buildTreeFromConllx(conllx);
//            graph = Graph.buildDGraph(dtree);
//        } else {
//            throw new IllegalStateException(("elizaNLPService is null."));
//        }
//
//        return graph;
//    }
//
//    /** **************************************************************
//     * Convert plain text to conllx using parser in ElizaNLPService
//     */
//    public static String stringToConllx(ElizaNLPService elizaNLPService, String text) {
//
//        String conllx;
//        if (elizaNLPService != null) {
//            SRUAnalysis sruAnalysis = elizaNLPService.parseSentence(text.toLowerCase());
//            DEPTree deptree = sruAnalysis.getDepTree();
//            conllx = deptree.toStringCoNLL().replaceAll("\n", "\t_\t_\n") + "\t_\t_";
//        } else {
//            throw new IllegalStateException(("elizaNLPService is null."));
//        }
//
//        return conllx;
//    }

    public static Graph stringToGraph(String text) {

        if (text.isEmpty()) { return null; }
        DTree dtree = buildTree(text);
        return Graph.buildDGraph(dtree);
    }

    public static String textToConllx(String text) {

        DTree dtree = pcfgParser.parse(text);
        String conllx = dtree.toString();
        return conllx;
    }

    /** **************************************************************
     * Convert conllx string to graph
     */
    public static Graph conllxToGraph(String conllx) {

        DTree dtree = buildTreeFromConllx(conllx);
        return Graph.buildDGraph(dtree);
    }

    /** **************************************************************
     * Print Vertex and Edge set
     */
    public String toString() {

        StringBuilder builder = new StringBuilder();
        Set<DefaultWeightedEdge> edges = this.edgeSet();
        for (DefaultWeightedEdge edge : edges) {
            Object obj = this.getEdgeTarget(edge);

            if(obj instanceof DNode) {
                DNode node = (DNode) obj;
                builder.append(node.getId()).append("\t\t");
                builder.append(node.getForm()).append("\t\t");
                builder.append(node.getLemma()).append("\t\t");
                builder.append(node.getcPOSTag()).append("\t\t");
                builder.append(node.getPOS()).append("\t\t");
                builder.append(node.getHead().getId()).append("\t\t");
                builder.append(node.getDepLabel());
                builder.append(System.lineSeparator());
            }
            else    {
                obj.toString();
            }
        }

        return builder.toString();
    }

    public static String toString(Subgraph subgraph) {

        StringBuilder builder = new StringBuilder();
        Set<DefaultEdge> edges = subgraph.edgeSet();
        for (DefaultEdge edge : edges) {
            DNode node = (DNode) subgraph.getEdgeTarget(edge);

            builder.append(node.getId()).append("\t\t");
            builder.append(node.getForm()).append("\t\t");
            builder.append(node.getLemma()).append("\t\t");
            builder.append(node.getPOS()).append("\t\t");
            builder.append(node.getHead().getId()).append("\t\t");
            builder.append(node.getDepLabel());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /** **************************************************************
     * Find the shortest path from "from" to "to"
     */
    public List findShortestPath(DNode from, Object to) {

        List path = new ArrayList();
        if (!this.vertexSet().contains(from)) {
            //    System.out.println("WARNING: " + from + " is not in the graph!");
            return path;
        } else if (!this.vertexSet().contains(to)) {
            //    System.out.println("WARNING: " + to + " is not in the graph!");
            return path;
        }

        path = DijkstraShortestPath.findPathBetween(this, from, to);
        if (path == null)
            path = new ArrayList<>();
        return path;
    }

    /** **************************************************************
     * Build an undirected graph from dtree, where all nodes whose POS
     * tag is NN, NNS, NNP, NNPS
     */
    public Graph getSubgraph(Set<String> posTags) {

        DNode root = this.getNodeById(0);
        Set edgeSubset = new HashSet<>();
        Set vertexSubset = new HashSet<>();
        for (Object vn : this.vertexSet()) {
            if (filterSubGraphByNodeType(vn, posTags)) {
                vertexSubset.add(vn);
                List<DefaultWeightedEdge> path = findShortestPath(root, vn);
                edgeSubset.addAll(path);
                for (DefaultWeightedEdge p : path) {
                    Object sn = this.getEdgeSource(p);
                    Object tn = this.getEdgeTarget(p);
                    vertexSubset.add(sn);
                    vertexSubset.add(tn);
                }
            }
        }
        Subgraph subgraph = new Subgraph(this, vertexSubset, edgeSubset);
        Graph dsubgraph = new Graph(DefaultWeightedEdge.class, vertexSubset, edgeSubset);
        return dsubgraph;
    }

    /** **************************************************************
     * Get the first node (with smallest ID) with a specific posTag
     */
    public DNode getFirstNodeWithPosTag(String posTag) {

        int size = this.vertexSet().size();
        for (int i = 1; i <= size; i++) {
            DNode dnode = this.getNodeById(i);
            if (posTag.equals(dnode.getPOS()))
                return dnode;
        }
        return null;
    }

    /** **************************************************************
     * Return -1 if A is not the direct ancestor of C; otherwise return
     * the generation level; e.g. if A is the parent of C, then generation
     * level = 1; if A is exactly the same C, then generation level = 0;
     */
    public int isAncestorOf(DNode A, DNode D) {

        if (A.getId() == D.getId())
            return 0;

        int generation = 1;
        Queue<DNode> queue = new LinkedList<>();
        queue.add(A);
        queue.add(null);
        while (!queue.isEmpty()) {
            DNode T = queue.remove();
            if (T == null) {
                generation++;
                if (!queue.isEmpty())
                    T = queue.remove();
            }
            if (queue.isEmpty())
                break;
            List<DNode> children = T.getChildren();
            for (DNode C : children) {
                if (C.getId() == D.getId())
                    return generation;
                queue.add(C);
            }
            if (!children.isEmpty())
                queue.add(null);
        }

        return -1;
    }

    /** **************************************************************
     * Find the lowest common ancestor for the given node list;
     */
    public DNode getLowestCommonAncestor(List<DNode> dnodeList) {

        if (dnodeList.isEmpty()) {
            System.out.println("WARNING: NO GOLDEN ANSER IS FOUND!");
            return null;
        }
        int size = dnodeList.size();
        if (size == 1)
            return dnodeList.get(0);
        else {
            int lowest = Integer.MAX_VALUE;
            List<DNode> nodeWithSameLevel = new ArrayList<>();
            for (DNode node : dnodeList) {
                int level = this.getLevel(node);
                if (level < lowest) {
                    lowest = level;
                    nodeWithSameLevel = new ArrayList<>();
                    nodeWithSameLevel.add(node);
                } else if (level == lowest) {
                    nodeWithSameLevel.add(node);
                }
            }
            if (nodeWithSameLevel.size() == 1)
                return nodeWithSameLevel.get(0);
            else if (nodeWithSameLevel.size() >= 2) {
                DNode dnode0 = nodeWithSameLevel.get(0);
                DNode dnode1 = nodeWithSameLevel.get(1);
                List path = findShortestPath(dnode0, dnode1);
                if (path == null || path.isEmpty())
                    return null;
                DefaultWeightedEdge edge = (DefaultWeightedEdge) path.get(0);
                return (DNode) this.getEdgeSource(edge);
            } else {
                System.out.println("ERROR: ERRORS IN FINDING LOWEST COMMON ANCESTOR!");
                return null;
            }
        }
    }

    /** **************************************************************
     * Get the first node with one of give posTags
     */
    public DNode getFirstNodeWithPosTag(Set<String> posTagList) {

        int size = this.vertexSet().size();
        for (int i = 1; i <= size; i++) {
            DNode dnode = this.getNodeById(i);
            if (dnode != null && posTagList.contains(dnode.getPOS()))
                return dnode;
        }
        return null;
    }

    /** **************************************************************
     * Compute the level / height from root to the given node
     */
    public int getLevel(DNode node) {

        List path = findShortestPath(this.getNodeById(0), node);
        if (path.isEmpty())
            return -1;
        else
            return path.size();
    }

    /** **************************************************************
     * Lets us allow or reject subgraphs according to the type of node object.
     * @param obj
     * @return
     */
    private boolean filterSubGraphByNodeType(Object obj, Set<String> posTags)    {
        if (obj instanceof DNode)   {
            DNode node = (DNode) obj;
            return posTags.contains(node.getPOS());
        }
        return true;
    }

    /** **************************************************************
     * In graph, return the node whose id is equal to targetId
     * FIXME: Returns null if this is not a graph of DNode objects
     */
    public DNode getNodeById(int targetID) {

        Set<Object> vertexSet = this.vertexSet();
        for (Object n : vertexSet) {
            if (! (n instanceof DNode))   {
                return null;
            }
            DNode node = (DNode) n;
            if (targetID == node.getId())
                return node;
        }
        return null;
    }

    /** **************************************************************
     * Build dependency-tree from a plain sentence
     */
    @Deprecated
    public static DTree buildTree(String s) {

        DTree dtree = pcfgParser.parse(s);

        return dtree;
    }

    /** **************************************************************
     * Build dependency-tree from conllx
     */
    public static DTree buildTreeFromConllx(String conllx) {

        DTree dtree = LangTools.getDTreeFromCoNLLXString(conllx);
        return dtree;
    }
}
