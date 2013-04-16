package yocto.indexing.parsing;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import yocto.indexing.parsing.wikipedia.WikiPageAnalyzer;

/**
 * Unit test for the {@link WikiPageAnalyzer}.
 *
 * @author billy
 */
public class WikiPageAnalyzerTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Tests the tokenization process.
     * <ul>
     * Issues:
     *     <li>
     *     Regular expression used must be Unicode-character-aware to ensure
     *     correct splitting.
     *     </li>
     * </ul>
     */
    @Test
    public void testTokenizer() {
        String uni = "pokémon animēshon";
        String[] tokens;

        tokens = uni.split("[\\W]+");
        assertNotEquals(2, tokens.length);

        tokens = WikiPageAnalyzer.TOKENIZR.split(uni);
        assertEquals(2, tokens.length);
    }

}
