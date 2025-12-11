package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.List;

/**
 * Validator for method calls in the MiniJaja AST.
 * Performs comprehensive validation: declaration checking, parameter count, and parameter types.
 */
public class MethodCallValidator {

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;
    private final DeclarationCollector declarationCollector;

    /**
     * Constructor.
     *
     * @param context the semantic context
     * @param scopeResolver the scope resolver for method lookup
     * @param declarationCollector the declaration collector for parameter type extraction
     */
    public MethodCallValidator(SemanticContext context,
                               ScopeResolver scopeResolver,
                               DeclarationCollector declarationCollector) {
        this.context = context;
        this.scopeResolver = scopeResolver;
        this.declarationCollector = declarationCollector;
    }

    /**
     * Validates a method call with the given method name and arguments.
     * This method performs all necessary checks: method declaration, parameter count, and parameter types.
     *
     * @param methodName the name of the method being called (unqualified)
     * @param args the arguments passed to the method (can be null for methods with no parameters)
     * @return the return type of the method, or null if validation failed
     */
    public Type validateMethodCall(String methodName, ListExpNode args) {
        // Search for method declaration, stored as "methodName@returnType" (ex: , "f@integer")
        String methodSignature = scopeResolver.findMethodSignature(methodName);

        if (methodSignature == null) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(),
                String.format("Undeclared method: method '%s' has not been declared. " +
                              "Make sure to declare the method before calling it.", methodName)
            );
            return null;
        }

        // Get the MethodeNode from stacks
        Object methodValue = context.getStacks().getValue(methodSignature);
        if (!(methodValue instanceof MethodeNode methodeNode)) {
            // This shouldn't happen if symbol table is correct
            return null;
        }

        // Extract expected parameter types from the MethodeNode
        List<Type> expectedParamTypes = declarationCollector.collectParameterTypes(methodeNode.getEntetes());

        // Get actual parameter types
        List<Type> actualParamTypes = declarationCollector.collectArgumentTypes(args);

        // Check parameter count
        if (expectedParamTypes.size() != actualParamTypes.size()) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(),
                String.format("Method call error: method '%s' expects %d parameter(s), but got %d. " +
                              "Ensure the number of arguments matches the method signature.",
                              methodName, expectedParamTypes.size(), actualParamTypes.size())
            );
            return null;
        }

        // Check parameter types
        for (int i = 0; i < expectedParamTypes.size(); i++) {
            Type expected = expectedParamTypes.get(i);
            Type actual = actualParamTypes.get(i);

            if (actual != null && expected != actual) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Method call error: parameter %d of method '%s' expects type '%s', but got '%s'. " +
                                  "Ensure argument types match the method signature.",
                                  i + 1, methodName, expected, actual)
                );
            }
        }

        // Return the method's return type
        return methodeNode.getTypeMeth();
    }
}
