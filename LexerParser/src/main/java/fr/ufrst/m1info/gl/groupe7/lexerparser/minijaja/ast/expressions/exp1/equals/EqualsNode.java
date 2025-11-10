package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class EqualsNode extends Expression {

    private final Expression exp1; // left expression
    private final Expression exp2; // right expression

    public EqualsNode(Expression exp1, Expression exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    public AstNode getExp1() {
        return exp1;
    }

    public AstNode getExp2() {
        return exp2;
    }

    public Object evaluate(Stacks stack) {
        return exp1.evaluate(stack).equals( exp2.evaluate(stack));
    }

    @Override
   public String toStringTree() {
        return "== (" + exp1.toStringTree() + "," + exp2.toStringTree() + ")";
    }

}
