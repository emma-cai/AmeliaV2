package ipgraph.datastructure;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by qingqingcai on 5/3/15.
 */
public class DPath {

    private DNode from;
    private DNode to;
    private String depLabel;

    public DPath() {

        from = null;
        to = null;
        depLabel = StringUtils.EMPTY;
    }

    public DPath(DNode from, DNode to, String depLabel) {

        this();
        this.from = from;
        this.to = to;
        this.depLabel = depLabel;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(from.getId()).append(":").append(from.getForm()).append("-->");
        builder.append(depLabel).append("-->");
        builder.append(to.getId()).append(":").append(to.getForm());
        return builder.toString().trim();
    }
}
