import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Save original console so we can print messages safely
            PrintStream consoleOut = System.out;
            
            // 2. Create the output folder
            new File("output").mkdirs();

            // --- PROCESS GRAMMAR ---
            Grammar grammar = new Grammar();
            grammar.loadFromFile("input/grammar1.txt"); // Testing Grammar 2
            grammar.applyLeftFactoring();
            grammar.removeLeftRecursion();

            PrintStream grammarFile = new PrintStream(new FileOutputStream("output/grammar_transformed.txt"));
            System.setOut(grammarFile);
            grammar.display();
            grammarFile.close();

            FirstFollow ff = new FirstFollow(grammar);
            ff.first();
            ff.follow();

            PrintStream setsFile = new PrintStream(new FileOutputStream("output/first_follow_sets.txt"));
            System.setOut(setsFile);
            ff.displayfirst();
            ff.displayfollow();
            setsFile.close();

            // --- PROCESS PARSING TABLE ---
            Parser parser = new Parser(grammar, ff);
            parser.buildParsingTable();

            PrintStream tableFile = new PrintStream(new FileOutputStream("output/parsing_table.txt"));
            System.setOut(tableFile);
            parser.displayTable();
            if (!parser.isLL1) {
                System.out.println("\nConflict: picks first available rule");
            }
            tableFile.close();

            // --- PROCESS TRACES AND TREES ---
            System.setOut(consoleOut); // Switch back to console briefly
            System.out.println("Processing Traces and extracting Parse Trees...");

            // Open the parse_trees file so we can append all trees to it
            PrintStream treesFile = new PrintStream(new FileOutputStream("output/parse_trees.txt"));

            PrintStream trace1File = new PrintStream(new FileOutputStream("output/parsing_trace1.txt"));
            trace1File.println(">>> Valid Inputs Trace\n");
            runParserAndSplitOutput(parser, "input/input_valid.txt", trace1File, treesFile);
            trace1File.close();

            PrintStream trace2File = new PrintStream(new FileOutputStream("output/parsing_trace2.txt"));
            trace2File.println(">>> Error Recovery Trace\n");
            runParserAndSplitOutput(parser, "input/input_errors.txt", trace2File, treesFile);
            trace2File.close();

            treesFile.close();

            // 3. Final Success Message
            System.setOut(consoleOut);
            System.out.println("SUCCESS! The Traces and Trees have been successfully separated into their respective files.");

        } catch (Exception e) {
            System.err.println("Critical Error: " + e.getMessage());
        }
    }

    /**
     * Helper method: Captures parser output, splits the Trace and the Tree, 
     * and writes them to their correct files.
     */
    private static void runParserAndSplitOutput(Parser parser, String filePath, PrintStream traceStream, PrintStream treeStream) {
        File file = new File(filePath);
        if (!file.exists()) {
            traceStream.println("    [!] File not found: " + filePath);
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            // Setup an output catcher
            PrintStream originalOut = System.out;
            ByteArrayOutputStream catcher = new ByteArrayOutputStream();
            PrintStream captureStream = new PrintStream(catcher);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    // 1. Tell Java to print to our "catcher" instead of the console
                    System.setOut(captureStream);
                    parser.parseInput(line);
                    System.out.flush(); // Make sure everything is written
                    
                    // 2. Convert caught output to a string and clear the catcher for the next line
                    String output = catcher.toString();
                    catcher.reset();

                    // 3. Split the text at "--- Parse Tree ---"
                    int treeIndex = output.indexOf("--- Parse Tree ---");
                    
                    if (treeIndex != -1) {
                        // We found a tree! Split it up.
                        String tracePart = output.substring(0, treeIndex);
                        String treePart = output.substring(treeIndex);

                        // Write trace
                        traceStream.print(tracePart);
                        traceStream.println("\n" + "*".repeat(85) + "\n");

                        // Write tree
                        treeStream.println("Input String: " + line);
                        treeStream.print(treePart);
                        treeStream.println("\n" + "=".repeat(50) + "\n");
                    } else {
                        // If no tree was generated (e.g. fatal error), just dump to trace
                        traceStream.print(output);
                        traceStream.println("\n" + "*".repeat(85) + "\n");
                    }
                }
            }
            // Restore normal printing just to be safe
            System.setOut(originalOut);
            
        } catch (Exception e) {
            System.out.println("Error while testing parser: " + e.getMessage());
        }
    }
}