package yocto.searching;

/**
 * Parses string queries and constructs {@link Query} object
 * handled by the search engine.
 *
 * @author billy
 */
public class QueryParser {

    /**
     * Constructor.
     */
    public QueryParser() {
    }

    /**
     * Parses the given query.
     *
     * @param query
     *     The query to parse.
     *
     * @return
     *     A {@code Query} object representing the given query.
     */
    public static Query parse(String query) {
        String[] tokens = query.split("[\\s]+");
        Query q = null;

        for (String token : tokens) {
            token = token.toLowerCase().trim();
            if (!token.equals("")) {
                if(token.endsWith("*")) {
                    token = token.substring(0, token.length() - 1);
                    // Prefix query.
                    q = new PrefixQuery(
                            new QueryTerm(token));
                }
                else {
                    // Normal query.
                    q = new NormalQuery(
                            new QueryTerm(token));
                }
            }
        }

        return q;
    }

}
