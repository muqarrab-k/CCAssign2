import java.util.ArrayList;

//keeps makingn first and follow set until it stops chnging
public class FirstFollow
{
    public ArrayList<ArrayList<String>> first = new ArrayList<>();
    public ArrayList<ArrayList<String>> follow = new ArrayList<>();
    private Grammar grammar;

    public FirstFollow(Grammar g)
    {
        this.grammar = g; //initialise first grammar to read
    }

    //keep finding first sets until they stop changing
    public void first()
    {
        //non terminals
        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            first.add(new ArrayList<>());
        }

        boolean changed = true;
        while(changed)
        {
            changed = false;
            for (int i = 0; i < grammar.nonterminals.size(); i++)
            {
                int origsize = first.get(i).size();
                ArrayList<ArrayList<String>> currentRules = grammar.allRules.get(i);

                for (int j = 0; j < currentRules.size(); j++)
                {
                    ArrayList<String> production = currentRules.get(j);
                    //if production is epsilon add it to first set
                    if (production.size() == 1 && (production.get(0).equals("epsilon") || production.get(0).equals("@")))
                    {
                        addUnique(first.get(i), "epsilon");
                        continue;
                    }

                    boolean epsilon = true;
                    for (int k = 0; k < production.size(); k++)
                    {
                        String symbol = production.get(k);
                        //if terminal first sybmbol add it ad stop looking further
                        if (!grammar.isNonTerminal(symbol))
                        {
                            addUnique(first.get(i), symbol);
                            epsilon = false;
                            break;
                        }
                        else
                        {
                            //if non rterminal pull in first set
                            int ntIndex = grammar.findNonTerminalIndex(symbol);
                            ArrayList<String> symbolFirst = first.get(ntIndex);
                            
                            boolean epsilonsymbol = false;
                            for (int l = 0; l < symbolFirst.size(); l++)
                            {
                                String s = symbolFirst.get(l);
                                if (!s.equals("epsilon"))
                                {
                                    addUnique(first.get(i), s);
                                }
                                else
                                {
                                    epsilonsymbol = true;
                                }
                            }
                            
                            //if no epsilon stop checking (string vanishes if only epsilon)
                            if (!epsilonsymbol)
                            {
                                epsilon = false;
                                break;
                            }
                        }
                    }
                    //if epsilon then whole rule can be epsilon
                    if (epsilon)
                    {
                        addUnique(first.get(i), "epsilon");
                    }
                }
                //if set grows need to check again
                if (first.get(i).size() != origsize)
                    changed = true;
            }
        }
    }
    //keep finding follow sets until they stop chnging
    public void follow()
    {
        //setup for non terminals
        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            follow.add(new ArrayList<>());
        }

        //every start symbol gets $ in follow set atomaticalluy
        if (grammar.nonterminals.size() > 0)
        {
            addUnique(follow.get(0), "$");
        }

        boolean changed = true;
        while (changed)
        {
            changed = false;
            for (int i = 0; i < grammar.nonterminals.size(); i++)
            {
                ArrayList<ArrayList<String>> rules = grammar.allRules.get(i);

                for (int j = 0; j < rules.size(); j++)
                {
                    ArrayList<String> production = rules.get(j);
                    //check term after curr non terminal
                    for (int k = 0; k < production.size(); k++)
                    {
                        String B = production.get(k);

                        if (grammar.isNonTerminal(B))
                        {
                            int indexb = grammar.findNonTerminalIndex(B);
                            int origsize = follow.get(indexb).size();

                            boolean suffixepsilon = true;
                            for (int next = k + 1; next < production.size(); next++)
                            {
                                //if next is terminal it belongs in follow set
                                String nextSymbol = production.get(next);
                                if (!grammar.isNonTerminal(nextSymbol))
                                {
                                    addUnique(follow.get(indexb), nextSymbol);
                                    suffixepsilon = false;
                                    break;
                                }
                                else
                                {
                                    //if non terminal check follow of next term
                                    int nextnonterminal = grammar.findNonTerminalIndex(nextSymbol);
                                    ArrayList<String> nextFirst = first.get(nextnonterminal);
                                    
                                    boolean empty = false;
                                    for (int m = 0; m < nextFirst.size(); m++)
                                    {
                                        String s = nextFirst.get(m);
                                        if (!s.equals("epsilon"))
                                        {
                                            addUnique(follow.get(indexb), s);
                                        }
                                        else
                                        {
                                            empty = true;
                                        }
                                    }
                                    //if not epsilon stop looking forward
                                    if (!empty)
                                    {
                                        suffixepsilon = false;
                                        break;
                                    }
                                }
                            }
                            //if everything after b can vanish, b inherits the follow of lhs
                            if (suffixepsilon)
                            {
                                ArrayList<String> followA = follow.get(i);
                                for (int m = 0; m < followA.size(); m++)
                                {
                                    addUnique(follow.get(indexb), followA.get(m));
                                }
                            }
                            //if changes then loop again
                            if (follow.get(indexb).size() != origsize)
                                changed = true;
                        }
                    }
                }
            }
        }
    }

    //first of whole seq of symbols to build parse tab;e
    public ArrayList<String> firstofsequence(ArrayList<String> sequence)
    {
        ArrayList<String> result = new ArrayList<>();

        boolean empty = true;
        for (int i = 0; i < sequence.size(); i++)
        {
            String s = sequence.get(i);
            if (!grammar.isNonTerminal(s))
            {
                addUnique(result, s);
                empty = false;
                break;
            }
            else
            {
                int index = grammar.findNonTerminalIndex(s);
                ArrayList<String> f = first.get(index);
                boolean hasEps = false;
                for (int j = 0; j < f.size(); j++)
                {
                    if (f.get(j).equals("epsilon"))
                        hasEps = true;
                    else
                        addUnique(result, f.get(j));
                }
                if (!hasEps)
                {
                    empty = false;
                    break;
                }
            }
        }
        if (empty)
            addUnique(result, "epsilon");

        return result;
    }

    //hash set behavior so it doesnt find duplicate sybmbols
    private void addUnique(ArrayList<String> list, String val)
    {
        boolean found = false;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).equals(val))
            {
                found = true;
                break;
            }
        }

        if (!found)
            list.add(val);
    }

    //display all first sets
    public void displayfirst()
    {
        System.out.println("\nFIRST Sets");
        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            System.out.print("FIRST(" + grammar.nonterminals.get(i) + ") = { ");
            ArrayList<String> set = first.get(i);
            for (int j = 0; j < set.size(); j++)
            {
                System.out.print(set.get(j) + (j < set.size() - 1 ? ", " : ""));
            }
            System.out.println(" }");
        }
    }

    //display all follow sets
    public void displayfollow()
    {
        System.out.println("\nFOLLOW Sets");
        for (int i = 0; i < grammar.nonterminals.size(); i++)
        {
            System.out.print("FOLLOW(" + grammar.nonterminals.get(i) + ") = { ");
            ArrayList<String> set = follow.get(i);
            for (int j = 0; j < set.size(); j++) 
            {
                System.out.print(set.get(j) + (j < set.size() - 1 ? ", " : ""));
            }
            System.out.println(" }");
        }
    }
}