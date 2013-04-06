package yocto.indexing.parsing.wikipedia;

import yocto.indexing.parsing.AbstractXMLDumpParser;

/**
 * An abstract extension of the {@code AbstractXMLDumpParser} defining
 * Wikipedia XML dump file specifics that can be shared by concrete
 * implementations (i.e. DOM-based or stream-based).
 *
 * @see {@link AbstractXMLDumpParser}
 *
 * @author billy
 */
public abstract class AbstractWikipediaXMLDumpParser
        extends AbstractXMLDumpParser {

    // Basic XML tags for Wikipedia's XML dumps. For more info check
    // http://www.mediawiki.org/xml/export-0.8.xsd

    /** Page element tag */
    public static final String TAG_PAGE = "page";


    /** Page title element tag */
    public static final String TAG_PAGE_TITLE = "title";


    /** Page namespace element tag */
    public static final String TAG_PAGE_NS = "ns";


    /** Page identification element tag */
    public static final String TAG_PAGE_ID = "id";


    /** Page redirect element tag */
    public static final String TAG_PAGE_REDIRECT = "redirect";


    /** Page revision element tag */
    public static final String TAG_PAGE_REVISION = "revision";


    /** Page revision identification element tag */
    public static final String TAG_PAGE_REVISION_ID = "id";


    /** Page revision parent identification element tag */
    public static final String TAG_PAGE_REVISION_PARENTID = "parentid";


    /** Page revision timestamp element tag */
    public static final String TAG_PAGE_REVISION_TIMESTAMP = "timestamp";


    /** Page revision contributor element tag */
    public static final String TAG_PAGE_REVISION_CONTRIBUTOR = "contributor";


    /** Page revision contributor user name element tag */
    public static final String TAG_PAGE_REVISION_CONTRIBUTOR_USERNAME = "username";


    /** Page revision contributor identification element tag */
    public static final String TAG_PAGE_REVISION_CONTRIBUTOR_ID = "id";


    /** Page revision comment element tag */
    public static final String TAG_PAGE_REVISION_COMMENT = "comment";


    /** Page revision text (main body) element tag */
    public static final String TAG_PAGE_REVISION_TEXT = "text";


    /** Page revision SHA1 element tag */
    public static final String TAG_PAGE_REVISION_SHA1 = "sha1";


    /** Page revision model element tag */
    public static final String TAG_PAGE_REVISION_MODEL = "model";


    /** Page revision format element tag */
    public static final String TAG_PAGE_REVISION_FORMAT = "format";


    /**
     * Constructor.
     *
     * @param pathname
     *     The pathname of the Wikipedia XML dump file.
     */
    public AbstractWikipediaXMLDumpParser(String pathname) {
        super(pathname);
    }

}
