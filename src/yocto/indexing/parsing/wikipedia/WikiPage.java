package yocto.indexing.parsing.wikipedia;

import java.lang.reflect.Field;

/**
 * An abstraction of a Wikipedia article page.
 *
 * Currently immutable.
 *
 * @author billy
 */
public class WikiPage {

    /** The Wikipedia article page title. */
    private final String title;

    /** The Wikipedia article page id. */
    private final String id;

    /** The Wikipedia article page revision contributor username. */
    private final String revisionContributorUsername;

    /** The Wikipedia article page revision text. */
    private final String revisionText;


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
    public WikiPage(String title, String id,
            String revisionContributorUsername, String revisionText) {
        this.title = title.trim();
        this.id = id.trim();
        this.revisionContributorUsername = revisionContributorUsername.trim();
        this.revisionText = revisionText.trim();

//        System.out.println("Object for Wiki page \"" + this.title + "\" created!");
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
     * Gets the page id.
     *
     * @return
     *     The page id.
     */
    public String getId() {
        return id;
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


    /**
     * Gets the page revision text.
     *
     * @return
     *     The page revision text.
     */
    public String getRevisionText() {
        return revisionText;
    }


    /**
     * Peek inside the {@code WikiPage} object to see its contents in a
     * human readable format.
     *
     * @return
     *     A {@code String} representation of the {@code WikiPage}.
     */
    public String peek() {
        StringBuilder sb = new StringBuilder();
        final String NEW_LINE = System.getProperty("line.separator");

        sb.append( this.getClass().getName() );
        sb.append( " Object {" );
        sb.append(NEW_LINE);

        // Get a reflection of class' fields.
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
          sb.append("  ");
          try {
              sb.append( field.getName() );
              sb.append(": ");
              sb.append( field.get(this) );
          }
          catch ( IllegalAccessException ex ) {
            System.out.println(ex);
          }
          sb.append(NEW_LINE);
        }
        sb.append("}");

        return sb.toString();
    }

}
