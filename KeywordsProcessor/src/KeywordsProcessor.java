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
    private final static String CSV_PATH = "./../data/file.csv";
    private final static String DATA_RESULTS_FILE_PATH = "./../data/results.txt";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "ID";

    private static final int DEFAULT_MIN_COLUMN_FOR_KEYWORDS = 0;

    private static final int DEFAULT_MAX_COLUMN_FOR_KEYWORDS = 9;

    private static final int OOEE_MIN_COLUMN_FOR_KEYWORDS = 0;

    private static final int OOEE_MAX_COLUMN_FOR_KEYWORDS = 9;

    private static final String OOEE_TYPE="OE";

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
                    "ha", "han", "es", "son", "tiene", "tienen", "id", "lo", "cual", "¿cuántos", "¿la", "¿el", "7.", "/", "lleva", "mas", "sin", "anio"));

    /**
     * Specific project useless words to not consider when searching keywords for clusters
     */
    private static List<String> specificUselessWords =new ArrayList<>(
            Arrays.asList("ooee", "rraa","informacion","través","registro","reporte","todo", "diferentes", "región", "cada", "estadisticas", "estadistica",
                    "internacional", "espacio", "general","nuestra","nacional","medio","1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                    "13", "14", "15", "16", "17", "18", "19", "20", "25", "30","31","343", "50", "100", "120","180"));

    private static StringBuilder clustersResultText = new StringBuilder();

    /**
     * Allow to find an element and return the second list which contains this element, otherwise null
     * @param element the element to find
     * @param indexInSecondList the index where to find inside the second list of the principal list (for  example, the 4th element of each element of the principal list)
     * @param list the input list
     * @return the second list which contains the element, otherwise null
     */
    private static int containsElementInListOfList(Integer element, int indexInSecondList, List<List<Integer>> list){
        for (int i =0; i<list.size();i++) {
            List<Integer> innerList=list.get(i);
            if(element.equals(innerList.get(indexInSecondList))){
                return i;
            }
        }
        return -1;
    }
    /**
     * Allow to build a map with the keywords, number of occurrence and lines of occurrences
     * @param map the map to be writed with the keywords, number of occurrence and lines of occurrences
     * @return the map with the keywords, number of occurrence and lines of occurrences
     */
    private static Consumer<String> getConsumerWord1(Map<String, List<List<Integer>>> map) {
        return (x) ->{
            //Second part of the line - the type
            String itemTypeAsString = x.trim().split(CSV_SEPARATOR)[1];
            int min=DEFAULT_MIN_COLUMN_FOR_KEYWORDS;
            int max=DEFAULT_MAX_COLUMN_FOR_KEYWORDS;
            if (OOEE_TYPE.equals(itemTypeAsString)){
                min=OOEE_MIN_COLUMN_FOR_KEYWORDS;
                max=OOEE_MAX_COLUMN_FOR_KEYWORDS;
            }
            List<String> splitStrBefore = new ArrayList<>(Arrays.asList(x.trim().split(CSV_SEPARATOR)));
            splitStrBefore=splitStrBefore.subList(min,max);
            StringBuilder lin=new StringBuilder();
            for(String col:splitStrBefore){
                lin.append(col);
                lin.append(CSV_SEPARATOR);
            }
            String li= lin.toString().substring(0, lin.length()-1);
            String[] splitStr = li.trim().split("\\W+");
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
                        //do not quit repetitions here for get the "strength" of the link/the keyword
                        List<Integer> innerListRepeat= new ArrayList<>();
                        innerListRepeat.add(lineId);
                        int index=containsElementInListOfList(lineId,0, map.get(splitStr[i]));
                        if (-1==index){
                            List<Integer> innerList2= new ArrayList<>();
                            innerList2.add(lineId);
                            innerList2.add(1);
                            map.get(splitStr[i]).add(innerList2);
                        }
                        else{
                            List<Integer> innerList2=  map.get(splitStr[i]).get(index);
                            innerList2.set(1,innerList2.get(1)+1);
                            map.get(splitStr[i]).set(index,innerList2);

                        }
                    }
                }
            }
        };
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
            //Second part of the line - the type
            String itemTypeAsString = x.trim().split(CSV_SEPARATOR)[1];
            int min=DEFAULT_MIN_COLUMN_FOR_KEYWORDS;
            int max=DEFAULT_MAX_COLUMN_FOR_KEYWORDS;
            if (OOEE_TYPE.equals(itemTypeAsString)){
                min=OOEE_MIN_COLUMN_FOR_KEYWORDS;
                max=OOEE_MAX_COLUMN_FOR_KEYWORDS;
            }
            List<String> splitStrBefore = new ArrayList<>(Arrays.asList(x.trim().split(CSV_SEPARATOR)));
            splitStrBefore=splitStrBefore.subList(min,max);
            StringBuilder lin=new StringBuilder();
            for(String col:splitStrBefore){
                lin.append(col);
                lin.append(CSV_SEPARATOR);
            }
            String li= lin.toString().substring(0, lin.length()-1);
            String[] splitStr = li.trim().split("\\W+");
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
        readMapAndWriteOnStaticStringVar(map1,8);
        //Map<String, List<List<Integer>>> map2 = new ConcurrentHashMap<>();
        //buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map2,map1,2)); //3
        //readMapAndWriteOnStaticStringVar(map2,6);
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