package yocto.searching;

/**
 * A successfully retrieved information.
 *
 * @author billy
 */
public class Hit {

    /* The resource associated with this hit. */
    private final String resource;


    /**
     * @param resource
     */
    public Hit(String resource) {
        this.resource = resource;
    }


    /**
     * Gets the resource associated with this hit.
     *
     * @return
     *     The resource.
     */
    public String getResource() {
        return resource;
    }

}
