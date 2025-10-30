package fr.ufrst.m1info.gl.groupe7;

/**
 * Simple symbol table without HashMap and without scope management.
 * Uses a fixed-size array to store symbols linearly.
 * Provides methods to add, lookup, check existence, and update values.
 * Debug output is printed to System.err (not System.out) to avoid CLI interference.
 */
public class SymbolTable {
    private static final int MAX_SIZE = 256;
    private Symbol[] table;
    private int size;

    public SymbolTable() {
        this.table = new Symbol[MAX_SIZE];
        this.size = 0;
    }

    /** Adds a new symbol if it does not already exist. */
    public void add(Symbol s) {
        if (exists(s.getId())) {
            System.err.println("[WARN] Identifier already declared: " + s.getId());
            return;
        }
        if (size >= MAX_SIZE) {
            System.err.println("[ERROR] Symbol table full!");
            return;
        }
        table[size++] = s;
    }

    /** Returns the symbol for a given identifier, or null if not found. */
    public Symbol get(String id) {
        for (int i = size - 1; i >= 0; i--) {
            if (table[i].getId().equals(id)) {
                return table[i];
            }
        }
        return null;
    }

    /** Checks whether a given identifier already exists. */
    public boolean exists(String id) {
        for (int i = 0; i < size; i++) {
            if (table[i].getId().equals(id)) return true;
        }
        return false;
    }

    /** Updates the value of an existing identifier, prints error if not found. */
    public void update(String id, Object newVal) {
        for (int i = size - 1; i >= 0; i--) {
            if (table[i].getId().equals(id)) {
                table[i].setValue(newVal);
                return;
            }
        }
        System.err.println("[ERROR] Unknown identifier: " + id);
    }

    /** Prints the entire symbol table content to System.err (for debug). */
    public void printTable() {
        System.err.println("=== Symbol Table ===");
        for (int i = 0; i < size; i++) {
            System.err.println("  " + table[i]);
        }
        System.err.println("====================");
    }

    /** Returns current number of symbols. */
    public int getSize() { return size; }
}








/*


public class SymbolTable {

    // this is  Stack of scopes (each scope = HashMap of symbols)
    private Stack<Map<String, Symbol>> scopes;

    public SymbolTable() {
        scopes = new Stack<>();
        openScope(); //
    }


    public void openScope() {
        scopes.push(new HashMap<>());
    }

    public void closeScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }


    public void addSymbol(Symbol symbol) {
        if (!scopes.isEmpty()) {
            scopes.peek().put(symbol.getName(), symbol);
        }
    }

    public Symbol findSymbol(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Symbol s = scopes.get(i).get(name);
            if (s != null) return s;
        }
        return null;
    }

    public boolean containsSymbol(String name) {
        return findSymbol(name) != null;
    }

    public void removeSymbol(String name) {
        if (!scopes.isEmpty()) {
            scopes.peek().remove(name);
        }
    }

    public void printTable() {
        System.out.println("( ͡ᵔ ͜ʖ ͡ᵔ ) Symbol Table ( ͡ᵔ ͜ʖ ͡ᵔ )");
        for (int i = 0; i < scopes.size(); i++) {
            System.out.println("Scope " + i + ": " + scopes.get(i).values());
        }
        System.out.println("( ͡ᵔ ͜ʖ ͡ᵔ )");
    }

    //  Debug helper
    public int getScopeCount() {
        return scopes.size();
    }
}
*/
