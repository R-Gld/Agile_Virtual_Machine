package fr.ufrst.m1info.gl.groupe7.memoire;



/**
 * Represents a symbol entry in the symbol table.
 * Each symbol contains its identifier, object type, data type, and value.
 * No HashMap or external collection is used; this class is a simple data holder.
 */
public class Symbol {
    private final String name;
    private final String type;
    private int addressStack;//position in the stack


    public Symbol(String name, String type, int addressStack) {
        this.name = name;
        this.type = type;
        this.addressStack = addressStack;

    }

    public String getName() { return name; }
    public String getType() { return type; }
    public int getAddressStack() { return addressStack; }
    public void setAddressStack(int addressStack) { this.addressStack = addressStack; }

    @Override
    public String toString() {
        return name + " : " + type + " (" + addressStack + ")" ;
    }
}
