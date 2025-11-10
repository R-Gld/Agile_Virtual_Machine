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
    
    // Constructor: create an empty stack
    public Stacks() {
        stack = new java.util.Stack<>();
        symbolTable = new SymbolTable();
    }


    // ============================================================
    // BASIC OPERATIONS
    // ============================================================

    /** Push (Empiler): add a new element on top of the stack */
    public void push(Quad q) {
        stack.push(q);
        System.err.println("Pushed: " + q);
    }

    /** Pop (Dépiler): remove the top element from the stack */
    public Quad pop() {
        if (!stack.isEmpty()) {
            Quad q = stack.pop();
            System.out.println("Popped: " + q);
            return q;
        } else {
            System.out.println("Stack is empty. Nothing to pop!");
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
            System.out.println("Swapped top elements: " + q1.ident + " and " + q2.ident);
        } else {
            System.out.println("Cannot swap: not enough elements in the stack.");
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

    // ============================================================
    // DECLARATION METHODS
    // ============================================================

    /** Declare a variable */
    public void declareVar(String ident, Object value, String type) {
        Quad q = new Quad(ident, value, "var", type);
        symbolTable.declareVar(ident, value,  type);
        push(q);
    }

    /** Declare a constant */
    public void declareCst(String ident, Object value, String type) {
        Quad q = new Quad(ident, value, "cst", type);
        symbolTable.declareCst(ident, value,  type);
        push(q);
    }

    /** Declare an array (simulated here by its size) */
    public void declareTab(String ident, int size, String type) {
        Quad q = new Quad(ident, "size=" + size, "tab", type);
        symbolTable.declareTab(ident, size, type);
        push(q);
    }

    /** Declare a method (record its signature only) */
    public void declareMeth(String ident, Object body, String type) {
        Quad q = new Quad(ident, body, "meth", type);
        symbolTable.declareMeth(ident, body,  type);
        push(q);
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
                    System.out.println("Erreur : type incompatible pour " + ident +
                            " (" + q.type + " attendu, mais " + newValue.getClass().getSimpleName() + " fourni)");
                    return false;
                }
                if (q.object.equals("cst")) {
                    System.out.println("Error: cannot modify a constant!");
                    return false;
                } else {
                    q.value = newValue;
                    System.err.println("Updated value of " + ident + " → " + newValue);
                    return true;
                }
            }
        }
        System.out.println("Identifier not found should not go here l 207: " + ident);
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
                System.err.println("⚠️ Type inconnu : " + type);
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
                    " | objet=" + s.getKind() +
                    " | valeur=" + s.getValue());
        } else {
            System.out.println(" Symbole non trouvé : " + name);
        }
    }

}


