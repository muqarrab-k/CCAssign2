import java.util.ArrayList;

public class ErrorHandler
{
    //showing where error and where parser got stck
    public static void reportError(int tokenIndex, String expected, String found, String errorType)
    {
        System.out.println(">>> ERROR: " + errorType);
        System.out.println("Position: Token " + (tokenIndex + 1));
        System.out.println("Expected: " + expected + " | Found: " + found);
    }

    //if code gets stuck in infinite loop, code starts panic mode
    public static int panicRecover(String nonTerminal, ArrayList<String> followSet, String[] tokens, int ip)
    {
        System.out.println("[Panic Mode] Synchronizing using FOLLOW(" + nonTerminal + "): " + followSet);

        //if follow token or end of file found wewill stop skippingand resume parsing
        while(ip < tokens.length)
        {
            if (followSet.contains(tokens[ip]) || tokens[ip].equals("$"))
            {
                System.out.println("[Panic Mode] Resuming parse at token: " + tokens[ip] + "\n");
                return ip;
            }
            //else thro token and check the next one
            System.out.println("[Panic Mode] Skipping unexpected token: " + tokens[ip]);
            ip++;
        }
        //return input pointer
        return ip;
    }
}