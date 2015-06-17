package rte.answerextraction;

import org.jgrapht.graph.DefaultWeightedEdge;
import rte.datastructure.DTree;
import rte.datastructure.Graph;
import rte.parser.pcfg.StanfordPCFGParser;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class GraphExtended extends Graph {

    private static StanfordPCFGParser pcfgParser = new StanfordPCFGParser("", "", false);

    public GraphExtended(Class<? extends DefaultWeightedEdge> aClass) {
        super(aClass);
    }

    public static Graph stringToGraph(String text) {

        if (text.isEmpty()) { return null; }
        DTree dtree = buildTree(text);
        return Graph.buildDGraph(dtree);
    }
}
