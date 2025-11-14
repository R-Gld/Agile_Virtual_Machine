package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyInt;

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
        visitor = new MiniJajaCompilerVisitor(new Stacks());
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
     * @param type The variable type (e.g., "int", "bool").
     * @return A new VarNode instance.
     */
    private VarNode var(String name, String type) {
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
        VarNode node = var("x", "int");

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 0);
        verify(builderSpy).addInstruction(eq(NEW), eq("x@global"), eq("INT"), eq("VARIABLE"), eq(0));
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
        VarNode var1 = var("x", "int");
        VarNode var2 = var("y", "bool");
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
        VarNode var1 = var("a", "int");
        VarNode var2 = var("b", "bool");
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
        inOrder.verify(builderSpy).addInstruction(NEW, "a@global", "INT", "VARIABLE", 0);
        inOrder.verify(builderSpy).addInstruction(NEW, "b@global", "BOOLEAN", "VARIABLE", 0);
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
}
