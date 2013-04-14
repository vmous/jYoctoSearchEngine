package yocto.indexing;

/**
 * A class encapsulating the basic term-related information.
 *
 * Implements {@code Comparable<T>} in order to enable sets of the class to be
 * preserved into sorted order by document id.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class Posting implements Comparable<Posting> {

    /* The document id number associated with this posting. */
    private final long docId;

    /*
     * The term frequency of the term in the specific document.
     * TODO Currently not used but good to know where it goes...
     */
    private final long tf;


    /**
     * Constructor.
     *
     * @param docId
     *     The document id number with which this posting will be associated.
     */
    public Posting(long docId) {
        this.docId = docId;
        this.tf = 0;
    }


    // -- Getters


    /**
     * Gets the document id with which the posting is associated.
     *
     * @return
     *     The document id.
     */
    public long getDocId() {
        return docId;
    }


    // -- Overriding


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result
                + (int) (docId ^ (docId >>> 32));

        result = prime * result
                + (int) (tf ^ (tf >>> 32));
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        Posting other = (Posting) obj;

        return this.docId == other.docId;
    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Posting other) {
        final int LESS = -1;
        final int EQUAL = 0;
        final int GREATER = 1;

        if (other == null) return GREATER;

        // Enabling sorting by ascending document id.
        if (this.docId > other.docId) return GREATER;
        else if (this.docId < other.docId) return LESS;
        else return EQUAL;
    }

}
