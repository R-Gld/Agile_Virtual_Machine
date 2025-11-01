package fr.ufrst.m1info.gl.groupe7;
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
    private final java.util.Stack<Quad> stack;

    // Constructor: create an empty stack
    public Stacks() {
        stack = new java.util.Stack<>();
    }

    // ============================================================
    // BASIC OPERATIONS
    // ============================================================

    /** Push (Empiler): add a new element on top of the stack */
    public void push(Quad q) {
        stack.push(q);
        System.out.println("Pushed: " + q);
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
        push(q);
    }

    /** Declare a constant */
    public void declareCst(String ident, Object value, String type) {
        Quad q = new Quad(ident, value, "cst", type);
        push(q);
    }

    /** Declare an array (simulated here by its size) */
    public void declareTab(String ident, int size, String type) {
        Quad q = new Quad(ident, "size=" + size, "tab", type);
        push(q);
    }

    /** Declare a method (record its signature only) */
    public void declareMeth(String ident, Object body, String type) {
        Quad q = new Quad(ident, body, "meth", type);
        push(q);
    }

    // ============================================================
    // VALUE ASSIGNMENT & ACCESS METHODS
    // ============================================================

    /** Assign a new value to an existing identifier return false if cst or not found*/
    public boolean assignValue(String ident, Object newValue) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Quad q = stack.get(i);
            if (q.ident.equals(ident)) {
                if (q.object.equals("cst")) {
                    System.out.println("Error: cannot modify a constant!");
                    return false;
                } else {
                    q.value = newValue;
                    System.out.println("Updated value of " + ident + " → " + newValue);
                    return true;
                }
            }
        }
        System.out.println("Identifier not found: " + ident);
        return false;
    }

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

    /** Display the entire stack content */
    public void printStack() {
        System.out.println("\n--- Current Stack Content ---");
        for (int i = stack.size() - 1; i >= 0; i--) {
            System.out.println(stack.get(i));
        }
        System.out.println("------------------------------\n");
    }

}


