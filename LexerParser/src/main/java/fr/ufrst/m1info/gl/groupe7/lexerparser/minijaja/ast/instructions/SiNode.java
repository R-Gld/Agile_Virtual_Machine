package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * AST node representing a conditional (if) instruction.
 *
 * <p>A SiNode stores a condition expression, a "then" block (InstructionsNode)
 * and an optional "else" block. During interpretation the node evaluates the
 * condition and updates its children to the instructions of the chosen branch
 * so that a walker can continue traversal/execution in preorder.</p>
 */
public class SiNode extends InstructionNode {

    private final Expression expressionNode;
    private final InstructionsNode instructionsNode;
    private final InstructionsNode instructionsNode2;
    private Iterable<AstNode> children;

    /**
     * Create an if node without an else branch.
     *
     * @param expressionNode the condition expression (expected to evaluate to a Boolean)
     * @param instructionsNode the instructions to execute when the condition is true
     */
    public SiNode(Expression expressionNode, InstructionsNode instructionsNode) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
        this.instructionsNode2 = null;
    }

    /**
     * Create an if node with an else branch.
     *
     * @param expressionNode the condition expression (expected to evaluate to a Boolean)
     * @param instructionsNode the instructions to execute when the condition is true
     * @param instructionsNode2 the instructions to execute when the condition is false
     */
    public SiNode(Expression expressionNode, InstructionsNode instructionsNode,
            InstructionsNode instructionsNode2) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
        this.instructionsNode2 = instructionsNode2;
    }

    /**
     * Replace the current children iterable for this node.
     *
     * <p>This method is used by {@link #interpret(Stacks)} to set which nodes
     * should be visited next by a walker (for example the chosen branch's
     * instructions).</p>
     *
     * @param child the new children iterable (may be null or empty)
     */
    public void setchildren(Iterable<AstNode> child) {
       this.children = child;
    }

    /**
     * Return the children of this node as set by interpretation.
     *
     * @return an {@link Iterable} of child {@link AstNode}s, or {@code null} if
     *         interpretation has not set children yet
     */
    @Override
    public Iterable<AstNode> getChildren() {
        return children;
    }

    /**
     * Render this node as a compact tree string used for debugging and tests.
     *
     * @return a string representation of this node and its subtree
     */
    @Override
    public String toStringTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("si (").append(expressionNode.toStringTree()).append(",").append(instructionsNode.toString());
        if (instructionsNode2 != null)
            sb.append(instructionsNode2);
        else
            sb.append("inil");
        sb.append(")");
        return sb.toString();
    }

    /**
     * Get the condition expression of this if statement.
     *
     * @return the condition expression
     */
    public Expression getExpressionNode() {
        return expressionNode;
    }

    /**
     * Get the "then" branch instructions.
     *
     * @return the instructions to execute when the condition is true
     */
    public InstructionsNode getInstructionsNode() {
        return instructionsNode;
    }

    /**
     * Get the "else" branch instructions.
     *
     * @return the instructions to execute when the condition is false, or null if no else branch
     */
    public InstructionsNode getInstructionsNode2() {
        return instructionsNode2;
    }

    /**
     * Interpret this conditional node using the provided runtime stacks.
     *
     * <p>The condition expression is evaluated and expected to return a Boolean.
     * If it evaluates to {@code true}, this node's children are set to the
     * {@code then} branch instructions. If it evaluates to {@code false} and an
     * {@code else} branch is present, children are set to the {@code else}
     * branch instructions. Otherwise children remain {@code null} (no branch).</p>
     *
     * <p>Note: a {@link ClassCastException} may be thrown if the expression does
     * not return a {@code Boolean} value; callers/tests should ensure expressions
     * used as conditions produce boolean results.</p>
     *
     * @param stacks the runtime stacks/environment used during interpretation
     */
    @Override
    public void interpret(Stacks stacks) {
        // Always reset children to avoid stale state from previous interpretations
        // (critical when same AST node is reused across recursive calls)
        setchildren(java.util.List.of());
        
        if ((Boolean) expressionNode.evaluate(stacks)) {
            setchildren(instructionsNode.getChildren());
        } else if (instructionsNode2 != null) {
            setchildren(instructionsNode2.getChildren());
        }
        // If condition is false and no else branch, children remains empty
    }

}