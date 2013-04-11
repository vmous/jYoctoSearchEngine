package yocto.storage;

public class StoreOffsetsRecord {

    private final long docId;
    private final long offset;

    public StoreOffsetsRecord(long docId, long offset) {
        this.docId = docId;
        this.offset = offset;
    }

    /**
     * @return the docId
     */
    public long getDocId() {
        return docId;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

}
