package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

/**
 * Engine for inferring types of expressions in the MiniJaja AST.
 * Handles all expression types including literals, variables, operators, and method calls.
 */
public class TypeInferenceEngine {

    private final SemanticContext context;
    private final ScopeResolver scopeResolver;

    // Injected to break circular dependency, set after construction
    private MethodCallValidator methodCallValidator;

    /**
     * Constructor.
     *
     * @param context the semantic context
     * @param scopeResolver the scope resolver for variable lookup
     */
    public TypeInferenceEngine(SemanticContext context, ScopeResolver scopeResolver) {
        this.context = context;
        this.scopeResolver = scopeResolver;
    }

    /**
     * Set the method call validator (for breaking circular dependency).
     * Must be called after construction before inferType is used with method calls.
     *
     * @param validator the method call validator
     */
    public void setMethodCallValidator(MethodCallValidator validator) {
        this.methodCallValidator = validator;
    }

    /**
     * Infer the type of an expression
     * @param expr the Expression to infer the type of
     * @return the inferred type, or null if the type cannot be determined
     */
    public Type inferType(Expression expr) {
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
                String qualifiedName = scopeResolver.resolveAndQualifyName(varName);

                // Check if variable is declared
                if (!context.getSymbolTable().contains(qualifiedName)) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(ident),
                            String.format("Undeclared variable: '%s' is used before being declared. " +
                                            "Declare it first (ex: 'int %s;' or 'boolean %s;').",
                                    varName, varName, varName)
                    );
                    return null;
                }

                return context.getSymbolTable().type(qualifiedName);
            }


            // Arithmetic operations (require int operands, return int)
            case PlusNode plus -> {
                Type leftType = inferType(plus.getExp2());
                Type rightType = inferType(plus.getTerme());
                boolean valid = checkBinaryOperation(plus, leftType, rightType, Type.ENTIER, "+");
                return valid ? Type.ENTIER : null;
            }
            case MinusNode minus -> {
                Type leftType = inferType(minus.getExp2());
                Type rightType = inferType(minus.getTerme());
                boolean valid = checkBinaryOperation(minus, leftType, rightType, Type.ENTIER, "-");
                return valid ? Type.ENTIER : null;
            }
            case MultiplicationNode mult -> {
                Type leftType = inferType(mult.getTerme());
                Type rightType = inferType(mult.getFact());
                boolean valid = checkBinaryOperation(mult, leftType, rightType, Type.ENTIER, "*");
                return valid ? Type.ENTIER : null;
            }
            case DivisionNode div -> {
                Type leftType = inferType(div.getTerme());
                Type rightType = inferType(div.getFact());
                boolean valid = checkBinaryOperation(div, leftType, rightType, Type.ENTIER, "/");
                return valid ? Type.ENTIER : null;
            }
            case UnaryMinusNode unary -> {
                Type operandType = inferType(unary.getTerme());
                if (operandType != null && operandType != Type.ENTIER) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(unary),
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
                boolean valid = checkBinaryOperation(and, leftType, rightType, Type.BOOLEEN, "and");
                return valid ? Type.BOOLEEN : null;
            }
            case OrNode or -> {
                Type leftType = inferType(or.getExp());
                Type rightType = inferType(or.getExp1());
                boolean valid = checkBinaryOperation(or, leftType, rightType, Type.BOOLEEN, "or");
                return valid ? Type.BOOLEEN : null;
            }
            case NotNode not -> {
                Type operandType = inferType((Expression) not.getExp());
                if (operandType != null && operandType != Type.BOOLEEN) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(not),
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
                    // Invalid arguments node, this shouldn't happen with a valid AST
                    return null;
                }

                // Validate the method call and return its type
                // Note: methodCallValidator is injected after construction
                if (methodCallValidator != null) {
                    return methodCallValidator.validateMethodCall(appelE);
                }
                // If validator not set yet, return null
                return null;
            }


            // Comparison operations (can compare any same types, return boolean)
            case EqualsNode equals -> {
                Type leftType = inferType((Expression) equals.getExp1());
                Type rightType = inferType((Expression) equals.getExp2());
                if (leftType != null && rightType != null && leftType != rightType) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(equals),
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
                boolean valid = checkBinaryOperation(greater, leftType, rightType, Type.ENTIER, ">");
                return valid ? Type.BOOLEEN : null;
            }


            // Array operations
            case TabNode tabNode -> {
                // Array access: arr[index] returns the array's element type
                String arrayName = tabNode.getIdent().getNom();
                String qualifiedName = scopeResolver.resolveAndQualifyName(arrayName);

                // Check if array is declared
                if (!context.getSymbolTable().contains(qualifiedName)) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(tabNode),
                            String.format("Undeclared array: '%s' is used before being declared. " +
                                            "Declare it first (ex: 'int %s[10];').",
                                    arrayName, arrayName)
                    );
                    return null;
                }

                // Validate index is integer type
                Type indexType = inferType(tabNode.getIndex());
                if (indexType != null && indexType != Type.ENTIER) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(tabNode),
                            String.format("Array index must be an integer: array '%s' accessed with index type '%s'. " +
                                            "Array indices must be integer expressions.",
                                    arrayName, indexType)
                    );
                }

                // Return the array's element type
                return context.getSymbolTable().type(qualifiedName);
            }

            case LengthNode lengthNode -> {
                // length(arr) always returns int
                String arrayName = lengthNode.getId().getNom();
                String qualifiedName = scopeResolver.resolveAndQualifyName(arrayName);

                // Check if array is declared
                if (!context.getSymbolTable().contains(qualifiedName)) {
                    context.getCollector().report(
                            Severity.ERROR,
                            Phase.SEMANTIC,
                            context.createPosition(lengthNode),
                            String.format("Undeclared array: '%s' is used before being declared. " +
                                            "Declare it first (ex: 'int %s[10];').",
                                    arrayName, arrayName)
                    );
                    return null;
                }

                // length operation always returns ENTIER
                return Type.ENTIER;
            }

            default -> {}
        }

        // Unknown expression type
        return null;
    }

    /**
     * Check binary operation type compatibility
     * @param node the AST node representing the operation
     * @param leftType the type of the left operand
     * @param rightType the type of the right operand
     * @param expectedType the expected type for both operands
     * @param operation the operation symbol (ex: "+", "and", ">")
     * @return true if types are valid, false otherwise
     */
    private boolean checkBinaryOperation(AstNode node, Type leftType, Type rightType, Type expectedType, String operation) {
        String operationName = getOperationName(operation);
        boolean valid = true;

        if (leftType != null && leftType != expectedType) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(node),
                String.format("Type error in %s: left operand of '%s' must be '%s', but got '%s'. " +
                              "Make sure both operands are of type '%s'.",
                              operationName, operation, expectedType, leftType, expectedType)
            );
            valid = false;
        }
        if (rightType != null && rightType != expectedType) {
            context.getCollector().report(
                Severity.ERROR,
                Phase.SEMANTIC,
                context.createPosition(node),
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
     * @param operation the operation symbol (ex: "+", "and", ">")
     * @return a human-readable description of the operation type
     */
    private String getOperationName(String operation) {
        return switch (operation) {
            case "+", "-", "*", "/" -> "arithmetic operation";
            case "and", "or" -> "logical operation";
            case ">" -> "comparison operation";
            default -> "operation";
        };
    }

    /**
     * Check if an expression represents an array variable.
     * @param expr the Expression to check
     * @return true if the expression is an array variable, false otherwise
     */
    public boolean isArrayExpression(Expression expr) {
        if (expr instanceof IdentNode ident) {
            String varName = ident.getNom();
            String qualifiedName = scopeResolver.resolveAndQualifyName(varName);

            var symbol = context.getSymbolTable().findSymbol(qualifiedName);
            return symbol != null && symbol.isArray();
        }
        return false;
    }
}
