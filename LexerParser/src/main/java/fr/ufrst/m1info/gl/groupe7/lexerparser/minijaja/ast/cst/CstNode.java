package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class CstNode extends AstNode {

   private final Type type;
    private final IdentNode ident;
    private final Expression vexp; // todo : vexpNode

    public CstNode(Type type, IdentNode ident, Expression vexp) {
        this.type = type;
        this.ident = ident;
        this.vexp = vexp;
    }

    public CstNode(Type type, IdentNode ident) {
        this.type = type;
        this.ident = ident;
        this.vexp = null;
    }

    public Type getType() {
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
            sb.append("cst (").append(type).append(" , ").append(ident.toStringTree()).append(",").append("Omega").append(")");
            return sb.toString();
            
        }
        sb.append("cst (").append(type).append(" , ").append(ident.toStringTree()).append(" , ").append(vexp.toStringTree()).append(")");
        return sb.toString();
    }


    @Override
    public void interpret(Stacks stacks) {
        // Get the constant name - use scoped name if inside a method
        String varName = ident.getNom();
        if (stacks.isInMethodContext()) {
            varName = stacks.getScopedName(varName);
        }
        
        if (vexp != null) {
            Object value = vexp.evaluate(stacks);
            stacks.declareCst(varName, value, type);
        } else {
            stacks.declareCst(varName, type);
        }
    }

}