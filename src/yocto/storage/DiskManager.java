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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import yocto.indexing.Posting;

/**
 * The underlying persistence storage management class.
 *
 * @author billy
 */
public class DiskManager {

    /* The buffer for writing. */
    private static final int OUT_BUFF_SIZE = 8 * 1024;

    /* The buffer for reading. */
    private static final int IN_BUFF_SIZE = 4 * 1024;

    /* The file naming. */
//    private static final String INDEX_FILENAME = "indx";
//    private static final String INDEX_OFFSETS_FILENAME = "indx.off";
    private static final String SEGMENT_FILENAME = "_seg.";
    private static final String SEGMENT_OFFSETS_FILENAME = "_seg.off.";
    private static final String STORE_FILENAME = "stor";
    private static final String STORE_OFFSETS_FILENAME = "stor.off";

//    /* The pathname to the index file. */
//    private final String pathnameIndex;
//
//    /* The pathname to the index offsets file. */
//    private final String pathnameIndexOffsets;

    /*
     * The pathname to a segment file. The manager appends an id number for a
     * specific file.
     */
    private final String pathnameSegment;

    /*
     * The pathname to a segment offsets file. The manager appends an id number
     * for a specific file.
     */
    private final String pathnameSegmentOffsets;

    /* The pathname to the store file. */
    private final String pathnameStore;

    /* The pathname to the store offsets file. */
    private final String pathnameStoreOffsets;

    /*
     * The number of processed segments. Used for naming. Accessed by both the
     * main inverting thread and the background merging worker thread thus
     * needs to be consistently increment.
     */
    private final AtomicInteger numSegments;

    /*
     * The queue where the producing, inverting main thread offers new segments.
     * This is from where new merging tasks are created.
     */
    private final Queue<Segment> segments;

    /*
     * The thread pool.
     */
    private final ExecutorService executor;

    /*
     * A Queue of futures for the submitted threads.
     */
    private final Queue<Future<?>> futures;


    /* The offset for the store file */
    private long storeOffset = 0;


    /**
     * Constructor.
     *
     * @param dir
     *     The directory to store.
     */
    public DiskManager(String dir) {
        File d = new File(dir);
        if( !d.exists() )
            d.mkdirs();

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

        // TODO double check this datastructure for multithreading.
        this.segments = new ConcurrentLinkedQueue<Segment>();

        // Currently one merging thread.
        executor = Executors.newSingleThreadExecutor();

        futures = new LinkedList<Future<?>>();
    }


    /**
     * Opens the disk manager.
     */
    public void open() {
    }


