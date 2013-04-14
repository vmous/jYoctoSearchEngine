package yocto.searching;

/**
 * Query abstract class.
 *
 * @author billy
 */
public abstract class Query {

    /* The term of this query. */
    private final QueryTerm queryTerm;

    /**
     * Constructor.
     *
     * @param queryTerm
     *     The term of the query. Currently only one term is supported.
     */
    public Query(QueryTerm queryTerm) {
        this.queryTerm = queryTerm;
    }

    /**
     * Gets the query term.
     *
     * @return
     *     The query term.
     */
    public QueryTerm getQueryTerm() {
        return queryTerm;
    }

}
