package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class EcrireLnNode extends EcrireNode {
    private final Object ident1Node;

    public EcrireLnNode(Object ident1Node) {
        super(ident1Node);
        this.ident1Node = ident1Node;
    }
   
    @Override
    public String toStringTree() {
        if (this.ident1Node instanceof Expression ident1) {
           

            return "ecrireln (" + ident1.toStringTree() + ")";
        }
            
        return "ecrireln (" +  this.getIdent1Node() + ")";
    }
    @Override
    public void interpret(Stacks stacks) {
        if (ident1Node instanceof IdentNode ident1) {
            String varName = ident1.getNom();
            
            // Try scoped name first (for function context with recursion support)
            String actualVarName = varName;
            if (stacks.isInMethodContext()) {
                String scopedName = stacks.getScopedName(varName);
                if (stacks.getObjectType(scopedName) != null) {
                    actualVarName = scopedName;
                }
            }
            
            String OT = stacks.getObjectType(actualVarName);
            if (OT == null) {
                OT = stacks.getObjectType(varName);
            }
            
            if (OT != null && (OT.equals("tab") || OT.equals("meth"))) { 
                throw new RuntimeException("Type error: cannot print array directly or method reference");
            }
            
            System.out.println(ident1.evaluate(stacks));
        } else if (ident1Node instanceof Expression expr) {
            System.out.println(expr.evaluate(stacks));
        } else if (ident1Node instanceof TabNode  tabNode) {

            String varName = tabNode.getIdent().getNom();


            if (stacks.isInMethodContext()) {
                String scopedName = stacks.getScopedName(varName);
                if (stacks.getObjectType(scopedName) != null) {
                    varName = scopedName;
                }
            }

            int index = (int) tabNode.getIndex().evaluate(stacks);
            Object currentValue =  stacks.getArrayValue(varName, index);

            if (currentValue instanceof Integer) {
                System.out.println(currentValue);
            }  else if (currentValue instanceof Boolean) {
                System.out.println(currentValue);
            }

        } else {
            System.out.println(ident1Node);
        }

    }
}