import java.util.ArrayList;

public class FirstFollow {
    public ArrayList<ArrayList<String>> firstSets = new ArrayList<>();
    public ArrayList<ArrayList<String>> followSets = new ArrayList<>();
    private Grammar grammar;

    public FirstFollow(Grammar g) 
    {
        this.grammar = g;
    }

    public void computeFirstSets() {
        // empty list for non terminal
        for (int i = 0; i < grammar.nonTerminals.size(); i++) 
        {
            firstSets.add(new ArrayList<>());
        }

        boolean changed = true;
        while (changed) 
            {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++) 
                {
                int beforeSize = firstSets.get(i).size();
                ArrayList<ArrayList<String>> currentRules = grammar.allRules.get(i);

                for (int j = 0; j < currentRules.size(); j++) 
                    {
                    ArrayList<String> production = currentRules.get(j);
                    
                    // epsilon rule
                    if (production.size() == 1 && (production.get(0).equals("epsilon") || production.get(0).equals("@"))) {
                        addUnique(firstSets.get(i), "epsilon");
                        continue;
                    }

                    // rule 2
                    boolean allDeriveEpsilon = true;
                    for (int k = 0; k < production.size(); k++) {
                        String symbol = production.get(k);

                        if (!grammar.isNonTerminal(symbol)) {
                            // then terminal hai
                            addUnique(firstSets.get(i), symbol);
                            allDeriveEpsilon = false;
                            break;
                        } else {
                            // add first set if epsilon
                            int ntIndex = grammar.findNonTerminalIndex(symbol);
                            ArrayList<String> symbolFirst = firstSets.get(ntIndex);
                            
                            boolean symbolHasEpsilon = false;
                            for (int m = 0; m < symbolFirst.size(); m++) 
                                {
                                String s = symbolFirst.get(m);
                                if (!s.equals("epsilon")) 
                                {
                                    addUnique(firstSets.get(i), s);
                                } else 
                                {
                                    symbolHasEpsilon = true;
                                }
                            }
                            
                            
                            if (!symbolHasEpsilon) 
                            {
                                allDeriveEpsilon = false;
                                break;
                            }
                        }
                    }
                    if (allDeriveEpsilon) 
                    {
                        addUnique(firstSets.get(i), "epsilon");
                    }
                }
                if (firstSets.get(i).size() != beforeSize) 
                    changed = true;
            }
        }
    }

    public void computeFollowSets() 
    {

        for (int i = 0; i < grammar.nonTerminals.size(); i++) {
            followSets.add(new ArrayList<>());
        }


        if (grammar.nonTerminals.size() > 0) {
            addUnique(followSets.get(0), "$");
        }

        boolean changed = true;
        while (changed) 
            {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++) 
                {
                ArrayList<ArrayList<String>> rules = grammar.allRules.get(i);

                for (int j = 0; j < rules.size(); j++) {
                    ArrayList<String> production = rules.get(j);

                    
                    for (int k = 0; k < production.size(); k++) {
                        String B = production.get(k);

                        if (grammar.isNonTerminal(B)) {
                            int bIndex = grammar.findNonTerminalIndex(B);
                            int beforeSize = followSets.get(bIndex).size();

                            
                            boolean allSuffixDeriveEpsilon = true;
                            for (int next = k + 1; next < production.size(); next++) 
                                {
                                String nextSymbol = production.get(next);

                                if (!grammar.isNonTerminal(nextSymbol)) 
                                    {
                                    
                                    addUnique(followSets.get(bIndex), nextSymbol);
                                    allSuffixDeriveEpsilon = false;
                                    break;
                                } else 
                                    {
                                    
                                    int nextNtIdx = grammar.findNonTerminalIndex(nextSymbol);
                                    ArrayList<String> nextFirst = firstSets.get(nextNtIdx);
                                    
                                    boolean nextCanBeEmpty = false;
                                    for (int m = 0; m < nextFirst.size(); m++) 
                                        {
                                        String s = nextFirst.get(m);
                                        if (!s.equals("epsilon")) 
                                        {
                                            addUnique(followSets.get(bIndex), s);
                                        } else 
                                        {
                                            nextCanBeEmpty = true;
                                        }
                                    }
                                    
                                    if (!nextCanBeEmpty)
                                    {
                                        allSuffixDeriveEpsilon = false;
                                        break;
                                    }
                                }
                            }


                            if (allSuffixDeriveEpsilon) 
                                {
                                ArrayList<String> followA = followSets.get(i);
                                for (int m = 0; m < followA.size(); m++) 
                                {
                                    addUnique(followSets.get(bIndex), followA.get(m));
                                }
                            }

                            if (followSets.get(bIndex).size() != beforeSize) changed = true;
                        }
                    }
                }
            }
        }
    }

    
    public ArrayList<String> getFirstOfSequence(ArrayList<String> sequence) 
    {
        ArrayList<String> result = new ArrayList<>();
        boolean allEmpty = true;
        for (int i = 0; i < sequence.size(); i++) 
            {
            String s = sequence.get(i);
            if (!grammar.isNonTerminal(s)) 
            {
                addUnique(result, s);
                allEmpty = false;
                break;
            } 
            else 
            {
                int idx = grammar.findNonTerminalIndex(s);
                ArrayList<String> f = firstSets.get(idx);
                boolean hasEps = false;
                for (int j = 0; j < f.size(); j++) {
                    if (f.get(j).equals("epsilon")) hasEps = true;
                    else addUnique(result, f.get(j));
                }
                if (!hasEps) 
                {
                    allEmpty = false;
                    break;
                }
            }
        }
        if (allEmpty) addUnique(result, "epsilon");
        return result;
    }

    private void addUnique(ArrayList<String> list, String val) {
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(val)) {
                found = true;
                break;
            }
        }
        if (!found) list.add(val);
    }

    public void displayFirstSets() {
        System.out.println("\nFIRST Sets");
        for (int i = 0; i < grammar.nonTerminals.size(); i++) 
            {
            System.out.print("FIRST(" + grammar.nonTerminals.get(i) + ") = { ");
            ArrayList<String> set = firstSets.get(i);
            for (int j = 0; j < set.size(); j++) 
            {
                System.out.print(set.get(j) + (j < set.size() - 1 ? ", " : ""));
            }
            System.out.println(" }");
        }
    }

    public void displayFollowSets() 
    {
        System.out.println("\nFOLLOW Sets");
        for (int i = 0; i < grammar.nonTerminals.size(); i++) 
            {
            System.out.print("FOLLOW(" + grammar.nonTerminals.get(i) + ") = { ");
            ArrayList<String> set = followSets.get(i);
            for (int j = 0; j < set.size(); j++) 
            {
                System.out.print(set.get(j) + (j < set.size() - 1 ? ", " : ""));
            }
            System.out.println(" }");
        }
    }
}