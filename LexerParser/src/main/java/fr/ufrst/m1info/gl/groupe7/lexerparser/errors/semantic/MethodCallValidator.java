package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
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
     * Validates a method call node (AppelI or AppelE).
     * Extracts method name and arguments from the node and performs validation.
     *
     * @param callNode the method call node (must be AppelINode or AppelENode)
     * @return the return type of the method, or null if validation failed
     * @throws IllegalArgumentException if callNode is not an AppelINode or AppelENode
     */
    public Type validateMethodCall(AstNode callNode) {
        String methodName;
        ListExpNode args;

        // Extract method name and arguments based on node type
        if (callNode instanceof AppelINode appelI) {
            methodName = appelI.getIdent().getNom();
            args = appelI.getListExp();
        } else if (callNode instanceof AppelENode appelE) {
            methodName = appelE.getIdent().getNom();
            args = (ListExpNode) appelE.getExp();
        } else {
            throw new IllegalArgumentException(
                "validateMethodCall expects an AppelINode or AppelENode, but got: " +
                (callNode != null ? callNode.getClass().getSimpleName() : "null")
            );
        }

        // Perform validation
        // Search for method declaration, stored as "methodName@returnType" (ex: , "f@integer")
        String methodSignature = scopeResolver.findMethodSignature(methodName);

        if (methodSignature == null) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(callNode),
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
                context.createPosition(callNode),
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
                    context.createPosition(callNode),
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
