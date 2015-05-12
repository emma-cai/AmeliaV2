package ipgraph.stanford.semanticgraph;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.trees.Tree;
import ipgraph.parser.pcfg.StanfordPCFGParser;



/**
 * Created by qingqingcai on 5/5/15.
 */
public class SemanticGraphTry {

    public static void main(String[] args) {
        String s = "What are transported into the RER during synthesis?";
        StanfordPCFGParser pcfgParser = new StanfordPCFGParser("", false);
        Tree tree = pcfgParser.getLexicalizedParser().parse(s);
        SemanticGraph semanticGraph = SemanticGraphFactory.generateCollapsedDependencies(tree);
        System.out.println(semanticGraph);
        for (IndexedWord v : semanticGraph.vertexSet()) {
            System.out.println();
            System.out.println("v.toString = " + v.toString());
            System.out.println("v.docID = " + v.docID());
            System.out.println("v.lemma = " + v.lemma());
            System.out.println("v.originalText = " + v.originalText());
            System.out.println("v.tag = " + v.tag());
        }
    }

    private static SemanticGraph makeGraph(Tree tree) {

        SemanticGraph semanticGraph = SemanticGraphFactory.generateCollapsedDependencies(tree);
        return semanticGraph;
     //   return SemanticGraphFactory.makeFromTree(tree, SemanticGraphFactory.Mode.BASIC, GrammaticalStructure.Extras.MAXIMAL, true);
    }



//    public static SemanticGraph makeFromTree(Tree tree, SemanticGraphFactory.Mode mode, boolean includeExtras, boolean threadSafe, Filter<TypedDependency> filter) {
//        Filter<String> wordFilt;
//        if (SemanticGraphFactory.INCLUDE_PUNCTUATION_DEPENDENCIES) {
//            wordFilt = Filters.acceptFilter();
//        } else {
//            wordFilt = new PennTreebankLanguagePack().punctuationWordRejectFilter();
//        }
//        GrammaticalStructure gs = new EnglishGrammaticalStructure(tree, wordFilt, new SemanticHeadFinder(true), threadSafe);
//        return makeFromTree(gs, mode, includeExtras, threadSafe, filter);
//    }
}
