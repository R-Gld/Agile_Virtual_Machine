package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.Symbol;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MiniJajaSemanticAnalyzer {

    private final DiagnosticCollector collector;
    private final Stacks stacks;
    private String currentFileName;

    // Track the expected return type of the current method being checked
    private Type currentMethodReturnType;

    // Scope management using name mangling (same approach as compiler)
    private String currentScope = "global";
    private final Set<String> mainLocalVariables = new HashSet<>();
    private final Set<String> currentScopeVariables = new HashSet<>();

    /**
     * Constructor.
     * Creates its own internal Stacks instance for semantic analysis.
     * This ensures the analysis is independent and doesn't pollute the execution memory.
     *
     * @param collector the DiagnosticCollector to report errors to
     */
    public MiniJajaSemanticAnalyzer(DiagnosticCollector collector) {
        this.collector = collector;
        this.stacks = new Stacks();
        this.currentFileName = null;
    }

    /**
     * Set the current file name for error reporting
     * @param fileName the file name to use
     */
    public void setFileName(String fileName) {
        this.currentFileName = fileName;
    }

    /**
     * Create a source position for error reporting
     * @return a SourcePosition
     */
    private SourcePosition createPosition() {
        return new SourcePosition(currentFileName, 0, 0);
    }

    /**
     * Resolve the scope for a given variable name using the same logic as the compiler.
     * Searches in order: current scope (method) -> main -> global
     *
     * @param variableName the variable name (without scope suffix)
     * @return the scope string ("global", "main", or method signature)
     */
    private String resolveVariableScope(String variableName) {
        // First search in current scope (method parameters/locals)
        if (currentScopeVariables.contains(variableName)) {
            return currentScope;
        }

        // Then search in main
        if (mainLocalVariables.contains(variableName)) {
            return "main";
        }

        // Finally, assume it's global (will be validated later)
        return "global";
    }

    /**
     * Qualify a variable name with its scope suffix (name mangling)
     * @param varName the variable name
     * @return the qualified name (e.g., "x@global", "local@main", "param@f@int")
     */
    private String qualifyName(String varName) {
        return varName + "@" + currentScope;
    }

    /**
     * Qualify a variable name with resolved scope
     * @param varName the variable name
     * @return the qualified name with resolved scope
     */
    private String resolveAndQualifyName(String varName) {
        String scope = resolveVariableScope(varName);
        return varName + "@" + scope;
    }

    /**
     * Find a method's full signature by searching for methodName@*
     * Methods are stored as "methodName@returnType" (e.g., "f@integer")
     *
     * @param methodName the unqualified method name
     * @return the full method signature (e.g., "f@integer"), or null if not found
     */
    private String findMethodSignature(String methodName) {
        String prefix = methodName + "@";

        // Get all symbols and search for matching method
        List<Symbol> allSymbols = stacks.getSymbolTable().getAllSymbols();
        for (Symbol symbol : allSymbols) {
            String symbolName = symbol.getName();
            if (symbolName.startsWith(prefix)) {
                // Verify it's actually a method by checking the value in stack
                Object value = stacks.getValue(symbolName);
                if (value instanceof MethodeNode) {
                    return symbolName;
                }
            }
        }

        return null;
    }

    /**
     * Performs semantic analysis on the provided abstract syntax tree (AST) by
     * checking variable declarations and validating type compatibility.
     *
     * @param ast the root node of the abstract syntax tree (ClasseNode) to be analyzed
     */
    public void analyse(ClasseNode ast) {
        checkDeclarations(ast);
        checkType(ast);
    }

    /**
     * Collect all variable declarations and check for duplicates
     */
    private void checkDeclarations(ClasseNode ast) {
        // Collect global variable declarations
        collectDeclarations(ast.getDeclarations());

        // Process main method - enter main scope
        currentScope = "main";
        MainNode mainNode = (MainNode) ast.getMethodeMain();
        collectVars(mainNode.getVars());
        currentScope = "global";  // Return to global scope
    }

    /**
     * Collect declarations from DeclsNode
     * @param decls the DeclsNode to process
     */
    private void collectDeclarations(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof VarNode varNode) {
            String varName = varNode.getIdent().getNom();
            String qualifiedName = qualifyName(varName);  // e.g., "x@global"
            Type varType = varNode.getType();

            // Check for duplicate declarations in current scope
            if (stacks.getSymbolTable().contains(qualifiedName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate variable declaration: '%s' has already been declared. " +
                                  "Each variable can only be declared once in the same scope.", varName)
                );
            } else {
                stacks.getSymbolTable().creationSymbol(qualifiedName, 0, varType);
            }

            // Check initialization expression if present
            Expression initExpr = varNode.getExp().getVexp();
            if (initExpr != null) {
                Type initType = inferType(initExpr);
                if (initType != null && varType != initType) {
                    collector.report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        createPosition(),
                        String.format("Type mismatch in variable initialization: variable '%s' declared as '%s' but initialized with '%s'. " +
                                      "The initialization expression must match the declared type.",
                                      varName, varType, initType)
                    );
                }
            }
        } else if (decl instanceof MethodeNode methodeNode) {
            // Declare method using signature format: methodName@returnType (e.g., "f@integer")
            String methodName = methodeNode.getIdent().getNom();
            Type returnType = methodeNode.getTypeMeth();
            String methodSignature = methodName + "@" + returnType.toString();

            // Check for duplicate method declarations
            if (stacks.getSymbolTable().contains(methodSignature)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate method declaration: method '%s' has already been declared. " +
                                  "Each method can only be declared once.", methodName)
                );
            } else {
                stacks.declareMeth(methodSignature, methodeNode, returnType);
            }

            // Note: Method parameters and local variables are collected during type checking
            // in checkMethodsInDeclarations() to properly manage scoping
        }

        // Process next declaration
        collectDeclarations(decls.getDecls());
    }

    /**
     * Collect parameter types from EntetesNode
     * @param entetes the EntetesNode to process
     * @return list of parameter types
     */
    private List<Type> collectParameterTypes(EntetesNode entetes) {
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
     * Collect method parameters and add them to symbol table
     * @param entetes the EntetesNode to process
     */
    private void collectMethodParameters(EntetesNode entetes) {
        if (entetes == null) return;

        EntetesNode current = entetes;
        while (current != null && current.getEntete() != null) {
            EnteteNode entete = current.getEntete();
            String paramName = entete.getIdent().getNom();
            String qualifiedName = qualifyName(paramName);  // e.g., "x@f@int"
            Type paramType = entete.getType();

            // Check for duplicate parameter names in current scope
            if (stacks.getSymbolTable().contains(qualifiedName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate parameter name: '%s' has already been declared. " +
                                  "Parameter names must be unique.", paramName)
                );
            } else {
                stacks.getSymbolTable().creationSymbol(qualifiedName, 0, paramType);
                // Track this parameter in current scope variables
                currentScopeVariables.add(paramName);
            }

            current = current.getEntetes();
        }
    }

    /**
     * Collect local variable declarations from VarsNode
     * @param vars the VarsNode to process
     */
    private void collectVars(VarsNode vars) {
        if (vars == null) return;

        // VarsNode has a linked-list structure: var + vars
        AstNode var = vars.getVar();
        if (var instanceof VarNode varNode) {
            String varName = varNode.getIdent().getNom();
            String qualifiedName = qualifyName(varName);  // e.g., "local@main" or "local@f@int"
            Type varType = varNode.getType();

            // Check for duplicate declarations in current scope
            if (stacks.getSymbolTable().contains(qualifiedName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate local variable: '%s' has already been declared. " +
                                  "Please use a different variable name.", varName)
                );
            } else {
                stacks.getSymbolTable().creationSymbol(qualifiedName, 0, varType);

                // Track this variable in the appropriate set
                if ("main".equals(currentScope)) {
                    mainLocalVariables.add(varName);
                } else if (!"global".equals(currentScope)) {
                    currentScopeVariables.add(varName);
                }
            }

            // Check initialization expression if present
            Expression initExpr = varNode.getExp().getVexp();
            if (initExpr != null) {
                Type initType = inferType(initExpr);
                if (initType != null && varType != initType) {
                    collector.report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        createPosition(),
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
     * Check type compatibility and variable usage
     * @param ast the ClasseNode to check
     */
    private void checkType(ClasseNode ast) {
        // Check declared methods
        checkMethodsInDeclarations(ast.getDeclarations());

        // Check main method - enter main scope
        currentScope = "main";
        MainNode mainNode = (MainNode) ast.getMethodeMain();
        // Variables already collected in checkDeclarations, just verify instructions
        checkInstructions(mainNode.getInstrs());
        currentScope = "global";  // Return to global scope
    }

    /**
     * Check methods declared in DeclsNode
     * @param decls the DeclsNode to process
     */
    private void checkMethodsInDeclarations(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof MethodeNode methodeNode) {
            String methodName = methodeNode.getIdent().getNom();
            Type returnType = methodeNode.getTypeMeth();

            // Create method signature (e.g., "f@int" for method f returning int)
            String methodSignature = methodName + "@" + returnType.toString();

            // Save and enter method scope
            String previousScope = currentScope;
            currentScope = methodSignature;
            currentScopeVariables.clear();  // Clear previous method's variables

            // Set the expected return type for this method
            currentMethodReturnType = returnType;

            // Collect method's local scope (parameters + local variables)
            collectMethodParameters(methodeNode.getEntetes());
            collectVars(methodeNode.getVars());

            // Check instructions
            checkInstructions(methodeNode.getInstrs());

            // Check that non-void methods contain at least one return statement
            if (currentMethodReturnType != Type.VOID) {
                if (!hasReturnStatement(methodeNode.getInstrs())) {
                    collector.report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        createPosition(),
                        String.format("Missing return statement: method '%s' must return a value of type '%s'. " +
                                      "Add a return statement with an expression of the correct type.",
                                      methodName, currentMethodReturnType)
                    );
                }
            }

            // Restore previous scope
            currentMethodReturnType = null;
            currentScope = previousScope;
            currentScopeVariables.clear();  // Clean up method scope variables
        }

        checkMethodsInDeclarations(decls.getDecls());
    }

    /**
     * Check if an instruction tree contains at least one return statement
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
     * Check instructions node
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
     * Check any AST node recursively
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
     * Check return statement
     * @param retour the RetourNode to check
     */
    private void checkRetour(RetourNode retour) {
        AstNode exp = retour.getExp();

        // If we're not in a method, return shouldn't be used
        if (currentMethodReturnType == null) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
                "Return statement error: 'return' can only be used inside a method, not in 'main'. " +
                "Remove the return statement or move this code to a method."
            );
            return;
        }

        // Check the type of the returned expression
        if (exp instanceof Expression expression) {
            Type returnedType = inferType(expression);
            if (returnedType != null && returnedType != currentMethodReturnType) {
                collector.report(
                        Severity.ERROR,
                        Phase.SEMANTIC,
                        createPosition(),
                        String.format("Return type mismatch: method expects return type '%s', but got '%s'. " +
                                        "The returned expression must have the same type as the method's declared return type.",
                                currentMethodReturnType, returnedType)
                );
            }
        }
    }

    /**
     * Check ecrire/ecrireln statement
     * @param ecrire the EcrireNode to check
     */
    private void checkEcrire(EcrireNode ecrire) {
        Object ident1Node = ecrire.getIdent1Node();

        // If it's an expression, check its type
        if (ident1Node instanceof Expression expression) {
            inferType(expression);
        }
        // If it's a string literal, nothing to check
    }

    /**
     * Check method call instruction
     * @param appelI the AppelINode to check
     */
    private void checkAppelI(AppelINode appelI) {
        String methodName = appelI.getIdent().getNom();
        // Validate the method call (checks declaration, parameter count, and types)
        validateMethodCall(methodName, appelI.getListExp());
    }

    /**
     * Collect argument types from ListExpNode
     * @param listExp the ListExpNode to process
     * @return list of argument types
     */
    private List<Type> collectArgumentTypes(ListExpNode listExp) {
        List<Type> argTypes = new ArrayList<>();
        if (listExp == null) return argTypes;

        // ListExpNode has a linked-list structure
        ListExpNode current = listExp;
        while (current != null && current.getExp() != null) {
            AstNode exp = current.getExp();
            if (exp instanceof Expression expression) {
                Type argType = inferType(expression);
                argTypes.add(argType);
            }
            current = current.getListExp();
        }

        return argTypes;
    }

    /**
     * Validates a method call with the given method name and arguments.
     * This method performs all necessary checks: method declaration, parameter count, and parameter types.
     *
     * @param methodName the name of the method being called (unqualified)
     * @param args the arguments passed to the method (can be null for methods with no parameters)
     * @return the return type of the method, or null if validation failed
     */
    private Type validateMethodCall(String methodName, ListExpNode args) {
        // Search for method declaration - stored as "methodName@returnType" (e.g., "f@integer")
        String methodSignature = findMethodSignature(methodName);

        if (methodSignature == null) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
                String.format("Undeclared method: method '%s' has not been declared. " +
                              "Make sure to declare the method before calling it.", methodName)
            );
            return null;
        }

        // Get the MethodeNode from stacks
        Object methodValue = stacks.getValue(methodSignature);
        if (!(methodValue instanceof MethodeNode methodeNode)) {
            // This shouldn't happen if symbol table is correct
            return null;
        }

        // Extract expected parameter types from the MethodeNode
        List<Type> expectedParamTypes = collectParameterTypes(methodeNode.getEntetes());

        // Get actual parameter types
        List<Type> actualParamTypes = collectArgumentTypes(args);

        // Check parameter count
        if (expectedParamTypes.size() != actualParamTypes.size()) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
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
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Method call error: parameter %d of method '%s' expects type '%s', but got '%s'. " +
                                  "Ensure argument types match the method signature.",
                                  i + 1, methodName, expected, actual)
                );
            }
        }

        // Return the method's return type
        return methodeNode.getTypeMeth();
    }

    /**
     * Check assignment statement
     * @param affectation the AffectationNode to check
     */
    private void checkAffectation(AffectationNode affectation) {
        AstNode identNode = affectation.getIdent1Node();
        Expression expression = affectation.getExpression();

        if (identNode instanceof IdentNode ident) {
            String varName = ident.getNom();
            String qualifiedName = resolveAndQualifyName(varName);

            // Check if variable is declared
            if (!stacks.getSymbolTable().contains(qualifiedName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Undeclared variable: '%s' has not been declared. " +
                                  "Make sure to declare the variable before using it (e.g., 'int %s;').",
                                  varName, varName)
                );
                return;
            }

            // Check type compatibility
            Type varType = stacks.getSymbolTable().type(qualifiedName);
            Type exprType = inferType(expression);

            if (varType != null && exprType != null && varType != exprType) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Type mismatch in assignment: cannot assign value of type '%s' to variable '%s' of type '%s'. " +
                                  "The assigned expression must have the same type as the variable.",
                                  exprType, varName, varType)
                );
            }
        }
    }

    /**
     * Infer the type of an expression
     * @param expr the Expression to infer the type of
     */
    private Type inferType(Expression expr) {
        switch (expr) {
            case null -> { return null; }

            // Literals
            case NbreNode ignored -> {
                return Type.ENTIER;
            }
            case BoolValueNode ignored -> {
                return Type.BOOLEEN;
            }


            // Variable reference
            case IdentNode ident -> {
                String varName = ident.getNom();
                String qualifiedName = resolveAndQualifyName(varName);

                // Check if variable is declared
                if (!stacks.getSymbolTable().contains(qualifiedName)) {
                    collector.report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            createPosition(),
                            String.format("Undeclared variable: '%s' is used before being declared. " +
                                            "Declare it first (e.g., 'int %s;' or 'boolean %s;').",
                                    varName, varName, varName)
                    );
                    return null;
                }

                return stacks.getSymbolTable().type(qualifiedName);
            }


            // Arithmetic operations (require int operands, return int)
            case PlusNode plus -> {
                Type leftType = inferType(plus.getExp2());
                Type rightType = inferType(plus.getTerme());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.ENTIER, "+");
                return valid ? Type.ENTIER : null;
            }
            case MinusNode minus -> {
                Type leftType = inferType(minus.getExp2());
                Type rightType = inferType(minus.getTerme());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.ENTIER, "-");
                return valid ? Type.ENTIER : null;
            }
            case MultiplicationNode mult -> {
                Type leftType = inferType(mult.getTerme());
                Type rightType = inferType(mult.getFact());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.ENTIER, "*");
                return valid ? Type.ENTIER : null;
            }
            case DivisionNode div -> {
                Type leftType = inferType(div.getTerme());
                Type rightType = inferType(div.getFact());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.ENTIER, "/");
                return valid ? Type.ENTIER : null;
            }
            case UnaryMinusNode unary -> {
                Type operandType = inferType(unary.getTerme());
                if (operandType != null && operandType != Type.ENTIER) {
                    collector.report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            createPosition(),
                            String.format("Type error: unary minus operator '-' requires an integer operand, but got '%s'. " +
                                            "You cannot negate boolean values.",
                                    operandType)
                    );
                }
                return Type.ENTIER;
            }


            // Boolean operations (require boolean operands, return boolean)
            case AndNode and -> {
                Type leftType = inferType(and.getExp());
                Type rightType = inferType(and.getExp1());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.BOOLEEN, "and");
                return valid ? Type.BOOLEEN : null;
            }
            case OrNode or -> {
                Type leftType = inferType(or.getExp());
                Type rightType = inferType(or.getExp1());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.BOOLEEN, "or");
                return valid ? Type.BOOLEEN : null;
            }
            case NotNode not -> {
                Type operandType = inferType((Expression) not.getExp());
                if (operandType != null && operandType != Type.BOOLEEN) {
                    collector.report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            createPosition(),
                            String.format("Type error: logical NOT operator 'non' requires a boolean operand, but got '%s'. " +
                                            "Use comparison operators (==, >) to convert integers to booleans first.",
                                    operandType)
                    );
                }
                return Type.BOOLEEN;
            }


            // Method call as expression
            case AppelENode appelE -> {
                String methodName = appelE.getIdent().getNom();

                // Safely cast arguments to ListExpNode
                AstNode argsNode = appelE.getExp();
                if (!(argsNode instanceof ListExpNode args)) {
                    // Invalid arguments node - this shouldn't happen with a valid AST
                    return null;
                }

                // Validate the method call and return its type
                return validateMethodCall(methodName, args);
            }


            // Comparison operations (can compare any same types, return boolean)
            case EqualsNode equals -> {
                Type leftType = inferType((Expression) equals.getExp1());
                Type rightType = inferType((Expression) equals.getExp2());
                if (leftType != null && rightType != null && leftType != rightType) {
                    collector.report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            createPosition(),
                            String.format("Type error in equality comparison: cannot compare '%s' with '%s'. " +
                                            "Both operands of '==' must have the same type.",
                                    leftType, rightType)
                    );
                }
                return Type.BOOLEEN;
            }
            case GreaterThanNode greater -> {
                Type leftType = inferType(greater.getExp1());
                Type rightType = inferType(greater.getExp2());
                boolean valid = checkBinaryOperation(leftType, rightType, Type.ENTIER, ">");
                return valid ? Type.BOOLEEN : null;
            }
            default -> {}
        }

        // Unknown expression type
        return null;
    }

    /**
     * Check binary operation type compatibility
     * @param leftType the type of the left operand
     * @param rightType the type of the right operand
     * @param expectedType the expected type for both operands
     * @param operation the operation symbol (e.g., "+", "and", ">")
     * @return true if types are valid, false otherwise
     */
    private boolean checkBinaryOperation(Type leftType, Type rightType, Type expectedType, String operation) {
        String operationName = getOperationName(operation);
        boolean valid = true;

        if (leftType != null && leftType != expectedType) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
                String.format("Type error in %s: left operand of '%s' must be '%s', but got '%s'. " +
                              "Make sure both operands are of type '%s'.",
                              operationName, operation, expectedType, leftType, expectedType)
            );
            valid = false;
        }
        if (rightType != null && rightType != expectedType) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
                String.format("Type error in %s: right operand of '%s' must be '%s', but got '%s'. " +
                              "Make sure both operands are of type '%s'.",
                              operationName, operation, expectedType, rightType, expectedType)
            );
            valid = false;
        }
        return valid;
    }

    /**
     * Get a human-readable name for an operation
     * @param operation the operation symbol (e.g., "+", "and", ">")
     */
    private String getOperationName(String operation) {
        return switch (operation) {
            case "+", "-", "*", "/" -> "arithmetic operation";
            case "and", "or" -> "logical operation";
            case ">" -> "comparison operation";
            default -> "operation";
        };
    }

    // ============================================================
    // PUBLIC ACCESSORS
    // ============================================================

    public SymbolTable symbolTable() { return stacks.getSymbolTable(); }
    public DiagnosticCollector collector() { return collector; }
    public Stacks stacks() { return stacks; }
}
