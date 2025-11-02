package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.var;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.ident.IdentNode;

public class VarNode extends AstNode {

    private final String type; // todo : valueNode
    private final IdentNode ident;
    private final Expression vexp; // todo : vexpNode

    public VarNode(String type, IdentNode ident, Expression vexp) {
        this.type = type;
        this.ident = ident;
        this.vexp = vexp;
    }

    public String getType() {
        return type;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public Vexp getExp() {
        return new Vexp(vexp);
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        if (vexp == null) {
            sb.append("var (").append(type).append(" , ").append(ident.toStringTree()).append(",").append("Omega").append(")");
            return sb.toString();
            
        }
        sb.append("var (").append(type).append(" , ").append(ident.toStringTree()).append(" , ").append(vexp.toStringTree()).append(")");
        return sb.toString();
    }
   

}
