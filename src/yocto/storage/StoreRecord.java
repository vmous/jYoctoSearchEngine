package yocto.storage;

public class StoreRecord {
    private final String stored;

    public StoreRecord(String stored) {
        this.stored = stored;
    }

    /**
     * @return the stored
     */
    public String getStored() {
        return stored;
    }
}
