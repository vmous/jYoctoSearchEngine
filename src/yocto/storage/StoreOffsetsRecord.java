package yocto.storage;

/**
 * A class abstracting a store offsets formated record.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class StoreOffsetsRecord {

    /* The document id */
    private final long docId;

    /* The offset of the document's stored fields in the store file. */
    private final long offset;


    /**
     * Constructor.
     *
     * @param docId
     *     The document id.
     * @param offset
     *     The offset.
     */
    public StoreOffsetsRecord(long docId, long offset) {
        this.docId = docId;
        this.offset = offset;
    }


    /**
     * Gets the record's document id.
     *
     * @return
     *     The document id
     */
    public long getDocId() {
        return docId;
    }


    /**
     * Gets the record's offset.
     *
     * @return The offset related to the record's document
     */
    public long getOffset() {
        return offset;
    }

}
