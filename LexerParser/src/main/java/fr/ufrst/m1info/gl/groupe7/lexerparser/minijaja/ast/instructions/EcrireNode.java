package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcrireNode extends InstructionNode {

    private static final Logger logger = LoggerFactory.getLogger(EcrireNode.class);
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
            
            logger.info("{}", ident1.evaluate(stacks));
        } else if (Ident1Node instanceof Expression expr) {
            logger.info("{}", expr.evaluate(stacks));
        } else if (Ident1Node instanceof TabNode tabNode) {
            String varName = stacks.resolveVariableName(tabNode.getIdent().getNom());


            int index = (int) tabNode.getIndex().evaluate(stacks);
            Object currentValue =  stacks.getArrayValue(varName, index);

            if (currentValue instanceof Integer) {
                logger.info("{}", currentValue);
            }  else if (currentValue instanceof Boolean) {
                logger.info("{}", currentValue);
            }

        } else {
            logger.info("{}", Ident1Node);
        }



    }

    
    
}
