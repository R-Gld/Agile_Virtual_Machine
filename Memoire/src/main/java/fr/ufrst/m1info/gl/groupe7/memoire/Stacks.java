package fr.ufrst.m1info.gl.groupe7.memoire;
import java.util.ArrayList;
import java.util.List;

public class Stacks {

    /**
     * Inner class representing a memory cell (Quadruple)
     * ---------------------------------------------------
     * Each cell stores:
     *   - ident : the variable name
     *   - value : the current value
     *   - object : the kind of object (var, cst, tab, meth)
     *   - type : the data type (integer, boolean, void)
     */
    public static class Quad {
        public String ident;
        public Object value;
        public String object;
        public String type;

        public Quad(String ident, Object value, String object, String type) {
            this.ident = ident;
            this.value = value;
            this.object = object;
            this.type = type;
        }

        @Override
        public String toString() {
            return "<" + ident + ", " + value + ", " + object + ", " + type + ">";
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            Quad quad = (Quad) obj;

            return ident.equals(quad.ident) &&
                    ((value == null && quad.value == null) || (value != null && value.equals(quad.value))) &&
                    object.equals(quad.object) &&
                    type.equals(quad.type);
        }

        @Override
        public int hashCode() {
            int result = ident.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + object.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }


    // ------------------------------------------------------------
    // Main structure: a classic LIFO stack (Last In, First Out)
    // ------------------------------------------------------------
    protected final java.util.Stack<Quad> stack;
    private final SymbolTable symbolTable;
    private final Heap heap;
    
    // Constructor: create an empty stack
    public Stacks() {
        stack = new java.util.Stack<>();
        symbolTable = new SymbolTable();
        heap = new Heap();
    }


    // ============================================================
    // BASIC OPERATIONS
    // ============================================================

    /** Push (Empiler): add a new element on top of the stack */
    public void push(Quad q) {
        stack.push(q);
        updateSymbolPositions();
    }

    /** Pop (Dépiler): remove the top element from the stack */
    public Quad pop() {
        if (!stack.isEmpty()) {
            Quad q = stack.pop();
            updateSymbolPositions();
            return q;
        } else {
            return null;
        }
    }

