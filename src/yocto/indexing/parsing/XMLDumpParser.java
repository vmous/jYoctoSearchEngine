package yocto.indexing.parsing;

import java.util.List;

import yocto.indexing.Document;

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
    public abstract List<? extends Document> parse();

}
