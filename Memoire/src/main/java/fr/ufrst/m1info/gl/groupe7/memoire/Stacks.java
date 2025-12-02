package fr.ufrst.m1info.gl.groupe7.memoire;
import fr.ufrst.m1info.gl.groupe7.memoire.Omega.Omega;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

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
        public Type type;

        public Quad(String ident, Object value, String object, Type type) {
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
        //double check
        if (("tab".equals(q.object))&& q.value instanceof ArrayInfo info) {

            Symbol existing = symbolTable.findSymbol(q.ident);

            // Si le tableau existe déjà → on incrémente les références
            if (existing != null) {
                int base = info.getBaseAddress();

                HeapEntry entry = heap.getEntryNotFree(base);
                if (entry != null) {
                    entry.incrementRef();
                    System.out.println("[GC] Increment refCount of array '" + q.ident +
                            "' ⇒ now " + entry.getRefCount());
                }
            }
        }
        stack.push(q);
        updateSymbolPositions();
    }
    /** Push (Empiler): add a new element on top of the stack for new tab */
    public void pushNewTab(Quad q) {

        stack.push(q);
        updateSymbolPositions();
    }

    /** Pop (Dépiler): remove the top element from the stack */
    public Quad pop() {
        if (!stack.isEmpty()) {
            Quad q =stack.peek();

            if (("tab".equals(q.object))&& q.value instanceof ArrayInfo info) {

                Symbol existing = symbolTable.findSymbol(q.ident);

                // Si le tableau existe déjà → on incrémente les références
                if (existing != null) {
                    int base = info.getBaseAddress();

                    HeapEntry entry = heap.getEntryNotFree(base);
                    if (entry != null) {
                        entry.decrementRef();
                        if (entry.getRefCount() == 0) {
                            freeTab(q.ident);
                        }
                        System.out.println("[GC] Increment refCount of array '" + q.ident +
                                "' ⇒ now " + entry.getRefCount());
                    }
                }
            }
            stack.pop();// must be delete after free because free use quad
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
    public void declareVar(String ident, Object value, Type type) {
        Symbol existing = symbolTable.findSymbol(ident);
        if (existing != null) {
            throw new RuntimeException("var already  declare: " + ident);
        }
        Quad q = new Quad(ident, value, "var", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident,positionStack,type);

    }
    /** Declare a variable with default value Omega */
    public  void declareVar(String ident, Type type) {
        Symbol existing = symbolTable.findSymbol(ident);
        if (existing != null) {
            throw new RuntimeException("var already  declare: " + ident);
        }
        declareVar(ident, Omega.getInstance(), type);
    }
    
    /** Declare a constant */
    public void declareCst(String ident, Object value, Type type) {
        Symbol existing = symbolTable.findSymbol(ident);
        if (existing != null) {
            throw new RuntimeException("cst already declare: " + ident);
        }
        Quad q = new Quad(ident, value, "cst", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident, positionStack, type);
    }
    /** Declare a constant with default value Omega */
    public void declareCst(String ident, Type type) {
        Symbol existing = symbolTable.findSymbol(ident);
        if (existing != null) {
            throw new RuntimeException("cst already declare: " + ident);
        }
        declareCst(ident, Omega.getInstance(), type);
    }

    /**
     * Declare an array:
     *  - allocate a block in the Heap with heap.allocate(...)
     *  - store an ArrayInfo in the Quad.value (contains base address + logical size)
     *  - push the Quad on the stack and register the symbol as before
     */
    public void declareTab(String ident, int size, Type type) {
        Symbol tabSymbol = symbolTable.findSymbol(ident);
        if (tabSymbol != null) {
            throw new RuntimeException(" array already in tab declare" + ident);
        }
        int cellPerElement;
        switch (type) {
            case ENTIER:  cellPerElement = 1; break;
            case BOOLEEN: cellPerElement = 1; break;
            default:
                throw new RuntimeException("Unsupported array type: " + type);
        }
        int totalSize = size * cellPerElement;


        HeapEntry entry = heap.allocate(ident, totalSize, null);
        if (entry == null) {
            throw new RuntimeException("Heap allocation failed for array " + ident);
        }
        int baseAddress = entry.getAddress();


        ArrayInfo info = new ArrayInfo(size);
        info.setBaseAddress(baseAddress);


        Quad q = new Quad(ident, info, "tab", type);
        pushNewTab(q);


        int pos = getStackPosition(ident);
        symbolTable.creationSymbol(ident, pos, type);

        System.out.println("→ Array " + ident +
                " allocated: base=" + baseAddress +
                " cells=" + totalSize + " (size=" + size + ")");

    }


    /** Declare a method (record its signature only) */
    public void declareMeth(String ident, Object body, Type type) {
        Symbol existing = symbolTable.findSymbol(ident);
        if (existing != null) {
            throw new RuntimeException("meth already declare: " + ident);
        }
        Quad q = new Quad(ident, body, "meth", type);
        push(q);
        int positionStack = getStackPosition(ident);
        symbolTable.creationSymbol(ident,positionStack,type);
    }
    // ============================================================
    // retirerDecl
    // ============================================================
    /**
     * Removes a declaration (Quad) from the stack and from the symbol table.
     * This is NOT a normal stack pop: it removes a specific identifier located
     * anywhere inside the stack. Normally, pop() handles the classic LIFO case.
     *
     * This method is intended for explicit targeted removal of declarations.
     */
    public void retirerDecl(String ident) {
        Quad q = null;
        int position = -1;

        // 1. Find declaration in stack
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).ident.equals(ident)) {
                q = stack.get(i);
                position = i;
                break;
            }
        }
        if (position == -1) {
            System.out.println("Symbol '" + ident + "' not found — cannot remove.");
            return;
        }

        // 2. If it's an array, release reference
        if ("tab".equals(q.object) && q.value instanceof ArrayInfo info) {
            int base = info.getBaseAddress();
            HeapEntry entry = heap.getEntryNotFree(base);

            if (entry != null) {
                heap.releaseReference(entry); // decrementRef + free si refCount==0
                System.out.println("[GC] Decremented refCount of array '" + q.ident +
                        "' → now " + entry.getRefCount());
            }
        }

        // 3. Remove Quad from stack
        stack.remove(position);
        System.out.println("→ Removed declaration '" + ident + "' from stack.");

        // 4. Remove from symbol table
        symbolTable.remove(ident);
        System.out.println("→ Symbol '" + ident + "' removed from the symbol table.");

        // 5. Update stack positions (if you track positions)
        updateSymbolPositions();
    }
    // ============================================================
    // VALUE ASSIGNMENT & ACCESS METHODS
    // ============================================================

    /** Assign a new value to an existing identifier return false if cst or not found*/

    /** Get the value of an identifier */
    public Object getValue(String ident) {

        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {

                Object value = q.value;

                //  Si la variable est déclarée mais non initialisée
                if (value instanceof Omega) {
                    throw new RuntimeException(
                            "Variable '" + ident + "' is declared but not initialized."
                    );
                }

                return value;
            }
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
    public Type getDataType(String ident) {
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


                if (!isTypeCompatible(q.type, newValue)) {
                    return false;
                }

                // ----------------------------
                //   CAS CONSTANTE
                // ----------------------------
                if (q.object.equals("cst")) {


                    if (q.value instanceof Omega) {
                        q.value = newValue;
                        return true;
                    }


                    throw new RuntimeException("Cannot modify constant: " + ident);
                }

                // ----------------------------
                //   CAS VARIABLE
                // ----------------------------
                q.value = newValue;
                return true;
            }
        }
        return false;
    }
    /**
     * Vérifie si la valeur donnée correspond bien au type attendu (sous forme de String)
     */
    private boolean isTypeCompatible(Type type, Object value) {
        if (value == null) return true; // null accepté pour tous types

        switch (type) {
            case Type.ENTIER:
                return value instanceof Integer;

            case Type.BOOLEEN:
                return value instanceof Boolean;

            case Type.STRING:
                return value instanceof String;

            case Type.VOID:
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

        }
    }
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Find a Quad by identifier from top to bottom.
     */
    public Quad findQuad(String ident) {
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
        if (q == null) throw new RuntimeException("Unknown array " + ident);

        if (!(q.value instanceof ArrayInfo info))
            throw new RuntimeException("Not an array: " + ident);

        if (index < 0 || index >= info.getSize())
            throw new RuntimeException("Index out of bounds: " + ident + "[" + index + "]");


        int address = info.getBaseAddress() + index ;

        return heap.read(address);

    }
    /**
     * Set array[index] = value.
     * throw exeption if not possible
     */
    public void setArrayValue(String ident, int index, Object value) {
        Quad q = findQuad(ident);
        if (q == null) throw new RuntimeException("Unknown array " + ident);
        if (!(q.value instanceof ArrayInfo info)) throw new RuntimeException("Not an array: " + ident);
        if(!(isTypeCompatible(q.type, value))){
            throw new RuntimeException("Element not compatible with type " + q.type + " and value " + value);
        }

        if (index < 0 || index >= info.getSize()) {
            throw new RuntimeException("Index out of bounds");
        }

        // determine cell size for element
        int cellSize;
        if (value instanceof Integer) {
            cellSize = 1;
        }
        else if (value instanceof Boolean) {
            cellSize = 1;
        }
        else {
            throw new RuntimeException("Unsupported type");
        }

        int base = info.getBaseAddress();
        if (base < 0) throw new RuntimeException("Array not allocated");

        // compute address IN THE CONTIGUOUS BLOCK
        int address = base + (index * cellSize);

        heap.write(address, value);
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
    public Object[] getAlMemory() {
        // Simple collection of all entries
        List<Object> entries = new java.util.ArrayList<>();

        for (int i = 0; i < 256; i++) { // check each cell in memory table
            Object mem = heap.getMemory()[i];
            if (mem !=null) {
                entries.add( mem);
            }
        }

        return entries.toArray(new Object[0]);
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
    public void freeArrayElement(String ident, int index) {

        Quad q = findQuad(ident);
        if (q == null)
            throw new RuntimeException("Unknown array " + ident);

        if (!(q.value instanceof ArrayInfo info))
            throw new RuntimeException("Not an array: " + ident);

        if (index < 0 || index >= info.getSize())
            throw new RuntimeException("Index not inside the array size");

        int base = info.getBaseAddress();
        if (base < 0)
            throw new RuntimeException("Array not allocated in heap");



        int address = base + (index );

        // If already empty
        if (heap.read(address) == null) {
            throw new RuntimeException("Array element already free: " + ident + "[" + index + "]");

        }

        // Free the cell inside the contiguous block
        heap.write(address, null);

        System.out.println("[ARRAY FREE] cleared element " + ident + "[" + index + "] at heap address=" + address);
    }
    public void freeTab(String ident) {

        Quad q = findQuad(ident);
        if (q == null)
            throw new RuntimeException("Unknown array " + ident);

        if (!(q.value instanceof ArrayInfo info))
            throw new RuntimeException("Not an array: " + ident);

        int base = info.getBaseAddress();
        if (base < 0)
            throw new RuntimeException("Array not allocated");

        // build a HeapEntry matching the original allocation
        HeapEntry entry = heap.getEntryNotFree(base);
        if (entry == null)
            throw new RuntimeException("HeapEntry not found for array " + ident);


        // Now release the entire block
        //heap.free(entry);
        heap.releaseReference(entry);
        System.out.println("← Freed array " + ident + " (block starting at " + base + ")");
    }
    public int getArrayLength(String ident) {
        Quad q = findQuad(ident);
        if (q == null) throw new RuntimeException("Unknown array " + ident);
        if (!(q.value instanceof ArrayInfo info)) throw new RuntimeException("Not an array: " + ident);


        return info.getSize();

    }
    /**
     * Assigns a reference from one array variable to another.
     * Handles reference counting: the destination loses its old reference,
     * and gains a reference to the source's block.
     */
    public void affecterTab(String identDest, String identSource) {

        Quad qDest = findQuad(identDest);
        Quad qSource = findQuad(identSource);

        if (qDest == null || qSource == null)
            throw new RuntimeException("Unknown array identifier: " + identDest + " or " + identSource);

        // Ensure both symbols are arrays
        if (!(qDest.value instanceof ArrayInfo oldDestInfo) || !(qSource.value instanceof ArrayInfo newSourceInfo))
            throw new RuntimeException("Array reference assignment allowed only between arrays.");

        // 1. Release the previous reference held by the destination

        retirerReference(oldDestInfo);

        // 2. Add a reference to the source block

        ajouterReference(newSourceInfo);

        // 3. Update the Quad's value to point to the new array reference
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(identDest)) {
                // create a new Quad with the same ident and object type, but updated value
                Quad newQuad = new Quad(q.ident, newSourceInfo, q.object,q.type);
                stack.set(i, newQuad); // replace old Quad
                break;
            }
        }

        System.out.println("[REF COPY] " + identDest + " = " + identSource +
                "  (base=" + newSourceInfo.getBaseAddress() +
                ", refCount=" + heap.getEntry(newSourceInfo.getBaseAddress()).getRefCount() + ")");
    }
    /**
     * Decrements the reference counter of a heap block.
     * If the refCount reaches 0, the block is physically freed in the heap.
     */
    public void retirerReference(ArrayInfo info) {
        int base = info.getBaseAddress();
        HeapEntry entry = heap.getEntry(base);

        if (entry == null) return; // Already freed or invalid

        entry.decrementRef();

        if (entry.getRefCount() == 0) {
            heap.free(entry);
            System.out.println("   [GC] Block freed because refCount reached 0 (base=" + base + ")");
        } else {
            System.out.println("   [GC] refCount-- → " + entry.getRefCount() + " (base=" + base + ")");
        }
    }
    /**
     * Increments the reference counter of a heap block.
     * Used when a new variable points to an already allocated array.
     */
    public void ajouterReference(ArrayInfo info) {
        int base = info.getBaseAddress();
        HeapEntry entry = heap.getEntry(base);

        if (entry == null) return; // Should never happen but safe

        entry.incrementRef();

        System.out.println("   [GC] refCount++ → " + entry.getRefCount() + " (base=" + base + ")");
    }


}


