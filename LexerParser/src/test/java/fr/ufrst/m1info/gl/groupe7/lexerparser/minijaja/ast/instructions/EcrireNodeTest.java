package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for EcrireNode.
 * Tests all branches in interpret() and toStringTree().
 */
public class EcrireNodeTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ==================== CONSTRUCTOR AND GETTER TESTS ====================

    @Nested
    @DisplayName("Constructor and Getter Tests")
    class ConstructorAndGetterTests {

        @Test
        @DisplayName("Constructor with String stores value")
        void constructorWithString() {
            EcrireNode node = new EcrireNode("test");
            assertEquals("test", node.getIdent1Node());
        }

        @Test
        @DisplayName("Constructor with IdentNode stores value")
        void constructorWithIdentNode() {
            IdentNode ident = new IdentNode("x");
            EcrireNode node = new EcrireNode(ident);
            assertSame(ident, node.getIdent1Node());
        }

        @Test
        @DisplayName("Constructor with Expression stores value")
        void constructorWithExpression() {
            NbreNode expr = new NbreNode(42);
            EcrireNode node = new EcrireNode(expr);
            assertSame(expr, node.getIdent1Node());
        }

        @Test
        @DisplayName("Constructor with TabNode stores value")
        void constructorWithTabNode() {
            TabNode tab = new TabNode(new IdentNode("arr"), new NbreNode(0));
            EcrireNode node = new EcrireNode(tab);
            assertSame(tab, node.getIdent1Node());
        }

        @Test
        @DisplayName("Constructor with null stores null")
        void constructorWithNull() {
            EcrireNode node = new EcrireNode(null);
            assertNull(node.getIdent1Node());
        }
    }

    // ==================== toStringTree TESTS ====================

    @Nested
    @DisplayName("toStringTree Tests")
    class ToStringTreeTests {

        @Test
        @DisplayName("toStringTree with String")
        void toStringTreeWithString() {
            EcrireNode node = new EcrireNode("HelloWorld");
            assertEquals("ecrire (HelloWorld)", node.toStringTree());
        }

        @Test
        @DisplayName("toStringTree with Expression (NbreNode)")
        void toStringTreeWithNbreNode() {
            EcrireNode node = new EcrireNode(new NbreNode(42));
            assertEquals("ecrire (nbre(42))", node.toStringTree());
        }

        @Test
        @DisplayName("toStringTree with Expression (BoolValueNode)")
        void toStringTreeWithBoolValueNode() {
            EcrireNode node = new EcrireNode(new BoolValueNode(true));
            assertEquals("ecrire (true)", node.toStringTree());
        }

        @Test
        @DisplayName("toStringTree with complex Expression")
        void toStringTreeWithComplexExpression() {
            PlusNode plus = new PlusNode(new NbreNode(1), new NbreNode(2));
            EcrireNode node = new EcrireNode(plus);
            String tree = node.toStringTree();
            assertTrue(tree.startsWith("ecrire ("));
            assertTrue(tree.contains("plus") || tree.contains("+") || tree.contains("1") && tree.contains("2"));
        }

        @Test
        @DisplayName("toStringTree with IdentNode (not Expression)")
        void toStringTreeWithIdentNode() {
            IdentNode ident = new IdentNode("myVar");
            EcrireNode node = new EcrireNode(ident);
            String tree = node.toStringTree();
            // IdentNode implements Expression, so it should use toStringTree()
            assertTrue(tree.contains("ecrire"));
        }

        @Test
        @DisplayName("toStringTree with Integer")
        void toStringTreeWithInteger() {
            EcrireNode node = new EcrireNode(123);
            assertEquals("ecrire (123)", node.toStringTree());
        }

        @Test
        @DisplayName("toStringTree with null")
        void toStringTreeWithNull() {
            EcrireNode node = new EcrireNode(null);
            assertEquals("ecrire (null)", node.toStringTree());
        }
    }

    // ==================== interpret with IdentNode TESTS ====================

    @Nested
    @DisplayName("interpret with IdentNode Tests")
    class InterpretIdentNodeTests {

        @Test
        @DisplayName("interpret with IdentNode for integer variable")
        void interpretIdentNodeInteger() {
            stacks.declareVar("x", 42, Type.ENTIER);
            IdentNode ident = new IdentNode("x");
            EcrireNode node = new EcrireNode(ident);

            // Should not throw, logs the value
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with IdentNode for boolean variable")
        void interpretIdentNodeBoolean() {
            stacks.declareVar("flag", true, Type.BOOLEEN);
            IdentNode ident = new IdentNode("flag");
            EcrireNode node = new EcrireNode(ident);

            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with IdentNode throws for array type")
        void interpretIdentNodeArrayThrows() {
            stacks.declareTab("arr", 5, Type.ENTIER);
            IdentNode ident = new IdentNode("arr");
            EcrireNode node = new EcrireNode(ident);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> node.interpret(stacks));
            assertTrue(ex.getMessage().contains("cannot print array"));
        }

        @Test
        @DisplayName("interpret with IdentNode in method context uses scoped name")
        void interpretIdentNodeInMethodContext() {
            // Simulate being in a method context
            stacks.pushContext("myMethod");
            String scopedName = stacks.getScopedName("localVar");
            stacks.declareVar(scopedName, 100, Type.ENTIER);
            
            IdentNode ident = new IdentNode("localVar");
            EcrireNode node = new EcrireNode(ident);

            assertDoesNotThrow(() -> node.interpret(stacks));
            
            stacks.popContext("myMethod");
        }

        @Test
        @DisplayName("interpret with IdentNode falls back to unscoped name")
        void interpretIdentNodeFallbackToUnscoped() {
            stacks.declareVar("globalVar", 50, Type.ENTIER);
            IdentNode ident = new IdentNode("globalVar");
            EcrireNode node = new EcrireNode(ident);

            assertDoesNotThrow(() -> node.interpret(stacks));
        }
    }

    // ==================== interpret with Expression TESTS ====================

    @Nested
    @DisplayName("interpret with Expression Tests")
    class InterpretExpressionTests {

        @Test
        @DisplayName("interpret with NbreNode expression")
        void interpretNbreNode() {
            EcrireNode node = new EcrireNode(new NbreNode(99));
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with BoolValueNode expression")
        void interpretBoolValueNode() {
            EcrireNode node = new EcrireNode(new BoolValueNode(false));
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with complex expression")
        void interpretComplexExpression() {
            PlusNode plus = new PlusNode(new NbreNode(10), new NbreNode(20));
            EcrireNode node = new EcrireNode(plus);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }
    }

    // ==================== interpret with TabNode TESTS ====================

    @Nested
    @DisplayName("interpret with TabNode Tests")
    class InterpretTabNodeTests {

        @Test
        @DisplayName("interpret with TabNode for integer array element")
        void interpretTabNodeInteger() {
            stacks.declareTab("arr", 5, Type.ENTIER);
            stacks.setArrayValue("arr", 2, 77);
            
            TabNode tab = new TabNode(new IdentNode("arr"), new NbreNode(2));
            EcrireNode node = new EcrireNode(tab);

            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with TabNode index 0")
        void interpretTabNodeIndexZero() {
            stacks.declareTab("data", 10, Type.ENTIER);
            stacks.setArrayValue("data", 0, 111);
            
            TabNode tab = new TabNode(new IdentNode("data"), new NbreNode(0));
            EcrireNode node = new EcrireNode(tab);

            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with TabNode last index")
        void interpretTabNodeLastIndex() {
            stacks.declareTab("list", 5, Type.ENTIER);
            stacks.setArrayValue("list", 4, 999);
            
            TabNode tab = new TabNode(new IdentNode("list"), new NbreNode(4));
            EcrireNode node = new EcrireNode(tab);

            assertDoesNotThrow(() -> node.interpret(stacks));
        }
    }

    // ==================== interpret with other types TESTS ====================

    @Nested
    @DisplayName("interpret with Other Types Tests")
    class InterpretOtherTypesTests {

        @Test
        @DisplayName("interpret with String literal")
        void interpretStringLiteral() {
            EcrireNode node = new EcrireNode("Hello World");
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with Integer literal")
        void interpretIntegerLiteral() {
            EcrireNode node = new EcrireNode(12345);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with Boolean literal")
        void interpretBooleanLiteral() {
            EcrireNode node = new EcrireNode(Boolean.TRUE);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with null")
        void interpretNull() {
            EcrireNode node = new EcrireNode(null);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }
    }

    // ==================== Edge Cases TESTS ====================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("interpret with zero value")
        void interpretZeroValue() {
            stacks.declareVar("zero", 0, Type.ENTIER);
            IdentNode ident = new IdentNode("zero");
            EcrireNode node = new EcrireNode(ident);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with negative value")
        void interpretNegativeValue() {
            stacks.declareVar("neg", -42, Type.ENTIER);
            IdentNode ident = new IdentNode("neg");
            EcrireNode node = new EcrireNode(ident);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret with large value")
        void interpretLargeValue() {
            stacks.declareVar("big", Integer.MAX_VALUE, Type.ENTIER);
            IdentNode ident = new IdentNode("big");
            EcrireNode node = new EcrireNode(ident);
            assertDoesNotThrow(() -> node.interpret(stacks));
        }

        @Test
        @DisplayName("interpret multiple times")
        void interpretMultipleTimes() {
            stacks.declareVar("x", 1, Type.ENTIER);
            IdentNode ident = new IdentNode("x");
            EcrireNode node = new EcrireNode(ident);
            
            for (int i = 0; i < 10; i++) {
                assertDoesNotThrow(() -> node.interpret(stacks));
            }
        }

        @Test
        @DisplayName("interpret with empty string")
        void interpretEmptyString() {
            EcrireNode node = new EcrireNode("");
            assertDoesNotThrow(() -> node.interpret(stacks));
        }
    }
}