    /** Swap (Échanger): swap the two topmost elements of the stack */
    public void swap() {
        if (stack.size() >= 2) {
            Quad q1 = stack.pop();
            Quad q2 = stack.pop();
            stack.push(q1);
            stack.push(q2);
            updateSymbolPositions();
        }
    }
    /**return the top of the pil */
    public Quad getTop() {
        return stack.isEmpty() ? null : stack.peek();
    }
    /*get all quad from the pill */
    public List<Quad> getStackFromTopToBottom() {
        List<Quad> result = new ArrayList<>();
        for (int i = stack.size() - 1; i >= 0; i--) {
            result.add(stack.get(i));
        }
        return result;
    }
    /**
     * Retourne la position d'un identifiant dans la pile.
     * 0 = bas de la pile, size()-1 = haut.
     * Retourne -1 si l'identifiant n'existe pas.
     */
    public int getStackPosition(String ident) {
        for (int i = 0; i < stack.size(); i++) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                return i;
            }
        }
        return -1;
    }
    // ============================================================
    // DECLARATION METHODS
    // ============================================================

    /** Declare a variable */
    public void declareVar(String ident, Object value, String type) {
        Quad q = new Quad(ident, value, "var", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident,positionStack,type);

    }

    /** Declare a constant */
    public void declareCst(String ident, Object value, String type) {
        Quad q = new Quad(ident, value, "cst", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident,positionStack,type);
    }

    /**
     * Declare an array:
     *  - allocate a block in the Heap with heap.allocate(...)
     *  - store an ArrayInfo in the Quad.value (contains base address + logical size)
     *  - push the Quad on the stack and register the symbol as before
     */
    public void declareTab(String ident, int size, String type) {
        // 1) allocate block in heap
        HeapEntry entry = heap.allocate(ident, size, null);

        if (entry == null) {
            throw new RuntimeException("Heap allocation failed for array '" + ident + "' of size " + size);
        }

        // 2) create ArrayInfo
        ArrayInfo info = new ArrayInfo(entry.getAddress(), size);

        Quad q = new Quad(ident, info, "tab", type);
        push(q);

        // 3) register in symbol table
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident, positionStack, type);
    }


    /** Declare a method (record its signature only) */
    public void declareMeth(String ident, Object body, String type) {
        Quad q = new Quad(ident, body, "meth", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident,positionStack,type);
    }

    // ============================================================
    // VALUE ASSIGNMENT & ACCESS METHODS
    // ============================================================

    /** Assign a new value to an existing identifier return false if cst or not found*/

    /** Get the value of an identifier */
    public Object getValue(String ident) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) return q.value;
        }
        return null;    
    }

    /** Get the object type (var, cst, tab, meth) */
    public String getObjectType(String ident) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) return q.object;
        }
        return null;
    }

    /** Get the data type (integer, boolean, void) */
    public String getDataType(String ident) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) return q.type;
        }
        return null;
    }
    // ============================================================
    // Axiome D'interpretation
    // ============================================================
    public boolean AffecterVal(String ident, Object newValue) {
        if(!symbolTable.contains(ident)){
            return false;
        }
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                // Vérification de la compatibilité de type
                if (!isTypeCompatible(q.type, newValue)) {
                    return false;
                }
                if (q.object.equals("cst")) {
                    return false;
                } else {
                    q.value = newValue;
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Vérifie si la valeur donnée correspond bien au type attendu (sous forme de String)
     */
    private boolean isTypeCompatible(String type, Object value) {
        if (value == null) return true; // null accepté pour tous types

        switch (type.toLowerCase()) {
            case "entier":
            case "integer":
            case "int":
                return value instanceof Integer;

            case "booleen":
            case "boolean":
                return value instanceof Boolean;

            case "chaine":
            case "string":
                return value instanceof String;

            case "void":
                return value == null;

            default:
                return false;
        }
    }



    

    // ============================================================
    // PRINT STACK CONTENT
    // ============================================================
    /** Display the entire stack content */
    public void printStack() {
        System.out.println("\n--- Current Stack Content ---");
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.println(stack.get(i));
        }
        System.out.println("------------------------------\n");
    }
    /**
     * Affiche tout le contenu de la table des symboles.
     */
    public void printSymbolTable() {

        symbolTable.printTable();
    }

    /**
     * Affiche un symbole précis s’il existe.
     */
    public void printSymbol(String name) {

        Symbol s = symbolTable.findSymbol(name);
        if (s != null) {
            System.out.println("🔹 " + s.getName() + " | type=" + s.getType() +
                    " | adress=" + s.getAddressStack()) ;
        } else {
            System.out.println(" Symbole non trouvé : " + name);
        }
    }
    // ------------------------------------------------------------
    // UTILITY: Update SymbolTable positions after stack changes
    // ------------------------------------------------------------
    private void updateSymbolPositions() {
        for (int i = 0; i < stack.size(); i++) {
            Quad q = stack.get(i);
            Symbol s = symbolTable.findSymbol(q.ident);
            if (s != null) {
                symbolTable.updateAddressStack(s.getName(),i);
            }
            // Note: Il est normal que certains Quad temporaires (%TMP%) ne soient pas dans la table des symboles
        }
    }
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Find a Quad by identifier from top to bottom.
     */
    private Quad findQuad(String ident) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                return q;
            }
        }
        return null;
    }
    /**
     * Return the element at array[ index ].
     * Returns null if not found, out-of-bounds, or not an array.
     */
    public Object getArrayValue(String ident, int index) {
        Quad q = findQuad(ident);
        if (q == null) return null;
        if (!"tab".equals(q.object)) return null;

        // q.value is ArrayInfo
        if (!(q.value instanceof ArrayInfo)) return null;
        ArrayInfo info = (ArrayInfo) q.value;

        if (index < 0 || index >= info.getSize()) return null;

        int address = info.getAddress() + index;

        // Use heap.read; may throw if address invalid — you can catch if desired
        return heap.read(address);
    }
    /**
     * Set array[index] = value.
     * Returns true on success, false otherwise.
     */
    public boolean setArrayValue(String ident, int index, Object value) {
        Quad q = findQuad(ident);
        if (q == null) return false;
        if (!"tab".equals(q.object)) return false;
        if (!(q.value instanceof ArrayInfo)) return false;

        ArrayInfo info = (ArrayInfo) q.value;
        if (index < 0 || index >= info.getSize()) return false;

        // Type checking using existing helper
        if (!isTypeCompatible(q.type, value)) return false;

        int address = info.getAddress() + index;
        heap.write(address, value);
        return true;
    }
    // ============================================================
// HEAP UTILITIES
// ============================================================

    /**
     * Print the full content of the heap from Stacks.
     */
    public void printHeap() {
        heap.printHeap();
    }

    /**
     * Get all free blocks currently in the heap.
     * Useful for integration tests.
     */
    public HeapEntry[] getAllHeapEntries() {
        // Simple collection of all entries
        java.util.List<HeapEntry> entries = new java.util.ArrayList<>();

        for (int i = 0; i < 256; i++) { // check each cell in memory table
            Object mem = heap.getMemory()[i];
            if (mem instanceof HeapEntry) {
                entries.add((HeapEntry) mem);
            }
        }

        return entries.toArray(new HeapEntry[0]);
    }

    /**
     * Retrieve the HeapEntry for a given identifier (array or allocated block)
     */
    public HeapEntry getHeapEntry(String id) {
        // Since we allocate via heap.allocate and get the entry back in declareTab or declareVar
        // We'll search through Quads in stack
        for (Quad q : stack) {
            if (q.ident.equals(id)) {
                Object value = q.value;
                if (value instanceof ArrayInfo) {
                    ArrayInfo info = (ArrayInfo) value;
                    return new HeapEntry(id, info.getAddress(), info.getSize(), null, false);
                } else {
                    // normal variable → allocate 1 cell
                    int pos = getStackPosition(id);
                    if (pos >= 0) {
                        return new HeapEntry(id, pos, 1, null, false);
                    }
                }
            }
        }
        return null;
    }
    /**
     * Return all symbols currently in the symbol table.
     * Useful for integration and unit tests.
     */
    public List<Symbol> getAllSymbols() {

        return symbolTable.getAllSymbols();
    }
    /**
     * Return the Heap instance (for testing or inspection purposes)
     */
    public Heap getHeap() {
        return heap;
    }


}


