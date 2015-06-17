//package rte.examples;
//
//import edu.ucla.sspace.basis.StringBasisMapping;
//import edu.ucla.sspace.lsa.LatentSemanticAnalysis;
//import edu.ucla.sspace.matrix.NoTransform;
//import edu.ucla.sspace.matrix.factorization.SingularValueDecompositionLibC;
//import edu.ucla.sspace.vector.Vector;
//import rte.datastructure.Graph;
//
//import java.io.IOException;
//
///**
// * Created by qingqingcai on 6/16/15.
// */
//public class ShortAnswerCandidateExtractor {
//
//    public static void main(String[] args) {
//
////        testGraph();
//        testLSA();
//    }
//
//    public static void testGraph() {
//
//        String query = "What was the monetary value of the Nobel Peace prize in 1989 ?";
//        String text = "Each Nobel Prize is worth $ 469,000 .";
//        Graph graphH = Graph.stringToGraph(query);
//        Graph graphT = Graph.stringToGraph(text);
//        System.out.println("\ngraphH = " + graphH);
//        System.out.println("\ngraphT = " + graphT);
//    }
//
//    public static void testLSA() {
//
//        try {
//            LatentSemanticAnalysis lsa = new LatentSemanticAnalysis(true, 2,
//                    new NoTransform(), new SingularValueDecompositionLibC(), false, new StringBasisMapping());
//            lsa.processSpace(System.getProperties());
//            String sen1 = "shipment of gold damaged in a fire.";
//            String sen2 = "gold silver truck";
//
//            Vector projected = lsa.getVector("gold");
//            System.out.println(projected);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
