import java.util.ArrayList;

public class FirstFollow {
    public ArrayList<ArrayList<String>> firstSets = new ArrayList<>();
    public ArrayList<ArrayList<String>> followSets = new ArrayList<>();
    private Grammar grammar;

    public FirstFollow(Grammar g) {
        this.grammar = g;
    }

    public void computeFirstSets() {
        for (int i = 0; i < grammar.nonTerminals.size(); i++) firstSets.add(new ArrayList<>());

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++) {
                int beforeSize = firstSets.get(i).size();
                for (ArrayList<String> production : grammar.allRules.get(i)) {
                    if (production.size() == 1 && (production.get(0).equals("epsilon") || production.get(0).equals("@"))) {
                        addUnique(firstSets.get(i), "epsilon");
                        continue;
                    }
                    boolean allDeriveEpsilon = true;
                    for (String symbol : production) {
                        if (!grammar.isNonTerminal(symbol)) {
                            addUnique(firstSets.get(i), symbol);
                            allDeriveEpsilon = false;
                            break;
                        } else {
                            int ntIndex = grammar.findNonTerminalIndex(symbol);
                            ArrayList<String> nextFirst = firstSets.get(ntIndex);
                            boolean symbolHasEpsilon = false;
                            for (String s : nextFirst) {
                                if (!s.equals("epsilon")) addUnique(firstSets.get(i), s);
                                else symbolHasEpsilon = true;
                            }
                            if (!symbolHasEpsilon) { allDeriveEpsilon = false; break; }
                        }
                    }
                    if (allDeriveEpsilon) addUnique(firstSets.get(i), "epsilon");
                }
                if (firstSets.get(i).size() != beforeSize) changed = true;
            }
        }
    }

    public void computeFollowSets() {
        for (int i = 0; i < grammar.nonTerminals.size(); i++) followSets.add(new ArrayList<>());
        if (!grammar.nonTerminals.isEmpty()) addUnique(followSets.get(0), "$");

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < grammar.nonTerminals.size(); i++) {
                for (ArrayList<String> production : grammar.allRules.get(i)) {
                    for (int j = 0; j < production.size(); j++) {
                        String B = production.get(j);
                        if (grammar.isNonTerminal(B)) {
                            int bIndex = grammar.findNonTerminalIndex(B);
                            int beforeSize = followSets.get(bIndex).size();
                            if (j + 1 < production.size()) {
                                ArrayList<String> beta = new ArrayList<>(production.subList(j + 1, production.size()));
                                ArrayList<String> firstBeta = getFirstOfSequence(beta);
                                for (String s : firstBeta) if (!s.equals("epsilon")) addUnique(followSets.get(bIndex), s);
                                if (firstBeta.contains("epsilon")) for (String s : followSets.get(i)) addUnique(followSets.get(bIndex), s);
                            } else {
                                for (String s : followSets.get(i)) addUnique(followSets.get(bIndex), s);
                            }
                            if (followSets.get(bIndex).size() != beforeSize) changed = true;
                        }
                    }
                }
            }
        }
    }

    public ArrayList<String> getFirstOfSequence(ArrayList<String> sequence) {
        ArrayList<String> result = new ArrayList<>();
        boolean allDeriveEpsilon = true;
        for (String symbol : sequence) {
            if (!grammar.isNonTerminal(symbol)) {
                addUnique(result, symbol);
                allDeriveEpsilon = false;
                break;
            } else {
                int ntIndex = grammar.findNonTerminalIndex(symbol);
                ArrayList<String> firstOfSymbol = firstSets.get(ntIndex);
                boolean hasEpsilon = false;
                for (String s : firstOfSymbol) {
                    if (s.equals("epsilon")) hasEpsilon = true;
                    else addUnique(result, s);
                }
                if (!hasEpsilon) { allDeriveEpsilon = false; break; }
            }
        }
        if (allDeriveEpsilon) addUnique(result, "epsilon");
        return result;
    }

    private void addUnique(ArrayList<String> list, String val) {
        if (!list.contains(val)) list.add(val);
    }

    public void displayFirstSets() {
        System.out.println("\n--- FIRST Sets ---");
        for (int i = 0; i < grammar.nonTerminals.size(); i++) {
            System.out.println("FIRST(" + grammar.nonTerminals.get(i) + ") = { " + String.join(", ", firstSets.get(i)) + " }");
        }
    }

    public void displayFollowSets() {
        System.out.println("\n--- FOLLOW Sets ---");
        for (int i = 0; i < grammar.nonTerminals.size(); i++) {
            System.out.println("FOLLOW(" + grammar.nonTerminals.get(i) + ") = { " + String.join(", ", followSets.get(i)) + " }");
        }
    }
}