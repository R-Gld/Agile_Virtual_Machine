package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JajaCodeInterpreterVisitorUnitTest {

    @Mock
    private Stacks stacks;

    private JajaCodeInterpreterVisitor visitor;

    // Stack interne pour simuler le comportement push/pop du mock
    private java.util.Stack<Stacks.Quad> mockStack;

    @BeforeEach
    void setUp() {
        mockStack = new java.util.Stack<>();

        // Configurer le mock pour simuler push() et pop()
        // Utiliser lenient() car ces stubs ne sont pas utilisés par tous les tests
        lenient().doAnswer(invocation -> {
            Stacks.Quad quad = invocation.getArgument(0);
            mockStack.push(quad);
            return null;
        }).when(stacks).push(any(Stacks.Quad.class));

        lenient().when(stacks.pop()).thenAnswer(invocation ->
            mockStack.isEmpty() ? null : mockStack.pop()
        );

        lenient().doAnswer(invocation -> {
            if (mockStack.size() >= 2) {
                Stacks.Quad q1 = mockStack.pop();
                Stacks.Quad q2 = mockStack.pop();
                mockStack.push(q1);
                mockStack.push(q2);
            }
            return null;
        }).when(stacks).swap();

        visitor = new JajaCodeInterpreterVisitor(stacks);
    }

    // ------------------------
    // Helpers for mocking ANTLR ctx
    // ------------------------

    private TerminalNode term(String text) {
        /* Partial reimplementation of TerminalNode to avoid UnfinishedStubbing from mockito.*/
        return new TerminalNode() {
            @Override public Token getSymbol() { return null; }
            @Override public String getText() { return text; }
            @Override public ParseTree getParent() { return null; }
            @Override public Object getPayload() { return null; }
            @Override public Interval getSourceInterval() { return Interval.INVALID; }
            @Override public <T> T accept(ParseTreeVisitor<? extends T> visitor) { return null; }
            @Override public String toStringTree(Parser parser) { return text; }
            @Override public String toStringTree() { return text; }
            @Override public int getChildCount() { return 0; }
            @Override public ParseTree getChild(int i) { return null; }
            @Override public void setParent(RuleContext ruleContext) {}
            @Override public String toString() { return text; }
        };
    }

    private JajaCodeParser.InstrContext instrWithPush(int value) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.getText()).thenReturn(Integer.toString(value));
        when(ctx.valeur()).thenReturn(valeur);
        return ctx;
    }

    private JajaCodeParser.InstrContext instrWithPop() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.POP()).thenReturn(term("pop"));
        return ctx;
    }

    private JajaCodeParser.InstrContext instrWithSwap() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.SWAP()).thenReturn(term("swap"));
        return ctx;
    }

    private JajaCodeParser.InstrContext instrWithNew(String ident, String type, String kind) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.NEW()).thenReturn(term("new"));

        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);
        when(identCtx.getText()).thenReturn(ident);
        when(ctx.ident()).thenReturn(identCtx);

        when(ctx.TYPE()).thenReturn(term(type));
        when(ctx.SORTE()).thenReturn(term(kind));
        return ctx;
    }

    private JajaCodeParser.InstrContext instrWithStore(String ident) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.STORE()).thenReturn(term("store"));
        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);
        when(identCtx.getText()).thenReturn(ident);
        when(ctx.ident()).thenReturn(identCtx);
        return ctx;
    }

    // ------------------------
    // visitValeur tests
    // ------------------------

    @Test
    void visitValeur_returnsInteger_whenNombre() {
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.NOMBRE()).thenReturn(term("123"));
        Object res = visitor.visitValeur(valeur);
        assertEquals(123, res);
    }

    @Test
    void visitValeur_returnsTrue_whenTRUE() {
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.TRUE()).thenReturn(term("true"));
        Object res = visitor.visitValeur(valeur);
        assertEquals(Boolean.TRUE, res);
    }

    @Test
    void visitValeur_returnsFalse_whenFALSE() {
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.FALSE()).thenReturn(term("false"));
        Object res = visitor.visitValeur(valeur);
        assertEquals(Boolean.FALSE, res);
    }

    @Test
    void visitValeur_returnsNull_whenVIDE() {
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.VIDE()).thenReturn(term("vide"));
        Object res = visitor.visitValeur(valeur);
        assertNull(res);
    }

    @Test
    void visitValeur_throws_whenUnsupported() {
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.getText()).thenReturn("<unsupported>");
        assertThrows(UnsupportedOperationException.class, () -> visitor.visitValeur(valeur));
    }

    // ------------------------
    // PUSH + NEW (var) path
    // ------------------------

    @Test
    void visitInstr_newVar_declaresVar_withTopValue() {
        // Given a value pushed on the interpreter stack
        visitor.visitInstr(instrWithPush(5));

        // When NEW var x:int is executed
        visitor.visitInstr(instrWithNew("x", "int", "var"));

        // Then Stacks.declareVar is called with the popped value
        verify(stacks).declareVar(eq("x"), eq(5), eq("int"));

        // And declareCst are not called here
        verify(stacks, never()).declareCst(anyString(), any(), anyString());
    }

    @Test
    void visitInstr_newMeth_declaresCst_withTopValue() {
        visitor.visitInstr(instrWithPush(8));
        visitor.visitInstr(instrWithNew("f", "int", "meth"));
        verify(stacks).declareCst(eq("f"), eq(8), eq("int"));
        verify(stacks, never()).declareVar(anyString(), any(), anyString());
    }

    // ------------------------
    // PUSH + SWAP + STORE path (indirectly tests stack order)
    // ------------------------

    @Test
    void visitInstr_swap_thenStore_assignsInSwappedOrder() {
        // Push two values
        visitor.visitInstr(instrWithPush(1)); // bottom
        visitor.visitInstr(instrWithPush(2)); // top

        // Swap -> top becomes 1
        visitor.visitInstr(instrWithSwap());

        // Prepare Stacks to say both symbols exist (AffecterVal returns true)
        when(stacks.AffecterVal(anyString(), any())).thenReturn(true);

        // Store into x then y; due to swap, x gets 1 then y gets 2
        visitor.visitInstr(instrWithStore("x"));
        visitor.visitInstr(instrWithStore("y"));

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).AffecterVal(eq("x"), eq(1));
        inOrder.verify(stacks).AffecterVal(eq("y"), eq(2));
    }

    // ------------------------
    // STORE error path
    // ------------------------

    @Test
    void visitInstr_store_whenSymbolMissing_doesNotAssign() {
        visitor.visitInstr(instrWithPush(42));
        when(stacks.AffecterVal("z", 42)).thenReturn(false); // missing symbol or error

        visitor.visitInstr(instrWithStore("z"));

        verify(stacks).AffecterVal("z", 42);
    }

    // ------------------------
    // POP (smoke test: should only push/pop, no symbol management)
    // ------------------------

    @Test
    void visitInstr_pop_smoke() {
        visitor.visitInstr(instrWithPush(9));
        visitor.visitInstr(instrWithPop());

        // POP should push then pop, but not touch symbol management methods
        verify(stacks).push(any(Stacks.Quad.class));
        verify(stacks).pop();
        verify(stacks, never()).declareVar(anyString(), any(), anyString());
        verify(stacks, never()).declareCst(anyString(), any(), anyString());
        verify(stacks, never()).AffecterVal(anyString(), any());
    }
}
