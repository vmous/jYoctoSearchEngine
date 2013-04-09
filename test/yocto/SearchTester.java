package yocto;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import yocto.searching.Searcher;

public class SearchTester {

    /**
     * The version of the crawler.
     */
    private static final String VERSION =
            "0.0";

    /**
     * The author of the crawler.
     */
    private static final String strAppAuthor =
            "Vassilis S. Moustakas (vsmoustakas[at]gmail[dot]com)";

    /**
     * The application name.
     */
    private static final String strAppName =
            "Yocto Search Engine v." + VERSION;

    /**
     * The application header.
     */
    private static final String strAppHeader = strAppName
            + "\nA tiny, yet functional, search engine."
            + "\nAuthor: " + strAppAuthor;


    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean exit = false;

        System.out.println("");
        System.out.println(strAppHeader);

        System.out.println("");
        System.out.print("Initializing... ");
        Searcher s;
        try {
            s = new Searcher("./seg.i.0", "./seg.0");
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
                        List<String> results = s.searchQuery(query);
                        long elapsedTime = System.nanoTime() - startTime;
                        System.out.println("Results for \"" + query + "\" "
                                +"(" + results.size()
                                + " documents in " + TimeUnit.MILLISECONDS.convert(elapsedTime,
                                        TimeUnit.NANOSECONDS) + "ms)");

                        Iterator<String> iter = results.iterator();
                        while(iter.hasNext()) {
                            System.out.println("  " + iter.next());
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
