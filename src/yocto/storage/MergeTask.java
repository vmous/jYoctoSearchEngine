package yocto.storage;

/**
 * A runnable task for merging two segments in a different thread.
 *
 * @author billy
 */
public class MergeTask implements Runnable{

    /* The first segment to merge. */
    private final Segment one;

    /* The second segment to merge. */
    private final Segment two;

    /* The parent of this task */
    private final DiskManager dm;


    /**
     * Constructor.
     *
     * @param one
     *     The first segment to merge.
     * @param two
     *     The second segment to merge.
     * @param dm
     *     The parent of this task.
     */
    public MergeTask(Segment one, Segment two, DiskManager dm) {
        this.one = one;
        this.two = two;
        this.dm = dm;
    }


    // -- Getters

    /**
     * Gets the first segment associated with this merge task.
     *
     * @return The first segment.
     */
    public Segment getOne() {
        return one;
    }


    /**
     * Gets the second segment associated with this merge task.
     *
     * @return The second segment.
     */
    public Segment getTwo() {
        return two;
    }


    // -- Overwrite


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        this.dm.mergeSegmentsOnDisk(one, two);
        one.deleteFiles();
        two.deleteFiles();
    }

}
