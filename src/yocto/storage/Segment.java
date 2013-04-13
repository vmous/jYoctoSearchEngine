package yocto.storage;

import java.io.File;

/**
 * Immutable container class for keeping the references to the files,
 * offsets and postings, that make a segment.
 *
 * @author billy
 */
public class Segment {

    private final File offsets;
    private final File postings;


    /**
     * Constructor.
     *
     * @param offsets
     *     The offsets file.
     * @param postings
     *     The postings file.
     */
    public Segment(File offsets, File postings) {
        this.offsets = offsets;
        this.postings = postings;
    }

    /**
     * Getter for the offsets file.
     *
     * @return
     *     The offsets file.
     */
    public File getOffsets() {
        return offsets;
    }

    /**
     * Getter for the postings file.
     *
     * @return
     *     The postings file.
     */
    public File getPostings() {
        return postings;
    }

    /**
     * Deletes the underlying files.
     */
    public void deleteFiles() {
        if (offsets != null)
            offsets.delete();
        if (postings != null)
            postings.delete();
    }

    /**
     * Deletes the underlying files on exit.
     */
    public void deleteFilesOnExit() {
        if (offsets != null)
            offsets.deleteOnExit();
        if (postings != null)
            postings.deleteOnExit();
    }

}
