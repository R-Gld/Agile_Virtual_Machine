package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.context;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * AST node that restores (pops) a method context when interpreted.
 * Used at the end of method/main execution to restore the previous context.
 */
public class RestoreContextNode extends AstNode {
    
    private final String methodName;
    
    /**
     * Create a RestoreContextNode for the given method name.
     * @param methodName the method name to pop from context stack (e.g., "main", "foo")
     */
    public RestoreContextNode(String methodName) {
        this.methodName = methodName;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    @Override
    public void interpret(Stacks stacks) {
        stacks.popContext(methodName);
    }
    
    @Override
    public String toStringTree() {
        return "restoreContext(" + methodName + ")";
    }
}
