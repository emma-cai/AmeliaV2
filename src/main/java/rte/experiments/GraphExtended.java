package rte.experiments;

import rte.datastructure.DNode;
import rte.datastructure.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qingqingcai on 6/5/15.
 */
public class GraphExtended {

    public static List<DNode> getNodeList(Graph graph, List<Integer> ANSIDList) {

        List<DNode> ANSDNodeList = new ArrayList<>();
        ANSIDList.forEach(nodeID -> {
            ANSDNodeList.add(graph.getNodeById(nodeID));
        });

        return ANSDNodeList;
    }

    public static List<DNode> getNodeList(Graph graph, String s) {

        return getNodeList(graph, stringToList(s));
    }

    public static List<String> getFieldList(List<DNode> nodeList, String field) {

        List<String> AnsFieldList = new ArrayList<>();

        for (DNode dnode : nodeList) {
            switch (field) {
                case "pos":
                    String pos = dnode.getPOS();
                    AnsFieldList.add(pos);
                    break;
                case "dep":
                    String dep = dnode.getDepLabel();
                    AnsFieldList.add(dep);
                    break;
            }

        }
        return AnsFieldList;
    }

    public static String getFieldStr(List<DNode> nodeList, String field) {

        String AnsFieldStr = "";
        List<String> AnsFieldList = getFieldList(nodeList, field);
        for (String s : AnsFieldList) {
            AnsFieldStr += AnsFieldStr.isEmpty() ? s : ("_" + s);
        }

        return AnsFieldStr;
    }

    public static List<Integer> stringToList(String s) {

        List<Integer> list= new ArrayList<>();
        String[] idArr = s.split(" ");
        for (String id : idArr) {
            if (!id.equals("#"))
                list.add(Integer.parseInt(id));
            else
                break;
        }

        return list;
    }
}
