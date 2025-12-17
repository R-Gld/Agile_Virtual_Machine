package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AffectationNode}.
 * Tests cover:
 * - Constructor and getters
 * - Simple variable assignment
 * - Array-to-array reference assignment
 * - Array element assignment
 * - Scope resolution in method contexts
 * - Type checking
 */
public class AffectationNodeTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ========================================
    // Constructor and Getter Tests
    // ========================================

    @Nested
    @DisplayName("Constructor and Getter Tests")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("Constructor initializes fields correctly")
        void testConstructor() {
            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(42);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            assertNotNull(affectationNode);
            assertEquals(identNode, affectationNode.getIdent1Node());
            assertEquals(expression, affectationNode.getExpression());
        }

        @Test
        @DisplayName("getIdent1Node returns correct identifier")
        void testGetIdent1Node() {
            IdentNode identNode = new IdentNode("myVar");
            Expression expression = new NbreNode(10);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            assertEquals(identNode, affectationNode.getIdent1Node());
        }

        @Test
        @DisplayName("getExpression returns correct expression")
        void testGetExpression() {
            IdentNode identNode = new IdentNode("myVar");
            Expression expression = new NbreNode(99);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            assertEquals(expression, affectationNode.getExpression());
        }
    }

    // ========================================
    // toStringTree Tests
    // ========================================

    @Nested
    @DisplayName("toStringTree Tests")
    class ToStringTreeTests {

        @Test
        @DisplayName("toStringTree returns correct format for simple assignment")
        void testToStringTree_SimpleAssignment() {
            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(5);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            assertEquals("affectation(Ident(x),nbre(5))", affectationNode.toStringTree());
        }

        @Test
        @DisplayName("toStringTree returns correct format for boolean assignment")
        void testToStringTree_BooleanAssignment() {
            IdentNode identNode = new IdentNode("flag");
            Expression expression = new BoolValueNode(true);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            assertEquals("affectation(Ident(flag),true)", affectationNode.toStringTree());
        }
    }

    // ========================================
    // getChildren Tests
    // ========================================

    @Nested
    @DisplayName("getChildren Tests")
    class GetChildrenTests {

        @Test
        @DisplayName("getChildren returns identifier and expression")
        void testGetChildren() {
            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(42);

            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            Iterable<AstNode> children = affectationNode.getChildren();
            List<AstNode> childrenList = (List<AstNode>) children;

            assertEquals(2, childrenList.size());
            assertEquals(identNode, childrenList.get(0));
            assertEquals(expression, childrenList.get(1));
        }
    }

    // ========================================
    // Simple Variable Assignment Tests
    // ========================================

    @Nested
    @DisplayName("Simple Variable Assignment Tests")
    class SimpleVariableAssignmentTests {

        @Test
        @DisplayName("Assign integer value to integer variable")
        void testAssignIntegerToVariable() {
            stacks.declareVar("x", 0, Type.ENTIER);

            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(42);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(42, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Assign boolean value to boolean variable")
        void testAssignBooleanToVariable() {
            stacks.declareVar("flag", false, Type.BOOLEEN);

            IdentNode identNode = new IdentNode("flag");
            Expression expression = new BoolValueNode(true);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(true, stacks.getValue("flag"));
        }

        @Test
        @DisplayName("Type error when assigning integer to boolean variable")
        void testTypeError_IntegerToBoolean() {
            stacks.declareVar("flag", false, Type.BOOLEEN);

            IdentNode identNode = new IdentNode("flag");
            Expression expression = new NbreNode(42);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                affectationNode.interpret(stacks);
            });
            assertTrue(exception.getMessage().contains("Type error"));
        }

        @Test
        @DisplayName("Type error when assigning boolean to integer variable")
        void testTypeError_BooleanToInteger() {
            stacks.declareVar("x", 0, Type.ENTIER);

            IdentNode identNode = new IdentNode("x");
            Expression expression = new BoolValueNode(true);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                affectationNode.interpret(stacks);
            });
            assertTrue(exception.getMessage().contains("Type error"));
        }
    }

    // ========================================
    // Array Element Assignment Tests
    // ========================================

    @Nested
    @DisplayName("Array Element Assignment Tests")
    class ArrayElementAssignmentTests {

        @Test
        @DisplayName("Assign value to array element")
        void testAssignToArrayElement() {
            stacks.declareTab("arr", 5, Type.ENTIER);

            TabNode tabNode = new TabNode(new IdentNode("arr"), new NbreNode(2));
            Expression expression = new NbreNode(99);
            AffectationNode affectationNode = new AffectationNode(tabNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(99, stacks.getArrayValue("arr", 2));
        }

        @Test
        @DisplayName("Assign value to first array element")
        void testAssignToFirstArrayElement() {
            stacks.declareTab("arr", 3, Type.ENTIER);

            TabNode tabNode = new TabNode(new IdentNode("arr"), new NbreNode(0));
            Expression expression = new NbreNode(10);
            AffectationNode affectationNode = new AffectationNode(tabNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(10, stacks.getArrayValue("arr", 0));
        }

        @Test
        @DisplayName("Assign value to last array element")
        void testAssignToLastArrayElement() {
            stacks.declareTab("arr", 4, Type.ENTIER);

            TabNode tabNode = new TabNode(new IdentNode("arr"), new NbreNode(3));
            Expression expression = new NbreNode(77);
            AffectationNode affectationNode = new AffectationNode(tabNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(77, stacks.getArrayValue("arr", 3));
        }

        @Test
        @DisplayName("Assign boolean to boolean array element")
        void testAssignBooleanToArrayElement() {
            stacks.declareTab("flags", 3, Type.BOOLEEN);

            TabNode tabNode = new TabNode(new IdentNode("flags"), new NbreNode(1));
            Expression expression = new BoolValueNode(true);
            AffectationNode affectationNode = new AffectationNode(tabNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(true, stacks.getArrayValue("flags", 1));
        }
    }

    // ========================================
    // Array-to-Array Reference Assignment Tests
    // ========================================

    @Nested
    @DisplayName("Array Reference Assignment Tests")
    class ArrayReferenceAssignmentTests {

        @Test
        @DisplayName("Assign array reference from one array to another")
        void testArrayReferenceAssignment() {
            stacks.declareTab("arr1", 3, Type.ENTIER);
            stacks.declareTab("arr2", 3, Type.ENTIER);

            // Set values in arr2
            stacks.setArrayValue("arr2", 0, 10);
            stacks.setArrayValue("arr2", 1, 20);
            stacks.setArrayValue("arr2", 2, 30);

            // arr1 = arr2 (reference assignment)
            IdentNode identNode = new IdentNode("arr1");
            Expression expression = new IdentNode("arr2");
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            // After assignment, arr1 should reference arr2's data
            assertEquals(10, stacks.getArrayValue("arr1", 0));
            assertEquals(20, stacks.getArrayValue("arr1", 1));
            assertEquals(30, stacks.getArrayValue("arr1", 2));
        }

        @Test
        @DisplayName("Array reference assignment shares the same memory block")
        void testArrayReferenceSharedMemory() {
            stacks.declareTab("arr1", 2, Type.ENTIER);
            stacks.declareTab("arr2", 2, Type.ENTIER);

            stacks.setArrayValue("arr2", 0, 100);

            // arr1 = arr2
            IdentNode identNode = new IdentNode("arr1");
            Expression expression = new IdentNode("arr2");
            AffectationNode affectationNode = new AffectationNode(identNode, expression);
            affectationNode.interpret(stacks);

            // Modify arr2[0]
            stacks.setArrayValue("arr2", 0, 999);

            // arr1 should see the change since they share the same block
            assertEquals(999, stacks.getArrayValue("arr1", 0));
        }
    }

    // ========================================
    // Scope Resolution Tests (Method Context)
    // ========================================

    @Nested
    @DisplayName("Scope Resolution Tests")
    class ScopeResolutionTests {

        @Test
        @DisplayName("Assign to scoped variable in method context")
        void testAssignToScopedVariable() {
            // Global variable
            stacks.declareVar("x", 10, Type.ENTIER);

            // Enter method context
            stacks.pushContext("myMethod");
            stacks.declareVar("x@myMethod", 0, Type.ENTIER);

            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(42);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            // Scoped variable should be updated
            assertEquals(42, stacks.getValue("x@myMethod"));
            // Global variable should remain unchanged
            assertEquals(10, stacks.getValue("x"));

            stacks.popContext("myMethod");
        }

        @Test
        @DisplayName("Assign to global variable when no scoped variable exists")
        void testAssignToGlobalWhenNoScopedExists() {
            // Only global variable
            stacks.declareVar("globalVar", 0, Type.ENTIER);

            // Enter method context
            stacks.pushContext("myMethod");

            IdentNode identNode = new IdentNode("globalVar");
            Expression expression = new NbreNode(55);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            // Global variable should be updated
            assertEquals(55, stacks.getValue("globalVar"));

            stacks.popContext("myMethod");
        }

        @Test
        @DisplayName("Assign to array element in method context")
        void testAssignToArrayElementInMethodContext() {
            // Global array
            stacks.declareTab("arr", 3, Type.ENTIER);

            // Enter method context
            stacks.pushContext("myMethod");
            stacks.declareTab("arr@myMethod", 3, Type.ENTIER);

            TabNode tabNode = new TabNode(new IdentNode("arr"), new NbreNode(0));
            Expression expression = new NbreNode(123);
            AffectationNode affectationNode = new AffectationNode(tabNode, expression);

            affectationNode.interpret(stacks);

            // Scoped array should be updated
            assertEquals(123, stacks.getArrayValue("arr@myMethod", 0));
            // Global array should remain unchanged (null or default)
            assertNull(stacks.getArrayValue("arr", 0));

            stacks.popContext("myMethod");
        }

        @Test
        @DisplayName("Array reference assignment with scoped variables")
        void testArrayReferenceAssignmentInMethodContext() {
            // Global arrays
            stacks.declareTab("src", 2, Type.ENTIER);
            stacks.setArrayValue("src", 0, 11);

            stacks.declareTab("dest", 2, Type.ENTIER);

            // Enter method context
            stacks.pushContext("myMethod");
            stacks.declareTab("src@myMethod", 2, Type.ENTIER);
            stacks.setArrayValue("src@myMethod", 0, 99);

            stacks.declareTab("dest@myMethod", 2, Type.ENTIER);

            // dest = src (should use scoped versions)
            IdentNode identNode = new IdentNode("dest");
            Expression expression = new IdentNode("src");
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            // Scoped dest should now reference scoped src
            assertEquals(99, stacks.getArrayValue("dest@myMethod", 0));

            stacks.popContext("myMethod");
        }

        @Test
        @DisplayName("Recursive context: assign in nested call")
        void testAssignInRecursiveContext() {
            // First call
            stacks.pushContext("factorial");
            stacks.declareVar("n@factorial", 5, Type.ENTIER);

            // Second call (recursive)
            stacks.pushContext("factorial");
            stacks.declareVar("n@factorial1", 4, Type.ENTIER);

            IdentNode identNode = new IdentNode("n");
            Expression expression = new NbreNode(100);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            // Most recent context should be updated
            assertEquals(100, stacks.getValue("n@factorial1"));
            // Previous context should remain unchanged
            assertEquals(5, stacks.getValue("n@factorial"));

            stacks.popContext("factorial");
            stacks.popContext("factorial");
        }
    }

    // ========================================
    // Edge Cases and Error Handling Tests
    // ========================================

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Assignment with IdentNode expression (non-array) evaluates variable")
        void testAssignmentWithIdentNodeExpression() {
            stacks.declareVar("source", 42, Type.ENTIER);
            stacks.declareVar("dest", 0, Type.ENTIER);

            IdentNode destNode = new IdentNode("dest");
            Expression sourceExpr = new IdentNode("source");
            AffectationNode affectationNode = new AffectationNode(destNode, sourceExpr);

            affectationNode.interpret(stacks);

            assertEquals(42, stacks.getValue("dest"));
        }

        @Test
        @DisplayName("Multiple assignments update value correctly")
        void testMultipleAssignments() {
            stacks.declareVar("x", 0, Type.ENTIER);

            for (int i = 1; i <= 5; i++) {
                IdentNode identNode = new IdentNode("x");
                Expression expression = new NbreNode(i * 10);
                AffectationNode affectationNode = new AffectationNode(identNode, expression);
                affectationNode.interpret(stacks);
            }

            assertEquals(50, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Assign negative integer value")
        void testAssignNegativeValue() {
            stacks.declareVar("x", 0, Type.ENTIER);

            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(-123);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(-123, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Assign zero value")
        void testAssignZeroValue() {
            stacks.declareVar("x", 100, Type.ENTIER);

            IdentNode identNode = new IdentNode("x");
            Expression expression = new NbreNode(0);
            AffectationNode affectationNode = new AffectationNode(identNode, expression);

            affectationNode.interpret(stacks);

            assertEquals(0, stacks.getValue("x"));
        }
    }
}
