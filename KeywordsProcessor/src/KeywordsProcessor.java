import java.io.*;
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
    private final static String CSV_BAR_CHART_PATH = "./../data/data1.csv";
    private final static String CSV_BAR_CHART2_PATH = "./../data/data2.csv";

    private final static String USELESS_WORDS_FILE_PATH = "./../data/uselessWords.txt";
    private final static String EXCLUDED_WORDS_FILE_PATH = "./../data/excludedWords.txt";

    /**
     * The string of the id column in the CSV file
     */
    private static final String ID = "ID";

    private static final int DEFAULT_MIN_COLUMN_FOR_KEYWORDS = 2;

    private static final int DEFAULT_MAX_COLUMN_FOR_KEYWORDS = 10;

    private static final int OOEE_MIN_COLUMN_FOR_KEYWORDS = 2;

    private static final int OOEE_MAX_COLUMN_FOR_KEYWORDS = 10;

    private static final String OOEE_TYPE="OOEE";


    /**
     * The separator of the CSV file
     */
    private static final String CSV_SEPARATOR = ",";
    public static final int INDEX_FOR_OLD_CATEGORY = 17;
    public static final int INDEX_FOR_TYPE = 1;

    private static final String CSV_COLUMN_NAME_FOR_OLD_CATEGORY = "G. TEMA 2. Tema al que pertenece el registro administrativo   (Este campo sera diligenciado por el funcionario del DANE)";
    /**
     * Generic useless words to not consider when searching keywords for clusters
     */
    private static List<String> wordAlwaysExcluded;
    /**
     * Specific project useless words to not consider when searching keywords for clusters
     */
    private static List<String> wordsExcludedOnlyAtFirstPosition;

    private static StringBuilder clustersResultText = new StringBuilder();

    public static void buildListNotAcceptedKeywords() {
        wordAlwaysExcluded = buildListNotIncludedKeywords(USELESS_WORDS_FILE_PATH);
        wordsExcludedOnlyAtFirstPosition = buildListNotIncludedKeywords(EXCLUDED_WORDS_FILE_PATH);
    }

    private static List buildListNotIncludedKeywords(String filePath){
        Scanner s = null;
        List list = new ArrayList();
        try {
            s = new Scanner(new File(filePath));
            list = new ArrayList<String>();
            while (s.hasNext()){
                list.add(s.next());
            }
            s.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
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
            String word;
            //Read the complete line and build a map with keywords
            List allUselessWords = wordAlwaysExcluded.subList(0, wordAlwaysExcluded.size());//clone the list
            allUselessWords.addAll(wordsExcludedOnlyAtFirstPosition);
            for(int i=0; i< splitStr.length; i++){
                if (!allUselessWords.contains(splitStr[i].toLowerCase())){
                    word= splitStr[i].toLowerCase();
                    //quit plural on words (spanish => s or es
                    if (word.length()>=2){
                        String end1 = word.substring(word.length()-1);
                        String end2 = word.substring(word.length()-2);
                        if ("es".equals(end2)&& (!("interes".equals(word)||"bienes".equals(word)))){
                            word=word.substring(0,word.length()-2);
                        }
                        else if ("s".equals(end1) && !("interes".equals(word)||"bienes".equals(word) ||"pais".equals(word))){
                            word=word.substring(0,word.length()-1);
                        }
                    }

                    if (!map.containsKey(word)){

                        List< List<Integer>> newL = new ArrayList<>();

                        List<Integer> innerList= new ArrayList<>();
                        innerList.add(1);
                        innerList.add(-1);
                        newL.add(innerList);
                        List<Integer> innerList2= new ArrayList<>();
                        innerList2.add(lineId);
                        innerList2.add(1);
                        newL.add(innerList2);
                        map.put(word.toLowerCase(), newL);
                    }
                    else {
                        List<Integer> innerList= map.get(word).get(0);
                        innerList.set(0,innerList.get(0)+1);
                        map.get(word).set(0,innerList);
                        //do not quit repetitions here for get the "strength" of the link/the keyword
                        List<Integer> innerListRepeat= new ArrayList<>();
                        innerListRepeat.add(lineId);
                        int index=containsElementInListOfList(lineId,0, map.get(word));
                        if (-1==index){
                            List<Integer> innerList2= new ArrayList<>();
                            innerList2.add(lineId);
                            innerList2.add(1);
                            map.get(word).add(innerList2);
                        }
                        else{
                            List<Integer> innerList2=  map.get(word).get(index);
                            innerList2.set(1,innerList2.get(1)+1);
                            map.get(word).set(index,innerList2);

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
     * @param map the map to read
     */
    private static void readMapAndWriteOnStaticStringVar(Map<String, List<List<Integer>>> map) {

        //First filter the map
        //Map<String, List<List<Integer>>> map = filterMapResultByMinimumOfOccurrences(mapToRead, min);

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
    private static Consumer<String> getConsumer2WordsOrMore(Map<String, List<List<Integer>>> map, int numberOfWords) {
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
                if (!wordAlwaysExcluded.contains(lowerCase)){

                   // if(map1Word.containsKey(lowerCase))
                    //{
                        //Integer value=1;
                        List<List<Integer>> newL;
                        if (i+numberOfWords-1<splitStr.length){
                            if(! wordAlwaysExcluded.contains(splitStr[i+numberOfWords-1].toLowerCase())){
                                String multipleWords="";
                                for (int j=i; j < numberOfWords+i; j++){
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
           // }
        };
    }

    public static void writeOldCategoryFile(){
        Map<String, List<Integer>> map = new ConcurrentHashMap<>();

        try (Stream<String> stream = Files.lines(Paths.get(CSV_PATH))) {
            stream.forEach(getConsumerOldCategories(map));
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }

        List<String> values = new ArrayList<>(map.keySet());

        Collections.sort(values, new Comparator<String>() {
            public int compare(String a, String b) {
                // no need to worry about nulls as we know a and b are both in map
                return map.get(b).get(0)- map.get(a).get(0);
            }
        });
        values.size();

        StringBuilder categorical2Results=new StringBuilder();
        categorical2Results.append("oldCategory");
        categorical2Results.append(CSV_SEPARATOR);
        categorical2Results.append("occurrencesRRAA");
        categorical2Results.append(CSV_SEPARATOR);
        categorical2Results.append("occurrencesOOEE");
        categorical2Results.append("\n");
        Scanner s;
        try {
            s = new Scanner(new File(DATA_RESULTS_FILE_PATH));
            for (String line : values){
                categorical2Results.append(line.toLowerCase());
                categorical2Results.append(CSV_SEPARATOR);
                categorical2Results.append(map.get(line).get(0));
                categorical2Results.append(CSV_SEPARATOR);
                categorical2Results.append(map.get(line).get(1));
                categorical2Results.append("\n");
            }
            s.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedWriter writerCSV2;
        try {
            writerCSV2 = new BufferedWriter( new FileWriter(CSV_BAR_CHART_PATH));

            writerCSV2.write(categorical2Results.toString());
            writerCSV2.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }

    private static Consumer<String> getConsumerOldCategories(Map<String, List<Integer>> map) {
        return (x) ->{
            List<String> splitStrBefore = new ArrayList<>(Arrays.asList(x.trim().split(CSV_SEPARATOR)));
            String val=splitStrBefore.get(INDEX_FOR_OLD_CATEGORY);
            if(!CSV_COLUMN_NAME_FOR_OLD_CATEGORY.equals(val)){
                String tipo=splitStrBefore.get(INDEX_FOR_TYPE);
                if (!map.containsKey(val)) {

                    if(!tipo.equals(OOEE_TYPE)){
                        map.put(val, new ArrayList<>(Arrays.asList(1,0)));
                    }
                    else{
                        map.put(val, new ArrayList<>(Arrays.asList(0,1)));
                    }

                }
                else{
                    List<Integer> oldList = map.get(val);
                    if(!tipo.equals(OOEE_TYPE)){
                        map.put(val, new ArrayList<>(Arrays.asList(oldList.get(0)+1,oldList.get(1))));
                    }
                    else{
                        map.put(val, new ArrayList<>(Arrays.asList(oldList.get(0),oldList.get(1)+1)));
                    }
                }
            }
        };
    }

    private static StringBuilder categorical2Results(Map<String, List<List<Integer>>> map) {


        List<String> values = new ArrayList<>(map.keySet());

       /* Collections.sort(values, new Comparator<String>() {
            public int compare(String a, String b) {
                // no need to worry about nulls as we know a and b are both in map
                return map.get(b).get(0).get(0) - map.get(a).get(0).get(0);
            }
        });*/

        StringBuilder categorical2Results =new StringBuilder();
        categorical2Results.append("newCategory");
        categorical2Results.append(CSV_SEPARATOR);
        categorical2Results.append("occurrencesRRAA");
        categorical2Results.append(CSV_SEPARATOR);
        categorical2Results.append("occurrencesOOEE");
        categorical2Results.append("\n");

        String category;
        List<String> lines;
        try  {
            lines = Files.readAllLines(Paths.get(CSV_PATH));
            int i =0;
            while(i <map.size()) {
                category = values.get(i);
                List<List<Integer>> listNodes = map.get(values.get(i)).subList(1,map.get(values.get(i)).size());
                int j=0;
                int totalRRAA=0;
                int totalOOEE=0;
                String type="";

                while(j <listNodes.size()) {
                    int indexLine = listNodes.get(j).get(0);
                    String line = lines.get(indexLine);
                    type =line.trim().split(CSV_SEPARATOR)[1];
                    if(OOEE_TYPE.equals(type)){
                        totalOOEE++;
                    }
                    else{
                        totalRRAA++;
                    }
                    j++;
                }
                categorical2Results.append(category);
                categorical2Results.append(CSV_SEPARATOR);
                categorical2Results.append(totalRRAA);
                categorical2Results.append(CSV_SEPARATOR);
                categorical2Results.append(totalOOEE);
                categorical2Results.append("\n");

                i++;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return categorical2Results;
    }
    @SuppressWarnings("deprecation")
    public static String getKeywordsListWithOccurrences(){
        //Adding specific excluded words
        //wordAlwaysExcluded.addAll(wordsExcludedOnlyAtFirstPosition);

        //Build keywords maps (1,2,3,4 words)
        Map<String, List<List<Integer>>> map1 = buildKeywordsOccurrencesNumberMap1Word(); //5
        Map<String, List<List<Integer>>> mapFiltered =filterMapResultByMinimumOfOccurrences(map1, 150);
        readMapAndWriteOnStaticStringVar(mapFiltered);

        /*Map<String, List<List<Integer>>> map2 = new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map2,2)); //3
        readMapAndWriteOnStaticStringVar(map2,70);
        Map<String, List<List<Integer>>> map3 =new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map3,3));
        readMapAndWriteOnStaticStringVar(map3,70);
        Map<String, List<List<Integer>>> map4 =new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map4,4));
        readMapAndWriteOnStaticStringVar(map4,50);
        Map<String, List<List<Integer>>> map5 =new ConcurrentHashMap<>();
        buildKeywordsOccurrencesNumberMap2OrMoreWords(getConsumer2WordsOrMore(map5,5));
        readMapAndWriteOnStaticStringVar(map5,50);*/
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


        StringBuilder categorical2Results=categorical2Results(mapFiltered);

        BufferedWriter writerCSV2;
        try {
            writerCSV2 = new BufferedWriter( new FileWriter(CSV_BAR_CHART2_PATH));

            writerCSV2.write(categorical2Results.toString());
            writerCSV2.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.global.log(Level.SEVERE, e.getMessage(), e.getCause());
        }

        return clustersResultText.toString();
    }
}