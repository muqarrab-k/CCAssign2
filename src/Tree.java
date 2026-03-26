import java.util.ArrayList;
import java.util.List;

public class Tree
{
    public String symbol;
    public List<Tree> children;

    //constructor
    public Tree(String symbol)
    {
        this.symbol = symbol;
        this.children = new ArrayList<>();
    }

    //add tree node
    public void addchild(Tree child)
    {
        this.children.add(child);
    }

    //display
    public void display(int level)
    {
        for (int i = 0; i < level; i++)
        {
            System.out.print("  ");
        }
        System.out.println(symbol);
        
        //display all children
        if (children != null)
        {
            for (Tree child:children)
            {
                child.display(level + 1);
            }
        }
    }
}