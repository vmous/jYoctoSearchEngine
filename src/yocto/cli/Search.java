package yocto.cli;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import yocto.searching.Hit;
import yocto.searching.Searcher;

/**
 * A simple testing CLI for testing the searching infrastructure.
 *
 * @author billy
 */
public class Search {

    /* The version of the crawler. */
    private static final String VERSION =
            "1.0";

    /* The author of the crawler. */
    private static final String strAppAuthor =
            "Vassilis S. Moustakas (vsmoustakas[at]gmail[dot]com)";

    /* The application name. */
    private static final String strAppName =
            "Yocto Search Engine v." + VERSION + " (Search Module)";

    /* The application header. */
    private static final String strAppHeader = strAppName
            + "\nA tiny, yet functional, search engine."
            + "\nAuthor: " + strAppAuthor;


    /**
     * The main function that drives the execution.
     *
     * @param args
     *     The command-line arguments.
     */
    public static void main(String[] args) {
        boolean exit = false;

        System.out.println("");
        System.out.println(strAppHeader);

        System.out.println("");
        System.out.print("Initializing... ");
        Searcher s;
        try {
            s = new Searcher(args[0]);
            System.out.println("Done");
            System.out.println("");

            // Get input.
            Scanner scanner = new Scanner(System.in);

            while(!exit) {
                //  prompt the user to enter their name
                System.out.print("Yocto Search # ");

                // there are several ways to get the input, this is
                // just one approach
                String query = scanner.nextLine().trim();

                if (!query.equals("")) {
                    if(query.equals("q!")) {
                        System.out.println("Goodbye!");
                        exit = true;
                    }
                    else {
                        long startTime = System.nanoTime();
                        List<Hit> hits = s.searchQuery(query);
                        long elapsedTime = System.nanoTime() - startTime;
                        System.out.println("Results for \"" + query + "\" "
                                +"(" + hits.size()
                                + " documents in " + TimeUnit.MILLISECONDS.convert(elapsedTime,
                                        TimeUnit.NANOSECONDS) + "ms)");

                        if (hits.size() > 0) {
                            Iterator<Hit> iter = hits.iterator();
                            while(iter.hasNext()) {
                                System.out.println("  " + iter.next().getResource());
                            }
                        }
                        else {
                            if (!query.startsWith("author:"))
                                System.out.println("You searched for knowledge not discovered yet... Start writing that paper then :-)");
                        }
                        System.out.println();
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Failed! Aborting...");
            System.out.println("");
            e.printStackTrace();
        }
    }

}
