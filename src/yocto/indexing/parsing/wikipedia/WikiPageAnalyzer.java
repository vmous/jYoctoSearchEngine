package yocto.indexing.parsing.wikipedia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A singleton class offering both class and instance resources for
 * trivial text analysis.
 *
 * @author billy
 */
public class WikiPageAnalyzer {
    private static final Pattern WIKI_P0 = Pattern.compile("[=]+", Pattern.DOTALL);
    private static final Pattern WIKI_P1 = Pattern.compile("[\\s]*\\*(.*?)", Pattern.DOTALL);
//    private static final Pattern WIKI_P2 = Pattern.compile("\\[\\[[iI]mage(.*?)(\\|.*?)*\\|(.*?)\\]\\]", Pattern.DOTALL);
//    private static final Pattern WIKI_P3 = Pattern.compile("\\[\\[[fF]ile(.*?)(\\|.*?)*\\|(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P4 = Pattern.compile("\\[\\[category:(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P5 = Pattern.compile("(?s)\\{\\{redirect\\|(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P6 = Pattern.compile("(?s)\\{\\{cite(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P7 = Pattern.compile("(?s)\\{\\|(.*?)\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P8 = Pattern.compile("\\'+", Pattern.DOTALL);
    private static final Pattern WIKI_P9 = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P9_1 = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P9_2 = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P10 = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P11 = Pattern.compile("(?s)\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P12 = Pattern.compile("<math([> ].*?)(</math>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P13 = Pattern.compile("(?s)<ref([> ].*?)(</ref>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P14 = Pattern.compile("(?s)<sup([> ].*?)(</sup>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P15 = Pattern.compile("(?s)<blockquote>(.*?)</blockquote>", Pattern.DOTALL);
    private static final Pattern WIKI_P16 = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern WIKI_P17 = Pattern.compile("&gt;", Pattern.DOTALL);
    private static final Pattern WIKI_P18 = Pattern.compile("&lt;", Pattern.DOTALL);
    private static final Pattern WIKI_P19 = Pattern.compile("&nbsp;", Pattern.DOTALL);
    private static final Pattern WIKI_P20 = Pattern.compile("\\d+", Pattern.DOTALL);
    private static final Pattern WIKI_P21 = Pattern.compile("_+", Pattern.DOTALL);

    /** The singleton instance. */
    private static WikiPageAnalyzer INSTANCE;

    /**
     * The path name to the stopwords file to be loaded. The file format
     * supported is one stopword in each line.
     */
    public static final String STOPWORDS_FILE = "./resources/stopwords.en.txt";

    /**
     * Stopwords are loaded in this instance variable set.
     *
     * TODO Maybe I can consider to make this a static resource!
     */
    private final HashSet<String> stopwords;


    /**
     * Constructor.
     */
    private WikiPageAnalyzer() {
        stopwords = new HashSet<String>();

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(STOPWORDS_FILE));
            String line;
            while((line = br.readLine()) != null) {
                stopwords.add(line);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (br != null) br.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    /**
     * A non-syncrohized getter for {@code WikiPageAnalyzer} singleton
     * instance.
     *
     * TODO If I use multiple threads for parallel analysis I need to
     * review this!
     *
     * @return
     *     The singleton instance of the {@code WikiPageAnalyzer}.
     */
    public static WikiPageAnalyzer getInstance() {

        if (INSTANCE == null) INSTANCE = new WikiPageAnalyzer();

        return INSTANCE;
    }


    /**
     * Gets the stopwords loaded by the singleton instance.
     *
     * @return
     *     A set of stopwords.
     */
    public static HashSet<String> getInstanceStopwords() {
        return getInstance().stopwords;
    }


    /**
     * Tokenizes a string.
     *
     * TODO Needs refinement!
     *
     * @param normalizedPageRevisionText
     *     The string to be tokenized. It is recommended that this string is
     *     first normalized before doing this for better results.
     * @param stopwords
     *     A set of stopwords to be ignored. Pass {@code null} in order not to
     *     ignore any word.
     *
     * @return
     *     A bag of words, as a set (no duplicates) stripped from stopwords.
     */
    public static HashSet<String> tokenizePageRevisionText(
            String normalizedPageRevisionText,
            HashSet<String> stopwords) {
        HashSet<String> bagOfWords = new HashSet<String>();

//        System.out.println(normalizedPageRevisionText);

        // Simple tokenization on non-alphanumeric characters.
        String[] tokens = normalizedPageRevisionText.split("[\\W]+");

        for (String token : tokens) {
//            System.out.println(token);
            if (!token.equals("")) {
                if (stopwords != null) {
                    if (!stopwords.contains(token)) {
                        bagOfWords.add(token);
                    }
                }
                else {
                    bagOfWords.add(token);
                }
            }
        }

        return bagOfWords;
    }


    /**
     * Normalizes a string.
     *
     * TODO Needs refinement!
     *
     * @param rawPageRevisionText
     *     The string to be normalized (most of the time in raw format with
     *     markup - HTML, Wiki e.t.c.).
     *
     * @return
     *     A normalized version of the given string.
     */
    public static String normalizePlainPageRevisionText(String rawPageRevisionText) {

        rawPageRevisionText = rawPageRevisionText.toLowerCase();
        rawPageRevisionText = WIKI_P0.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P1.matcher(rawPageRevisionText).replaceAll("$1");
//        rawPageRevisionText = WIKI_P2.matcher(rawPageRevisionText).replaceAll("$3");
//        rawPageRevisionText = WIKI_P3.matcher(rawPageRevisionText).replaceAll("$3");
        rawPageRevisionText = WIKI_P4.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P5.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P6.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P7.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P8.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P9.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P9_1.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P9_2.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P10.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P11.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P12.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P13.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P14.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P15.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P16.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P17.matcher(rawPageRevisionText).replaceAll(">");
        rawPageRevisionText = WIKI_P18.matcher(rawPageRevisionText).replaceAll("<");
        rawPageRevisionText = WIKI_P19.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P20.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P21.matcher(rawPageRevisionText).replaceAll(" ");

        return rawPageRevisionText;

    }

}
