package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class LengthNode extends Expression {
    private int value;
    private final IdentNode ident;

    public LengthNode(IdentNode ident) {
        this.ident = ident;
        this.value = ident.getNom().length();
    }

    public IdentNode getId() {
        return ident;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toStringTree() {
        return "length(" + ident.toStringTree() + ")";
    }

}
