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


    @Override
    public void interpret(Stacks stacks) {
        if (ident1 instanceof IdentNode identNode) {
            String varName = identNode.getNom();
            int currentValue = (int) stacks.getValue(varName);
            int newValue = currentValue + 1;
            stacks.AffecterVal(varName, newValue);
        } else if (ident1 instanceof TabNode tabNode) {
            String varName = tabNode.getIdent().getNom();
            int index = (int) tabNode.getIndex().evaluate(stacks);
            int currentValue = (int) stacks.getArrayValue(varName, index);
            int newValue = currentValue + 1;
            stacks.setArrayValue(varName, index, newValue);
        }
    }

}