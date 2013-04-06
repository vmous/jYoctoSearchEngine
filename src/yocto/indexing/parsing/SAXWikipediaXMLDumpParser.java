package yocto.indexing.parsing;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

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
     * @see yocto.indexing.parsing.WikipediaXMLDumpParser#parse()
     */
    @Override
    public void parse() {
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

//        return handler.getWebPages();
    }

}
