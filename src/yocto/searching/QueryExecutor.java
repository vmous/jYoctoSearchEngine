package yocto.searching;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The class of objects handling the execution of search queries.
 *
 * @author billy
 */
public class QueryExecutor {

    /* In-memory data structure of fast and shorted index file lookup. */
    private final TreeMap<String, Long> postingsLookup;

    /* In-memeory data structure of fast store file lookup. */
    private final HashMap<Long, Long> storeLookup;

    /* The index file for random access. */
    private final RandomAccessFile postings;

    /* The store file for random access. */
    private final RandomAccessFile store;


    /**
     * Constructor.
     *
     * @param postingsLookup
     *     The look-up table for the postings file.
     * @param storeLookup
     *     The look-up table for the store file.
     * @param index
     *     The postings file.
     * @param store
     *     The store file
     *
     * @throws FileNotFoundException
     */
    public QueryExecutor(
            TreeMap<String, Long> postingsLookup,
            HashMap<Long, Long> storeLookup,
            RandomAccessFile postings,
            RandomAccessFile store) throws FileNotFoundException {
        this.postingsLookup = postingsLookup;
        this.storeLookup = storeLookup;
        this.postings = postings;
        this.store = store;
    }


    /**
     * Executes the query.
     *
     * @param query
     *     The query to execute.
     *
     * @return
     *     A list of hits satisfying the given query.
     */
    public List<Hit> execute(Query query) {
        List<Hit> hits;

        if (query instanceof PrefixQuery) {
            hits = executePrefixQuery((PrefixQuery) query);
        }
        else {
            hits = executeNormalQuery((NormalQuery) query);
        }

        return hits;
    }


    /**
     * Prints the postings look-up table.
     *
     * For debugging.
     */
    protected void printDictionary() {
        for(Map.Entry<String, Long> entry : postingsLookup.entrySet()) {
            System.out.println("term: " + entry.getKey() + " offset: " + entry.getValue());
        }
    }


    /*
     * Executes a prefix query.
     *
     * @param query
     *     The prefix query
     *
     * @return
     *     A list of hits satisfying the given prefix query.
     */
    private List<Hit> executePrefixQuery(PrefixQuery query) {
        List<Hit> hits = new LinkedList<Hit>();

        String prefix = query.getQueryTerm().getTerm();
        SortedMap<String, Long> over = postingsLookup.tailMap(prefix, true);
        String term = "";
        for (Map.Entry<String, Long> entry : over.entrySet()) {
            term = entry.getKey();
            if (!term.startsWith(prefix))
                break;

            hits.addAll(gatherHitsForTerm(entry.getValue()));
        }

        return hits;
    }


    /*
     * Executes a normal query.
     *
     * @param query
     *     The normal query
     *
     * @return
     *     A list of hits satisfying the given normal query.
     */
    private List<Hit> executeNormalQuery(NormalQuery query) {

        return gatherHitsForTerm(postingsLookup.get(query.getQueryTerm().getTerm()));
    }


    /*
     * Helper method for gathering hits for a term.
     *
     * @param term
     *     The term.
     *
     * @return
     *     A list of hits satisfying the given term.
     */
    private List<Hit> gatherHitsForTerm(Long offset) {
        List<Hit> hits = new LinkedList<Hit>();

        if (offset != null) {
            try {
                postings.seek(offset.longValue());

                // Retrieve the number of postings...
                int postingsListSize = postings.readInt();
                for (int i = 0; i < postingsListSize; i++) {
                    long docId = postings.readLong();
                    Long offStore = storeLookup.get(docId);
                    String label;
                    if (offStore != null) {
                        store.seek(offStore.longValue());
                        label = store.readUTF();
                    }
                    else label = docId+"";

                    hits.add(new Hit(label));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return hits;
    }


    // -- Override


    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        postings.close();
        store.close();
    }

}
