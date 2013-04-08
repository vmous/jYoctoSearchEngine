package yocto.indexing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import yocto.indexing.parsing.wikipedia.WikiPageAnalyzer;

/**
 * An indexer for large semi-structured datasets.
 *
 * @author billy
 */
public class Indexer {

    /**
     * The in-memory index.
     *
     * It is the data structure that will hold an in-memory instance of the
     * inverted index for the sorting/grouping phase of the indexing process.
     *
     * The terms should be kept in lexicographic order so we use a tree that
     * that implements a dictionary-based interface.
     *
     * It is also a good idea to keep the postings lists in increasing document
     * id order. In the special case that we process a stream of documents that
     * are coming in increasing id order a simple linked-list structure would
     * suffice. For the general case, here, we use a tree-based data structure
     * holding the postings (which we have made comparable on the document id
     * field).
     *
     * TODO If steps that access it are to be threaded, I need to make sure the
     *      that it is thread-safe ({@code TreeMap<K, V> for example is not
     *      synchronized by default). Better some concurrent collection.
     */
    private TreeMap<String, TreeSet<Posting>> index;

    /**
     * The queue of the documents to be indexed.
     */
    private final List<Document> queue;


    /**
     * Constructor.
     */
    public Indexer() {
        queue = new ArrayList<>();
    }


    /**
     * Adds the document to the indexer.
     *
     * This does not mean it is indexed or flushed to disk yet. The indexer
     * will decide according to a policy when the indexing procedure per se
     * will take place.
     *
     * @param doc
     *     The document to be indexed.
     */
    public void addDocument(Document doc) {
        queue.add(doc);
    }


    /**
     * Force the indexing procedure to index and flush to disk the currently
     * queued documents.
     */
    public void forceCommit() {
        index();
    }


    /**
     * Prints {@code <term, postings-list>} tuples from the given index.
     *
     * @param index
     *     The index to print.
     */
    public static void printIndex(TreeMap<String, TreeSet<Posting>> index) {
        Set<String> terms = index.keySet();

        for (String term : terms) {
            System.out.print(term);
            System.out.print(" ->");
            TreeSet<Posting> postings = index.get(term);
            for (Posting posting : postings) {
                System.out.print(" " + posting.getDocId());
            }
            System.out.println();

        }

    }


    /**
     * The indexing process.
     *
     * TODO I have to leverage the composite design pattern for a plugable
     *      analyzer object.
     */
    private void index() {

        // Prepare a new in-memory index to process this indexing block
        index = new TreeMap<String, TreeSet<Posting>>();

        Document doc;
        HashSet<String> docTerms;
        Iterator<Document> iter = queue.iterator();

        while (iter.hasNext()) {

            doc = iter.next();
            iter.remove();

            docTerms = WikiPageAnalyzer.tokenizePageRevisionText(
                    WikiPageAnalyzer.normalizePlainPageRevisionText(doc.getContent()),
                    WikiPageAnalyzer.getInstanceStopwords());

            TreeSet<Posting> termPostings;

            // Iterate through all terms of the current document...
            for (String term : docTerms) {
                //... try to get the postings for the term in the inverted index...
                termPostings = index.get(term);
                if (termPostings == null) {
                    // ...no term appears in the inverted index

                    // prepare a new sorted list of postings
                    TreeSet<Posting> addedPostings = new TreeSet<Posting>();
                    // ... prepare a posting for the current document and add
                    // it to the newly created sorted list of postings
                    addedPostings.add(new Posting(doc.getId()));
                    // and add a term/sorted-list-o-postings pair to the
                    // inverted index.
                    index.put(term, addedPostings);
                }
                else {
                    // the term already appears in the inverted index so simply
                    // add a new posting in the term's sorted list of postings.
                    termPostings.add(new Posting(doc.getId()));
                }

            } // -- foreach term in the document

        } // -- while there are documents

//        printIndex(invertedIndexSortedGrouped);
    }

}
