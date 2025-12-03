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
        Object exp = this.getExp().evaluate(stacks);

        String varClasse = stacks.getVariableClasse();
        if (varClasse == null) {
            throw new RuntimeException("Erreur: Variable de classe non définie dans la pile"); // TODO : message
                                                                                               // d'erreur plus clair
        }
        stacks.AffecterVal(varClasse, exp);
    }
}
