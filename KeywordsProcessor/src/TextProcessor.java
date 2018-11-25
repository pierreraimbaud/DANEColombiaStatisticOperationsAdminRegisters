import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TextProcessor {

    /**
     * Constant for file paths
     */
    private final static String CSV_PATH = "./../data/file.csv";
    private final static String DEST_JSON_FILE_PATH = "./../data/data.json";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "ID";

    /**
     * The separator of the CSV file
     */
    private static final String CSV_SEPARATOR = ",";

    /**
     * The number of columns in the CSV file
     */
    private static final int COLUMN_NUMBER_CSV_FILE =18;

    /**
     * Variables for writing the whole file for cluster or json
     */
    private static String jsonResultText ="{\n\t\"nodes\": [\n";

    /**
     * Map for the nodes for JSON file
     */
    private static Map<Integer, Node> nodesMap = new ConcurrentHashMap<>();

    private static List<String> clusterLinesNames = new ArrayList<>();


    @SuppressWarnings("deprecation")
    public static void main(String[] args) {

        KeywordsProcessor.buildListNotAcceptedKeywords();
        //Get the "hidden" keywords from CSV
        KeywordsProcessor.writeOldCategoryFile();
        String clustersResults= KeywordsProcessor.getKeywordsListWithOccurrences();
        System.out.println(clustersResults);
        //Build nodes map from CSV
        buildNodesMapReadingCSVFile();

        //Write for JSON
        jsonResultText ="{\n\t\"nodes\": [\n";

        List<List<List<String>>> list= createReverseResultsList(nodesMap, clustersResults);
        list.size();
        //Build nodes lines for JSON from node map
        List<String> nodeLines2 = createDeepNodeLinesForJson(nodesMap,list);

        //Build "cluster nodes" lines for JSON from node map
        List<String> clusterLines2 =createClusterNodesDeepLines(clustersResults,nodeLines2);

        //Build link lines for JSON from node map
        List<String> deepLinkLines = createDeepLinkLinesForJson(clustersResults,nodeLines2);

        //Write for JSON
        writeLinesFromListInString(nodeLines2,false);
        writeLinesFromListInString(clusterLines2,true);
        jsonResultText +="\n\t],\n\t\"links\": [\n";
        writeLinesFromListInString(deepLinkLines,true);
        jsonResultText +="\n\t]\n}";

        //Create JSON file
        createJsonFile(jsonResultText);

        /* DEBUG
		int i =0;
		List<Integer> values = new ArrayList<>(nodesMap.keySet());
		while(i < nodesMap.size()) {
			//System.out.println(values.get(i));
			i++;
		}*/

        /* DEBUG
        //Build nodes lines for JSON from node map
        List<String> nodeLines = createNodeLinesForJson(nodesMap);

        //Build "cluster nodes" lines for JSON from node map
        List<String> clusterNodesLines =createClusterNodesLines();

        //Build link lines for JSON from node map
        List<String> linkLines = createLinkLinesForJson(nodesMap, clusterNames);

        //Build one string which will be the content of the final JSON file
		writeLinesFromListInString(nodeLines,false);
		writeLinesFromListInString(clusterNodesLines,true);
		jsonResultText +="\n\t],\n\t\"links\": [\n";
		writeLinesFromListInString(linkLines,true);
		jsonResultText +="\n\t]\n}";
		createJsonFile(jsonResultText);
        */
    }

    /**
     * Allow to build the nodes map reading the CSV file
     */
    @SuppressWarnings("deprecation")
    private static void buildNodesMapReadingCSVFile() {

        try (Stream<String> stream = Files.lines(Paths.get(CSV_PATH))) {
            stream.forEach(getConsumerForBuildingNodesMapReadingCSVFile(nodesMap));
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }

    /**
     * Provide a consumer of file which build a map
     * @param pNodesMap the nodes map
     * @return the consumer of file
     */
    private static Consumer<String> getConsumerForBuildingNodesMapReadingCSVFile(Map<Integer, Node> pNodesMap) {
        return (x) ->{
            String[] splitStr = x.trim().split(CSV_SEPARATOR);
            if(!splitStr[0].equals(ID)) {
                if (splitStr.length==COLUMN_NUMBER_CSV_FILE) {
                    pNodesMap.put(Integer.parseInt(splitStr[0]),new Node(splitStr[0], splitStr[1], splitStr[2],
                            splitStr[3], splitStr[4], splitStr[5],
                            splitStr[6], splitStr[7], splitStr[8],
                            splitStr[9], splitStr[10], splitStr[11]
                            , splitStr[12], splitStr[13], splitStr[14]
                            , splitStr[15], splitStr[16], splitStr[17]));
                }
            }
        };
    }

    /**
     * Allow to remove from array the empty or " " elements
     * @param lineValues the input array
     * @return the new array without the empty or " " elements
     */
    private static String[] removeEmptyValues(String[] lineValues){
        List <String> linesAsList = new ArrayList<>();
        for(String line : lineValues){
            if(!line.trim().isEmpty()) {
                linesAsList.add(line);
            }
        }
        String [] lineAsArray= new String [linesAsList.size()];
        for(int i=0; i< linesAsList.size();i++){
            lineAsArray[i]=linesAsList.get(i);
        }
        return lineAsArray;
    }

    private static Integer containsInFirstElement(List<List<String>> list,String element){
        Integer rep=null;
        for(int i=0; i<list.size();i++){
            List innerList=list.get(i);
            if(innerList.get(0).equals(element)){
                rep= i;
            }
        }
        return rep;
    }

    /**
     * Allow, from a map and an input list of keywords, to write a reversed list TODO EXPLAIN
     * @param mapN the input map
     * @param resultFromMining the input list of keywords
     * @return a reversed list TODO EXPLAIN
     */
    private static List<List<List<String>>> createReverseResultsList(Map<Integer, Node> mapN, String resultFromMining) {

        List<List<List<String>>> reverseResultsList= new ArrayList<>();

        String[] lines = resultFromMining.trim().split("\n");

        for (String line:lines) {
            String[] lineValues = line.trim().split(CSV_SEPARATOR+"|\\[|\\]");
            lineValues = removeEmptyValues(lineValues);
            if (lineValues.length>1){
                String keyword =lineValues[0];
                for (int j = 3; j < lineValues.length; j=j+2) {
                    String lineID =lineValues[j].trim();
                    String lineKeywordNumberOccurrence =lineValues[j+1].trim();

                    List<List<String>> smallListList= new ArrayList<>();

                    List<String> smallList1= new ArrayList<>();
                    smallList1.add(lineID);

                    List<String> smallList2= new ArrayList<>();
                    smallList2.add(keyword);
                    smallList2.add(lineKeywordNumberOccurrence);

                    smallListList.add(smallList1);
                    smallListList.add(smallList2);
                    Integer pos = containsElementInListOfList(lineID, reverseResultsList);
                    if(null == pos){
                        reverseResultsList.add(smallListList);
                    }
                    else{
                        Integer rep=containsInFirstElement(reverseResultsList.get(pos),keyword);
                        if(rep==null){
                            reverseResultsList.get(pos).add(smallList2);
                        }
                        else{
                            List<String> smallList2bis= new ArrayList<>();
                            smallList2bis.add(reverseResultsList.get(pos).get(rep).get(0));
                            smallList2bis.add(reverseResultsList.get(pos).get(rep).get(1));
                            reverseResultsList.get(pos).set(rep,smallList2bis);
                        }
                    }
                }
            }
        }
        return reverseResultsList;
    }

    /**
     * Allow to find an element and return the second list which contains this element, otherwise null
     * @param element the element to find
     * @param list the input list
     * @return the second list which contains the element, otherwise null
     */
    private static Integer containsElementInListOfList(String element, List<List<List<String>>> list){
        for (int j =0; j<list.size();j++) {
            List<List<String>> innerList=list.get(j);
            for (List<String> inInnerList:innerList) {
                if (element.equals(inInnerList.get(0))) {
                    return j;
                }
            }
        }
        return null;
    }


    private static String getBiggestValueKeyword(List<String> groupsList){
        String valMax=groupsList.get(0);
        int valueMax=0;
        try {
            String val = groupsList.get(1);
            valueMax = Integer.parseInt(val);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            //TODO exception OK
        }
        for(int i=1; i< groupsList.size();i=i+2){
            try{
                String val = groupsList.get(i);
                int newValue = Integer.parseInt(val);
                if(newValue>valueMax){
                    valMax = groupsList.get(i-1);
                    valueMax=newValue;
                }
            }
            catch (NumberFormatException e){
                e.printStackTrace();
                //TODO exception OK
            }
        }
        return valMax;
    }

    /** Allow to get a the list of String which represent all the nodes lines
     * @param mapN the input map of nodes
     * @param reverseResultsList  a reversed list of list TODO EXPLAIN
     * @return the list of String which represent all the nodes lines
     */
    private static List<String> createDeepNodeLinesForJson(Map<Integer, Node> mapN, List<List<List<String>>> reverseResultsList) {
        //{"id": "50", "group": "Artístico-Cultural"}
        List<String> nodesLines= new ArrayList<>();

        List<Integer> values = new ArrayList<>(mapN.keySet());

        for (List<List<String>> line:reverseResultsList) {
            List<String> groupsList=new ArrayList<>();
            for(int i=1; i<line.size();i++) {
                String s1 =line.get(i).get(0).trim();
                String s2 =line.get(i).get(1).trim();
                groupsList.add(s1);
                groupsList.add(s2);
            }

            String nodeID =line.get(0).get(0);
            int id = Integer.parseInt(nodeID)-1;
            if(id>1){
                Node node =mapN.get(values.get(id));
                String group=getBiggestValueKeyword(groupsList);
                String groups="[";
                for(int i=0; i<groupsList.size();i=i+2){
                    groups+="[\""+groupsList.get(i).trim()+"\",\""+groupsList.get(i+1).trim()+"\"]"+",";
                }
                groups=groups.substring(0,groups.length()-1);
                groups+="]";
                nodesLines.add("{\"id\": \""+nodeID+"\", \"name\": \""+node.getName()+"\", \"groups\": "+groups
                        +",\"group\": \""+group+"\","
                        +"\"type\": \""+node.getType()+"\","
                        +"\"statisticOperations\": \""+node.getStatisticalOperations()
                        +"\","+"\"objective\": \""+node.getObjective()+"\","
                        +"\"unityObservation\": \""+node.getUnityObservation()+"\","+"\"entityUsing\": \""+node.getEntitiesUsing()+"\","
                        +"\"variables\": \""+node.getVariables()+"\","+"\"daneDependency\": \""+node.getDaneDependency()+"\","
                        +"\"thematic\": \""+node.getThematic()+"\","+"\"thematic2\": \""+node.getThematics2()+"\","
                        +"\"thematicShared\": \""+node.getThematicShared()+"\","+"\"entityProducing\": \""+node.getEntityProducing()+"\","
                        +"\"periodicity\": \""+node.getPeriodicity()+"\","+"\"geographicZone\": \""+node.getGeographicZone()+"\","
                        +"\"geographicDivision\": \""+node.getGeographicDivision()+"\","+"\"indicators\": \""+node.getIndicators()+"\","
                        +"\"methodologyOOEE\": \""+node.getMethodologyOOEE()+"\","+ "\"main\": \""+"false\"}");
            }
        }
        return nodesLines;
    }

    /** Allow to get a the list of String which represent all the cluster nodes lines
     * @param clusterNodesInputLinesFromCSV  the input list for the cluster nodes for CSV file
     * @return the list of String which represent all the cluster nodes lines
     */
    private static List<String> createClusterNodesDeepLines(String clusterNodesInputLinesFromCSV,List<String> nodeLines2) {

        String[] lines = clusterNodesInputLinesFromCSV.trim().split("\n");
        List<String> nodesLines= new ArrayList<>();
        for (int i =0; i<lines.length;i++) {
            String line= lines[i];
            String name = line.trim().split(CSV_SEPARATOR)[0];
            clusterLinesNames.add(name);
            nodesLines.add("{\"id\": \""+(nodeLines2.size()+i)+"\", \"name\": \""+name+"\", \"groups\": ["+"\""+name+"\""+","+"\""+name+"\""+ "],"+"\"group\": \""+name+"\",\"main\": \""+"true\"}");
        }
        return nodesLines;
    }

    /** Allow to get a the list of String which represent all the link lines
     * @param clusterAndIDlist  the input list for the links
     * @return the list of String which represent all the links lines TODO EXPLAIN
     */
    private static List<String> createDeepLinkLinesForJson(String clusterAndIDlist, List<String> nodeLines2) {
        //{"source": "Napoleon", "target": "Myriel", "value": 1},
        List<String> linkLines= new ArrayList<>();

        String[] lines = clusterAndIDlist.trim().split("\n");

        for (String line:lines) {
            String[] lineValues = line.trim().split(CSV_SEPARATOR+"|\\[|\\]");
            lineValues = removeEmptyValues(lineValues);
            if(lineValues.length>1) {
                int id = clusterLinesNames.indexOf(lineValues[0]);
                for (int j = 3; j < lineValues.length; j=j+2) {
                    if(!"1".equals(lineValues[j].trim())&&!"0".equals(lineValues[j].trim())&&!"2".equals(lineValues[j].trim())) {
                        linkLines.add("{\"source\": \"" + lineValues[j].trim() + "\", \"target\": \"" + (id+nodeLines2.size()) + "\", \"value\": \"" + lineValues[j + 1].trim() + "\"}");
                    }
                }
            }
        }
        return linkLines;
    }

    /**
     * Allow to write lines of nodes in a String, with or without the last character
     * @param nodeLines lines of nodes
     * @param withoutLastCharacter true if with the last character, otherwise false
     */
    private static void writeLinesFromListInString(List<String> nodeLines, boolean withoutLastCharacter) {
        for (String line:nodeLines) {
            jsonResultText +=("\t\t"+line+",\n");
        }
        if(withoutLastCharacter) {
            jsonResultText = jsonResultText.substring(0, jsonResultText.length()-2);
        }
    }

    /**
     * Allow to write a text in a json file
     * @param inputText the input text
     */
    @SuppressWarnings("deprecation")
    private static void createJsonFile(String inputText) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter( new FileWriter(DEST_JSON_FILE_PATH));
            writer.write(inputText);
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }
	/*
	/**
     * Allow, from a map, to write lines of nodes as a list of string
     * @param mapN the input map
     * @return the lines of nodes as a list of string
    private static List<String> createNodeLinesForJson(Map<Integer, Node> mapN) {
        int i =0;
        List<String> nodesLines= new ArrayList<>();
        List<Integer> values = new ArrayList<>(mapN.keySet());
        while(i <mapN.size()) {
            Node n=mapN.get(values.get(i));
            nodesLines.add("{\"id\": \""+n.getId()+"\", \"name\": \""+n.getName()+"\", \"group\": \""+n.getGroup().trim()+" \",\"main\": \""+"false\"}");
            if(!clusterNames.contains(n.getGroup())) {
                clusterNames.add(n.getGroup());
            }
            i++;
        }
        return nodesLines;
    }

    /**
     * Allow, from a map, to write lines of cluster nodes as a list of string
     * @return the lines of cluster nodes as a list of string
    private static List<String> createClusterNodesLines() {
        int i =0;
        List<String> nodesLines= new ArrayList<>();
        while(i <clusterNames.size()) {
            nodesLines.add("{\"id\": \""+clusterNames.get(i)+"\", \"name\": \""+clusterNames.get(i)+"\", \"group\": \""+clusterNames.get(i).trim()+" \",\"main\": \""+"true\"}");
            i++;
        }
        return nodesLines;
    }
     *
     * @param mapN the input map of nodes
     * @param clusterNames the cluster list
     * @return the list of String which represent all the links lines
	private static List<String> createLinkLinesForJson(Map<Integer, Node> mapN, List<String> clusterNames) {
		int i =0;
		//{"source": "Napoleon", "target": "Myriel", "value": 1},
		List<String> linkLines= new ArrayList<>();
		List<Integer> values = new ArrayList<>(mapN.keySet());
		while(i <mapN.size()) {
			Node n =mapN.get(values.get(i));
			for (String name:clusterNames) {
				if(name.equalsIgnoreCase(n.getGroup())) {
					linkLines.add("{\"source\": \""+n.getId()+"\", \"target\": \""+name+"\", \"value\": \""+1+"\"}");
				}
			}
			i++;
		}
		return linkLines;
	}
	*/
}