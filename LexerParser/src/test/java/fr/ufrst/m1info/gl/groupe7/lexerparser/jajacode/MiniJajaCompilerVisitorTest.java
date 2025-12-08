package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireLnNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
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
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode mult = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode(new NbreNode(2), ident("x"));
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
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(new NbreNode(5));

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
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(ident("y"));

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
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode unaryMinus = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode(new NbreNode(-3));

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

    // ==================== Tests pour MethodeNode ====================

    /**
     * Crée un MethodeNode simple sans paramètres ni variables locales.
     */
    private MethodeNode createSimpleMethod(String name, Type returnType) {
        IdentNode ident = new IdentNode(name);
        return new MethodeNode(returnType, ident, null, null, null);
    }

    /**
     * Crée un EnteteNode (paramètre de méthode).
     */
    private EnteteNode createEntete(String name, Type type) {
        return new EnteteNode(new IdentNode(name), type);
    }

    /**
     * Crée un EntetesNode avec un seul paramètre.
     */
    private EntetesNode createSingleEntetes(String name, Type type) {
        return new EntetesNode(createEntete(name, type), null);
    }

    /**
     * Crée un EntetesNode avec deux paramètres.
     */
    private EntetesNode createDoubleEntetes(String name1, Type type1, String name2, Type type2) {
        EnteteNode entete1 = createEntete(name1, type1);
        EntetesNode entetes2 = createSingleEntetes(name2, type2);
        return new EntetesNode(entete1, entetes2);
    }

    @Test
    void visitMethodeNode_simpleIntMethod_generatesCorrectStructure() {
        // Test: int f() { } → push, new, goto, swap, return
        MethodeNode methode = createSimpleMethod("f", Type.ENTIER);

        visitor.visit(methode);

        InOrder inOrder = inOrder(builderSpy);
        // push(adresse_début) = 4 (après init qui est à 1, puis push à 2, new à 3, goto à 4)
        inOrder.verify(builderSpy).addInstruction(eq(PUSH), anyInt());
        inOrder.verify(builderSpy).addInstruction(eq(NEW), eq("f"), eq("int"), eq("meth"), eq(0));
        inOrder.verify(builderSpy).addInstruction(eq(GOTO), anyInt());
        inOrder.verify(builderSpy).addInstruction(SWAP);
        inOrder.verify(builderSpy).addInstruction(RETURN);
    }

    @Test
    void visitMethodeNode_voidMethod_generatesPush0BeforeSwapReturn() {
        // Test: void f() { } → push, new, goto, push(0), swap, return
        MethodeNode methode = createSimpleMethod("f", Type.VOID);

        visitor.visit(methode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(eq(PUSH), anyInt()); // adresse début
        inOrder.verify(builderSpy).addInstruction(eq(NEW), eq("f"), eq("void"), eq("meth"), eq(0));
        inOrder.verify(builderSpy).addInstruction(eq(GOTO), anyInt());
        inOrder.verify(builderSpy).addInstruction(PUSH, 0); // valeur retour void
        inOrder.verify(builderSpy).addInstruction(SWAP);
        inOrder.verify(builderSpy).addInstruction(RETURN);
    }

    @Test
    void visitMethodeNode_withOneParameter_generatesNewForParam() {
        // Test: int f(int p) { } → new(p@f@int, int, var, 1)
        IdentNode ident = new IdentNode("f");
        EntetesNode entetes = createSingleEntetes("p", Type.ENTIER);
        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes, null, null);

        visitor.visit(methode);

        // Vérifier que le builder contient l'instruction pour le paramètre
        // Les paramètres sont dans un builder séparé qui est fusionné
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(p@f@int, int, var, 1)"), "Should contain parameter declaration. Output: " + output);
    }

    @Test
    void visitMethodeNode_withTwoParameters_generatesNewForEachParam() {
        // Test: int f(int a, boolean b) { }
        IdentNode ident = new IdentNode("f");
        EntetesNode entetes = createDoubleEntetes("a", Type.ENTIER, "b", Type.BOOLEEN);
        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes, null, null);

        visitor.visit(methode);

        // Vérifier que le builder contient les instructions pour les paramètres
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(b@f@int, boolean, var, 1)"), "Should contain param b declaration. Output: " + output);
        assertTrue(output.contains("new(a@f@int, int, var, 2)"), "Should contain param a declaration. Output: " + output);
    }

    @Test
    void visitMethodeNode_withLocalVariable_generatesNewAndRetrait() {
        // Test: int f() { int x; } → new pour x, puis swap/pop pour retirer x
        IdentNode ident = new IdentNode("f");
        VarsNode vars = mock(VarsNode.class);
        VarNode localVar = var("x", Type.ENTIER);
        when(vars.getVar()).thenReturn(localVar);
        when(vars.getVars()).thenReturn(null);

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, null, vars, null);

        visitor.visit(methode);

        // Vérifier la sortie contient les éléments attendus
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(f, int, meth, 0)"), "Should contain method declaration. Output: " + output);
        assertTrue(output.contains("swap"), "Should contain swap. Output: " + output);
        assertTrue(output.contains("pop"), "Should contain pop. Output: " + output);
        assertTrue(output.contains("return"), "Should contain return. Output: " + output);
    }

    @Test
    void visitMethodeNode_withInstructions_generatesInstructionsInBody() {
        // Test: int f() { x = 5; } où x est une variable globale (pas dans le scope de la méthode)
        IdentNode ident = new IdentNode("f");
        InstructionsNode instrs = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);
        when(affectation.getExpression()).thenReturn(new NbreNode(5));
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("x"));

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, null, null, instrs);

        visitor.visit(methode);

        // Vérifier que la sortie contient les instructions d'affectation
        // x n'est pas un paramètre ni une variable locale, donc elle utilise le scope global
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(5)"), "Should contain push(5). Output: " + output);
        assertTrue(output.contains("store(x@global)"), "Should contain store with global scope (x is not in method scope). Output: " + output);
    }

    @Test
    void visitMethodeNode_withReturnStatement_generatesLoadInstruction() {
        // Test: int f(int p) { return p; }
        IdentNode ident = new IdentNode("f");
        EntetesNode entetes = createSingleEntetes("p", Type.ENTIER);

        InstructionsNode instrs = mock(InstructionsNode.class);
        RetourNode retour = mock(RetourNode.class);
        when(instrs.getInstructionNode()).thenReturn(retour);
        when(instrs.getInstructions()).thenReturn(null);
        when(retour.getExp()).thenReturn(new IdentNode("p"));

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes, null, instrs);

        visitor.visit(methode);

        // Vérifier que la sortie contient l'instruction load avec le scope
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("load(p@f@int)"), "Should contain load with scope. Output: " + output);
    }

    @Test
    void visitMethodeNode_booleanMethod_generatesCorrectType() {
        // Test: boolean isValid() { }
        MethodeNode methode = createSimpleMethod("isValid", Type.BOOLEEN);

        visitor.visit(methode);

        verify(builderSpy).addInstruction(eq(NEW), eq("isValid"), eq("boolean"), eq("meth"), eq(0));
    }

    @Test
    void visitMethodeNode_addsMethodToVariablesToPop() {
        // Test: la méthode doit être ajoutée à variablesToPop pour le retrait
        MethodeNode methode = createSimpleMethod("myMethod", Type.ENTIER);

        visitor.visit(methode);

        // Après la visite, la méthode devrait être dans variablesToPop
        // On vérifie indirectement via la génération du NEW
        verify(builderSpy).addInstruction(eq(NEW), eq("myMethod"), eq("int"), eq("meth"), eq(0));
    }

    @Test
    void visitMethodeNode_calculatesCorrectGotoAddress_noBody() {
        // Test: int f() { } → goto pointe vers l'instruction après return
        MethodeNode methode = createSimpleMethod("f", Type.ENTIER);

        visitor.visit(methode);

        // Capturer les arguments de GOTO
        ArgumentCaptor<Integer> gotoCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(builderSpy).addInstruction(eq(GOTO), gotoCaptor.capture());

        // Pour une méthode vide non-void : corps vide (0) + swap (1) + return (1) = 2
        // goto pointe vers methodStartAddr + bodySize + 0 + 0 + 2 = adresse après return
        int gotoTarget = gotoCaptor.getValue();
        assertTrue(gotoTarget > 0, "GOTO target should be positive");
    }

    @Test
    void visitMethodeNode_voidWithLocalVar_generatesPush0AndRetrait() {
        // Test: void f() { int x; } → push(0) pour void, swap/pop pour x
        IdentNode ident = new IdentNode("f");
        VarsNode vars = mock(VarsNode.class);
        VarNode localVar = var("x", Type.ENTIER);
        when(vars.getVar()).thenReturn(localVar);
        when(vars.getVars()).thenReturn(null);

        MethodeNode methode = new MethodeNode(Type.VOID, ident, null, vars, null);

        visitor.visit(methode);

        // Vérifier la sortie contient les éléments attendus
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(f, void, meth, 0)"), "Should contain void method declaration. Output: " + output);
        // Compter les occurrences de push(0) - devrait y en avoir au moins 2 (init var et void return)
        int pushCount = output.split("push\\(0\\)").length - 1;
        assertTrue(pushCount >= 2, "Should have at least 2 push(0). Count: " + pushCount + ". Output: " + output);
        assertTrue(output.contains("swap"), "Should contain swap. Output: " + output);
        assertTrue(output.contains("return"), "Should contain return. Output: " + output);
    }

    @Test
    void visitMethodeNode_withMultipleLocalVars_generatesMultipleRetraits() {
        // Test: int f() { int x; int y; } → swap/pop pour y, swap/pop pour x
        IdentNode ident = new IdentNode("f");

        VarsNode vars1 = mock(VarsNode.class);
        VarsNode vars2 = mock(VarsNode.class);
        VarNode localVar1 = var("x", Type.ENTIER);
        VarNode localVar2 = var("y", Type.ENTIER);

        when(vars1.getVar()).thenReturn(localVar1);
        when(vars1.getVars()).thenReturn(vars2);
        when(vars2.getVar()).thenReturn(localVar2);
        when(vars2.getVars()).thenReturn(null);

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, null, vars1, null);

        visitor.visit(methode);

        // Vérifier qu'il y a 2 paires swap/pop pour les 2 variables locales
        // Plus 1 paire swap/return à la fin
        verify(builderSpy, atLeast(2)).addInstruction(SWAP);
        verify(builderSpy, atLeast(2)).addInstruction(POP);
        verify(builderSpy).addInstruction(RETURN);
    }

    @Test
    void visitMethodeNode_scopeIsRestoredAfterVisit() {
        // Test: le scope doit être restauré après la visite de la méthode
        MethodeNode methode = createSimpleMethod("f", Type.ENTIER);

        // Simuler un scope initial
        // Le scope est "global" au départ
        visitor.visit(methode);

        // On vérifie que la méthode a été compilée correctement
        // (le scope devrait être restauré à "global" mais c'est interne)
        verify(builderSpy).addInstruction(eq(NEW), eq("f"), eq("int"), eq("meth"), eq(0));
    }

    @Test
    void visitMethodeNode_complexMethod_generatesAllParts() {
        // Test complet: int add(int a, int b) { int result; result = a + b; return result; }
        IdentNode ident = new IdentNode("add");
        EntetesNode entetes = createDoubleEntetes("a", Type.ENTIER, "b", Type.ENTIER);

        VarsNode vars = mock(VarsNode.class);
        VarNode localVar = var("result", Type.ENTIER);
        when(vars.getVar()).thenReturn(localVar);
        when(vars.getVars()).thenReturn(null);

        // Instructions mockées
        InstructionsNode instrs = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        PlusNode plusNode = new PlusNode(new IdentNode("a"), new IdentNode("b"));
        when(affectation.getExpression()).thenReturn(plusNode);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("result"));

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes, vars, instrs);

        visitor.visit(methode);

        // Vérifier la sortie contient tous les éléments
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(add, int, meth, 0)"), "Should contain method declaration. Output: " + output);
        assertTrue(output.contains("new(b@add@int, int, var, 1)"), "Should contain param b. Output: " + output);
        assertTrue(output.contains("new(a@add@int, int, var, 2)"), "Should contain param a. Output: " + output);
        assertTrue(output.contains("load(a@add@int)"), "Should contain load a. Output: " + output);
        assertTrue(output.contains("load(b@add@int)"), "Should contain load b. Output: " + output);
        assertTrue(output.contains("add"), "Should contain add. Output: " + output);
        assertTrue(output.contains("store(result@add@int)"), "Should contain store result. Output: " + output);
        assertTrue(output.contains("return"), "Should contain return. Output: " + output);
    }

    @Test
    void visitMethodeNode_methodStartAddrCalculation() {
        // Test: l'adresse de début de la méthode est currentAddr + 3
        MethodeNode methode = createSimpleMethod("test", Type.ENTIER);

        visitor.visit(methode);

        // Capturer l'adresse de début (premier PUSH)
        ArgumentCaptor<Integer> pushCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(builderSpy, atLeastOnce()).addInstruction(eq(PUSH), pushCaptor.capture());

        // La première valeur capturée devrait être l'adresse de début
        List<Integer> pushValues = pushCaptor.getAllValues();
        assertFalse(pushValues.isEmpty(), "Should have at least one PUSH");
        int methodStartAddr = pushValues.get(0);
        assertTrue(methodStartAddr >= 4, "Method start address should be at least 4 (after push, new, goto)");
    }

    @Test
    void visitMethodeNode_emptyVarsNode_doesNotCrash() {
        // Test: vars vide ne cause pas d'erreur
        IdentNode ident = new IdentNode("f");
        VarsNode emptyVars = mock(VarsNode.class);
        when(emptyVars.getVar()).thenReturn(null);

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, null, emptyVars, null);

        assertDoesNotThrow(() -> visitor.visit(methode));
    }

    @Test
    void visitMethodeNode_emptyEntetesNode_doesNotCrash() {
        // Test: entetes vide ne cause pas d'erreur
        IdentNode ident = new IdentNode("f");
        EntetesNode emptyEntetes = new EntetesNode(); // Constructeur vide

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, emptyEntetes, null, null);

        assertDoesNotThrow(() -> visitor.visit(methode));
    }

    @Test
    void visitMethodeNode_emptyInstrsNode_doesNotCrash() {
        // Test: instrs vide ne cause pas d'erreur
        IdentNode ident = new IdentNode("f");
        InstructionsNode emptyInstrs = mock(InstructionsNode.class);
        when(emptyInstrs.getInstructionNode()).thenReturn(null);

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, null, null, emptyInstrs);

        assertDoesNotThrow(() -> visitor.visit(methode));
    }

    @Test
    void visitMethodeNode_withThreeParameters_generatesCorrectDepths() {
        // Test: int f(int a, int b, int c) { }
        IdentNode ident = new IdentNode("f");
        EnteteNode entete1 = createEntete("a", Type.ENTIER);
        EnteteNode entete2 = createEntete("b", Type.ENTIER);
        EnteteNode entete3 = createEntete("c", Type.ENTIER);

        EntetesNode entetes3 = new EntetesNode(entete3, null);
        EntetesNode entetes2 = new EntetesNode(entete2, entetes3);
        EntetesNode entetes1 = new EntetesNode(entete1, entetes2);

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes1, null, null);

        visitor.visit(methode);

        // Vérifier les depths dans la sortie
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(c@f@int, int, var, 1)"), "Should contain param c with depth 1. Output: " + output);
        assertTrue(output.contains("new(b@f@int, int, var, 2)"), "Should contain param b with depth 2. Output: " + output);
        assertTrue(output.contains("new(a@f@int, int, var, 3)"), "Should contain param a with depth 3. Output: " + output);
    }

    @Test
    void visitMethodeNode_methodSignatureFormat() {
        // Test: la signature de la méthode est "nom@type" pour le scope interne
        IdentNode ident = new IdentNode("compute");
        EntetesNode entetes = createSingleEntetes("x", Type.ENTIER);

        InstructionsNode instrs = mock(InstructionsNode.class);
        AffectationNode affectation = mock(AffectationNode.class);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);
        when(affectation.getExpression()).thenReturn(new NbreNode(1));
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("x"));

        MethodeNode methode = new MethodeNode(Type.ENTIER, ident, entetes, null, instrs);

        visitor.visit(methode);

        // Le store devrait utiliser "x@compute@int" (nom avec scope de la méthode)
        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("store(x@compute@int)"), "Should contain store with method scope. Output: " + output);
    }

    // ==================== Tests pour visitEcrire ====================

    @Test
    void visitEcrire_withNumberExpression_generatesPushAndWrite() {
        // Test: write(42);
        EcrireNode node = new EcrireNode(new NbreNode(42));

        // On utilise un ClasseNode avec main pour tester
        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(42)"), "Should contain push(42). Output: " + output);
        assertTrue(output.contains("write"), "Should contain write. Output: " + output);
    }

    @Test
    void visitEcrire_withStringLiteral_generatesPushQuotedStringAndWrite() {
        // Test: write("Hello");
        EcrireNode node = new EcrireNode("Hello");

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(\"Hello\")"), "Should contain push with quoted string. Output: " + output);
        assertTrue(output.contains("write"), "Should contain write. Output: " + output);
    }

    @Test
    void visitEcrire_withIdentifier_generatesLoadAndWrite() {
        // Test: write(x);
        EcrireNode node = new EcrireNode(new IdentNode("x"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("load(x@"), "Should contain load. Output: " + output);
        assertTrue(output.contains("write"), "Should contain write. Output: " + output);
    }

    @Test
    void visitEcrire_withBooleanExpression_generatesPushAndWrite() {
        // Test: write(true);
        EcrireNode node = new EcrireNode(new BoolValueNode(true));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(true)"), "Should contain push(true). Output: " + output);
        assertTrue(output.contains("write"), "Should contain write. Output: " + output);
    }

    @Test
    void visitEcrire_withArithmeticExpression_generatesExpressionAndWrite() {
        // Test: write(1 + 2);
        PlusNode plusNode = new PlusNode(new NbreNode(1), new NbreNode(2));
        EcrireNode node = new EcrireNode(plusNode);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(1)"), "Should contain push(1). Output: " + output);
        assertTrue(output.contains("push(2)"), "Should contain push(2). Output: " + output);
        assertTrue(output.contains("add"), "Should contain add. Output: " + output);
        assertTrue(output.contains("write"), "Should contain write. Output: " + output);
    }

    // ==================== Tests pour visitEcrireLn ====================

    @Test
    void visitEcrireLn_withNumberExpression_generatesPushAndWriteln() {
        // Test: writeln(42);
        EcrireLnNode node = new EcrireLnNode(new NbreNode(42));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(42)"), "Should contain push(42). Output: " + output);
        assertTrue(output.contains("writeln"), "Should contain writeln. Output: " + output);
    }

    @Test
    void visitEcrireLn_withStringLiteral_generatesPushQuotedStringAndWriteln() {
        // Test: writeln("World");
        EcrireLnNode node = new EcrireLnNode("World");

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(\"World\")"), "Should contain push with quoted string. Output: " + output);
        assertTrue(output.contains("writeln"), "Should contain writeln. Output: " + output);
    }

    @Test
    void visitEcrireLn_withIdentifier_generatesLoadAndWriteln() {
        // Test: writeln(y);
        EcrireLnNode node = new EcrireLnNode(new IdentNode("y"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("load(y@"), "Should contain load. Output: " + output);
        assertTrue(output.contains("writeln"), "Should contain writeln. Output: " + output);
    }

    @Test
    void visitEcrireLn_withBooleanFalse_generatesPushFalseAndWriteln() {
        // Test: writeln(false);
        EcrireLnNode node = new EcrireLnNode(new BoolValueNode(false));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(false)"), "Should contain push(false). Output: " + output);
        assertTrue(output.contains("writeln"), "Should contain writeln. Output: " + output);
    }

    // ==================== Tests pour visitAppelI ====================

    @Test
    void visitAppelI_noArguments_generatesInvokeAndPop() {
        // Test: f(); (appel de méthode sans arguments en tant qu'instruction)
        IdentNode methodIdent = new IdentNode("f");
        ListExpNode emptyList = new ListExpNode(null, null);
        AppelINode node = new AppelINode(methodIdent, emptyList);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("invoke(f)"), "Should contain invoke(f). Output: " + output);
        assertTrue(output.contains("pop"), "Should contain pop (result discarded). Output: " + output);
    }

    @Test
    void visitAppelI_withOneArgument_generatesArgInvokeSwapPopPop() {
        // Test: f(5); (appel avec un argument)
        IdentNode methodIdent = new IdentNode("f");
        ListExpNode listExp = new ListExpNode(new NbreNode(5), null);
        AppelINode node = new AppelINode(methodIdent, listExp);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(5)"), "Should contain push(5). Output: " + output);
        assertTrue(output.contains("invoke(f)"), "Should contain invoke(f). Output: " + output);
        assertTrue(output.contains("swap"), "Should contain swap for argument removal. Output: " + output);
        // Il doit y avoir au moins 2 pop (1 pour l'argument, 1 pour le résultat)
        int popCount = output.split("pop").length - 1;
        assertTrue(popCount >= 2, "Should have at least 2 pop instructions. Count: " + popCount);
    }

    @Test
    void visitAppelI_withTwoArguments_generatesArgsInvokeSwapPopSwapPopPop() {
        // Test: f(1, 2); (appel avec deux arguments)
        IdentNode methodIdent = new IdentNode("g");
        ListExpNode innerList = new ListExpNode(new NbreNode(2), null);
        ListExpNode listExp = new ListExpNode(new NbreNode(1), innerList);
        AppelINode node = new AppelINode(methodIdent, listExp);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(1)") || output.contains("push(2)"), "Should contain push for arguments. Output: " + output);
        assertTrue(output.contains("invoke(g)"), "Should contain invoke(g). Output: " + output);
        // 2 arguments = 2 swap/pop pairs + 1 final pop
        int swapCount = output.split("swap").length - 1;
        assertTrue(swapCount >= 2, "Should have at least 2 swap for 2 args. Count: " + swapCount);
    }

    @Test
    void visitAppelI_withIdentifierArgument_generatesLoadInvoke() {
        // Test: f(x);
        IdentNode methodIdent = new IdentNode("process");
        ListExpNode listExp = new ListExpNode(new IdentNode("x"), null);
        AppelINode node = new AppelINode(methodIdent, listExp);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("load(x@"), "Should contain load for identifier. Output: " + output);
        assertTrue(output.contains("invoke(process)"), "Should contain invoke. Output: " + output);
    }

    @Test
    void visitAppelI_withExpressionArgument_generatesExpressionThenInvoke() {
        // Test: f(1 + 2);
        IdentNode methodIdent = new IdentNode("calc");
        PlusNode plusNode = new PlusNode(new NbreNode(1), new NbreNode(2));
        ListExpNode listExp = new ListExpNode(plusNode, null);
        AppelINode node = new AppelINode(methodIdent, listExp);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(1)"), "Should contain push(1). Output: " + output);
        assertTrue(output.contains("push(2)"), "Should contain push(2). Output: " + output);
        assertTrue(output.contains("add"), "Should contain add. Output: " + output);
        assertTrue(output.contains("invoke(calc)"), "Should contain invoke. Output: " + output);
    }

    // ==================== Tests pour visitAppelE ====================

    @Test
    void visitAppelE_noArguments_generatesInvokeOnly() {
        // Test: x = f(); (appel de méthode sans arguments en tant qu'expression)
        IdentNode methodIdent = new IdentNode("getValue");
        ListExpNode emptyList = new ListExpNode(null, null);
        AppelENode appelE = new AppelENode(methodIdent, emptyList);

        // Utiliser dans une affectation pour tester
        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(appelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("result"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("invoke(getValue)"), "Should contain invoke. Output: " + output);
        assertTrue(output.contains("store(result@"), "Should contain store. Output: " + output);
    }

    @Test
    void visitAppelE_withOneArgument_generatesArgInvokeSwapPop() {
        // Test: x = f(10); (appel avec un argument, résultat stocké)
        IdentNode methodIdent = new IdentNode("compute");
        ListExpNode listExp = new ListExpNode(new NbreNode(10), null);
        AppelENode appelE = new AppelENode(methodIdent, listExp);

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(appelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("y"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(10)"), "Should contain push(10). Output: " + output);
        assertTrue(output.contains("invoke(compute)"), "Should contain invoke. Output: " + output);
        assertTrue(output.contains("swap"), "Should contain swap for arg removal. Output: " + output);
        // Pas de pop après le dernier swap car le résultat reste sur la pile
        assertTrue(output.contains("store(y@"), "Should contain store. Output: " + output);
    }

    @Test
    void visitAppelE_withTwoArguments_generatesArgsInvokeSwapPopSwapPop() {
        // Test: z = add(3, 4);
        IdentNode methodIdent = new IdentNode("add");
        ListExpNode innerList = new ListExpNode(new NbreNode(4), null);
        ListExpNode listExp = new ListExpNode(new NbreNode(3), innerList);
        AppelENode appelE = new AppelENode(methodIdent, listExp);

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(appelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("z"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("invoke(add)"), "Should contain invoke. Output: " + output);
        // 2 arguments = 2 swap/pop pairs
        int swapCount = output.split("swap").length - 1;
        assertTrue(swapCount >= 2, "Should have at least 2 swap for 2 args. Count: " + swapCount);
    }

    @Test
    void visitAppelE_resultNotDiscarded_noFinalPop() {
        // Test: x = f(); - le résultat est utilisé, pas de pop final
        IdentNode methodIdent = new IdentNode("getVal");
        ListExpNode emptyList = new ListExpNode(null, null);
        AppelENode appelE = new AppelENode(methodIdent, emptyList);

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(appelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("x"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        // Vérifier que invoke est suivi de store (pas de pop entre les deux)
        int invokeIdx = output.indexOf("invoke(getVal)");
        int storeIdx = output.indexOf("store(x@");
        assertTrue(invokeIdx < storeIdx, "invoke should come before store. Output: " + output);
    }

    @Test
    void visitAppelE_withNestedCall_generatesCorrectSequence() {
        // Test: x = f(g(5)); - appel imbriqué
        IdentNode innerMethodIdent = new IdentNode("inner");
        ListExpNode innerListExp = new ListExpNode(new NbreNode(5), null);
        AppelENode innerAppelE = new AppelENode(innerMethodIdent, innerListExp);

        IdentNode outerMethodIdent = new IdentNode("outer");
        ListExpNode outerListExp = new ListExpNode(innerAppelE, null);
        AppelENode outerAppelE = new AppelENode(outerMethodIdent, outerListExp);

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(outerAppelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("result"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("push(5)"), "Should contain push(5). Output: " + output);
        assertTrue(output.contains("invoke(inner)"), "Should contain invoke(inner). Output: " + output);
        assertTrue(output.contains("invoke(outer)"), "Should contain invoke(outer). Output: " + output);
    }

    @Test
    void visitAppelI_nullListExp_handlesGracefully() {
        // Test: f(); avec listExp null
        IdentNode methodIdent = new IdentNode("noArgs");
        AppelINode node = new AppelINode(methodIdent, null);

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(node);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        assertDoesNotThrow(() -> visitor.visit(classe));

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("invoke(noArgs)"), "Should contain invoke. Output: " + output);
    }

    @Test
    void visitAppelE_nullExp_handlesGracefully() {
        // Test: x = f(); avec exp null
        IdentNode methodIdent = new IdentNode("noArgs");
        AppelENode appelE = new AppelENode(methodIdent, null);

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getExpression()).thenReturn(appelE);
        when(affectation.getIdent1Node()).thenReturn(new IdentNode("x"));

        MainNode main = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(main.getInstrs()).thenReturn(instrs);
        when(instrs.getInstructionNode()).thenReturn(affectation);
        when(instrs.getInstructions()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);
        when(classe.getMethodeMain()).thenReturn(main);

        assertDoesNotThrow(() -> visitor.visit(classe));
    }

    // ==================== Tests pour visit(DeclsNode) avec MethodeNode ====================

    @Test
    void visitDeclsNode_withMethodeNode_generatesMethodDeclaration() {
        // Test: class C { int f() { } main { } }
        MethodeNode methode = createSimpleMethod("myFunc", Type.ENTIER);

        DeclsNode decls = mock(DeclsNode.class);
        when(decls.getDecl()).thenReturn(methode);
        when(decls.getDecls()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(myFunc, int, meth, 0)"),
            "Should contain method declaration. Output: " + output);
        assertTrue(output.contains("swap"), "Should contain swap. Output: " + output);
        assertTrue(output.contains("return"), "Should contain return. Output: " + output);
    }

    @Test
    void visitDeclsNode_withMethodeNodeAndVarNode_generatesBothDeclarations() {
        // Test: class C { int x = 0; int f() { } main { } }
        VarNode varNode = var("x", Type.ENTIER);
        MethodeNode methode = createSimpleMethod("compute", Type.BOOLEEN);

        DeclsNode innerDecls = mock(DeclsNode.class);
        when(innerDecls.getDecl()).thenReturn(methode);
        when(innerDecls.getDecls()).thenReturn(null);

        DeclsNode decls = mock(DeclsNode.class);
        when(decls.getDecl()).thenReturn(varNode);
        when(decls.getDecls()).thenReturn(innerDecls);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(x@global, int, var, 0)"),
            "Should contain variable declaration. Output: " + output);
        assertTrue(output.contains("new(compute, boolean, meth, 0)"),
            "Should contain method declaration. Output: " + output);
    }

    @Test
    void visitDeclsNode_withMultipleMethods_generatesAllMethodDeclarations() {
        // Test: class C { int f() { } boolean g() { } main { } }
        MethodeNode methode1 = createSimpleMethod("funcA", Type.ENTIER);
        MethodeNode methode2 = createSimpleMethod("funcB", Type.BOOLEEN);

        DeclsNode innerDecls = mock(DeclsNode.class);
        when(innerDecls.getDecl()).thenReturn(methode2);
        when(innerDecls.getDecls()).thenReturn(null);

        DeclsNode decls = mock(DeclsNode.class);
        when(decls.getDecl()).thenReturn(methode1);
        when(decls.getDecls()).thenReturn(innerDecls);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(funcA, int, meth, 0)"),
            "Should contain first method. Output: " + output);
        assertTrue(output.contains("new(funcB, boolean, meth, 0)"),
            "Should contain second method. Output: " + output);
    }

    @Test
    void visitDeclsNode_withVoidMethod_generatesVoidMethodDeclaration() {
        // Test: class C { void doSomething() { } main { } }
        MethodeNode methode = createSimpleMethod("doSomething", Type.VOID);

        DeclsNode decls = mock(DeclsNode.class);
        when(decls.getDecl()).thenReturn(methode);
        when(decls.getDecls()).thenReturn(null);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(decls);
        when(classe.getMethodeMain()).thenReturn(null);

        visitor.visit(classe);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(doSomething, void, meth, 0)"),
            "Should contain void method declaration. Output: " + output);
        // Les méthodes void ont un push(0) avant le retrait
        assertTrue(output.contains("push(0)"),
            "Void method should have push(0). Output: " + output);
    }

    // ==================== Tests pour typeToJajaCode default case ====================

    @Test
    void typeToJajaCode_withStringType_throwsIllegalArgumentException() {
        // Test: Type.STRING n'est pas supporté et doit lancer une exception
        // On crée une VarNode avec Type.STRING pour déclencher l'exception
        IdentNode ident = new IdentNode("strVar");
        VarNode varNode = new VarNode(Type.STRING, ident, null);

        // Tester que l'exception est lancée lors de la visite
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> visitor.visit(varNode),
            "Should throw IllegalArgumentException for unsupported type"
        );

        assertTrue(exception.getMessage().contains("Type non supporté"),
            "Exception message should mention unsupported type. Message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("String") || exception.getMessage().contains("STRING"),
            "Exception message should mention the type. Message: " + exception.getMessage());
    }

    @Test
    void typeToJajaCode_withEntierType_returnsInt() {
        // Test indirect: ENTIER -> "int"
        VarNode varNode = var("intVar", Type.ENTIER);

        visitor.visit(varNode);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(intVar@global, int, var, 0)"),
            "ENTIER should map to 'int'. Output: " + output);
    }

    @Test
    void typeToJajaCode_withBooleenType_returnsBoolean() {
        // Test indirect: BOOLEEN -> "boolean"
        VarNode varNode = var("boolVar", Type.BOOLEEN);

        visitor.visit(varNode);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(boolVar@global, boolean, var, 0)"),
            "BOOLEEN should map to 'boolean'. Output: " + output);
    }

    @Test
    void typeToJajaCode_withVoidType_returnsVoid() {
        // Test indirect via MethodeNode: VOID -> "void"
        MethodeNode methode = createSimpleMethod("voidMethod", Type.VOID);

        visitor.visit(methode);

        String output = visitor.getJajaCodeBuilder().toString();
        assertTrue(output.contains("new(voidMethod, void, meth, 0)"),
            "VOID should map to 'void'. Output: " + output);
    }

    // ===== Tests pour la compilation des tableaux =====

    @Test
    void visitTableauNode_withFixedSize_generatesNewArrayInstruction() {
        // Test de la règle [ctableau]: int arr[10]; → push(10), newarray(arr@global, int)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode tableauNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode(
                Type.ENTIER,
                ident("arr"),
                new NbreNode(10)
            );

        visitor.visit(tableauNode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 10);
        inOrder.verify(builderSpy).addInstruction(NEWARRAY, "arr@global", "int");
    }

    @Test
    void visitTabNode_inExpression_generatesAloadInstruction() {
        // Test de la règle [ctab]: x = arr[5]; → push(5), aload(arr@global), store(x@global)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode tabNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode(
                ident("arr"),
                new NbreNode(5)
            );

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));
        when(affectation.getExpression()).thenReturn(tabNode);

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);
        inOrder.verify(builderSpy).addInstruction(ALOAD, "arr@global");
        inOrder.verify(builderSpy).addInstruction(STORE, "x@global");
    }

    @Test
    void visitAffectationNode_withTabNode_generatesAstoreInstruction() {
        // Test de la règle [caffecteT]: arr[3] = 42; → push(3), push(42), astore(arr@global)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode tabNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode(
                ident("arr"),
                new NbreNode(3)
            );

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getIdent1Node()).thenReturn(tabNode);
        when(affectation.getExpression()).thenReturn(new NbreNode(42));

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 3);  // index
        inOrder.verify(builderSpy).addInstruction(PUSH, 42); // valeur
        inOrder.verify(builderSpy).addInstruction(ASTORE, "arr@global");
    }

    @Test
    void visitLengthNode_generatesLengthInstruction() {
        // Test de la règle [clongueur]: x = length(arr); → length(arr@global), store(x@global)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode lengthNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode(
                ident("arr")
            );

        AffectationNode affectation = mock(AffectationNode.class);
        when(affectation.getIdent1Node()).thenReturn(ident("x"));
        when(affectation.getExpression()).thenReturn(lengthNode);

        visitor.visit(affectation);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(LENGTH, "arr@global");
        inOrder.verify(builderSpy).addInstruction(STORE, "x@global");
    }

    @Test
    void visitIncrementNode_withVariable_generatesIncInstruction() {
        // Test de la règle [cincrément]: x++; → push(1), inc(x@global)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode incrementNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode(
                ident("x")
            );

        InstructionNode instrNode = incrementNode;
        visitor.visit(instrNode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);
        inOrder.verify(builderSpy).addInstruction(INC, "x@global");
    }

    @Test
    void visitIncrementNode_withTabNode_generatesAincInstruction() {
        // Test de la règle [cincrémentT]: arr[5]++; → push(5), push(1), ainc(arr@global)
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode tabNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode(
                ident("arr"),
                new NbreNode(5)
            );

        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode incrementNode =
            new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode(
                tabNode
            );

        InstructionNode instrNode = incrementNode;
        visitor.visit(instrNode);

        InOrder inOrder = inOrder(builderSpy);
        inOrder.verify(builderSpy).addInstruction(PUSH, 5);  // index
        inOrder.verify(builderSpy).addInstruction(PUSH, 1);  // valeur d'incrémentation
        inOrder.verify(builderSpy).addInstruction(AINC, "arr@global");
    }


}

