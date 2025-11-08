package fr.ufrst.m1info.gl.groupe7.Memoire;

import java.util.Arrays;

/**
 * SymbolTable
 * ----------------------------------------------------------------------------
 * Implements a hash table using chained lists of Stacks.Quad.
 * Each bucket contains a linked list of Quad elements (ident, value, object,
 * type).
 * - No HashMap or Collections are used.
 * - Based on professor’s MiniJaja specification: “table de hachage avec chaînes
 * de Quad”.
 * - Compatible with project symbols (var, cst, tab, meth).
 * Average complexity: O(1) for insert, search, and update operations.
 */
public class SymbolTable {

    /** Fixed size of the hash table (prime number for better distribution). */
    private static final int TABLE_SIZE = 97;

    /** Each bucket stores a linked list of Quad entries. */
    private final Node[] table;

    /** Number of total symbols stored. */
    private int count = 0;

    /**
     * Node represents a single linked element inside a bucket.
     */
    private static class Node {
        Symbol symbol;
        Node next;

        Node(Symbol symbol) {
            this.symbol= symbol;
        }

        @Override
        public String toString() {
            return symbol.toString();
        }
    }

    /** Constructor: initialize all buckets to null. */
    public SymbolTable() {
        table = new Node[TABLE_SIZE];
    }

    // =========================================================================
    // ======================== HASHING FUNCTION ===============================
    // =========================================================================

    /**
     * Simple polynomial rolling hash based on identifier characters.
     * 
     * @param ident identifier name
     * @return integer index within [0, TABLE_SIZE)
     */
    private int hash(String ident) {
        if (ident == null || ident.isEmpty())
            return 0;
        int h = 0;
        for (int i = 0; i < ident.length(); i++) {
            h = (31 * h + ident.charAt(i)) % TABLE_SIZE;
        }
        return (h < 0) ? -h : h;
    }

    // =========================================================================
    // ======================== CORE UTILITIES =================================
    // =========================================================================

    /**
     * Find a node by identifier name in the correct bucket.
     * 
     * @param ident identifier name
     * @return Node if found, otherwise null
     */
    private Node findNode(String ident) {
        int index = hash(ident);
        Node current = table[index];
        while (current != null) {
            if (current.symbol.getName().equals(ident))
                return current;
            current = current.next;
        }
        return null;
    }

    /**
     * Insert or replace a Quad in the hash table.
     * If the identifier already exists → replace its Quad.
     */
    private void put(Symbol symbol) {
        int index = hash(symbol.getName());
        Node current = table[index];
        if (current == null) {
            table[index] = new Node(symbol);
            count++;
            return;
        }
        Node prev = null;
        while (current != null) {
            if (current.symbol.getName().equals(symbol.getName())) {
                current.symbol = symbol; // replace existing entry
                return;
            }
            prev = current;
            current = current.next;
        }
        prev.next = new Node(symbol);
        count++;
    }

    // =========================================================================
    // ======================== PUBLIC API METHODS =============================
    // =========================================================================

    /** make symbol */
    public boolean creationSymbol(String name, int positionStack, String type) {
        if (name == null || type == null) return false;
        if(!this.contains(name)){

        put(new Symbol(name,type , positionStack));

        System.err.println("Pushed: <" + name  + ", var, " + type + ">");
        return true;
        }else{
            return false;
        }
    }



    /**
     * Update an existing symbol’s value.
     * Constants (cst) cannot be modified.
     * 
     * @param name     identifier to update
     * @param addressStack new position
     * @return true if success, false otherwise
     */
    public boolean updateAddressStack(String name, int addressStack) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("Identifier not found: " + name);
            return false;
        }

        node.symbol.setAddressStack(addressStack); ;
        System.err.println("Updated value of " + name + " new position " + addressStack);
        return true;
    }

    /**
     * Remove a symbol completely from the table.
     * 
     * @param name identifier name
     * @return true if the symbol was found and removed, false otherwise
     */
    public boolean remove(String name) {
        int index = hash(name);
        Node current = table[index];
        Node prev = null;
        while (current != null) {
            if (current.symbol.getName().equals(name)) {
                if (prev == null)
                    table[index] = current.next;
                else
                    prev.next = current.next;
                count--;
                System.err.println("Removed: " + name);
                return true;
            }
            prev = current;
            current = current.next;
        }
        System.err.println("Identifier not found for removal: " + name);
        return false;
    }

    /** Check whether an identifier exists in the table */
    public boolean contains(String name) {
        return findNode(name) != null;
    }

    /** return type of node */
    public String type(String name) {
        Node node = findNode(name);
        if (node == null) return null;
        return node.symbol.getType();
    }

    /**
     * Return a Symbol object built from the corresponding Quad.
     * 
     * @param name identifier name
     * @return Symbol or null if not found
     */
    public Symbol findSymbol(String name) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("Identifier not found: " + name);
            return null;
        }
        return node.symbol;
    }

    /** Return the total number of entries in the table */
    public int size() {
        return count;
    }

    /**
     * Print the full content of the table (for debugging or visualization).
     */
    public void printTable() {
        System.err.println("\n--- Current Symbol Table (Hash) ---");
        for (int i = 0; i < TABLE_SIZE; i++) {
            Node e = table[i];
            if (e != null) {
                System.err.print("[" + i + "] -> ");
                while (e != null) {
                    System.err.print("<" + e.symbol.getName() + ", " + e.symbol.getType() +
                             ", " + e.symbol.getAddressStack()+ "> ");
                    e = e.next;
                }
                System.err.println();
            }
        }
        System.err.println("-----------------------------------\n");
    }

    // =========================================================================
    // ==================== EXTENDED METHODS (for full coverage) ===============
    // =========================================================================

    /**
     * Return the number of symbol (tab).
     */
    public int getCount() {
        return count;
    }




    @Override
    public String toString() {
        return "SymbolTable{" +
                "table=" + Arrays.toString(table).replace("null, ", "") +
                ", count=" + count +
                '}';
    }
}
