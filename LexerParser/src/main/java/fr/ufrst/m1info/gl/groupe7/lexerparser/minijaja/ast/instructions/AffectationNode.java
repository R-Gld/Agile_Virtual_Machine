package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * AST node representing an assignment (affectation) instruction.
 *
 * <p>This node holds a target identifier (as an {@link AstNode}, typically an
 * {@link IdentNode}) and a right-hand side expression. During interpretation
 * the expression is evaluated and the resulting value is assigned to the
 * variable name resolved from the identifier by calling
 * {@link Stacks#affecterVal(String, Object)}.</p>
 *
 * <p>Only simple identifier targets are supported by this implementation; if
 * the target is not an {@link IdentNode} the {@link #interpret(Stacks)} method
 * does nothing.</p>
 */
public class AffectationNode extends InstructionNode {

    private final AstNode ident1Node;
    private final Expression expression;

    /**
     * Create a new assignment node.
     *
     * @param ident1Node the target identifier node (expected to be {@link IdentNode})
     * @param expression the expression whose evaluated value will be assigned
     */
    public AffectationNode(AstNode ident1Node, Expression expression) {
        this.ident1Node = ident1Node;
        this.expression = expression;
    }

    /**
     * Return the target identifier node.
     *
     * @return the target as an {@link AstNode} (usually {@link IdentNode})
     */
    public AstNode getIdent1Node() {
        return ident1Node;
    }

    /**
     * Return the right-hand side expression of this assignment.
     *
     * @return the {@link Expression} to be evaluated and assigned
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Return a compact tree string representation of this assignment node.
     *
     * @return a string like {@code affectation(Ident(x),nbre(5))}
     */
    @Override
    public String toStringTree() {
        return "affectation(" + ident1Node.toStringTree() + "," + expression.toStringTree() + ")";
    }

    /**
     * Return the children of this node for tree traversal.
     *
     * @return an iterable containing the identifier node and the expression
     */
    @Override
    public Iterable<AstNode> getChildren() {
        return java.util.List.of(ident1Node, expression);
    }

    /**
     * Interpret/execute this assignment node using the provided runtime stacks.
     *
     * <p>The expression is evaluated via {@code expression.evaluate(stacks)} and
     * the resulting value is assigned to the variable whose name is obtained
     * from the {@link IdentNode} target by calling
     * {@link Stacks#affecterVal(String, Object)}.</p>
     *
     * <p>Behavior and side-effects:
     * <ul>
     *   <li>If {@code ident1Node} is not an {@link IdentNode} this method does nothing.</li>
     *   <li>If {@code expression.evaluate} or {@link Stacks#affecterVal} produce
     *       runtime exceptions, they propagate to the caller.</li>
     * </ul>
     * </p>
     *
     * @param stacks the runtime stacks/environment used during interpretation
     */
    @Override
    public void interpret(Stacks stacks) {
        if (ident1Node instanceof IdentNode identNode) {
            String varName = identNode.getNom();
            Object value = expression.evaluate(stacks);
            
            // Try scoped name first if in method context
            String actualVarName = varName;
            if (stacks.isInMethodContext()) {
                String scopedName = stacks.getScopedName(varName);
                if (stacks.getObjectType(scopedName) != null) {
                    actualVarName = scopedName;
                }
            }
            
            // Check type compatibility
            String varType = stacks.getDataType(actualVarName).toString();
            String valueType = value instanceof Integer ? "integer"
                    : value instanceof Boolean ? "boolean"
                    : "unknown";
            if (!varType.equals(valueType)) {
                throw new RuntimeException(String.format(
                        "Type error: cannot assign value of type %s to variable %s of type %s",
                        valueType, varName, varType));
            }
            stacks.affecterVal(actualVarName, value);
        }else if (ident1Node instanceof TabNode tabNode)
        {
         String varName = tabNode.getIdent().getNom();
         
         // Try scoped name first if in method context
         if (stacks.isInMethodContext()) {
             String scopedName = stacks.getScopedName(varName);
             if (stacks.getObjectType(scopedName) != null) {
                 varName = scopedName;
             }
         }
         
         int index = (int) tabNode.getIndex().evaluate(stacks);
         Object value = expression.evaluate(stacks);
         stacks.setArrayValue(varName, index, value);

        }
    }

}
