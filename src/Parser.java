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

        if (!grammar.terminals.contains("$")) {
            grammar.terminals.add("$");
        }

        int rows = grammar.nonTerminals.size();
        int cols = grammar.terminals.size();
        parsingTable = new String[rows][cols];


        for (int i = 0; i < rows; i++) 
        {
            for (int j = 0; j < cols; j++)
            {
                parsingTable[i][j] = "";
            }
        }


        for (int i = 0; i < grammar.nonTerminals.size(); i++) 
            {
            ArrayList<ArrayList<String>> productions = grammar.allRules.get(i);

            for (ArrayList<String> alpha : productions) 
                {
              //get first alpha
                ArrayList<String> firstAlpha = ff.getFirstOfSequence(alpha);

                for (String a : firstAlpha) 
                    {
                    if (!a.equals("epsilon") && !a.equals("@")) {
                   
                        int colIndex = grammar.terminals.indexOf(a);
                        if (colIndex != -1) {
                            addToTable(i, colIndex, alpha);
                        }
                    } 
                    else 
                    {

                        ArrayList<String> followA = ff.followSets.get(i);
                        for (String b : followA) 
                        {
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

    // this is helper ftn to avid conflicr
    private void addToTable(int row, int col, ArrayList<String> production) {
        String prodStr = String.join(" ", production);
        
        if (!parsingTable[row][col].isEmpty() && !parsingTable[row][col].equals(prodStr)) 
            {
            
            isLL1 = false;
            parsingTable[row][col] += " | " + prodStr;
        } else {
            parsingTable[row][col] = prodStr;
        }
    }

    public void displayTable() {
        System.out.println("\nLL(1) Parsing Table");
        

        System.out.print(String.format("%-15s", ""));
        for (String t : grammar.terminals) {
            System.out.print(String.format("%-15s", t));
        }
        System.out.println();


        //for hot to print i used chat
        for (int i = 0; i < grammar.nonTerminals.size(); i++) 
            {
            System.out.print(String.format("%-15s", grammar.nonTerminals.get(i)));
            for (int j = 0; j < grammar.terminals.size(); j++) {
                System.out.print(String.format("%-15s", parsingTable[i][j]));
            }
            System.out.println();
        }

        System.out.println("\nIs Grammar LL(1)? " + (isLL1 ? "YES" : "NO (Conflicts present)"));
    }
}