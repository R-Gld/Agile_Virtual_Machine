package fr.ufrst.m1info.gl.groupe7;

public class Symbol {
    private String name;
    private String type;
    private String kind;
    private Object value;

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
