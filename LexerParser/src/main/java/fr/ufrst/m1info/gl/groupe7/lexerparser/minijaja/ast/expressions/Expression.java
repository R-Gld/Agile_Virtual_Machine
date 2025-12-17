package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Base class for all expression nodes in the AST.
 *
 * <p>Concrete expression nodes must implement {@link #evaluate(Stacks)} to
 * compute and return their runtime value. The returned object may be an
 * Integer, Boolean or any other type depending on the expression kind. Callers
 * should inspect the returned value with instanceof (or use a typed wrapper
 * value class) to handle different result types safely.</p>
 *
 * <p>Expressions are also AST nodes and may participate in tree traversal via
 * {@link AstNode#getChildren()} and {@link AstNode#interpret(Stacks)} when
 * appropriate.</p>
 */
public abstract class Expression extends AstNode {

    /**
     * Evaluate this expression in the context of the provided runtime stacks.
     *
     * @param stacks the runtime stacks/environment used during evaluation; may be
     *               consulted or updated by the expression (must not be null
     *               if the expression relies on runtime state)
     * @return the evaluated value of the expression. Typical returned types:
     *         <ul>
     *           <li>{@link Integer} for numeric expressions</li>
     *           <li>{@link Boolean} for boolean expressions</li>
     *           <li>other object types for more complex expressions</li>
     *         </ul>
     *         Callers should perform an instanceof check or use a project-specific
     *         value wrapper to discriminate the actual type at runtime.
     *
     * @throws RuntimeException if evaluation fails (e.g. missing variable, type error)
     */
    public abstract Object evaluate(Stacks stacks);

}