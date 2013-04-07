package yocto.indexing.parsing;

import java.util.List;

/**
 * Interface of a generic XML parser for large semi-structured datasets.
 *
 * @author billy
 */
public interface XMLDumpParser {

    /**
     * Parses the XML dump file.
     */
    public abstract List<? extends Object> parse();

}
