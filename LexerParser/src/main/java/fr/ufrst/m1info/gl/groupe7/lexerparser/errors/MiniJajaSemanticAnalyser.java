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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.HashMap;
import java.util.Map;

public class MiniJajaSemanticAnalyser {

    private final DiagnosticCollector collector;
    private final SymbolTable symbolTable;
    private final Map<String, Type> declaredVariables;
    private String currentFileName;

    /**
     * Constructor.
     * @param collector the DiagnosticCollector to report errors to
     * @param symbolTable the SymbolTable to check against
     */
    public MiniJajaSemanticAnalyser(DiagnosticCollector collector, SymbolTable symbolTable) {
        this.collector = collector;
        this.symbolTable = symbolTable;
        this.declaredVariables = new HashMap<>();
        this.currentFileName = null;
    }

    /**
     * Set the current file name for error reporting
     */
    public void setFileName(String fileName) {
        this.currentFileName = fileName;
    }

    /**
     * Create a source position for error reporting
     */
    private SourcePosition createPosition() {
        return new SourcePosition(currentFileName, 0, 0);
    }

    public void analyse(ClasseNode ast) {
        // Phase 1: Collect all declarations
        checkDeclarations(ast);

        // Phase 2: Check types and variable usage
        checkType(ast);
    }

    /**
     * Phase 1: Collect all variable declarations and check for duplicates
     */
    private void checkDeclarations(ClasseNode ast) {
        // Collect global variable declarations
        collectDeclarations(ast.getDeclarations());

        // Process main method
        AstNode mainMethod = ast.getMethodeMain();
        if (mainMethod instanceof MainNode mainNode) {
            collectVars(mainNode.getVars());
        } else if (mainMethod instanceof MethodeNode methodeNode) {
            collectVars(methodeNode.getVars());
            // TODO: Handle method parameters (entetes)
        }
    }

    /**
     * Collect declarations from DeclsNode
     */
    private void collectDeclarations(DeclsNode decls) {
        if (decls == null) return;

        AstNode decl = decls.getDecl();
        if (decl instanceof VarNode varNode) {
            String varName = varNode.getIdent().getNom();
            Type varType = varNode.getType();

            // Check for duplicate declarations
            if (declaredVariables.containsKey(varName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate variable declaration: '%s' has already been declared. " +
                                  "Each variable can only be declared once in the same scope.", varName)
                );
            } else {
                declaredVariables.put(varName, varType);
            }
        }

