package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class RetourNode extends InstructionNode {

    private final Expression Exp;

    public RetourNode(Expression Exp) {
        this.Exp = Exp;
    }

    public Expression getExp() {
        return Exp;
    }

    @Override
    public String toStringTree() {
        return "Retour(" + Exp.toStringTree() +
                ')';
    }
   @Override
   public void interpret(Stacks stacks) {

    // TODO: Implement the interpretation logic for the return node here.

   }
}
