package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Collector for declarations (variables, methods) in the MiniJaja AST.
 * First-pass analysis: collects all declarations and checks for duplicates.
 */
public class DeclarationCollector {

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;
    private final TypeInferenceEngine typeInferenceEngine;

    /**
     * Constructor.
     *
     * @param context the semantic context
     * @param scopeResolver the scope resolver
     * @param typeInferenceEngine the type inference engine for validating initialization expressions
     */
    public DeclarationCollector(SemanticContext context,
                                ScopeResolver scopeResolver,
                                TypeInferenceEngine typeInferenceEngine) {
        this.context = context;
        this.scopeResolver = scopeResolver;
        this.typeInferenceEngine = typeInferenceEngine;
    }

    /**
     * Collect all declarations from the AST and check for duplicates.
     * Entry point for declaration collection phase.
     *
     * @param ast the root node of the abstract syntax tree (ClasseNode)
     */
    public void collectDeclarations(ClasseNode ast) {
        // Collect global variable declarations
        collectDeclarationsRecursive(ast.getDeclarations());

        // Process main method, enter main scope
        context.setCurrentScope(ScopeResolver.MAIN_SCOPE);
        MainNode mainNode = (MainNode) ast.getMethodeMain();
        collectVars(mainNode.getVars());
        context.setCurrentScope(ScopeResolver.GLOBAL_SCOPE);  // Return to global scope
    }

    /**
     * Collect declarations from DeclsNode (recursive).
     * @param decls the DeclsNode to process
     */
    private void collectDeclarationsRecursive(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof VarNode varNode) {
            String varName = varNode.getIdent().getNom();
            String qualifiedName = scopeResolver.qualifyName(varName);  // ex: , "x@global"
            Type varType = varNode.getType();

            // Check for duplicate declarations in current scope
            if (context.getSymbolTable().contains(qualifiedName)) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Duplicate variable declaration: '%s' has already been declared. " +
                                  "Each variable can only be declared once in the same scope.", varName)
                );
            } else {
                context.getSymbolTable().creationSymbol(qualifiedName, 0, varType);
            }

            // Check initialization expression if present
            Expression initExpr = varNode.getExp().getVexp();
            if (initExpr != null) {
                Type initType = typeInferenceEngine.inferType(initExpr);
                if (initType != null && varType != initType) {
                    context.getCollector().report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        context.createPosition(),
                        String.format("Type mismatch in variable initialization: variable '%s' declared as '%s' but initialized with '%s'. " +
                                      "The initialization expression must match the declared type.",
                                      varName, varType, initType)
                    );
                }
            }
        } else if (decl instanceof MethodeNode methodeNode) {
            // Declare method using signature format: methodName@returnType (ex: , "f@integer")
            String methodName = methodeNode.getIdent().getNom();
            Type returnType = methodeNode.getTypeMeth();
            String methodSignature = methodName + "@" + returnType.toString();

            // Check for duplicate method declarations
            if (context.getSymbolTable().contains(methodSignature)) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Duplicate method declaration: method '%s' has already been declared. " +
                                  "Each method can only be declared once.", methodName)
                );
            } else {
                context.getStacks().declareMeth(methodSignature, methodeNode, returnType);
            }

            // Note: Method parameters and local variables are collected during type checking
            // in TypeChecker to properly manage scoping
        }

        // Process next declaration
        collectDeclarationsRecursive(decls.getDecls());
    }

    /**
     * Collect parameter types from EntetesNode.
     * Public helper method used by MethodCallValidator.
     *
     * @param entetes the EntetesNode to process
     * @return list of parameter types
     */
    public List<Type> collectParameterTypes(EntetesNode entetes) {
        List<Type> paramTypes = new ArrayList<>();
        if (entetes == null) return paramTypes;

        EntetesNode current = entetes;
        while (current != null && current.getEntete() != null) {
            paramTypes.add(current.getEntete().getType());
            current = current.getEntetes();
        }

        return paramTypes;
    }

    /**
     * Collect method parameters and add them to symbol table.
     * Called by TypeChecker when processing method declarations.
     *
     * @param entetes the EntetesNode to process
     */
    public void collectMethodParameters(EntetesNode entetes) {
        if (entetes == null) return;

        EntetesNode current = entetes;
        while (current != null && current.getEntete() != null) {
            EnteteNode entete = current.getEntete();
            String paramName = entete.getIdent().getNom();
            String qualifiedName = scopeResolver.qualifyName(paramName);  // ex: , "x@f@int"
            Type paramType = entete.getType();

            // Check for duplicate parameter names in current scope
            if (context.getSymbolTable().contains(qualifiedName)) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Duplicate parameter name: '%s' has already been declared. " +
                                  "Parameter names must be unique.", paramName)
                );
            } else {
                context.getSymbolTable().creationSymbol(qualifiedName, 0, paramType);
                // Track this parameter in current scope variables
                context.getCurrentScopeVariables().add(paramName);
            }

            current = current.getEntetes();
        }
    }

    /**
     * Collect local variable declarations from VarsNode.
     * Called by both collectDeclarations (for main) and TypeChecker (for methods).
     *
     * @param vars the VarsNode to process
     */
    public void collectVars(VarsNode vars) {
        if (vars == null) return;

        // VarsNode has a linked-list structure: var + vars
        AstNode var = vars.getVar();
        if (var instanceof VarNode varNode) {
            String varName = varNode.getIdent().getNom();
            String qualifiedName = scopeResolver.qualifyName(varName);  // ex: , "local@main" or "local@f@int"
            Type varType = varNode.getType();

            // Check for duplicate declarations in current scope
            if (context.getSymbolTable().contains(qualifiedName)) {
                context.getCollector().report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    context.createPosition(),
                    String.format("Duplicate local variable: '%s' has already been declared. " +
                                  "Please use a different variable name.", varName)
                );
            } else {
                context.getSymbolTable().creationSymbol(qualifiedName, 0, varType);

                // Track this variable in the appropriate set
                if (ScopeResolver.MAIN_SCOPE.equals(context.getCurrentScope())) {
                    context.getMainLocalVariables().add(varName);
                } else if (!ScopeResolver.GLOBAL_SCOPE.equals(context.getCurrentScope())) {
                    context.getCurrentScopeVariables().add(varName);
                }
            }

            // Check initialization expression if present
            Expression initExpr = varNode.getExp().getVexp();
            if (initExpr != null) {
                Type initType = typeInferenceEngine.inferType(initExpr);
                if (initType != null && varType != initType) {
                    context.getCollector().report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        context.createPosition(),
                        String.format("Type mismatch in variable initialization: variable '%s' declared as '%s' but initialized with '%s'. " +
                                      "The initialization expression must match the declared type.",
                                      varName, varType, initType)
                    );
                }
            }
        }

        // Process the rest of the variables recursively
        collectVars(vars.getVars());
    }

    /**
     * Collect argument types from ListExpNode.
     * Public helper method used by MethodCallValidator.
     *
     * @param listExp the ListExpNode to process
     * @return list of argument types
     */
    public List<Type> collectArgumentTypes(ListExpNode listExp) {
        List<Type> argTypes = new ArrayList<>();
        if (listExp == null) return argTypes;

        // ListExpNode has a linked-list structure
        ListExpNode current = listExp;
        while (current != null && current.getExp() != null) {
            AstNode exp = current.getExp();
            if (exp instanceof Expression expression) {
                Type argType = typeInferenceEngine.inferType(expression);
                argTypes.add(argType);
            }
            current = current.getListExp();
        }

        return argTypes;
    }
}
