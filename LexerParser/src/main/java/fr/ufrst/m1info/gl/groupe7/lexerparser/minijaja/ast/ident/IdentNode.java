package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class IdentNode extends Expression {

    private final String nom;

    public IdentNode(String nom) {
        this.nom = nom;

    }

    public String getNom() {
        return nom;
    }

    @Override
    public Object evaluate(Stacks stack) {
        // If we're inside a method context, try scoped name first (varName@methodName)
        if (stack.isInMethodContext()) {
            String scopedName = stack.getScopedName(this.nom);
            // Check if scoped variable exists
            if (stack.getObjectType(scopedName) != null) {
                return stack.getValue(scopedName);
            }
        }
        // Fall back to global/unscoped name (for class-level variables or main variables)
        return stack.getValue(this.nom);
    }

    @Override
    public String toStringTree() {
        return "Ident(" + nom + ")";
    }

}
