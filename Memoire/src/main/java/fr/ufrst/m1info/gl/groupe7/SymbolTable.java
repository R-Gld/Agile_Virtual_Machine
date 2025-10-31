package fr.ufrst.m1info.gl.groupe7;

/**
 * SymbolTable facade that starts from the project's Pile (Stacks).
 * - Declarations delegate to Stacks (var, cst, tab, meth).
 * - Reads (get/find) are built from Stacks getters.
 * - Updates delegate to Stacks.assignValue and respect const rule.
 * No HashMap used here.
 */

public class SymbolTable {
    private final Stacks stacks;

    public SymbolTable(Stacks stacks){
        this.stacks = stacks;
    }

    // ---- Declarations ----
    public void declareVar(String name, Object value, String type){
        stacks.declareVar(name, value, type);
    }
    public void declareCst(String name, Object value, String type){
        stacks.declareCst(name, value, type);
    }
    public void declareTab(String name, int size, String type){
        stacks.declareTab(name, size, type);
    }
    public void declareMeth(String name, Object body, String type){
        stacks.declareMeth(name, body, type);
    }

    // ---- Queries ----
    public boolean contains(String name){
        return stacks.getObjectType(name) != null;
    }
    public Symbol findSymbol(String name){
        String kind = stacks.getObjectType(name);
        if (kind == null) return null;
        String type = stacks.getDataType(name);
        Object val  = stacks.getValue(name);
        return new Symbol(name, type, kind, val);
    }

    // ---- Update ----
    public boolean updateValue(String name, Object newValue){
        return stacks.assignValue(name, newValue);
    }
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
