package rte.graphmatching;

import com.google.common.collect.ImmutableMultimap;
import rte.datastructure.DNode;
import rte.datastructure.LangLib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Compares two nodes.
 */
public interface NodeComparer {

    public static final Set<String> postagSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN,
                    LangLib.POS_NNS,
                    LangLib.POS_NNP,
                    LangLib.POS_NNPS,
                    LangLib.POS_WP}));

    public static final Set<Set<String>> equalPostagSet =
            new HashSet<Set<String>>() { {
                add(new HashSet<>(Arrays.asList(new String[]{LangLib.POS_JJ, LangLib.POS_JJR, LangLib.POS_JJS})));
                add(new HashSet<>(Arrays.asList(new String[] {LangLib.POS_NN, LangLib.POS_NNS} )));
                add(new HashSet<>(Arrays.asList(new String[] {LangLib.POS_NNP, LangLib.POS_NNPS} )));
                add(new HashSet<>(Arrays.asList(new String[] {LangLib.POS_VB, LangLib.POS_VBD, LangLib.POS_VBG, LangLib.POS_VBN, LangLib.POS_VBP, LangLib.POS_VBZ} )));
            }};

    public static final Set<Set<String>> equalDeplabelSet =
            new HashSet<Set<String>>() { {
                add(new HashSet<>(Arrays.asList(new String[] {
                        LangLib.DEP_DOBJ, LangLib.DEP_IOBJ, LangLib.DEP_POBJ, LangLib.DEP_ACOMP, LangLib.DEP_XCOMP, LangLib.DEP_CCOMP
                })));
                add(new HashSet<>(Arrays.asList(new String[] {
                        LangLib.DEP_NSUBJ, LangLib.DEP_NSUBJPASS, LangLib.DEP_CSUBJ, LangLib.DEP_CSUBJPASS
                })));
                add(new HashSet<>(Arrays.asList(new String[] {
                        LangLib.DEP_AMOD, LangLib.DEP_APPOS, LangLib.DEP_ADVCL, LangLib.DEP_DET, LangLib.DEP_PREDET,
                        LangLib.DEP_PRECONJ, LangLib.DEP_VMOD, LangLib.DEP_ADVMOD, LangLib.DEP_RCMOD, LangLib.DEP_QUANTMOD,
                        LangLib.DEP_NN, LangLib.DEP_NPADVMOD, LangLib.DEP_NUM, LangLib.DEP_NUM, LangLib.DEP_PREP,
                        LangLib.DEP_POSS, LangLib.DEP_POSSESSIVE, LangLib.DEP_PRT
                })));
            } };

//    ImmutableMap<String, Set<String>> equalDeplabelMap = ImmutableMap.<String, Set<String>>builder()
//            .put(LangLib.DEP_DEP, Sets.newHashSet(LangLib.DEP_ROOT, LangLib.DEP_XCOMP))
//            .put(LangLib.DEP_ROOT, Sets.newHashSet(LangLib.DEP_VMOD))
//            .put(LangLib.DEP_PREP, Sets.newHashSet(LangLib.DEP_ADVMOD))
//            .put(LangLib.DEP_DOBJ, Sets.newHashSet(LangLib.DEP_NSUBJ))
//            .build();

    ImmutableMultimap<String, String> equalDeplabelMap = new ImmutableMultimap.Builder<String, String>()
            // DEP_ROOT, DEP_XCOMP same as DEP_DEP
            .putAll(LangLib.DEP_DEP, LangLib.DEP_ROOT, LangLib.DEP_XCOMP)
            // DEP_VMOD same as DEP_ROOT
            .put(LangLib.DEP_ROOT, LangLib.DEP_VMOD)
            // DEP_ADVMOD same as DEP_PREP
            .put(LangLib.DEP_PREP, LangLib.DEP_ADVMOD)
            // DEP_NSUBJ same as DEP_DOBJ
            .put(LangLib.DEP_DOBJ, LangLib.DEP_NSUBJ)
            .build();

    // QINGQING: Categorized POSTag
    public static final HashSet<String> VerbSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.POS_VB, LangLib.POS_VBD, LangLib.POS_VBG,
                      LangLib.POS_VBN, LangLib.POS_VBP, LangLib.POS_VBZ} ));

    public static final Set<String> NounSet =
            new HashSet<>(Arrays.asList(new String[]{
                    LangLib.POS_NN, LangLib.POS_NNS,
                    LangLib.POS_NNP, LangLib.POS_NNPS}));

    public static final Set<String> WhSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.POS_WP, LangLib.POS_WPS,
                      LangLib.POS_WDT, LangLib.POS_WRB}));

    public static final Set<String> SubjSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.DEP_NSUBJ, LangLib.DEP_NSUBJPASS,
                      LangLib.DEP_CSUBJ, LangLib.DEP_CSUBJPASS}));

    public static final Set<String> ObjSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.DEP_DOBJ, LangLib.DEP_IOBJ, LangLib.DEP_POBJ,
                      LangLib.DEP_ACOMP, LangLib.DEP_XCOMP, LangLib.DEP_CCOMP}));

    public static final Set<String> AdjSet =
            new HashSet<>(Arrays.asList(new String[]
                    { LangLib.DEP_AMOD, LangLib.DEP_APPOS, LangLib.DEP_ADVCL, LangLib.DEP_DET, LangLib.DEP_PREDET,
                      LangLib.DEP_PRECONJ, LangLib.DEP_VMOD, LangLib.DEP_ADVMOD, LangLib.DEP_RCMOD, LangLib.DEP_QUANTMOD,
                      LangLib.DEP_NN, LangLib.DEP_NPADVMOD, LangLib.DEP_NUM, LangLib.DEP_NUM, LangLib.DEP_PREP,
                      LangLib.DEP_POSS, LangLib.DEP_POSSESSIVE, LangLib.DEP_PRT }));

    /**
     * Calculcate a value from 0 to 1 inclusive that indicates how similar the two nodes are.
     * @param node1
     * @param node2
     * @return
     */
    double getNodeSimilarity(DNode node1, DNode node2);

}
