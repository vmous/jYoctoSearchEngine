package yocto.indexing;

import java.util.concurrent.TimeUnit;

import yocto.indexing.parsing.SAXWikipediaXMLDumpParser;

public class Indexer {

    private static final String WIKIPEDIA_DUMP_XML = "./enwiki-latest-pages-articles.xml";

    public static void main(String args[]) {
        System.out.println("Parsing Wikipedia XML dump file: " + WIKIPEDIA_DUMP_XML);
        SAXWikipediaXMLDumpParser parser = new SAXWikipediaXMLDumpParser(WIKIPEDIA_DUMP_XML);

        long startTime = System.nanoTime();
        parser.parse();
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");
    }

}
