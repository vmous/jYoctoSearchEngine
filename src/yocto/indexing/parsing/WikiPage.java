package yocto.indexing.parsing;

public class WikiPage {

    private final String title;

    private final String id;

    private final String revisionContributorUsername;

    private final String revisionText;

    public WikiPage(String title, String id,
            String revisionContributorUsername, String revisionText) {
//        System.out.print("Creating object for Wiki page \"" + title + "\"... ");
        this.title = title;
        this.id = id;
        this.revisionContributorUsername = revisionContributorUsername;
        this.revisionText = revisionText;
//        System.out.println("[DONE]");
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

}
