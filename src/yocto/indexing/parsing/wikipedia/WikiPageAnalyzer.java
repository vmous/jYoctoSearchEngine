package yocto.indexing.parsing.wikipedia;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * A utility class offering trivial text analysis.
 *
 * @author billy
 */
public class WikiPageAnalyzer {

    public static final Pattern TOKENIZR = Pattern.compile("[\\W]+", Pattern.UNICODE_CHARACTER_CLASS);
//    public static final Pattern TOKENIZR = Pattern.compile("[\\P{L}]+");

    private static final Pattern WIKI_P0 = Pattern.compile("&lt;!--.*?--&gt;", Pattern.DOTALL);
    private static final Pattern WIKI_P1 = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern WIKI_P2 = Pattern.compile("<math([> ].*?)(</math>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P3 = Pattern.compile("(?s)<ref([> ].*?)(</ref>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P4 = Pattern.compile("(?s)<sup([> ].*?)(</sup>|/>)", Pattern.DOTALL);
    private static final Pattern WIKI_P5 = Pattern.compile("(?s)<blockquote>(.*?)</blockquote>", Pattern.DOTALL);
    private static final Pattern WIKI_P6 = Pattern.compile("&nbsp;", Pattern.DOTALL);


    private static final Pattern WIKI_P7 = Pattern.compile("[=]+", Pattern.DOTALL);
//    private static final Pattern WIKI_P8 = Pattern.compile("[\\s]*\\*(.*?)", Pattern.DOTALL);
//    private static final Pattern WIKI_P9 = Pattern.compile("\\[\\[[iI]mage(.*?)(\\|.*?)*\\|(.*?)\\]\\]", Pattern.DOTALL);
//    private static final Pattern WIKI_P10 = Pattern.compile("\\[\\[[fF]ile(.*?)(\\|.*?)*\\|(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P11 = Pattern.compile("\\[\\[category:(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P12 = Pattern.compile("(?s)\\{\\{redirect\\|(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P13 = Pattern.compile("(?s)\\{\\{cite(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P14 = Pattern.compile("(?s)\\{\\|(.*?)\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P15 = Pattern.compile("\\'+", Pattern.DOTALL);
    private static final Pattern WIKI_P16 = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P17 = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P18 = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern WIKI_P19 = Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
    private static final Pattern WIKI_P20 = Pattern.compile("(?s)\\{\\{(.*?)\\}\\}", Pattern.DOTALL);

    private static final Pattern WIKI_P21 = Pattern.compile("\\d+", Pattern.DOTALL);
    private static final Pattern WIKI_P22 = Pattern.compile("&gt;", Pattern.DOTALL);
    private static final Pattern WIKI_P23 = Pattern.compile("&lt;", Pattern.DOTALL);
    private static final Pattern WIKI_P24 = Pattern.compile("_+", Pattern.DOTALL);


    /**
     * Stopwords.
     */
    @SuppressWarnings("serial")
    private static final HashSet<String> stopwords = new HashSet<String>(){{
        add("a");
        add("a's");
        add("able");
        add("about");
        add("above");
        add("according");
        add("accordingly");
        add("across");
        add("actually");
        add("after");
        add("afterwards");
        add("again");
        add("against");
        add("ain't");
        add("all");
        add("allow");
        add("allows");
        add("almost");
        add("alone");
        add("along");
        add("already");
        add("also");
        add("although");
        add("always");
        add("am");
        add("among");
        add("amongst");
        add("an");
        add("and");
        add("another");
        add("any");
        add("anybody");
        add("anyhow");
        add("anyone");
        add("anything");
        add("anyway");
        add("anyways");
        add("anywhere");
        add("apart");
        add("appear");
        add("appreciate");
        add("appropriate");
        add("are");
        add("aren't");
        add("around");
        add("as");
        add("aside");
        add("ask");
        add("asking");
        add("associated");
        add("at");
        add("available");
        add("away");
        add("awfully");
        add("b");
        add("be");
        add("became");
        add("because");
        add("become");
        add("becomes");
        add("becoming");
        add("been");
        add("before");
        add("beforehand");
        add("behind");
        add("being");
        add("believe");
        add("below");
        add("beside");
        add("besides");
        add("best");
        add("better");
        add("between");
        add("beyond");
        add("both");
        add("brief");
        add("but");
        add("by");
        add("c");
        add("c'mon");
        add("c's");
        add("came");
        add("can");
        add("can't");
        add("cannot");
        add("cant");
        add("cause");
        add("causes");
        add("certain");
        add("certainly");
        add("changes");
        add("clearly");
        add("co");
        add("com");
        add("come");
        add("comes");
        add("concerning");
        add("consequently");
        add("consider");
        add("considering");
        add("contain");
        add("containing");
        add("contains");
        add("corresponding");
        add("could");
        add("couldn't");
        add("course");
        add("currently");
        add("d");
        add("definitely");
        add("described");
        add("despite");
        add("did");
        add("didn't");
        add("different");
        add("do");
        add("does");
        add("doesn't");
        add("doing");
        add("don't");
        add("done");
        add("down");
        add("downwards");
        add("during");
        add("e");
        add("each");
        add("edu");
        add("eg");
        add("eight");
        add("either");
        add("else");
        add("elsewhere");
        add("enough");
        add("entirely");
        add("especially");
        add("et");
        add("etc");
        add("even");
        add("ever");
        add("every");
        add("everybody");
        add("everyone");
        add("everything");
        add("everywhere");
        add("ex");
        add("exactly");
        add("example");
        add("except");
        add("f");
        add("far");
        add("few");
        add("fifth");
        add("first");
        add("five");
        add("followed");
        add("following");
        add("follows");
        add("for");
        add("former");
        add("formerly");
        add("forth");
        add("four");
        add("from");
        add("further");
        add("furthermore");
        add("g");
        add("get");
        add("gets");
        add("getting");
        add("given");
        add("gives");
        add("go");
        add("goes");
        add("going");
        add("gone");
        add("got");
        add("gotten");
        add("greetings");
        add("h");
        add("had");
        add("hadn't");
        add("happens");
        add("hardly");
        add("has");
        add("hasn't");
        add("have");
        add("haven't");
        add("having");
        add("he");
        add("he's");
        add("hello");
        add("help");
        add("hence");
        add("her");
        add("here");
        add("here's");
        add("hereafter");
        add("hereby");
        add("herein");
        add("hereupon");
        add("hers");
        add("herself");
        add("hi");
        add("him");
        add("himsel");
        add("his");
        add("hither");
        add("hopefully");
        add("how");
        add("howbeit");
        add("however");
        add("i");
        add("i'd");
        add("i'll");
        add("i'm");
        add("i've");
        add("ie");
        add("if");
        add("ignored");
        add("immediate");
        add("in");
        add("inasmuch");
        add("inc");
        add("indeed");
        add("indicate");
        add("indicated");
        add("indicates");
        add("inner");
        add("insofar");
        add("instead");
        add("into");
        add("inward");
        add("is");
        add("isn't");
        add("it");
        add("it'd");
        add("it'll");
        add("it's");
        add("its");
        add("itself");
        add("j");
        add("just");
        add("k");
        add("keep");
        add("keeps");
        add("kept");
        add("know");
        add("knows");
        add("known");
        add("l");
        add("last");
        add("lately");
        add("later");
        add("latter");
        add("latterly");
        add("least");
        add("less");
        add("lest");
        add("let");
        add("let's");
        add("like");
        add("liked");
        add("likely");
        add("little");
        add("look");
        add("looking");
        add("looks");
        add("ltd");
        add("m");
        add("mainly");
        add("many");
        add("may");
        add("maybe");
        add("me");
        add("mean");
        add("meanwhile");
        add("merely");
        add("might");
        add("more");
        add("moreover");
        add("most");
        add("mostly");
        add("much");
        add("must");
        add("my");
        add("myself");
        add("n");
        add("name");
        add("namely");
        add("nd");
        add("near");
        add("nearly");
        add("necessary");
        add("need");
        add("needs");
        add("neither");
        add("never");
        add("nevertheless");
        add("new");
        add("next");
        add("nine");
        add("no");
        add("nobody");
        add("non");
        add("none");
        add("noone");
        add("nor");
        add("normally");
        add("not");
        add("nothing");
        add("novel");
        add("now");
        add("nowhere");
        add("o");
        add("obviously");
        add("of");
        add("off");
        add("often");
        add("oh");
        add("ok");
        add("okay");
        add("old");
        add("on");
        add("once");
        add("one");
        add("ones");
        add("only");
        add("onto");
        add("or");
        add("other");
        add("others");
        add("otherwise");
        add("ought");
        add("our");
        add("ours");
        add("ourselves");
        add("out");
        add("outside");
        add("over");
        add("overall");
        add("own");
        add("p");
        add("particular");
        add("particularly");
        add("per");
        add("perhaps");
        add("placed");
        add("please");
        add("plus");
        add("possible");
        add("presumably");
        add("probably");
        add("provides");
        add("q");
        add("que");
        add("quite");
        add("qv");
        add("r");
        add("rather");
        add("rd");
        add("re");
        add("really");
        add("reasonably");
        add("regarding");
        add("regardless");
        add("regards");
        add("relatively");
        add("respectively");
        add("right");
        add("s");
        add("said");
        add("same");
        add("saw");
        add("say");
        add("saying");
        add("says");
        add("second");
        add("secondly");
        add("see");
        add("seeing");
        add("seem");
        add("seemed");
        add("seeming");
        add("seems");
        add("seen");
        add("self");
        add("selves");
        add("sensible");
        add("sent");
        add("serious");
        add("seriously");
        add("seven");
        add("several");
        add("shall");
        add("she");
        add("should");
        add("shouldn't");
        add("since");
        add("six");
        add("so");
        add("some");
        add("somebody");
        add("somehow");
        add("someone");
        add("something");
        add("sometime");
        add("sometimes");
        add("somewhat");
        add("somewhere");
        add("soon");
        add("sorry");
        add("specified");
        add("specify");
        add("specifying");
        add("still");
        add("sub");
        add("such");
        add("sup");
        add("sure");
        add("t");
        add("t's");
        add("take");
        add("taken");
        add("tell");
        add("tends");
        add("th");
        add("than");
        add("thank");
        add("thanks");
        add("thanx");
        add("that");
        add("that's");
        add("thats");
        add("the");
        add("their");
        add("theirs");
        add("them");
        add("themselves");
        add("then");
        add("thence");
        add("there");
        add("there's");
        add("thereafter");
        add("thereby");
        add("therefore");
        add("therein");
        add("theres");
        add("thereupon");
        add("these");
        add("they");
        add("they'd");
        add("they'll");
        add("they're");
        add("they've");
        add("think");
        add("third");
        add("this");
        add("thorough");
        add("thoroughly");
        add("those");
        add("though");
        add("three");
        add("through");
        add("throughout");
        add("thru");
        add("thus");
        add("to");
        add("together");
        add("too");
        add("took");
        add("toward");
        add("towards");
        add("tried");
        add("tries");
        add("truly");
        add("try");
        add("trying");
        add("twice");
        add("two");
        add("u");
        add("un");
        add("under");
        add("unfortunately");
        add("unless");
        add("unlikely");
        add("until");
        add("unto");
        add("up");
        add("upon");
        add("us");
        add("use");
        add("used");
        add("useful");
        add("uses");
        add("using");
        add("usually");
        add("uucp");
        add("v");
        add("value");
        add("various");
        add("very");
        add("via");
        add("viz");
        add("vs");
        add("w");
        add("want");
        add("wants");
        add("was");
        add("wasn't");
        add("way");
        add("we");
        add("we'd");
        add("we'll");
        add("we're");
        add("we've");
        add("welcome");
        add("well");
        add("went");
        add("were");
        add("weren't");
        add("what");
        add("what's");
        add("whatever");
        add("when");
        add("whence");
        add("whenever");
        add("where");
        add("where's");
        add("whereafter");
        add("whereas");
        add("whereby");
        add("wherein");
        add("whereupon");
        add("wherever");
        add("whether");
        add("which");
        add("while");
        add("whither");
        add("who");
        add("who's");
        add("whoever");
        add("whole");
        add("whom");
        add("whose");
        add("why");
        add("will");
        add("willing");
        add("wish");
        add("with");
        add("within");
        add("without");
        add("won't");
        add("wonder");
        add("would");
        add("would");
        add("wouldn't");
        add("x");
        add("y");
        add("yes");
        add("yet");
        add("you");
        add("you'd");
        add("you'll");
        add("you're");
        add("you've");
        add("your");
        add("yours");
        add("yourself");
        add("yourselves");
        add("z");
        add("zero");
    }};


    /**
     * Constructor.
     */
    private WikiPageAnalyzer() {
    }


    /**
     * Gets the stopwords loaded.
     *
     * @return
     *     A set of stopwords.
     */
    public static HashSet<String> getStopwords() {
        return stopwords;
    }


    /**
     * Tokenizes a string.
     *
     * @param normalizedPageRevisionText
     *     The string to be tokenized. It is recommended that this string is
     *     first normalized before doing this for better results.
     * @param stopwords
     *     A set of stopwords to be ignored. Pass {@code null} in order not to
     *     ignore any word.
     *
     * @return
     *     A bag of words, as a set (no duplicates) stripped from stopwords.
     */
    public static HashSet<String> tokenizePageRevisionText(
            String normalizedPageRevisionText,
            HashSet<String> stopwords) {
        HashSet<String> bagOfWords = new HashSet<String>();

        // Simple tokenization on non-alphanumeric Unicode characters.
        String[] tokens = TOKENIZR.split(normalizedPageRevisionText);

        for (String token : tokens) {
            if (!token.equals("")) {
                if (stopwords != null) {
                    if (!stopwords.contains(token)) {
                        bagOfWords.add(token);
                    }
                }
                else {
                    bagOfWords.add(token);
                }
            }
        }

        return bagOfWords;
    }


    /**
     * Normalizes a string.
     * <ul>
     * Basic steps are:
     *   <li>lower cases the entire string,</li>
     *   <li>removes Wikipedia markup,</li>
     *   <li>removes other tags (some HTML)</li>
     *   <li>removes numbers</li>
     * </ul>
     *
     * @param rawPageRevisionText
     *     The string to be normalized (most of the time in raw format with
     *     markup - HTML, Wiki e.t.c.).
     *
     * @return
     *     A normalized version of the given string.
     */
    public static String normalizePlainPageRevisionText(String rawPageRevisionText) {
        // TODO Needs refinement! Some pattern matches where very heavy
        // computationally.

        rawPageRevisionText = rawPageRevisionText.toLowerCase();

        rawPageRevisionText = WIKI_P0.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P1.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P2.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P3.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P4.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P5.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P6.matcher(rawPageRevisionText).replaceAll(" ");

        rawPageRevisionText = WIKI_P7.matcher(rawPageRevisionText).replaceAll(" ");
//        rawPageRevisionText = WIKI_P8.matcher(rawPageRevisionText).replaceAll("$1");
//        rawPageRevisionText = WIKI_P9.matcher(rawPageRevisionText).replaceAll("$3");
//        rawPageRevisionText = WIKI_P10.matcher(rawPageRevisionText).replaceAll("$3");
        rawPageRevisionText = WIKI_P11.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P12.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P13.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P14.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P15.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P16.matcher(rawPageRevisionText).replaceAll("$1");
        rawPageRevisionText = WIKI_P17.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P18.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P19.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P20.matcher(rawPageRevisionText).replaceAll(" ");

        rawPageRevisionText = WIKI_P21.matcher(rawPageRevisionText).replaceAll(" ");
        rawPageRevisionText = WIKI_P22.matcher(rawPageRevisionText).replaceAll(">");
        rawPageRevisionText = WIKI_P23.matcher(rawPageRevisionText).replaceAll("<");
        rawPageRevisionText = WIKI_P24.matcher(rawPageRevisionText).replaceAll(" ");

        return rawPageRevisionText;
    }

}
