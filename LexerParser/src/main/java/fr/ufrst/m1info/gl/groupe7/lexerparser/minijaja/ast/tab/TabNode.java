package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
public class TabNode extends AstNode {

    private final IdentNode ident;
    private final Expression expR;

    public TabNode(IdentNode ident, Expression expR) {
        this.ident = ident;
        this.expR = expR;
    }

    public IdentNode getIdent() {
        return ident;
    }
    public Expression getIndex() {
        return expR;
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        if (expR != null) {
            sb.append("tab").append("(").append(ident.toStringTree()).append(",").append(expR.toStringTree()).append(")");
        } 
        return sb.toString();
    }
    
}
