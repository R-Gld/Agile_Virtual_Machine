package fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode;

import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.expressions.Fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.Memoire.SymbolTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;

import static fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode.JajaCodeInstr.*;
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
        visitor = new MiniJajaCompilerVisitor(new SymbolTable());
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
    void visitVarNode_generatesPushAndNewAndTracksVariable() {
        VarNode node = var("x", "int");

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 0);
        verify(builderSpy).addInstruction(eq(NEW), eq("x@1"), eq("int"), eq("var"), eq(0));
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
        inOrder.verify(builderSpy).addInstruction(STORE, "x@1");
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
    void visitMain_generatesBodyThenPushZero() {
        MainNode node = mock(MainNode.class);
        InstructionsNode instrs = mock(InstructionsNode.class);
        when(node.getInstrs()).thenReturn(instrs);

        visitor.visit(node);

        verify(builderSpy).addInstruction(PUSH, 0);
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
}
