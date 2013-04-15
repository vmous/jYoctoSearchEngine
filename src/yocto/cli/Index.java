package yocto.cli;

import java.util.concurrent.TimeUnit;

import yocto.indexing.parsing.wikipedia.SAXWikipediaXMLDumpParser;

/**
 * A simple testing CLI for testing the indexing infrastructure.
 *
 * @author billy
 */
public class Index {

    /* The version of the crawler. */
    private static final String VERSION =
            "1.0";

    /* The author of the crawler. */
    private static final String strAppAuthor =
            "Vassilis S. Moustakas (vsmoustakas[at]gmail[dot]com)";

    /* The application name. */
    private static final String strAppName =
            "Yocto Search Engine v." + VERSION + " (Indexing Module)";

    /* The application header. */
    private static final String strAppHeader = strAppName
            + "\nA tiny, yet functional, search engine."
            + "\nAuthor: " + strAppAuthor;

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

        System.out.println("");
        System.out.println(strAppHeader);

        System.out.println("Ingesting XML dump file: " + WIKIPEDIA_DUMP_XML + ".");
        System.out.println("This will take a while. Please wait...");
        System.out.println("");
        SAXWikipediaXMLDumpParser parser = new SAXWikipediaXMLDumpParser(WIKIPEDIA_DUMP_XML);
        startTime = System.nanoTime();
        parser.parse();
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("\nTotal excecution time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");
    }

}
