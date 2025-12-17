package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
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
    // callback when debug is in pause
    private final HandlePauseCallback callback;
    // line counter for tracking nodes visited
    private int lineCounter = 0;

    private static final Logger logger = LoggerFactory.getLogger(Walker.class);

    /**
     * Create a new Walker for the given AST root and runtime stacks with debugging support.
     *
     * @param root     the AST root node to traverse (may be null)
     * @param stack    the runtime stacks/environment provided to node interpretation
     * @param debug    the debug controller for breakpoints and stepping
     * @param callback the callback methode called when debug walker is paused
     */
    public Walker(AstNode root, Stacks stack, Debug debug, HandlePauseCallback callback) {
        this.root = root;
        this.stack = stack;
        this.debug = debug != null ? debug : new Debug();
        this.callback = callback;
    }

    /**
     * Create a new Walker without debugging.
     *
     * @param root  the AST root node to traverse (may be null)
     * @param stack the runtime stacks/environment provided to node interpretation
     */
    public Walker(AstNode root, Stacks stack) {
        this(root, stack, new Debug(), null);
    }

    /**
     * Start walking the AST from the configured root.
     *
     * <p>This performs a preorder traversal: the walker visits the node first
     * (invoking {@code interpret}), then recurses into its children.</p>
     */
    public void walk() {
        stopped = false;
        lineCounter = 0;

        if (debug.isEnabled()) {
            System.out.println(" Debug mode: " + debug.getMode());
            System.out.println("Breakpoints: " + debug.getBreakPoints());
            System.out.println("Starting execution...\n");
        }

        visitNode(root, this.callback);

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
    private void visitNode(AstNode node, HandlePauseCallback callback) {
        if (node == null || stopped) {
            return;
        }

        // Increment line counter for each node visited
        lineCounter++;

        // Always display all nodes being visited
        int sourceLineNumber = getSourceLine(node);
        if (sourceLineNumber > 0) {
            System.err.println("  → Visiting source line " + sourceLineNumber + ": " + node.getClass().getSimpleName());
        } else {
            System.err.println("  → Traversing: " + node.getClass().getSimpleName());
        }

        // Trace and check for specific node types before debugging
        if (node instanceof VarNode || node instanceof MethodeNode || node instanceof InstructionNode || node instanceof CstNode || node instanceof TableauNode) {

            if (sourceLineNumber > 0) {
                System.err.println("    [BREAKABLE] Visiting source line " + sourceLineNumber + ": " + node.getClass().getSimpleName());

                // Check debug breakpoints / step before executing
                if (debug.isEnabled()) {
                    System.err.println("      Debug enabled, breakpoints: " + debug.getBreakPoints() + ", checking line: " + sourceLineNumber);
                    boolean shouldContinue = debug.beforeNode(sourceLineNumber, node, stack, callback);
                    if (!shouldContinue) {
                        stopped = true;
                        return;
                    }
                }
            }
        }

        // Execute the node
        node.interpret(stack);

        // Always visit children (null-safe) - don't skip them
        Iterable<AstNode> children = node.getChildren();
        if (children != null) {
            for (AstNode child : children) {
                if (stopped) break;
                visitNode(child, callback);
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
        return lineCounter;
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
