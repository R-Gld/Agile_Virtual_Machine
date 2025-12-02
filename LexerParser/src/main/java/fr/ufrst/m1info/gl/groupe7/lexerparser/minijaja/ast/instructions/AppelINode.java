package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class AppelINode extends InstructionNode {

    private final IdentNode ident;
    private final ListExpNode listExp;

    public AppelINode(IdentNode ident, ListExpNode listExp) {
        this.ident = ident;
        this.listExp = listExp;
    }

    public IdentNode getIdent() { return ident; }

    public ListExpNode getListExp() { return listExp; }

    @Override
    public String toStringTree() {
        return "appelI(" + ident.toStringTree() + "," + listExp.toStringTree() + ")";
    }
    
}
