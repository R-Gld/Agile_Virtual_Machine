package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class AppelENode extends Expression {

    private final IdentNode ident;
    private final AstNode listexp;

    public AppelENode(IdentNode ident2, AstNode listexp) {
        this.ident = ident2;
        this.listexp = listexp;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public AstNode getExp() {
        return listexp;
    }

    public Object evaluate(Stacks stack) {
        return 0;//TODO :gerer le retour de  appelE
    }

    @Override
    public String toString() {
        return "AppelENode{" + "ident=" + ident + ", exp=" + listexp + '}';
    }

    @Override
    public String toStringTree() {
        return "appelE(" + ident.toStringTree() + "," + listexp.toStringTree() + ")";
    }
    
}
