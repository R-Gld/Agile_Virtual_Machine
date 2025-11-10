package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class TabNode extends AstNode {

    private final IdentNode ident;
    private final AstNode expR;

    public TabNode(IdentNode ident, AstNode expR) {
        this.ident = ident;
        this.expR = expR;
    }

    public IdentNode getIdent() {
        return ident;
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