        // Process next declaration
        collectDeclarations(decls.getDecls());
    }

    /**
     * Collect local variable declarations from VarsNode
     */
    private void collectVars(VarsNode vars) {
        if (vars == null) return;

        // VarsNode has a linked-list structure: var + vars
        VarNode var = vars.getVar();
        if (var != null) {
            String varName = var.getIdent().getNom();
            Type varType = var.getType();

            // Check for duplicate declarations
            if (declaredVariables.containsKey(varName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Duplicate local variable: '%s' has already been declared. " +
                                  "Please use a different variable name.", varName)
                );
            } else {
                declaredVariables.put(varName, varType);
            }
        }

        // Process the rest of the variables recursively
        collectVars(vars.getVars());
    }

    /**
     * Phase 2: Check type compatibility and variable usage
     */
    private void checkType(ClasseNode ast) {
        // Check main method
        AstNode mainMethod = ast.getMethodeMain();
        if (mainMethod instanceof MainNode mainNode) {
            checkInstructions(mainNode.getInstrs());
        } else if (mainMethod instanceof MethodeNode methodeNode) {
            checkInstructions(methodeNode.getInstrs());
        }
    }

    /**
     * Check instructions node
     */
    private void checkInstructions(InstructionsNode instrs) {
        if (instrs == null) return;

        for (AstNode child : instrs.getChildren()) {
            checkNode(child);
        }
    }

    /**
     * Check any AST node recursively
     */
    private void checkNode(AstNode node) {
        switch (node) {
            case null -> {return;}

            // Check specific node types
            case AffectationNode affectation -> checkAffectation(affectation);
            case InstructionsNode instructions -> checkInstructions(instructions);
            case Expression expression -> inferType(expression);
            default -> {
            }
        }

        // Recursively check children
        for (AstNode child : node.getChildren()) {
            checkNode(child);
        }
    }

    /**
     * Check assignment statement
     */
    private void checkAffectation(AffectationNode affectation) {
        AstNode identNode = affectation.getIdent1Node();
        Expression expression = affectation.getExpression();

        if (identNode instanceof IdentNode ident) {
            String varName = ident.getNom();

            // Check if variable is declared
            if (!declaredVariables.containsKey(varName)) {
                collector.report(
                    Severity.ERROR,
                    Phase.SEMANTIC,
                    createPosition(),
                    String.format("Undeclared variable: '%s' has not been declared. " +
                                  "Make sure to declare the variable before using it (e.g., 'var integer %s;').",
                                  varName, varName)
                );
                return;
            }

            // Check type compatibility
            Type varType = declaredVariables.get(varName);
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

                // Check if variable is declared
                if (!declaredVariables.containsKey(varName)) {
                    collector.report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            createPosition(),
                            String.format("Undeclared variable: '%s' is used before being declared. " +
                                            "Declare it first (e.g., 'var integer %s;' or 'var boolean %s;').",
                                    varName, varName, varName)
                    );
                    return null;
                }

                return symbolTable.type(varName);
            }


            // Arithmetic operations (require int operands, return int)
            case PlusNode plus -> {
                Type leftType = inferType(plus.getExp2());
                Type rightType = inferType(plus.getTerme());
                checkBinaryOperation(leftType, rightType, Type.ENTIER, "+");
                return Type.ENTIER;
            }
            case MinusNode minus -> {
                Type leftType = inferType(minus.getExp2());
                Type rightType = inferType(minus.getTerme());
                checkBinaryOperation(leftType, rightType, Type.ENTIER, "-");
                return Type.ENTIER;
            }
            case MultiplicationNode mult -> {
                Type leftType = inferType(mult.getTerme());
                Type rightType = inferType(mult.getFact());
                checkBinaryOperation(leftType, rightType, Type.ENTIER, "*");
                return Type.ENTIER;
            }
            case DivisionNode div -> {
                Type leftType = inferType(div.getTerme());
                Type rightType = inferType(div.getFact());
                checkBinaryOperation(leftType, rightType, Type.ENTIER, "/");
                return Type.ENTIER;
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
                checkBinaryOperation(leftType, rightType, Type.BOOLEEN, "and");
                return Type.BOOLEEN;
            }
            case OrNode or -> {
                Type leftType = inferType(or.getExp());
                Type rightType = inferType(or.getExp1());
                checkBinaryOperation(leftType, rightType, Type.BOOLEEN, "or");
                return Type.BOOLEEN;
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
                checkBinaryOperation(leftType, rightType, Type.ENTIER, ">");
                return Type.BOOLEEN;
            }
            default -> {}
        }

        // Unknown expression type
        return null;
    }

    /**
     * Check binary operation type compatibility
     */
    private void checkBinaryOperation(Type leftType, Type rightType, Type expectedType, String operation) {
        String operationName = getOperationName(operation);

        if (leftType != null && leftType != expectedType) {
            collector.report(
                Severity.ERROR,
                Phase.SEMANTIC,
                createPosition(),
                String.format("Type error in %s: left operand of '%s' must be '%s', but got '%s'. " +
                              "Make sure both operands are of type '%s'.",
                              operationName, operation, expectedType, leftType, expectedType)
            );
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
        }
    }

    /**
     * Get a human-readable name for an operation
     */
    private String getOperationName(String operation) {
        return switch (operation) {
            case "+", "-", "*", "/" -> "arithmetic operation";
            case "and", "or" -> "logical operation";
            case ">" -> "comparison operation";
            default -> "operation";
        };
    }

    public SymbolTable symbolTable() { return symbolTable; }
    public DiagnosticCollector collector() { return collector; }
}
