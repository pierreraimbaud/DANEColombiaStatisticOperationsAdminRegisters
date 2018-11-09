import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class KeywordsProcessor {

    /**
     * Constant for file paths
     */
    private final static String CSV_PATH = "./data/file.csv";
    private final static String DATA_RESULTS_FILE_PATH = "./data/results.txt";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "id";

    /**
     * The separator of the CSV file
     */
    private static final String CSV_SEPARATOR = ",";

    /**
     * Generic useless words to not consider when searching keywords for clusters
     */
    private static List<String> generalUselessWords = new ArrayList<>(
            Arrays.asList("de", "la", "el", ",", ";", "", "en", "del", "los", "las", "demás", "bajo", "les", "hecho", "hecho,", "pertenece", "recibió", "realiza", "realizan",
                    "anualmente", "mes", "para", "con", "que", "-", "–", "sobre", "dentro", "y", "o", "al", "se", "no", "ni", "si", "(si,", "(no,", "(para,", "hasta", "cabo", "donde",
                    "por", "a", "su", "e", "un", "una", "sus", "según", "1.", "2.", "3.", "4.", "5;", "6.", "y/o", "esta", "este", "durante", "está", "más", "como", "así", "hace",
                    "ha", "han", "es", "son", "tiene", "tienen", "id", "lo", "cual", "¿cuántos", "¿la", "¿el", "7.", "/", "lleva"));

    /**
     * Specific project useless words to not consider when searching keywords for clusters
     */
    private static List<String> specificUselessWords =new ArrayList<>(
            Arrays.asList("valle", "regional","través","corregimiento","marco","todo", "diferentes", "región", "cada", "años", "nacionales","septiembre",
                    "internacional", "espacio", "general","manifestaciones","municipio","fiesta","fiestas","principal","eventos","comunidad", "muestra","nuestra",
                    "evento","festividad","festival","encuentro","año","celebración","nacional","medio","actividades"));

    private static StringBuilder clustersResultText = new StringBuilder();

    /**
     * Allow to build a map with the keywords, number of occurrence and lines of occurrences
     * @param map the map to be writed with the keywords, number of occurrence and lines of occurrences
     * @return the map with the keywords, number of occurrence and lines of occurrences
     */
    private static Consumer<String> getConsumerWord1(Map<String, List<List<Integer>>> map) {
        return (x) ->{
            String[] splitStr = x.trim().split("\\W+");
            //First part of the line - the ID
            String lineIdAsString = x.trim().split(CSV_SEPARATOR)[0];
            Integer lineId = 0;
            if(!ID.equals(lineIdAsString)) {
                //Assign line id
                lineId = Integer.parseInt(lineIdAsString);
            }
            //Read the complete line and build a map with keywords
            for(int i=0; i< splitStr.length; i++){
                if (!generalUselessWords.contains(splitStr[i].toLowerCase())){
                    Integer value=1;
                    splitStr[i]= splitStr[i].toLowerCase();

                    if (!map.containsKey(splitStr[i])){

                        List< List<Integer>> newL = new ArrayList<>();

                        List<Integer> innerList= new ArrayList<>();
                        innerList.add(1);
                        innerList.add(-1);
                        newL.add(innerList);
                        List<Integer> innerList2= new ArrayList<>();
                        innerList2.add(lineId);
                        innerList2.add(1);
                        newL.add(innerList2);
                        map.put(splitStr[i].toLowerCase(), newL);
                    }
                    else {


                        List<Integer> innerList= map.get(splitStr[i]).get(0);
                        innerList.set(0,innerList.get(0)+1);
                        map.get(splitStr[i]).set(0,innerList);
                        //quit repetitions here for simplification
                        List<Integer> innerListRepeat= new ArrayList<>();
                        innerListRepeat.add(lineId);
                        if (getSecondElementListWithFirstElementOtherList(map.get(splitStr[i]), innerListRepeat) != -1){
                            List<Integer> innerList2= new ArrayList<>();
                            innerList2.add(lineId);
                            innerList2.add(1);
                            map.get(splitStr[i]).add(innerList2);
                        }
                        else{
                            int index=map.get(splitStr[i]).indexOf(innerListRepeat);
                            List<Integer> innerList2=  map.get(splitStr[i]).get(index);
                            innerList2.set(1,innerList2.get(1)+1);
                            map.get(splitStr[i]).set(index,innerList2);

                        }


                        /*value = map.get(splitStr[i]).get(0)+1;
                        map.get(splitStr[i]).set(0,value);
                        if (!map.get(splitStr[i]).contains(lineId)){
                            map.get(splitStr[i]).add(lineId);
                        }
                        map.put(splitStr[i], map.get(splitStr[i].toLowerCase()));*/
                    }
                }
            }
        };
    }

    public static int getSecondElementListWithFirstElementOtherList(List<Integer> list, List<Integer> listWithElement){
        int element=listWithElement.get(0);
        if (list.contains(element)){
            return list.get(1);
        }
        return -1;
    }
    /**
     * Allow to build a the keywords-occurrences number map (for word alone)
     * @return the keywords-occurrences number map (for word alone)
     */
    @SuppressWarnings("deprecation")
    private static Map<String, List<List<Integer>>> buildKeywordsOccurrencesNumberMap1Word() {

        Map<String, List<List<Integer>>> mapWord1 = new ConcurrentHashMap<>();

        try (Stream<String> stream = Files.lines(Paths.get(CSV_PATH))) {
            stream.forEach(getConsumerWord1(mapWord1));
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
        return mapWord1;
    }

    /**
     * Allow to filter a map depending on a minimum number for the first number of the value (a list of integer)
     * @param map the map to filter
     * @param min the min number
     * @return the filtered map
     */
    private static Map<String, List<List<Integer>>> filterMapResultByMinimumOfOccurrences(Map<String, List<List<Integer>>> map, int min) {
        int i =0;
        Map<String, List<List<Integer>>> mapNew = new ConcurrentHashMap<>();
        List<String> values = new ArrayList<>(map.keySet());
        while(i <map.size()) {
            //Here we check the value of the first number of the list
            if (map.get(values.get(i)).get(0).get(0) >=min){
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
     */
    private static void readMapAndWriteOnStaticStringVar(Map<String, List<List<Integer>>> mapToRead, int min) {

        //First filter the map
        Map<String, List<List<Integer>>> map = filterMapResultByMinimumOfOccurrences(mapToRead, min);

        List<String> values = new ArrayList<>(map.keySet());

        Collections.sort(values, new Comparator<String>() {
            public int compare(String a, String b) {
                // no need to worry about nulls as we know a and b are both in map
                return map.get(b).get(0).get(0) - map.get(a).get(0).get(0);
            }
        });
        String val;//"There are "+ map.size()+ " words which repeat themselves in the file";
        //clustersResultText+=val+"\n";
        int i =0;
        while(i <map.size()) {
            val = values.get(i) + CSV_SEPARATOR + map.get(values.get(i))+"\n";
            clustersResultText.append(val);
            i++;
        }
        //clustersResultText+= (char)12;
    }

    @SuppressWarnings("deprecation")
    private static void buildKeywordsOccurrencesNumberMap2OrMoreWords(Consumer<String> consumer) {

        try (Stream<String> stream = Files.lines(Paths.get(CSV_PATH))) {
            stream.forEach(consumer);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }

    /**
     * Allow to build a map with the keywords (by group of 2 or more), number of occurrence and lines of occurrences
     * @param map the map to be writed with the keywords, number of occurrence and lines of occurrences
     * @return the map with the keywords (by group of 2 or more), number of occurrence and lines of occurrences
     */
    private static Consumer<String> getConsumer2WordsOrMore(Map<String, List<List<Integer>>> map, Map<String, List<List<Integer>>> map1Word, int numberOfWords) {
        return (x) ->{
            String[] splitStr = x.trim().split("\\W+");
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
                        //Integer value=1;
                        List<List<Integer>> newL;
                        if (i+numberOfWords-1<splitStr.length){
                            if(! generalUselessWords.contains(splitStr[i+1].toLowerCase())){
                                String multipleWords="";
                                for (int j=1; j < numberOfWords+1; j++){
                                    multipleWords+=splitStr[j].toLowerCase()+ " ";
                                }
                                multipleWords=multipleWords.substring(0,multipleWords.length()-1);

                                if (!map.containsKey(multipleWords)){
                                    newL = new ArrayList<>();
                                    List<Integer> innerList= new ArrayList<>();
                                    innerList.add(1);
                                    innerList.add(-1);
                                    newL.add(innerList);
                                    List<Integer> innerList2= new ArrayList<>();
                                    innerList2.add(lineId);
                                    innerList2.add(1);
                                    newL.add(innerList2);
                                    map.put(multipleWords, newL);
                                }
                                else {
                                    List<Integer> innerList= map.get(multipleWords).get(0);
                                    innerList.set(0,innerList.get(0)+1);
                                    map.get(multipleWords).set(0,innerList);
                                    //quit repetitions here for simplification
                                    if (!map.get(multipleWords).contains(lineId)){
                                        List<Integer> innerList2= new ArrayList<>();
                                        innerList2.add(lineId);
                                        innerList2.add(1);
                                        map.get(multipleWords).add(innerList2);
                                    }
                                 //   else{

                                  //  }
                                    map.put(multipleWords, map.get(multipleWords));
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    @SuppressWarnings("deprecation")
    public static String getKeywordsListWithOccurrences(){
        //Adding specific excluded words
        generalUselessWords.addAll(specificUselessWords);

        //Build keywords maps (1,2,3,4 words)
        Map<String, List<List<Integer>>> map1 = buildKeywordsOccurrencesNumberMap1Word(); //5
        readMapAndWriteOnStaticStringVar(map1,2);
        Map<String, List<List<Integer>>> map2 = new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map2,map1,2)); //3
        readMapAndWriteOnStaticStringVar(map2,5);
        /*Map<String, List<Integer>> map3 =new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map3,map1,3));
        readMapAndWriteOnStaticStringVar(map3,5);
        Map<String, List<Integer>> map4 =new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map4,map1,4));*/
        //readMapAndWriteOnStaticStringVar(map4,5);
        //The result is stored in a String static var
        BufferedWriter writer;
        try {
            writer = new BufferedWriter( new FileWriter(DATA_RESULTS_FILE_PATH));
            writer.write(clustersResultText.toString());
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
        return clustersResultText.toString();
    }
}