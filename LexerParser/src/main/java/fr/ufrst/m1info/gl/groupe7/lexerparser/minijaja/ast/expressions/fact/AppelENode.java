package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import java.util.List;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;

public class AppelENode extends Expression {

    private final IdentNode ident;
    private final ListExpNode listexp;

    public AppelENode(IdentNode ident2, ListExpNode listexp) {
        this.ident = ident2;
        this.listexp = listexp;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public AstNode getExp() {
        return listexp;
    }

    public Object evaluate(Stacks stack) {
        // AppelE does not evaluate to a value directly; it represents a function/method call.

        // AppelINode appelI = new AppelINode(ident, listexp);
        // appelI.interpret(stack);
        // String varClasse = stacks.getVariableClasse(); 
        // if (varClasse == null) {
        //     throw new RuntimeException("Erreur: appelE hors d'une classe");
        // }
        // return stacks.getValue(varClasse);

        return null; // or throw an exception if evaluation is not applicable

     
    }

    @Override
    public String toStringTree() {
        return "appelE(" + ident.toStringTree() + "," + listexp.toStringTree() + ")";
    }

}
