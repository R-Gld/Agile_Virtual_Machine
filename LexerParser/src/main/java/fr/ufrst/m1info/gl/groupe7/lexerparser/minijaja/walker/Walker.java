package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>Supports debugging with breakpoints and step-by-step execution via {@link Debug}.</p>
 *
 * <p>Note: {@code AstNode.getChildren()} should return a non-null {@link Iterable}
 * (the default implementation returns an empty iterable) to avoid {@code NullPointerException}.</p>
 */
public class Walker {
    // root of the AST to walk
    private final AstNode root;
    // runtime stacks/environment passed to node interpretation
    private final Stacks stack;
    // debug controller
    private final Debug debug;
    // flag to stop execution
    private boolean stopped = false;
    
    private static final Logger logger = LoggerFactory.getLogger(Walker.class);

    /**
     * Create a new Walker for the given AST root and runtime stacks with debugging support.
     *
     * @param root  the AST root node to traverse (may be null)
     * @param stack the runtime stacks/environment provided to node interpretation
     * @param debug the debug controller for breakpoints and stepping
     */
    public Walker(AstNode root, Stacks stack, Debug debug) {
        this.root = root;
        this.stack = stack;
        this.debug = debug != null ? debug : new Debug();
    }

    /**
     * Create a new Walker without debugging.
     *
     * @param root  the AST root node to traverse (may be null)
     * @param stack the runtime stacks/environment provided to node interpretation
     */
    public Walker(AstNode root, Stacks stack) {
        this(root, stack, new Debug());
    }

    /**
     * Start walking the AST from the configured root.
     *
     * <p>This performs a preorder traversal: the walker visits the node first
     * (invoking {@code interpret}), then recurses into its children.</p>
     */
    public void walk() {
        stopped = false;
        
        if (debug.isEnabled()) {
            logger.debug(" Debug mode: {}", debug.getMode());
            logger.debug("Breakpoints: {}", debug.getBreakPoints());
            logger.debug("Starting execution...\n");
        }
        
        visitNode(root);
        
        if (debug.isEnabled() && !stopped) {
            System.out.println("\n Execution completed.");
        }
    }

    /**
     * Visit a single node: if the node is non-null this method invokes its
     * {@code interpret} method with the configured stacks and then recursively
     * visits each child returned by {@link AstNode#getChildren()}.
     *
     * <p>The method is resilient to a {@code null} node reference (it simply returns).
     * The method also checks if {@link AstNode#getChildren()} returns null and handles it safely.</p>
     *
     * @param node the AST node to visit (may be null)
     */
    private void visitNode(AstNode node) {
        if (node == null || stopped) {
            return;
        }
        
        // Check debug breakpoints / step on every node
        if (debug.isEnabled()) {
            int sourceLineNumber = getSourceLine(node);
            
            boolean shouldContinue = debug.beforeNode(sourceLineNumber, node, stack);
            if (!shouldContinue) {
                stopped = true;
                return;
            }
        }
        
        // Execute the node
        node.interpret(stack);

        // Always visit children (null-safe)
        Iterable<AstNode> children = node.getChildren();
        if (children != null) {
            for (AstNode child : children) {
                if (stopped) break;
                visitNode(child);
            }
        }
    }

    /**
     * Extract the source line number from a node's SourcePosition.
     * 
     * @param node the AST node
     * @return the source line number, or 0 if not available
     */
    private int getSourceLine(AstNode node) {
        if (node == null || node.getSourcePosition() == null) {
            return 0;
        }
        return node.getSourcePosition().line();
    }
    
    /**
     * Get the debug controller.
     * @return the debug controller
     */
    public Debug getDebug() {
        return debug;
    }
    
    /**
     * Get the current line/node counter.
     * @return the current line number
     */
    public int getCurrentLine() {
        return debug.getCurrentLine();
    }
    
    /**
     * Check if execution was stopped.
     * @return true if stopped
     */
    public boolean isStopped() {
        return stopped;
    }
    
    /**
     * Stop the execution.
     */
    public void stop() {
        stopped = true;
    }

}
