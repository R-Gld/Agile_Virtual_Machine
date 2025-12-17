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
   
    /**
     * Interprets the EcrireNode (print statement) by evaluating the node's argument and logging its value.
     * Handles different types of nodes:
     * <ul>
     *   <li>If the argument is an identifier, resolves its value in the current context (including method scope) and logs it, 
     *       throwing an exception if the identifier refers to an array or method.</li>
     *   <li>If the argument is an expression, evaluates and logs its result.</li>
     *   <li>If the argument is an array access (TabNode), evaluates the index, retrieves the value at that index, and logs it if it is an integer or boolean.</li>
     *   <li>Otherwise, logs the argument as is.</li>
     * </ul>
     * 
     * @param stacks The current execution stacks, providing variable and context resolution.
     * @throws RuntimeException if attempting to print an array or method reference directly.
     */
    public void interpret(Stacks stacks) {
        switch (Ident1Node) {
            case IdentNode ident1 -> {
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
            }
            case TabNode tabNode -> {
                String varName = stacks.resolveVariableName(tabNode.getIdent().getNom());


                int index = (int) tabNode.getIndex().evaluate(stacks);
                Object currentValue = stacks.getArrayValue(varName, index);

                if (currentValue instanceof Integer) {
                    logger.info("{}", currentValue);
                } else if (currentValue instanceof Boolean) {
                    logger.info("{}", currentValue);
                }

            }
            case Expression expr -> logger.info("{}", expr.evaluate(stacks));
            case null, default -> logger.info("{}", Ident1Node);
        }



    }

    
    
}
