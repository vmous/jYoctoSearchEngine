package yocto.indexing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
    private final TreeMap<String, TreeSet<Posting>> index;

    /** The queue of the documents to be indexed. */
    private final List<Document> queue;

    /** The number of documents indexed **/
    private long docNum;

    /** The number of segments **/
    private int numSegments;


    /**
     * Constructor.
     */
    public Indexer() {
        index = new TreeMap<String, TreeSet<Posting>>();
        docNum = 0;
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

        // TODO Recheck this naive policy which is tightly coupled with
        //      the Wikipedia corpus.
        if (queue.size() >= 45000) {
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
     * Proxy method for inversion process.
     */
    protected void commit() {
        invertSPIMI();
    }


    /**
     * The indexing process.
     *
     * Implements a variation of the Single-Pass In-Mamory Indexing (SPIMI)
     * algorithm.
     *
     * TODO I have to leverage the composite design pattern for a plugable
     *      analyzer object.
     */
    protected void invertSPIMI() {
        System.out.print("Feeding " + queue.size() +" documents... ");

        // Stats
        long startTime = System.nanoTime();
        long numDocsIndexed = 0;
        long numTokensProcessed = 0;

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
                numTokensProcessed++;

            } // -- foreach term in the document
            docNum++;
            numDocsIndexed++;

        } // -- while there are documents

//        printIndex(index);

        writeSegmentToDisk();
        mergeSegmentsOnDisk();

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Done [tokens: " + numTokensProcessed
                + " | docs: " + numDocsIndexed
                + " | time(s): " + TimeUnit.SECONDS.convert(elapsedTime,
                        TimeUnit.NANOSECONDS)
                + "].");
    }


    /**
     * Persisting index to disk.
     */
    private void writeSegmentToDisk() {
        File fSeg = new File("./seg." + numSegments);
        File fOff = new File("./seg.i." + numSegments);

        FileOutputStream fosSeg = null;
        DataOutputStream dosSeg = null;
        FileOutputStream fosOff = null;
        DataOutputStream dosOff = null;

        try {
            fosSeg = new FileOutputStream(fSeg);
            dosSeg = new DataOutputStream(fosSeg);
            fosOff = new FileOutputStream(fOff);
            dosOff = new DataOutputStream(fosOff);

            Set<String> terms = index.keySet();
            TreeSet<Posting> termPostings;
            Posting posting;
            int offset;

            for (String term : terms) {
                // The data concerning the current term will be written
                // to segment file starting from here.
                offset = dosSeg.size();

                // -- Offsets file

                // Save the term literal and offset to the offsets
                // file for enabling random access dictionary to the segment
                // file
                dosOff.writeUTF(term);
                dosOff.writeInt(offset);
//                System.out.println("Offset: " + offset);

                // -- Segment file

                termPostings = index.get(term);
                Iterator<Posting> iter = termPostings.iterator();
                // Write the size of the postings list, so that the
                // reader can iteratively pick up the correct number of
                // postings.
                dosSeg.writeInt(termPostings.size());
                // Iterate through the term's postings and...
                while (iter.hasNext()) {
                    posting = iter.next();
                    // ...write the document id
                    dosSeg.writeLong(posting.getDocId());
//                    dosSegment.writeInt((new Long(posting.getDocId()).intValue()));
                } // -- while postings
            } // -- for all terms

            numSegments++;

            // clear the in-memory index since persisted on disk.
            index.clear();
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (dosSeg != null) {
                    dosSeg.flush();
                    dosSeg.close();
                }
                if (fosSeg != null){
                    fosSeg.flush();
                    fosSeg.close();
                }
                if (dosOff != null){
                    dosOff.flush();
                    dosOff.close();
                }
                if (fosOff != null){
                    fosOff.flush();
                    fosOff.close();
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }


    /**
     * Merge all segments to one humongous index
     *
     * TODO Currently only reads and prints (TESTING!).
     */
    private static void mergeSegmentsOnDisk() {

        File fSeg = new File("./seg." + 0);
        File fOff = new File("./seg.i." + 0);

        FileInputStream fisSeg = null;
        DataInputStream disSeg = null;
        FileInputStream fisOff = null;
        DataInputStream disOff = null;

        TreeMap<String, TreeSet<Posting>> foo = null;
        try {
            fisSeg = new FileInputStream(fSeg);
            disSeg = new DataInputStream(fisSeg);
            fisOff = new FileInputStream(fOff);
            disOff = new DataInputStream(fisOff);

            foo = new TreeMap<String, TreeSet<Posting>>();

            String term;
            int offset;
            while (true) {
                term = disOff.readUTF();
                offset = disOff.readInt();
//                System.out.println("term: " + term
//                        + " offset: " + offset);

                // Retrieve the number of postings...
                int postingsListSize = disSeg.readInt();
                TreeSet<Posting> termPostings =
                        new TreeSet<Posting>();
                // ...and iterate through the appropriate number of bytes...
                for (int i = 0; i < postingsListSize; i++) {
                    // ...to fetch the needed data
                    long docId = disSeg.readLong();
                    // ...and add a new posting in the term's list
                    termPostings.add(new Posting(docId));
                }

                // add the new term to an in-memory index for printing.
                foo.put(term, termPostings);
            } // -- while not EOF
        }
        catch (EOFException eofe) {
            // Done reading segment or offset file.
//            if (foo != null) printIndex(foo);
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (disSeg != null) disSeg.close();
                if (fisSeg != null) fisSeg.close();
                if (disOff != null) disOff.close();
                if (fisOff != null) fisOff.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
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

}
