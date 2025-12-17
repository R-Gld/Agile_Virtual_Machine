package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

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
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for type incompatibility detection in MiniJaja expressions and operations.
 * Tests all combinations of incompatible types across arithmetic, logical, and comparison operators.
 */
public class TypeIncompatibilityTest {

    private Stacks stacks;
    private Type type ;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
        // Declare test variables of different types
        stacks.declareVar("intVar", 10, Type.ENTIER);
        stacks.declareVar("boolVar", true, Type.BOOLEEN);
        stacks.declareVar("intVar2", 5, Type.ENTIER);
        stacks.declareVar("boolVar2", false, Type.BOOLEEN);
    }

    // ========================================
    // ARITHMETIC OPERATIONS (require integers)
    // ========================================

    @Test
    void testAddition_IntPlusInt_Valid() {
        Expression expr = new PlusNode(new NbreNode(5), new NbreNode(3));
        Object result = expr.evaluate(stacks);
        assertEquals(8, result);
    }

    @Test
    void testAddition_IntPlusBool_Invalid() {
        Expression expr = new PlusNode(new NbreNode(5), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Adding integer + boolean should throw exception");
    }

    @Test
    void testAddition_BoolPlusInt_Invalid() {
        Expression expr = new PlusNode(new BoolValueNode(false), new NbreNode(10));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Adding boolean + integer should throw exception");
    }

    @Test
    void testAddition_BoolPlusBool_Invalid() {
        Expression expr = new PlusNode(new BoolValueNode(true), new BoolValueNode(false));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Adding boolean + boolean should throw exception");
    }

    @Test
    void testSubtraction_IntMinusInt_Valid() {
        Expression expr = new MinusNode(new NbreNode(10), new NbreNode(3));
        Object result = expr.evaluate(stacks);
        assertEquals(7, result);
    }

    @Test
    void testSubtraction_IntMinusBool_Invalid() {
        Expression expr = new MinusNode(new NbreNode(10), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Subtracting integer - boolean should throw exception");
    }

    @Test
    void testSubtraction_BoolMinusInt_Invalid() {
        Expression expr = new MinusNode(new BoolValueNode(true), new NbreNode(5));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Subtracting boolean - integer should throw exception");
    }

    @Test
    void testSubtraction_BoolMinusBool_Invalid() {
        Expression expr = new MinusNode(new BoolValueNode(false), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Subtracting boolean - boolean should throw exception");
    }

    @Test
    void testMultiplication_IntTimesInt_Valid() {
        Expression expr = new MultiplicationNode(new NbreNode(4), new NbreNode(5));
        Object result = expr.evaluate(stacks);
        assertEquals(20, result);
    }

    @Test
    void testMultiplication_IntTimesBool_Invalid() {
        Expression expr = new MultiplicationNode(new NbreNode(4), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Multiplying integer * boolean should throw exception");
    }

    @Test
    void testMultiplication_BoolTimesInt_Invalid() {
        Expression expr = new MultiplicationNode(new BoolValueNode(false), new NbreNode(7));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Multiplying boolean * integer should throw exception");
    }

    @Test
    void testMultiplication_BoolTimesBool_Invalid() {
        Expression expr = new MultiplicationNode(new BoolValueNode(true), new BoolValueNode(false));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Multiplying boolean * boolean should throw exception");
    }

    @Test
    void testDivision_IntDivideInt_Valid() {
        Expression expr = new DivisionNode(new NbreNode(20), new NbreNode(4));
        Object result = expr.evaluate(stacks);
        assertEquals(5, result);
    }

    @Test
    void testDivision_IntDivideBool_Invalid() {
        Expression expr = new DivisionNode(new NbreNode(20), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Dividing integer / boolean should throw exception");
    }

    @Test
    void testDivision_BoolDivideInt_Invalid() {
        Expression expr = new DivisionNode(new BoolValueNode(true), new NbreNode(2));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Dividing boolean / integer should throw exception");
    }

    @Test
    void testDivision_BoolDivideBool_Invalid() {
        Expression expr = new DivisionNode(new BoolValueNode(false), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Dividing boolean / boolean should throw exception");
    }

    @Test
    void testUnaryMinus_Int_Valid() {
        Expression expr = new UnaryMinusNode(new NbreNode(5));
        Object result = expr.evaluate(stacks);
        assertEquals(-5, result);
    }

    @Test
    void testUnaryMinus_Bool_Invalid() {
        Expression expr = new UnaryMinusNode(new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Unary minus on boolean should throw exception");
    }

    // ========================================
    // LOGICAL OPERATIONS (require booleans)
    // ========================================

    @Test
    void testAnd_BoolAndBool_Valid() {
        Expression expr = new AndNode(new BoolValueNode(true), new BoolValueNode(false));
        Object result = expr.evaluate(stacks);
        assertEquals(false, result);
    }

    @Test
    void testAnd_IntAndBool_Invalid() {
        Expression expr = new AndNode(new NbreNode(5), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "AND with integer && boolean should throw exception");
    }

    @Test
    void testAnd_BoolAndInt_Invalid() {
        Expression expr = new AndNode(new BoolValueNode(false), new NbreNode(10));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "AND with boolean && integer should throw exception");
    }

    @Test
    void testAnd_IntAndInt_Invalid() {
        Expression expr = new AndNode(new NbreNode(1), new NbreNode(0));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "AND with integer && integer should throw exception");
    }

    @Test
    void testOr_BoolOrBool_Valid() {
        Expression expr = new OrNode(new BoolValueNode(false), new BoolValueNode(true));
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testOr_IntOrBool_Invalid() {
        Expression expr = new OrNode(new NbreNode(0), new BoolValueNode(false));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "OR with integer || boolean should throw exception");
    }

    @Test
    void testOr_BoolOrInt_Invalid() {
        Expression expr = new OrNode(new BoolValueNode(true), new NbreNode(1));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "OR with boolean || integer should throw exception");
    }

    @Test
    void testOr_IntOrInt_Invalid() {
        Expression expr = new OrNode(new NbreNode(5), new NbreNode(3));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "OR with integer || integer should throw exception");
    }

    @Test
    void testNot_Bool_Valid() {
        Expression expr = new NotNode(new BoolValueNode(true));
        Object result = expr.evaluate(stacks);
        assertEquals(false, result);
    }

    @Test
    void testNot_Int_Invalid() {
        Expression expr = new NotNode(new NbreNode(1));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "NOT with integer should throw exception");
    }

    // ========================================
    // COMPARISON OPERATIONS
    // ========================================

    @Test
    void testGreaterThan_IntGreaterInt_Valid() {
        Expression expr = new GreaterThanNode(new NbreNode(10), new NbreNode(5));
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testGreaterThan_IntGreaterBool_Invalid() {
        Expression expr = new GreaterThanNode(new NbreNode(10), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing integer > boolean should throw exception");
    }

    @Test
    void testGreaterThan_BoolGreaterInt_Invalid() {
        Expression expr = new GreaterThanNode(new BoolValueNode(true), new NbreNode(5));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing boolean > integer should throw exception");
    }

    @Test
    void testGreaterThan_BoolGreaterBool_Invalid() {
        Expression expr = new GreaterThanNode(new BoolValueNode(true), new BoolValueNode(false));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing boolean > boolean should throw exception");
    }

    @Test
    void testEquals_IntEqualsInt_Valid() {
        Expression expr = new EqualsNode(new NbreNode(5), new NbreNode(5));
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testEquals_BoolEqualsBool_Valid() {
        Expression expr = new EqualsNode(new BoolValueNode(true), new BoolValueNode(true));
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testEquals_IntEqualsBool_Invalid() {
        Expression expr = new EqualsNode(new NbreNode(1), new BoolValueNode(true));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing integer == boolean should throw exception");
    }

    @Test
    void testEquals_BoolEqualsInt_Invalid() {
        Expression expr = new EqualsNode(new BoolValueNode(false), new NbreNode(0));
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing boolean == integer should throw exception");
    }

    // ========================================
    // MIXED/COMPLEX EXPRESSION TYPE CHECKS
    // ========================================

    @Test
    void testComplexArithmetic_AllInt_Valid() {
        // (5 + 3) * (10 - 2)
        Expression expr = new MultiplicationNode(
                new PlusNode(new NbreNode(5), new NbreNode(3)),
                new MinusNode(new NbreNode(10), new NbreNode(2))
        );
        Object result = expr.evaluate(stacks);
        assertEquals(64, result);
    }

    @Test
    void testComplexArithmetic_WithBool_Invalid() {
        // (5 + true) * 2  -- should fail at addition
        Expression expr = new MultiplicationNode(
                new PlusNode(new NbreNode(5), new BoolValueNode(true)),
                new NbreNode(2)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Complex arithmetic with boolean operand should throw exception");
    }

    @Test
    void testComplexLogical_AllBool_Valid() {
        // (true && false) || true
        Expression expr = new OrNode(
                new AndNode(new BoolValueNode(true), new BoolValueNode(false)),
                new BoolValueNode(true)
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testComplexLogical_WithInt_Invalid() {
        // (true && 5) || false  -- should fail at AND
        Expression expr = new OrNode(
                new AndNode(new BoolValueNode(true), new NbreNode(5)),
                new BoolValueNode(false)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Complex logical with integer operand should throw exception");
    }

    @Test
    void testMixedComparison_ArithmeticThenCompare_Valid() {
        // (5 + 3) > 10  -- evaluates to false
        Expression expr = new GreaterThanNode(
                new PlusNode(new NbreNode(5), new NbreNode(3)),
                new NbreNode(10)
        );
        Object result = expr.evaluate(stacks);
        assertEquals(false, result);
    }

    @Test
    void testMixedComparison_BoolInArithmetic_Invalid() {
        // (true + 5) > 10  -- should fail at addition
        Expression expr = new GreaterThanNode(
                new PlusNode(new BoolValueNode(true), new NbreNode(5)),
                new NbreNode(10)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparison with type-incompatible arithmetic should throw exception");
    }

    @Test
    void testLogicalWithComparison_Valid() {
        // (5 > 3) && (10 == 10)
        Expression expr = new AndNode(
                new GreaterThanNode(new NbreNode(5), new NbreNode(3)),
                new EqualsNode(new NbreNode(10), new NbreNode(10))
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testLogicalWithInvalidComparison_Invalid() {
        // (5 > true) && false  -- should fail at comparison
        Expression expr = new AndNode(
                new GreaterThanNode(new NbreNode(5), new BoolValueNode(true)),
                new BoolValueNode(false)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Logical operation with invalid comparison should throw exception");
    }

    // ========================================
    // VARIABLE-BASED TYPE INCOMPATIBILITIES
    // ========================================

    @Test
    void testVariableArithmetic_IntPlusInt_Valid() {
        Expression expr = new PlusNode(
                new IdentNode("intVar"),
                new IdentNode("intVar2")
        );
        Object result = expr.evaluate(stacks);
        assertEquals(15, result); // 10 + 5
    }

    @Test
    void testVariableArithmetic_IntPlusBoolVar_Invalid() {
        Expression expr = new PlusNode(
                new IdentNode("intVar"),
                new IdentNode("boolVar")
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Adding int variable + bool variable should throw exception");
    }

    @Test
    void testVariableLogical_BoolAndBool_Valid() {
        Expression expr = new AndNode(
                new IdentNode("boolVar"),
                new IdentNode("boolVar2")
        );
        Object result = expr.evaluate(stacks);
        assertEquals(false, result); // true && false
    }

    @Test
    void testVariableLogical_IntAndBoolVar_Invalid() {
        Expression expr = new AndNode(
                new IdentNode("intVar"),
                new IdentNode("boolVar")
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "AND with int variable && bool variable should throw exception");
    }

    @Test
    void testVariableComparison_IntGreaterInt_Valid() {
        Expression expr = new GreaterThanNode(
                new IdentNode("intVar"),
                new IdentNode("intVar2")
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result); // 10 > 5
    }

    @Test
    void testVariableComparison_BoolGreaterBool_Invalid() {
        Expression expr = new GreaterThanNode(
                new IdentNode("boolVar"),
                new IdentNode("boolVar2")
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing bool variable > bool variable should throw exception");
    }

    @Test
    void testVariableEquality_IntEqualsInt_Valid() {
        Expression expr = new EqualsNode(
                new IdentNode("intVar2"),
                new NbreNode(5)
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testVariableEquality_BoolEqualsBool_Valid() {
        Expression expr = new EqualsNode(
                new IdentNode("boolVar"),
                new BoolValueNode(true)
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testVariableEquality_MixedTypes_Invalid() {
        Expression expr = new EqualsNode(
                new IdentNode("intVar"),
                new IdentNode("boolVar")
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Comparing int variable == bool variable should throw exception");
    }

    // ========================================
    // EDGE CASES AND NESTED SCENARIOS
    // ========================================

    @Test
    void testDeeplyNestedArithmetic_AllInt_Valid() {
        // ((10 + 5) * 2) - (8 / 4)
        Expression expr = new MinusNode(
                new MultiplicationNode(
                        new PlusNode(new NbreNode(10), new NbreNode(5)),
                        new NbreNode(2)
                ),
                new DivisionNode(new NbreNode(8), new NbreNode(4))
        );
        Object result = expr.evaluate(stacks);
        assertEquals(28, result);
    }

    @Test
    void testDeeplyNestedArithmetic_WithBoolAtBottom_Invalid() {
        // ((10 + true) * 2) - 4
        Expression expr = new MinusNode(
                new MultiplicationNode(
                        new PlusNode(new NbreNode(10), new BoolValueNode(true)),
                        new NbreNode(2)
                ),
                new NbreNode(4)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Deeply nested arithmetic with boolean should throw exception");
    }

    @Test
    void testDeeplyNestedLogical_AllBool_Valid() {
        // !((true || false) && true)
        Expression expr = new NotNode(
                new AndNode(
                        new OrNode(new BoolValueNode(true), new BoolValueNode(false)),
                        new BoolValueNode(true)
                )
        );
        Object result = expr.evaluate(stacks);
        assertEquals(false, result);
    }

    @Test
    void testDeeplyNestedLogical_WithIntAtMiddle_Invalid() {
        // !((true || 5) && false)
        Expression expr = new NotNode(
                new AndNode(
                        new OrNode(new BoolValueNode(true), new NbreNode(5)),
                        new BoolValueNode(false)
                )
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Deeply nested logical with integer should throw exception");
    }

    @Test
    void testChainedComparisons_Valid() {
        // (5 > 3) == (10 > 8)  -- both comparisons return true, so true == true
        Expression expr = new EqualsNode(
                new GreaterThanNode(new NbreNode(5), new NbreNode(3)),
                new GreaterThanNode(new NbreNode(10), new NbreNode(8))
        );
        Object result = expr.evaluate(stacks);
        assertEquals(true, result);
    }

    @Test
    void testInvalidChainedComparisons_Invalid() {
        // (5 > 3) > 2  -- first comparison returns boolean, then boolean > int is invalid
        Expression expr = new GreaterThanNode(
                new GreaterThanNode(new NbreNode(5), new NbreNode(3)),
                new NbreNode(2)
        );
        assertThrows(RuntimeException.class, () -> expr.evaluate(stacks),
                "Chained comparison with boolean > int should throw exception");
    }

    @Test
    void testDivisionByZero_ShouldThrow() {
        Expression expr = new DivisionNode(new NbreNode(10), new NbreNode(0));
        assertThrows(ArithmeticException.class, () -> expr.evaluate(stacks),
                "Division by zero should throw ArithmeticException");
    }
}
