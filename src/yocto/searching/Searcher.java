package yocto.searching;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author billy
 */
public class Searcher {

    /***/
    private final String offsetsPathName;

    /***/
    private final String indexPathName;

    /***/
    private TreeMap<String, Integer> dictionary;

    /***/
    private RandomAccessFile index;


    /**
     * Constructor.
     *
     * @param offsetsPathName
     *     Pathname to the offsets file.
     * @param indexPathName
     *     Pathname to the index file.
     * @throws FileNotFoundException
     */
    public Searcher(String offsetsPathName, String indexPathName) throws FileNotFoundException {
        this.offsetsPathName = offsetsPathName;
        this.indexPathName = indexPathName;
        loadDictionary();
        openIndex();
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

        Integer offset = dictionary.get(query);

        if (offset != null) {
            try {
                index.seek(offset.longValue());

                // Retrieve the number of postings...
                int postingsListSize = index.readInt();
                for (int i = 0; i < postingsListSize; i++) {
                    long docId = index.readLong();
                    hits.add(docId + "");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return hits;
    }


    /**
     * Loads the look-up dictionary to the memory.
     *
     * @throws FileNotFoundException
     */
    private void loadDictionary() throws FileNotFoundException {
        File fOff = new File(offsetsPathName);
        FileInputStream fisOff = null;
        DataInputStream disOff = null;

        dictionary = new TreeMap<String, Integer>();

        fisOff = new FileInputStream(fOff);
        disOff = new DataInputStream(fisOff);

        try {
            while (true) {
                dictionary.put(disOff.readUTF(), new Integer(disOff.readInt()));
            }
        }
        catch (EOFException eofe) {
            // Done reading offsets file.
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (disOff != null) disOff.close();
                if (fisOff != null) fisOff.close();
            }
            catch (IOException ioe) {
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
        this.index = new RandomAccessFile(indexPathName, "r");
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
    }

}
