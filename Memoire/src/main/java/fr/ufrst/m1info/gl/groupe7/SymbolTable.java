package fr.ufrst.m1info.gl.groupe7;

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
        Stacks.Quad quad;
        Node next;

        Node(Stacks.Quad q) {
            this.quad = q;
        }

        @Override
        public String toString() {
            return quad.toString();
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
            if (current.quad.ident.equals(ident))
                return current;
            current = current.next;
        }
        return null;
    }

    /**
     * Insert or replace a Quad in the hash table.
     * If the identifier already exists → replace its Quad.
     */
    private void put(Stacks.Quad q) {
        int index = hash(q.ident);
        Node current = table[index];
        if (current == null) {
            table[index] = new Node(q);
            count++;
            return;
        }
        Node prev = null;
        while (current != null) {
            if (current.quad.ident.equals(q.ident)) {
                current.quad = q; // replace existing entry
                return;
            }
            prev = current;
            current = current.next;
        }
        prev.next = new Node(q);
        count++;
    }

    // =========================================================================
    // ======================== PUBLIC API METHODS =============================
    // =========================================================================

    /** Declare a variable */
    public boolean declareVar(String name, Object value, String type) {
        if (name == null || type == null) return false;
        if(!this.contains(name)){
        put(new Stacks.Quad(name, value, "var", type));

        System.err.println("Pushed: <" + name + ", " + value + ", var, " + type + ">");
        return true;
        }else{
            return false;
        }
    }

    /** Declare a constant */
    public boolean declareCst(String name, Object value, String type) {
        if (name == null || type == null) return false;
        if(!this.contains(name)){
        put(new Stacks.Quad(name, value, "cst", type));
    
        System.err.println("Pushed: <" + name + ", " + value + ", cst, " + type + ">");
        return true;
        }else{
            return false;
        }
    }

    /** Declare an array (tab) */
    public boolean declareTab(String name, int size, String type) {
        if (name == null || type == null) return false;
        // BEGIN: Fix stacks reference
        if(!this.contains(name)){
        // END: Fix stacks reference
            put(new Stacks.Quad(name, "size=" + size, "tab", type));
            System.err.println("Pushed: <" + name + ", size=" + size + ", tab, " + type + ">");
            return true;
        } else {
            return false;
        }
    }

    /** Declare a method (meth) */
    public boolean declareMeth(String name, Object body, String type) {
        if (name == null || type == null)
            return false;
        put(new Stacks.Quad(name, body, "meth", type));
        System.err.println("Pushed: <" + name + ", " + body + ", meth, " + type + ">");
        return true;
    }

    /**
     * Update an existing symbol’s value.
     * Constants (cst) cannot be modified.
     * 
     * @param name     identifier to update
     * @param newValue new value to assign
     * @return true if success, false otherwise
     */
    public boolean updateValue(String name, Object newValue) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("Identifier not found: " + name);
            return false;
        }
        if ("cst".equals(node.quad.object)) {
            System.err.println("Error: cannot modify a constant!");
            return false;
        }
        node.quad.value = newValue;
        System.err.println("Updated value of " + name + " = " + newValue);
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
            if (current.quad.ident.equals(name)) {
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
        if (contains(name)) {
            return findNode(name).quad.type;
        } else {
            return null;
        }

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
        return new Symbol(node.quad.ident, node.quad.type, node.quad.object, node.quad.value);
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
                    System.err.print("<" + e.quad.ident + ", " + e.quad.value +
                            ", " + e.quad.object + ", " + e.quad.type + "> ");
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

    /** Assign a new value to a variable (non-constant symbol). */
    public boolean assign(String name, Object newValue) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("assign(): symbol not found: " + name);
            return false;
        }
        if ("cst".equals(node.quad.object)) {
            System.err.println("assign(): cannot modify constant " + name);
            return false;
        }
        node.quad.value = newValue;
        System.err.println("assign(): updated " + name + " = " + newValue);
        return true;
    }

    /** Lookup a symbol by its name. */
    public Symbol lookup(String name) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("lookup(): not found: " + name);
            return null;
        }
        return new Symbol(node.quad.ident, node.quad.type, node.quad.object, node.quad.value);
    }

    /** Check if a symbol is a constant (cst). */
    public boolean isConst(String name) {
        Node node = findNode(name);
        if (node == null)
            return false;
        return "cst".equals(node.quad.object);
    }

    public boolean updateType(String name, String newType) {
        Node node = findNode(name);
        if (node == null) {
            System.err.println("updateType(): symbol not found: " + name);
            return false;
        }
        node.quad.type = newType;
        System.err.println("updateType(): changed type of " + name + " to " + newType);
        return true;
    }

    /**
     * Return the length of an array symbol (tab).
     */
    public int lengthOf(String name) {
        Node node = findNode(name);
        if (node == null || !"tab".equals(node.quad.object)) {
            System.err.println("lengthOf(): not a tab or not found: " + name);
            return -1;
        }
        try {
            String s = node.quad.value.toString();
            if (s.startsWith("size=")) {
                return Integer.parseInt(s.substring(5));
            }
        } catch (Exception e) {
            System.err.println("lengthOf(): Exception - " + e.getMessage());
        }

        return -1;
    }

    @Override
    public String toString() {
        return "SymbolTable{" +
                "table=" + Arrays.toString(table).replace("null, ", "") +
                ", count=" + count +
                '}';
    }
}
