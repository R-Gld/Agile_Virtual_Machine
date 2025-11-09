package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class AppelINode extends InstructionNode {

    private final IdentNode ident;
    private final ListExpNode listExp;

    public AppelINode(IdentNode ident, ListExpNode listExp) {
        this.ident = ident;
        this.listExp = listExp;
    }

    @Override
    public String toStringTree() {
        return "appelI(" + ident.toStringTree() + "," + listExp.toStringTree() + ")";
    }
    
}
