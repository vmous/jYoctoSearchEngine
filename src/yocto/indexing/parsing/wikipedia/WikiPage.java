package yocto.indexing.parsing.wikipedia;

import yocto.indexing.Document;

/**
 * An abstraction of a Wikipedia article page.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class WikiPage extends Document {

    /** The Wikipedia article page title. */
    private final String title;

    /** The Wikipedia article page revision contributor username. */
    private final String revisionContributorUsername;


    /**
     * Constructor.
     *
     * @param title
     *     The Wikipedia article page title.
     * @param id
     *     The Wikipedia article page id.
     * @param revisionContributorUsername
     *     The Wikipedia article page revision contributor username.
     * @param revisionText
     *     The Wikipedia article page revision text.
     */
    public WikiPage(String title, long id,
            String revisionContributorUsername, String revisionText) {
        super(id, revisionText);
        this.title = title;
        this.revisionContributorUsername = revisionContributorUsername;
    }


    // -- Getters


    /**
     * Gets the page title.
     *
     * @return
     *     The page title.
     */
    public String getTitle() {
        return title;
    }


    /**
     * Gets the page revision contributor username.
     *
     * @return
     *     The page revision contributor username.
     */
    public String getRevisionContributorUsername() {
        return revisionContributorUsername;
    }

}
