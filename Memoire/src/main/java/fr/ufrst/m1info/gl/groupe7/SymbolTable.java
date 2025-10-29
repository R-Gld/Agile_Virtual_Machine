package fr.ufrst.m1info.gl.groupe7;

import java.util.*;

/**
 * Flat symbol table for Release 1: no scopes, one map.
 * Methods openScope/closeScope exist for compatibility but do nothing.
 */
public class SymbolTable {

    private final Map<String, Symbol> table = new LinkedHashMap<>();

    /** Add or replace a symbol in the single global map. */
    public void addSymbol(Symbol s) {
        table.put(s.getName(), s);
    }

    /** Find by name, or null if absent. */
    public Symbol findSymbol(String name) {
        return table.get(name);
    }

    /** Update value keeping other attributes; returns previous or null. */
    public Symbol setValue(String name, Object newValue) {
        Symbol prev = table.get(name);
        if (prev == null) return null;
        Symbol updated = new Symbol(prev.getName(), prev.getType(), prev.getKind(), newValue);
        table.put(name, updated);
        return prev;
    }

    /** Remove a symbol by name. */
    public Symbol remove(String name) {
        return table.remove(name);
    }

    /** For compatibility with former API: no-op (no scopes). */
    public void openScope() { /* no-op */ }

    /** For compatibility with former API: no-op (no scopes). */
    public void closeScope() { /* no-op */ }

    /** Debug print. */
    public void printTable() {
        System.out.println("Symbol Table (flat): " + table.values());
    }

    /** Size helper. */
    public int size() { return table.size(); }
}
