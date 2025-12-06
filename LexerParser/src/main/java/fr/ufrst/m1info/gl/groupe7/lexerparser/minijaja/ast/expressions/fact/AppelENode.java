package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class AppelENode extends Expression {

    private final IdentNode ident;
    private final ListExpNode listexp;

    public AppelENode(IdentNode ident2, ListExpNode listexp) {
        this.ident = ident2;
        this.listexp = listexp;
        this.type = Type.ENTIER;
    }

    public IdentNode getIdent() {
        return ident;
    }

    public AstNode getExp() {
        return listexp;
    }

    public Object evaluate(Stacks stack) {
        // AppelE does not evaluate to a value directly; it represents a function/method call.

         AppelINode appelI = new AppelINode(ident, listexp);
         appelI.InterpretChildren(stack);
         String varClasse = stack.getVariableClasse();
         System.err.println("[DEBUG] AppelENode evaluate: varClasse = " + varClasse); 
         if (varClasse == null) {
             throw new RuntimeException("Erreur: appelE hors d'une classe");
         }
         return stack.getValue(varClasse);  
       

     
    }

    @Override
    public String toStringTree() {
        return "appelE(" + ident.toStringTree() + "," + listexp.toStringTree() + ")";
    }

}
