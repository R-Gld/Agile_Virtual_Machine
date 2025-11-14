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

        lenient().when(stacks.pop()).thenAnswer(invocation -> mockStack.isEmpty() ? null : mockStack.pop());

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
            @Override
            public Token getSymbol() {
                return null;
            }

            @Override
            public String getText() {
                return text;
            }

            @Override
            public ParseTree getParent() {
                return null;
            }

            @Override
            public Object getPayload() {
                return null;
            }

            @Override
            public Interval getSourceInterval() {
                return Interval.INVALID;
            }

            @Override
            public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
                return null;
            }

            @Override
            public String toStringTree(Parser parser) {
                return text;
            }

            @Override
            public String toStringTree() {
                return text;
            }

            @Override
            public int getChildCount() {
                return 0;
            }

            @Override
            public ParseTree getChild(int i) {
                return null;
            }

            @Override
            public void setParent(RuleContext ruleContext) {
            }

            @Override
            public String toString() {
                return text;
            }
        };
    }

    private JajaCodeParser.InstrContext instrWithPush(int value) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.NOMBRE()).thenReturn(term(Integer.toString(value)));
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

    // ------------------------
    // Tests INIT
    // ------------------------

    @Test
    void visitInstr_init_executesSuccessfully() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.INIT()).thenReturn(term("init"));

        visitor.visitInstr(ctx);

        // INIT ne fait aucun appel à stacks
        verify(stacks, never()).push(any());
        verify(stacks, never()).pop();
    }

    // ------------------------
    // Tests PUSH avec différentes valeurs
    // ------------------------

    @Test
    void visitInstr_push_withZero() {
        visitor.visitInstr(instrWithPush(0));
        verify(stacks).push(argThat(quad -> quad.value.equals(0)));
    }

    @Test
    void visitInstr_push_withNegativeValue() {
        visitor.visitInstr(instrWithPush(-42));
        verify(stacks).push(argThat(quad -> quad.value.equals(-42)));
    }

    @Test
    void visitInstr_push_withLargeValue() {
        visitor.visitInstr(instrWithPush(999999));
        verify(stacks).push(argThat(quad -> quad.value.equals(999999)));
    }

    @Test
    void visitInstr_push_multipleValues() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithPush(3));

        verify(stacks, times(3)).push(any(Stacks.Quad.class));
    }

    @Test
    void visitInstr_push_booleanTrue() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.TRUE()).thenReturn(term("true"));
        when(ctx.valeur()).thenReturn(valeur);

        visitor.visitInstr(ctx);

        verify(stacks).push(argThat(quad -> Boolean.TRUE.equals(quad.value)));
    }

    @Test
    void visitInstr_push_booleanFalse() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.FALSE()).thenReturn(term("false"));
        when(ctx.valeur()).thenReturn(valeur);

        visitor.visitInstr(ctx);

        verify(stacks).push(argThat(quad -> Boolean.FALSE.equals(quad.value)));
    }

    @Test
    void visitInstr_push_vide() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.VIDE()).thenReturn(term("vide"));
        when(ctx.valeur()).thenReturn(valeur);

        visitor.visitInstr(ctx);

        verify(stacks).push(argThat(quad -> quad.value == null));
    }

    // ------------------------
    // Tests NEW avec différentes sortes
    // ------------------------

    @Test
    void visitInstr_newVar_withInt() {
        visitor.visitInstr(instrWithPush(10));
        visitor.visitInstr(instrWithNew("count", "int", "var"));

        verify(stacks).declareVar("count", 10, "int");
    }

    @Test
    void visitInstr_newVar_withBoolean() {
        JajaCodeParser.InstrContext pushCtx = mock(JajaCodeParser.InstrContext.class);
        when(pushCtx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.TRUE()).thenReturn(term("true"));
        when(pushCtx.valeur()).thenReturn(valeur);

        visitor.visitInstr(pushCtx);
        visitor.visitInstr(instrWithNew("flag", "bool", "var"));

        verify(stacks).declareVar("flag", true, "bool");
    }

    @Test
    void visitInstr_newCst_declaresConstant() {
        visitor.visitInstr(instrWithPush(42));
        visitor.visitInstr(instrWithNew("MAX", "int", "cst"));

        verify(stacks).declareCst("MAX", 42, "int");
        verify(stacks, never()).declareVar(anyString(), any(), anyString());
    }

    @Test
    void visitInstr_newTab_declaresArray() {
        visitor.visitInstr(instrWithPush(10)); // taille du tableau
        visitor.visitInstr(instrWithNew("array", "int", "tab"));

        verify(stacks).declareTab("array", 10, "int");
    }

    @Test
    void visitInstr_newMeth_declaresMethod() {
        visitor.visitInstr(instrWithPush(100)); // adresse de la méthode
        visitor.visitInstr(instrWithNew("main", "void", "meth"));

        verify(stacks).declareCst("main", 100, "void");
    }

    @Test
    void visitInstr_new_withVariableKeyword() {
        visitor.visitInstr(instrWithPush(5));
        visitor.visitInstr(instrWithNew("x", "int", "variable")); // VARIABLE au lieu de VAR

        verify(stacks).declareVar("x", 5, "int");
    }

    @Test
    void visitInstr_new_multipleVariables() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithNew("a", "int", "var"));

        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithNew("b", "int", "var"));

        visitor.visitInstr(instrWithPush(3));
        visitor.visitInstr(instrWithNew("c", "int", "var"));

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).declareVar("a", 1, "int");
        inOrder.verify(stacks).declareVar("b", 2, "int");
        inOrder.verify(stacks).declareVar("c", 3, "int");
    }


    // ------------------------
    // Tests STORE
    // ------------------------

    @Test
    void visitInstr_store_storesValue() {
        visitor.visitInstr(instrWithPush(100));
        when(stacks.AffecterVal("result", 100)).thenReturn(true);

        visitor.visitInstr(instrWithStore("result"));

        verify(stacks).AffecterVal("result", 100);
    }

    // ------------------------
    // Tests SWAP
    // ------------------------

    @Test
    void visitInstr_swap_exchangesTwoValues() {
        visitor.visitInstr(instrWithPush(10));
        visitor.visitInstr(instrWithPush(20));
        visitor.visitInstr(instrWithSwap());

        verify(stacks).swap();
    }

    @Test
    void visitInstr_swap_multipleSwaps() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithSwap());
        visitor.visitInstr(instrWithSwap()); // Remet dans l'ordre original

        verify(stacks, times(2)).swap();
    }

    // ------------------------
    // Tests POP
    // ------------------------

    @Test
    void visitInstr_pop_removesTopValue() {
        visitor.visitInstr(instrWithPush(99));
        visitor.visitInstr(instrWithPop());

        verify(stacks).pop();
    }

    @Test
    void visitInstr_pop_multipleValues() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithPush(3));
        visitor.visitInstr(instrWithPop());
        visitor.visitInstr(instrWithPop());

        verify(stacks, times(2)).pop();
    }

    // ------------------------
    // Tests des opérations arithmétiques (via visitOper2)
    // ------------------------

    @Test
    void visitOper2_add_addsValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.ADD()).thenReturn(term("add"));

        // Empiler deux valeurs
        visitor.visitInstr(instrWithPush(10));
        visitor.visitInstr(instrWithPush(5));

        visitor.visitOper2(ctx);

        // Vérifie que deux valeurs ont été dépilées et le résultat empilé
        verify(stacks, atLeast(2)).pop();
        verify(stacks, atLeast(3)).push(any(Stacks.Quad.class)); // 2 push + 1 résultat
    }

    @Test
    void visitOper2_sub_subtractsValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUB()).thenReturn(term("sub"));

        visitor.visitInstr(instrWithPush(20));
        visitor.visitInstr(instrWithPush(8));

        visitor.visitOper2(ctx);

        verify(stacks, atLeast(2)).pop();
        verify(stacks, atLeast(3)).push(any(Stacks.Quad.class));
    }

    @Test
    void visitOper2_mul_multipliesValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.MUL()).thenReturn(term("mul"));

        visitor.visitInstr(instrWithPush(6));
        visitor.visitInstr(instrWithPush(7));

        visitor.visitOper2(ctx);

        verify(stacks, atLeast(2)).pop();
        verify(stacks, atLeast(3)).push(any(Stacks.Quad.class));
    }

    @Test
    void visitOper2_div_dividesValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.DIV()).thenReturn(term("div"));

        visitor.visitInstr(instrWithPush(20));
        visitor.visitInstr(instrWithPush(4));

        visitor.visitOper2(ctx);

        verify(stacks, atLeast(2)).pop();
        verify(stacks, atLeast(3)).push(any(Stacks.Quad.class));
    }

    // ------------------------
    // Tests des opérations unaires (via visitOper1)
    // ------------------------

    @Test
    void visitOper1_neg_negatesValue() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NEG()).thenReturn(term("neg"));

        visitor.visitInstr(instrWithPush(42));

        visitor.visitOper1(ctx);

        verify(stacks, atLeast(1)).pop();
        verify(stacks, atLeast(2)).push(any(Stacks.Quad.class));
    }

    @Test
    void visitOper1_not_negatesBoolean() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NOT()).thenReturn(term("not"));

        JajaCodeParser.InstrContext pushCtx = mock(JajaCodeParser.InstrContext.class);
        when(pushCtx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.TRUE()).thenReturn(term("true"));
        when(pushCtx.valeur()).thenReturn(valeur);

        visitor.visitInstr(pushCtx);
        visitor.visitOper1(ctx);

        verify(stacks, atLeast(1)).pop();
        verify(stacks, atLeast(2)).push(any(Stacks.Quad.class));
    }

    // ------------------------
    // Tests de scénarios complets
    // ------------------------

    @Test
    void scenario_declareAndAssignVariable() {
        // int x = 0; x = 5;
        visitor.visitInstr(instrWithPush(0));
        visitor.visitInstr(instrWithNew("x", "int", "var"));

        visitor.visitInstr(instrWithPush(5));
        when(stacks.AffecterVal("x", 5)).thenReturn(true);
        visitor.visitInstr(instrWithStore("x"));

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).declareVar("x", 0, "int");
        inOrder.verify(stacks).AffecterVal("x", 5);
    }


    @Test
    void scenario_multipleDeclarations() {
        // int a = 1; int b = 2; int c = 3;
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithNew("a", "int", "var"));

        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithNew("b", "int", "var"));

        visitor.visitInstr(instrWithPush(3));
        visitor.visitInstr(instrWithNew("c", "int", "var"));

        verify(stacks).declareVar("a", 1, "int");
        verify(stacks).declareVar("b", 2, "int");
        verify(stacks).declareVar("c", 3, "int");
    }

    @Test
    void scenario_stackManipulation() {
        // push(1), push(2), swap, pop -> reste 1 sur la pile
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithSwap());
        visitor.visitInstr(instrWithPop());

        verify(stacks).swap();
        verify(stacks, times(1)).pop();
        verify(stacks, times(2)).push(any(Stacks.Quad.class));
    }


    // ------------------------
    // Tests de cas d'erreur
    // ------------------------

    @Test
    void scenario_storeToNonExistentVariable_fails() {
        visitor.visitInstr(instrWithPush(99));
        when(stacks.AffecterVal("unknown", 99)).thenReturn(false);

        visitor.visitInstr(instrWithStore("unknown"));

        verify(stacks).AffecterVal("unknown", 99);
    }

    @Test
    void scenario_arrayDeclaration() {
        // Déclaration d'un tableau de 10 éléments
        visitor.visitInstr(instrWithPush(10));
        visitor.visitInstr(instrWithNew("numbers", "int", "tab"));

        verify(stacks).declareTab("numbers", 10, "int");
    }

    @Test
    void scenario_constantDeclaration() {
        // const PI = 314 (simplifié)
        visitor.visitInstr(instrWithPush(314));
        visitor.visitInstr(instrWithNew("PI", "int", "cst"));

        verify(stacks).declareCst("PI", 314, "int");
    }

    @Test
    void scenario_booleanOperations() {
        // Empiler deux booléens
        JajaCodeParser.InstrContext pushTrue = createPushBoolean(true);
        JajaCodeParser.InstrContext pushFalse = createPushBoolean(false);

        visitor.visitInstr(pushTrue);
        visitor.visitInstr(pushFalse);

        verify(stacks, times(2)).push(any(Stacks.Quad.class));
    }

    // Helper pour créer PUSH(boolean)
    private JajaCodeParser.InstrContext createPushBoolean(boolean value) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        if (value) {
            when(valeur.TRUE()).thenReturn(term("true"));
        } else {
            when(valeur.FALSE()).thenReturn(term("false"));
        }
        when(ctx.valeur()).thenReturn(valeur);
        return ctx;
    }

    // ------------------------
    // Tests de séquences d'instructions
    // ------------------------

    @Test
    void sequence_initPushNewStoreSwapPop() {
        // Séquence typique: init, push(0), new(x), push(5), store(x), push(0), swap, pop, pop
        JajaCodeParser.InstrContext initCtx = mock(JajaCodeParser.InstrContext.class);
        when(initCtx.INIT()).thenReturn(term("init"));

        when(stacks.AffecterVal("x", 5)).thenReturn(true);

        visitor.visitInstr(initCtx);
        visitor.visitInstr(instrWithPush(0));
        visitor.visitInstr(instrWithNew("x", "int", "var"));
        visitor.visitInstr(instrWithPush(5));
        visitor.visitInstr(instrWithStore("x"));
        visitor.visitInstr(instrWithPush(0));
        visitor.visitInstr(instrWithSwap());
        visitor.visitInstr(instrWithPop());
        visitor.visitInstr(instrWithPop());

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).push(any()); // push(0)
        inOrder.verify(stacks).pop(); // new consomme le 0
        inOrder.verify(stacks).declareVar("x", 0, "int");
        inOrder.verify(stacks).push(any()); // push(5)
        inOrder.verify(stacks).pop(); // store consomme le 5
        inOrder.verify(stacks).AffecterVal("x", 5);
        inOrder.verify(stacks).push(any()); // push(0) final
        inOrder.verify(stacks).swap();
        inOrder.verify(stacks, times(2)).pop(); // 2 pops finaux
    }
}
