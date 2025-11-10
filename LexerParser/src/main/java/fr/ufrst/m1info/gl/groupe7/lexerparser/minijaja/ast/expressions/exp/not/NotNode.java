package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class NotNode extends Expression {

    private final Expression exp; // expression to negate

    public NotNode(Expression exp) {
        this.exp = exp;
         //add !
    }

    public AstNode getExp() {
        return exp;
    }

    @Override
    public Object evaluate(Stacks stack) {
        return  !(Boolean) exp.evaluate(stack);
    }

    @Override
    public String toStringTree() {
        return "non (" + exp.toStringTree() + ")";
    }
}
