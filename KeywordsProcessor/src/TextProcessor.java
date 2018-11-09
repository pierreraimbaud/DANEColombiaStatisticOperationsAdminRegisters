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
    private final static String CSV_PATH = "./data/file.csv";
    private final static String DEST_JSON_FILE_PATH = "./data/data.json";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "id";

    /**
     * The separator of the CSV file
     */
    private static final String CSV_SEPARATOR = ",";

    /**
     * The number of columns in the CSV file
     */
    private static final int COLUMN_NUMBER_CSV_FILE =12;

    /**
     * Variables for writing the whole file for cluster or json
     */
    private static String jsonResultText ="{\n\t\"nodes\": [\n";

    /**
     * Map for the nodes for JSON file
     */
    private static Map<Integer, Node> nodesMap = new ConcurrentHashMap<>();


    @SuppressWarnings("deprecation")
    public static void main(String[] args) {

        //Get the "hidden" keywords from CSV
        String clustersResults= KeywordsProcessor.getKeywordsListWithOccurrences();
        System.out.println(clustersResults);
        //Build nodes map from CSV
        /*buildNodesMapReadingCSVFile();

        //Write for JSON
        jsonResultText ="{\n\t\"nodes\": [\n";

        //Build nodes lines for JSON from node map
        List<String> nodeLines2 = createDeepNodeLinesForJson(nodesMap, createReverseResultsList(nodesMap, clustersResults));

        //Build "cluster nodes" lines for JSON from node map
        List<String> clusterLines2 =createClusterNodesDeepLines(clustersResults);

        //Build link lines for JSON from node map
        List<String> deepLinkLines = createDeepLinkLinesForJson(clustersResults);

        //Write for JSON
        writeLinesFromListInString(nodeLines2,false);
        writeLinesFromListInString(clusterLines2,true);
        jsonResultText +="\n\t],\n\t\"links\": [\n";
        writeLinesFromListInString(deepLinkLines,true);
        jsonResultText +="\n\t]\n}";

        //Create JSON file
        createJsonFile(jsonResultText);
*/
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
							splitStr[9], splitStr[10], splitStr[11]));
				}
			}
		};
	}

    /**
     * Allow, from a map and an input list of keywords, to write a reversed list TODO EXPLAIN
     * @param mapN the input map
     * @param resultFromMining the input list of keywords
     * @return a reversed list TODO EXPLAIN
     */
	private static List<List<String>> createReverseResultsList(Map<Integer, Node> mapN, String resultFromMining) {

		List<List<String>> reverseResultsList= new ArrayList<>();

		String[] lines = resultFromMining.trim().split("\n");

		for (String line:lines) {
			String[] lineValues = line.trim().split(CSV_SEPARATOR);
			if (lineValues.length>1){
			    if (lineValues[1].length()>1){
                    lineValues[1]=lineValues[1].substring(1);
                    lineValues[lineValues.length-1]=lineValues[lineValues.length-1].substring(0,lineValues[lineValues.length-1].length()-1);
                    String name =lineValues[0];
                    for (int j = 1; j < lineValues.length; j++) {
                        List<String> smallList= new ArrayList<>();
                        smallList.add(lineValues[j].trim());
                        smallList.add(name);
                        List <String> el = containsElementInListOfList(mapN.get(Integer.parseInt(lineValues[j].trim())).getId(), 0, reverseResultsList);
                        if(null == el){
                            reverseResultsList.add(smallList);
                        }
                        else{
                            int pos =reverseResultsList.indexOf(el);
                            if(!reverseResultsList.get(pos).contains(name)){
                                reverseResultsList.get(pos).add(name);
                            }
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
     * @param indexInSecondList the index where to find inside the second list of the principal list (for  example, the 4th element of each element of the principal list)
     * @param list the input list
     * @return the second list which contains the element, otherwise null
     */
    private static List containsElementInListOfList(String element, int indexInSecondList, List<List<String>> list){
        for (List innerList:list) {
            if(element.equals(innerList.get(indexInSecondList))){
                return innerList;
            }
        }
        return null;
    }

	/** Allow to get a the list of String which represent all the nodes lines
	 * @param mapN the input map of nodes
	 * @param reverseResultsList  a reversed list of list TODO EXPLAIN
	 * @return the list of String which represent all the nodes lines
	 */
	private static List<String> createDeepNodeLinesForJson(Map<Integer, Node> mapN, List<List<String>> reverseResultsList) {
		//{"id": "50", "group": "Artístico-Cultural"}
		List<String> nodesLines= new ArrayList<String>();

		List<Integer> values = new ArrayList<>(mapN.keySet());

		for (List<String> line:reverseResultsList) {
			String group="";
			for(int i=1; i<line.size();i++) {
				group+=line.get(i)+" ";
			}
			group=group.substring(0, group.length()-1);
			nodesLines.add("{\"id\": \""+line.get(0)+"\", \"name\": \""+mapN.get(values.get(Integer.parseInt(line.get(0))-1)).getName()+"\", \"group\": \""+group.trim()+" \",\"main\": \""+"false\"}");
		}
		return nodesLines;
	}

	/** Allow to get a the list of String which represent all the cluster nodes lines
	 * @param clusterNodesInputLinesFromCSV  the input list for the cluster nodes for CSV file
	 * @return the list of String which represent all the cluster nodes lines
	 */
	private static List<String> createClusterNodesDeepLines(String clusterNodesInputLinesFromCSV) {

		String[] lines = clusterNodesInputLinesFromCSV.trim().split("\n");
		List<String> nodesLines= new ArrayList<>();
		for (String line:lines) {
			String name = line.trim().split(CSV_SEPARATOR)[0];

			nodesLines.add("{\"id\": \""+name+"\", \"name\": \""+name+"\", \"group\": \""+name.trim()+" \",\"main\": \""+"true\"}");
		}
		return nodesLines;
	}

    /** Allow to get a the list of String which represent all the link lines
     * @param clusterAndIDlist  the input list for the links
     * @return the list of String which represent all the links lines TODO EXPLAIN
     */
	private static List<String> createDeepLinkLinesForJson(String clusterAndIDlist) {
		//{"source": "Napoleon", "target": "Myriel", "value": 1},
		List<String> linkLines= new ArrayList<>();

		String[] lines = clusterAndIDlist.trim().split("\n");

		for (String line:lines) {
			String[] lineValues = line.trim().split(CSV_SEPARATOR);
			if(lineValues.length>1) {
                if (lineValues[1].length()>1) {
                    lineValues[1] = lineValues[1].substring(1);
                    lineValues[lineValues.length - 1] = lineValues[lineValues.length - 1].substring(0, lineValues[lineValues.length - 1].length() - 1);
                    String name = lineValues[0];
                    for (int j = 1; j < lineValues.length; j++) {
                        linkLines.add("{\"source\": \"" + Integer.parseInt(lineValues[j].trim()) + "\", \"target\": \"" + name + "\", \"value\": \"" + 1 + "\"}");
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
        System.out.println(jsonResultText);
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
        } catch (IOException e) {
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