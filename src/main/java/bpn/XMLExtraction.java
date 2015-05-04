package bpn;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qingqingcai on 4/28/15.
 */
public class XMLExtraction {

    public static void readDataFromDirectory(String directoryPath, ArrayList<Data> sayList) {

        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                String filepath = file.getAbsolutePath();
                if (filepath.endsWith(".xml") && isProcessed(sayList, filepath)==false) {
                    readXML(filepath, sayList);
                }
            } else if (file.isDirectory()) {
                readDataFromDirectory(file.getAbsolutePath(), sayList);
            }
        }
    }

    /** **************************************************************
     * Read data from XML file, add all "say" statement into sayList
     */
    public static void readXML(String filepath, ArrayList<Data> sayList) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line = br.readLine();
            while (line != null) {
                String[] elements = extractElements(line);
                if (elements != null) {
                    if (elements[1] != null) {
                        Data data = new Data();
                        data.id = sayList.size();
                        data.text = elements[1];
                        data.type = "say";
                        data.source = filepath;
                        sayList.add(data);
                    }
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** **************************************************************
     * Extract "id" and "name" from the given string
     */
    public static String[] extractElements(String line) {

        if (line.isEmpty())
            return null;

        String[] elements = new String[2];
        Pattern p = Pattern.compile(" *<userTask id=\"(.*)\" name=\"[S|s]ay (.*)\" ");
        Matcher m = p.matcher(line);
        if (m.find()) {
            String id = m.group(1);
            String name = m.group(2);
            name = name.replaceAll("&quot;|&lt;|br/>", "");
//            System.out.println("\n---begin debug");
//            System.out.println("id = " + id);
//            System.out.println("name = " + name);
//            System.out.println("---end debug\n");

            elements[0] = id;
            elements[1] = name;
        }
        return elements;
    }

    /** **************************************************************
     * print out expected and actual "id" and "name" extractions
     */
    public static void print(String input, String[] expected, String[] actual) {

        System.out.println("\n-----------------------------------------------");
        System.out.println("input = " + input);
        System.out.println("expected = ");
        System.out.println("\texpected_id = " + expected[0]);
        System.out.println("\texpected_name = " + expected[1]);
        System.out.println("actual = ");
        System.out.println("\tactual_id = " + actual[0]);
        System.out.println("\tactual_name = " + actual[1]);
        System.out.println("-----------------------------------------------\n");
    }

    /** **************************************************************
     * check if the current file has been processed before due to the
     * duplicate files in different directories
     */
    public static boolean isProcessed(ArrayList<Data> sayList, String filepath) {

        for (Data data : sayList) {
            String existed_filepath = data.source;
            String existed_filename = existed_filepath.substring(existed_filepath.lastIndexOf('/')+1);
            String current_filename = filepath.substring(filepath.lastIndexOf('/')+1);

            if (current_filename.equals(existed_filename))
                return true;
        }
        return false;
    }

    public static void test1() {

        String input = "    <userTask id=\"sid-c7fa5f25-0ff6-4572-af53-51403e19f719\" name=\"Say hi\" activiti:exclusive=\"false\"/>";
        String[] expected = {"sid-c7fa5f25-0ff6-4572-af53-51403e19f719", "hi"};
        String[] actual = extractElements(input);
        print(input, expected, actual);

        input = "    <userTask id=\"sid-376e19ff-c64b-4ce0-ba8a-cb6a328da627\" name=\"say Thank you for the information\" activiti:exclusive=\"false\"/>";
        expected[0] = "sid-376e19ff-c64b-4ce0-ba8a-cb6a328da627";
        expected[1] = "Thank you for the information";
        actual = extractElements(input);
        print(input, expected, actual);

        input = "    <userTask id=\"sid-92b6491f-ec7f-449b-b305-6ac809fded7b\" name=\"say &quot;The invoice number is ${invoice_number}.&lt;br/> The invoice amount is ${invoice_amount}.&lt;br/> The date is ${input2display}&quot; \" activiti:exclusive=\"false\"/>";
        expected[0] = "sid-92b6491f-ec7f-449b-b305-6ac809fded7b";
        expected[1] = "&quot;The invoice number is ${invoice_number}.&lt;br/> The invoice amount is ${invoice_amount}.&lt;br/> The date is ${input2display}&quot; ";
        actual = extractElements(input);
        print(input, expected, actual);
    }

    public static void test2() {

        String inputPath = "/Users/qingqingcai/Documents/IntellijWorkspace/BPN/data/bpn_2/bpm-model/greeting_customized_6.bpmn20.xml";
        ArrayList<Data> sayList = new ArrayList<Data>();
        readXML(inputPath, sayList);
        for (Data data : sayList)
            System.out.println(data.text);
    }

    public static void run(String directory, ArrayList<Data> sayList) {

    //    test1();
    //    test2();

        readDataFromDirectory(directory, sayList);          // read all raw data from xml
    }

    /**
     public static void readXML(String filepath, ArrayList<String> sayList) {

     File file = new File(filepath);
     DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
     try {
     DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
     Document doc = dBuilder.parse(file);
     doc.getDocumentElement().normalize();

     //  System.out.println("root of xml file " + doc.getDocumentElement().getNodeName());
     Node root = doc.getDocumentElement();
     NodeList nodes = root.getChildNodes();
     for (int i = 0; i < nodes.getLength(); i++) {
     Node node = nodes.item(i);
     if (node.getNodeName().equals("process")) {
     NodeList nodes2 = node.getChildNodes();
     for (int j = 0; j < nodes2.getLength(); j++) {
     Node node2 = nodes2.item(j);
     if (node2.getNodeType() == node2.ELEMENT_NODE) {
     Element element = (Element) node2;
     if (element != null)
     System.out.println("value1 = " + node2.getNodeName() + "\t" + getValue("id", element));
     }

     }
     }

     }


     //            NodeList nodes = doc.getElementsByTagName("process");
     //            for (int i = 0; i < nodes.getLength(); i++) {
     //                Node node = nodes.item(i);
     //                if (node.getNodeType() == node.ELEMENT_NODE) {
     //                    Element element = (Element) node;
     //                    System.out.println("value1 = " + getValue("userTask", element));
     //                }
     //
     //
     //                System.out.println("node_name = " + node.getNodeName()
     //                        + "\tnode value = " + node.getNodeValue());
     //            }

     } catch (ParserConfigurationException e) {
     e.printStackTrace();
     } catch (SAXException e) {
     e.printStackTrace();
     } catch (IOException e) {
     e.printStackTrace();
     }
     }

     public static String getValue(String tag, Element element) {
     NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
     Node node = (Node) nodes.item(0);
     if (node == null)
     return null;
     return node.getNodeValue();
     }
     **/
}