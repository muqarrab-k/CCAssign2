import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Grammar {
    public ArrayList<String> nonTerminals = new ArrayList<>();
    public ArrayList<ArrayList<ArrayList<String>>> allRules = new ArrayList<>();
    public ArrayList<String> terminals = new ArrayList<>();
    public String startSymbol = null;

    public void loadFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("->");
            String lhs = parts[0].trim();
            String rhsSection = parts[1].trim();

            if (startSymbol == null) startSymbol = lhs;

            int index = findNonTerminalIndex(lhs);
            if (index == -1) {
                nonTerminals.add(lhs);
                allRules.add(new ArrayList<>());
                index = nonTerminals.size() - 1;
            }

            String[] alternatives = rhsSection.split("\\|");
            for (String alt : alternatives) {
                ArrayList<String> tokens = new ArrayList<>();
                Scanner tokenScanner = new Scanner(alt.trim());
                while (tokenScanner.hasNext()) {
                    tokens.add(tokenScanner.next());
                }
                allRules.get(index).add(tokens);
            }
        }
        sc.close();
        identifyTerminals();
    }

    public int findNonTerminalIndex(String name) {
        for (int i = 0; i < nonTerminals.size(); i++) {
            if (nonTerminals.get(i).equals(name)) return i;
        }
        return -1;
    }

    private void identifyTerminals() {
        terminals.clear();
        for (ArrayList<ArrayList<String>> ruleSet : allRules) {
            for (ArrayList<String> production : ruleSet) {
                for (String symbol : production) {
                    if (!isNonTerminal(symbol) && !symbol.equals("epsilon") && !symbol.equals("@")) {
                        if (!terminals.contains(symbol)) {
                            terminals.add(symbol);
                        }
                    }
                }
            }
        }
    }

    public boolean isNonTerminal(String s) {
        return findNonTerminalIndex(s) != -1;
    }

    public void display() {
        System.out.println("\n--- Grammar ---");
        for (int i = 0; i < nonTerminals.size(); i++) {
            System.out.print(nonTerminals.get(i) + " -> ");
            ArrayList<ArrayList<String>> productions = allRules.get(i);
            for (int j = 0; j < productions.size(); j++) {
                ArrayList<String> symbols = productions.get(j);
                for (String s : symbols) System.out.print(s + " ");
                if (j < productions.size() - 1) System.out.print("| ");
            }
            System.out.println();
        }
    }

    public void applyLeftFactoring() {
        for (int i = 0; i < nonTerminals.size(); i++) {
            String lhs = nonTerminals.get(i);
            ArrayList<ArrayList<String>> productions = allRules.get(i);
            for (int j = 0; j < productions.size(); j++) {
                for (int k = j + 1; k < productions.size(); k++) {
                    ArrayList<String> p1 = productions.get(j);
                    ArrayList<String> p2 = productions.get(k);

                    if (!p1.isEmpty() && !p2.isEmpty() && p1.get(0).equals(p2.get(0))) {
                        String commonPrefix = p1.get(0);
                        String newNT = lhs + "Prime";

                        if (findNonTerminalIndex(newNT) == -1) {
                            nonTerminals.add(newNT);
                            allRules.add(new ArrayList<>());
                        }
                        int newIndex = findNonTerminalIndex(newNT);

                        ArrayList<String> suffix1 = (p1.size() > 1) ? new ArrayList<>(p1.subList(1, p1.size())) : new ArrayList<>();
                        if (suffix1.isEmpty()) suffix1.add("epsilon");

                        ArrayList<String> suffix2 = (p2.size() > 1) ? new ArrayList<>(p2.subList(1, p2.size())) : new ArrayList<>();
                        if (suffix2.isEmpty()) suffix2.add("epsilon");

                        allRules.get(newIndex).add(suffix1);
                        allRules.get(newIndex).add(suffix2);
                        productions.remove(k);
                        productions.remove(j);

                        ArrayList<String> factored = new ArrayList<>();
                        factored.add(commonPrefix);
                        factored.add(newNT);
                        productions.add(factored);
                        j = -1; 
                        break;
                    }
                }
            }
        }
        identifyTerminals();
    }

    public void removeLeftRecursion() {
        for (int i = 0; i < nonTerminals.size(); i++) {
            for (int j = 0; j < i; j++) {
                String Aj = nonTerminals.get(j);
                ArrayList<ArrayList<String>> Ai_rules = allRules.get(i);
                ArrayList<ArrayList<String>> Aj_rules = allRules.get(j);
                ArrayList<ArrayList<String>> new_Ai_rules = new ArrayList<>();

                for (ArrayList<String> rule : Ai_rules) {
                    if (!rule.isEmpty() && rule.get(0).equals(Aj)) {
                        ArrayList<String> gamma = new ArrayList<>(rule.subList(1, rule.size()));
                        for (ArrayList<String> delta : Aj_rules) {
                            ArrayList<String> combined = new ArrayList<>(delta);
                            combined.addAll(gamma);
                            new_Ai_rules.add(combined);
                        }
                    } else {
                        new_Ai_rules.add(rule);
                    }
                }
                allRules.set(i, new_Ai_rules);
            }
            eliminateDirectLeftRecursion(i);
        }
        identifyTerminals();
    }

    private void eliminateDirectLeftRecursion(int index) {
        String lhs = nonTerminals.get(index);
        ArrayList<ArrayList<String>> rules = allRules.get(index);
        ArrayList<ArrayList<String>> alphas = new ArrayList<>();
        ArrayList<ArrayList<String>> betas = new ArrayList<>();

        for (ArrayList<String> rule : rules) {
            if (!rule.isEmpty() && rule.get(0).equals(lhs)) {
                alphas.add(new ArrayList<>(rule.subList(1, rule.size())));
            } else {
                betas.add(rule);
            }
        }

        if (alphas.isEmpty()) return;

        String newNT = lhs + "Dash";
        nonTerminals.add(newNT);
        ArrayList<ArrayList<String>> newNTRules = new ArrayList<>();
        allRules.add(newNTRules);

        rules.clear();
        for (ArrayList<String> beta : betas) {
            ArrayList<String> newRule = new ArrayList<>(beta);
            if (!newRule.isEmpty() && (newRule.get(0).equals("epsilon") || newRule.get(0).equals("@"))) newRule.clear();
            newRule.add(newNT);
            rules.add(newRule);
        }

        for (ArrayList<String> alpha : alphas) {
            ArrayList<String> newRule = new ArrayList<>(alpha);
            newRule.add(newNT);
            newNTRules.add(newRule);
        }
        ArrayList<String> epsilon = new ArrayList<>();
        epsilon.add("epsilon");
        newNTRules.add(epsilon);
    }
}