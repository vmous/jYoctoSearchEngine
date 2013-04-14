package yocto.storage;

/**
 * A class abstracting a postings offsets formated record.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class PostingsOffsetsRecord {

    /* The term. */
    private final String term;

    /* The offset of the term's postings in the postings file. */
    private final long offset;


    /**
     * Constructor.
     *
     * @param term
     *     The term.
     * @param offset
     *     The offset.
     */
    public PostingsOffsetsRecord(String term, long offset) {
        this.term = term;
        this.offset = offset;
    }


    /**
     * Gets the record's term.
     *
     * @return
     *     The record's term
     */
    public String getTerm() {
        return term;
    }


    /**
     * Gets the record's offset.
     *
     * @return
     *     The offset related to the record's term.
     */
    public long getOffset() {
        return offset;
    }

}
