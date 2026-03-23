import java.util.ArrayList;

public class Parser {
    private Grammar grammar;
    private FirstFollow ff;
    public String[][] parsingTable;
    public boolean isLL1 = true;

    public Parser(Grammar g, FirstFollow ff) {
        this.grammar = g;
        this.ff = ff;
    }

    public void buildParsingTable() {
        // 1. Ensure $ is in the terminals list for the table columns
        if (!grammar.terminals.contains("$")) {
            grammar.terminals.add("$");
        }

        int rows = grammar.nonTerminals.size();
        int cols = grammar.terminals.size();
        parsingTable = new String[rows][cols];

        // Initialize the table with empty strings to avoid null pointers
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                parsingTable[i][j] = "";
            }
        }

        // 2. Iterate through each Non-Terminal and its productions
        for (int i = 0; i < grammar.nonTerminals.size(); i++) {
            ArrayList<ArrayList<String>> productions = grammar.allRules.get(i);

            for (ArrayList<String> alpha : productions) {
                // Get FIRST(alpha)
                ArrayList<String> firstAlpha = ff.getFirstOfSequence(alpha);

                for (String a : firstAlpha) {
                    if (!a.equals("epsilon") && !a.equals("@")) {
                        // Rule: Add A -> alpha to M[A, a]
                        int colIndex = grammar.terminals.indexOf(a);
                        if (colIndex != -1) {
                            addToTable(i, colIndex, alpha);
                        }
                    } else {
                        // Rule: If epsilon is in FIRST(alpha), add A -> alpha to M[A, b] for each b in FOLLOW(A)
                        ArrayList<String> followA = ff.followSets.get(i);
                        for (String b : followA) {
                            int colIndex = grammar.terminals.indexOf(b);
                            if (colIndex != -1) {
                                addToTable(i, colIndex, alpha);
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper to detect conflicts (check if grammar is LL1)
    private void addToTable(int row, int col, ArrayList<String> production) {
        String prodStr = String.join(" ", production);
        
        if (!parsingTable[row][col].isEmpty() && !parsingTable[row][col].equals(prodStr)) {
            // Conflict detected! Multiple productions in one cell
            isLL1 = false;
            parsingTable[row][col] += " | " + prodStr;
        } else {
            parsingTable[row][col] = prodStr;
        }
    }

    public void displayTable() {
        System.out.println("\n--- LL(1) Parsing Table ---");
        
        // Print column headers (Terminals)
        System.out.print(String.format("%-15s", ""));
        for (String t : grammar.terminals) {
            System.out.print(String.format("%-15s", t));
        }
        System.out.println();

        // Print rows (Non-Terminal and its productions)
        for (int i = 0; i < grammar.nonTerminals.size(); i++) {
            System.out.print(String.format("%-15s", grammar.nonTerminals.get(i)));
            for (int j = 0; j < grammar.terminals.size(); j++) {
                System.out.print(String.format("%-15s", parsingTable[i][j]));
            }
            System.out.println();
        }

        System.out.println("\nIs Grammar LL(1)? " + (isLL1 ? "YES" : "NO (Conflicts present)"));
    }
}