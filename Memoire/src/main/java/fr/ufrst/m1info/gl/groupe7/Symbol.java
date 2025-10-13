package fr.ufrst.m1info.gl.groupe7;

public class Symbol {
    final String name;
    final String type;
    final String kind;
    final Object value;

    public Symbol(String name, String type, String kind, Object value) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getKind() {
        return kind;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + " : " + type + " (" + kind + ") = " + value;
    }
}
