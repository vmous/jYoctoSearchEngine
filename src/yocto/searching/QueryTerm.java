package yocto.searching;

/**
 * The representation of a term in a search query.
 */
public class QueryTerm {

    /* The term literal */
    private final String term;

//    /* The term frequency in the query. Currently not in use. */
//    private final int tf;

    /**
     * Constructor.
     *
     * @param term
     *     The term.
     * @param tf
     *     The term frequency in the query.
     */
    public QueryTerm(String term) {
        this.term = term;
//        this.tf = 0;
    }


    // -- Getters


    /**
     * Gets the term literal of the query term.
     *
     * @return
     *     The term.
     */
    public String getTerm() {
        return term;
    }

}
