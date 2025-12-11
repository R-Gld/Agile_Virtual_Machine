package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared context for semantic analysis components.
 * Centralizes all mutable state that needs to be coordinated across
 * declaration collection, type checking, and validation phases.
 */
public class SemanticContext {

    // Immutable references
    private final DiagnosticCollector collector;
    private final Stacks stacks;

    // Mutable state for error reporting
    private String currentFileName;

    // Mutable state for scope tracking
    private String currentScope = ScopeResolver.GLOBAL_SCOPE;
    private Type currentMethodReturnType;

    // Variable tracking for scope resolution
    private final Set<String> mainLocalVariables = new HashSet<>();
    private final Set<String> currentScopeVariables = new HashSet<>();
    private final Set<String> globalConstants = new HashSet<>(); // Pour détecter les réassignations de constantes
    private final Set<String> initializedConstants = new HashSet<>(); // Pour tracker les constantes déjà initialisées

    /**
     * Constructor.
     * Creates its own internal Stacks instance for semantic analysis.
     * This ensures the analysis is independent and doesn't pollute the execution memory.
     *
     * @param collector the DiagnosticCollector to report errors to
     */
    public SemanticContext(DiagnosticCollector collector) {
        this.collector = collector;
        this.stacks = new Stacks();
        this.currentFileName = null;
    }

    // ============================================================
    // IMMUTABLE REFERENCE ACCESSORS
    // ============================================================

    /**
     * @return the diagnostic collector for error reporting
     */
    public DiagnosticCollector getCollector() {
        return collector;
    }

    /**
     * @return the stacks (contains symbol table and runtime memory)
     */
    public Stacks getStacks() {
        return stacks;
    }

    /**
     * @return the symbol table (shortcut to stacks.getSymbolTable())
     */
    public SymbolTable getSymbolTable() {
        return stacks.getSymbolTable();
    }

    // ============================================================
    // FILE NAME MANAGEMENT
    // ============================================================

    /**
     * Set the current file name for error reporting
     * @param fileName the file name to use
     */
    public void setFileName(String fileName) {
        this.currentFileName = fileName;
    }

    /**
     * Create a source position for error reporting
     * @return a SourcePosition with current file name
     */
    public SourcePosition createPosition() {
        return new SourcePosition(currentFileName, 0, 0);
    }

    // ============================================================
    // SCOPE MANAGEMENT
    // ============================================================

    /**
     * @return the current scope ("global", "main", or "methodName@returnType")
     */
    public String getCurrentScope() {
        return currentScope;
    }

    /**
     * Set the current scope
     * @param scope the scope name
     */
    public void setCurrentScope(String scope) {
        this.currentScope = scope;
    }

    /**
     * @return the expected return type for the current method (null if in main or global)
     */
    public Type getCurrentMethodReturnType() {
        return currentMethodReturnType;
    }

    /**
     * Set the expected return type for the current method
     * @param type the return type (null for main)
     */
    public void setCurrentMethodReturnType(Type type) {
        this.currentMethodReturnType = type;
    }

    /**
     * @return the set of variables declared in main
     */
    public Set<String> getMainLocalVariables() {
        return mainLocalVariables;
    }

    /**
     * @return the set of variables in the current method scope
     */
    public Set<String> getCurrentScopeVariables() {
        return currentScopeVariables;
    }

    /**
     * Enter a new scope (method or main).
     * For methods, clears the current scope variables set.
     *
     * @param scopeName the scope to enter ("main" or "methodName@returnType")
     */
    public void enterScope(String scopeName) {
        this.currentScope = scopeName;
        if (!ScopeResolver.MAIN_SCOPE.equals(scopeName) && !ScopeResolver.GLOBAL_SCOPE.equals(scopeName)) {
            // Entering a method scope, clear previous method's variables
            currentScopeVariables.clear();
        }
    }

    /**
     * Exit the current scope and return to global.
     * Clears method-specific state.
     */
    public void exitScope() {
        this.currentScope = ScopeResolver.GLOBAL_SCOPE;
        this.currentMethodReturnType = null;
        this.currentScopeVariables.clear();
    }

    // ============================================================
    // CONSTANT MANAGEMENT
    // ============================================================

    /**
     * @return the set of global constants (final variables)
     */
    public Set<String> getGlobalConstants() {
        return globalConstants;
    }

    /**
     * Check if a variable is a constant.
     * @param varName the variable name (without scope qualifier)
     * @return true if the variable is declared as final
     */
    public boolean isConstant(String varName) {
        return globalConstants.contains(varName);
    }

    /**
     * Register a variable as a constant.
     * @param varName the variable name (without scope qualifier)
     */
    public void addConstant(String varName) {
        globalConstants.add(varName);
    }

    /**
     * Check if a constant has been initialized.
     * @param varName the variable name (without scope qualifier)
     * @return true if the constant has already been initialized
     */
    public boolean isConstantInitialized(String varName) {
        return initializedConstants.contains(varName);
    }

    /**
     * Mark a constant as initialized.
     * @param varName the variable name (without scope qualifier)
     */
    public void markConstantAsInitialized(String varName) {
        initializedConstants.add(varName);
    }
}
