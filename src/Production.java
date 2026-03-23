import java.util.*;

public class Production {
    String lhs; 
    List<String> rhs;

    public Production(String lhs, List<String> rhs) 
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return lhs + " -> " + String.join(" ", rhs);
    }
}