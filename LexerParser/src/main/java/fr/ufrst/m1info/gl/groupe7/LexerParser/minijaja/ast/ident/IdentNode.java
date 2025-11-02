package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.ident;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;

public class IdentNode extends AstNode {

    private final String nom;

    public IdentNode(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return nom;
    }

    @Override
    public String toStringTree() {
        return "Ident(" + nom + ")";
    }

}
