package fr.ufrst.m1info.gl.groupe7.memoire.utils;

public enum Type {
    ENTIER ("integer"),
    BOOLEEN ("boolean"),
    STRING ("String"),
    VOID ("void"),
    ANY ("any");  // Type pour la variable de classe

    private final String name;

    Type(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
