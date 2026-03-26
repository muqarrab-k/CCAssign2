import java.util.*;

//grammar rules
public class Production
{
    String leftside; //non terminal
    List<String> rightside; //terminals

    public Production(String leftside, List<String> rightside)
    {
        this.leftside = leftside;
        this.rightside = rightside;
    }
    //write cfgs
    @Override
    public String toString()
    {
        return leftside + " -> " + String.join(" ", rightside);
    }
}