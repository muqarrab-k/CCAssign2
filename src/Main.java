import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        try {
            // 1. Load and Transform
            grammar.loadFromFile("input/grammar1.txt");
            grammar.applyLeftFactoring();
            grammar.removeLeftRecursion();
            grammar.display();
            
            // 2. Compute Sets using the modular FirstFollow class
            FirstFollow ff = new FirstFollow(grammar);
            ff.computeFirstSets();
            ff.displayFirstSets();
            
            ff.computeFollowSets();
            ff.displayFollowSets();

            Parser parser = new Parser(grammar, ff);
            parser.buildParsingTable();
            parser.displayTable();

            // Check if we can proceed to Part 2
            if (!parser.isLL1) {
                System.out.println("Note: Because there are conflicts, the parser might pick the first available rule.");
            }
            
            System.out.println("\nNon-Terminals: " + grammar.nonTerminals);
            System.out.println("Terminals: " + grammar.terminals);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}