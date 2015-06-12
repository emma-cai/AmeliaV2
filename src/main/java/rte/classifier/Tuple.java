package rte.classifier;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by qingqingcai on 6/11/15.
 */
public class Tuple {

    public int id;
    public String label;
    public double[] featureVector;
    public double distance;

    // for training data
    public Tuple(int id, double[] featureVector, String label) {

        this.id = id;
        this.featureVector = featureVector;
        this.label = label;
    }

    // for predict
    public Tuple(double[] featureVector) {

        this.featureVector = featureVector;
        label = null;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(label).append(StringUtils.SPACE);
        for(double v : featureVector)
            stringBuilder.append(v).append(StringUtils.SPACE);

        return stringBuilder.toString().trim();
    }
}
