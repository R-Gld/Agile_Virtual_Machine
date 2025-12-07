package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Type checker for the MiniJaja AST.
 * Second-pass analysis: validates type compatibility and usage throughout the AST.
 */
public class TypeChecker {

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;
    private final DeclarationCollector declarationCollector;
    private final TypeInferenceEngine typeInferenceEngine;
    private final MethodCallValidator methodCallValidator;

    /**
     * Constructor.
     *
     * @param context the semantic context
     * @param scopeResolver the scope resolver
     * @param declarationCollector the declaration collector
     * @param typeInferenceEngine the type inference engine
     * @param methodCallValidator the method call validator
     */
    public TypeChecker(SemanticContext context,
                       ScopeResolver scopeResolver,
                       DeclarationCollector declarationCollector,
                       TypeInferenceEngine typeInferenceEngine,
                       MethodCallValidator methodCallValidator) {
        this.context = context;
        this.scopeResolver = scopeResolver;
        this.declarationCollector = declarationCollector;
        this.typeInferenceEngine = typeInferenceEngine;
        this.methodCallValidator = methodCallValidator;
    }

    /**
     * Check type compatibility and variable usage throughout the AST.
     * Entry point for type checking phase.
     *
     * @param ast the ClasseNode to check
     */
    public void checkTypes(ClasseNode ast) {
        // Check declared methods
        checkMethodsInDeclarations(ast.getDeclarations());

        // Check main method, enter main scope
        context.setCurrentScope(ScopeResolver.MAIN_SCOPE);
        MainNode mainNode = (MainNode) ast.getMethodeMain();
        // Variables already collected in declaration phase, just verify instructions
        checkInstructions(mainNode.getInstrs());
        context.setCurrentScope(ScopeResolver.GLOBAL_SCOPE);  // Return to global scope
    }

    /**
     * Check methods declared in DeclsNode.
     * @param decls the DeclsNode to process
     */
    private void checkMethodsInDeclarations(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof MethodeNode methodeNode) {
            String methodName = methodeNode.getIdent().getNom();
            Type returnType = methodeNode.getTypeMeth();

            // Create method signature (ex: , "f@int" for method f returning int)
            String methodSignature = methodName + "@" + returnType.toString();

            // Save and enter method scope
            String previousScope = context.getCurrentScope();
            context.setCurrentScope(methodSignature);
            context.getCurrentScopeVariables().clear();  // Clear previous method's variables

            // Set the expected return type for this method
            context.setCurrentMethodReturnType(returnType);

            // Collect method's local scope (parameters + local variables)
            declarationCollector.collectMethodParameters(methodeNode.getEntetes());
            declarationCollector.collectVars(methodeNode.getVars());

            // Check instructions
            checkInstructions(methodeNode.getInstrs());

            // Check that non-void methods contain at least one return statement
            if (context.getCurrentMethodReturnType() != Type.VOID) {
                if (!hasReturnStatement(methodeNode.getInstrs())) {
                    context.getCollector().report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        context.createPosition(),
                        String.format("Missing return statement: method '%s' must return a value of type '%s'. " +
                                      "Add a return statement with an expression of the correct type.",
                                      methodName, context.getCurrentMethodReturnType())
                    );
                }
            }

