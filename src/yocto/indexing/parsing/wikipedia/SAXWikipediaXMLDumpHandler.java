package yocto.indexing.parsing.wikipedia;

import static yocto.indexing.parsing.wikipedia.AbstractWikipediaXMLDumpParser.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import yocto.indexing.Indexer;
import yocto.storage.DiskManager;

/**
 * The hanlder that handles the parsing of a Wikipedia XML dump file.
 *
 * @see {@link SAXWikipediaXMLDumpParser}
 *
 * @author billy
 */
public class SAXWikipediaXMLDumpHandler extends DefaultHandler {

    private Indexer indexer;

    /** The currently manipulated Wikipedia article page. */
    private WikiPage page;

    /** The currently manipulated element tag. */
    private String tag;

    /** A builder for a page title. */
    private StringBuilder pageTitle;

    /** A builder for a page id. */
    private StringBuilder pageId;

    /** A builder for a page revision contributor username. */
    private StringBuilder pageRevisionContributorUsername;

    /** A builder for a page revision text. */
    private StringBuilder pageRevisionText;


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);

        if (tag.equalsIgnoreCase(TAG_PAGE_TITLE)) {
            pageTitle.append(ch, start, length);
        }
        else if (tag.equalsIgnoreCase(TAG_PAGE_ID)) {
            // Append only the page id and ignore page revision id and
            // page revision contributor id.
            if (pageId.length() == 0) {
                pageId.append(ch, start, length);
            }
        }
        else if (tag.equalsIgnoreCase(TAG_PAGE_REVISION_CONTRIBUTOR_USERNAME)) {
            pageRevisionContributorUsername.append(ch, start, length);
        }
        else if (tag.equalsIgnoreCase(TAG_PAGE_REVISION_TEXT)) {
            pageRevisionText.append(ch, start, length);
        }
    }


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        indexer = new Indexer(new DiskManager("./index"), 20000);

        // Created once, used many times...
        // Do not forget to delete( ) at the end of each page element!
        pageTitle = new StringBuilder();
        pageId = new StringBuilder();
        pageRevisionContributorUsername = new StringBuilder();
        pageRevisionText = new StringBuilder();
    }


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();

        indexer.close();
    }


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        tag = qName;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equalsIgnoreCase(TAG_PAGE)) {
            // Flush the parsed content to the wiki page object...
            page = new WikiPage(
                    pageTitle.toString(),
                    Long.parseLong(pageId.toString()),
                    pageRevisionContributorUsername.toString(),
                    pageRevisionText.toString());

            indexer.addDocument(page);
//            System.out.println("JVM memory (free / total): " + Runtime.getRuntime().freeMemory() + " / "
//                    + Runtime.getRuntime().totalMemory());

            // ...and clear the string builders for reuse.
            pageTitle.delete(0, pageTitle.length());
            pageId.delete(0, pageId.length());
            pageRevisionContributorUsername.delete(0,
                    pageRevisionContributorUsername.length());
            pageRevisionText.delete(0, pageRevisionText.length());

//            System.out.println(page.peek() + "\n\n");
        }
    }

}
