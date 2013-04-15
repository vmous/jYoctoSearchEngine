package yocto.indexing.parsing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

//import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * An abstract class encapsulating the generic information and behaviour for
 * an XML parser.
 *
 * Leaves the specifics of the parse method to its subclasses, potentially
 * enabling a factory pattern for SAX, StaX or DOM specific implementations.
 *
 * @see {@link XMLDumpParser}
 *
 * @author billy
 */
public abstract class AbstractXMLDumpParser implements XMLDumpParser {

    /* The pathname of the XML dump file. */
    private final String pathname;


    /**
     * Constructor.
     *
     * @param pathname
     *     The pathname of the XML dump file.
     */
    public AbstractXMLDumpParser(String pathname) {
        this.pathname = pathname;
    }


    /**
     * Gets the input stream from the path name of the XML dump file.
     *
     * It also checks the file name suffix and decorates the stream accordingly
     * before returning it to the caller. Archive types currently supported are
     * GZip, BZip2 and ZIP.
     *
     * @return
     *     The input stream from the XML dump file.
     *
     * @throws IOException
     */
    protected InputStream getInputStream() throws IOException {
        InputStream is = new FileInputStream(pathname);

        if (pathname.endsWith(".gz")) {
            is = new GZIPInputStream(is);
        }
//        else if (pathname.endsWith(".bz2")) {
//            is = new BZip2CompressorInputStream(is);
//        }
        else if (pathname.endsWith(".zip")) {
            is = new ZipInputStream(is);
        }

        return is;
    }

}
