import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TextProcessor {

    /**
     * Constant for file paths
     */
    private final static String CSV_PATH = "./data/file.csv";
    private final static String FILE_PATH = "./data/file.clustersResultText";
    private final static String DEST_FILE_PATH = "./data/results.clustersResultText";
    private final static String DEST_JSON_FILE_PATH = "./data//data.json";
    private final static String DEST_JSON_FILE_PATH_2 = "./data//data2.json";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "id";

    /**
     * The number of columns in the CSV file
     */
    private static final int COLUMN_NUMBER_CSV_FILE =12;
    /**
     * Variables for writing the whole file for cluster or json
     */
	private static String clustersResultText ="";
    private static String jsonResultText ="{\n\t\"nodes\": [\n";

    /**
     * Cluster names as a list
     */
    private static List<String> clusterNames = new ArrayList<String>();

    /**
     * Map for the nodes for JSON file
     */
    private static Map<Integer, Node> nodesMap = new ConcurrentHashMap<Integer, Node>();

    private static final String CSV_SEPARATOR = ",";
    /**
     * Generic useless words to not consider when searching keywords for clusters
     */
	private static List<String> generalUselessWords =new ArrayList<String>(
			Arrays.asList("de", "la", "el", ",", ";", "", "en", "del", "los", "las", "demás","bajo","les","hecho","hecho,","pertenece","recibió","realiza","realizan",
					"anualmente","mes","para", "con", "que", "-", "–", "sobre", "dentro", "y", "o", "al", "se", "no", "ni", "si", "(si,","(no,","(para,","hasta","cabo","donde",
					"por", "a", "su", "e", "un", "una", "sus", "según", "1.", "2.", "3.", "4.", "5;", "6.", "y/o","esta","este","durante", "está","más","como","así","hace",
					"ha", "han","es","son", "tiene", "tienen", ID, "lo", "cual", "¿cuántos", "¿la" , "¿el","7.", "/","lleva"));

    /**
     * Specific project useless words to not consider when searching keywords for clusters
     */
	private static List<String> specificUselessWords =new ArrayList<String>(
			Arrays.asList("valle", "regional","través","corregimiento","marco","todo", "diferentes", "región", "cada", "años", "nacionales","septiembre",
					"internacional", "espacio", "general","manifestaciones","municipio","fiesta","fiestas","principal","eventos","comunidad", "muestra","nuestra",
					"evento","festividad","festival","encuentro","año","celebración","nacional","medio","actividades"));

    /**
     * Allow to build the nodes map reading the CSV file
     * @param csvFilePath the csv file path
     */
	@SuppressWarnings("deprecation")
	private static void buildNodesMapReadingCSVFile(String csvFilePath, int columnNumberCSVFile, String csvSeparator) {

		try (Stream<String> stream = Files.lines(Paths.get(csvFilePath))) {
			stream.forEach(loadNodesInMapFromFileLines(nodesMap,columnNumberCSVFile,csvSeparator));
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
    private static Consumer<String> loadNodesInMapFromFileLines(Map<Integer, Node> pNodesMap, int columnNumberCSVFile, String csvSeparator) {
		Consumer<String> c = (x) ->{
			String[] splitStr = x.trim().split(CSV_SEPARATOR);
			if(!splitStr[0].equals(ID)) {
				if (splitStr.length==columnNumberCSVFile) {
                    pNodesMap.put(Integer.parseInt(splitStr[0]),new Node(splitStr[0], splitStr[1], splitStr[2],
							splitStr[3], splitStr[4], splitStr[5],
							splitStr[6], splitStr[7], splitStr[8],
							splitStr[9], splitStr[10], splitStr[11]));
				}
			}
		};
		return c;
	}

    /**
     * Allow to build a the keywords-occurrences number map (for word alone)
     * @param csvFilePath the csv file path
     * @param minNumberOccurrence the minimum number occurrence for a word
     * @return the keywords-occurrences number map (for word alone)
     */
	@SuppressWarnings("deprecation")
    private static Map<String, List<Integer>> buildKeywordsOccurrencesNumberMap1Word(String csvFilePath, int minNumberOccurrence) {

		Map<String, List<Integer>> mapWord1 = new ConcurrentHashMap<String, List<Integer>>();

		try (Stream<String> stream = Files.lines(Paths.get(csvFilePath))) {
			stream.forEach(getConsumerWord1(mapWord1));
		}
		catch (IOException e) {
			e.printStackTrace();
			Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
		}
		return readMapResult(mapWord1,minNumberOccurrence);
	}

    /**
     * Allow to filter a map depending on a minimum number for the first number of the value (a list of integer)
     * @param map the map to filter
     * @param min the min number
     * @return the filtered map
     */
	private static Map<String, List<Integer>> filterMapResult(Map<String, List<Integer>> map, int min) {
		int i =0;
		Map<String, List<Integer>> mapNew = new ConcurrentHashMap<String, List<Integer>>();
		List<String> values = new ArrayList<>(map.keySet());
		while(i <map.size()) {
		    //Here we check the value of the first number of the list
			if (map.get(values.get(i)).get(0) >=min){
				mapNew.put(values.get(i) , map.get(values.get(i)));
			}
			i++;
		}
		return mapNew;
	}

    /**
     * Allow to read a map and put it as a one String (after filtering it)
     * @param mapToRead the map to read
     * @param min the min number for filtering (before reading)
     * @return the filtered map
     */
    private static Map<String, List<Integer>> readMapResult(Map<String, List<Integer>> mapToRead, int min) {

        //First filter the map
		Map<String, List<Integer>> map = filterMapResult(mapToRead, min);

		List<String> values = new ArrayList<>(map.keySet());

		Collections.sort(values, new Comparator<String>() {
			public int compare(String a, String b) {
				// no need to worry about nulls as we know a and b are both in map
				return map.get(b).get(0) - map.get(a).get(0);
			}
		});
		String val = "";//"There are "+ map.size()+ " words which repeat themselves in the file";
		//clustersResultText+=val+"\n";
		int i =0;
		while(i <map.size()) {
			val = values.get(i) + CSV_SEPARATOR + map.get(values.get(i))+"\n";
			clustersResultText +=val;
			i++;
		}
		//clustersResultText+= (char)12;
        //TODO WHY return the map ?
		return map;
	}

    /**
     * Allow to build a map with the keywords, number of occurrence and lines of occurrences
     * @param map the map to be writed with the keywords, number of occurrence and lines of occurrences
     * @return the map with the keywords, number of occurrence and lines of occurrences
     */
    private static Consumer<String> getConsumerWord1(Map<String, List<Integer>> map) {
		Consumer<String> c = (x) ->{
			String[] splitStr = x.trim().split("\\s+");
			String lineIdAsString = x.trim().split(CSV_SEPARATOR)[0];
			Integer lineId = 0;
			if(!ID.equals(lineIdAsString)) {
				lineId = Integer.parseInt(lineIdAsString);
			}
			for(int i=0; i< splitStr.length; i++){
				if (!generalUselessWords.contains(splitStr[i].toLowerCase())){
					Integer value=1;
					List<Integer> newL=new ArrayList<Integer>();

					splitStr[i]= splitStr[i].toLowerCase();

					if(splitStr[i].equals("musicales"))
							splitStr[i]="música";
					if(splitStr[i].equals("musicos"))
						splitStr[i]="música";
					if(splitStr[i].equals("musico"))
						splitStr[i]="música";
					if(splitStr[i].equals("cultural"))
						splitStr[i]="cultura";
					if(splitStr[i].equals("culturales"))
						splitStr[i]="cultura";
					if(splitStr[i].equals("danza"))
						splitStr[i]="danzas";
					if(splitStr[i].equals("tradicional"))
						splitStr[i]="tradicionales";
					//if(splitStr[i].equals("tradicionales"))
						//break;
					if(splitStr[i].equals("artistas"))
						splitStr[i]="artístico";
					if(splitStr[i].equals("artista"))
						splitStr[i]="artístico";
					if(splitStr[i].equals("santa"))
						splitStr[i]="san";

					if (!map.containsKey(splitStr[i])){
						newL = new ArrayList<Integer>();
						newL.add(value);
						newL.add(lineId);
						map.put(splitStr[i].toLowerCase(), newL);
					}
					else {
						value = map.get(splitStr[i]).get(0)+1;
						map.get(splitStr[i]).set(0,value);
						if (!map.get(splitStr[i]).contains(lineId)){
							map.get(splitStr[i]).add(lineId);
						}
						map.put(splitStr[i], map.get(splitStr[i].toLowerCase()));
					}


					//	if (map.containsKey(splitStr[i].toLowerCase())){
					//	value = map.get(splitStr[i].toLowerCase())+1;
					//	}
					//	map.put(splitStr[i].toLowerCase(), 7);
				}
			}
		};
		return c;
	}

	/*
	public static Consumer<String> getConsumerWord2(Map<String, List<Integer>> map, Map<String, List<Integer>> map1Word) {
		Consumer<String> c = (x) ->{
			String[] splitStr = x.trim().split("\\s+");
			String lineIdAsString = x.trim().split(CSV_SEPARATOR)[0];
			Integer lineId = 0;
			if(!ID.equals(lineIdAsString)) {
				lineId = Integer.parseInt(lineIdAsString);
			}
			for(int i=0; i< splitStr.length; i++){
				String lowerCase = splitStr[i].toLowerCase();
				if (!generalUselessWords.contains(lowerCase)){

					if(map1Word.containsKey(lowerCase))
					{
						Integer value=1;
						List<Integer> newL;
						if (i+1<splitStr.length){
							if(! generalUselessWords.contains(splitStr[i+1].toLowerCase())){
								String twoWords = splitStr[i].toLowerCase()+ " " +splitStr[i+1].toLowerCase();

								if (!map.containsKey(twoWords)){
									newL = new ArrayList<Integer>();
									newL.add(value);
									newL.add(lineId);
									map.put(twoWords, newL);
								}
								else {
									value = map.get(twoWords).get(0)+1;
									map.get(twoWords).set(0,value);
									//quit repetitions here for simplification
									if (!map.get(twoWords).contains(lineId)){
										map.get(twoWords).add(lineId);
									}
									map.put(twoWords, map.get(twoWords));
								}
							}
						}
					}
				}
			}
		};
		return c;
	}

	public static Consumer<String> getConsumerWord3(Map<String, List<Integer>> map, Map<String, List<Integer>> map1Word) {
		Consumer<String> c = (x) ->{
			String[] splitStr = x.trim().split("\\s+");
			String lineIdAsString = x.trim().split(CSV_SEPARATOR)[0];
			Integer lineId = 0;
			if(!ID.equals(lineIdAsString)) {
				lineId = Integer.parseInt(lineIdAsString);
			}
			for(int i=0; i< splitStr.length; i++){
				String lowerCase = splitStr[i].toLowerCase();
				if (!generalUselessWords.contains(lowerCase)){

					if(map1Word.containsKey(lowerCase))
					{
						Integer value=1;
						List<Integer> newL;

						if (i+2<splitStr.length){
							if(!generalUselessWords.contains(splitStr[i+2].toLowerCase())){
								String threeWords = splitStr[i].toLowerCase()+ " " +splitStr[i+1].toLowerCase()+ " " +splitStr[i+2].toLowerCase();

								if (!map.containsKey(threeWords)){
									newL = new ArrayList<Integer>();
									newL.add(value);
									newL.add(lineId);
									map.put(threeWords, newL);
								}
								else {
									value = map.get(threeWords).get(0)+1;
									map.get(threeWords).set(0,value);
									if (!map.get(threeWords).contains(lineId)){
										map.get(threeWords).add(lineId);
									}
									map.put(threeWords, map.get(threeWords));
								}
							}
						}
					}
				}
			}
		};
		return c;
	}

	public static Consumer<String> getConsumerWord4(Map<String, List<Integer>> map, Map<String, List<Integer>> map1Word) {
		Consumer<String> c = (x) ->{
			String[] splitStr = x.trim().split("\\s+");
			String lineIdAsString = x.trim().split(CSV_SEPARATOR)[0];
			Integer lineId = 0;
			if(!ID.equals(lineIdAsString)) {
				lineId = Integer.parseInt(lineIdAsString);
			}
			for(int i=0; i< splitStr.length; i++){
				String lowerCase = splitStr[i].toLowerCase();
				if (!generalUselessWords.contains(lowerCase)){

					if(map1Word.containsKey(lowerCase))
					{
						Integer value=1;
						List<Integer> newL;
						if (i+3<splitStr.length){
							if(!generalUselessWords.contains(splitStr[i+3].toLowerCase())){
								String threeWords = splitStr[i].toLowerCase()+ " " +splitStr[i+1].toLowerCase()+ " " +splitStr[i+2].toLowerCase()+ " " +splitStr[i+3].toLowerCase();

								if (!map.containsKey(threeWords)){
									newL = new ArrayList<Integer>();
									newL.add(value);
									newL.add(lineId);
									map.put(threeWords, newL);
								}
								else {
									value = map.get(threeWords).get(0)+1;
									map.get(threeWords).set(0,value);
									if (!map.get(threeWords).contains(lineId)){
										map.get(threeWords).add(lineId);
									}
									map.put(threeWords, map.get(threeWords));
								}
							}
						}
					}
				}
			}
		};
		return c;
	}

	@SuppressWarnings("deprecation")
	public static void resultByNum(Map<String, List<Integer>> map, Map<String, List<Integer>> map1Word,  Consumer<String> consumer, int min) {

		String filePath = "./src/file.clustersResultText";

		try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
			stream.forEach(consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
		}

		readMapResult(map, min);
	}
    */

    /**
     * Allow to write a text in a json file
     * @param text the input text
     * @param destJsonFilePath the Json file path
     */
	@SuppressWarnings("deprecation")
	private static void createJsonFile(String text, String destJsonFilePath) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter( destJsonFilePath));
			writer.write( text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
		}
	}

    /**
     * Allow to write lines of nodes in a String, with or without the last character
     * @param nodeLines lines of nodes
     * @param withoutLastCharacter true if with the last character, otherwise false
     */
    private static void groupNodeLines(List<String> nodeLines, boolean withoutLastCharacter) {
		for (String line:nodeLines) {
			jsonResultText +=("\t\t"+line+",\n");
		}
		if(withoutLastCharacter) {
			jsonResultText = jsonResultText.substring(0, jsonResultText.length()-2);
		}
		System.out.println(jsonResultText);
	}

    /**
     * Allow, from a map, to write lines of nodes as a list of string
     * @param mapN the input map
     * @return the lines of nodes as a list of string
     */
	private static List<String> createNodeLinesForJson(Map<Integer, Node> mapN) {

	    int i =0;

		List<String> nodesLines= new ArrayList<String>();

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
     */
	private static List<String> createClusterNodesLines() {
		int i =0;
		List<String> nodesLines= new ArrayList<String>();
		while(i <clusterNames.size()) {
			nodesLines.add("{\"id\": \""+clusterNames.get(i)+"\", \"name\": \""+clusterNames.get(i)+"\", \"group\": \""+clusterNames.get(i).trim()+" \",\"main\": \""+"true\"}");
			i++;
		}
		return nodesLines;
	}

    /**
     * Allow, from a map and an input list of keywords, to write a reversed list TODO EXPLAIN
     * @param mapN the input map
     * @param resultFromMining the input list of keywords
     * @return a reversed list TODO EXPLAIN
     */
	private static List<List<String>> createReverseResultsList(Map<Integer, Node> mapN, String resultFromMining) {

		List<Integer> values = new ArrayList<>(mapN.keySet());
		List<List<String>> reverseResultsList= new ArrayList<List<String>>();

		String[] lines = resultFromMining.trim().split("\n");

		for (String line:lines) {
			String[] lineValues = line.trim().split(CSV_SEPARATOR);
			if (lineValues.length>1){
                lineValues[1]=lineValues[1].substring(1);
                lineValues[lineValues.length-1]=lineValues[lineValues.length-1].substring(0,lineValues[lineValues.length-1].length()-1);
                String name =lineValues[0];
                for (int j = 1; j < lineValues.length; j++) {
                    List<String> smallList= new ArrayList<String>();
                    smallList.add(lineValues[j].trim());
                    //smallList.add(mapN.get(values.get(Integer.parseInt(lineValues[j].trim()))).getId());
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
		return reverseResultsList;
	}

    /**
     *
     * @param element the element to find
     * @param index the index to find
     * @param list the input list
     * @return
     */
	private static List<String> containsElementInListOfList(String element, int index, List<List<String>> list){
		for (int i = 0; i < list.size(); i++) {
			if(element.equals(list.get(i).get(index))){
				return list.get(i);
			}
		}
		return null;
	}

	/**
	 * @param mapN
	 * @param reverseResultsList
	 * @return
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

	private static List<String> createClusterNodesDeepLines(String t) {

		String[] lines = t.trim().split("\n");
		List<String> nodesLines= new ArrayList<String>();

		for (String line:lines) {
			String name = line.trim().split(CSV_SEPARATOR)[0];

			nodesLines.add("{\"id\": \""+name+"\", \"name\": \""+name+"\", \"group\": \""+name.trim()+" \",\"main\": \""+"true\"}");
		}
		return nodesLines;
	}

	private static List<String> createDeepLinkLinesForJson(Map<Integer, Node> mapN, String clusterAndIDlist) {
		//{"source": "Napoleon", "target": "Myriel", "value": 1},
		List<String> linkLines= new ArrayList<String>();

		String[] lines = clusterAndIDlist.trim().split("\n");

		for (String line:lines) {
			String[] lineValues = line.trim().split(CSV_SEPARATOR);
			if(lineValues.length>1) {
                lineValues[1] = lineValues[1].substring(1);
                lineValues[lineValues.length - 1] = lineValues[lineValues.length - 1].substring(0, lineValues[lineValues.length - 1].length() - 1);
                String name = lineValues[0];
                for (int j = 1; j < lineValues.length; j++) {
                    linkLines.add("{\"source\": \"" + Integer.parseInt(lineValues[j].trim()) + "\", \"target\": \"" + name + "\", \"value\": \"" + 1 + "\"}");
                }
            }
		}
		return linkLines;
	}

	private static List<String> createLinkLinesForJson(Map<Integer, Node> mapN, List<String> clusterNames) {
		int i =0;
		//{"source": "Napoleon", "target": "Myriel", "value": 1},
		List<String> linkLines= new ArrayList<String>();

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

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {

		generalUselessWords.addAll(specificUselessWords);
		Map<String, List<Integer>> map1 = buildKeywordsOccurrencesNumberMap1Word(FILE_PATH, 6); //5
		//Map<String, List<Integer>> map2 = new ConcurrentHashMap<String, List<Integer>>();
		//resultByNum(map2, map1,getConsumerWord2(map2,map1), 3); //3

		//Map<String, List<Integer>> map3 = new ConcurrentHashMap<String, List<Integer>>();
		//Map<String, List<Integer>> map4 = new ConcurrentHashMap<String, List<Integer>>();
		//resultByNum(map3, map1,getConsumerWord3(map3,map1), 3);
		//resultByNum(map4, map1,getConsumerWord4(map4,map1), 3);

		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(DEST_FILE_PATH));
			writer.write(clustersResultText);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
		}
		buildNodesMapReadingCSVFile(CSV_PATH, COLUMN_NUMBER_CSV_FILE, CSV_SEPARATOR);
		System.out.println(nodesMap.size());
		int i =0;
		List<Integer> values = new ArrayList<>(nodesMap.keySet());
		while(i < nodesMap.size()) {
			System.out.println(values.get(i));
			i++;
		}
		List<String> nodeLines = createNodeLinesForJson(nodesMap);
		List<String> clusterLines =createClusterNodesLines();
		List<String> linkLines = createLinkLinesForJson(nodesMap, clusterNames);
		groupNodeLines(nodeLines,false);
		groupNodeLines(clusterLines,true);
		jsonResultText +="\n\t],\n\t\"links\": [\n";
		groupNodeLines(linkLines,true);
		jsonResultText +="\n\t]\n}";
		createJsonFile(DEST_JSON_FILE_PATH, jsonResultText);


		jsonResultText ="{\n\t\"nodes\": [\n";

		List<String> nodeLines2 = createDeepNodeLinesForJson(nodesMap, createReverseResultsList(nodesMap, clustersResultText));
		List<String> clusterLines2 =createClusterNodesDeepLines(clustersResultText);
		List<String> deepLinkLines = createDeepLinkLinesForJson(nodesMap, clustersResultText);

		groupNodeLines(nodeLines2,false);
		groupNodeLines(clusterLines2,true);
		jsonResultText +="\n\t],\n\t\"links\": [\n";
		groupNodeLines(deepLinkLines,true);
		jsonResultText +="\n\t]\n}";
		createJsonFile(DEST_JSON_FILE_PATH_2, jsonResultText);
	}
}