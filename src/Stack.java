import java.util.ArrayList;

public class Stack {
    private ArrayList<String> stack;

    public Stack() {
        stack = new ArrayList<>();
    }

    public void push(String symbol) {
        stack.add(symbol);
    }

    public String pop() {
        if (isEmpty()) return null;
        return stack.remove(stack.size() - 1);
    }

    public String top() {
        if (isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}