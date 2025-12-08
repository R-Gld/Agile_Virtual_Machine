package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import java.util.List;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * AST node representing a "while" loop (tantque).
 *
 * <p>This node holds a boolean expression and a body (InstructionsNode). During
 * interpretation the node will evaluate the condition and set its children to
 * either the body instructions followed by a new TantqueNode (for the next
 * iteration) when the condition is true, or to an empty list when the
 * condition is false.</p>
 */
public class TantqueNode extends InstructionNode {

    private final Expression expressionNode;
    private final InstructionsNode instructionsNode;
    private Iterable<AstNode> children;

    /**
     * Create a new TantqueNode with the given condition expression and body.
     *
     * @param expressionNode the condition expression; expected to evaluate to a Boolean
     * @param instructionsNode the body of the loop as an InstructionsNode
     */
    public TantqueNode(Expression expressionNode, InstructionsNode instructionsNode) {
        this.expressionNode = expressionNode;
        this.instructionsNode = instructionsNode;
    }

    public Expression getExpressionNode() {
        return expressionNode;
    }

    public InstructionsNode getInstructionsNode() {
        return instructionsNode;
    }

    /**
     * Render this node as a compact tree string used for debugging and tests.
     *
     * @return a string representation of this node and its subtree
     */
    @Override
    public String toStringTree() {
        return "tantque(" + expressionNode.toStringTree() + "," + instructionsNode.toString() + ")";
    }

    /**
     * Replace the current children iterable for this node.
     *
     * <p>This is used by {@link #interpret(Stacks)} to update which nodes the
     * walker should visit next (e.g. body instructions plus a new loop node
     * for the next iteration).</p>
     *
     * @param child the new children iterable (may be empty, non-null)
     */
    public void setChildren(Iterable<AstNode> child) {
       this.children = child;
    }

    /**
     * Return the children of this node. May be null if not yet set by interpretation.
     *
     * @return an {@link Iterable} of child {@link AstNode}s or null
     */
    @Override
    public Iterable<AstNode> getChildren() {
        return children;
    }

    /**
     * Interpret the loop node using the provided runtime stacks.
     *
     * <p>If the condition evaluates to {@code true}, the current children are set
     * to the body's children followed by a new TantqueNode to represent the next
     * iteration. If the condition evaluates to {@code false}, the children are set
     * to an empty list.</p>
     *
     * @param stacks the runtime stacks/environment used during interpretation
     */
    @Override
    public void interpret(Stacks stacks) {

        if (Boolean.TRUE.equals(expressionNode.evaluate(stacks))) {
            java.util.List<AstNode> childs = new java.util.ArrayList<>();
            Iterable<AstNode> instructionChildren = this.instructionsNode.getChildren();
            if (instructionChildren != null) {
                for (AstNode n : instructionChildren) {
                    childs.add(n);
                }
            }
            childs.add(new TantqueNode(expressionNode, instructionsNode));
            setChildren(childs);
        } else {
            setChildren(List.of());
        }
    }

}
