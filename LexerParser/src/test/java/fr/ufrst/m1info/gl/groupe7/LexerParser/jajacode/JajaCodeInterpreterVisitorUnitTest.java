package fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.Memoire.Symbol;
import fr.ufrst.m1info.gl.groupe7.Memoire.SymbolTable;
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
    private SymbolTable symbolTable;

    private JajaCodeInterpreterVisitor visitor;

    @BeforeEach
    void setUp() {
        visitor = new JajaCodeInterpreterVisitor(symbolTable);
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

        // Then SymbolTable.declareVar is called with the popped value
        verify(symbolTable).creationVar(eq("x"), eq(5), eq("int"));

        // And declareCst / assign are not called here
        verify(symbolTable, never()).declareCst(anyString(), any(), anyString());
        verify(symbolTable, never()).assign(anyString(), any());
    }

    @Test
    void visitInstr_newMeth_declaresCst_withTopValue() {
        visitor.visitInstr(instrWithPush(8));
        visitor.visitInstr(instrWithNew("f", "int", "meth"));
        verify(symbolTable).declareCst(eq("f"), eq(8), eq("int"));
        verify(symbolTable, never()).creationVar(anyString(), any(), anyString());
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

        // Prepare SymbolTable to say both symbols exist
        Symbol s1 = mock(Symbol.class);
        Symbol s2 = mock(Symbol.class);
        when(symbolTable.findSymbol(anyString())).thenReturn(s1, s2);

        // Store into x then y; due to swap, x gets 1 then y gets 2
        visitor.visitInstr(instrWithStore("x"));
        visitor.visitInstr(instrWithStore("y"));

        InOrder inOrder = inOrder(symbolTable);
        inOrder.verify(symbolTable).findSymbol("x");
        inOrder.verify(symbolTable).assign(eq("x"), eq(1));
        inOrder.verify(symbolTable).findSymbol("y");
        inOrder.verify(symbolTable).assign(eq("y"), eq(2));
    }

    // ------------------------
    // STORE error path
    // ------------------------

    @Test
    void visitInstr_store_whenSymbolMissing_doesNotAssign() {
        visitor.visitInstr(instrWithPush(42));
        when(symbolTable.findSymbol("z")).thenReturn(null); // missing symbol

        visitor.visitInstr(instrWithStore("z"));

        verify(symbolTable).findSymbol("z");
        verify(symbolTable, never()).assign(anyString(), any());
    }

    // ------------------------
    // POP (smoke test: no interaction with SymbolTable)
    // ------------------------

    @Test
    void visitInstr_pop_smoke() {
        visitor.visitInstr(instrWithPush(9));
        visitor.visitInstr(instrWithPop());
        // POP should not touch the SymbolTable
        verifyNoInteractions(symbolTable);
    }
}
