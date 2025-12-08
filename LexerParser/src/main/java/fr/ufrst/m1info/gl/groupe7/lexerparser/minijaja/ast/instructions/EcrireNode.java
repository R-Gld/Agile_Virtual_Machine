package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class EcrireNode extends InstructionNode {
    private final Object Ident1Node;
    
    public EcrireNode(Object ident1Node) {
        this.Ident1Node = ident1Node;
    }

    public Object getIdent1Node() {
        return Ident1Node;
    }

    @Override
    public String toStringTree() {
        if (this.Ident1Node instanceof Expression) {
            return "ecrire (" + ((Expression) this.Ident1Node).toStringTree() + ")";
            
        }
        return "ecrire (" + this.Ident1Node + ")";
    }
   
    public void interpret(Stacks stacks) {
        if (Ident1Node instanceof IdentNode ident1) {
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
            
            System.out.print(ident1.evaluate(stacks)); // TODO STREAM ??
        } else if (Ident1Node instanceof Expression expr) {
            System.out.print(expr.evaluate(stacks)); // TODO STREAM ??
        } else if (Ident1Node instanceof TabNode tabNode) {
            String varName = stacks.resolveVariableName(tabNode.getIdent().getNom());


            int index = (int) tabNode.getIndex().evaluate(stacks);
            Object currentValue =  stacks.getArrayValue(varName, index);

            if (currentValue instanceof Integer) {
                System.out.print(currentValue);
            }  else if (currentValue instanceof Boolean) {
                System.out.print(currentValue);
            }

        } else {
            System.out.print(Ident1Node);
        }



    }

    
    
}
