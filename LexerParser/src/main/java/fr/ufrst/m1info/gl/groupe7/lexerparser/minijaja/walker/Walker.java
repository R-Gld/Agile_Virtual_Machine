package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Simple AST walker that traverses an abstract syntax tree in preorder and
 * invokes interpretation / processing on each visited node.
 *
 * <p>The walker visits the root node (if non-null), calls {@code node.interpret(stack)}
 * and then recursively visits the node's children as returned by {@link AstNode#getChildren()}.
 * Concrete interpretation behaviour is delegated to the nodes themselves.</p>
 *
 * <p>Note: {@code AstNode.getChildren()} should return a non-null {@link Iterable}
 * (the default implementation returns an empty iterable) to avoid {@code NullPointerException}.</p>
 */
public class Walker {
    // root of the AST to walk
    private final AstNode root;
    // runtime stacks/environment passed to node interpretation
    private final Stacks stack;

    /**
     * Create a new Walker for the given AST root and runtime stacks.
     *
     * @param root  the AST root node to traverse (may be null)
     * @param stack the runtime stacks/environment provided to node interpretation (must be non-null if nodes use it)
     */
    public Walker(AstNode root, Stacks stack) {
        this.root = root;
        this.stack = stack;
    }

    /**
     * Start walking the AST from the configured root.
     *
     * <p>This performs a preorder traversal: the walker visits the node first
     * (invoking {@code interpret}), then recurses into its children.</p>
     */
    public void walk() {
        visitNode(root);
    }

    /**
     * Visit a single node: if the node is non-null this method invokes its
     * {@code interpret} method with the configured stacks and then recursively
     * visits each child returned by {@link AstNode#getChildren()}.
     *
     * <p>The method is resilient to a {@code null} node reference (it simply returns).
     * Implementations assume {@link AstNode#getChildren()} returns a non-null iterable
     * (it may be empty).</p>
     *
     * @param node the AST node to visit (may be null)
     */
    private void visitNode(AstNode node) {
        if (node == null) {
            return;
        } else {
            node.interpret(stack);
        }

        for (AstNode child : node.getChildren()) {
            visitNode(child);
        }
    }
}