    /**
     * Closes the disk manager. This will block until all threads have concluded.
     */
    public void close() {
        while (!futures.isEmpty() ) {
//            System.out.println(futures.size());
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


    /**
     * Persisting index to disk.
     *
     * @param index
     *     The in-memory index to persist.
     */
    public void writeIndexSegment(TreeMap<String, TreeSet<Posting>> index) {
        int i = numSegments.getAndIncrement();
        Segment segment = new Segment(
                new File(pathnameSegmentOffsets + i),
                new File(pathnameSegment + i));

//        System.out.println("\n\nSegment: " + segment.getPostings() + "\n\n");

        try (   DataOutputStream dosSegment = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(segment.getPostings()),
                                OUT_BUFF_SIZE));

                DataOutputStream dosSegmentOffsets = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(segment.getOffsets()),
                                OUT_BUFF_SIZE));)
        {


            Set<String> terms = index.keySet();
            Iterator<String> iter = terms.iterator();
            TreeSet<Posting> termPostings;
            long offset = 0;
            String term = "";
            while (iter.hasNext()) {
                term = iter.next();
                PostingsOffsetsRecord por = new PostingsOffsetsRecord(term, offset);
                writePostingsOffsetsRecord(dosSegmentOffsets, por);

                termPostings = index.get(term);
                PostingsRecord pr = new PostingsRecord(termPostings.size(), termPostings);
                // The data concerning the next term will be written
                // to segment file starting from here.
                offset += writePostingsRecord(dosSegment, pr);

            } // -- for all terms

            segments.offer(segment);

            if (segments.size() >= 2) {
                futures.add(executor.submit(new MergeTask(segments.poll(), segments.poll(), this)));
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
     * Merging two segments into one.
     *
     * @param one
     *     The first segment to be merged.
     * @param two
     *     The second segment to be merged.
     */
    public void mergeSegmentsOnDisk(Segment one, Segment two) {
        int i = numSegments.getAndIncrement();
        Segment merged = new Segment(
                new File(pathnameSegmentOffsets + i),
                new File(pathnameSegment + i));

//        System.out.println("\n\nMerging: " + one.getPostings() + " + " + two.getPostings() + " -> " + merged.getPostings() + "\n\n");

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
            PostingsOffsetsRecord porOne = null;
            PostingsRecord prOne = null;

            PostingsOffsetsRecord porTwo = null;
            PostingsRecord prTwo = null;

            PostingsOffsetsRecord porMerged = null;
            PostingsRecord prMerged = null;

            long offsetMerged = 0;
            int ordering = 0;

            while (true) {

                if (porOne == null) {
                    if (    ((porOne = readPostingsOffsetsRecord(disSegmentOffsetsOne)) == null ) ||
                            ((prOne = readPostingsRecord(disSegmentOne)) == null))
                    {
                        // Segment one EOF

                        // Write the objects you might have currently from segment two...
                        if (porTwo != null) {
                            prMerged = prTwo;
                            porMerged = new PostingsOffsetsRecord(porTwo.getTerm(), offsetMerged);
                            writePostingsOffsetsRecord(dosMergedOffsets, porMerged);
                            offsetMerged += writePostingsRecord(dosMerged, prMerged);
                        }

                        // And flush the rest of segment two to the merged
                        try {
                            while (true) {
                                if (    ((porTwo = readPostingsOffsetsRecord(disSegmentOffsetsTwo)) == null ||
                                        ((prTwo = readPostingsRecord(disSegmentTwo)) == null )))
                                {
                                    throw new EOFException();
                                }

                                prMerged = prTwo;
                                porMerged = new PostingsOffsetsRecord(porTwo.getTerm(), offsetMerged);
                                writePostingsOffsetsRecord(dosMergedOffsets, porMerged);
                                offsetMerged += writePostingsRecord(dosMerged, prMerged);
                            }
                        } catch (EOFException e) {
                            break;
                        }
                    }
                }

                if (porTwo == null) {
                    if (    ((porTwo = readPostingsOffsetsRecord(disSegmentOffsetsTwo)) == null ) ||
                            ((prTwo = readPostingsRecord(disSegmentTwo)) == null))
                    {
                        // Segment two EOF

                        // Write the objects you might have currently from segment one...
                        if (porOne != null) {
                            prMerged = prOne;
                            porMerged = new PostingsOffsetsRecord(porOne.getTerm(), offsetMerged);
                            writePostingsOffsetsRecord(dosMergedOffsets, porMerged);
                            offsetMerged += writePostingsRecord(dosMerged, prMerged);
                        }

                        // And flush the rest of segment one to the merged
                        try {
                            while (true) {
                                if (    ((prOne = readPostingsRecord(disSegmentOne)) == null ) ||
                                        ((porOne = readPostingsOffsetsRecord(disSegmentOffsetsOne)) == null ))
                                {
                                    throw new EOFException();
                                }

                                prMerged = prOne;
                                porMerged = new PostingsOffsetsRecord(porOne.getTerm(), offsetMerged);
                                writePostingsOffsetsRecord(dosMergedOffsets, porMerged);
                                offsetMerged += writePostingsRecord(dosMerged, prMerged);
                            }
                        } catch (EOFException e) {
                            break;
                        }
                    }
                }

                ordering = porOne.getTerm().compareToIgnoreCase(porTwo.getTerm());

                if (ordering < 0) {
                    // The term from segment one comes lexicographically first.
                    prMerged = prOne;
                    porMerged = new PostingsOffsetsRecord(porOne.getTerm(), offsetMerged);
                    prOne = null;
                    porOne = null;
                }
                else if (ordering > 0) {
                    // The term from segment two comes lexicographically first.
                    prMerged = prTwo;
                    porMerged = new PostingsOffsetsRecord(porTwo.getTerm(), offsetMerged);
                    prTwo = null;
                    porTwo = null;
                }
                else {
                    TreeSet<Posting> postingsMerged = prOne.getPostings();
                    postingsMerged.addAll(prTwo.getPostings());
                    // The two terms are lexicographically equivalent.
                    prMerged = new PostingsRecord(postingsMerged.size(), postingsMerged);
                    // It is the same as if we used iorTwo.getTerm()
                    porMerged = new PostingsOffsetsRecord(porOne.getTerm(), offsetMerged);
                    prOne = prTwo = null;
                    porOne = porTwo = null;
                }

                writePostingsOffsetsRecord(dosMergedOffsets, porMerged);
                offsetMerged += writePostingsRecord(dosMerged, prMerged);

            } // -- while I can read the streams

            segments.offer(merged);

            if (segments.size() >= 2) {
                futures.add(executor.submit(new MergeTask(segments.poll(), segments.poll(), this)));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }























    // -- Static methods.


    /**
     * Reads a postings offsets formatted record from a stream.
     *
     * @param dis
     *     The input stream to read from.
     *
     * @return
     *     A {@code PostingsOffsetsRecord} filled with data or {@code null} if
     *     attempting to read indicated the end of file.
     *
     * @throws IOException
     *     When unable to read from the given stream or if this is {@null}.
     */
    public static PostingsOffsetsRecord readPostingsOffsetsRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        PostingsOffsetsRecord r = null;
        try {
            r = new PostingsOffsetsRecord(dis.readUTF(), dis.readLong());

        } catch (EOFException e) {
            r = null;
        }

        return r;
    }


    /**
     * Copies a postings offsets formatted record from one stream to another.
     *
     * @param dis
     *     The data stream to read from.
     * @param dos
     *     The data stream to redirect to.
     *
     * @throws IOException
     *     When unable to write from/to the given data streams or either of the
     *     streams provided are {@code null}
     */
    public static void copyPostingsOffsetsRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeUTF(dis.readUTF());
        dos.writeLong(dis.readLong());
    }


    /**
     * Writes a postings offsets formatted record to a stream.
     *
     * @param dos
     *     The data stream to write to.
     * @param record
     *     The record to be written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the record
     *     provided is {@null null}.
     */
    public static void writePostingsOffsetsRecord(
            DataOutputStream dos, PostingsOffsetsRecord record) throws IOException {

        if (dos == null || record == null)
            throw new IOException("Data stream or index record null.");

        // Save the term literal and offset to the offsets
        // file for enabling random access dictionary to the segment
        // file
        dos.writeUTF(record.getTerm());
        dos.writeLong(record.getOffset());
    }


    /**
     * Reads a postings formated record from a stream.
     *
     * @param dis
     *     The input stream to read from.
     *
     * @return
     *     A {@code PostingsRecord} filled with data or {@code null} if
     *     attempting to read indicated the end of file.
     *
     * @throws IOException
     *     When unable to read from the given stream or if this is {@null}.
     */
    public static PostingsRecord readPostingsRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        PostingsRecord r = null;
        try {
            int postingsSize = dis.readInt();
            TreeSet<Posting> postings = new TreeSet<Posting>();

            for (int i = 0; i < postingsSize; i++) {
                postings.add(new Posting(dis.readLong()));
            }

            r = new PostingsRecord(postings.size(), postings);
        } catch (EOFException e) {
            r = null;
        }
        return r;
    }


    /**
     * Copies a postings formatted record from one stream to another.
     *
     * @param dis
     *     The data stream to read from.
     * @param dos
     *     The data stream to redirect to.
     *
     * @throws IOException
     *     When unable to write from/to the given data streams or either of the
     *     streams provided are {@code null}
     */
    public static void copyIndexRecord(DataInputStream dis, DataOutputStream dos)
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
     * Writes a postings formatted record to a stream.
     *
     * @param dos
     *     The data stream to write to.
     * @param record
     *     The record to be written.
     *
     * @return
     *     The number of bytes written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the record
     *     provided is {@null null}.
     */
    public static long writePostingsRecord(DataOutputStream dos,
            PostingsRecord record) throws IOException {

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
     * Reads a store offsets formatted record from a stream.
     *
     * @param dis
     *     The input stream to read from.
     *
     * @return
     *     A {@code StoreOffsetsRecord} filled with data or {@code null} if
     *     attempting to read indicated the end of file.
     *
     * @throws IOException
     *     When unable to read from the given stream or if this is {@null}.
     */
    public static StoreOffsetsRecord readStoreOffsetsRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        StoreOffsetsRecord r = null;
        try {
            r = new StoreOffsetsRecord(dis.readLong(), dis.readLong());
        } catch (EOFException e) {
            r = null;
        }

        return r;
    }


    /**
     * Copies an store offsets formatted record from one stream to another.
     *
     * @param dis
     *     The data stream to read from.
     * @param dos
     *     The data stream to redirect to.
     *
     * @throws IOException
     *     When unable to write from/to the given data streams or either of the
     *     streams provided are {@code null}
     */
    protected void copyStoreOffsetsRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeLong(dis.readLong());
        dos.writeLong(dis.readLong());
    }


    /**
     * Writes an store offsets formatted record to a stream.
     *
     * @param dos
     *     The data stream to write to.
     * @param record
     *     The record to be written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the record
     *     provided is {@null null}.
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

    /**
     * Reads an store formated record from a stream.
     *
     * @param dis
     *     The input stream to read from.
     *
     * @return
     *     A {@code StoreRecord} filled with data or {@code null} if
     *     attempting to read indicated the end of file.
     *
     * @throws IOException
     *     When unable to read from the given stream or if this is {@null}.
     */
    public static StoreRecord readStoreRecord(DataInputStream dis)
            throws IOException {

        if (dis == null)
            throw new IOException("Data stream null.");

        StoreRecord r = null;
        try {
            r = new StoreRecord(dis.readUTF());
        } catch (EOFException e) {
            r = null;
        }

        return r;
    }


    /**
     * Copies a store formatted record from one stream to another.
     *
     * @param dis
     *     The data stream to read from.
     * @param dos
     *     The data stream to redirect to.
     *
     * @throws IOException
     *     When unable to write from/to the given data streams or either of the
     *     streams provided are {@code null}
     */
    public static  void copyStoreRecord(DataInputStream dis, DataOutputStream dos)
            throws IOException {

        if (dis == null || dos == null)
            throw new IOException("Data streams null.");

        dos.writeUTF(dis.readUTF());
    }

    /**
     * Writes a store formatted record to a stream.
     *
     * @param dos
     *     The data stream to write to.
     * @param record
     *     The record to be written.
     *
     * @return
     *     The number of bytes written.
     *
     * @throws IOException
     *     When unable to write to the given data stream or the record
     *     provided is {@null null}.
     */
    public static  long writeStoreRecord(
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

}
