package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

public class AppelINode extends InstructionNode {

    private final IdentNode ident;
    private final ListExpNode listExp;
    private final Type type;

    public AppelINode(IdentNode ident, ListExpNode listExp) {
        this.ident = ident;
        this.listExp = listExp;
        this.type = Type.VOID;
    }

    public IdentNode getIdent() { return ident; }

    public ListExpNode getListExp() { return listExp; }

    public Type getType() { return type; }

    @Override
    public String toStringTree() {
        return "appelI(" + ident.toStringTree() + "," + listExp.toStringTree() + ")";
    }
    
}
