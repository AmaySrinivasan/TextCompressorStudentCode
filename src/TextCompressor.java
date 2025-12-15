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
 * The {@code TextCompressor} class provides static methods for compressing
 * and expanding natural language through textfile input.
 *
 * @author Zach Blick, Amay Srinivasan
 */
public class TextCompressor {
    // Number of ASCII characters
    private static final int R = 128;
    // End of File marker at hex 0x80
    private static final int EOF = 128;
    // Total number of available codeword for 8-bit size
    private static final int L = 256;
    // Width of each codeword in number of bits
    private static final int W = 8;

    // Compress the input using LZW and writes encoded bitsream to ouput
    private static void compress() {
        // Reads the entire inputted text into memory
        String input = BinaryStdIn.readString();
        // Using a TST dictionary mapping strings to codewords, initializing with all single character strings in ASCII
        TST tst = new TST();
        for (int i = 0; i < R; i++) {
            tst.insert("" + (char) i, i);
        }
        // Next available dictionary code (starting after EOF) (129)
        int nextCode = R + 1;
        int index = 0;
        while (index < input.length()) {
            // Finds longest prefix of input starting at index existing in the dictionary, then writes matching codeword
            String prefix = tst.getLongestPrefix(input, index);
            int code = tst.lookup(prefix);
            BinaryStdOut.write(code, W);
            // If possible, adds new dictionary entry with prefix + next character in the input
            if (index + prefix.length() < input.length() && nextCode < L) {
                String newEntry = prefix + input.charAt(index + prefix.length());
                tst.insert(newEntry, nextCode++);
            }
            // Moves forward by the length of the matched prefix
            index += prefix.length();
        }
        // WRites the End of File code so that it knows when to stop
        BinaryStdOut.write(EOF, W);
        BinaryStdOut.close();

    }

    // Expands LZW compressed stream that was inputted and writes original text to output
    private static void expand() {
        // Dictionary maps codewords to strings, intializing with single-character ASCII strings
        String[] table = new String[L];
        for (int i = 0; i < R; i++) {
            table[i] = "" + (char) i;
        }
        // Next available code at 129 (after the ASCIIs)
        int nextCode = R + 1;
        // Reads first codeword and if the file is empty, just exist immediatley
        int codeWord = BinaryStdIn.readInt(W);
        if (codeWord == EOF) {
            return;
        }
        // The current decoded string
        String val = table[codeWord];
        while (true) {
            // Outputs current decoded string and reads the next codeword
            BinaryStdOut.write(val);
            codeWord = BinaryStdIn.readInt(W);
            // Stops or Breaks when it reaches the End of File
            if (codeWord == EOF) {
                break;
            }
            String entry;
            // If the codewords exists in the dictionary, get its associated string
            if (table[codeWord] != null) {
                entry = table[codeWord];
            }
            // Edge case for LZW where the codeword is the next one to be defined
            else {
                entry = val + val.charAt(0);
            }
            // Add new dictionary entry if there is space
            if (nextCode < L) {
                table[nextCode++] = val + entry.charAt(0);
            }
            // Moves to the next decoded string
            val = entry;
        }
        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
