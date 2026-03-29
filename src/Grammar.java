import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Grammar
{
    public ArrayList<String> nonterminals = new ArrayList<>(); //lhs symbols
    public ArrayList<ArrayList<ArrayList<String>>> allRules = new ArrayList<>(); //3d array for non term productions
    public ArrayList<String> terminals = new ArrayList<>(); //tokens iutsekf
    public String startSymbol = null;

    public void loadFromFile(String filename) throws FileNotFoundException
    {
        File file = new File(filename);
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine())
        {
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                continue;

            //split cfg into lhs and rhs
            String[] parts = line.split("->");
            String lhs = parts[0].trim();
            String rhsSection = parts[1].trim();

            if (startSymbol == null) startSymbol = lhs;

            //if no non terminal add it to list
            int index = nontermfind(lhs);
            if (index == -1)
            {
                nonterminals.add(lhs);
                allRules.add(new ArrayList<>());
                index = nonterminals.size() - 1;
            }

            String[] alternatives = rhsSection.split("\\|");
            for (String alt : alternatives)
            {
                ArrayList<String> tokens = new ArrayList<>();
                Scanner tokenScanner = new Scanner(alt.trim());
                while (tokenScanner.hasNext())
                {
                    tokens.add(tokenScanner.next());
                }
                allRules.get(index).add(tokens);
            }
        }
        sc.close();
        findterminals();
    }

    //fnd all non terminals in the cfg
    public int nontermfind(String name)
    {
        for (int i = 0; i < nonterminals.size(); i++)
        {
            if (nonterminals.get(i).equals(name)) return i;
        }
        return -1;
    }

    //checks if non term or epsilon if not then its terminal
    private void findterminals()
    {
        terminals.clear();
        for (ArrayList<ArrayList<String>> ruleSet : allRules)
        {
            for (ArrayList<String> production : ruleSet)
            {
                for (String symbol : production)
                {
                    if (!isNonTerminal(symbol) && !symbol.equals("epsilon") && !symbol.equals("@"))
                    {
                        if (!terminals.contains(symbol))
                        {
                            terminals.add(symbol);
                        }
                    }
                }
            }
        }
    }

    //checks is string is variable ot not
    public boolean isNonTerminal(String s)
    {
        return nontermfind(s) != -1;
    }

    //basic display dun
    public void display()
    {
        System.out.println("\n--- Grammar ---");
        for (int i = 0; i < nonterminals.size(); i++)
        {
            System.out.print(nonterminals.get(i) + " -> ");
            ArrayList<ArrayList<String>> productions = allRules.get(i);
            for (int j = 0; j < productions.size(); j++)
            {
                ArrayList<String> symbols = productions.get(j);
                for (String s : symbols)
                    System.out.print(s + " ");
                if (j < productions.size() - 1)
                    System.out.print("| ");
            }
            System.out.println();
        }
    }

    //finish first set confiltcs
    public void leftfactoring()
    {
        for (int i = 0; i < nonterminals.size(); i++)
        {
            String lhs = nonterminals.get(i);
            ArrayList<ArrayList<String>> productions = allRules.get(i);
            //compare every production
            for (int j = 0; j < productions.size(); j++)
            {
                for (int k = j + 1; k < productions.size(); k++)
                {
                    ArrayList<String> p1 = productions.get(j);
                    ArrayList<String> p2 = productions.get(k);

                    //if sme starting sybmbol we have conflict
                    if (!p1.isEmpty() && !p2.isEmpty() && p1.get(0).equals(p2.get(0)))
                    {
                        String commonPrefix = p1.get(0);
                        String newNT = lhs + "Prime";

                        //create new prime non terminal
                        if (nontermfind(newNT) == -1)
                        {
                            nonterminals.add(newNT);
                            allRules.add(new ArrayList<>());
                        }
                        int newIndex = nontermfind(newNT);

                        //get leftocver after common prefix idk
                        ArrayList<String> suffix1 = (p1.size() > 1) ? new ArrayList<>(p1.subList(1, p1.size())) : new ArrayList<>();
                        if (suffix1.isEmpty())
                            suffix1.add("epsilon");

                        ArrayList<String> suffix2 = (p2.size() > 1) ? new ArrayList<>(p2.subList(1, p2.size())) : new ArrayList<>();
                        if (suffix2.isEmpty())
                            suffix2.add("epsilon");

                        //add sucffixes rto prime table
                        allRules.get(newIndex).add(suffix1);
                        allRules.get(newIndex).add(suffix2);
                        //delete old rules
                        productions.remove(k);
                        productions.remove(j);

                        //replace with new
                        ArrayList<String> factored = new ArrayList<>();
                        factored.add(commonPrefix);
                        factored.add(newNT);
                        productions.add(factored);
                        //reste loop
                        j = -1;
                        break;
                    }
                }
            }
        }
        //recalcilate  terminals
        findterminals();
    }

    //removes indirect left recyr
    public void leftrecursionremoval()
    {
        for (int i = 0; i < nonterminals.size(); i++)
        {
            for (int j = 0; j < i; j++)
            {
                //cjheck rules of both and if undiretly recurse fix it
                String Aj = nonterminals.get(j);
                ArrayList<ArrayList<String>> Ai_rules = allRules.get(i);
                ArrayList<ArrayList<String>> Aj_rules = allRules.get(j);
                ArrayList<ArrayList<String>> new_Ai_rules = new ArrayList<>();

                for (ArrayList<String> rule : Ai_rules)
                {
                    if (!rule.isEmpty() && rule.get(0).equals(Aj))
                    {
                        //replace rules if recrusion
                        ArrayList<String> gamma = new ArrayList<>(rule.subList(1, rule.size()));
                        for (ArrayList<String> delta : Aj_rules)
                        {
                            ArrayList<String> combined = new ArrayList<>(delta);
                            combined.addAll(gamma);
                            new_Ai_rules.add(combined);
                        }
                    }
                    else
                    {
                        new_Ai_rules.add(rule);
                    }
                }
                allRules.set(i, new_Ai_rules);
            }
            //once done we remove recusion
            directrecursionleft(i);
        }
        findterminals();
    }

    //direct recursion remobe
    private void directrecursionleft(int index)
    {
        //sort rules into recusrive or non recursive
        String lhs = nonterminals.get(index);
        ArrayList<ArrayList<String>> rules = allRules.get(index);
        ArrayList<ArrayList<String>> recurs = new ArrayList<>();
        ArrayList<ArrayList<String>> notrecurs = new ArrayList<>();

        for (ArrayList<String> rule : rules)
        {
            if (!rule.isEmpty() && rule.get(0).equals(lhs))
            {
                recurs.add(new ArrayList<>(rule.subList(1, rule.size())));
            }
            else
            {
                notrecurs.add(rule);
            }
        }

        //if alpha is empty aka no recursion
        if (recurs.isEmpty())
            return;

        //create new non termonal;
        String newNT = lhs + "Dash";
        nonterminals.add(newNT);
        ArrayList<ArrayList<String>> newNTRules = new ArrayList<>();
        allRules.add(newNTRules);

        //rewrite origi rules
        rules.clear();
        for (ArrayList<String> beta : notrecurs)
        {
            ArrayList<String> newRule = new ArrayList<>(beta);
            if (!newRule.isEmpty() && (newRule.get(0).equals("epsilon") || newRule.get(0).equals("@")))
                newRule.clear();
            newRule.add(newNT);
            rules.add(newRule);
        }

        //creat enew rules
        for (ArrayList<String> alpha : recurs)
        {
            ArrayList<String> newRule = new ArrayList<>(alpha);
            newRule.add(newNT);
            newNTRules.add(newRule);
        }
        //add epsilon
        ArrayList<String> epsilon = new ArrayList<>();
        epsilon.add("epsilon");
        newNTRules.add(epsilon);
    }
}