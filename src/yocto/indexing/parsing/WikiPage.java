package yocto.indexing.parsing;

import java.lang.reflect.Field;

public class WikiPage {

    private final String title;

    private final String id;

    private final String revisionContributorUsername;

    private final String revisionText;

    public WikiPage(String title, String id,
            String revisionContributorUsername, String revisionText) {
        this.title = title.trim();
        this.id = id.trim();
        this.revisionContributorUsername = revisionContributorUsername.trim();
        this.revisionText = revisionText.trim();

//        System.out.println("Object for Wiki page \"" + this.title + "\" created!");
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the revisionContributorUsername
     */
    public String getRevisionContributorUsername() {
        return revisionContributorUsername;
    }

    /**
     * @return the revisionText
     */
    public String getRevisionText() {
        return revisionText;
    }


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
