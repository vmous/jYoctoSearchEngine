package yocto.searching;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author billy
 */
public class Searcher {

    /* Pathname to index postings file. */
    private final String pathnameIndex;

    /* Pathname to index offsets file. */
    private final String pathnameIndexOffsets;

    /* Path name to store file. */
    private final String pathnameStore;

    /* Path name to store offsets file. */
    private final String pathnameStoreOffsets;

    /*
     * In-memory data structure of fast and shorted index file lookup.
     *
     * TODO Tree data structure may enable partial matching???
     * else a hash map is the fastest lookup
     * (luck favours the brave:)
     */
    private TreeMap<String, Long> indexLookup;

    /* In-memeory data structure of fast store file lookup. */
    private HashMap<Long, Long> storeLookup;

    /* The index file for random access. */
    private RandomAccessFile index;

    /* The store file for random access. */
    private RandomAccessFile store;


    /**
     * Constructor.
     *
     * @param offsetsPathName
     *     Pathname to the offsets file.
     * @param indexPathName
     *     Pathname to the index file.
     *
     * @throws FileNotFoundException
     */
    public Searcher(String pathnameIndex, String pathnameIndexOffsets,
            String pathnameStore, String pathnameStoreOffsets)
                    throws FileNotFoundException {
        this.pathnameIndex = pathnameIndex;
        this.pathnameIndexOffsets = pathnameIndexOffsets;
        this.pathnameStore = pathnameStore;
        this.pathnameStoreOffsets = pathnameStoreOffsets;
        loadIndexLookup();
        loadStoreLookup();
        openIndex();
        openStore();
    }


    /**
     * Performs a search query.
     *
     * @param query
     *     The query.
     *
     * @return
     *     A list of the hits that satisfy the given query.
     */
    public List<String> searchQuery(String query) {
        List<String> hits = new ArrayList<String>();

        Long offIndex = indexLookup.get(query.toLowerCase());

        if (offIndex != null) {
            try {
                index.seek(offIndex.longValue());

                // Retrieve the number of postings...
                int postingsListSize = index.readInt();
                for (int i = 0; i < postingsListSize; i++) {
                    long docId = index.readLong();
                    Long offStore = storeLookup.get(docId);
                    String label;
                    if (offStore != null) {
                        store.seek(offStore.longValue());
                        label = store.readUTF();
                    }
                    else label = docId+"";

                    hits.add(label);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return hits;
    }


    /**
     * Loads the look-up table for the index into the memory.
     *
     * @throws FileNotFoundException
     */
    private void loadIndexLookup() throws FileNotFoundException {
        DataInputStream dis = null;

        indexLookup = new TreeMap<String, Long>();

        dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(pathnameIndexOffsets),
                        32 * 1024));

        try {
            while (true) {
                indexLookup.put(dis.readUTF(), new Long(dis.readLong()));
            }
        } catch (EOFException eofe) {
            // Done reading offsets file.
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (dis != null) dis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    /**
     * Loads the look-up table for the store into the memory.
     *
     * @throws FileNotFoundException
     */
    private void loadStoreLookup() throws FileNotFoundException {
        DataInputStream dis = null;

        storeLookup = new HashMap<Long, Long>();

        dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(pathnameStoreOffsets),
                        32 * 1024));

        try {
            while (true) {
                storeLookup.put(new Long(dis.readLong()), new Long(dis.readLong()));
            }
        }
        catch (EOFException eofe) {
            // Done reading offsets file.
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (dis != null) dis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    /**
     * Opens the index file.
     *
     * @throws FileNotFoundException
     */
    private void openIndex() throws FileNotFoundException {
        this.index = new RandomAccessFile(pathnameIndex, "r");
    }


    /**
     * Opens the store file.
     *
     * @throws FileNotFoundException
     */
    private void openStore() throws FileNotFoundException {
        this.store = new RandomAccessFile(pathnameStore, "r");
    }


    /**
     * Prints a dictionary.
     *
     * For debugging.
     *
     * @param dictionary
     *     The dictionary to print.
     */
    protected static void printDictionary(TreeMap<String, Integer> dictionary) {
        for(Map.Entry<String, Integer> entry : dictionary.entrySet()) {
            System.out.println("term: " + entry.getKey() + " offset: " + entry.getValue());
        }
    }


    // -- Override


    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        index.close();
        store.close();
    }

}
