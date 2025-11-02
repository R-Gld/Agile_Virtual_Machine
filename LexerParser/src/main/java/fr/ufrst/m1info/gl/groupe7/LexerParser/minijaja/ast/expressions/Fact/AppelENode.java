package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Fact;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.ident.IdentNode;

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
    
    public int getValue() {
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
