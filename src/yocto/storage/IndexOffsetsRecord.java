package yocto.storage;

public class IndexOffsetsRecord {

    private final String term;
    private final long offset;

    public IndexOffsetsRecord(String term, long offset) {
        this.term = term;
        this.offset = offset;
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

}
