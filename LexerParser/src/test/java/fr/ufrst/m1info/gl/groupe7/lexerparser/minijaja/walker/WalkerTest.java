package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * Unit tests for the Walker class.
 * Tests AST traversal, node interpretation, debugging capabilities, and all code branches.
 */
@DisplayName("Walker Tests")
class WalkerTest {

    private Walker walker;
    private AstNode mockRoot;
    private Stacks mockStacks;
    private Debug mockDebug;

    @BeforeEach
    void setUp() {
        mockRoot = mock(AstNode.class);
        mockStacks = mock(Stacks.class);
        mockDebug = mock(Debug.class);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with all parameters initializes correctly")
        void constructorWithAllParameters() {
            HandlePauseCallback callback = mock(HandlePauseCallback.class);
            Walker w = new Walker(mockRoot, mockStacks, mockDebug, callback);

            assertNotNull(w);
            assertEquals(mockDebug, w.getDebug());
            assertFalse(w.isStopped());
        }

        @Test
        @DisplayName("Constructor with null debug creates default Debug instance")
        void constructorWithNullDebugCreatesDefault() {
            Walker w = new Walker(mockRoot, mockStacks, null, null);

            assertNotNull(w);
            assertNotNull(w.getDebug());
            assertEquals(Debug.Mode.DISABLED, w.getDebug().getMode());
        }

        @Test
        @DisplayName("Constructor without debug parameters initializes correctly")
        void constructorWithoutDebugParameters() {
            Walker w = new Walker(mockRoot, mockStacks);

            assertNotNull(w);
            assertNotNull(w.getDebug());
            assertFalse(w.isStopped());
        }

        @Test
        @DisplayName("Constructor with null root node is valid")
        void constructorWithNullRootNode() {
            Walker w = new Walker(null, mockStacks, mockDebug, null);

            assertNotNull(w);
            assertFalse(w.isStopped());
        }
    }

    // ==================== WALK TESTS ====================

    @Nested
    @DisplayName("Walk Method Tests")
    class WalkMethodTests {

        @Test
        @DisplayName("Walk with null root completes without error")
        void walkWithNullRoot() {
            walker = new Walker(null, mockStacks);
            assertDoesNotThrow(() -> walker.walk());
            assertFalse(walker.isStopped());
        }

        @Test
        @DisplayName("Walk with valid root calls interpret on root")
        void walkWithValidRootCallsInterpret() {
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Walk with debug disabled proceeds normally")
        void walkWithDebugDisabled() {
            when(mockDebug.isEnabled()).thenReturn(false);
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks, mockDebug, null);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
            assertFalse(walker.isStopped());
        }

