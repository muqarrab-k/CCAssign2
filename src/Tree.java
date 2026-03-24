import java.util.ArrayList;
import java.util.List;

public class Tree {
    public String symbol;
    public List<Tree> children;

    public Tree(String symbol) {
        this.symbol = symbol;
        this.children = new ArrayList<>();
    }

    public void addChild(Tree child) {
        this.children.add(child);
    }

    // Task 2.5: Indented text format display
    public void display(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  "); // Indentation
        }
        System.out.println(symbol);
        
        if (children != null) {
            for (Tree child : children) {
                child.display(level + 1);
            }
        }
    }
}