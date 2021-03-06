package yocto.indexing;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import yocto.indexing.parsing.wikipedia.WikiPageAnalyzer;
import yocto.storage.DiskManager;

/**
 * An indexer for large semi-structured datasets.
 *
 * @author billy
 */
public class Indexer {

    /*
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
     * that it is thread-safe ({@code TreeMap<K, V> for example is not
     * synchronized by default). Better some concurrent collection.
     */
    private final TreeMap<String, TreeSet<Posting>> index;

    /*
     * The in-memory stored fields.
     *
     * A data structure that maps document ids with a stored string field (in the
     * future maybe a collection of stored fields). The map is linked so that we
     * can preserve the insertion  sequence since we are manipulating documents in
     * an ascending order of document id number.
     */
    private final LinkedHashMap<Long, String> store;

    /*
     * Handles the persistence functions
     */
    private final DiskManager dm;

    /* The queue of the documents to be indexed. */
    private final Queue<Document> documents;

    /*
     * Defines the number of documents each inversion will handle.
     *
     * TODO Revisit this batch size approach. Might be better the batch size
     * to be dynamically chosen with respect to the available memory.
     */
    private final int batchSize;

    /* The number of ingested batches. */
    private final AtomicInteger batchNum;


    /**
     * Constructor.
     *
     * @param dm
     *     The disk manager associated with this indexer.
     * @param batchSize
     *     The threshold in the number of documents added to the indexer that
     *     triggers the actual indexing. Should be carefully chosen taking into
     *     account the memory available to the indexer (JVM + actual physical).
     */
    public Indexer(DiskManager dm, int batchSize) {
        this.index = new TreeMap<String, TreeSet<Posting>>();
        this.store = new LinkedHashMap<Long, String>();
        this.dm = dm;
        this.documents = new LinkedList<Document>();
        this.batchSize = batchSize;
        this.batchNum = new AtomicInteger();
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
        documents.offer(doc);

        if (documents.size() >= batchSize) {
            forceCommit();
        }
    }


    /**
     * Force the indexing procedure to index and flush to disk the currently
     * queued documents.
     */
    public void forceCommit() {
        commit();
    }


    /**
     * Closes the indexer.
     *
     * This commits any documents remaining in the queue and makes sure the
     * final, merged index is properly written to disk.
     */
    public void close() {
        commit();

        dm.close();
    }


    /**
     * Proxy method for inversion process.
     */
    protected void commit() {
        invertSPIMI();
    }


    /*
     * Attempts to flush the in-memory structures to persistence storage.
     */
    private void flush(TreeMap<String, TreeSet<Posting>> index,
            LinkedHashMap<Long, String> store) {
        dm.writeIndexSegment(index);
        index.clear();
        dm.appendStore(store);
        store.clear();
    }


    /*
     * The indexing process.
     *
     * Implements a variation of the Single-Pass In-Mamory Indexing (SPIMI)
     * algorithm.
     *
     * TODO Could be a good idea to leverage the composite design pattern for
     * a plugable analyzer object.
     */
    protected void invertSPIMI() {

        // Stats
        int batchId = batchNum.incrementAndGet();
        long startTime = System.nanoTime();
        long numDocsIndexed = 0;
        long numTokensProcessed = 0;

        Document doc;
        HashSet<String> docTerms;

        // Iterate through all available documents.
        while ( (doc = documents.poll()) != null ) {

            // -- Keep the stored fields.

            store.put(doc.getId(), doc.getLabel().trim());

            // -- Invert document.

            docTerms = WikiPageAnalyzer.tokenizePageRevisionText(
                    WikiPageAnalyzer.normalizePlainPageRevisionText(doc.getContent()),
                    WikiPageAnalyzer.getStopwords());


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
                numTokensProcessed++;

            } // -- foreach term in the document

            // -- Naive approach for inverting author names due to lack of time.
            String author = "author:" + doc.getAuthor().trim().toLowerCase();
            TreeSet<Posting> authorPostings = index.get(author);
            if (authorPostings == null) {
                TreeSet<Posting> addedAuthorPostings = new TreeSet<Posting>();
                addedAuthorPostings.add(new Posting(doc.getId()));
                // add the new author/sorted-list-o-postings pair to the
                // inverted index.
                index.put(author, addedAuthorPostings);
            }
            else {
                // the author already appears in the inverted index so simply
                // add a new posting in the term's sorted list of postings.
                authorPostings.add(new Posting(doc.getId()));
            }

            numDocsIndexed++;

        } // -- while there are documents

//        printIndex(index);
//        printStore(store);

        // Persist to disk
        flush(index, store);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Batch ingestion complete [id: " + batchId
                + " | docs: " + numDocsIndexed
                + " | tokens: " + numTokensProcessed
                + " | time(s): " + TimeUnit.SECONDS.convert(elapsedTime,
                        TimeUnit.NANOSECONDS)
                + "].");
    }


    /**
     * Prints {@code <term, postings-list>} tuples from the given in-memory index.
     *
     * @param index
     *     The in-memory index to print.
     */
    public static void printIndex(TreeMap<String, TreeSet<Posting>> index) {
        Set<String> terms = index.keySet();

        for (String term : terms) {
            System.out.println("");
            System.out.print(term + " => ");
            TreeSet<Posting> postings = index.get(term);
            for (Posting posting : postings) {
                System.out.print(" " + posting.getDocId());
            }
        }

    }


    /**
     * Prints {@code <doc_id, stored_field(s)>} tuples from the given in-memory store.
     *
     * @param store
     *     The in-memory store to print.
     */
    public static void printStore(LinkedHashMap<Long, String> store) {
        for (Map.Entry<Long,String> entry : store.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
    }

}
