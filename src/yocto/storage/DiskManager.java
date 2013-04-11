package yocto.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    private static final int OUT_BUFF_SIZE = 32 * 1024;
    private static final int IN_BUFF_SIZE = 32 * 1024;

    public static final String INDEX_FILENAME = "indx";
    public static final String INDEX_OFFSETS_FILENAME = "indx.off";
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
     *
     * @param index
     *     The in-memory index to persist.
     */
    public void writeIndexSegment(TreeMap<String, TreeSet<Posting>> index) {

        try (   DataOutputStream dosSegment = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(pathnameSegment + numSegments),
                                OUT_BUFF_SIZE));

                DataOutputStream dosSegmentOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(pathnameSegmentOffsets + numSegments),
                                OUT_BUFF_SIZE));)
        {
            numSegments++;

            Set<String> terms = index.keySet();

            TreeSet<Posting> termPostings;
            long offset = 0;

            for (String term : terms) {
                IndexOffsetsRecord ior = new IndexOffsetsRecord(term, offset);
                writeIndexOffsetsRecord(dosSegmentOffsets, ior);

                termPostings = index.get(term);
                IndexRecord ir = new IndexRecord(termPostings.size(), termPostings);
                // The data concerning the next term will be written
                // to segment file starting from here.
                offset += writeIndexRecord(dosSegment, ir);

            } // -- for all terms

            // clear the in-memory index since persisted on disk.
            // TODO maybe this is a POB if multi-threaded.
//            index.clear();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * Appends fields to store file.
     *
     * The store file is not fragmented and merged because we assume that
     * the documents are assigned a monotonically increasing id number.
     */
    public void appendStore(LinkedHashMap<Long, String> store) {

        try (   DataOutputStream dosStore = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(pathnameStore, true),
                                OUT_BUFF_SIZE));

                DataOutputStream dosStoreOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(pathnameStoreOffsets, true),
                                OUT_BUFF_SIZE));)
        {

            long offset = 0;

            for (Map.Entry<Long,String> entry : store.entrySet()) {

                StoreOffsetsRecord sor = new StoreOffsetsRecord(entry.getKey(), offset);
                writeStoreOffsetsRecord(dosStoreOffsets, sor);
                StoreRecord sr = new StoreRecord(entry.getValue());
                // The stored field(s) concerning the next document will be
                // written to store file starting from here.
                offset += writeStoreRecord(dosStore, sr);

            } // -- for all documents

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Merge all segments to one humongous index
     *
     * TODO Currently only reads and prints (TESTING!).
     */
    private void mergeSegmentsOnDisk(
            String segmentOne, String segmentOffsetsOne,
            String segmentTwo, String segmentOffsetsTwo,
            String merged, String mergedOffsets) {

        try (   DataInputStream disSegmentOne = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(segmentOne),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentOffsetsOne = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(segmentOffsetsOne),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentTwo = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(segmentTwo),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentOffsetsTwo = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(segmentOffsetsTwo),
                                IN_BUFF_SIZE));

                DataOutputStream dosIndex = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(merged),
                                OUT_BUFF_SIZE));

                DataOutputStream dosIndexOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(mergedOffsets),
                                OUT_BUFF_SIZE)); ){

            // What we read from the segments
            String termFromOne = "";
            String termFromTwo = "";
            boolean termFromOneConsumed = true;
            boolean termFromTwoConsumed = true;

            // What we write in the merged index
            String term = "";
            int offset;


            if (termFromOneConsumed)
                termFromOne = disSegmentOffsetsOne.readUTF();

            if (termFromTwoConsumed)
                termFromTwo = disSegmentOffsetsTwo.readUTF();

            int ordering = termFromOne.compareToIgnoreCase(termFromTwo);

            if (ordering < 0) {
                // The term from segment one comes lexicographically first.

                // We haven't consumed term from segment two so in next
                // iteration we do not need to read a new.
                termFromTwoConsumed = false;
            }
            else if (ordering > 0) {
                // The term from segment two comes lexicographically first.

                // We haven't consumed term from segment two so in next
                // iteration we do not need to read a new.
                termFromOneConsumed = false;
            }
            else {
                // The two terms are lexicographically equivalent.

            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//        File fSeg = new File("./seg." + 0);
//        File fOff = new File("./seg.i." + 0);
//
//        FileInputStream fisSeg = null;
//        DataInputStream disSeg = null;
//        FileInputStream fisOff = null;
//        DataInputStream disOff = null;
//
//        TreeMap<String, TreeSet<Posting>> foo = null;
//        try {
//            fisSeg = new FileInputStream(fSeg);
//            disSeg = new DataInputStream(fisSeg);
//            fisOff = new FileInputStream(fOff);
//            disOff = new DataInputStream(fisOff);
//
//            foo = new TreeMap<String, TreeSet<Posting>>();
//
//            String term;
//            int offset;
//            while (true) {
//                term = disOff.readUTF();
//                offset = disOff.readInt();
////                System.out.println("term: " + term
////                        + " offset: " + offset);
//
//                // Retrieve the number of postings...
//                int postingsListSize = disSeg.readInt();
//                TreeSet<Posting> termPostings =
//                        new TreeSet<Posting>();
//                // ...and iterate through the appropriate number of bytes...
//                for (int i = 0; i < postingsListSize; i++) {
//                    // ...to fetch the needed data
//                    long docId = disSeg.readLong();
//                    // ...and add a new posting in the term's list
//                    termPostings.add(new Posting(docId));
//                }
//
//                // add the new term to an in-memory index for printing.
//                foo.put(term, termPostings);
//            } // -- while not EOF
//        }
//        catch (EOFException eofe) {
//            // Done reading segment or offset file.
////            if (foo != null) printIndex(foo);
//        }
//        catch (FileNotFoundException fnfe) {
//            fnfe.printStackTrace();
//        }
//        catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        finally {
//            try {
//                if (disSeg != null) disSeg.close();
//                if (fisSeg != null) fisSeg.close();
//                if (disOff != null) disOff.close();
//                if (fisOff != null) fisOff.close();
//            }
//            catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
//        }
    }


    /**
     * @param dos
     * @param record
     * @throws IOException
     */
    protected void writeIndexOffsetsRecord(
            DataOutputStream dos, IndexOffsetsRecord record) throws IOException {

        if (dos == null || record == null) throw new IOException("Data stream or index record null.");

        // Save the term literal and offset to the offsets
        // file for enabling random access dictionary to the segment
        // file
        dos.writeUTF(record.getTerm());
        dos.writeLong(record.getOffset());
    }


    /**
     * Writes an index formatted record to a data stream.
     *
     * @param dos
     *     The {@link DataOutputStream} to write the record to. For better
     *     performance make sure its underlying {@link FileOutputStream} is
     *     decorated buffering (i.e., wrapped with a
     *     {@code BufferedOutputStream}.
     * @param record
     *     The index record to be written.
     *
     * @return
     *     The number of bytes written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the index record
     *     provided is {@null null}.
     */
    protected long writeIndexRecord(
            DataOutputStream dos, IndexRecord record) throws IOException {

        if (dos == null || record == null) throw new IOException("Data stream or index record null.");

        long bytesWritten = 0;

        int postingsSize = record.getPostingsSize();
        TreeSet<Posting> termPostings = record.getPostings();

        // Write to stream only if we have something to write.
        if (postingsSize > 0) {

            Iterator<Posting> iter = termPostings.iterator();
            // Write the size of the postings list, so that the
            // reader can iteratively pick up the correct number of
            // postings.
            dos.writeInt(record.getPostingsSize());
            // Iterate through the term's postings and...
            bytesWritten += 4;
            Posting posting;
            while (iter.hasNext()) {
                posting = iter.next();
                // ...write the document id
                dos.writeLong(posting.getDocId());
                bytesWritten += 8;
            } // -- while postings
        }

        return bytesWritten;
    }


    /**
     * @param dos
     * @param record
     * @throws IOException
     */
    protected void writeStoreOffsetsRecord(
            DataOutputStream dos, StoreOffsetsRecord record) throws IOException {

        if (dos == null || record == null) throw new IOException("Data stream or index record null.");

        // Save the document id and offset in the store offset file.
        // This will enable random access dictionary to the store
        // file.
        dos.writeLong(record.getDocId());
        dos.writeLong(record.getOffset());
    }


    /**
     * Writes an store formatted record to a data stream.
     *
     * @param dos
     *     The {@link DataOutputStream} to write the record to. For better
     *     performance make sure its underlying {@link FileOutputStream} is
     *     decorated with buffering (i.e., wrapped with a
     *     {@code BufferedOutputStream}.
     * @param record
     *     The store record to be written.
     *
     * @return
     *     The number of bytes written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the store record
     *     provided is {@code null}.
     */
    protected long writeStoreRecord(
            DataOutputStream dos, StoreRecord record) throws IOException {

        if (dos == null || record == null) throw new IOException("Data stream or index record null.");

        long bytesWritten = dos.size();

        // Write stored field(s) to the store file.
        // TODO Currently we have only one stored field. If we ever
        // have multiple, then we can do similarly to the segment file
        // 1. store 4 bytes (int) in the beginning as an indicator to
        // the reader of how many fields are next for her to retrieve,
        // 2. iteratively store the fields
        dos.writeUTF(record.getStored());

        return dos.size() - bytesWritten;
    }

}
