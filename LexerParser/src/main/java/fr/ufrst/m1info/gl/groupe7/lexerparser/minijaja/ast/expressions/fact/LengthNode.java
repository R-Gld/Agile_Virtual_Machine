package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class LengthNode extends Expression {

    private final IdentNode ident;

    public LengthNode(IdentNode ident) {
        this.ident = ident;
    }

    public IdentNode getId() {
        return ident;
    }

    public Object evaluate(Stacks stack) {
        String varName = ident.getNom();
        
        // Try scoped name first if in method context
        if (stack.isInMethodContext()) {
            String scopedName = stack.getScopedName(varName);
            if (stack.getObjectType(scopedName) != null) {
                varName = scopedName;
            }
        }
        
        String objType = stack.getObjectType(varName);
        if (objType == null || !objType.equals("tab")) {
            throw new RuntimeException("Type error: length can only be applied to arrays");
        }

        return stack.getArrayLength(varName);
    }   

    @Override
    public String toStringTree() {
        return "length(" + ident.toStringTree() + ")";
    }

}
