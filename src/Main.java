import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        try {
            // this will load from file
            grammar.loadFromFile("input/grammar1.txt");
            grammar.applyLeftFactoring();
            grammar.removeLeftRecursion();
            grammar.display();
            
            // first and follow sets
            FirstFollow ff = new FirstFollow(grammar);
            ff.computeFirstSets();
            ff.displayFirstSets();
            
            ff.computeFollowSets();
            ff.displayFollowSets();

            Parser parser = new Parser(grammar, ff);
            parser.buildParsingTable();
            parser.displayTable();

            
            if (!parser.isLL1) 
            {
                System.out.println("Conflict, picks first available rule");
            }
            
            System.out.println("\nNon-Terminals: " + grammar.nonTerminals);
            System.out.println("Terminals: " + grammar.terminals);
            
        } catch (IOException e) 
        {
            System.err.println("Error reading file ");
        }
    }
}