package rte.answerextraction;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import rte.datastructure.DNode;
import rte.datastructure.Graph;
import rte.graphmatching.NodeComparer;

import java.util.*;

import static rte.answerextraction.FeatureExtractorUtils.*;

/**
 * Created by qingqingcai on 6/17/15.
 */
public class FeatureExtractor {

    private static List<String> POSFILTERLIST
            = new ArrayList<>(Arrays.asList(",", "", "``", "''"));

    public static HashMap<String, String> extractFeatures(
            Graph graphT, Graph graphQ, List<DNode> ansCandNodeList) {

        List<String> ansPosList = getFieldList(ansCandNodeList, "pos", POSFILTERLIST);
        List<String> ansDepList = getFieldList(ansCandNodeList, "dep", null);

//        String ansPosStr = getFieldStr(ansCandNodeList, "pos", POSFILTERLIST, "_");
//        String ansDepStr = getFieldStr(ansCandNodeList, "dep", null, "_");
//        String ansLemStr = getFieldStr(ansCandNodeList, "lemma", null, "_").replaceAll("_", " ");

        DNode whNode = graphQ.getFirstNodeWithPosTag(NodeComparer.WhSet);
        whNode = (whNode == null) ? graphQ.getNodeById(1) : whNode;
        String whLemma = whNode.getLemma();

        DNode lcaNode = graphT.getLowestCommonAncestor(ansCandNodeList);
        String lcaPosStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "pos", null, "_");
        String lcaDepStr = getFieldStr(new ArrayList<>(Arrays.asList(lcaNode)), "dep", null, "_");

        HashMap<String, String> feamap = new HashMap<>();

        // # of tokens in answer-candidate
        String fv = Double.toString(ansCandNodeList.size());
        feamap.put("N:a_TN", fv);

        // overlap of QUERY_LEMMA and TEXT_LEMMA
        fv = Double.toString(overlap(graphQ, ansCandNodeList));
        feamap.put("N:overlapN", fv);

        // (wh-word-form, 0/1); 0/1 -> Does answer-candidate contain number/CD?
        fv = ansPosList.contains("CD") ? "1" : "0";
        feamap.put("C:whLemma_a_hasCD", whLemma + "-" + fv);

        // (wh-word-lemma, lca-dep)
        feamap.put("C:whLemma_lcaDep", whLemma + "-" + lcaDepStr);

        return feamap;
    }

    public static Vector toNumericFeature(
            HashMap<Integer, String> FITOFN , HashMap<String, String> feaMap) {

        TreeMap<Integer, Double> numericfeamap = new TreeMap<>();

        for (Integer fi : FITOFN.keySet()) {
            String normalizedFN = FITOFN.get(fi);
            if (isNumeric(normalizedFN)) {
                String fv = feaMap.get(normalizedFN);
                numericfeamap.put(fi, Double.parseDouble(fv));
            } else {
                for (String fntmp : feaMap.keySet()) {
                    String fv = feaMap.get(fntmp);

                    if (isCategorial(fntmp)) {
                        String newfn = fntmp + ":" + fv;
                        if (normalizedFN.equals(newfn))
                            numericfeamap.put(fi, 1.0);
                        else
                            numericfeamap.put(fi, 0.0);
                        break;
                    }
                }
            }
        }
        double[] featureArray = new ArrayList<>(numericfeamap.values())
                .stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
        Vector featureVector = Vectors.dense(featureArray);
        return featureVector;
    }
}
