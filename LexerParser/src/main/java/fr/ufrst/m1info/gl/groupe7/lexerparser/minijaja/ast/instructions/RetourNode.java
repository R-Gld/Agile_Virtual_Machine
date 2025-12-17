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

    /**
     * Interprets the return instruction for the AST node.
     * <p>
     * Evaluates the expression associated with this return node and assigns its value
     * to the class variable currently defined in the stack. If no class variable is defined,
     * a {@link RuntimeException} is thrown.
     *
     * @param stacks the execution stacks containing variable and class context
     * @throws RuntimeException if no class variable is defined in the stack
     */
    @Override
    public void interpret(Stacks stacks) {
        Object exp = this.getExp().evaluate(stacks);

        String varClasse = stacks.getVariableClasse();
        if (varClasse == null) {
            throw new RuntimeException("Error: class variable not defined on the stack"); // TODO: clearer error message
        }
        stacks.affecterVal(varClasse, exp);
    }
}
