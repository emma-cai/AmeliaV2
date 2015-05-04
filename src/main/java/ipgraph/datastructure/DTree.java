package ipgraph.datastructure;

import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import ipgraph.parser.pcfg.StanfordPCFGParser;
import ipgraph.utils.LangTools;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright 2014-2015 maochen.org
 * Author: Maochen.G   contact@maochen.org
 * For the detail information about license, check the LICENSE.txt
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA  02111-1307 USA
 * <p>
 * This follows CoNLL-X shared task: Multi-Lingual Dependency Parsing Format
 * <p>
 * Created by Maochen on 12/8/14.
 */
public class DTree extends ArrayList<DNode> {
    private DNode padding;
    private String sentenceType = StringUtils.EMPTY;
    private String originalSentence = StringUtils.EMPTY;

    @Override
    public String toString() {
        return this.stream()
                .filter(x -> x != padding)
                .map(x -> x.toString() + System.lineSeparator())
                .reduce((x, y) -> x + y).get();
    }

    @Override
    public boolean add(DNode node) {
        if (node == null) return false;
        if (this.contains(node)) return false;

        node.setTree(this);
        return super.add(node);
    }

    public List<DNode> getRoots() {
        return this.stream().parallel().filter(x -> x.getDepLabel().equals(LangLib.DEP_ROOT)).distinct().collect(Collectors.toList());
    }

    public DNode getPaddingNode() {
        return padding;
    }

    public String getOriginalSentence() {
        return originalSentence;
    }

    public void setOriginalSentence(String originalSentence) {
        this.originalSentence = originalSentence;
    }

    public String getSentenceType() {
        return sentenceType;
    }

    public void setSentenceType(String sentenceType) {
        this.sentenceType = sentenceType;
    }

    public DTree() {
        padding = new DNode();
        padding.setId(0);
        padding.setForm("^");
        padding.setLemma("^");
        this.add(padding);
    }

    /** **************************************************************
     * Return DNode with specific ID
     */
    public DNode getNodeById(int id) {

        for (DNode n : this) {
            if (n.getId() == id)
                return n;
        }
        return null;
    }

    /** **************************************************************
     * Return true if n1 and n2 are directly connected in the tree;
     */
    public boolean isDirectlyConnected(DNode n1, DNode n2) {

        return n1.getChildren().contains(n2) || n2.getChildren().contains(n1);
    }

    public boolean isDirectlyConnected(int n1_index, int n2_index) {

        DNode n1 = getNodeById(n1_index);
        DNode n2 = getNodeById(n2_index);

        return isDirectlyConnected(n1, n2);
    }

    /** **************************************************************
     * Return true is n1 is the parent of n2; Otherwise return false;
     */
    public boolean isParent(DNode n1, DNode n2) {
        return n1.getChildren().contains(n2);
    }

    /** **************************************************************
     * TODO:
     * Return the shortest path between n1 and n2. If n1 and n2 are not
     * connected, return null;
     */
    public void getShortestPath(DNode n1, DNode n2, List<DPath> shortestPath, boolean found) {

        List<DNode> children = n1.getChildren();
        if (children.isEmpty())
            return;

        if (children.contains(n2)) {
            shortestPath.add(new DPath(n1, n2, n2.getDepLabel()));
            found = true;
        } else {
            for (DNode ntmp : children) {
                getShortestPath(ntmp, n2, shortestPath, false);
            }
        }

//        if (isParent(n1, n2))
//            return new DPath(n1, n2, n1.getDepLabel());
//        else if (isParent(n2, n1))
//            return new DPath(n2, n1, n2.getDepLabel());
//        else
//            return null;    //DEBUG DEBUG
    }


    public void getShortestPath(int n1_index, int n2_index, List<DPath> shortestPath, boolean found) {

        DNode n1 = this.getNodeById(0);
        DNode n2 = this.getNodeById(1);

        this.getShortestPath(n1, n2, shortestPath, false);
    }

    /** **************************************************************
     * TODO: Get minimum subgraph which contains all nodes in nodeSet;
     */
    public DTree getMinimumSubtree(ArrayList<DNode> nodeSet) {
        return null;
    }


    /**
     *
     */
    public static DTree buildTree(String s) {
        StanfordPCFGParser pcfgParser = new StanfordPCFGParser("", false);
        Tree tree = pcfgParser.getLexicalizedParser().parse(s);

        SemanticHeadFinder headFinder = new SemanticHeadFinder(false); // keep copula verbs as head
        GrammaticalStructure egs = new EnglishGrammaticalStructure(tree, string -> true, headFinder, true);

        String conllx = EnglishGrammaticalStructure.dependenciesToString(egs, egs.typedDependenciesCCprocessed(), tree, true, true);
        System.out.println(conllx);

        DTree dtree = LangTools.getDTreeFromCoNLLXString(conllx, true);
        return dtree;
    }
}
