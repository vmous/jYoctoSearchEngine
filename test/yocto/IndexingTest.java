package yocto;

import java.util.concurrent.TimeUnit;

import yocto.indexing.parsing.wikipedia.SAXWikipediaXMLDumpParser;

/**
 * A simple testing CLI for testing the indexing infrastructure.
 *
 * @author billy
 */
public class IndexingTest {

    /**
     * The main function that drives the execution.
     *
     * @param args
     *     The command-line arguments.
     */
    public static void main(String[] args) {
        final String WIKIPEDIA_DUMP_XML = args[0];
        long startTime;
        long elapsedTime;

        System.out.println("Extracting and indexing documents from Wikipedia XML dump file: " + WIKIPEDIA_DUMP_XML + "\n");
        SAXWikipediaXMLDumpParser parser = new SAXWikipediaXMLDumpParser(WIKIPEDIA_DUMP_XML);
        startTime = System.nanoTime();
        parser.parse();
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("\nTotal excecution time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");
    }

}
