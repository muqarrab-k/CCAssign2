import java.util.ArrayList;

public class ErrorHandler {

    // Reports exact error details (Task 2.4)
    public static void reportError(int tokenIndex, String expected, String found, String errorType) {
        System.out.println(">>> ERROR: " + errorType);
        System.out.println("    Position: Token " + (tokenIndex + 1));
        System.out.println("    Expected: " + expected + " | Found: " + found);
    }

    // Panic Mode Recovery Strategy
    public static int panicModeRecover(String nonTerminal, ArrayList<String> followSet, String[] tokens, int ip) {
        System.out.println("    [Panic Mode] Synchronizing using FOLLOW(" + nonTerminal + "): " + followSet);
        
        // Skip tokens until we find a synchronizing symbol (in FOLLOW set or EOF)
        while (ip < tokens.length) {
            if (followSet.contains(tokens[ip]) || tokens[ip].equals("$")) {
                System.out.println("    [Panic Mode] Resuming parse at token: " + tokens[ip] + "\n");
                return ip;
            }
            System.out.println("    [Panic Mode] Skipping unexpected token: " + tokens[ip]);
            ip++;
        }
        return ip;
    }
}