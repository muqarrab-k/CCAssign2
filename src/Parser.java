import java.util.ArrayList;

public class Parser
{
    private Grammar grammar;
    private FirstFollow ff;
    public String[][] parsingTable;
    public boolean isLL1 = true; //to check that a cell doesnt contain more than one prod rule

    public Parser(Grammar g, FirstFollow ff)
    {
        this.grammar = g;
        this.ff = ff;
    }

    public void parseInput(String input)
    {
        // Task 2.1: Split by space and add the EOF marker ($)
        String[] tokens = (input + " $").split("\\s+");
        int ip = 0; // Input Pointer

        Stack stack = new Stack();
        stack.push("$");
        stack.push(grammar.startSymbol);

        // Initialize the Tree for Task 4.5
        Tree root = new Tree(grammar.startSymbol);
        java.util.Stack<Tree> treeStack = new java.util.Stack<>();
        treeStack.push(null); // End marker
        treeStack.push(root);

        System.out.println("\nParsing String: " + input);
        
        // Task 2.3: Table Header Formatting
        System.out.printf("%-5s | %-30s | %-20s | %s\n", "Step", "Stack", "Remaining Input", "Action");
        System.out.println("-".repeat(85));

        int step = 1;

        // Follow the Parsing Algorithm (Task 4.3)
        while (!stack.isEmpty())
        {
            String X = stack.top();
            String a = (ip < tokens.length) ? tokens[ip] : "$";

            // Calculate remaining input for display
            StringBuilder remaining = new StringBuilder();

            for (int i = ip; i < tokens.length; i++)
                remaining.append(tokens[i]).append(" ");

            // Print the current step's state (wait to print Action and \n)
            System.out.printf("%-5d | %-30s | %-20s | ", step++, stack.toString(), remaining.toString().trim());

            //Match
            if (X.equals(a))
            {
                if (X.equals("$"))
                {
                    System.out.println("Accept");
                    System.out.println("\nRESULT: String Accepted Successfully!");
                    System.out.println("--- Parse Tree ---");
                    root.display(0); // Show the tree
                    return;
                }
                System.out.println("Match " + a);
                stack.pop();
                treeStack.pop();
                ip++;
            }
            //nonterminal Expansion
            else if (grammar.isNonTerminal(X))
            {
                int row = grammar.findNonTerminalIndex(X);
                int col = grammar.terminals.indexOf(a);

                if (col == -1 || parsingTable[row][col].isEmpty())
                {
                    // Task 2.4: Error Recovery - Empty Table Entry / Unexpected Symbol
                    System.out.println("ERROR!");
                    ErrorHandler.reportError(ip, "Valid production for " + X, a, "Empty Table Entry / Unexpected Symbol");
                    
                    // Panic Mode: Skip tokens until follow set is met
                    ip = ErrorHandler.panicRecover(X, ff.follow.get(row), tokens, ip);
                    stack.pop();
                    treeStack.pop();
                }
                else
                {
                    String production = parsingTable[row][col];
                    System.out.println(X + " -> " + production);
                    expand(stack, treeStack, production);
                }
            }
            //Terminal Mismatch
            else
            {
                //Error Recovery
                System.out.println("ERROR!");
                ErrorHandler.reportError(ip, X, a, "Missing Symbol");
                System.out.println("    [Recovery] Popping expected terminal '" + X + "' from stack.");
                stack.pop();
                treeStack.pop();
            }
        }
    }
    private void expand(Stack stack, java.util.Stack<Tree> treeStack, String productionStr)
    {
        //Remove the Non-Terminal we are currently expanding
        stack.pop();
        Tree currentNode = treeStack.pop();

        //Epsilon cases
        if (productionStr.equals("epsilon") || productionStr.equals("@") || productionStr.isEmpty())
        {
            currentNode.addchild(new Tree("ε"));
            return;
        }

        //Split the production
        String[] symbols = productionStr.split("\\s+");

        //ush to stack in REVERSE order (Task 4.3)
        // if rule is A -> B C, C goes on stack first, then B is on top.
        for (int i = symbols.length - 1; i >= 0; i--) {
            String s = symbols[i];
            stack.push(s);
            
            // Create tree node and sync with treeStack
            Tree child = new Tree(s);
            // We add to index 0 so the tree displays in correct left-to-right order
            currentNode.children.add(0, child);
            treeStack.push(child);
        }
    }

    //simpel hwo to build parse tba;le
    public void buildParsingTable()
    {
        if (!grammar.terminals.contains("$"))
        {
            grammar.terminals.add("$");
        }

        int rows = grammar.nonterminals.size();
        int cols = grammar.terminals.size();
        parsingTable = new String[rows][cols];

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                parsingTable[i][j] = "";
            }
        }


        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            ArrayList<ArrayList<String>> productions = grammar.allRules.get(i);

            for (ArrayList<String> alpha : productions)
            {
              //get first alpha
                ArrayList<String> firstAlpha = ff.firstofsequence(alpha);

                for (String a : firstAlpha)
                {
                    if (!a.equals("epsilon") && !a.equals("@"))
                    {
                        int colIndex = grammar.terminals.indexOf(a);
                        if (colIndex != -1)
                        {
                            addToTable(i, colIndex, alpha);
                        }
                    }
                    else
                    {
                        ArrayList<String> followA = ff.follow.get(i);
                        for (String b : followA)
                        {
                            int colIndex = grammar.terminals.indexOf(b);
                            if (colIndex != -1)
                            {
                                addToTable(i, colIndex, alpha);
                            }
                        }
                    }
                }
            }
        }
    }

    // this is helper ftn to avid conflicr
    private void addToTable(int row, int col, ArrayList<String> production)
    {
        String prodStr = String.join(" ", production);
        
        if (!parsingTable[row][col].isEmpty() && !parsingTable[row][col].equals(prodStr))
        {
            isLL1 = false;
            parsingTable[row][col] += " | " + prodStr;
        }
        else
        {
            parsingTable[row][col] = prodStr;
        }
    }

    public void displayTable()
    {
        System.out.println("\nLL(1) Parsing Table");
        System.out.print(String.format("%-15s", ""));

        for (String t : grammar.terminals)
        {
            System.out.print(String.format("%-15s", t));
        }
        System.out.println();

        //for hot to print i used chat
        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            System.out.print(String.format("%-15s", grammar.nonterminals.get(i)));
            for (int j = 0; j < grammar.terminals.size(); j++)
            {
                System.out.print(String.format("%-15s", parsingTable[i][j]));
            }
            System.out.println();
        }

        System.out.println("\nIs Grammar LL(1)? " + (isLL1 ? "YES" : "NO (Conflicts present)"));
    }
}