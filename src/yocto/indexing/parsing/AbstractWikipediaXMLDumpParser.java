package yocto.indexing.parsing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public abstract class AbstractWikipediaXMLDumpParser implements WikipediaXMLDumpParser {

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


    /** The filename of the Wikipedia XML dump file. */
    private final String pathname;


    /**
     * Constructor.
     *
     * @param filename
     *     The pathname of the Wikipedia XML dump file.
     */
    public AbstractWikipediaXMLDumpParser(String pathname) {
        this.pathname = pathname;
    }


    /**
     * Gets the input stream from the path name of the Wikipedia XML dump file.
     *
     * It also checks the file name suffix and decorates the stream accordingly
     * before returning it to the caller. Types of archive currently supported
     * are GZip, BZ2 and ZIP.
     *
     * @return
     *     The input stream from the Wikipedia XML dump file.
     *
     * @throws IOException
     */
    protected InputStream getInputStream() throws IOException {
        InputStream is = new FileInputStream(pathname);

        if (pathname.endsWith(".gz")) {
            is = new GZIPInputStream(is);
        }
        else if (pathname.endsWith(".bz2")) {
            is = new BZip2CompressorInputStream(is);
        }
        else if (pathname.endsWith(".zip")) {
            is = new ZipInputStream(is);
        }

        return is;
    }
}
