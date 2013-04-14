package yocto.searching;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * The entry point class for the search engine.
 *
 * @author billy
 */
public class Searcher {

    /* Path name to postings offsets file. */
    private final String pathPostingsOffsets;

    /* Path name to postings file. */
    private final String pathPostings;

    /* Path name to store offsets file. */
    private final String pathStoreOffsets;

    /* Path name to store file. */
    private final String pathStore;

    /* The query executor. */
    private final QueryExecutor qexec;


    /**
     * Constructor.
     *
     * @param pathPostingsOffsets
     *     Path to postings offsets file.
     * @param pathPostings
     *     Path to postings file.
     * @param pathStoreOffsets
     *     Path to store offsets file.
     * @param pathStore
     *     Path to store file.
     *
     * @throws FileNotFoundException
     */
    public Searcher(
            String pathPostingsOffsets,
            String pathPostings,
            String pathStoreOffsets,
            String pathStore) throws FileNotFoundException {

        this.pathPostingsOffsets = pathPostingsOffsets;
        this.pathPostings = pathPostings;
        this.pathStoreOffsets = pathStoreOffsets;
        this.pathStore = pathStore;

        this.qexec = new QueryExecutor(
                loadPostingsLookup(),
                loadStoreLookup(),
                openPostings(),
                openStore());
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
    public List<Hit> searchQuery(String query) {

        Query q = QueryParser.parse(query);

        return qexec.execute(q);
    }


    /**
     * Loads the look-up table for the index into the memory.
     *
     * @return
     *     The look-up table for the postings. If an error was encountered then
     *     an empty look-up table is returned.
     */
    private TreeMap<String, Long> loadPostingsLookup() {
        TreeMap<String, Long> lu = new TreeMap<String, Long>();

        try (   DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(pathPostingsOffsets),
                                2 * 1024));)
        {
            while (true) {
                lu.put(dis.readUTF(), new Long(dis.readLong()));
            }
        } catch (EOFException eofe) {
            // Done reading offsets file.
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return lu;
    }


    /**
     * Loads the look-up table for the store into the memory.
     *
     * @return
     *     The look-up table for the store. If an error was encountered then
     *     an empty look-up table is returned.
     */
    private HashMap<Long, Long> loadStoreLookup() {


        HashMap<Long, Long> lu = new HashMap<Long, Long>();



        try (   DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(pathStoreOffsets),
                                2 * 1024));)
        {
            while (true) {
                lu.put(new Long(dis.readLong()), new Long(dis.readLong()));
            }
        }
        catch (EOFException eofe) {
            // Done reading offsets file.
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return lu;
    }


    /*
     * Opens the postings file.
     *
     * @return
     *     A reference to the postings file.
     */
    private RandomAccessFile openPostings() {

        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(pathPostings, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return raf;
    }


    /*
     * Opens the store file.
     *
     * @return
     *     A reference to the store file.
     */
    private RandomAccessFile openStore() {

        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(pathStore, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return raf;
    }

}