            // Restore previous scope
            context.setCurrentMethodReturnType(null);
            context.setCurrentScope(previousScope);
            context.getCurrentScopeVariables().clear();  // Clean up method scope variables
        }

        checkMethodsInDeclarations(decls.getDecls());
    }

    /**
     * Check if an instruction tree contains at least one return statement.
     * @param node the root node to check
     * @return true if a return statement is found, false otherwise
     */
    private boolean hasReturnStatement(AstNode node) {
        if (node == null) return false;

        // Check if this node is a return statement
        if (node instanceof RetourNode) {
            return true;
        }

        // Recursively check children
        // Here we assume the minijaja codes are too light to create a stack overflow (no deep recursion)
        Iterable<? extends AstNode> children = node.getChildren();
        if (children != null) {
            for (AstNode child : children) {
                if (hasReturnStatement(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check instructions node.
     * @param instrs the InstructionsNode to check
     */
    private void checkInstructions(InstructionsNode instrs) {
        if (instrs == null) return;

        Iterable<AstNode> children = instrs.getChildren();
        if (children != null) {
            for (AstNode child : children) {
                checkNode(child);
            }
        }
    }

    /**
     * Check any AST node recursively.
     * @param node the node to check
     */
    private void checkNode(AstNode node) {
        switch (node) {
            case null -> {return;}

            // Check specific node types
            case AffectationNode affectation -> {
                checkAffectation(affectation);
                return;
            }
            case InstructionsNode instructions -> {
                checkInstructions(instructions);
                return;
            }
            case EcrireNode ecrire -> {
                checkEcrire(ecrire);
                return;
            }
            case AppelINode appelI -> {
                checkAppelI(appelI);
                return;
            }
            case RetourNode retour -> {
                checkRetour(retour);
                return;
            }
            default -> {
            }
        }

        // Recursively check children
        Iterable<AstNode> children = node.getChildren();
        if (children != null) {
            for (AstNode child : children) {
                checkNode(child);
            }
        }
    }

    /**
     * Check return statement.
     * @param retour the RetourNode to check
     */
    private void checkRetour(RetourNode retour) {
        AstNode exp = retour.getExp();

        // If we're not in a method, return shouldn't be used
        if (context.getCurrentMethodReturnType() == null) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(),
                "Return statement error: 'return' can only be used inside a method, not in 'main'. " +
                "Remove the return statement or move this code to a method."
            );
            return;
        }

        // Check the type of the returned expression
        if (exp instanceof Expression expression) {
            Type returnedType = typeInferenceEngine.inferType(expression);
            if (returnedType != null && returnedType != context.getCurrentMethodReturnType()) {
                context.getCollector().report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        context.createPosition(),
                        String.format("Return type mismatch: method expects return type '%s', but got '%s'. " +
                                        "The returned expression must have the same type as the method's declared return type.",
                                context.getCurrentMethodReturnType(), returnedType)
                );
            }
        }
    }

    /**
     * Check ecrire/ecrireln statement.
     * @param ecrire the EcrireNode to check
     */
    private void checkEcrire(EcrireNode ecrire) {
        Object ident1Node = ecrire.getIdent1Node();

        // If it's an expression, check its type
        if (ident1Node instanceof Expression expression) {
            typeInferenceEngine.inferType(expression);
        }
        // If it's a string literal, nothing to check
    }

    /**
     * Check method call instruction.
     * @param appelI the AppelINode to check
     */
    private void checkAppelI(AppelINode appelI) {
        String methodName = appelI.getIdent().getNom();
        // Validate the method call (checks declaration, parameter count, and types)
        methodCallValidator.validateMethodCall(methodName, appelI.getListExp());
    }

    /**
     * Check assignment statement.
     * @param affectation the AffectationNode to check
     */
    private void checkAffectation(AffectationNode affectation) {
        AstNode identNode = affectation.getIdent1Node();
        Expression expression = affectation.getExpression();

        if (identNode instanceof IdentNode ident) {
            String varName = ident.getNom();
            String qualifiedName = scopeResolver.resolveAndQualifyName(varName);

            // Check if variable is declared
            if (!context.getSymbolTable().contains(qualifiedName)) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Undeclared variable: '%s' has not been declared. " +
                                  "Make sure to declare the variable before using it (ex: , 'int %s;').",
                                  varName, varName)
                );
                return;
            }

            // Check type compatibility
            Type varType = context.getSymbolTable().type(qualifiedName);
            Type exprType = typeInferenceEngine.inferType(expression);

            if (varType != null && exprType != null && varType != exprType) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Type mismatch in assignment: cannot assign value of type '%s' to variable '%s' of type '%s'. " +
                                  "The assigned expression must have the same type as the variable.",
                                  exprType, varName, varType)
                );
            }
        }
    }
}