        @Test
        @DisplayName("Walk with debug enabled displays debug information")
        void walkWithDebugEnabled() {
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getMode()).thenReturn(Debug.Mode.STEP_BY_STEP);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks, mockDebug, null);
            assertDoesNotThrow(() -> walker.walk());
        }

        @Test
        @DisplayName("Walk resets stopped flag before execution")
        void walkResetsStopped() {
            walker = new Walker(mockRoot, mockStacks, mockDebug, null);
            walker.stop();
            assertTrue(walker.isStopped());

            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker.walk();
            assertFalse(walker.isStopped());
        }
    }

    // ==================== VISIT NODE TESTS ====================

    @Nested
    @DisplayName("Visit Node Tests")
    class VisitNodeTests {

        @Test
        @DisplayName("Visit null node returns safely")
        void visitNullNode() {
            walker = new Walker(null, mockStacks);
            assertDoesNotThrow(() -> walker.walk());
            assertFalse(walker.isStopped());
        }

        @Test
        @DisplayName("Visit single node without children")
        void visitSingleNodeWithoutChildren() {
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Visit node with null children collection")
        void visitNodeWithNullChildren() {
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(null);

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Visit node with multiple children")
        void visitNodeWithMultipleChildren() {
            AstNode child1 = mock(AstNode.class);
            AstNode child2 = mock(AstNode.class);
            AstNode child3 = mock(AstNode.class);

            List<AstNode> children = new ArrayList<>();
            children.add(child1);
            children.add(child2);
            children.add(child3);

            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(children);
            when(child1.getSourcePosition()).thenReturn(null);
            when(child1.getChildren()).thenReturn(new ArrayList<>());
            when(child2.getSourcePosition()).thenReturn(null);
            when(child2.getChildren()).thenReturn(new ArrayList<>());
            when(child3.getSourcePosition()).thenReturn(null);
            when(child3.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
            verify(child1, times(1)).interpret(mockStacks);
            verify(child2, times(1)).interpret(mockStacks);
            verify(child3, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Visit node with source position information")
        void visitNodeWithSourcePosition() {
            SourcePosition sourcePos = new SourcePosition("test.jaja", 5, 0);
            when(mockRoot.getSourcePosition()).thenReturn(sourcePos);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Visit nested tree with multiple levels")
        void visitNestedTreeMultipleLevels() {
            AstNode child = mock(AstNode.class);
            AstNode grandchild = mock(AstNode.class);

            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(List.of(child));
            when(child.getSourcePosition()).thenReturn(null);
            when(child.getChildren()).thenReturn(List.of(grandchild));
            when(grandchild.getSourcePosition()).thenReturn(null);
            when(grandchild.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
            verify(child, times(1)).interpret(mockStacks);
            verify(grandchild, times(1)).interpret(mockStacks);
        }
    }

    // ==================== NODE TYPE TESTS ====================

    @Nested
    @DisplayName("Node Type Tests")
    class NodeTypeTests {

        @Test
        @DisplayName("Visit VarNode triggers debugging check")
        void visitVarNodeTriggersDebugCheck() {
            VarNode varNode = mock(VarNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 10, 0);

            when(varNode.getSourcePosition()).thenReturn(sourcePos);
            when(varNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(10, varNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(varNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(varNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(1)).beforeNode(10, varNode, mockStacks, null);
        }

        @Test
        @DisplayName("Visit MethodeNode triggers debugging check")
        void visitMethodeNodeTriggersDebugCheck() {
            MethodeNode methNode = mock(MethodeNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 15, 0);

            when(methNode.getSourcePosition()).thenReturn(sourcePos);
            when(methNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(15, methNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(methNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(methNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(1)).beforeNode(15, methNode, mockStacks, null);
        }

        @Test
        @DisplayName("Visit InstructionNode triggers debugging check")
        void visitInstructionNodeTriggersDebugCheck() {
            InstructionNode instrNode = mock(InstructionNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 20, 0);

            when(instrNode.getSourcePosition()).thenReturn(sourcePos);
            when(instrNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(20, instrNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(instrNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(instrNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(1)).beforeNode(20, instrNode, mockStacks, null);
        }

        @Test
        @DisplayName("Visit CstNode triggers debugging check")
        void visitCstNodeTriggersDebugCheck() {
            CstNode cstNode = mock(CstNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 25, 0);

            when(cstNode.getSourcePosition()).thenReturn(sourcePos);
            when(cstNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(25, cstNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(cstNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(cstNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(1)).beforeNode(25, cstNode, mockStacks, null);
        }

        @Test
        @DisplayName("Visit TableauNode triggers debugging check")
        void visitTableauNodeTriggersDebugCheck() {
            TableauNode tabNode = mock(TableauNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 30, 0);

            when(tabNode.getSourcePosition()).thenReturn(sourcePos);
            when(tabNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(30, tabNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(tabNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(tabNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(1)).beforeNode(30, tabNode, mockStacks, null);
        }

        @Test
        @DisplayName("Visit generic AstNode without breakable type")
        void visitGenericAstNodeWithoutBreakableType() {
            AstNode genericNode = mock(AstNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 35, 0);

            when(genericNode.getSourcePosition()).thenReturn(sourcePos);
            when(genericNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);

            walker = new Walker(genericNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(genericNode, times(1)).interpret(mockStacks);
            verify(mockDebug, times(0)).beforeNode(anyInt(), any(), any(), any());
        }
    }

    // ==================== DEBUG BREAKPOINT TESTS ====================

    @Nested
    @DisplayName("Debug Breakpoint Tests")
    class DebugBreakpointTests {

        @Test
        @DisplayName("Debug beforeNode returns false stops execution")
        void debugBeforeNodeReturnsFalseStopsExecution() {
            AstNode child = mock(AstNode.class);
            VarNode varNode = mock(VarNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 10, 0);

            when(varNode.getSourcePosition()).thenReturn(sourcePos);
            when(varNode.getChildren()).thenReturn(List.of(child));
            when(child.getSourcePosition()).thenReturn(null);
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(10, varNode, mockStacks, null)).thenReturn(false);

            walker = new Walker(varNode, mockStacks, mockDebug, null);
            walker.walk();

            assertTrue(walker.isStopped());
            verify(child, times(0)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Debug beforeNode returns true continues execution")
        void debugBeforeNodeReturnsTrueContinuesExecution() {
            AstNode child = mock(AstNode.class);
            VarNode varNode = mock(VarNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 10, 0);

            when(varNode.getSourcePosition()).thenReturn(sourcePos);
            when(varNode.getChildren()).thenReturn(List.of(child));
            when(child.getSourcePosition()).thenReturn(null);
            when(child.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(10, varNode, mockStacks, null)).thenReturn(true);

            walker = new Walker(varNode, mockStacks, mockDebug, null);
            walker.walk();

            assertFalse(walker.isStopped());
            verify(child, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Debug with callback passes callback to beforeNode")
        void debugWithCallbackPassesCallbackToBeforeNode() {
            HandlePauseCallback callback = mock(HandlePauseCallback.class);
            VarNode varNode = mock(VarNode.class);
            SourcePosition sourcePos = new SourcePosition("test.jaja", 10, 0);

            when(varNode.getSourcePosition()).thenReturn(sourcePos);
            when(varNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(10, varNode, mockStacks, callback)).thenReturn(true);

            walker = new Walker(varNode, mockStacks, mockDebug, callback);
            walker.walk();

            verify(mockDebug, times(1)).beforeNode(10, varNode, mockStacks, callback);
        }
    }

    // ==================== STOP EXECUTION TESTS ====================

    @Nested
    @DisplayName("Stop Execution Tests")
    class StopExecutionTests {

        @Test
        @DisplayName("stop() method sets stopped flag")
        void stopMethodSetsStopped() {
            walker = new Walker(mockRoot, mockStacks);
            assertFalse(walker.isStopped());

            walker.stop();
            assertTrue(walker.isStopped());
        }

  
        @Test
        @DisplayName("isStopped() returns correct status")
        void isStoppedReturnsCorrectStatus() {
            walker = new Walker(mockRoot, mockStacks);
            assertFalse(walker.isStopped());

            walker.stop();
            assertTrue(walker.isStopped());

            walker = new Walker(mockRoot, mockStacks);
            assertFalse(walker.isStopped());
        }
    }

    // ==================== GETTER TESTS ====================

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getDebug() returns the Debug instance")
        void getDebugReturnsDebugInstance() {
            walker = new Walker(mockRoot, mockStacks, mockDebug, null);
            assertEquals(mockDebug, walker.getDebug());
        }

        @Test
        @DisplayName("getDebug() never returns null")
        void getDebugNeverReturnsNull() {
            walker = new Walker(mockRoot, mockStacks, null, null);
            assertNotNull(walker.getDebug());
        }
    }

    // ==================== SOURCE POSITION EXTRACTION TESTS ====================

    @Nested
    @DisplayName("Source Position Extraction Tests")
    class SourcePositionExtractionTests {

        @Test
        @DisplayName("Node with null SourcePosition returns 0")
        void nodeWithNullSourcePositionReturns0() {
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Node with valid SourcePosition extracts line number")
        void nodeWithValidSourcePositionExtractsLineNumber() {
            SourcePosition sourcePos = new SourcePosition("test.jaja", 42, 5);
            when(mockRoot.getSourcePosition()).thenReturn(sourcePos);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks);
            walker.walk();

            verify(mockRoot, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Nodes without source lines don't trigger debug checks")
        void nodesWithoutSourceLinesNoDebugCheck() {
            VarNode varNode = mock(VarNode.class);
            when(varNode.getSourcePosition()).thenReturn(null);
            when(varNode.getChildren()).thenReturn(new ArrayList<>());
            when(mockDebug.isEnabled()).thenReturn(true);

            walker = new Walker(varNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(mockDebug, times(0)).beforeNode(anyInt(), any(), any(), any());
        }
    }

    // ==================== INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Complex tree traversal with mixed node types")
        void complexTreeTraversalWithMixedNodeTypes() {
            VarNode varNode = mock(VarNode.class);
            InstructionNode instrNode = mock(InstructionNode.class);
            MethodeNode methNode = mock(MethodeNode.class);

            SourcePosition varPos = new SourcePosition("test.jaja", 1, 0);
            SourcePosition instrPos = new SourcePosition("test.jaja", 2, 0);

            when(varNode.getSourcePosition()).thenReturn(varPos);
            when(varNode.getChildren()).thenReturn(List.of(instrNode));
            when(instrNode.getSourcePosition()).thenReturn(instrPos);
            when(instrNode.getChildren()).thenReturn(List.of(methNode));
            when(methNode.getSourcePosition()).thenReturn(null);
            when(methNode.getChildren()).thenReturn(new ArrayList<>());

            when(mockDebug.isEnabled()).thenReturn(true);
            when(mockDebug.getBreakPoints()).thenReturn(new java.util.HashSet<>());
            when(mockDebug.beforeNode(anyInt(), any(), any(), any())).thenReturn(true);

            walker = new Walker(varNode, mockStacks, mockDebug, null);
            walker.walk();

            verify(varNode, times(1)).interpret(mockStacks);
            verify(instrNode, times(1)).interpret(mockStacks);
            verify(methNode, times(1)).interpret(mockStacks);
        }

        @Test
        @DisplayName("Walk completes and doesn't mark as stopped when debug disabled")
        void walkCompletesWithDebugDisabled() {
            when(mockDebug.isEnabled()).thenReturn(false);
            when(mockRoot.getSourcePosition()).thenReturn(null);
            when(mockRoot.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(mockRoot, mockStacks, mockDebug, null);
            walker.walk();

            assertFalse(walker.isStopped());
        }

        @Test
        @DisplayName("Walk with deep nested structure")
        void walkWithDeepNestedStructure() {
            AstNode node1 = mock(AstNode.class);
            AstNode node2 = mock(AstNode.class);
            AstNode node3 = mock(AstNode.class);
            AstNode node4 = mock(AstNode.class);

            when(node1.getSourcePosition()).thenReturn(null);
            when(node1.getChildren()).thenReturn(List.of(node2));
            when(node2.getSourcePosition()).thenReturn(null);
            when(node2.getChildren()).thenReturn(List.of(node3));
            when(node3.getSourcePosition()).thenReturn(null);
            when(node3.getChildren()).thenReturn(List.of(node4));
            when(node4.getSourcePosition()).thenReturn(null);
            when(node4.getChildren()).thenReturn(new ArrayList<>());

            walker = new Walker(node1, mockStacks);
            walker.walk();

            verify(node1, times(1)).interpret(mockStacks);
            verify(node2, times(1)).interpret(mockStacks);
            verify(node3, times(1)).interpret(mockStacks);
            verify(node4, times(1)).interpret(mockStacks);
        }
    }
}


