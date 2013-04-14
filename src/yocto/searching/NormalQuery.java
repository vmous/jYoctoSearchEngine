package yocto.searching;

/**
 * Wrapper class for a normal query.
 *
 * @author billy
 */
public class NormalQuery extends Query {

    /**
     * Constructor.
     *
     * @param term
     *     The term.
     */
    public NormalQuery(QueryTerm term) {
        super(term);
    }

}
