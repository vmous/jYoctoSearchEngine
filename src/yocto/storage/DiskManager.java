package yocto.storage;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import yocto.indexing.Posting;

/**
 *
 *
 * @author billy
 */
public class DiskManager {
//    public static final String INDEX_FILENAME = "indx";
//    public static final String INDEX_OFFSETS_FILENAME = "indx.off";
    public static final String SEGMENT_FILENAME = "_seg.";
    public static final String SEGMENT_OFFSETS_FILENAME = "_seg.off.";
    public static final String STORE_FILENAME = "stor";
    public static final String STORE_OFFSETS_FILENAME = "stor.off";

//    private final String pathnameIndex;
//    private final String pathnameIndexOffsets;
    private final String pathnameSegment;
    private final String pathnameSegmentOffsets;
    private final String pathnameStore;
    private final String pathnameStoreOffsets;

//    private DataOutputStream dosIndex;
//    private DataOutputStream dosIndexOffsets;

    /**The number of processed segments*/
    private int numSegments;

    /**
     * Constructor.
     *
     * @param dir
     *     The directory
     */
    public DiskManager(String dir) {
//        this.pathnameIndex =
//                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + INDEX_FILENAME;
//        this.pathnameIndexOffsets =
//                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + INDEX_OFFSETS_FILENAME;
        this.pathnameStore =
                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + STORE_FILENAME;
        this.pathnameStoreOffsets =
                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + STORE_OFFSETS_FILENAME;
        this.pathnameSegment =
                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + SEGMENT_FILENAME;
        this.pathnameSegmentOffsets =
                ((dir == null || dir.trim().equals("")) ? "" : dir + File.separator) + SEGMENT_OFFSETS_FILENAME;

        this.numSegments = 0;
    }


    /**
     * Persisting index to disk.
     */
    public void writeIndexSegment(TreeMap<String, TreeSet<Posting>> index) {
        DataOutputStream dosSegment = null;
        DataOutputStream dosSegmentOffsets = null;

        try {

            dosSegment = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(pathnameSegment + numSegments),
                            32 * 1024));
            dosSegmentOffsets = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(pathnameSegmentOffsets + numSegments),
                            32 * 1024));
            numSegments++;

            Set<String> terms = index.keySet();
            TreeSet<Posting> termPostings;
            Posting posting;
            int offset;

            for (String term : terms) {
                // The data concerning the current term will be written
                // to segment file starting from here.
                offset = dosSegment.size();

                // -- Segment Offsets file

                // Save the term literal and offset to the offsets
                // file for enabling random access dictionary to the segment
                // file
                dosSegmentOffsets.writeUTF(term);
                dosSegmentOffsets.writeInt(offset);

                // -- Segment file

                termPostings = index.get(term);
                Iterator<Posting> iter = termPostings.iterator();
                // Write the size of the postings list, so that the
                // reader can iteratively pick up the correct number of
                // postings.
                dosSegment.writeInt(termPostings.size());
                // Iterate through the term's postings and...
                while (iter.hasNext()) {
                    posting = iter.next();
                    // ...write the document id
                    dosSegment.writeLong(posting.getDocId());
//                    dosSegment.writeInt((new Long(posting.getDocId()).intValue()));
                } // -- while postings
            } // -- for all terms

            // clear the in-memory index since persisted on disk.
            // TODO maybe this is a POB if multi-threaded.
//            index.clear();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (dosSegment != null) {
                    dosSegment.close();
                }
                if (dosSegmentOffsets != null) {
                    dosSegmentOffsets.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }


    /**
     * Appends fields to store file.
     *
     * The store file is not fragmented and merged because we assume that
     * the documents are assigned a monotonically increasing id number.
     */
    public void appendStore(LinkedHashMap<Long, String> store) {
        DataOutputStream dosStore = null;
        DataOutputStream dosStoreOffsets = null;

        try {
            dosStore = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(pathnameStore, true),
                            32 * 1024));
            dosStoreOffsets = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(pathnameStoreOffsets, true),
                            32 * 1024));

            int offset;
            for (Map.Entry<Long,String> entry : store.entrySet()) {
                // The stored field(s) concerning the current document will be
                // written to store file starting from here.
                offset = dosStore.size();

                // -- Store Offsets file

                // Save the document id and offset in the store offset file.
                // This will enable random access dictionary to the segment
                // file.
                dosStoreOffsets.writeLong(entry.getKey());
                dosStoreOffsets.writeInt(offset);

                // -- Store file

                // Write stored field(s) to the store file.
                // TODO Currently we have only one stored field. If we ever
                // have multiple, then we can do similarly to the segment file
                // 1. store 4 bytes (int) in the beginning as an indicator to
                // the reader of how many fields are next for her to retrieve,
                // 2. iteratively store the fields
                dosStore.writeUTF(entry.getValue());
            } // -- for all documents

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dosStore != null) {
                    dosStore.close();
                }
                if (dosStoreOffsets != null) {
                    dosStoreOffsets.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Merge all segments to one humongous index
     *
     * TODO Currently only reads and prints (TESTING!).
     */
    private static void mergeSegmentsOnDisk(String segmentOne, String segmentOffsetsOne,
            String segmentTwo, String segmentOffsetsTwo) {

        File fSeg = new File("./seg." + 0);
        File fOff = new File("./seg.i." + 0);

        FileInputStream fisSeg = null;
        DataInputStream disSeg = null;
        FileInputStream fisOff = null;
        DataInputStream disOff = null;

        TreeMap<String, TreeSet<Posting>> foo = null;
        try {
            fisSeg = new FileInputStream(fSeg);
            disSeg = new DataInputStream(fisSeg);
            fisOff = new FileInputStream(fOff);
            disOff = new DataInputStream(fisOff);

            foo = new TreeMap<String, TreeSet<Posting>>();

            String term;
            int offset;
            while (true) {
                term = disOff.readUTF();
                offset = disOff.readInt();
//                System.out.println("term: " + term
//                        + " offset: " + offset);

                // Retrieve the number of postings...
                int postingsListSize = disSeg.readInt();
                TreeSet<Posting> termPostings =
                        new TreeSet<Posting>();
                // ...and iterate through the appropriate number of bytes...
                for (int i = 0; i < postingsListSize; i++) {
                    // ...to fetch the needed data
                    long docId = disSeg.readLong();
                    // ...and add a new posting in the term's list
                    termPostings.add(new Posting(docId));
                }

                // add the new term to an in-memory index for printing.
                foo.put(term, termPostings);
            } // -- while not EOF
        }
        catch (EOFException eofe) {
            // Done reading segment or offset file.
//            if (foo != null) printIndex(foo);
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (disSeg != null) disSeg.close();
                if (fisSeg != null) fisSeg.close();
                if (disOff != null) disOff.close();
                if (fisOff != null) fisOff.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
