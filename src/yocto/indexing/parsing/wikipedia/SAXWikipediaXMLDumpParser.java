package yocto.indexing.parsing.wikipedia;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * A concrete implementation of a Wikipedia XML dump parser based on SAX.
 *
 * @see {@link AbstractWikipediaXMLDumpParser}
 *
 * @author billy
 */
public class SAXWikipediaXMLDumpParser extends AbstractWikipediaXMLDumpParser {

    /**
     * Constructor.
     *
     * @param pathname
     *     The pathname of the Wikipedia XML dump file.
     */
    public SAXWikipediaXMLDumpParser(String pathname) {
        super(pathname);
    }


    /* (non-Javadoc)
     * @see yocto.indexing.parsing.XMLDumpParser#parse()
     */
    @Override
    public List<WikiPage> parse() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXWikipediaXMLDumpHandler handler = null;
        try {
            SAXParser parser = factory.newSAXParser();
            handler = new SAXWikipediaXMLDumpHandler();
            parser.parse(getInputStream(), handler);
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        catch (SAXException saxe) {
            saxe.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return handler.getPages();
    }

}
