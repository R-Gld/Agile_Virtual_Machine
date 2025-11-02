package fr.ufrst.m1info.gl.groupe7;



/**
 * Represents a symbol entry in the symbol table.
 * Each symbol contains its identifier, object type, data type, and value.
 * No HashMap or external collection is used; this class is a simple data holder.
 */
public class Symbol {
    private final String name;
    private final String type;
    private final String kind;
    private final Object value;

    public Symbol(String name, String type, String kind, Object value) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.value = value;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getKind() { return kind; }
    public Object getValue() { return value; }

    @Override
    public String toString() {
        return name + " : " + type + " (" + kind + ") = " + value;
    }
}
