package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class IncrementNode extends InstructionNode {

    private final AstNode ident1;

    public IncrementNode(AstNode ident1) {
        this.ident1 = ident1;
    }

    @Override
    public String toStringTree() {
        return "Increment(" + ident1.toStringTree() + ")";

    }
    
    public AstNode getIdent1() {
        return ident1;
    }


    /**
     * Interprets the increment instruction for either a variable or an array element.
     * <p>
     * If {@code ident1} is an {@link IdentNode}, this method increments the value of the variable,
     * taking into account the current method context and possible variable scoping.
     * If {@code ident1} is a {@link TabNode}, this method increments the value at the specified index
     * of the array, also considering method context and scoping.
     * </p>
     *
     * @param stacks the current execution stacks, providing variable and array value management,
     *               as well as context information for scoping.
     */
    @Override
    public void interpret(Stacks stacks) {
        if (ident1 instanceof IdentNode identNode) {
            String varName = identNode.getNom();
            
            // Try scoped name first if in method context
            String actualVarName = varName;
            if (stacks.isInMethodContext()) {
                String scopedName = stacks.getScopedName(varName);
                if (stacks.getObjectType(scopedName) != null) {
                    actualVarName = scopedName;
                }
            }
            
            int currentValue = (int) stacks.getValue(actualVarName);
            int newValue = currentValue + 1;
            stacks.affecterVal(actualVarName, newValue);
        } else if (ident1 instanceof TabNode tabNode) {
            String varName = tabNode.getIdent().getNom();
            
            // Try scoped name first if in method context
            if (stacks.isInMethodContext()) {
                String scopedName = stacks.getScopedName(varName);
                if (stacks.getObjectType(scopedName) != null) {
                    varName = scopedName;
                }
            }
            
            int index = (int) tabNode.getIndex().evaluate(stacks);
            int currentValue = (int) stacks.getArrayValue(varName, index);
            int newValue = currentValue + 1;
            stacks.setArrayValue(varName, index, newValue);
        }
    }

}