package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Unit tests for the Walker class.
 * Tests AST traversal with and without debugging.
 */
class WalkerTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ==================== MOCK AST NODE ====================

    /**
     * A simple mock AstNode that records when it's interpreted.
     */
    static class MockAstNode extends AstNode {
        private final String name;
        private final List<AstNode> children;
        private boolean interpreted = false;
        private static final List<String> interpretOrder = new ArrayList<>();

        public MockAstNode(String name) {
            this.name = name;
            this.children = new ArrayList<>();
        }

        public MockAstNode(String name, List<AstNode> children) {
            this.name = name;
            this.children = children;
        }

        public void addChild(AstNode child) {
            children.add(child);
        }

        @Override
        public Iterable<AstNode> getChildren() {
            return children;
        }

        @Override
        public void interpret(Stacks stacks) {
            interpreted = true;
            interpretOrder.add(name);
        }

        @Override
        public String toStringTree() {
            return "MockNode(" + name + ")";
        }

        public boolean wasInterpreted() {
            return interpreted;
        }

        public static List<String> getInterpretOrder() {
            return new ArrayList<>(interpretOrder);
        }

        public static void resetInterpretOrder() {
            interpretOrder.clear();
        }
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with null root")
        void constructorWithNullRoot() {
            Walker walker = new Walker(null, stacks);
            assertNotNull(walker);
        }

        @Test
        @DisplayName("Constructor with null debug creates default debug")
        void constructorWithNullDebug() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks, null, null);
            assertNotNull(walker.getDebug());
            assertFalse(walker.getDebug().isEnabled());
        }

        @Test
        @DisplayName("Constructor with debug")
        void constructorWithDebug() {
            MockAstNode root = new MockAstNode("root");
            Debug debug = new Debug(Debug.Mode.STEP_BY_STEP);
            Walker walker = new Walker(root, stacks, debug, null);
            assertEquals(debug, walker.getDebug());
        }

        @Test
        @DisplayName("Constructor without debug parameter")
        void constructorWithoutDebug() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            assertNotNull(walker.getDebug());
            assertFalse(walker.getDebug().isEnabled());
        }
    }

    // ==================== WALK TESTS ====================

    @Nested
    @DisplayName("Walk Tests")
    class WalkTests {

        @BeforeEach
        void resetOrder() {
            MockAstNode.resetInterpretOrder();
        }

        @Test
        @DisplayName("Walk with null root does nothing")
        void walkWithNullRoot() {
            Walker walker = new Walker(null, stacks);
            walker.walk(); // Should not throw
            assertEquals(0, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Walk single node")
        void walkSingleNode() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.walk();
            
            assertTrue(root.wasInterpreted());
            assertEquals(1, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Walk tree with children")
        void walkTreeWithChildren() {
            MockAstNode root = new MockAstNode("root");
            MockAstNode child1 = new MockAstNode("child1");
            MockAstNode child2 = new MockAstNode("child2");
            root.addChild(child1);
            root.addChild(child2);
            
            Walker walker = new Walker(root, stacks);
            walker.walk();
            
            assertTrue(root.wasInterpreted());
            assertTrue(child1.wasInterpreted());
            assertTrue(child2.wasInterpreted());
            assertEquals(3, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Walk tree in preorder")
        void walkTreeInPreorder() {
            MockAstNode root = new MockAstNode("root");
            MockAstNode child1 = new MockAstNode("child1");
            MockAstNode child2 = new MockAstNode("child2");
            MockAstNode grandchild = new MockAstNode("grandchild");
            
            root.addChild(child1);
            root.addChild(child2);
            child1.addChild(grandchild);
            
            Walker walker = new Walker(root, stacks);
            walker.walk();
            
            List<String> order = MockAstNode.getInterpretOrder();
            assertEquals(List.of("root", "child1", "grandchild", "child2"), order);
        }

        @Test
        @DisplayName("Walk deep tree")
        void walkDeepTree() {
            MockAstNode level0 = new MockAstNode("level0");
            MockAstNode level1 = new MockAstNode("level1");
            MockAstNode level2 = new MockAstNode("level2");
            MockAstNode level3 = new MockAstNode("level3");
            
            level0.addChild(level1);
            level1.addChild(level2);
            level2.addChild(level3);
            
            Walker walker = new Walker(level0, stacks);
            walker.walk();
            
            List<String> order = MockAstNode.getInterpretOrder();
            assertEquals(List.of("level0", "level1", "level2", "level3"), order);
            assertEquals(4, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Walk with empty children")
        void walkWithEmptyChildren() {
            MockAstNode root = new MockAstNode("root", new ArrayList<>());
            Walker walker = new Walker(root, stacks);
            
            walker.walk();
            
            assertTrue(root.wasInterpreted());
            assertEquals(1, walker.getCurrentLine());
        }
    }

    // ==================== DEBUG INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Debug Integration Tests")
    class DebugIntegrationTests {

        @BeforeEach
        void resetOrder() {
            MockAstNode.resetInterpretOrder();
        }

        @Test
        @DisplayName("Walk with disabled debug runs normally")
        void walkWithDisabledDebug() {
            MockAstNode root = new MockAstNode("root");
            MockAstNode child = new MockAstNode("child");
            root.addChild(child);
            
            Debug debug = new Debug(Debug.Mode.DISABLED);
            Walker walker = new Walker(root, stacks, debug, null);
            
            walker.walk();
            
            assertTrue(root.wasInterpreted());
            assertTrue(child.wasInterpreted());
        }

        @Test
        @DisplayName("getDebug returns the debug controller")
        void getDebugReturnsController() {
            Debug debug = new Debug(Debug.Mode.BREAKPOINTS);
            Walker walker = new Walker(null, stacks, debug, null);
            
            assertSame(debug, walker.getDebug());
        }

        @Test
        @DisplayName("getCurrentLine returns correct count")
        void getCurrentLineReturnsCorrectCount() {
            MockAstNode root = new MockAstNode("root");
            root.addChild(new MockAstNode("child1"));
            root.addChild(new MockAstNode("child2"));
            
            Walker walker = new Walker(root, stacks);
            assertEquals(0, walker.getCurrentLine());
            
            walker.walk();
            assertEquals(3, walker.getCurrentLine());
        }

        @Test
        @DisplayName("isStopped returns false after normal walk")
        void isStoppedReturnsFalseAfterNormalWalk() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.walk();
            
            assertFalse(walker.isStopped());
        }

        @Test
        @DisplayName("stop method stops execution")
        void stopMethodStopsExecution() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.stop();
            
            assertTrue(walker.isStopped());
        }

        @Test
        @DisplayName("Walk resets lineCounter")
        void walkResetsLineCounter() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.walk();
            assertEquals(1, walker.getCurrentLine());
            
            // Walk again
            walker.walk();
            assertEquals(1, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Walk resets stopped flag")
        void walkResetsStoppedFlag() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.stop();
            assertTrue(walker.isStopped());
            
            walker.walk();
            assertFalse(walker.isStopped());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @BeforeEach
        void resetOrder() {
            MockAstNode.resetInterpretOrder();
        }

        @Test
        @DisplayName("Walk tree with null child in list")
        void walkTreeWithNullChild() {
            MockAstNode root = new MockAstNode("root");
            root.addChild(null);
            root.addChild(new MockAstNode("validChild"));
            
            Walker walker = new Walker(root, stacks);
            walker.walk();
            
            // Should handle null child gracefully
            List<String> order = MockAstNode.getInterpretOrder();
            assertEquals(List.of("root", "validChild"), order);
        }

        @Test
        @DisplayName("Walk wide tree with many children")
        void walkWideTree() {
            MockAstNode root = new MockAstNode("root");
            for (int i = 0; i < 10; i++) {
                root.addChild(new MockAstNode("child" + i));
            }
            
            Walker walker = new Walker(root, stacks);
            walker.walk();
            
            assertEquals(11, walker.getCurrentLine());
        }

        @Test
        @DisplayName("Multiple walks on same walker")
        void multipleWalks() {
            MockAstNode root = new MockAstNode("root");
            Walker walker = new Walker(root, stacks);
            
            walker.walk();
            walker.walk();
            walker.walk();
            
            // Each walk should reset and work correctly
            assertEquals(1, walker.getCurrentLine());
        }
    }
}
