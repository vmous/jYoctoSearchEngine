package yocto.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import yocto.indexing.Posting;

/**
 *
 *
 * @author billy
 */
public class DiskManager {
    private static final int OUT_BUFF_SIZE = 8 * 1024;
    private static final int IN_BUFF_SIZE = 4 * 1024;

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
    private final AtomicInteger numSegments;

    private final AtomicInteger numMerges;


    private final Deque<Segment> segments;

    /**
     * The thread pool.
     */
    private final ExecutorService executor;

    /**
     * A Queue of futures for the submitted threads.
     */
    private final Queue<Future<?>> futures;


    private long storeOffset = 0;


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

        this.numSegments = new AtomicInteger();
        this.numMerges = new AtomicInteger();

        // TODO double check this datastructure for multithreading.
        this.segments = new ConcurrentLinkedDeque<Segment>();
        executor = Executors.newSingleThreadExecutor();
        futures = new LinkedList<Future<?>>();
    }





    /**
     * Persisting index to disk.
     *
     * @param index
     *     The in-memory index to persist.
     */
    public void writeIndexSegment(TreeMap<String, TreeSet<Posting>> index) {
        Segment segment = new Segment(
                new File(pathnameSegmentOffsets + numSegments),
                new File(pathnameSegment + numSegments));

        try (   DataOutputStream dosSegment = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(segment.getPostings()),
                                OUT_BUFF_SIZE));

                DataOutputStream dosSegmentOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(segment.getOffsets()),
                                OUT_BUFF_SIZE));)
        {
            numSegments.incrementAndGet();

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

            segments.addLast(segment);

            if (segments.size() >= 2) {
                futures.add(executor.submit(new MergeTask(segments.removeFirst(), segments.removeFirst(), this)));
            }

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

            for (Map.Entry<Long,String> entry : store.entrySet()) {

                StoreOffsetsRecord sor = new StoreOffsetsRecord(entry.getKey(), storeOffset);
                writeStoreOffsetsRecord(dosStoreOffsets, sor);
                StoreRecord sr = new StoreRecord(entry.getValue());
                // The stored field(s) concerning the next document will be
                // written to store file starting from here.
                storeOffset += writeStoreRecord(dosStore, sr);

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
    public void mergeSegmentsOnDisk(Segment one, Segment two) {

        System.out.println("Merging!!!!");
        int i = numSegments.incrementAndGet();
        Segment merged = new Segment(
                new File(pathnameSegmentOffsets + i),
                new File(pathnameSegment + i));

        try (   DataInputStream disSegmentOffsetsOne = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(one.getOffsets()),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentOne = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(one.getPostings()),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentOffsetsTwo = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(two.getOffsets()),
                                IN_BUFF_SIZE));
                DataInputStream disSegmentTwo = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(two.getPostings()),
                                IN_BUFF_SIZE));
                DataOutputStream dosMergedOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(merged.getOffsets()),
                                OUT_BUFF_SIZE));
                DataOutputStream dosMerged = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(merged.getPostings()),
                                OUT_BUFF_SIZE));)
        {
            IndexOffsetsRecord iorOne = null;
            IndexRecord irOne = null;

            IndexOffsetsRecord iorTwo = null;
            IndexRecord irTwo = null;

            IndexOffsetsRecord iorMerged = null;
            IndexRecord irMerged = null;

            long offsetMerged = 0;
            int ordering = 0;

            while (true) {

                if (iorOne == null) {
                    if (    ((iorOne = readIndexOffsetsRecord(disSegmentOffsetsOne)) == null ) ||
                            ((irOne = readIndexRecord(disSegmentOne)) == null))
                    {
                        // Segment one EOF

                        // Write the objects you might have currently from segment two...
                        if (iorTwo != null) {
                            irMerged = irTwo;
                            iorMerged = new IndexOffsetsRecord(iorTwo.getTerm(), offsetMerged);
                            writeIndexOffsetsRecord(dosMergedOffsets, iorMerged);
                            offsetMerged += writeIndexRecord(dosMerged, irMerged);
                        }

                        // And flush the rest of segment two to the merged
                        try {
                            while (true) {
                                copyIndexOffsetsRecord(disSegmentOffsetsTwo, dosMergedOffsets);
                                copyIndexRecord(disSegmentTwo, dosMerged);
                            }
                        } catch (EOFException e) {
                            return;
                        }
                    }
                }

                if (iorTwo == null) {
                    if (    ((iorTwo = readIndexOffsetsRecord(disSegmentOffsetsTwo)) == null ) ||
                            ((irTwo = readIndexRecord(disSegmentTwo)) == null))
                    {
                        // Segment two EOF

                        // Write the objects you might have currently from segment one...
                        if (iorOne != null) {
                            irMerged = irOne;
                            iorMerged = new IndexOffsetsRecord(iorOne.getTerm(), offsetMerged);
                            writeIndexOffsetsRecord(dosMergedOffsets, iorMerged);
                            offsetMerged += writeIndexRecord(dosMerged, irMerged);
                        }

                        // And flush the rest of segment one to the merged
                        try {
                            while (true) {
                                copyIndexOffsetsRecord(disSegmentOffsetsOne, dosMergedOffsets);
                                copyIndexRecord(disSegmentOne, dosMerged);
                            }
                        } catch (EOFException e) {
                            return;
                        }
                    }
                }

                ordering = iorOne.getTerm().compareToIgnoreCase(iorTwo.getTerm());

                if (ordering < 0) {
                    // The term from segment one comes lexicographically first.
                    irMerged = irOne;
                    iorMerged = new IndexOffsetsRecord(iorOne.getTerm(), offsetMerged);
                    irOne = null;
                    iorOne = null;
                }
                else if (ordering > 0) {
                    // The term from segment two comes lexicographically first.
                    irMerged = irTwo;
                    iorMerged = new IndexOffsetsRecord(iorTwo.getTerm(), offsetMerged);
                    irTwo = null;
                    iorTwo = null;
                }
                else {
                    TreeSet<Posting> postingsMerged = irOne.getPostings();
                    postingsMerged.addAll(irTwo.getPostings());
                    // The two terms are lexicographically equivalent.
                    irMerged = new IndexRecord(postingsMerged.size(), postingsMerged);
                    // It is the same as if we used iorTwo.getTerm()
                    iorMerged = new IndexOffsetsRecord(iorOne.getTerm(), offsetMerged);
                    irOne = irTwo = null;
                    iorOne = iorTwo = null;
                }

                writeIndexOffsetsRecord(dosMergedOffsets, iorMerged);
                offsetMerged += writeIndexRecord(dosMerged, irMerged);

            } // -- while I can read the streams

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            segments.addLast(merged);

            if (segments.size() >= 2) {
                futures.add(executor.submit(new MergeTask(segments.removeFirst(), segments.removeFirst(), this)));
            }


            numMerges.incrementAndGet();
        }

    }


    /**
     *
     *
     * @param dis
     *
     * @return
     *
     * @throws IOException
     */
    protected IndexOffsetsRecord readIndexOffsetsRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        IndexOffsetsRecord ior = null;
        try {
            ior = new IndexOffsetsRecord(dis.readUTF(), dis.readLong());
        } catch (EOFException e) {
            ior = null;
        }

        return ior;
    }


    protected void copyIndexOffsetsRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeUTF(dis.readUTF());
        dos.writeLong(dis.readLong());
    }


    /**
     *
     *
     * @param dos
     * @param record
     * @throws IOException
     */
    protected void writeIndexOffsetsRecord(
            DataOutputStream dos, IndexOffsetsRecord record) throws IOException {

        if (dos == null || record == null)
            throw new IOException("Data stream or index record null.");

        // Save the term literal and offset to the offsets
        // file for enabling random access dictionary to the segment
        // file
        dos.writeUTF(record.getTerm());
        dos.writeLong(record.getOffset());
    }


    /**
     * @param dis
     * @return
     * @throws IOException
     */
    protected IndexRecord readIndexRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        IndexRecord ir = null;
        try {
            int postingsSize = dis.readInt();
            TreeSet<Posting> postings = new TreeSet<Posting>();

            for (int i = 0; i < postingsSize; i++) {
                postings.add(new Posting(dis.readLong()));
            }

            ir = new IndexRecord(postings.size(), postings);
        } catch (EOFException e) {
            ir = null;
        }
        return ir;
    }


    protected void copyIndexRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        int postingsSize = dis.readInt();
        dos.writeInt(postingsSize);

        for (int i = 0; i < postingsSize; i++) {
            dos.writeLong(dis.readLong());
        }

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

        if (dos == null || record == null)
            throw new IOException("Data stream or index record null.");

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
     * @param dis
     * @return
     * @throws IOException
     */
    protected StoreOffsetsRecord readStoreOffsetsRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        StoreOffsetsRecord sor = null;
        try {
            sor = new StoreOffsetsRecord(dis.readLong(), dis.readLong());
        } catch (EOFException e) {
            sor = null;
        }

        return sor;
    }


    protected void copyStoreOffsetsRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeLong(dis.readLong());
        dos.writeLong(dis.readLong());
    }


    /**
     * @param dos
     * @param record
     * @throws IOException
     */
    protected void writeStoreOffsetsRecord(
            DataOutputStream dos, StoreOffsetsRecord record) throws IOException {

        if (dos == null || record == null)
            throw new IOException("Data stream or index record null.");

        // Save the document id and offset in the store offset file.
        // This will enable random access dictionary to the store
        // file.
        dos.writeLong(record.getDocId());
        dos.writeLong(record.getOffset());
    }


    protected StoreRecord readStoreRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        StoreRecord sr = null;
        try {
            sr = new StoreRecord(dis.readUTF());
        } catch (EOFException e) {
            sr = null;
        }

        return sr;
    }


    protected void copyStoreRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeUTF(dis.readUTF());
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

        if (dos == null || record == null)
            throw new IOException("Data stream or index record null.");

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


    public void halt() {
        while (!futures.isEmpty() ) {
            System.out.println(futures.size());
            try {
                futures.remove().get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate.");
                }
            }
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();

            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
