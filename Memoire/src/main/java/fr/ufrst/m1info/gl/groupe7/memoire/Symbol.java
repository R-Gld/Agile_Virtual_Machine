package fr.ufrst.m1info.gl.groupe7.memoire;


import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Represents a symbol entry in the symbol table.
 * Each symbol contains its identifier, object type, data type, and value.
 * No HashMap or external collection is used; this class is a simple data holder.
 */
public class Symbol {
    private final String name;
    private final Type type;
    private int addressStack;//position in the stack
    private final boolean isArray; // true if this symbol represents an array


    public Symbol(String name, Type type, int addressStack) {
        this(name, type, addressStack, false);
    }

    public Symbol(String name, Type type, int addressStack, boolean isArray) {
        this.name = name;
        this.type = type;
        this.addressStack = addressStack;
        this.isArray = isArray;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public int getAddressStack() { return addressStack; }
    public void setAddressStack(int addressStack) { this.addressStack = addressStack; }
    public boolean isArray() { return isArray; }

    @Override
    public String toString() {
        String arrayMarker = isArray ? "[]" : "";
        return name + " : " + type + arrayMarker + " (" + addressStack + ")" ;
    }
}
