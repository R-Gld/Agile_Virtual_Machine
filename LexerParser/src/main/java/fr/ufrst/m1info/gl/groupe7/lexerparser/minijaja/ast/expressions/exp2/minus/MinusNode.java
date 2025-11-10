package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class MinusNode extends Expression {
    
    private final Expression exp2; // left expression
    private final Expression terme; // right term

    public MinusNode(Expression exp2, Expression terme) {
        this.exp2 = exp2;
        this.terme = terme;
       
    }

    public Expression getExp2() {
        return exp2;
    }

    public Expression getTerme() {
        return terme;
    }

    public Object evaluate(Stacks stack) {
        return (int) exp2.evaluate(stack) - (int) terme.evaluate(stack);
    }

    @Override
    public String toStringTree() {
        return "- (" + exp2.toStringTree() + "," + terme.toStringTree() + ")";
    }
}
