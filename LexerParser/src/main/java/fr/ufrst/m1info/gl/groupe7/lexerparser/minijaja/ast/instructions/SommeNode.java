package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;


/**
 * AST node representing an increment-by-expression instruction (somme).
 *
 * <p>This node models an operation that reads the current integer value of a
 * variable, evaluates an expression, adds the evaluated integer value to the
 * variable and writes back the result via {@link Stacks#affecterVal(String, Object)}.</p>
 *
 * <p>Behavior and assumptions:
 * <ul>
 *   <li>The target {@code ident1Node} is expected to be an {@link IdentNode};
 *       if it is not, {@link #interpret(Stacks)} does nothing.</li>
 *   <li>The stacks implementation is expected to return an {@link Integer}
 *       (or a value castable to int) from {@link Stacks#getValue(String)} for
 *       the given variable name.</li>
 *   <li>The expression's {@link Expression#evaluate(Stacks)} is expected to
 *       return an {@link Integer} (or a value castable to int). If the runtime
 *       values are not integers a {@link ClassCastException} may be thrown.</li>
 * </ul>
 * </p>
 */
public class SommeNode extends InstructionNode {

    private final AstNode ident1Node;
    private final Expression expressionNode;

    /**
     * Construct a SommeNode.
     *
     * @param ident1Node the target identifier node (should be an {@link IdentNode})
     * @param expressionNode the expression whose integer value will be added to the target
     */
    public SommeNode(AstNode ident1Node, Expression expressionNode) {
        this.ident1Node = ident1Node;
        this.expressionNode = expressionNode;
    }

    public AstNode getIdent1Node() {
        return ident1Node;
    }

    public Expression getExpressionNode() {
        return expressionNode;
    }

    /**
     * Render this node as a compact tree string used for debugging and tests.
     *
     * @return a string like {@code somme(Ident(x)nbre(3))}
     */
    @Override
    public String toStringTree() {
        return "somme(" + ident1Node.toStringTree() + expressionNode.toStringTree() + ")";
    }

    /**
     * Interpret/execute this somme node using the provided runtime stacks.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>If {@code ident1Node} is an {@link IdentNode}, obtain the variable name.</li>
     *   <li>Read the current value from {@link Stacks#getValue(String)} (expected int).</li>
     *   <li>Evaluate {@code expressionNode.evaluate(stacks)} (expected int).</li>
     *   <li>Compute the sum and assign it back with {@link Stacks#affecterVal(String, Object)}.</li>
     * </ol>
     * </p>
     *
     * <p>Note: this method may throw runtime exceptions (e.g. {@link ClassCastException})
     * if values are not integers; callers/tests should ensure types are correct.</p>
     *
     * @param stacks the runtime stacks/environment used during interpretation
     */
    @Override
    public void interpret(Stacks stacks) {
        if (ident1Node instanceof IdentNode identNode) {
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
            Object valueToAdd = expressionNode.evaluate(stacks);
            int newValue = currentValue + (int) valueToAdd;
            stacks.affecterVal(actualVarName, newValue);
        }else if(ident1Node instanceof  TabNode tabNode){
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
            Object valueToAdd = expressionNode.evaluate(stacks);
            int newValue = currentValue + (int) valueToAdd;
            stacks.setArrayValue(varName, index, newValue);
          
        }
    }
}