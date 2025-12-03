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
    
    // Context tracking: stack of method contexts for recursive calls
    // Each entry is the full context suffix (e.g., "fct", "fct1", "fct2" for recursion)
    private final java.util.Stack<String> contextStack = new java.util.Stack<>();
    
    // Track recursion depth per method name: methodName -> current depth
    private final java.util.Map<String, Integer> recursionDepth = new java.util.HashMap<>();
    
    // Constructor: create an empty stack
    public Stacks() {
        stack = new java.util.Stack<>();
        symbolTable = new SymbolTable();
        heap = new Heap();
    }
    
    // ============================================================
    // CONTEXT MANAGEMENT (for scoped variable lookup with recursion support)
    // ============================================================
    
    /**
     * Push a new method context onto the context stack.
     * Handles recursive calls by appending depth number:
     * - First call to "fct" -> context is "fct"
     * - Second call to "fct" (recursive) -> context is "fct1"
     * - Third call to "fct" -> context is "fct2"
     */
    public void pushContext(String methodName) {
        int depth = recursionDepth.getOrDefault(methodName, 0);
        String contextSuffix;
        if (depth == 0) {
            contextSuffix = methodName;
        } else {
            contextSuffix = methodName + depth;
        }
        recursionDepth.put(methodName, depth + 1);
        contextStack.push(contextSuffix);
    }
    
    /**
     * Pop the current method context from the stack.
     * Decrements the recursion depth for the method.
     */
    public void popContext(String methodName) {
        if (!contextStack.isEmpty()) {
            contextStack.pop();
        }
        int depth = recursionDepth.getOrDefault(methodName, 1);
        if (depth > 0) {
            recursionDepth.put(methodName, depth - 1);
        }
    }
    
    /** 
     * Set the current execution context (method name). Use null for main.
     * @deprecated Use pushContext/popContext for proper recursion support.
     */
    @Deprecated
    public void setCurrentContext(String methodName) {
        // For backward compatibility: clear stack and push new context
        if (methodName == null) {
            if (!contextStack.isEmpty()) {
                String currentMethod = getCurrentMethodName();
                if (currentMethod != null) {
                    popContext(currentMethod);
                }
            }
        } else {
            pushContext(methodName);
        }
    }
    
    /** Get the current execution context suffix (e.g., "fct", "fct1", "fct2"). Returns null if in main. */
    public String getCurrentContext() {
        return contextStack.isEmpty() ? null : contextStack.peek();
    }
    
    /** 
     * Get the base method name from the current context.
     * E.g., if context is "fct2", returns "fct".
     */
    public String getCurrentMethodName() {
        if (contextStack.isEmpty()) return null;
        String context = contextStack.peek();
        // Remove trailing digits to get base method name
        return context.replaceAll("\\d+$", "");
    }
    
    /** Get the current recursion depth for the given method (0 = not in recursion). */
    public int getRecursionDepth(String methodName) {
        return recursionDepth.getOrDefault(methodName, 0);
    }
    
    /** Check if currently executing inside a method (not main). */
    public boolean isInMethodContext() {
        return !contextStack.isEmpty();
    }
    
    /**
     * Build the scoped variable name: varName@contextSuffix.
     * For recursive calls:
     * - First call: varName@fct
     * - Second call: varName@fct1
     * - Third call: varName@fct2
     * If not in a method context, returns the original name.
     */
    public String getScopedName(String varName) {
        if (!contextStack.isEmpty()) {
            return varName + "@" + contextStack.peek();
        }
        return varName;
    }
    
    /**
     * Build the scoped variable name for a specific method context.
     * Useful when you need to declare variables for a method call.
     */
    public String getScopedNameForMethod(String varName, String methodName) {
        int depth = recursionDepth.getOrDefault(methodName, 0);
        if (depth == 0) {
            return varName + "@" + methodName;
        } else {
            return varName + "@" + methodName + depth;
        }
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

                HeapEntry entry = heap.getEntry(base);
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
            Quad q = stack.pop();
            if (("tab".equals(q.object))&& q.value instanceof ArrayInfo info) {

                Symbol existing = symbolTable.findSymbol(q.ident);

                // Si le tableau existe déjà → on incrémente les références
                if (existing != null) {
                    int base = info.getBaseAddress();

                    HeapEntry entry = heap.getEntry(base);
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

    /*Retrait de Declaration */
    public void RetirerDecl(String ident) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                System.err.println("[RETRAIT] Retiring declaration of '" + ident + "' from stack.");
                stack.remove(i);
                updateSymbolPositions();
                symbolTable.remove(ident);
                return;
            }
        }
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
            throw new RuntimeException(" array already in tab declare " + ident);
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
    // VALUE ASSIGNMENT & ACCESS METHODS
    // ============================================================

    /** Assign a new value to an existing identifier return false if cst or not found*/

    /** Get the value of an identifier */
    public Object getValue(String ident) {

        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                // Traitement de la variable Omega (valeur non initialisée)
                if (q.value == Omega.getInstance()) {
                    throw new RuntimeException("Variable '" + ident + "' non initialisee (Omega).");
                }
                return q.value;
            }
        }
        //TODO: throw exception
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
    /**
     * AffecterVal : assign a new value to an identifier.
     * Returns true on success, throws RuntimeException on error (unknown symbol,
     * attempt to assign to constant/array/method, or type mismatch).
     */
    public boolean AffecterVal(String ident, Object newValue) {
        // Verify symbol exists
        Symbol sym = symbolTable.findSymbol(ident);
        if (sym == null) {
            throw new RuntimeException("Variable  :" + ident+ " pas declaree.");
        }

        // Find the Quad from top to bottom
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (!q.ident.equals(ident)) continue;

            // Prevent assigning to constants
            if ("cst".equals(q.object) && !Omega.getInstance().equals(q.value)) {
                throw new RuntimeException("la valeur de la constante " + ident + " ne peut pas être modifiée.");
            }
            if ("tab".equals(q.object)) {
                throw new RuntimeException("Erreur : " + ident+" est un tableau, affectation non permise.");
                
            }

            // For arrays and methods, assignment should be handled by dedicated APIs
            if ("meth".equals(q.object)) {
                throw new RuntimeException("Erreur : " + ident+" est une méthode, affectation non permise.");
            }

            // Check type compatibility
            if (!isTypeCompatible(q.type, newValue)) {
                throw new RuntimeException("Type de variable " + ident +
                        ": attendu " + q.type + " mais reçu " +  newValue.getClass().getSimpleName());
            }

          

            // Perform assignment and update stack entry explicitly
            q.value = newValue;
            stack.set(i, q); // replace to be explicit (Quad is mutable, but keep consistency)
            return true;
        }

        // Shouldn't happen because we checked symbol existence, but keep safe fallback
        throw new RuntimeException("Symbol found in symbol table but not on stack: " + ident);
    }
    /**
     * Vérifie si la valeur donnée correspond bien au type attendu (sous forme de String)
     */
    private boolean isTypeCompatible(Type type, Object value) {
        if (value == null) return true; // null accepté pour tous types

        switch (type) {
            case ENTIER:
                return value instanceof Integer;

            case BOOLEEN:
                return value instanceof Boolean;

            case STRING:
                return value instanceof String;

            case VOID:
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
            System.out.println("[ARRAY FREE] " + ident + "[" + index + "] already empty.");
            return;
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
        HeapEntry entry = new HeapEntry(
                ident,
                base,
                info.getSize() ,
                null,
                false
        );

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


}


