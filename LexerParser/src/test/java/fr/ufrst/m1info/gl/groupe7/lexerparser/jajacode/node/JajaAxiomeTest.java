package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.*;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les axiomes JajaCode.
 * Vérifie l'implémentation par rapport aux règles d'interprétation :
 *
 * Règles de la sémantique interprétative :
 * < MEM, ADR > ⊢ JCODE –» < MEM, ADR >
 */
class JajaAxiomeTest {

    private Stacks stacks;
    private MachineContext context;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
        context = new MachineContext(stacks);
        context.setInstructionCounter(1);
    }

    // ==================== Tests pour InitAxiome ====================
    // [init] : <m,a> ⊢ init –» <[], a+1>

    @Nested
    @DisplayName("InitAxiome Tests")
    class InitAxiomeTests {

        @Test
        @DisplayName("init incrémente le PC de 1")
        void init_incrementsPC() {
            InitAxiome axiome = new InitAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            assertEquals(initialPC + 1, context.getInstructionCounter());
        }
    }

    // ==================== Tests pour JCStopAxiome ====================
    // [jcstop] : <m,a> ⊢ jcstop –» <m, ⊥>

    @Nested
    @DisplayName("JCStopAxiome Tests")
    class JCStopAxiomeTests {

        @Test
        @DisplayName("jcstop arrête l'exécution")
        void jcstop_stopsExecution() {
            JCStopAxiome axiome = new JCStopAxiome();
            assertTrue(context.isRunning());

            axiome.execute(context, null);

            assertFalse(context.isRunning());
        }
    }

    // ==================== Tests pour PushAxiome ====================
    // [push]: <m,a> ⊢ push(v) –» <<w, v, cst,*>.m, a+1>

    @Nested
    @DisplayName("PushAxiome Tests")
    class PushAxiomeTests {

        @Test
        @DisplayName("push(5) empile un entier")
        void push_integer_pushesOntoStack() {
            PushAxiome axiome = new PushAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "5");

            Stacks.Quad result = stacks.pop();
            assertEquals(5, result.value);
            assertEquals(Type.ENTIER, result.type);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("push(true) empile un booléen vrai")
        void push_true_pushesBooleanOntoStack() {
            PushAxiome axiome = new PushAxiome();

            axiome.execute(context, "true");

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
            assertEquals(Type.BOOLEEN, result.type);
        }

        @Test
        @DisplayName("push(false) empile un booléen faux")
        void push_false_pushesBooleanOntoStack() {
            PushAxiome axiome = new PushAxiome();

            axiome.execute(context, "false");

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
            assertEquals(Type.BOOLEEN, result.type);
        }

        @Test
        @DisplayName("push(-10) empile un entier négatif")
        void push_negativeInteger_pushesOntoStack() {
            PushAxiome axiome = new PushAxiome();

            axiome.execute(context, "-10");

            Stacks.Quad result = stacks.pop();
            assertEquals(-10, result.value);
            assertEquals(Type.ENTIER, result.type);
        }

        @Test
        @DisplayName("push(0) empile zéro")
        void push_zero_pushesOntoStack() {
            PushAxiome axiome = new PushAxiome();

            axiome.execute(context, "0");

            Stacks.Quad result = stacks.pop();
            assertEquals(0, result.value);
            assertEquals(Type.ENTIER, result.type);
        }
    }

    // ==================== Tests pour PopAxiome ====================
    // [pop]: <<i,v,o,t>.m,a>- pop –» <m, a+1>

    @Nested
    @DisplayName("PopAxiome Tests")
    class PopAxiomeTests {

        @Test
        @DisplayName("pop retire l'élément du sommet de la pile")
        void pop_removesTopElement() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            PopAxiome axiome = new PopAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            assertNull(stacks.pop()); // La pile devrait être vide
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("pop sur pile vide lance StackUnderflowException")
        void pop_emptyStack_throwsException() {
            PopAxiome axiome = new PopAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour SwapAxiome ====================
    // [swap]: <q1.q2.m,a> ⊢ swap –» <q2.q1.m, a+1>

    @Nested
    @DisplayName("SwapAxiome Tests")
    class SwapAxiomeTests {

        @Test
        @DisplayName("swap échange les deux éléments du sommet")
        void swap_exchangesTopTwoElements() {
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            SwapAxiome axiome = new SwapAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            assertEquals(1, stacks.pop().value); // Anciennement en dessous, maintenant au sommet
            assertEquals(2, stacks.pop().value); // Anciennement au sommet, maintenant en dessous
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }
    }

    // ==================== Tests pour NewAxiome ====================
    // [newV]: <m,a> ⊢ new(i,t,var,s) –» <IdentVal(i,t,m,s),a+1>

    @Nested
    @DisplayName("NewAxiome Tests")
    class NewAxiomeTests {

        @Test
        @DisplayName("new(x,int,var) déclare une variable entière")
        void new_variable_declaresInSymbolTable() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            NewAxiome axiome = new NewAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "x,int,var");

            assertEquals(42, stacks.getValue("x"));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("new(b,boolean,var) déclare une variable booléenne")
        void new_booleanVariable_declaresInSymbolTable() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            NewAxiome axiome = new NewAxiome();

            axiome.execute(context, "b,boolean,var");

            assertEquals(true, stacks.getValue("b"));
        }

        @Test
        @DisplayName("new(f,int,meth) déclare une méthode")
        void new_method_declaresInSymbolTable() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER)); // Adresse de la méthode
            NewAxiome axiome = new NewAxiome();

            axiome.execute(context, "f,int,meth");

            assertEquals(10, stacks.getValue("f"));
        }

        @Test
        @DisplayName("new sans valeur sur la pile lance StackUnderflowException")
        void new_emptyStack_throwsException() {
            NewAxiome axiome = new NewAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "x,int,var"));
        }

        @Test
        @DisplayName("new avec arguments manquants lance JajaCodeRuntimeException")
        void new_missingArgs_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            NewAxiome axiome = new NewAxiome();

            assertThrows(JajaCodeRuntimeException.class, () -> axiome.execute(context, "x,int"));
        }
    }

    // ==================== Tests pour StoreAxiome ====================
    // [store]: <<w, v, cst,*>.m,a> ⊢ store(i) –» <AffecterVal(i,v,m), a+1>

    @Nested
    @DisplayName("StoreAxiome Tests")
    class StoreAxiomeTests {

        @Test
        @DisplayName("store(x) affecte la valeur du sommet à x")
        void store_assignsValueToVariable() {
            // Déclarer d'abord la variable
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");
            context.setInstructionCounter(1); // Reset PC

            // Empiler la nouvelle valeur et faire store
            stacks.push(new Stacks.Quad("%TEMP%", 99, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "x");

            assertEquals(99, stacks.getValue("x"));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("store sur pile vide lance une exception")
        void store_emptyStack_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");

            StoreAxiome axiome = new StoreAxiome();

            assertThrows(RuntimeException.class, () -> axiome.execute(context, "x"));
        }

        @Test
        @DisplayName("store sur variable inexistante lance une exception")
        void store_undefinedVariable_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            StoreAxiome axiome = new StoreAxiome();

            assertThrows(RuntimeException.class, () -> axiome.execute(context, "undefined"));
        }
    }

    // ==================== Tests pour LoadAxiome ====================
    // [load]: <m,a> ⊢ load(i) –» <<w, Val(i,m), cst,*>.m, a+1>

    @Nested
    @DisplayName("LoadAxiome Tests")
    class LoadAxiomeTests {

        @Test
        @DisplayName("load(x) empile la valeur de x")
        void load_pushesVariableValue() {
            // Déclarer la variable
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");
            context.setInstructionCounter(1);

            LoadAxiome axiome = new LoadAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "x");

            Stacks.Quad result = stacks.pop();
            assertEquals(42, result.value);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("load sur variable inexistante lance UndefinedSymbolException")
        void load_undefinedVariable_throwsException() {
            LoadAxiome axiome = new LoadAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }
    }

    // ==================== Tests pour GotoAxiome ====================
    // [goto]: <m,a> ⊢ goto(a1) –» <m, a1>

    @Nested
    @DisplayName("GotoAxiome Tests")
    class GotoAxiomeTests {

        @Test
        @DisplayName("goto(10) saute à l'adresse 10")
        void goto_jumpsToAddress() {
            GotoAxiome axiome = new GotoAxiome();

            axiome.execute(context, "10");

            assertEquals(10, context.getInstructionCounter());
        }

        @Test
        @DisplayName("goto avec adresse invalide lance InvalidAddressException")
        void goto_invalidAddress_throwsException() {
            GotoAxiome axiome = new GotoAxiome();

            assertThrows(InvalidAddressException.class, () -> axiome.execute(context, "abc"));
        }
    }

    // ==================== Tests pour IfAxiome ====================
    // [iftrue]: <<w,true, cst, *>.m,a> ⊢ if(a1) –» <m,a1>
    // [iffalse]: <<w,false, cst, *>.m,a> ⊢ if(a1) –» <m,a+1>

    @Nested
    @DisplayName("IfAxiome Tests")
    class IfAxiomeTests {

        @Test
        @DisplayName("if(10) avec true saute à l'adresse 10")
        void if_true_jumpsToAddress() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            IfAxiome axiome = new IfAxiome();

            axiome.execute(context, "10");

            assertEquals(10, context.getInstructionCounter());
        }

        @Test
        @DisplayName("if(10) avec false continue séquentiellement")
        void if_false_continuesSequentially() {
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            IfAxiome axiome = new IfAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "10");

            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("if sur pile vide lance StackUnderflowException")
        void if_emptyStack_throwsException() {
            IfAxiome axiome = new IfAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "10"));
        }

        @Test
        @DisplayName("if avec entier non-zéro saute")
        void if_nonZeroInteger_jumps() {
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            IfAxiome axiome = new IfAxiome();

            axiome.execute(context, "10");

            assertEquals(10, context.getInstructionCounter());
        }

        @Test
        @DisplayName("if avec entier zéro continue")
        void if_zeroInteger_continues() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            IfAxiome axiome = new IfAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "10");

            assertEquals(initialPC + 1, context.getInstructionCounter());
        }
    }

    // ==================== Tests pour InvokeAxiome ====================
    // [invoke]: <m,a> ⊢ invoke(i) –» <<w, a+1,cst,*>.m, Val(i, m)>

    @Nested
    @DisplayName("InvokeAxiome Tests")
    class InvokeAxiomeTests {

        @Test
        @DisplayName("invoke(f) empile l'adresse de retour et saute")
        void invoke_pushesReturnAddressAndJumps() {
            // Déclarer la méthode f à l'adresse 20
            stacks.push(new Stacks.Quad("%TEMP%", 20, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "f,int,meth");
            context.setInstructionCounter(5);

            InvokeAxiome axiome = new InvokeAxiome();

            axiome.execute(context, "f");

            // Vérifier que le PC a sauté à l'adresse de la méthode
            assertEquals(20, context.getInstructionCounter());

            // Vérifier que l'adresse de retour (5+1=6) est sur la pile
            Stacks.Quad returnQuad = stacks.pop();
            assertEquals(6, returnQuad.value);
        }
    }

    // ==================== Tests pour ReturnAxiome ====================
    // [return]: <<w ,a1,cst,*>.m,a> ⊢ return –» <m, a1>

    @Nested
    @DisplayName("ReturnAxiome Tests")
    class ReturnAxiomeTests {

        @Test
        @DisplayName("return restaure le PC à l'adresse de retour")
        void return_restoresPC() {
            // Simuler un quad de retour sur la pile
            stacks.push(new Stacks.Quad("%TEMP%", 15, "cst", Type.ENTIER));
            ReturnAxiome axiome = new ReturnAxiome();

            axiome.execute(context, null);

            assertEquals(15, context.getInstructionCounter());
        }
    }

    // ==================== Tests pour AddAxiome ====================
    // [op2]: <<w, v2,cst,*>.<w, v1,cst,*>.m,a>⊢oper2 –» <<w, v1 oper2 v2, cst,*>.m, a+1>

    @Nested
    @DisplayName("AddAxiome Tests")
    class AddAxiomeTests {

        @Test
        @DisplayName("add: 3 + 5 = 8")
        void add_twoIntegers_pushesSum() {
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            AddAxiome axiome = new AddAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(8, result.value);
            assertEquals(Type.ENTIER, result.type);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("add sur pile insuffisante lance StackUnderflowException")
        void add_insufficientStack_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            AddAxiome axiome = new AddAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, null));
        }

        @Test
        @DisplayName("add avec types non entiers lance TypeMismatchException")
        void add_nonIntegerTypes_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            AddAxiome axiome = new AddAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour SubAxiome ====================

    @Nested
    @DisplayName("SubAxiome Tests")
    class SubAxiomeTests {

        @Test
        @DisplayName("sub: 10 - 3 = 7")
        void sub_twoIntegers_pushesDifference() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            SubAxiome axiome = new SubAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(7, result.value);
        }

        @Test
        @DisplayName("sub: 5 - 10 = -5 (résultat négatif)")
        void sub_negativeResult() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            SubAxiome axiome = new SubAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(-5, result.value);
        }
    }

    // ==================== Tests pour MulAxiome ====================

    @Nested
    @DisplayName("MulAxiome Tests")
    class MulAxiomeTests {

        @Test
        @DisplayName("mul: 4 * 7 = 28")
        void mul_twoIntegers_pushesProduct() {
            stacks.push(new Stacks.Quad("%TEMP%", 4, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 7, "%TEMP%", Type.ENTIER));
            MulAxiome axiome = new MulAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(28, result.value);
        }

        @Test
        @DisplayName("mul: 5 * 0 = 0")
        void mul_byZero_pushesZero() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            MulAxiome axiome = new MulAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(0, result.value);
        }
    }

    // ==================== Tests pour DivAxiome ====================

    @Nested
    @DisplayName("DivAxiome Tests")
    class DivAxiomeTests {

        @Test
        @DisplayName("div: 20 / 4 = 5")
        void div_twoIntegers_pushesQuotient() {
            stacks.push(new Stacks.Quad("%TEMP%", 20, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 4, "%TEMP%", Type.ENTIER));
            DivAxiome axiome = new DivAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(5, result.value);
        }

        @Test
        @DisplayName("div: 7 / 2 = 3 (division entière)")
        void div_integerDivision() {
            stacks.push(new Stacks.Quad("%TEMP%", 7, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            DivAxiome axiome = new DivAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(3, result.value);
        }

        @Test
        @DisplayName("div par zéro lance DivisionByZeroException")
        void div_byZero_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            DivAxiome axiome = new DivAxiome();

            assertThrows(DivisionByZeroException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour IncAxiome ====================
    // [inc]: <<w,v, cst,*>.m,a> ⊢ inc(i) –» <AffecterVal(i,Val(i,m)+v,m), a+1>

    @Nested
    @DisplayName("IncAxiome Tests")
    class IncAxiomeTests {

        @Test
        @DisplayName("inc(x) avec 5 sur la pile: x = x + 5")
        void inc_addsValueToVariable() {
            // Déclarer x = 10
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");
            context.setInstructionCounter(1);

            // Empiler l'incrément et exécuter inc
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            IncAxiome axiome = new IncAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "x");

            assertEquals(15, stacks.getValue("x"));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("inc sur variable inexistante lance UndefinedSymbolException")
        void inc_undefinedVariable_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            IncAxiome axiome = new IncAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }
    }

    // ==================== Tests pour SupAxiome ====================

    @Nested
    @DisplayName("SupAxiome Tests")
    class SupAxiomeTests {

        @Test
        @DisplayName("sup: 10 > 5 = true")
        void sup_greaterThan_pushesTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            SupAxiome axiome = new SupAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
            assertEquals(Type.BOOLEEN, result.type);
        }

        @Test
        @DisplayName("sup: 5 > 10 = false")
        void sup_lessThan_pushesFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            SupAxiome axiome = new SupAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }

        @Test
        @DisplayName("sup: 5 > 5 = false")
        void sup_equal_pushesFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            SupAxiome axiome = new SupAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }
    }

    // ==================== Tests pour CmpAxiome ====================

    @Nested
    @DisplayName("CmpAxiome Tests")
    class CmpAxiomeTests {

        @Test
        @DisplayName("cmp: 5 == 5 = true")
        void cmp_equal_pushesTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            CmpAxiome axiome = new CmpAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
            assertEquals(Type.BOOLEEN, result.type);
        }

        @Test
        @DisplayName("cmp: 5 == 10 = false")
        void cmp_notEqual_pushesFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            CmpAxiome axiome = new CmpAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }

        @Test
        @DisplayName("cmp: true == true = true")
        void cmp_booleans_equal() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            CmpAxiome axiome = new CmpAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }
    }

    // ==================== Tests pour NegAxiome ====================
    // [op1]: <<w, v1,cst,*>.m,a> ⊢ oper1 –» <<w, oper1 v1, cst,*>.m, a+1>

    @Nested
    @DisplayName("NegAxiome Tests")
    class NegAxiomeTests {

        @Test
        @DisplayName("neg: -5 = -5")
        void neg_negatesPositive() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NegAxiome axiome = new NegAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(-5, result.value);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("neg: -(-3) = 3")
        void neg_negatesNegative() {
            stacks.push(new Stacks.Quad("%TEMP%", -3, "%TEMP%", Type.ENTIER));
            NegAxiome axiome = new NegAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(3, result.value);
        }

        @Test
        @DisplayName("neg avec type non entier lance TypeMismatchException")
        void neg_nonInteger_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            NegAxiome axiome = new NegAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour NotAxiome ====================

    @Nested
    @DisplayName("NotAxiome Tests")
    class NotAxiomeTests {

        @Test
        @DisplayName("not: !true = false")
        void not_true_returnsFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            NotAxiome axiome = new NotAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("not: !false = true")
        void not_false_returnsTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            NotAxiome axiome = new NotAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }

        @Test
        @DisplayName("not avec type non booléen lance TypeMismatchException")
        void not_nonBoolean_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NotAxiome axiome = new NotAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour AndAxiome ====================

    @Nested
    @DisplayName("AndAxiome Tests")
    class AndAxiomeTests {

        @Test
        @DisplayName("and: true && true = true")
        void and_trueAndTrue_returnsTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            AndAxiome axiome = new AndAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }

        @Test
        @DisplayName("and: true && false = false")
        void and_trueAndFalse_returnsFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            AndAxiome axiome = new AndAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }

        @Test
        @DisplayName("and: false && false = false")
        void and_falseAndFalse_returnsFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            AndAxiome axiome = new AndAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }

        @Test
        @DisplayName("and avec types non booléens lance TypeMismatchException")
        void and_nonBooleans_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            AndAxiome axiome = new AndAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour OrAxiome ====================

    @Nested
    @DisplayName("OrAxiome Tests")
    class OrAxiomeTests {

        @Test
        @DisplayName("or: true || false = true")
        void or_trueOrFalse_returnsTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            OrAxiome axiome = new OrAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }

        @Test
        @DisplayName("or: false || false = false")
        void or_falseOrFalse_returnsFalse() {
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", false, "%TEMP%", Type.BOOLEEN));
            OrAxiome axiome = new OrAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(false, result.value);
        }

        @Test
        @DisplayName("or: true || true = true")
        void or_trueOrTrue_returnsTrue() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            OrAxiome axiome = new OrAxiome();

            axiome.execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }
    }

    // ==================== Tests pour WriteAxiome ====================
    // [write]: <<w, v, cst,*>.m,a> ⊢ write –» <Afficher(v,m),a+1>

    @Nested
    @DisplayName("WriteAxiome Tests")
    class WriteAxiomeTests {

        @Test
        @DisplayName("write dépile et incrémente le PC")
        void write_popsAndIncrementsPC() {
            stacks.push(new Stacks.Quad("%TEMP%", "Hello", "%TEMP%", Type.VOID));
            WriteAxiome axiome = new WriteAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            assertNull(stacks.pop()); // Pile vide après pop
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("write sur pile vide lance StackUnderflowException")
        void write_emptyStack_throwsException() {
            WriteAxiome axiome = new WriteAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour WriteLnAxiome ====================
    // [writeln]: <<w, v, cst,*>.m,a> ⊢ writeln –» <AfficherLn(v,m),a+1>

    @Nested
    @DisplayName("WriteLnAxiome Tests")
    class WriteLnAxiomeTests {

        @Test
        @DisplayName("writeln dépile et incrémente le PC")
        void writeln_popsAndIncrementsPC() {
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            WriteLnAxiome axiome = new WriteLnAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, null);

            assertNull(stacks.pop()); // Pile vide après pop
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("writeln sur pile vide lance StackUnderflowException")
        void writeln_emptyStack_throwsException() {
            WriteLnAxiome axiome = new WriteLnAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, null));
        }
    }

    // ==================== Tests pour NewarrayAxiome ====================
    // [newarray]: <<w, v, cst,*>.m,a> ⊢ newarray(i, t) –» <DeclTab(i, v, t, m), a+1>

    @Nested
    @DisplayName("NewarrayAxiome Tests")
    class NewarrayAxiomeTests {

        @Test
        @DisplayName("newarray(arr,int) avec taille 10 crée un tableau")
        void newarray_validSize_createsArray() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "arr,int");

            assertTrue(stacks.getSymbolTable().contains("arr"));
            assertEquals(10, stacks.getArrayLength("arr"));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("newarray(arr,boolean) avec taille 5 crée un tableau booléen")
        void newarray_booleanArray_createsArray() {
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            axiome.execute(context, "arr,boolean");

            assertTrue(stacks.getSymbolTable().contains("arr"));
            assertEquals(5, stacks.getArrayLength("arr"));
        }

        @Test
        @DisplayName("newarray sur pile vide lance StackUnderflowException")
        void newarray_emptyStack_throwsException() {
            NewarrayAxiome axiome = new NewarrayAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr,int"));
        }

        @Test
        @DisplayName("newarray avec taille non entière lance TypeMismatchException")
        void newarray_nonIntegerSize_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            NewarrayAxiome axiome = new NewarrayAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, "arr,int"));
        }

        @Test
        @DisplayName("newarray avec arguments manquants lance JajaCodeRuntimeException")
        void newarray_missingArgs_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            NewarrayAxiome axiome = new NewarrayAxiome();

            assertThrows(JajaCodeRuntimeException.class, () -> axiome.execute(context, "arr"));
        }
    }

    // ==================== Tests pour AloadAxiome ====================
    // [aload]: <<w, ind, cst,*>.m,a> ⊢ aload(i) –» <<w, ValT(i, ind, m), cst,*>.m, a+1>

    @Nested
    @DisplayName("AloadAxiome Tests")
    class AloadAxiomeTests {

        @Test
        @DisplayName("aload(arr) charge la valeur à l'indice")
        void aload_validIndex_loadsValue() {
            // Créer tableau [0, 0, 42, 0, 0]
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");
            context.setInstructionCounter(1);

            // Stocker 42 à l'indice 2
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            new AstoreAxiome().execute(context, "arr");
            context.setInstructionCounter(1);

            // Charger arr[2]
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            AloadAxiome axiome = new AloadAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "arr");

            Stacks.Quad result = stacks.pop();
            assertEquals(42, result.value);
            assertEquals(Type.ENTIER, result.type);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("aload sur pile vide lance StackUnderflowException")
        void aload_emptyStack_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Vider la pile (newarray laisse une référence sur la pile)
            stacks.pop();

            AloadAxiome axiome = new AloadAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("aload avec indice non entier lance TypeMismatchException")
        void aload_nonIntegerIndex_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Pousser indice invalide
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            AloadAxiome axiome = new AloadAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("aload sur tableau inexistant lance UndefinedSymbolException")
        void aload_undefinedArray_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            AloadAxiome axiome = new AloadAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }

        @Test
        @DisplayName("aload avec indice hors bornes lance RuntimeException")
        void aload_outOfBounds_throwsException() {
            // Créer tableau de taille 5
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Tenter de charger arr[10]
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            AloadAxiome axiome = new AloadAxiome();

            assertThrows(RuntimeException.class, () -> axiome.execute(context, "arr"));
        }
    }

    // ==================== Tests pour AstoreAxiome ====================
    // [astore]: <<w, v, cst,*>.<w, ind, cst,*>.m,a> ⊢ astore(i) –» <AffecterValT(i, ind, v,m), a+1>

    @Nested
    @DisplayName("AstoreAxiome Tests")
    class AstoreAxiomeTests {

        @Test
        @DisplayName("astore(arr) stocke la valeur à l'indice")
        void astore_validIndexAndValue_storesValue() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");
            context.setInstructionCounter(1);

            // Stocker 99 à l'indice 3
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 99, "%TEMP%", Type.ENTIER));
            AstoreAxiome axiome = new AstoreAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "arr");

            // Vérifier que la valeur a été stockée
            assertEquals(99, stacks.getArrayValue("arr", 3));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("astore avec tableau booléen stocke un booléen")
        void astore_booleanArray_storesBoolean() {
            // Créer tableau booléen
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,boolean");

            // Stocker true à l'indice 1
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            AstoreAxiome axiome = new AstoreAxiome();

            axiome.execute(context, "arr");

            assertEquals(true, stacks.getArrayValue("arr", 1));
        }

        @Test
        @DisplayName("astore sur pile vide lance StackUnderflowException")
        void astore_emptyStack_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Vider la pile (newarray laisse une référence sur la pile)
            stacks.pop();

            AstoreAxiome axiome = new AstoreAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("astore avec pile insuffisante (< 2 éléments) lance StackUnderflowException")
        void astore_insufficientStack_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Vider la pile (newarray laisse une référence sur la pile)
            stacks.pop();

            // Pousser seulement 1 élément (il en faut 2: index et value)
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            AstoreAxiome axiome = new AstoreAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("astore avec indice non entier lance TypeMismatchException")
        void astore_nonIntegerIndex_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Pousser indice invalide et valeur
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            AstoreAxiome axiome = new AstoreAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("astore sur tableau inexistant lance UndefinedSymbolException")
        void astore_undefinedArray_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            AstoreAxiome axiome = new AstoreAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }
    }

    // ==================== Tests pour AincAxiome ====================
    // [ainc]: <<w, v, cst,*>.<w, ind, cst,*>.m,a> ⊢ ainc(i) –» <AffecterValT(i,ind,ValT(i,ind,m)+v,m), a+1>

    @Nested
    @DisplayName("AincAxiome Tests")
    class AincAxiomeTests {

        @Test
        @DisplayName("ainc(arr) incrémente la valeur du tableau")
        void ainc_validIndexAndIncrement_incrementsValue() {
            // Créer tableau et stocker 10 à l'indice 2
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");
            context.setInstructionCounter(1);

            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            new AstoreAxiome().execute(context, "arr");
            context.setInstructionCounter(1);

            // Incrémenter arr[2] de 5
            stacks.push(new Stacks.Quad("%TEMP%", 2, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            AincAxiome axiome = new AincAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "arr");

            assertEquals(15, stacks.getArrayValue("arr", 2));
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("ainc avec incrément négatif décrémente")
        void ainc_negativeIncrement_decrementsValue() {
            // Créer tableau et stocker 20 à l'indice 0
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 20, "%TEMP%", Type.ENTIER));
            new AstoreAxiome().execute(context, "arr");

            // Décrémenter arr[0] de 7
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", -7, "%TEMP%", Type.ENTIER));
            AincAxiome axiome = new AincAxiome();

            axiome.execute(context, "arr");

            assertEquals(13, stacks.getArrayValue("arr", 0));
        }

        @Test
        @DisplayName("ainc sur pile vide lance StackUnderflowException")
        void ainc_emptyStack_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Vider la pile (newarray laisse une référence sur la pile)
            stacks.pop();

            AincAxiome axiome = new AincAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("ainc avec pile insuffisante lance StackUnderflowException")
        void ainc_insufficientStack_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Vider la pile (newarray laisse une référence sur la pile)
            stacks.pop();

            // Pousser seulement 1 élément (il en faut 2: index et increment)
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            AincAxiome axiome = new AincAxiome();

            assertThrows(StackUnderflowException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("ainc avec incrément non entier lance TypeMismatchException")
        void ainc_nonIntegerIncrement_throwsException() {
            // Créer tableau
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");

            // Pousser indice et incrément invalide
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            AincAxiome axiome = new AincAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("ainc avec valeur non entière dans tableau lance TypeMismatchException")
        void ainc_nonIntegerValueInArray_throwsException() {
            // Créer tableau booléen
            stacks.push(new Stacks.Quad("%TEMP%", 3, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,boolean");

            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", true, "%TEMP%", Type.BOOLEEN));
            new AstoreAxiome().execute(context, "arr");

            // Tenter d'incrémenter un booléen
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 1, "%TEMP%", Type.ENTIER));
            AincAxiome axiome = new AincAxiome();

            assertThrows(TypeMismatchException.class, () -> axiome.execute(context, "arr"));
        }

        @Test
        @DisplayName("ainc sur tableau inexistant lance UndefinedSymbolException")
        void ainc_undefinedArray_throwsException() {
            stacks.push(new Stacks.Quad("%TEMP%", 0, "%TEMP%", Type.ENTIER));
            stacks.push(new Stacks.Quad("%TEMP%", 5, "%TEMP%", Type.ENTIER));
            AincAxiome axiome = new AincAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }
    }

    // ==================== Tests pour LengthAxiome ====================
    // [length]: <m,a> ⊢ length(i) –» <<w,long(I,m),cst,*>, a+1>

    @Nested
    @DisplayName("LengthAxiome Tests")
    class LengthAxiomeTests {

        @Test
        @DisplayName("length(arr) empile la taille du tableau")
        void length_validArray_pushesLength() {
            // Créer tableau de taille 10
            stacks.push(new Stacks.Quad("%TEMP%", 10, "%TEMP%", Type.ENTIER));
            new NewarrayAxiome().execute(context, "arr,int");
            context.setInstructionCounter(1);

            LengthAxiome axiome = new LengthAxiome();
            int initialPC = context.getInstructionCounter();

            axiome.execute(context, "arr");

            Stacks.Quad result = stacks.pop();
            assertEquals(10, result.value);
            assertEquals(Type.ENTIER, result.type);
            assertEquals(initialPC + 1, context.getInstructionCounter());
        }

        @Test
        @DisplayName("length sur tableau inexistant lance UndefinedSymbolException")
        void length_undefinedArray_throwsException() {
            LengthAxiome axiome = new LengthAxiome();

            assertThrows(UndefinedSymbolException.class, () -> axiome.execute(context, "undefined"));
        }

        @Test
        @DisplayName("length sur variable non-tableau lance RuntimeException")
        void length_nonArrayVariable_throwsException() {
            // Déclarer une variable normale
            stacks.push(new Stacks.Quad("%TEMP%", 42, "%TEMP%", Type.ENTIER));
            new NewAxiome().execute(context, "x,int,var");

            LengthAxiome axiome = new LengthAxiome();

            assertThrows(RuntimeException.class, () -> axiome.execute(context, "x"));
        }
    }

    // ==================== Tests d'intégration ====================

    @Nested
    @DisplayName("Tests d'intégration")
    class IntegrationTests {

        @Test
        @DisplayName("Séquence: push(5), push(3), add -> résultat = 8")
        void integration_pushPushAdd() {
            new PushAxiome().execute(context, "5");
            new PushAxiome().execute(context, "3");
            new AddAxiome().execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(8, result.value);
        }

        @Test
        @DisplayName("Séquence: push(10), new(x), load(x) -> x = 10 sur la pile")
        void integration_pushNewLoad() {
            new PushAxiome().execute(context, "10");
            new NewAxiome().execute(context, "x,int,var");
            new LoadAxiome().execute(context, "x");

            Stacks.Quad result = stacks.pop();
            assertEquals(10, result.value);
        }

        @Test
        @DisplayName("Séquence: déclaration et incrémentation de variable")
        void integration_declareAndIncrement() {
            // int x = 5; x += 3; -> x = 8
            new PushAxiome().execute(context, "5");
            new NewAxiome().execute(context, "x,int,var");
            new PushAxiome().execute(context, "3");
            new IncAxiome().execute(context, "x");

            assertEquals(8, stacks.getValue("x"));
        }

        @Test
        @DisplayName("Séquence: comparaison et branchement conditionnel")
        void integration_compareAndBranch() {
            // if (5 > 3) goto 10
            new PushAxiome().execute(context, "5");
            new PushAxiome().execute(context, "3");
            new SupAxiome().execute(context, null);
            context.setInstructionCounter(1);
            new IfAxiome().execute(context, "10");

            assertEquals(10, context.getInstructionCounter()); // Devrait sauter
        }

        @Test
        @DisplayName("Séquence: opérations logiques complexes")
        void integration_logicalOperations() {
            // (true && false) || true = true
            new PushAxiome().execute(context, "true");
            new PushAxiome().execute(context, "false");
            new AndAxiome().execute(context, null);
            new PushAxiome().execute(context, "true");
            new OrAxiome().execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(true, result.value);
        }

        @Test
        @DisplayName("Séquence: expression arithmétique complexe (2 + 3) * 4 = 20")
        void integration_complexArithmetic() {
            new PushAxiome().execute(context, "2");
            new PushAxiome().execute(context, "3");
            new AddAxiome().execute(context, null);
            new PushAxiome().execute(context, "4");
            new MulAxiome().execute(context, null);

            Stacks.Quad result = stacks.pop();
            assertEquals(20, result.value);
        }
    }
}

