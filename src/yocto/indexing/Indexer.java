package yocto.indexing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
        WikiPage doc;
        HashSet<String> docTerms;
        Iterator<WikiPage> iter = docs.iterator();

        // TODO The following is the data structure for that will hold
        // an index by sorting and grouping. If I do the analysis concurrently
        // then I have to make sure the data structures I use are synchronized.
        // Better some concurrent collection.
        // ATTENTION Sorting is guaranteed by using the TreeMap and grouping
        //           by using a TreeSet of postings (we need postings sorted as
        //           well that is why we are using a tree again HAVE TO IMPLEMENT
        //           A COMPARATOR FOR THIS!!!!!
        TreeMap<String, TreeSet<Posting>> invertedIndexSortedGrouped =
                new TreeMap<String, TreeSet<Posting>>();
        while (iter.hasNext()) {

            doc = iter.next();
            iter.remove();

            docTerms = WikiPageAnalyzer.tokenizePageRevisionText(
                    WikiPageAnalyzer.normalizePlainPageRevisionText(doc.getRevisionText()),
                    WikiPageAnalyzer.getInstanceStopwords());

            TreeSet<Posting> termPostings;

            // Iterate through all terms of the current document...
            for (String term : docTerms) {
                //... try to get the postings for the term in the inverted index...
                termPostings = invertedIndexSortedGrouped.get(term);
                if (termPostings == null) {
                    // ...no term appears in the inverted index

                    // prepare a new sorted list of postings
                    TreeSet<Posting> addedPostings = new TreeSet<Posting>();
                    // ... prepare a posting for the current document and add
                    // it to the newly created sorted list of postings
                    addedPostings.add(new Posting(doc.getId()));
                    // and add a term/sorted-list-o-postings pair to the
                    // inverted index.
                    invertedIndexSortedGrouped.put(term, addedPostings);
                }
                else {
                    // the term already appears in the inverted index so simply
                    // add a new posting in the term's sorted list of postings.
                    termPostings.add(new Posting(doc.getId()));
                }


            }


        }

        elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total parsing time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");

        Set<String> terms = invertedIndexSortedGrouped.keySet();

        for (String term : terms) {
            System.out.print(term);
            System.out.print(" ->");
            TreeSet<Posting> postings = invertedIndexSortedGrouped.get(term);
            for (Posting posting : postings) {
                System.out.print(" " + posting.getDocId());
            }
            System.out.println();

        }
    }

}
