package yocto.indexing.parsing;

import static yocto.indexing.parsing.AbstractWikipediaXMLDumpParser.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXWikipediaXMLDumpHandler extends DefaultHandler {

    private WikiPage page;

    private String tag;

    private StringBuilder pageTitle;
    private StringBuilder pageId;
    private StringBuilder pageRevisionContributorUsername;
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
            pageId.append(ch, start, length);
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

        // Created once, used many times...
        // Do not forget to delete( ) at the end of each page element!
        pageTitle = new StringBuilder();
        pageId = new StringBuilder();
        pageRevisionContributorUsername = new StringBuilder();
        pageRevisionText = new StringBuilder();
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
                    pageId.toString(),
                    pageRevisionContributorUsername.toString(),
                    pageRevisionText.toString());

            // ...and clear the string builders for reuse.
            pageTitle.delete(0, pageTitle.length());
            pageId.delete(0, pageId.length());
            pageRevisionContributorUsername.delete(0,
                    pageRevisionContributorUsername.length());
            pageRevisionText.delete(0, pageRevisionText.length());
        }
    }

}
