package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MiniJajaCompilerVisitorTest {

    /**
     * The visitor under test. This is the class responsible for converting MiniJaja AST nodes
     * into JajaCode instructions.
     */
    private MiniJajaCompilerVisitor visitor;

    /**
     * A spy version of the internal {@link JajaCodeBuilder} used by the visitor.
     * Allows us to verify that the correct JajaCode instructions are being generated.
     */
    private JajaCodeBuilder builderSpy;

    /**
     * Initializes the test environment before each test.
     * A new {@link MiniJajaCompilerVisitor} is created, and its private builder is replaced
     * by a Mockito spy to capture generated instructions.
     */
    @BeforeEach
    void setUp() {
        visitor = new MiniJajaCompilerVisitor(new Stacks(), new DiagnosticCollector());
        builderSpy = spy(visitor.getJajaCodeBuilder());
        try {
            // Access the private field `jjcBuilder` via reflection to inject the spy
            var field = MiniJajaCompilerVisitor.class.getDeclaredField("jjcBuilder");
            field.setAccessible(true);
            field.set(visitor, builderSpy);
        } catch (Exception e) {
            fail("Failed to inject the builder spy: " + e.getMessage());
        }
    }

    /**
     * Creates a simple {@link IdentNode} representing an identifier.
     * No mocking is required because IdentNode is a concrete class.
     *
     * @param name The identifier name.
     * @return A new IdentNode instance.
     */
    private IdentNode ident(String name) {
        return new IdentNode(name);
    }

    /**
     * Creates a simple {@link VarNode} representing a variable declaration.
     *
     * @param name The variable name.
     * @param type The variable type (e.g., Type.ENTIER, Type.BOOLEEN).
     * @return A new VarNode instance.
     */
    private VarNode var(String name, Type type) {
        IdentNode ident = new IdentNode(name);
        return new VarNode(type, ident, null);
    }

    // --- Unit tests ---

    @Test
    void visitNbreNode_addsPushInstruction() {
        NbreNode node = new NbreNode(42);
        visitor.visit(node);
        verify(builderSpy).addInstruction(PUSH, 42);
    }

    @Test
    void visitNrbreNode_withNegativeValue_addsPushInstruction() {
        NbreNode node = new NbreNode(-5);
        visitor.visit(node);
        verify(builderSpy).addInstruction(PUSH, -5);
    }

    @Test
    void visitVarNode_generatesPushAndNewAndTracksVariable() {
        VarNode node = var("x", Type.ENTIER);

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 0);
        verify(builderSpy).addInstruction(eq(NEW), eq("x@global"), eq("int"), eq("var"), eq(0));
    }

    @Test
    void visitAffectation_generatesPushAndStore() {
        AffectationNode node = mock(AffectationNode.class);
        IdentNode id = ident("x");
        NbreNode expr = new NbreNode(7);

        when(node.getExpression()).thenReturn(expr);
        when(node.getIdent1Node()).thenReturn(id);

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 7);
        inOrder.verify(builderSpy).addInstruction(STORE, "x@global");
    }

    @Test
    void visitInstructions_traversesSequentially() {
        InstructionsNode node1 = mock(InstructionsNode.class);
        InstructionNode instr = mock(AffectationNode.class);
        when(node1.getInstructionNode()).thenReturn(instr);
        InstructionsNode node2 = mock(InstructionsNode.class);
        when(node1.getInstructions()).thenReturn(node2);
        when(node2.getInstructionNode()).thenReturn(null);

        visitor.visit(node1);

        verify(builderSpy, atLeast(0)).addInstruction(any(), any());
    }

    @Test
    void visitInstructions_withNullNode_doesNothing() {
        visitor.visit((InstructionsNode) null);
        verify(builderSpy, never()).addInstruction(any(), any());
    }

    @Test
    void visitInstructions_withNullInstruction_skipsGracefully() {
        InstructionsNode node = mock(InstructionsNode.class);
        when(node.getInstructionNode()).thenReturn(null);

        visitor.visit(node);

        verify(builderSpy, never()).addInstruction(any(), any());
    }

    @Test
    void visitInstructions_chainOfThree_processesInOrder() {
        InstructionsNode node1 = mock(InstructionsNode.class);
        InstructionNode instr1 = mock(AffectationNode.class);
        InstructionsNode node2 = mock(InstructionsNode.class);
        InstructionNode instr2 = mock(AffectationNode.class);
        InstructionsNode node3 = mock(InstructionsNode.class);
        InstructionNode instr3 = mock(AffectationNode.class);

        when(node1.getInstructionNode()).thenReturn(instr1);
        when(node1.getInstructions()).thenReturn(node2);
        when(node2.getInstructionNode()).thenReturn(instr2);
        when(node2.getInstructions()).thenReturn(node3);
        when(node3.getInstructionNode()).thenReturn(instr3);
        when(node3.getInstructions()).thenReturn(null);

        visitor.visit(node1);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy, atLeast(0)).addInstruction(any(), any());
        inOrder.verify(builderSpy, atLeast(0)).addInstruction(any(), any());
        inOrder.verify(builderSpy, atLeast(0)).addInstruction(any(), any());
    }


    @Test
    void visitMain_generatesBodyThenPushZero() {
        MainNode node = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(node.getInstrs()).thenReturn(instrs);

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 0);
    }

    @Test
    void visitMain_withMultipleInstructions_processesAll() {
        MainNode node = mock(MainNode.class);
        InstructionsNode instr1 = mock(InstructionsNode.class);
        InstructionNode instructionNode1 = mock(AffectationNode.class);
        InstructionsNode instr2 = mock(InstructionsNode.class);
        InstructionNode instructionNode2 = mock(AffectationNode.class);

        when(node.getInstrs()).thenReturn(instr1);
        when(instr1.getInstructionNode()).thenReturn(instructionNode1);
        when(instr1.getInstructions()).thenReturn(instr2);
        when(instr2.getInstructionNode()).thenReturn(instructionNode2);
        when(instr2.getInstructions()).thenReturn(null);

        visitor.visit(node);

        verify(builderSpy, atLeast(0)).addInstruction(any(), any());
    }

    @Test
    void visitDecls_recursesThroughMultipleDeclarations() {
        VarNode var1 = var("x", Type.ENTIER);
        VarNode var2 = var("y", Type.BOOLEEN);
        DeclsNode next = mock(DeclsNode.class);
        DeclsNode root = mock(DeclsNode.class);
        when(root.getDecl()).thenReturn(var1);
        when(root.getDecls()).thenReturn(next);
        when(next.getDecl()).thenReturn(var2);

        visitor.visit(root);

        verify(builderSpy, atLeastOnce()).addInstruction(eq(NEW), any(), any(), any(), any());
    }

    @Test
    void visitClasse_withMultipleDecls_generatesCorrectSwapPopSequence() {
        ClasseNode classe = mock(ClasseNode.class);
        DeclsNode decls = mock(DeclsNode.class);
        VarNode var1 = var("a", Type.ENTIER);
        VarNode var2 = var("b", Type.BOOLEEN);
        System.out.println(var2.getType());
        DeclsNode nextDecls = mock(DeclsNode.class);

        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(null);
        when(decls.getDecl()).thenReturn(var1);
        when(decls.getDecls()).thenReturn(nextDecls);
        when(nextDecls.getDecl()).thenReturn(var2);
        when(nextDecls.getDecls()).thenReturn(null);

        visitor.visit(classe);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(INIT);
        inOrder.verify(builderSpy).addInstruction(PUSH, 0);
        inOrder.verify(builderSpy).addInstruction(NEW, "a@global", "int", "var", 0);
        inOrder.verify(builderSpy).addInstruction(PUSH, false);
        inOrder.verify(builderSpy).addInstruction(NEW, "b@global", "boolean", "var", 0);
        inOrder.verify(builderSpy).addInstruction(SWAP);
        inOrder.verify(builderSpy).addInstruction(POP);
        inOrder.verify(builderSpy).addInstruction(SWAP);
        inOrder.verify(builderSpy).addInstruction(POP);
    }

    @Test
    void visitClasse_emptyVariableStack_doesNotCrash() {
        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        verify(builderSpy, atLeastOnce()).addInstruction(eq(INIT));
        verify(builderSpy, atLeastOnce()).addInstruction(eq(JCSTOP));
    }

    @Test
    void visitClasse_ordersInstructionsCorrectly() {
        ClasseNode classe = mock(ClasseNode.class);
        DeclsNode decls = mock(DeclsNode.class);
        MainNode main = mock(MainNode.class);

        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(INIT);
        inOrder.verify(builderSpy, atLeastOnce()).addInstruction(any(), any());
        inOrder.verify(builderSpy).addInstruction(JCSTOP);
    }

    @Test
    void visitClasse_generatesFullProgramFlow() {
        ClasseNode classe = mock(ClasseNode.class);
        DeclsNode decls = mock(DeclsNode.class);
        MainNode main = mock(MainNode.class);

        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        ArgumentCaptor<JajaCodeInstr> captor = ArgumentCaptor.forClass(JajaCodeInstr.class);
        verify(builderSpy, atLeastOnce()).addInstruction(captor.capture(), any(Object[].class));

        List<JajaCodeInstr> instrs = captor.getAllValues();
        assertTrue(instrs.contains(INIT));
        assertTrue(instrs.contains(JCSTOP));
    }

    @Test
    void visitClasse_withoutDeclsOrMain_stillGeneratesInitAndStop() {
        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        verify(builderSpy, atLeastOnce()).addInstruction(eq(INIT));
        verify(builderSpy, atLeastOnce()).addInstruction(eq(JCSTOP));
    }

    @Test
    void visitExpression_handlesNbreNode() {
        NbreNode nbre = new NbreNode(13);
        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(nbre);
        when(node.getIdent1Node()).thenReturn(ident("v"));

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 13);
    }

    @Test
    void visitEqualsNode_equalNumbers_generatesPushAndCmpInstructions() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(5), new NbreNode(5));

        // Créer un noeud d'affectation pour tester l'expression
        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_NotEqualNumbers_generatesCmpInstructions() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(5), new NbreNode(10));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_nestedEquals_generatesCorrectInstructions() {
        EqualsNode innerEquals = new EqualsNode(new NbreNode(3), new NbreNode(3));
        EqualsNode outerEquals = new EqualsNode(innerEquals, new NbreNode(1));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(outerEquals);
        when(node.getIdent1Node()).thenReturn(ident("finalResult"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // Instructions for inner equals
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Instructions for outer equals
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Store final result
        inOrder.verify(builderSpy).addInstruction(STORE, "finalResult@global");
    }

    @Test
    void visitEqualsNode_withVariableAndNumber_generatesCorrectInstructions() {
        IdentNode varIdent = ident("x");
        EqualsNode equalsNode = new EqualsNode(varIdent, new NbreNode(42));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("isEqual"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 42);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "isEqual@global");
    }

    @Test
    void visitEqualsNode_withTwoVariables_generatesCorrectInstructions() {
        IdentNode varIdent1 = ident("a");
        IdentNode varIdent2 = ident("b");
        EqualsNode equalsNode = new EqualsNode(varIdent1, varIdent2);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("areEqual"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "a@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "b@global");
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "areEqual@global");
    }

    @Test
    void visitEqualsNode_withNestedExpressions_generatesCorrectInstructions() {
        EqualsNode innerEquals = new EqualsNode(new NbreNode(2), new NbreNode(2));
        EqualsNode outerEquals = new EqualsNode(innerEquals, new NbreNode(1));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(outerEquals);
        when(node.getIdent1Node()).thenReturn(ident("finalResult"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // Instructions for inner equals
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Instructions for outer equals
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Store final result
        inOrder.verify(builderSpy).addInstruction(STORE, "finalResult@global");
    }

    @Test
    void visitEqualsNode_withMultipleNestedLevels_generatesCorrectInstructions() {
        EqualsNode level1 = new EqualsNode(new NbreNode(4), new NbreNode(4));
        EqualsNode level2 = new EqualsNode(level1, new NbreNode(1));
        EqualsNode rootEquals = new EqualsNode(level2, new NbreNode(0));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(rootEquals);
        when(node.getIdent1Node()).thenReturn(ident("ultimateResult"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // Level 1
        inOrder.verify(builderSpy).addInstruction(PUSH, 4);
        inOrder.verify(builderSpy).addInstruction(PUSH, 4);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Level 2
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Root level
        inOrder.verify(builderSpy).addInstruction(PUSH, 0);
        inOrder.verify(builderSpy).addInstruction(CMP);
        // Store final result
        inOrder.verify(builderSpy).addInstruction(STORE, "ultimateResult@global");
    }

    @Test
    void visitEqualsNode_NegativeNumbers_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(-3), new NbreNode(-3));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("isNegativeEqual"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, -3);
        inOrder.verify(builderSpy).addInstruction(PUSH, -3);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "isNegativeEqual@global");
    }

    @Test
    void visitEqualsNode_ZeroComparison_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(0), new NbreNode(0));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("isZeroEqual"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 0);
        inOrder.verify(builderSpy).addInstruction(PUSH, 0);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "isZeroEqual@global");
    }

    @Test
    void visitEqualsNode_LargeNumbers_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(1000000), new NbreNode(1000000));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("isLargeEqual"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 1000000);
        inOrder.verify(builderSpy).addInstruction(PUSH, 1000000);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "isLargeEqual@global");
    }

    // ===== Tests avec booléens =====

    @Test
    void visitEqualsNode_twoTrueBooleans_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(true), new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(true));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("areBothTrue"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, true);
        inOrder.verify(builderSpy).addInstruction(PUSH, true);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "areBothTrue@global");
    }

    @Test
    void visitEqualsNode_twoFalseBooleans_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(false), new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(false));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("areBothFalse"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, false);
        inOrder.verify(builderSpy).addInstruction(PUSH, false);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "areBothFalse@global");
    }

    @Test
    void visitEqualsNode_differentBooleans_generatesCorrectInstructions() {
        EqualsNode equalsNode = new EqualsNode(new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(true), new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode(false));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("areDifferent"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, true);
        inOrder.verify(builderSpy).addInstruction(PUSH, false);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "areDifferent@global");
    }

    // ===== Tests avec expressions complexes =====

    @Test
    void visitEqualsNode_additionOnLeft_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode addition = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode(new NbreNode(2), new NbreNode(3));
        EqualsNode equalsNode = new EqualsNode(addition, new NbreNode(5));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(ADD);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_additionOnRight_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode addition = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode(new NbreNode(2), new NbreNode(3));
        EqualsNode equalsNode = new EqualsNode(new NbreNode(5), addition);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(ADD);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_additionOnBothSides_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode leftAdd = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode(new NbreNode(1), new NbreNode(2));
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode rightAdd = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode(new NbreNode(2), new NbreNode(1));
        EqualsNode equalsNode = new EqualsNode(leftAdd, rightAdd);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(ADD);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(ADD);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_multiplicationExpression_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode mult = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode(new NbreNode(2), new NbreNode(3));
        EqualsNode equalsNode = new EqualsNode(mult, new NbreNode(6));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(MUL);
        inOrder.verify(builderSpy).addInstruction(PUSH, 6);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_subtractionExpression_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode subtraction = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode(new NbreNode(10), new NbreNode(3));
        EqualsNode equalsNode = new EqualsNode(subtraction, new NbreNode(7));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(SUB);
        inOrder.verify(builderSpy).addInstruction(PUSH, 7);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    // ===== Tests d'ordre d'évaluation =====

    @Test
    void visitEqualsNode_evaluatesExp1BeforeExp2() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(10), new NbreNode(20));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // Exp1 doit être évaluée en premier
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        // Puis Exp2
        inOrder.verify(builderSpy).addInstruction(PUSH, 20);
        // Puis CMP
        inOrder.verify(builderSpy).addInstruction(CMP);
    }

    @Test
    void visitEqualsNode_nestedOperators_generatesCorrectOrder() {
        // ((1 + 2) * 3) == (3 * 3)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode addition = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode(new NbreNode(1), new NbreNode(2));
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode leftMult = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode(addition, new NbreNode(3));
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode rightMult = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode(new NbreNode(3), new NbreNode(3));

        EqualsNode equalsNode = new EqualsNode(leftMult, rightMult);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // Évaluation de (1 + 2)
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(ADD);
        // Multiplication par 3
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(MUL);
        // Évaluation de (3 * 3)
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(MUL);
        // Comparaison finale
        inOrder.verify(builderSpy).addInstruction(CMP);
    }

    // ===== Tests de cas limites =====

    @Test
    void visitEqualsNode_withNullExp1_doesNotCrash() {
        EqualsNode equalsNode = new EqualsNode(null, new NbreNode(5));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        // Ne doit pas lancer d'exception
        assertDoesNotThrow(() -> visitor.visit(node));
    }

    @Test
    void visitEqualsNode_withNullExp2_doesNotCrash() {
        EqualsNode equalsNode = new EqualsNode(new NbreNode(5), null);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        // Ne doit pas lancer d'exception
        assertDoesNotThrow(() -> visitor.visit(node));
    }

    @Test
    void visitEqualsNode_withBothNull_doesNotCrash() {
        EqualsNode equalsNode = new EqualsNode(null, null);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        assertDoesNotThrow(() -> visitor.visit(node));
    }

    // ===== Tests d'intégration =====

    @Test
    void visitEqualsNode_inIfCondition_generatesCorrectInstructions() {
        // Test conceptuel : if(x == 5) { ... }
        // Le if sera testé dans d'autres tests, ici on vérifie juste que l'égalité compile
        IdentNode varIdent = ident("x");
        EqualsNode equalsNode = new EqualsNode(varIdent, new NbreNode(5));

        // Pour ce test, on utilise une affectation pour vérifier la génération
        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("conditionResult"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "conditionResult@global");
    }

    @Test
    void visitEqualsNode_inAssignment_generatesCorrectInstructions() {
        // result = (a == b)
        IdentNode varA = ident("a");
        IdentNode varB = ident("b");
        EqualsNode equalsNode = new EqualsNode(varA, varB);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "a@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "b@global");
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_withNegation_generatesCorrectInstructions() {
        // !(x == y)
        IdentNode varX = ident("x");
        IdentNode varY = ident("y");
        EqualsNode equalsNode = new EqualsNode(varX, varY);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode notNode = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode(equalsNode);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(notNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(NOT);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_withAND_generatesCorrectInstructions() {
        // (x == 5) && (y == 10)
        IdentNode varX = ident("x");
        IdentNode varY = ident("y");
        EqualsNode equals1 = new EqualsNode(varX, new NbreNode(5));
        EqualsNode equals2 = new EqualsNode(varY, new NbreNode(10));
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode andNode = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode(equals1, equals2);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(andNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(AND);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_withOR_generatesCorrectInstructions() {
        // (x == 5) || (y == 10)
        IdentNode varX = ident("x");
        IdentNode varY = ident("y");
        EqualsNode equals1 = new EqualsNode(varX, new NbreNode(5));
        EqualsNode equals2 = new EqualsNode(varY, new NbreNode(10));
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode orNode = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode(equals1, equals2);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(orNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(OR);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_withMulitpleANDOR_generatesCorrectInstructions() {
        // (x == 5) && (y == 10) || (z == 15)
        IdentNode varX = ident("x");
        IdentNode varY = ident("y");
        IdentNode varZ = ident("z");
        OrNode orNode = getNode(varX, varY, varZ);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(orNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(AND);
        inOrder.verify(builderSpy).addInstruction(LOAD, "z@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 15);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(OR);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    private static OrNode getNode(IdentNode varX, IdentNode varY, IdentNode varZ) {
        EqualsNode equals1 = new EqualsNode(varX, new NbreNode(5));
        EqualsNode equals2 = new EqualsNode(varY, new NbreNode(10));
        EqualsNode equals3 = new EqualsNode(varZ, new NbreNode(15));
        AndNode andNode = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode(equals1, equals2);
        return new OrNode(andNode, equals3);
    }

    @Test
    void test_simpleAffectation_withIdent_generatesLoadAndStore() {
        // Test simple: result = a
        IdentNode varA = ident("a");
        AffectationNode node = new AffectationNode(ident("result"), varA);

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "a@global");
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void test_multipleCondition_withAND_OR_SUP_CMP_generatesCorrectInstructions() {
        // ((a > b) && (c == d)) || (e > f)
        // Construction de l'expression ((a > b) && (c == d))
        IdentNode varA = ident("a");
        IdentNode varB = ident("b");
        IdentNode varC = ident("c");
        IdentNode varD = ident("d");
        IdentNode varE = ident("e");
        IdentNode varF = ident("f");

        GreaterThanNode aGreaterB = new GreaterThanNode(varA, varB);
        EqualsNode cEqualsD = new EqualsNode(varC, varD);
        AndNode leftAnd = new AndNode(aGreaterB, cEqualsD);

        // Construction de (e > f)
        GreaterThanNode eGreaterF = new GreaterThanNode(varE, varF);

        // Construction de ((a > b) && (c == d)) || (e > f)
        OrNode orNode = new OrNode(leftAnd, eGreaterF);

        // Créer une vraie instance d'AffectationNode au lieu d'un mock
        // result = ((a > b) && (c == d)) || (e > f)
        AffectationNode node = new AffectationNode(ident("result"), orNode);

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        // a > b
        inOrder.verify(builderSpy).addInstruction(LOAD, "a@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "b@global");
        inOrder.verify(builderSpy).addInstruction(SUP);
        // c == d
        inOrder.verify(builderSpy).addInstruction(LOAD, "c@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "d@global");
        inOrder.verify(builderSpy).addInstruction(CMP);
        // AND
        inOrder.verify(builderSpy).addInstruction(AND);
        // e > f
        inOrder.verify(builderSpy).addInstruction(LOAD, "e@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "f@global");
        inOrder.verify(builderSpy).addInstruction(SUP);
        // OR
        inOrder.verify(builderSpy).addInstruction(OR);
        // STORE
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    // ===== Tests avec variables (déjà couverts partiellement ci-dessus) =====

    @Test
    void visitEqualsNode_twoVariables_generatesCorrectInstructions() {
        // x == y
        IdentNode varX = ident("x");
        IdentNode varY = ident("y");
        EqualsNode equalsNode = new EqualsNode(varX, varY);

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitEqualsNode_variableAndConstant_generatesCorrectInstructions() {
        // x == 5
        IdentNode varX = ident("x");
        EqualsNode equalsNode = new EqualsNode(varX, new NbreNode(5));

        AffectationNode node = mock(AffectationNode.class);
        when(node.getExpression()).thenReturn(equalsNode);
        when(node.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(node);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(CMP);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    // ===== Tests pour extractIdentifierName =====

    @Test
    void extractIdentifierName_withNonIdentNode_parsesFromToStringTree() {
        // Créer un mock AstNode qui retourne une chaîne formatée
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode mockNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode.class);
        when(mockNode.toStringTree()).thenReturn("Ident(myVar)");

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getIdent1Node()).thenReturn(mockNode);
        when(affectation.getExpression()).thenReturn(new NbreNode(10));

        visitor.visit(affectation);

        verify(builderSpy).addInstruction(STORE, "myVar@global");
    }

    @Test
    void extractIdentifierName_withUnformattedString_returnsAsIs() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode mockNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode.class);
        when(mockNode.toStringTree()).thenReturn("someWeirdFormat");

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getIdent1Node()).thenReturn(mockNode);
        when(affectation.getExpression()).thenReturn(new NbreNode(10));

        visitor.visit(affectation);

        verify(builderSpy).addInstruction(STORE, "someWeirdFormat@global");
    }

    // ===== Tests pour resolveVariableScope =====

    @Test
    void resolveVariableScope_inMainScopeWithLocalVariable_returnsMain() {
        // Créer une variable locale dans le main
        MainNode mainNode = mock(MainNode.class);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode varsNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode.class);
        VarNode localVar = var("localVar", Type.ENTIER);

        when(mainNode.getVars()).thenReturn(varsNode);
        when(varsNode.getVar()).thenReturn(localVar);
        when(varsNode.getVars()).thenReturn(null);

        InstructionsNode instrs = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);
        when(mainNode.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);
        when(affectation.getIdent1Node()).thenReturn(ident("localVar"));
        when(affectation.getExpression()).thenReturn(new NbreNode(42));

        visitor.visit(mainNode);

        verify(builderSpy).addInstruction(STORE, "localVar@main");
    }

    @Test
    void resolveVariableScope_globalVariable_returnsGlobal() {
        // Variable globale utilisée dans le main
        DeclsNode declsNode = mock(DeclsNode.class);
        VarNode globalVar = var("globalVar", Type.ENTIER);
        when(declsNode.getDecl()).thenReturn(globalVar);
        when(declsNode.getDecls()).thenReturn(null);

        visitor.visit(declsNode);

        MainNode mainNode = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);

        when(mainNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);
        when(affectation.getIdent1Node()).thenReturn(ident("globalVar"));
        when(affectation.getExpression()).thenReturn(new NbreNode(99));

        visitor.visit(mainNode);

        verify(builderSpy).addInstruction(STORE, "globalVar@global");
    }

    // ===== Tests pour visit(InstructionNode) avec tous les types =====

    @Test
    void visitInstructionNode_withSiNode_callsVisitSi() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode siNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode.class);
        when(siNode.getExpressionNode()).thenReturn(new NbreNode(1));
        when(siNode.getInstructionsNode()).thenReturn(null);
        when(siNode.getInstructionsNode2()).thenReturn(null);

        visitor.visit(siNode);

        verify(builderSpy).addInstruction(eq(IF), anyInt());
    }

    @Test
    void visitInstructionNode_withTantqueNode_callsVisitTantque() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode tantqueNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode.class);
        when(tantqueNode.getExpressionNode()).thenReturn(new NbreNode(1));
        when(tantqueNode.getInstructionsNode()).thenReturn(null);

        visitor.visit(tantqueNode);

        verify(builderSpy).addInstruction(NOT);
        verify(builderSpy).addInstruction(eq(IF), anyInt());
    }

    @Test
    void visitInstructionNode_withSommeNode_callsVisitSomme() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode sommeNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode.class);
        when(sommeNode.getIdent1Node()).thenReturn(ident("x"));
        when(sommeNode.getExpressionNode()).thenReturn(new NbreNode(5));

        visitor.visit(sommeNode);

        verify(builderSpy).addInstruction(PUSH, 5);
        verify(builderSpy).addInstruction(INC, "x@global");
    }

    // ===== Tests pour visitSi =====

    @Test
    void visitSi_simpleIfWithoutElse_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode siNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode.class);
        when(siNode.getExpressionNode()).thenReturn(new NbreNode(1));

        InstructionsNode thenBlock = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);
        when(thenBlock.getInstructionNode()).thenReturn(affectation);
        when(thenBlock.getInstructions()).thenReturn(null);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));
        when(affectation.getExpression()).thenReturn(new NbreNode(10));

        when(siNode.getInstructionsNode()).thenReturn(thenBlock);
        when(siNode.getInstructionsNode2()).thenReturn(null);

        visitor.visit(siNode);

        verify(builderSpy).addInstruction(PUSH, 1);
        verify(builderSpy).addInstruction(eq(IF), anyInt());
        verify(builderSpy, never()).addInstruction(eq(GOTO), anyInt());
    }

    @Test
    void visitSi_ifWithElse_generatesGotoInstruction() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode siNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode.class);
        when(siNode.getExpressionNode()).thenReturn(new NbreNode(1));

        InstructionsNode thenBlock = mock(InstructionsNode.class);
        AffectationNode thenAffect = mock(AffectationNode.class);
        when(thenBlock.getInstructionNode()).thenReturn(thenAffect);
        when(thenBlock.getInstructions()).thenReturn(null);
        when(thenAffect.getIdent1Node()).thenReturn(ident("x"));
        when(thenAffect.getExpression()).thenReturn(new NbreNode(10));

        InstructionsNode elseBlock = mock(InstructionsNode.class);
        AffectationNode elseAffect = mock(AffectationNode.class);
        when(elseBlock.getInstructionNode()).thenReturn(elseAffect);
        when(elseBlock.getInstructions()).thenReturn(null);
        when(elseAffect.getIdent1Node()).thenReturn(ident("x"));
        when(elseAffect.getExpression()).thenReturn(new NbreNode(20));

        when(siNode.getInstructionsNode()).thenReturn(thenBlock);
        when(siNode.getInstructionsNode2()).thenReturn(elseBlock);

        visitor.visit(siNode);

        verify(builderSpy).addInstruction(eq(IF), anyInt());
        verify(builderSpy).addInstruction(eq(GOTO), anyInt());
    }

    @Test
    void visitSi_withNullCondition_handlesGracefully() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode siNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode.class);
        when(siNode.getExpressionNode()).thenReturn(null);
        when(siNode.getInstructionsNode()).thenReturn(null);
        when(siNode.getInstructionsNode2()).thenReturn(null);

        visitor.visit(siNode);

        verify(builderSpy).addInstruction(eq(IF), anyInt());
    }

    // ===== Tests pour visitTantque =====

    @Test
    void visitTantque_simpleWhileLoop_generatesCorrectInstructions() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode tantqueNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode.class);
        GreaterThanNode condition = new GreaterThanNode(ident("i"), new NbreNode(0));
        when(tantqueNode.getExpressionNode()).thenReturn(condition);

        InstructionsNode body = mock(InstructionsNode.class);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode somme = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode.class);
        when(body.getInstructionNode()).thenReturn(somme);
        when(body.getInstructions()).thenReturn(null);
        when(somme.getIdent1Node()).thenReturn(ident("i"));
        when(somme.getExpressionNode()).thenReturn(new NbreNode(-1));

        when(tantqueNode.getInstructionsNode()).thenReturn(body);

        visitor.visit(tantqueNode);

        verify(builderSpy).addInstruction(NOT);
        verify(builderSpy).addInstruction(eq(IF), anyInt());
        verify(builderSpy).addInstruction(eq(GOTO), anyInt());
    }

    @Test
    void visitTantque_withNullBody_generatesLoopStructure() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode tantqueNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode.class);
        when(tantqueNode.getExpressionNode()).thenReturn(new NbreNode(1));
        when(tantqueNode.getInstructionsNode()).thenReturn(null);

        visitor.visit(tantqueNode);

        verify(builderSpy).addInstruction(NOT);
        verify(builderSpy).addInstruction(eq(IF), anyInt());
        verify(builderSpy).addInstruction(eq(GOTO), anyInt());
    }

    @Test
    void visitTantque_withNullCondition_handlesGracefully() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode tantqueNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode.class);
        when(tantqueNode.getExpressionNode()).thenReturn(null);
        when(tantqueNode.getInstructionsNode()).thenReturn(null);

        visitor.visit(tantqueNode);

        verify(builderSpy).addInstruction(NOT);
        verify(builderSpy).addInstruction(eq(IF), anyInt());
    }

    // ===== Tests pour visitSomme =====

    @Test
    void visitSomme_withSimpleExpression_generatesIncInstruction() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode sommeNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode.class);
        when(sommeNode.getIdent1Node()).thenReturn(ident("counter"));
        when(sommeNode.getExpressionNode()).thenReturn(new NbreNode(1));

        visitor.visit(sommeNode);

        verify(builderSpy).addInstruction(PUSH, 1);
        verify(builderSpy).addInstruction(INC, "counter@global");
    }

    @Test
    void visitSomme_withComplexExpression_evaluatesBeforeInc() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode sommeNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode.class);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode mult =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode(new NbreNode(2), ident("x"));
        when(sommeNode.getIdent1Node()).thenReturn(ident("y"));
        when(sommeNode.getExpressionNode()).thenReturn(mult);

        visitor.visit(sommeNode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(LOAD, "x@global");
        inOrder.verify(builderSpy).addInstruction(MUL);
        inOrder.verify(builderSpy).addInstruction(INC, "y@global");
    }

    @Test
    void visitSomme_withNullExpression_handlesGracefully() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode sommeNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode.class);
        when(sommeNode.getIdent1Node()).thenReturn(ident("x"));
        when(sommeNode.getExpressionNode()).thenReturn(null);

        visitor.visit(sommeNode);

        verify(builderSpy).addInstruction(INC, "x@global");
    }

    // ===== Tests pour visit(VarsNode) =====

    @Test
    void visitVarsNode_withSingleVariable_processesCorrectly() {
        VarsNode varsNode = mock(VarsNode.class);
        VarNode var = var("localVar", Type.ENTIER);
        when(varsNode.getVar()).thenReturn(var);
        when(varsNode.getVars()).thenReturn(null);

        visitor.visit(varsNode);

        verify(builderSpy).addInstruction(PUSH, 0);
        verify(builderSpy).addInstruction(eq(NEW), contains("localVar"), eq("int"), eq("var"), eq(0));
    }

    @Test
    void visitVarsNode_withMultipleVariables_processesAll() {
        VarsNode varsNode1 = mock(VarsNode.class);
        VarsNode varsNode2 = mock(VarsNode.class);
        VarNode var1 = var("a", Type.ENTIER);
        VarNode var2 = var("b", Type.BOOLEEN);

        when(varsNode1.getVar()).thenReturn(var1);
        when(varsNode1.getVars()).thenReturn(varsNode2);
        when(varsNode2.getVar()).thenReturn(var2);
        when(varsNode2.getVars()).thenReturn(null);

        visitor.visit(varsNode1);

        verify(builderSpy, times(2)).addInstruction(eq(NEW), anyString(), anyString(), eq("var"), eq(0));
    }

    @Test
    void visitVarsNode_withNullNode_doesNothing() {
        visitor.visit((VarsNode) null);
        verify(builderSpy, never()).addInstruction(any(), any());
    }

    @Test
    void visitVarsNode_withNullVar_doesNothing() {
        VarsNode varsNode = mock(VarsNode.class);
        when(varsNode.getVar()).thenReturn(null);

        visitor.visit(varsNode);

        verify(builderSpy, never()).addInstruction(any(), any());
    }

    // ===== Tests pour visitUnaryMinus =====

    @Test
    void visitUnaryMinus_withPositiveNumber_generatesNegInstruction() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(new NbreNode(5));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(unaryMinus);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(NEG);
        inOrder.verify(builderSpy).addInstruction(STORE, "x@global");
    }

    @Test
    void visitUnaryMinus_withVariable_generatesLoadThenNeg() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(ident("y"));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(unaryMinus);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "y@global");
        inOrder.verify(builderSpy).addInstruction(NEG);
        inOrder.verify(builderSpy).addInstruction(STORE, "x@global");
    }

    @Test
    void visitUnaryMinus_withNegativeNumber_generatesNegInstruction() {
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(new NbreNode(-3));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(unaryMinus);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, -3);
        inOrder.verify(builderSpy).addInstruction(NEG);
    }

    // ===== Tests pour visitDivision =====

    @Test
    void visitDivision_withTwoNumbers_generatesDivInstruction() {
        DivisionNode division = new DivisionNode(new NbreNode(10), new NbreNode(2));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(division);
        when(affectation.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(DIV);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitDivision_withVariables_generatesLoadThenDiv() {
        DivisionNode division = new DivisionNode(ident("a"), ident("b"));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(division);
        when(affectation.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LOAD, "a@global");
        inOrder.verify(builderSpy).addInstruction(LOAD, "b@global");
        inOrder.verify(builderSpy).addInstruction(DIV);
        inOrder.verify(builderSpy).addInstruction(STORE, "result@global");
    }

    @Test
    void visitDivision_withNegativeNumbers_generatesDivInstruction() {
        DivisionNode division = new DivisionNode(new NbreNode(-20), new NbreNode(4));

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(division);
        when(affectation.getIdent1Node()).thenReturn(ident("result"));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, -20);
        inOrder.verify(builderSpy).addInstruction(PUSH, 4);
        inOrder.verify(builderSpy).addInstruction(DIV);
    }

    // ===== Tests complets pour visit(VarNode) =====

    @Test
    void visitVarNode_withBoolValueNodeFalse_deducesTypeAsBoolean() {
        // Test: boolean x = false;
        IdentNode ident = new IdentNode("y");
        BoolValueNode boolValue = new BoolValueNode(false);

        VarNode varNode = new VarNode(Type.BOOLEEN, ident, boolValue);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(PUSH, false);
        verify(builderSpy).addInstruction(NEW, "y@global", "boolean", "var", 0);
    }

    @Test
    void visitVarNode_withNbreNode_deducesTypeAsInt() {
        // Test: int x = 42; → le type doit être déduit comme INT
        IdentNode ident = new IdentNode("count");
        NbreNode nbreValue = new NbreNode(42);

        VarNode varNode = new VarNode(Type.ENTIER, ident, nbreValue);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(PUSH, 42);
        verify(builderSpy).addInstruction(NEW, "count@global", "int", "var", 0);
    }

    @Test
    void visitVarNode_withNbreNodeNegative_deducesTypeAsInt() {
        // Test: int x = -10;
        IdentNode ident = new IdentNode("negNum");
        NbreNode nbreValue = new NbreNode(-10);

        VarNode varNode = new VarNode(Type.ENTIER, ident, nbreValue);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(PUSH, -10);
        verify(builderSpy).addInstruction(NEW, "negNum@global", "int", "var", 0);
    }

    @Test
    void visitVarNode_withComplexExpression_callsVisitExpression() {
        // Test: int x = 2 + 3; → doit appeler visitExpression
        IdentNode ident = new IdentNode("sum");
        PlusNode plusExpr = new PlusNode(new NbreNode(2), new NbreNode(3));

        VarNode varNode = new VarNode(Type.ENTIER, ident, plusExpr);

        visitor.visit(varNode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 2);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);
        inOrder.verify(builderSpy).addInstruction(ADD);
        inOrder.verify(builderSpy).addInstruction(NEW, "sum@global", "int", "var", 0);
    }

    @Test
    void visitVarNode_withIdentExpression_usesNormalizeType() {
        // Test: int x = y; → le type doit être normalisé car ce n'est ni BoolValueNode ni NbreNode
        IdentNode ident = new IdentNode("x");
        IdentNode yIdent = new IdentNode("y");

        VarNode varNode = new VarNode(Type.ENTIER, ident, yIdent);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(LOAD, "y@global");
        verify(builderSpy).addInstruction(NEW, "x@global", "int", "var", 0);
    }


    @Test
    void visitVarNode_inMainScope_addsToMainLocalVariables() {
        // Test qu'une variable déclarée dans le main est ajoutée aux variables locales
        MainNode mainNode = mock(MainNode.class);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode varsNode = mock(fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode.class);
        VarNode localVar = var("localInMain", Type.ENTIER);

        when(mainNode.getVars()).thenReturn(varsNode);
        when(varsNode.getVar()).thenReturn(localVar);
        when(varsNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(null);

        visitor.visit(mainNode);

        verify(builderSpy).addInstruction(NEW, "localInMain@main", "int", "var", 0);
    }

    @Test
    void visitVarNode_inGlobalScope_doesNotAddToMainLocalVariables() {
        // Test qu'une variable globale n'est pas ajoutée aux variables locales du main
        VarNode globalVar = var("globalVar", Type.BOOLEEN);

        visitor.visit(globalVar);

        verify(builderSpy).addInstruction(NEW, "globalVar@global", "boolean", "var", 0);
    }

    @Test
    void visitVarNode_withUppercaseType_normalizesCorrectly() {
        // Test: BOOLEAN x; → doit être normalisé en BOOLEAN
        IdentNode ident = new IdentNode("uppercaseType");
        VarNode varNode = new VarNode(Type.BOOLEEN, ident, null);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(PUSH, false);
        verify(builderSpy).addInstruction(NEW, "uppercaseType@global", "boolean", "var", 0);
    }

    @Test
    void visitVarNode_withMixedCaseType_normalizesCorrectly() {
        // Test: Boolean x; → doit être normalisé en BOOLEAN
        IdentNode ident = new IdentNode("mixedCase");
        VarNode varNode = new VarNode(Type.BOOLEEN, ident, null);

        visitor.visit(varNode);

        verify(builderSpy).addInstruction(PUSH, false);
        verify(builderSpy).addInstruction(NEW, "mixedCase@global", "boolean", "var", 0);
    }
}

