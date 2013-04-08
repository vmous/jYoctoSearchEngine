package yocto.indexing.parsing;

/**
 * Interface of a generic XML parser for large semi-structured datasets.
 *
 * @author billy
 */
public interface XMLDumpParser {

    /**
     * Parses the XML dump file.
     *
     * @return
     *     A list of indexable documents.
     */
    public abstract void parse();

}
