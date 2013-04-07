package yocto.indexing.parsing.wikipedia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * A singleton class offering both class and instance resources for
 * trivial text analysis.
 *
 * @author billy
 */
public class WikiPageAnalyzer {

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
        String[] tokens = normalizedPageRevisionText.split("[^\\w']+");

        for (String token : tokens) {
//            System.out.println(token);

            if (stopwords != null) {
                if (!stopwords.contains(token)) {
                    bagOfWords.add(token);
                }
            }
            else {
                bagOfWords.add(token);
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

        // remove cites
        return rawPageRevisionText.toLowerCase()
                    // Greater than.
                    .replaceAll("&gt;", "")
                    // Less than.
                    .replaceAll("&lt;", "")
                    // Non-braking space.
                    .replaceAll("&nbsp", " ")
                    // Wikipedia murkup (e.g., bold)
                    .replaceAll("\\'+", "")
                    .replaceAll("\\{\\{.*?\\}\\}", "")
                    // URLs
                    .replaceAll("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "")
                    // HTML Tags
                    .replaceAll("(\\<(\\/?[^\\>]+)\\>)", "")
                    .replaceAll("\\[\\[(.*?)\\]\\]", "$1")
                    .replaceAll("<ref>.*?</ref>", " ")
                    .replaceAll("</?.*?>", " ")
                    .replaceAll("\\[\\[.*?:.*?\\]\\]", " ")
//                    .replaceAll("\\s(.*?)\\|(\\w+\\s)", " $2")
                    .replaceAll("\\[.*?\\]", " ")
                    // Blank lines
                    .replaceAll("^\\s*$", "")
                    ;
    }

}
