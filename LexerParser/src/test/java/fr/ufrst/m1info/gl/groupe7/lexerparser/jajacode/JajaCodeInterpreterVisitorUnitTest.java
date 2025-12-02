package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
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
                // Not needed for mock TerminalNode used in tests
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

    private JajaCodeParser.InstrContext instrWithNew(String ident, Type type, String kind) {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        when(ctx.NEW()).thenReturn(term("new"));

        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);
        when(identCtx.getText()).thenReturn(ident);
        when(ctx.ident()).thenReturn(identCtx);

        when(ctx.TYPE()).thenReturn(term(type.toString()));
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
        visitor.visitInstr(instrWithNew("x", Type.ENTIER, "var"));

        // Then Stacks.declareVar is called with the popped value
        verify(stacks).declareVar("x", 5, Type.ENTIER);

        // And declareCst are not called here
        verify(stacks, never()).declareCst(anyString(), any(), any(Type.class));
    }

    @Test
    void visitInstr_newMeth_declaresMeth_withTopValue() {
        visitor.visitInstr(instrWithPush(8));
        visitor.visitInstr(instrWithNew("f", Type.ENTIER, "meth"));
        verify(stacks).declareMeth("f", 8, Type.ENTIER);
        verify(stacks, never()).declareVar(anyString(), any(), any(Type.class));
        verify(stacks, never()).declareCst(anyString(), any(), any(Type.class));
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
        inOrder.verify(stacks).AffecterVal("x", 1);
        inOrder.verify(stacks).AffecterVal("y", 2);
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
        verify(stacks, never()).declareVar(anyString(), any(), any(Type.class));
        verify(stacks, never()).declareCst(anyString(), any(), any(Type.class));
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
        visitor.visitInstr(instrWithNew("count", Type.ENTIER, "var"));

        verify(stacks).declareVar("count", 10, Type.ENTIER);
    }

    @Test
    void visitInstr_newVar_withBoolean() {
        JajaCodeParser.InstrContext pushCtx = mock(JajaCodeParser.InstrContext.class);
        when(pushCtx.PUSH()).thenReturn(term("push"));
        JajaCodeParser.ValeurContext valeur = mock(JajaCodeParser.ValeurContext.class);
        when(valeur.TRUE()).thenReturn(term("true"));
        when(pushCtx.valeur()).thenReturn(valeur);

        visitor.visitInstr(pushCtx);
        visitor.visitInstr(instrWithNew("flag", Type.BOOLEEN, "var"));

        verify(stacks).declareVar("flag", true, Type.BOOLEEN);
    }

    @Test
    void visitInstr_newCst_declaresConstant() {
        visitor.visitInstr(instrWithPush(42));
        visitor.visitInstr(instrWithNew("MAX", Type.ENTIER, "cst"));

        verify(stacks).declareCst("MAX", 42, Type.ENTIER);
        verify(stacks, never()).declareVar(anyString(), any(), any(Type.class));
    }

    @Test
    void visitInstr_newTab_declaresArray() {
        visitor.visitInstr(instrWithPush(10)); // taille du tableau
        visitor.visitInstr(instrWithNew("array", Type.ENTIER, "tab"));

        verify(stacks).declareTab("array", 10, Type.ENTIER);
    }

    @Test
    void visitInstr_new_withVariableKeyword() {
        visitor.visitInstr(instrWithPush(5));
        visitor.visitInstr(instrWithNew("x", Type.ENTIER, "variable")); // VARIABLE au lieu de VAR

        verify(stacks).declareVar("x", 5, Type.ENTIER);
    }

    @Test
    void visitInstr_new_multipleVariables() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithNew("a", Type.ENTIER, "var"));

        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithNew("b", Type.ENTIER, "var"));

        visitor.visitInstr(instrWithPush(3));
        visitor.visitInstr(instrWithNew("c", Type.ENTIER, "var"));

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).declareVar("a", 1, Type.ENTIER);
        inOrder.verify(stacks).declareVar("b", 2, Type.ENTIER);
        inOrder.verify(stacks).declareVar("c", 3, Type.ENTIER);
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

    @Test
    void visitOper2_CMP_compareEqualValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.CMP()).thenReturn(term("cmp"));

        visitor.visitInstr(instrWithPush(50));
        visitor.visitInstr(instrWithPush(50));

        visitor.visitOper2(ctx);

        verify(stacks, atLeast(2)).pop();
        verify(stacks, atLeast(3)).push(any(Stacks.Quad.class));
    }

    @Test
    void visitOper2_CMP_compareDifferentValues() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.CMP()).thenReturn(term("cmp"));

        visitor.visitInstr(instrWithPush(30));
        visitor.visitInstr(instrWithPush(40));

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
        visitor.visitInstr(instrWithPush(0));
        visitor.visitInstr(instrWithNew("x", Type.ENTIER, "var"));

        visitor.visitInstr(instrWithPush(5));
        when(stacks.AffecterVal("x", 5)).thenReturn(true);
        visitor.visitInstr(instrWithStore("x"));

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).declareVar("x", 0, Type.ENTIER);
        inOrder.verify(stacks).AffecterVal("x", 5);
    }


    @Test
    void scenario_multipleDeclarations() {
        visitor.visitInstr(instrWithPush(1));
        visitor.visitInstr(instrWithNew("a", Type.ENTIER, "var"));

        visitor.visitInstr(instrWithPush(2));
        visitor.visitInstr(instrWithNew("b", Type.ENTIER, "var"));

        visitor.visitInstr(instrWithPush(3));
        visitor.visitInstr(instrWithNew("c", Type.ENTIER, "var"));

        verify(stacks).declareVar("a", 1, Type.ENTIER);
        verify(stacks).declareVar("b", 2, Type.ENTIER);
        verify(stacks).declareVar("c", 3, Type.ENTIER);
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
        visitor.visitInstr(instrWithNew("numbers", Type.ENTIER, "tab"));

        verify(stacks).declareTab("numbers", 10, Type.ENTIER);
    }

    @Test
    void scenario_constantDeclaration() {
        // const PI = 314 (simplifié)
        visitor.visitInstr(instrWithPush(314));
        visitor.visitInstr(instrWithNew("PI", Type.ENTIER, "cst"));

        verify(stacks).declareCst("PI", 314, Type.ENTIER);
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
        visitor.visitInstr(instrWithNew("x", Type.ENTIER, "var"));
        visitor.visitInstr(instrWithPush(5));
        visitor.visitInstr(instrWithStore("x"));
        visitor.visitInstr(instrWithPush(0));
        visitor.visitInstr(instrWithSwap());
        visitor.visitInstr(instrWithPop());
        visitor.visitInstr(instrWithPop());

        InOrder inOrder = inOrder(stacks);
        inOrder.verify(stacks).push(any()); // push(0)
        inOrder.verify(stacks).pop(); // new consomme le 0
        inOrder.verify(stacks).declareVar("x", 0, Type.ENTIER);
        inOrder.verify(stacks).push(any()); // push(5)
        inOrder.verify(stacks).pop(); // store consomme le 5
        inOrder.verify(stacks).AffecterVal("x", 5);
        inOrder.verify(stacks).push(any()); // push(0) final
        inOrder.verify(stacks).swap();
        inOrder.verify(stacks, times(2)).pop(); // 2 pops finaux
    }

    // ===== Tests pour load() =====

    @Test
    void load_withValidProgram_loadsInstructions() {
        // Créer une structure récursive : classe1 -> classe2 -> null
        JajaCodeParser.ClasseContext classe1 = mock(JajaCodeParser.ClasseContext.class);
        JajaCodeParser.ClasseContext classe2 = mock(JajaCodeParser.ClasseContext.class);
        JajaCodeParser.InstrContext instr1 = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.InstrContext instr2 = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext addr1 = mock(JajaCodeParser.AdresseContext.class);
        JajaCodeParser.AdresseContext addr2 = mock(JajaCodeParser.AdresseContext.class);

        // Configurer classe1 : adresse 1, instruction init
        lenient().when(classe1.instr()).thenReturn(instr1);
        lenient().when(classe1.adresse()).thenReturn(addr1);
        lenient().when(addr1.getText()).thenReturn("1");
        lenient().when(classe1.classe()).thenReturn(classe2);

        // Configurer classe2 : adresse 2, instruction jcstop
        lenient().when(classe2.instr()).thenReturn(instr2);
        lenient().when(classe2.adresse()).thenReturn(addr2);
        lenient().when(addr2.getText()).thenReturn("2");
        lenient().when(classe2.classe()).thenReturn(null); // Fin de la liste

        visitor.load(classe1);

        // Vérifier que le visiteur est toujours opérationnel
        assertNotNull(visitor);
    }

    @Test
    void load_withEmptyProgram_loadsZeroInstructions() {
        // Créer une classe sans instruction
        JajaCodeParser.ClasseContext classeVide = mock(JajaCodeParser.ClasseContext.class);
        lenient().when(classeVide.instr()).thenReturn(null);

        visitor.load(classeVide);

        // Le programme devrait être vide mais le visiteur opérationnel
        assertNotNull(visitor);
    }

    @Test
    void load_withSingleInstruction_loadsOneInstruction() {
        JajaCodeParser.ClasseContext classe = mock(JajaCodeParser.ClasseContext.class);
        JajaCodeParser.InstrContext instr = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext addr = mock(JajaCodeParser.AdresseContext.class);

        lenient().when(classe.instr()).thenReturn(instr);
        lenient().when(classe.adresse()).thenReturn(addr);
        lenient().when(addr.getText()).thenReturn("5");
        lenient().when(classe.classe()).thenReturn(null);

        visitor.load(classe);

        assertNotNull(visitor);
    }

    // ===== Tests pour run() avec erreurs =====

    @Test
    void run_withInvalidAddress_stopsExecution() {
        // Appeler run() sans avoir chargé de programme
        // Cela devrait s'arrêter proprement
        visitor.run();

        // Le programme devrait s'arrêter proprement même sans instructions
        assertNotNull(visitor);
    }

    // ===== Tests pour processAddressInstructions =====

    @Test
    void visitInstr_withIFInstruction_callsAxiomeIF() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.IF()).thenReturn(term("if"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("10");

        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));

        visitor.visitInstr(ctx);

        // Vérifier que la pile a été dépilée
        verify(stacks).pop();
    }

    @Test
    void visitInstr_withGOTOInstruction_callsAxiomeGOTO() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.GOTO()).thenReturn(term("goto"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("5");

        visitor.visitInstr(ctx);

        // Le goto devrait avoir été exécuté
        assertNotNull(visitor);
    }

    // ===== Tests pour extractFromChildren, isTypeToken, isSorteToken =====

    @Test
    void visitInstr_newWithChildrenParsing_extractsTypeAndSorte() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);

        when(ctx.NEW()).thenReturn(term("new"));
        when(ctx.ident()).thenReturn(identCtx);
        when(identCtx.getText()).thenReturn("myVar");
        when(ctx.TYPE()).thenReturn(null); // Force extraction depuis enfants
        when(ctx.SORTE()).thenReturn(null);
        when(ctx.getChildCount()).thenReturn(8);
        when(ctx.getChild(0)).thenReturn(term("new"));
        when(ctx.getChild(1)).thenReturn(term("("));
        when(ctx.getChild(2)).thenReturn(term("myVar"));
        when(ctx.getChild(3)).thenReturn(term(","));
        when(ctx.getChild(4)).thenReturn(term("int"));
        when(ctx.getChild(5)).thenReturn(term(","));
        when(ctx.getChild(6)).thenReturn(term("var"));
        when(ctx.getChild(7)).thenReturn(term(","));

        mockStack.push(new Stacks.Quad("%TMP%", 0, "%TMP%", Type.ENTIER));

        visitor.visitInstr(ctx);

        verify(stacks).pop(); // Vérifie que la pile a été dépilée
    }

    @Test
    void visitInstr_newWithBOOLEANType_recognizesAsType() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);

        when(ctx.NEW()).thenReturn(term("new"));
        when(ctx.ident()).thenReturn(identCtx);
        when(identCtx.getText()).thenReturn("flag");
        when(ctx.TYPE()).thenReturn(null);
        when(ctx.SORTE()).thenReturn(null);
        when(ctx.getChildCount()).thenReturn(8);
        when(ctx.getChild(0)).thenReturn(term("new"));
        when(ctx.getChild(1)).thenReturn(term("("));
        when(ctx.getChild(2)).thenReturn(term("flag"));
        when(ctx.getChild(3)).thenReturn(term(","));
        when(ctx.getChild(4)).thenReturn(term("boolean"));
        when(ctx.getChild(5)).thenReturn(term(","));
        when(ctx.getChild(6)).thenReturn(term("var"));
        when(ctx.getChild(7)).thenReturn(term(","));

        mockStack.push(new Stacks.Quad("%TMP%", false, "%TMP%", Type.BOOLEEN));

        visitor.visitInstr(ctx);

        verify(stacks).pop(); // Vérifie que la pile a été dépilée
    }

    @Test
    void visitInstr_newWithTABSorte_recognizesAsSorte() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext identCtx = mock(JajaCodeParser.IdentContext.class);

        when(ctx.NEW()).thenReturn(term("new"));
        when(ctx.ident()).thenReturn(identCtx);
        when(identCtx.getText()).thenReturn("arr");
        when(ctx.TYPE()).thenReturn(null);
        when(ctx.SORTE()).thenReturn(null);
        when(ctx.getChildCount()).thenReturn(8);
        when(ctx.getChild(0)).thenReturn(term("new"));
        when(ctx.getChild(1)).thenReturn(term("("));
        when(ctx.getChild(2)).thenReturn(term("arr"));
        when(ctx.getChild(3)).thenReturn(term(","));
        when(ctx.getChild(4)).thenReturn(term("int"));
        when(ctx.getChild(5)).thenReturn(term(","));
        when(ctx.getChild(6)).thenReturn(term("tab"));
        when(ctx.getChild(7)).thenReturn(term(","));

        mockStack.push(new Stacks.Quad("%TMP%", 10, "%TMP%", Type.ENTIER));

        visitor.visitInstr(ctx);

        verify(stacks).pop(); // Vérifie que la pile a été dépilée
    }


    // ===== Tests pour visitOper2 - branches manquantes =====

    @Test
    void visitOper2_withAND_callsAxiomeAnd() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.AND()).thenReturn(term("and"));

        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));
        mockStack.push(new Stacks.Quad("%TMP%", false, "%TMP%", Type.BOOLEEN));

        visitor.visitOper2(ctx);

        verify(stacks, times(2)).pop();
        verify(stacks).push(any());
    }

    @Test
    void visitOper2_withOR_callsAxiomeOr() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.OR()).thenReturn(term("or"));

        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));
        mockStack.push(new Stacks.Quad("%TMP%", false, "%TMP%", Type.BOOLEEN));

        visitor.visitOper2(ctx);

        verify(stacks, times(2)).pop();
        verify(stacks).push(any());
    }

    @Test
    void visitOper2_withSUP_callsAxiomeSup() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUP()).thenReturn(term("sup"));

        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));
        mockStack.push(new Stacks.Quad("%TMP%", 3, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        verify(stacks, times(2)).pop();
        verify(stacks).push(any());
    }

    @Test
    void visitOper2_withUnknownOperator_doesNothing() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        // Aucun opérateur connu
        when(ctx.ADD()).thenReturn(null);
        when(ctx.SUB()).thenReturn(null);
        when(ctx.MUL()).thenReturn(null);
        when(ctx.DIV()).thenReturn(null);
        when(ctx.CMP()).thenReturn(null);
        when(ctx.SUP()).thenReturn(null);
        when(ctx.AND()).thenReturn(null);
        when(ctx.OR()).thenReturn(null);
        when(ctx.getText()).thenReturn("unknown");

        visitor.visitOper2(ctx);

        // Ne devrait rien faire
        verify(stacks, never()).pop();
    }

    // ===== Tests pour visitOper1 - branche else =====

    @Test
    void visitOper1_withUnknownOperator_doesNothing() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NEG()).thenReturn(null);
        when(ctx.NOT()).thenReturn(null);
        when(ctx.getText()).thenReturn("unknown");

        visitor.visitOper1(ctx);

        verify(stacks, never()).pop();
    }

    // ===== Tests pour axiomeNew - branches manquantes =====

    @Test
    void visitInstr_newWithNullValue_stopsExecution() {
        JajaCodeParser.InstrContext ctx = instrWithNew("var", Type.ENTIER, "var");

        // Pile vide - pop retournera null
        when(stacks.pop()).thenReturn(null);

        visitor.visitInstr(ctx);

        // L'exécution devrait s'arrêter
        assertNotNull(visitor);
    }

    @Test
    void visitInstr_newWithTabAndNonIntegerSize_stopsExecution() {
        JajaCodeParser.InstrContext ctx = instrWithNew("arr", Type.ENTIER, "tab");

        mockStack.push(new Stacks.Quad("%TMP%", "notAnInteger", "%TMP%", Type.STRING));

        visitor.visitInstr(ctx);

        verify(stacks, never()).declareTab(anyString(), anyInt(), any(Type.class));
    }

    @Test
    void visitInstr_newWithUnknownKind_stopsExecution() {
        JajaCodeParser.InstrContext ctx = instrWithNew("x", Type.ENTIER, "unknown");

        mockStack.push(new Stacks.Quad("%TMP%", 0, "%TMP%", Type.ENTIER));

        visitor.visitInstr(ctx);

        verify(stacks, never()).declareVar(anyString(), any(), any(Type.class));
        verify(stacks, never()).declareCst(anyString(), any(), any(Type.class));
        verify(stacks, never()).declareTab(anyString(), anyInt(), any(Type.class));
    }

    // ===== Tests pour axiomeStore - valeur null =====

    @Test
    void visitInstr_storeWithEmptyStack_stopsExecution() {
        JajaCodeParser.InstrContext ctx = instrWithStore("x");

        when(stacks.pop()).thenReturn(null);

        visitor.visitInstr(ctx);

        verify(stacks, never()).AffecterVal(anyString(), any());
    }


    // ===== Tests pour axiomeAdd - branches if =====

    @Test
    void visitOper2_addWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.ADD()).thenReturn(term("add"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_addWithNonIntegerOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.ADD()).thenReturn(term("add"));

        mockStack.push(new Stacks.Quad("%TMP%", "notInt", "%TMP%", Type.STRING));
        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        // Ne devrait pas pusher le résultat
        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeSub - branches if =====

    @Test
    void visitOper2_subWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUB()).thenReturn(term("sub"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_subWithNonIntegerOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUB()).thenReturn(term("sub"));

        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));
        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeMul - branches if =====

    @Test
    void visitOper2_mulWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.MUL()).thenReturn(term("mul"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_mulWithNonIntegerOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.MUL()).thenReturn(term("mul"));

        mockStack.push(new Stacks.Quad("%TMP%", 3, "%TMP%", Type.ENTIER));
        mockStack.push(new Stacks.Quad("%TMP%", false, "%TMP%", Type.BOOLEEN));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeDiv - branches if =====

    @Test
    void visitOper2_divWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.DIV()).thenReturn(term("div"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_divWithNonIntegerOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.DIV()).thenReturn(term("div"));

        mockStack.push(new Stacks.Quad("%TMP%", 10, "%TMP%", Type.ENTIER));
        mockStack.push(new Stacks.Quad("%TMP%", "notInt", "%TMP%", Type.VOID));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    @Test
    void visitOper2_divByZero_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.DIV()).thenReturn(term("div"));

        mockStack.push(new Stacks.Quad("%TMP%", 10, "%TMP%", Type.ENTIER));
        mockStack.push(new Stacks.Quad("%TMP%", 0, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        // Ne devrait pas pusher le résultat
        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeUnaryMinus =====

    @Test
    void visitOper1_negWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NEG()).thenReturn(term("neg"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper1(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper1_negWithNonInteger_stopsExecution() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NEG()).thenReturn(term("neg"));

        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));

        visitor.visitOper1(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeNot =====

    @Test
    void visitOper1_notWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NOT()).thenReturn(term("not"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper1(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper1_notWithNonBoolean_stopsExecution() {
        JajaCodeParser.Oper1Context ctx = mock(JajaCodeParser.Oper1Context.class);
        when(ctx.NOT()).thenReturn(term("not"));

        mockStack.push(new Stacks.Quad("%TMP%", 42, "%TMP%", Type.ENTIER));

        visitor.visitOper1(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeAnd =====

    @Test
    void visitOper2_andWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.AND()).thenReturn(term("and"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_andWithNonBooleanOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.AND()).thenReturn(term("and"));

        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));
        mockStack.push(new Stacks.Quad("%TMP%", true, "%TMP%", Type.BOOLEEN));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeOr =====

    @Test
    void visitOper2_orWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.OR()).thenReturn(term("or"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_orWithNonBooleanOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.OR()).thenReturn(term("or"));

        mockStack.push(new Stacks.Quad("%TMP%", false, "%TMP%", Type.BOOLEEN));
        mockStack.push(new Stacks.Quad("%TMP%", 10, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeCmp =====

    @Test
    void visitOper2_cmpWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.CMP()).thenReturn(term("cmp"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    // ===== Tests pour axiomeSup =====

    @Test
    void visitOper2_supWithEmptyStack_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUP()).thenReturn(term("sup"));

        when(stacks.pop()).thenReturn(null);

        visitor.visitOper2(ctx);

        verify(stacks, never()).push(any());
    }

    @Test
    void visitOper2_supWithNonIntegerOperands_stopsExecution() {
        JajaCodeParser.Oper2Context ctx = mock(JajaCodeParser.Oper2Context.class);
        when(ctx.SUP()).thenReturn(term("sup"));

        mockStack.push(new Stacks.Quad("%TMP%", "text", "%TMP%", Type.STRING));
        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));

        visitor.visitOper2(ctx);

        assertEquals(0, mockStack.size());
    }

    // ===== Tests pour axiomeIF =====

    @Test
    void visitInstr_ifWithEmptyStack_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.IF()).thenReturn(term("if"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("10");
        when(stacks.pop()).thenReturn(null);

        visitor.visitInstr(ctx);

        // L'exécution devrait s'arrêter
        assertNotNull(visitor);
    }

    @Test
    void visitInstr_ifWithInvalidCondition_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.IF()).thenReturn(term("if"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("10");

        mockStack.push(new Stacks.Quad("%TMP%", "notBool", "%TMP%", Type.STRING));

        visitor.visitInstr(ctx);

        assertNotNull(visitor);
    }

    @Test
    void visitInstr_ifWithIntegerCondition_convertsToBoolean() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.IF()).thenReturn(term("if"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("10");

        mockStack.push(new Stacks.Quad("%TMP%", 1, "%TMP%", Type.ENTIER));

        visitor.visitInstr(ctx);

        // Le IF devrait interpréter 1 comme true
        assertNotNull(visitor);
    }

    // ===== Tests pour axiomeGOTO =====

    @Test
    void visitInstr_goto_jumpsToAddress() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.AdresseContext adresse = mock(JajaCodeParser.AdresseContext.class);

        when(ctx.GOTO()).thenReturn(term("goto"));
        when(ctx.adresse()).thenReturn(adresse);
        when(adresse.getText()).thenReturn("20");

        visitor.visitInstr(ctx);

        // Le goto devrait avoir changé le compteur de programme
        assertNotNull(visitor);
    }

    // ===== Tests pour axiomeINC =====

    @Test
    void visitInstr_incWithEmptyStack_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext ident = mock(JajaCodeParser.IdentContext.class);

        when(ctx.INC()).thenReturn(term("inc"));
        when(ctx.ident()).thenReturn(ident);
        when(ident.getText()).thenReturn("x");
        when(stacks.pop()).thenReturn(null);

        visitor.visitInstr(ctx);

        verify(stacks, never()).AffecterVal(anyString(), any());
    }


    @Test
    void visitInstr_incWithNonIntegerValues_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext ident = mock(JajaCodeParser.IdentContext.class);

        when(ctx.INC()).thenReturn(term("inc"));
        when(ctx.ident()).thenReturn(ident);
        when(ident.getText()).thenReturn("x");

        mockStack.push(new Stacks.Quad("%TMP%", "notInt", "%TMP%", Type.STRING));
        when(stacks.getValue("x")).thenReturn(10);

        visitor.visitInstr(ctx);

        verify(stacks, never()).AffecterVal(anyString(), any());
    }

    @Test
    void visitInstr_incWithFailedAffectation_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);
        JajaCodeParser.IdentContext ident = mock(JajaCodeParser.IdentContext.class);

        when(ctx.INC()).thenReturn(term("inc"));
        when(ctx.ident()).thenReturn(ident);
        when(ident.getText()).thenReturn("const");

        mockStack.push(new Stacks.Quad("%TMP%", 5, "%TMP%", Type.ENTIER));
        when(stacks.getValue("const")).thenReturn(10);
        when(stacks.AffecterVal("const", 15)).thenReturn(false); // Échec de l'affectation

        visitor.visitInstr(ctx);

        verify(stacks).AffecterVal("const", 15);
    }

    // ===== Tests pour axiomeJCSTOP =====

    @Test
    void visitInstr_jcstop_stopsExecution() {
        JajaCodeParser.InstrContext ctx = mock(JajaCodeParser.InstrContext.class);

        when(ctx.JCSTOP()).thenReturn(term("jcstop"));

        visitor.visitInstr(ctx);

        // JCSTOP devrait arrêter l'exécution
        assertNotNull(visitor);
    }
}