package yocto.indexing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import yocto.indexing.parsing.wikipedia.SAXWikipediaXMLDumpParser;
import yocto.indexing.parsing.wikipedia.WikiPage;
import yocto.indexing.parsing.wikipedia.WikiPageAnalyzer;

/**
 * An indexer for large semi-structured datasets.
 *
 * @author billy
 */
public class Indexer {

    /**
     * The main function that drives the execution.
     *
     * @param args
     *     The command-line arguments.
     */
    public static void main(String args[]) {
        final String WIKIPEDIA_DUMP_XML = args[0];
        long startTime;
        long elapsedTime;

        System.out.println("\nExtracting documents from Wikipedia XML dump file: " + WIKIPEDIA_DUMP_XML + "\n");
        SAXWikipediaXMLDumpParser parser = new SAXWikipediaXMLDumpParser(WIKIPEDIA_DUMP_XML);

        startTime = System.nanoTime();
        List<WikiPage> docs = parser.parse();
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total parsing time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");

        System.out.println("\nIndexing Wikipedia documents\n");
        startTime = System.nanoTime();
        String text;
        HashSet<String> bagOfWords;
        Iterator<WikiPage> iter = docs.iterator();
        while (iter.hasNext()) {

            text = new String(iter.next().getRevisionText());
            iter.remove();

            bagOfWords = WikiPageAnalyzer.tokenizePageRevisionText(
                    WikiPageAnalyzer.normalizePlainPageRevisionText(text),
                    WikiPageAnalyzer.getInstanceStopwords());


        }
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total parsing time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");
    }

}
