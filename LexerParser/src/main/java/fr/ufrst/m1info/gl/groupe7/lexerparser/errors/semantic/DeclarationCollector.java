package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
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

    private static final String TYPE_MISMATCH_MSG = "The initialization expression must match the declared type.";
    private static final String KIND_VARIABLE = "variable";
    private static final String KIND_CONSTANT = "constant";

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;
    private final TypeInferenceEngine typeInferenceEngine;

    /**
     * Constructor.
     *
     * @param context             the semantic context
     * @param scopeResolver       the scope resolver
     * @param typeInferenceEngine the type inference engine for validating initialization expressions
     */
    public DeclarationCollector(SemanticContext context, ScopeResolver scopeResolver, TypeInferenceEngine typeInferenceEngine) {
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
        collectDeclarationsRecursive(ast.getDeclarations());

        context.setCurrentScope(ScopeResolver.MAIN_SCOPE);
        MainNode mainNode = (MainNode) ast.getMethodeMain();
        collectVars(mainNode.getVars());
        context.setCurrentScope(ScopeResolver.GLOBAL_SCOPE);
    }

    /**
     * Collect declarations from DeclsNode (recursive).
     */
    private void collectDeclarationsRecursive(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof VarNode varNode) {
            collectVariable(varNode);
        } else if (decl instanceof MethodeNode methodeNode) {
            collectMethod(methodeNode);
        } else if (decl instanceof CstNode cstNode) {
            collectConstant(cstNode);
        }

        collectDeclarationsRecursive(decls.getDecls());
    }

    private void collectVariable(VarNode varNode) {
        String varName = varNode.getIdent().getNom();
        String qualifiedName = scopeResolver.qualifyName(varName);
        Type varType = varNode.getType();

        if (checkDuplicateAndRegister(qualifiedName, varType, KIND_VARIABLE, varName)) {
            trackVariableInScope(varName);
        }

        checkInitializationType(varNode.getExp().getVexp(), varType, varName, KIND_VARIABLE);
    }

    private void collectConstant(CstNode cstNode) {
        String cstName = cstNode.getIdent().getNom();
        String qualifiedName = scopeResolver.qualifyName(cstName);
        Type cstType = cstNode.getType();

        if (checkDuplicateAndRegister(qualifiedName, cstType, KIND_CONSTANT, cstName)) {
            context.addConstant(cstName);
            trackVariableInScope(cstName);
        }

        Expression initExpr = cstNode.getExp() != null ? cstNode.getExp().getVexp() : null;
        checkInitializationType(initExpr, cstType, cstName, KIND_CONSTANT);
    }

    private void collectMethod(MethodeNode methodeNode) {
        String methodName = methodeNode.getIdent().getNom();
        Type returnType = methodeNode.getTypeMeth();
        String methodSignature = methodName + "@" + returnType.toString();

        if (context.getSymbolTable().contains(methodSignature)) {
            reportError("Duplicate method declaration: method '%s' has already been declared. " + "Each method can only be declared once.", methodName);
        } else {
            context.getStacks().declareMeth(methodSignature, methodeNode, returnType);
        }
    }

    /**
     * Check for duplicate and register symbol if not duplicate.
     *
     * @return true if registered successfully, false if duplicate
     */
    private boolean checkDuplicateAndRegister(String qualifiedName, Type type, String kind, String name) {
        if (context.getSymbolTable().contains(qualifiedName)) {
            reportError("Duplicate %s declaration: '%s' has already been declared. " + "Each %s can only be declared once in the same scope.", kind, name, kind);
            return false;
        }
        context.getSymbolTable().creationSymbol(qualifiedName, 0, type);
        return true;
    }

    private void checkInitializationType(Expression initExpr, Type declaredType, String name, String kind) {
        if (initExpr == null) return;

        Type initType = typeInferenceEngine.inferType(initExpr);
        if (initType != null && declaredType != initType) {
            reportError("Type mismatch in %s initialization: %s '%s' declared as '%s' but initialized with '%s'. %s", kind, kind, name, declaredType, initType, TYPE_MISMATCH_MSG);
        }
    }

    private void trackVariableInScope(String name) {
        String currentScope = context.getCurrentScope();
        if (ScopeResolver.MAIN_SCOPE.equals(currentScope)) {
            context.getMainLocalVariables().add(name);
        } else if (!ScopeResolver.GLOBAL_SCOPE.equals(currentScope)) {
            context.getCurrentScopeVariables().add(name);
        }
    }

    private void reportError(String format, Object... args) {
        context.getCollector().report(Severity.ERROR, Phase.SEMANTIC, context.createPosition(), String.format(format, args));
    }

    /**
     * Collect parameter types from EntetesNode.
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
     */
    public void collectMethodParameters(EntetesNode entetes) {
        if (entetes == null) return;

        EntetesNode current = entetes;
        while (current != null && current.getEntete() != null) {
            EnteteNode entete = current.getEntete();
            String paramName = entete.getIdent().getNom();
            String qualifiedName = scopeResolver.qualifyName(paramName);
            Type paramType = entete.getType();

            if (context.getSymbolTable().contains(qualifiedName)) {
                reportError("Duplicate parameter name: '%s' has already been declared. " + "Parameter names must be unique.", paramName);
            } else {
                context.getSymbolTable().creationSymbol(qualifiedName, 0, paramType);
                context.getCurrentScopeVariables().add(paramName);
            }
            current = current.getEntetes();
        }
    }

    /**
     * Collect local variable declarations from VarsNode.
     */
    public void collectVars(VarsNode vars) {
        if (vars == null) return;

        AstNode varNode = vars.getVar();
        if (varNode instanceof VarNode vn) {
            collectLocalVariable(vn);
        } else if (varNode instanceof CstNode cn) {
            collectLocalConstant(cn);
        }

        collectVars(vars.getVars());
    }

    private void collectLocalVariable(VarNode varNode) {
        String varName = varNode.getIdent().getNom();
        String qualifiedName = scopeResolver.qualifyName(varName);
        Type varType = varNode.getType();

        if (checkDuplicateLocal(qualifiedName, varName, KIND_VARIABLE)) {
            context.getSymbolTable().creationSymbol(qualifiedName, 0, varType);
            trackVariableInScope(varName);
        }

        checkInitializationType(varNode.getExp().getVexp(), varType, varName, KIND_VARIABLE);
    }

    private void collectLocalConstant(CstNode cstNode) {
        String cstName = cstNode.getIdent().getNom();
        String qualifiedName = scopeResolver.qualifyName(cstName);
        Type cstType = cstNode.getType();

        if (checkDuplicateLocal(qualifiedName, cstName, KIND_CONSTANT)) {
            context.getSymbolTable().creationSymbol(qualifiedName, 0, cstType);
            context.addConstant(cstName);
            trackVariableInScope(cstName);
        }

        Expression initExpr = cstNode.getExp() != null ? cstNode.getExp().getVexp() : null;
        checkInitializationType(initExpr, cstType, cstName, KIND_CONSTANT);
    }

    private boolean checkDuplicateLocal(String qualifiedName, String name, String kind) {
        if (context.getSymbolTable().contains(qualifiedName)) {
            reportError("Duplicate local %s: '%s' has already been declared. " + "Please use a different %s name.", kind, name, kind);
            return false;
        }
        return true;
    }

    /**
     * Collect argument types from ListExpNode.
     */
    public List<Type> collectArgumentTypes(ListExpNode listExp) {
        List<Type> argTypes = new ArrayList<>();
        if (listExp == null) return argTypes;

        ListExpNode current = listExp;
        while (current != null && current.getExp() != null) {
            AstNode exp = current.getExp();
            if (exp instanceof Expression expression) {
                argTypes.add(typeInferenceEngine.inferType(expression));
            }
            current = current.getListExp();
        }

        return argTypes;
    }
}
