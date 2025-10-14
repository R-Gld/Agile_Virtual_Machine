package fr.ufrst.m1info.gl.groupe7;

import java.util.*;

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
