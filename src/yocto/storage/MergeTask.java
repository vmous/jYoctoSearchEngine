package yocto.storage;

public class MergeTask implements Runnable{

    private final DiskManager dm;
    private final Segment one;
    private final Segment two;


    /**
     * Constructor.
     *
     * @param one
     * @param two
     * @param dm
     */
    public MergeTask(Segment one, Segment two, DiskManager dm) {
        this.one = one;
        this.two = two;
        this.dm = dm;
    }


    /**
     * @return the one
     */
    public Segment getOne() {
        return one;
    }


    /**
     * @return the two
     */
    public Segment getTwo() {
        return two;
    }


    // -- Overwrite


    @Override
    public void run() {
        this.dm.mergeSegmentsOnDisk(one, two);
        one.deleteFiles();
        two.deleteFiles();
    }

}
