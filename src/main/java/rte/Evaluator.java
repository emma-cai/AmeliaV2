//package rte;
//
//import com.google.common.collect.Lists;
//import rte.RteMessageHandler;
//import rte.datastructure.Graph;
//import net.ipsoft.eliza.nlp.ElizaNLPService;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.io.Console;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Lets user evaluate different graph comparison algorithms from the command line.
// */
//public class Evaluator {
//
//    @Autowired
//    public ElizaNLPService elizaNLPService;
//
//    // A paired set of lists, where statements[i] is associated with graphs[i]
//    List<String> statements = Lists.newArrayList();
//    List<Graph> graphs = Lists.newArrayList();
//
//    public Evaluator()  {
//
//    }
//
//    /**
//     * Load or reset a list of input sentences.
//     * @param in
//     */
//    public void setStatements(List<String> in)   {
//        statements = new ArrayList(in); // Should be list instead of set for coref. Order does matter!!
//        graphs.clear();
//
//        for (String s : statements) {
//            Graph graph = Graph.stringToGraph(elizaNLPService, s.toLowerCase());
//            graphs.add(graph);
//        }
//    }
//
//    public String answer(String question)  {
//        if(statements == null || statements.isEmpty())  {
//            return "Please provide input text.";
//        }
//
//        //GraphComparer comparer = new SimilarityFloodingInit();
//
//        Graph graph_Q = Graph.stringToGraph(elizaNLPService, question.toLowerCase());
//
//        return RteSfEvaluator.getMinimumCostMatch(elizaNLPService, question, graph_Q,
//                statements, graphs, RteMessageHandler.defaultRteConfiguration);
//    }
//
//    /**
//     * FIXME: Running from the command line doesn't work. Either fix or remove.
//     * @param args
//     * @throws java.io.IOException
//     */
//    public static void main(String[] args) throws IOException {
//
//        if (args != null && args.length > 0 && args[0].equals("-h")) {
//            System.out.println("Usage: ");
//            System.out.println("Evaluator -h         % show this help info");
//            System.out.println("          -f fname   % use a particular input file");
//        }
//        else if (args != null && args.length > 1 && args[0].equals("-f")) {
//            initialize(args[1]);  // this passes the filename
//            boolean done = false;
//            while (!done) {
//                Console c = System.console();
//                if (c == null) {
//                    System.err.println("No console.");
//                    System.exit(1);
//                }
//                String input = c.readLine("> ");
//                System.out.println(matchBestInput(input));
//            }
//        }
//        else
//            System.out.println("Error, bad command line parameter");
//    }
//
//    private static String matchBestInput(String input) {
//        return "You said: " + input;
//    }
//
//    private static void initialize(String arg) {
//    }
//}
