/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Amay Srinivasan
 */
public class TextCompressor {
    // Here;s what I'm thinking for the basic idea,  befroe Mr. Blick shows the answer
    // Because it is really redundant and words like the, and, said, Alice are repeated
    // And punctuation is also repeated, and characters are not equally common, like space
    // and e, a, o, are also common, they don't really need 8-bit codes.
    // I need to basically create a dictionary type of text compressor, where I think I build a
    // frequency table for chars, where i scan through it once at count occurence for each char
    // and then build code for the characters to match them
    // And then I also need to build a dictionary of the most common words
    // And create an output for each word in the dictionary, otherwise output raw characters
    private static final int TABLE_SIZE = 257;
    private static char[] keys = new char[TABLE_SIZE];
    private static int[] vals = new int[TABLE_SIZE];
    private static boolean[] used = new boolean[TABLE_SIZE];

    private static int[] bitLength = new int[TABLE_SIZE];
    private static String[] code = new String[TABLE_SIZE];
    private static int hash(char c) {
        int h = c;
        h = (h*31 + 7) & TABLE_SIZE;
        if (h<0) {
            h += TABLE_SIZE;
        }
        return h;
    }
    private static int findSlot (char c) {
        int i = hash(c);
        while (used[i] && keys[i] != c) {
            i = (i+1) % TABLE_SIZE;
        }
        return i;
    }
    private static void compress() {

        // TODO: Complete the compress() method
        String s = BinaryStdIn.readString();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int pos = findSlot(c);
            if (!used[pos]) {
                used[pos] = true;
                keys[pos] = c;
                vals[pos] = 1;
            } else {
                vals[pos]++;
            }
        }
        int count = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (used[i]) {
                count++;
            }
        }
        char[] chars = new char[count];
        int[] frew = new int[count];

        int idx = 0;
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (used[i]) {
                chars[idx] = keys[i];
                frew[idx] = vals[i];
                idx++;
            }
        }
        for (int i = 0; i < count; i++) {
            bitLength[i] = 4 + (i/10);
            if (bitLength[i] > 12) {
                bitLength[i] = 12;
            }
            String b = Integer.toBinaryString(i);
            while (b.length() < bitLength[i]) {
                b = "0" + b;
            }
            code[i] = b;
        }
        BinaryStdOut.write((char) count);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int pos = findSlot(c);
            int index = -1;
            for (int j = 0; j < count; j++) {
                if (chars[j] == c) {
                    index = j;
                    break;
                }
            }
            String bits = code[index];
            for (int k = 0; k < bits.length(); k++) {
                if (bits.charAt(k) == '1') {
                    BinaryStdOut.write(true);
                } else {
                    BinaryStdOut.write(false);
                }
            }

        }
        BinaryStdOut.close();
    }

    private static void expand() {
        // Didn't get to this
        // TODO: Complete the expand() method

        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
