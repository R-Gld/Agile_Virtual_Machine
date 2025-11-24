package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau;

import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;

public class TableauNode extends AstNode {

    private final Type type; 
    private final IdentNode ident;
    private final Expression exp; 

    public TableauNode(Type tableauType, IdentNode ident, Expression exp) {
        this.type = tableauType;
        this.ident = ident;
        this.exp = exp;
    }

    public TableauNode(Type type, IdentNode ident) {
        this.type = type;
        this.ident = ident;
        this.exp = null;
    }

    public Type getType() {
        return type;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public Expression getExp() {
        return exp;
    }

    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        if (exp == null) {
            sb.append("tableau (").append(type).append(" , ").append(ident.toStringTree()).append(",").append("Omega")
                    .append(")");
            return sb.toString();

        }
        sb.append("tableau (").append(type).append(" , ").append(ident.toStringTree()).append(" , ")
                .append(exp.toStringTree()).append(")");
        return sb.toString();
    }

}
