package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;

import java.util.Collections;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Base class for all nodes in the abstract syntax tree (AST).
 *
 * <p>Concrete AST node implementations should override {@link #toStringTree()},
 * {@link #getChildren()} when they have children and {@link #interpret(Stacks)}
 * when they have runtime behaviour.</p>
 */
public abstract class AstNode {

    /**
     * Render this node (and typically its subtree) as a compact tree string.
     * Implementations should return a human readable representation used by
     * tests and debugging.
     *
     * @return the string representation of this node and its subtree
     */
    public abstract String toStringTree();

    /**
     * Delegates to {@link #toStringTree()} so standard toString() calls
     * produce the tree representation.
     *
     * @return the result of {@link #toStringTree()}
     */
    @Override
    public String toString() {
        return toStringTree();
    }

    /**
     * Return the runtime class of this node.
     *
     * @return the {@link Class} object representing the concrete node type
     */
    public Class<?> getNodeType(){
        return this.getClass();
    }
    
    /**
     * Return an iterable over this node's children. The default implementation
     * returns an empty iterable. Nodes that contain child nodes should override
     * this method to expose them so walkers and visitors can traverse the AST.
     *
     * @return an {@link Iterable} of child {@link AstNode}s (never null)
     */
    public Iterable<AstNode> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Interpret/execute this AST node using the provided runtime stacks.
     * Default implementation does nothing. Concrete nodes that have runtime
     * effects should override this method.
     *
     * @param stacks the runtime stacks/environment used during interpretation
     */
    public void interpret(Stacks stacks) {
        // Default implementation does nothing.
        // Subclasses can override this method to provide specific interpretation logic.
    }
   
   
}