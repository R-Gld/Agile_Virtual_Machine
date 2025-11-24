package fr.ufrst.m1info.gl.groupe7.memoire.utils;

public enum Type {
    ENTIER ("integer"),
    BOOLEEN ("boolean"),
    STRING ("String"),
    VOID ("void"),
    FLOAT ("float"),
    RANDOM_TYPE ("random type");

    private final String name;

    private Type(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
