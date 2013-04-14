package yocto.searching;

/**
 * Wrapper class for a prefix query.
 *
 * @author billy
 */
public class PrefixQuery extends Query {

    /**
     * Constructor.
     *
     * @param term
     *     The term.
     */
    public PrefixQuery(QueryTerm term) {
        super(term);
    }

}
