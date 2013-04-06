package yocto.indexing.parsing;

/**
 * Interface of a generic XML parser for large semi-structured datasets.
 *
 * @author billy
 */
public interface XMLDumpParser {

    /**
     * Parses the XML dump file.
     */
    public abstract void parse();

}
