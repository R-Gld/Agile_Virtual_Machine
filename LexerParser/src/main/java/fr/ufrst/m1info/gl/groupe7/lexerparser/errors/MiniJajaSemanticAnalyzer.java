package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic.*;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;

/**
 * Semantic analyzer for MiniJaja programs.
 *
 * This class serves as a facade that orchestrates semantic analysis through
 * specialized components: declaration collection, type inference, method call
 * validation, and type checking.
 *
 * The semantic analysis is performed in two phases:
 * 1. Declaration collection: Collects all declarations and checks for duplicates
 * 2. Type checking: Validates type compatibility and usage throughout the AST
 */
public class MiniJajaSemanticAnalyzer {

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;
    private final DeclarationCollector declarationCollector;
    private final TypeInferenceEngine typeInferenceEngine;
    private final MethodCallValidator methodCallValidator;
    private final TypeChecker typeChecker;

    /**
     * Constructor.
     * Creates its own internal Stacks instance for semantic analysis.
     * This ensures the analysis is independent and doesn't pollute the execution memory.
     *
     * @param collector the DiagnosticCollector to report errors to
     */
    public MiniJajaSemanticAnalyzer(DiagnosticCollector collector) {
        // Initialize shared context
        this.context = new SemanticContext(collector);

        // Initialize components in dependency order
        this.scopeResolver = new ScopeResolver(context);
        this.typeInferenceEngine = new TypeInferenceEngine(context, scopeResolver);
        this.declarationCollector = new DeclarationCollector(context, scopeResolver, typeInferenceEngine);
        this.methodCallValidator = new MethodCallValidator(context, scopeResolver, declarationCollector);
        this.typeChecker = new TypeChecker(context, scopeResolver, declarationCollector,
                                           typeInferenceEngine, methodCallValidator);

        // Break circular dependency
        this.typeInferenceEngine.setMethodCallValidator(methodCallValidator);
    }

    /**
     * Performs semantic analysis on the provided abstract syntax tree (AST) by
     * checking variable declarations and validating type compatibility.
     *
     * @param ast the root node of the abstract syntax tree (ClasseNode) to be analyzed
     */
    public void analyse(ClasseNode ast) {
        declarationCollector.collectDeclarations(ast);
        typeChecker.checkTypes(ast);
    }

    /**
     * Set the current file name for error reporting.
     *
     * @param fileName the file name to use
     */
    public void setFileName(String fileName) {
        context.setFileName(fileName);
    }

    // ============================================================
    // PUBLIC ACCESSORS
    // ============================================================

    /**
     * @return the symbol table containing all declared symbols
     */
    public SymbolTable symbolTable() {
        return context.getSymbolTable();
    }

    /**
     * @return the diagnostic collector for error reporting
     */
    public DiagnosticCollector collector() {
        return context.getCollector();
    }

    /**
     * @return the stacks (contains symbol table and runtime memory)
     */
    public Stacks stacks() {
        return context.getStacks();
    }
}
