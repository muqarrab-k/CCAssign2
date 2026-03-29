# LL(1) Parser Project

## Team Members
* aysha jamshaid - 23i0004
* muhammad muqarrab - 23i0511

## Programming Language Used
* **Java**

## Compilation Instructions
This project can be compiled using either the provided Makefile or standard Java commands.

**Option 1: Using the Makefile (Recommended)**
Open your terminal in the root directory of the project and run:
`make`

**Option 2: Using Standard Java Commands**
Open your terminal in the root directory and run:
`javac *.java`


## Execution Instructions
Once the code is compiled, you can run the parser. By default, the parser expects the grammar and input files to be located in the working directory.

**Option 1: Using the Makefile**
`make run`

**Option 2: Using Standard Java Commands**
`java Main`

*(Note: If you need to switch which grammar is being tested, simply update the filename string inside `Main.java` before recompiling, e.g., `grammar.loadFromFile("grammar2.txt");`)*

## Input File Format Specification

### 1. Grammar File Format (`.txt`)
The parser reads context-free grammars from a standard text file based on these strict formatting rules:
* **Rule definition:** The left-hand side (LHS) and right-hand side (RHS) must be separated by the `->` symbol.
* **Alternatives:** Multiple productions for the same non-terminal must be separated by the `|` symbol.
* **Tokens/Symbols:** Every symbol (terminal and non-terminal) on the RHS must be separated by a space.
* **Epsilon:** Empty productions must be explicitly written using the word `epsilon` or the `@` symbol.
* **Start Symbol:** The LHS non-terminal of the very first rule in the file is automatically treated as the start symbol.

*Example:*
Expr -> Term ExprPrime
ExprPrime -> + Term ExprPrime | epsilon
Term -> Factor TermPrime

### 2. Input String File Format (`.txt`)
The parser reads strings to test against the generated LL(1) table.
* Each string must be on its own line.
* Every token in the string must be separated by a space so the scanner can correctly identify individual terminals.
* Do not manually add the end-of-file symbol (`$`). The parser handles this internally.

*Example:*
id + id * id
( id + id ) * id


## Sample Grammar and Input Files Explanation
The project includes several sample files to demonstrate the parser's capabilities:
* **`grammar2.txt` (Expression Grammar):** Demonstrates precedence handling and left recursion removal.
* **`grammar3.txt` (Statement Grammar):** Tests left-factoring logic (handling the dangling-else problem).
* **`grammar4.txt` (Indirect Left Recursion):** A specialized grammar designed specifically to test the parser's ability to trace and eliminate indirect left recursion loops.
* **`input_valid.txt`:** Contains at least 5 perfectly valid strings for the currently loaded grammar to demonstrate successful parse tree generation.
* **`input_errors.txt`:** Contains at least 5 invalid strings to trigger the Panic Mode error recovery system and demonstrate FOLLOW set synchronization.

## Known Limitations
1. **Fundamentally Ambiguous Grammars:** While the parser handles left recursion and left factoring, if a grammar is fundamentally ambiguous and cannot be converted to LL(1) under any circumstances, the parse table will contain multiple entries for a single cell. The parser currently overwrites prior entries in a conflict.
2. **Spacing Strictness:** The parser relies on spaces to distinguish tokens. An input like `id+id` will be incorrectly read as a single token "id+id" rather than three tokens "id", "+", "id". It must be written as `id + id`.