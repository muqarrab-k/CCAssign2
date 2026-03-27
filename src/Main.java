import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] input)
    {
        try
        {
            //Save original console so we can print messages safely
            PrintStream consoleOut = System.out;
            // Create the output folder
            new File("output").mkdirs();
            //process grammar
            Grammar grammar = new Grammar();
            grammar.loadFromFile("input/grammar1.txt"); //diff grammar files used
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

            //parsing table
            Parser parser = new Parser(grammar, ff);
            parser.buildParsingTable();

            PrintStream tableFile = new PrintStream(new FileOutputStream("output/parsing_table.txt"));
            System.setOut(tableFile);
            parser.displayTable();
            if (!parser.isLL1) {
                System.out.println("\nConflict: picks first available rule");
            }
            tableFile.close();

            //parse traces and trees
            System.setOut(consoleOut);
            System.out.println("Processing Traces and extracting Parse Trees...");

            //save tress to parse trace fileamd parse trees
            PrintStream treesFile = new PrintStream(new FileOutputStream("output/parse_trees.txt"));

            PrintStream trace1File = new PrintStream(new FileOutputStream("output/parsing_trace1.txt"));
            trace1File.println(">>> Valid Inputs Trace\n");
            runparser(parser, "input/input_valid.txt", trace1File, treesFile);
            trace1File.close();

            PrintStream trace2File = new PrintStream(new FileOutputStream("output/parsing_trace2.txt"));
            trace2File.println(">>> Error Recovery Trace\n");
            runparser(parser, "input/input_errors.txt", trace2File, treesFile);
            trace2File.close();

            treesFile.close();

            //done
            System.setOut(consoleOut);
            System.out.println("SUCCESS! The Traces and Trees have been successfully separated into their respective files.");

        } catch (Exception e) {
            System.err.println("Critical Error: " + e.getMessage());
        }
    }
    //computers parsing idk
    private static void runparser(Parser parser, String filePath, PrintStream traceStream, PrintStream treeStream)
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            traceStream.println("    [!] File not found: " + filePath);
            return;
        }

        try (Scanner scanner = new Scanner(file))
        {
            //output catcher wtv that means
            PrintStream originalOut = System.out;
            ByteArrayOutputStream catcher = new ByteArrayOutputStream();
            PrintStream captureStream = new PrintStream(catcher);

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty())
                {
                    //Tell Java to print to our "catcher" instead of the console
                    System.setOut(captureStream);
                    parser.parseInput(line);
                    System.out.flush(); //Make sure everything is written
                    
                    //Convert caught output to a string and clear the catcher for the next line
                    String output = catcher.toString();
                    catcher.reset();

                    //split text at parse tree--
                    int treeIndex = output.indexOf("--- Parse Tree ---");
                    
                    if (treeIndex != -1)
                    {
                        //plit if tree foiudn
                        String tracePart = output.substring(0, treeIndex);
                        String treePart = output.substring(treeIndex);

                        // Write trace
                        traceStream.print(tracePart);
                        traceStream.println("\n" + "*".repeat(85) + "\n");

                        // Write tree
                        treeStream.println("Input String: " + line);
                        treeStream.print(treePart);
                        treeStream.println("\n" + "=".repeat(50) + "\n");
                    }
                    else
                    {
                        //if fatal error dump trace
                        traceStream.print(output);
                        traceStream.println("\n" + "*".repeat(85) + "\n");
                    }
                }
            }
            //normal printing
            System.setOut(originalOut);
            
        }
        catch (Exception e)
        {
            System.out.println("Error while testing parser: " + e.getMessage());
        }
    }
}