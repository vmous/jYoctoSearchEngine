package yocto.storage;

import java.util.TreeSet;

import yocto.indexing.Posting;

public class IndexRecord {

    private final int postingsSize;

    private final TreeSet<Posting> postings;

    public IndexRecord(int postingsSize, TreeSet<Posting> postings) {
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
     * @return the postingsSize
     */
    public int getPostingsSize() {
        return postingsSize;
    }

    /**
     * @return the postings
     */
    public TreeSet<Posting> getPostings() {
        return postings;
    }

}
