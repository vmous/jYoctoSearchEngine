package yocto.indexing;

import java.lang.reflect.Field;

/**
 * An abstraction of an indexable document.
 *
 * @author billy
 */
public abstract class Document {

    /** The document id. */
    private final long id;


    /**
     * Constructor.
     *
     * @param id
     *     The document id;
     */
    public Document(long id) {
        this.id = id;
    }


    /**
     * Gets the document id.
     *
     * @return
     *     The document id.
     */
    public long getId() {
        return id;
    }


    /**
     * Peek inside the {@code WikiPage} object to see its contents in a
     * human readable format.
     *
     * @return
     *     A {@code String} representation of the {@code Document}.
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
              field.setAccessible(true);
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
