package yocto;

//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.IntBuffer;
//import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import yocto.indexing.parsing.wikipedia.SAXWikipediaXMLDumpParser;

/**
 * A simple testing class for CMD-line testing.
 *
 * @author billy
 */
public class IndexingTest {

    /**
     * The main function that drives the execution.
     *
     * @param args
     *     The command-line arguments.
     */
    public static void main(String[] args) {
        final String WIKIPEDIA_DUMP_XML = args[0];
        long startTime;
        long elapsedTime;

        System.out.println("Extracting and indexing documents from Wikipedia XML dump file: " + WIKIPEDIA_DUMP_XML + "\n");
        SAXWikipediaXMLDumpParser parser = new SAXWikipediaXMLDumpParser(WIKIPEDIA_DUMP_XML);
        startTime = System.nanoTime();
        parser.parse();
        elapsedTime = System.nanoTime() - startTime;
        System.out.println("\nTotal excecution time: " +
                TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " seconds");
    }





//    private static int numOfInts = 4000000;
//
//    private static int numOfUbuffInts = 200000;
//
//    private abstract static class Tester {
//        private final String name;
//
//        public Tester(String name) {
//            this.name = name;
//        }
//
//        public long runTest() {
//            System.out.print(name + ": ");
//            try {
//                long startTime = System.currentTimeMillis();
//                test();
//                long endTime = System.currentTimeMillis();
//                return (endTime - startTime);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        public abstract void test() throws IOException;
//    }
//
//    private static Tester[] tests = {
//        new Tester("Stream Write") {
//            @Override
//            public void test() throws IOException {
//                java.io.DataOutputStream dos = new java.io.DataOutputStream(
//                        new java.io.BufferedOutputStream(new java.io.FileOutputStream(new File(
//                                "temp.tmp"))));
//                for (int i = 0; i < numOfInts; i++)
//                    dos.writeInt(i);
//                dos.close();
//            }
//        },
//        new Tester("Mapped Write") {
//            @Override
//            public void test() throws IOException {
//                FileChannel fc = new RandomAccessFile("temp.tmp", "rw").getChannel();
//                IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size()).asIntBuffer();
//                for (int i = 0; i < numOfInts; i++)
//                    ib.put(i);
//                fc.close();
//            }
//        },
//        new Tester("Stream Read") {
//            @Override
//            public void test() throws IOException {
//                java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.BufferedInputStream(
//                        new java.io.FileInputStream("temp.tmp")));
//                for (int i = 0; i < numOfInts; i++)
//                    dis.readInt();
//                dis.close();
//            }
//        },
//        new Tester("Mapped Read") {
//            @Override
//            public void test() throws IOException {
//                FileChannel fc = new java.io.FileInputStream(new File("temp.tmp")).getChannel();
//                IntBuffer ib = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).asIntBuffer();
//                while (ib.hasRemaining())
//                    ib.get();
//                fc.close();
//            }
//        },
//        new Tester("Stream Read/Write") {
//            @Override
//            public void test() throws IOException {
//                RandomAccessFile raf = new RandomAccessFile(new File("temp.tmp"),"rw");
//                raf.writeInt(1);
//                for (int i = 0; i < numOfUbuffInts; i++) {
//                    raf.seek(raf.length() - 4);
//                    raf.writeInt(raf.readInt());
//                }
//                raf.close();
//            }
//        },
//        new Tester("Mapped Read/Write") {
//            @Override
//            public void test() throws IOException {
//                FileChannel fc = new RandomAccessFile(new File("temp.tmp"), "rw").getChannel();
//                IntBuffer ib = fc.map(FileChannel.MapMode.READ_WRITE, 0, fc.size()).asIntBuffer();
//                ib.put(0);
//                for (int i = 1; i < numOfUbuffInts; i++)
//                    ib.put(ib.get(i - 1));
//                fc.close();
//            }
//        }
//    };
//
//    public static void main(String[] args) {
//      for (int i = 0; i < tests.length; i++)
//        System.out.println(tests[i].runTest());
//    }

}
