package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast;

public abstract class AstNode {

    public abstract String toStringTree();

    @Override
    public String toString() {
        return toStringTree();
    }
}