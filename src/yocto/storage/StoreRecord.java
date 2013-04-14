package yocto.storage;

/**
 * A class abstracting a store formated record.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class StoreRecord {

    /*
     * The stored information. This should be a list of objects in the future.
     * Currently we only store a single string.
     */
    private final String stored;


    /**
     * Constructor.
     *
     * @param stored
     *     The stored information.
     */
    public StoreRecord(String stored) {
        this.stored = stored;
    }


    /**
     * Gets the stored information.
     *
     * @return
     *     The stored information.
     */
    public String getStored() {
        return stored;
    }

}
