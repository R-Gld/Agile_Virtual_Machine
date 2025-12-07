package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Symbol;

import java.util.List;

/**
 * Handles scope resolution and name mangling for variables and methods.
 * This class is stateless, all state is accessed via SemanticContext.
 */
public class ScopeResolver {

    private final SemanticContext context;

    /**
     * Constructor.
     *
     * @param context the semantic context containing scope state
     */
    public ScopeResolver(SemanticContext context) {
        this.context = context;
    }

    /**
     * Resolve the scope for a given variable name using the same logic as the compiler.
     * Searches in order: current scope (method) > main > global
     *
     * @param variableName the variable name (without scope suffix)
     * @return the scope string ("global", "main", or method signature)
     */
    public String resolveVariableScope(String variableName) {
        // First search in current scope (method parameters/locals)
        if (context.getCurrentScopeVariables().contains(variableName)) {
            return context.getCurrentScope();
        }

        // Then search in main
        if (context.getMainLocalVariables().contains(variableName)) {
            return "main";
        }

        // Finally, assume it's global (will be validated later)
        return "global";
    }

    /**
     * Qualify a variable name with its scope suffix (name mangling)
     * @param varName the variable name
     * @return the qualified name (ex: , "x@global", "local@main", "param@f@int")
     */
    public String qualifyName(String varName) {
        return varName + "@" + context.getCurrentScope();
    }

    /**
     * Qualify a variable name with resolved scope
     * @param varName the variable name
     * @return the qualified name with resolved scope
     */
    public String resolveAndQualifyName(String varName) {
        String scope = resolveVariableScope(varName);
        return varName + "@" + scope;
    }

    /**
     * Find a method's full signature by searching for methodName@*
     * Methods are stored as "methodName@returnType" (ex: , "f@integer")
     *
     * @param methodName the unqualified method name
     * @return the full method signature (ex: , "f@integer"), or null if not found
     */
    public String findMethodSignature(String methodName) {
        String prefix = methodName + "@";

        // Get all symbols and search for matching method
        List<Symbol> allSymbols = context.getSymbolTable().getAllSymbols();
        for (Symbol symbol : allSymbols) {
            String symbolName = symbol.getName();
            if (symbolName.startsWith(prefix)) {
                // Verify it's actually a method by checking the value in stack
                Object value = context.getStacks().getValue(symbolName);
                if (value instanceof MethodeNode) {
                    return symbolName;
                }
            }
        }

        return null;
    }
}
