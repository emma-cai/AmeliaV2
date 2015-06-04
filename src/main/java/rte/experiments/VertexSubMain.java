package rte.experiments;

import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DGraph;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DNode;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.datastructure.DTree;
import net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.VertexSub;

import static net.ipsoft.eliza.ir.rte.sf.ipgraph.matching.VertexSub.excludeNodes;

/**
 * Created by qingqingcai on 5/25/15.
 */
public class VertexSubMain {

    public static void main(String[] args) {

        String T1 = "What has pairs of wings?";
        DTree dtree_T1 = DTree.buildTree(T1);
        DGraph dgraph_T1 = DGraph.buildDGraph(dtree_T1);
        System.out.println("\nsubgraph_T1 = \n" + dgraph_T1.toString());

        String Q1 = "House crickets have conspicuous jumping legs and have two pairs of wings, of which, only the back pair are used for flight.";
        DTree dtree_Q1 = DTree.buildTree(Q1);
        DGraph dgraph_Q1 = DGraph.buildDGraph(dtree_Q1);
        System.out.println("\nsubgraph_Q1 = \n" + dgraph_Q1.toString());


        VertexSub vs = new VertexSub();
        VertexSub.debug = true;
        for (DNode dnode_H : dgraph_Q1.vertexSet()) {
            if (excludeNodes(dnode_H))
                continue;
            for (DNode dnode_T : dgraph_T1.vertexSet()) {
                if (excludeNodes(dnode_T))
                    continue;
                double nodeSimilarity = vs.getNodeSimilarity(dnode_T, dnode_H);
                System.out.println("\n------------------------------------------");
                System.out.println(dnode_H + "\n"
                        + dnode_T + "\n"
                        + nodeSimilarity);
                System.out.println("------------------------------------------");
            }
        }
    }
}
