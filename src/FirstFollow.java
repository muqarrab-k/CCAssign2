import java.util.ArrayList;
//add comments
//keeps makingn first and follow set until it stops chnging
public class FirstFollow
{
    public ArrayList<ArrayList<String>> first = new ArrayList<>();
    public ArrayList<ArrayList<String>> follow = new ArrayList<>();
    private Grammar grammar;

    public FirstFollow(Grammar g)
    {
        this.grammar = g;
    }

    public void first()
    {
        for (int i = 0; i < grammar.nonTerminals.size(); i++)
        {
            first.add(new ArrayList<>());
        }

        boolean changed = true;
        while(changed)
        {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++)
            {
                int origsize = first.get(i).size();
                ArrayList<ArrayList<String>> currentRules = grammar.allRules.get(i);

                for (int j = 0; j < currentRules.size(); j++)
                {
                    ArrayList<String> production = currentRules.get(j);
                    
                    if (production.size() == 1 && (production.get(0).equals("epsilon") || production.get(0).equals("@")))
                    {
                        addUnique(first.get(i), "epsilon");
                        continue;
                    }

                    boolean epsilon = true;
                    for (int k = 0; k < production.size(); k++)
                    {
                        String symbol = production.get(k);

                        if (!grammar.isNonTerminal(symbol))
                        {
                            addUnique(first.get(i), symbol);
                            epsilon = false;
                            break;
                        }
                        else
                        {
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
                            
                            
                            if (!epsilonsymbol)
                            {
                                epsilon = false;
                                break;
                            }
                        }
                    }
                    if (epsilon)
                    {
                        addUnique(first.get(i), "epsilon");
                    }
                }
                if (first.get(i).size() != origsize)
                    changed = true;
            }
        }
    }

    public void follow()
    {
        for (int i = 0; i < grammar.nonTerminals.size(); i++)
        {
            follow.add(new ArrayList<>());
        }

        if (grammar.nonTerminals.size() > 0)
        {
            addUnique(follow.get(0), "$");
        }

        boolean changed = true;
        while (changed)
        {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++)
            {
                ArrayList<ArrayList<String>> rules = grammar.allRules.get(i);

                for (int j = 0; j < rules.size(); j++)
                {
                    ArrayList<String> production = rules.get(j);

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
                                String nextSymbol = production.get(next);
                                if (!grammar.isNonTerminal(nextSymbol))
                                {
                                    addUnique(follow.get(indexb), nextSymbol);
                                    suffixepsilon = false;
                                    break;
                                }
                                else
                                {
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
                                    
                                    if (!empty)
                                    {
                                        suffixepsilon = false;
                                        break;
                                    }
                                }
                            }

                            if (suffixepsilon)
                            {
                                ArrayList<String> followA = follow.get(i);
                                for (int m = 0; m < followA.size(); m++)
                                {
                                    addUnique(follow.get(indexb), followA.get(m));
                                }
                            }

                            if (follow.get(indexb).size() != origsize)
                                changed = true;
                        }
                    }
                }
            }
        }
    }

    //first of whole seq of symbols
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
        for (int i = 0; i < grammar.nonTerminals.size(); i++)
        {
            System.out.print("FIRST(" + grammar.nonTerminals.get(i) + ") = { ");
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
        for (int i = 0; i < grammar.nonTerminals.size(); i++)
        {
            System.out.print("FOLLOW(" + grammar.nonTerminals.get(i) + ") = { ");
            ArrayList<String> set = follow.get(i);
            for (int j = 0; j < set.size(); j++) 
            {
                System.out.print(set.get(j) + (j < set.size() - 1 ? ", " : ""));
            }
            System.out.println(" }");
        }
    }
}