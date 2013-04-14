package yocto.storage;

import java.util.TreeSet;

import yocto.indexing.Posting;

/**
 * A class abstracting a postings formated record.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class PostingsRecord {

    /* The number of postings the record contains. */
    private final int postingsSize;

    /* The record's postings, sorted */
    private final TreeSet<Posting> postings;


    /**
     * Constructor.
     *
     * @param postingsSize
     *     The number of postings.
     * @param postings
     *     The postings.
     */
    public PostingsRecord(int postingsSize, TreeSet<Posting> postings) {
        if (postings == null) {
            this.postingsSize = 0;
            this.postings = new TreeSet<Posting>();
        }
        else {
            this.postings = postings;
            this.postingsSize = postings.size();
        }
    }


    /**
     * Gets the number of posting in the record.
     *
     * @return The number of postings.
     */
    public int getPostingsSize() {
        return postingsSize;
    }


    /**
     * Gets the record's postings.
     *
     * @return The postings.
     */
    public TreeSet<Posting> getPostings() {
        return postings;
    }

}
