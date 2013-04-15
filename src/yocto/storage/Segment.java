package yocto.storage;

import java.io.File;

/**
 * Immutable container class for keeping the references to the files,
 * offsets and postings, that make a segment.
 *
 * Implements {@code Comparable<T>} in order to enable priority queues
 * to favour smaller segments to be merged first to maximize I/0 efficiency.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class Segment implements Comparable<Segment> {

    /* The postings offsets file. */
    private final File offsets;

    /* The postings file. */
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


    /**
     * Gets the segment size.
     *
     * @return
     *     The sum of the sizes in bytes of the postings offsets file
     *     and the postings files length constituting the segment.
     */
    public float getSegmentSize() {
        return (offsets.length() + postings.length());
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
                + ( (offsets == null) ? 0 : offsets.hashCode() );

        result = prime * result
                + ( (postings == null) ? 0 : postings.hashCode() );

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

        Segment other = (Segment) obj;

        return ( (this.offsets == other.offsets) || (this.offsets != null && this.offsets.equals(other.offsets)) )
                && ( (this.postings == other.postings) || (this.postings != null && this.postings.equals(other.postings)) );

    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Segment other) {
        final int LESS = -1;
        final int EQUAL = 0;
        final int GREATER = 1;

        if (other == null) return GREATER;

        // Maybe the following looks weird but what we want to achieve is
        // segments with smaller size to have higher priority.
        if ( (this.offsets.length() + this.postings.length()) > (other.offsets.length() + other.postings.length()) )
            return LESS;
        else if ( (this.offsets.length() + this.postings.length()) < (other.offsets.length() + other.postings.length()) )
            return GREATER;
        else return EQUAL;
    }

}
