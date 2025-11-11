package fr.ufrst.m1info.gl.groupe7.memoire;


/**
 * Represents a symbol entry in the symbol table.
 * Each symbol contains its identifier, object type, data type, and value.
 * No HashMap or external collection is used; this class is a simple data holder.
 */
public record Symbol(String name, String type, String kind, Object value) {

    @Override
    public String toString() {
        return name + " : " + type + " (" + kind + ") = " + value;
    }
}
