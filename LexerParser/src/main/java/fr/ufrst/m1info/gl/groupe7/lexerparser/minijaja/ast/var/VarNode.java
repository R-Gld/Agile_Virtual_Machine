package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class VarNode extends AstNode {

    private final String type; // todo : valueNode
    private final IdentNode ident;
    private final Expression vexp; // todo : vexpNode

    public VarNode(String type, IdentNode ident, Expression vexp) {
        this.type = type;
        this.ident = ident;
        this.vexp = vexp;
    }

    public VarNode(String type2, IdentNode ident2) {
        this.type = type2;
        this.ident = ident2;
        this.vexp = null;
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


    @Override
    public void interpret(Stacks stacks) {
        // Implement the interpretation logic for the variable declaration here.
        String varName = ident.getNom();
        Object value;
        if (vexp != null) {
            value = vexp.evaluate(stacks);
        } else {
            value = 0; // Default value for Omega
        }
        stacks.declareVar(varName, value, type);
    }

}
