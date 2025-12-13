package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.*;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class JajaCodeAxiomesCompletTest {

    private Stacks executeJajaCode(String code) {
        Stacks stacks = new Stacks();
        CharStream stream = CharStreams.fromString(code);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext tree = parser.classe();

        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(tree);
        interpreter.run();
        return stacks;
    }

    // ==================== Tests INIT ====================
    @Test
    void testInitAxiome() {
        String code = """
                1 init
                2 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertNotNull(stacks);
    }

    // ==================== Tests PUSH ====================
    @Test
    void testPushInteger() {
        String code = """
                1 init
                2 push(42)
                3 new(x@global, int, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(42, stacks.getValue("x@global"));
    }

    @Test
    void testPushBoolean_True() {
        String code = """
                1 init
                2 push(true)
                3 new(b@global, boolean, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("b@global"));
    }

    @Test
    void testPushBoolean_False() {
        String code = """
                1 init
                2 push(false)
                3 new(b@global, boolean, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("b@global"));
    }

    @Test
    void testPushNil() {
        String code = """
                1 init
                2 push(w)
                3 new(x@global, int, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        // After pushing omega, the variable is uninitialized
        // getValue should throw an exception for uninitialized variables
        assertThrows(RuntimeException.class, () -> stacks.getValue("x@global"),
                "Should throw exception for uninitialized variable");
    }

    @Test
    void testPushZero() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(0, stacks.getValue("x@global"));
    }

    @Test
    void testPushNegative() {
        String code = """
                1 init
                2 push(10)
                3 neg
                4 new(x@global, int, var, 0)
                5 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(-10, stacks.getValue("x@global"));
    }

    // ==================== Tests NEW ====================
    @Test
    void testNewVariable() {
        String code = """
                1 init
                2 push(100)
                3 new(x@global, int, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(100, stacks.getValue("x@global"));
        assertEquals(Type.ENTIER, stacks.getDataType("x@global"));
        assertEquals("var", stacks.getObjectType("x@global"));
    }

    @Test
    void testNewConstant() {
        String code = """
                1 init
                2 push(42)
                3 new(CONST@global, int, meth, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(42, stacks.getValue("CONST@global"));
        assertEquals("cst", stacks.getObjectType("CONST@global"));
    }

    @Test
    void testNewMethod() {
        String code = """
                1 init
                2 push(5)
                3 new(test, int, meth, 0)
                4 goto(10)
                5 new(y@test, int, var, 1)
                6 load(y@test)
                7 goto(8)
                8 swap
                9 return
                10 push(0)
                11 swap
                12 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(5, stacks.getValue("test"));
        assertEquals("cst", stacks.getObjectType("test"));
    }

    @Test
    void testNewBoolean() {
        String code = """
                1 init
                2 push(true)
                3 new(b@global, boolean, var, 0)
                4 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("b@global"));
        assertEquals(Type.BOOLEEN, stacks.getDataType("b@global"));
    }


    // ==================== Tests LOAD ====================
    @Test
    void testLoad() {
        String code = """
                1 init
                2 push(25)
                3 new(x@global, int, var, 0)
                4 load(x@global)
                5 new(y@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(25, stacks.getValue("x@global"));
        assertEquals(25, stacks.getValue("y@global"));
    }

    @Test
    void testLoadNil() {
        String code = """
                1 init
                2 load(x@global)
                3 jcstop
                """;
        UndefinedSymbolException e = assertThrows(UndefinedSymbolException.class, () -> executeJajaCode(code));
        assertEquals(2, e.getProgramCounter());
        assertTrue(e.getMessage().contains("PC=2, Axiome=LOAD] Symbole introuvable : 'x@global'"));
    }

    // ==================== Tests STORE ====================
    @Test
    void testStore() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(99)
                5 store(x@global)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(99, stacks.getValue("x@global"));
    }

    @Test
    void testStoreMultipleTimes() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(10)
                5 store(x@global)
                6 push(20)
                7 store(x@global)
                8 push(30)
                9 store(x@global)
                10 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(30, stacks.getValue("x@global"));
    }

    @Test
    void testStoreNil() {
        String code = """
                1 init
                2 push(50)
                3 new(x@global, int, var, 0)
                4 push(waza)
                5 store(x@global)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        // After storing omega (nil/waza), the variable becomes uninitialized
        // getValue should throw an exception for uninitialized variables
        assertThrows(RuntimeException.class, () -> stacks.getValue("x@global"),
                "Should throw exception for uninitialized variable");
    }

    @Test
    void testStoreWithoutPush() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 pop
                5 store(x@global)
                6 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(5, exception.getProgramCounter());
    }

    // ==================== Tests ADD ====================
    @Test
    void testAdd() {
        String code = """
                1 init
                2 push(10)
                3 push(20)
                4 add
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(30, stacks.getValue("result@global"));
    }

    @Test
    void testAddZero() {
        String code = """
                1 init
                2 push(0)
                3 push(0)
                4 add
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(0, stacks.getValue("result@global"));
    }

    @Test
    void testAddNegative() {
        String code = """
                1 init
                2 push(5)
                3 neg
                4 push(15)
                5 add
                6 new(result@global, int, var, 0)
                7 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(10, stacks.getValue("result@global"));
    }

    @Test
    void testAddOperanceVide() {
        String code = """
                1 init
                2 push(5)
                3 add
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testAddNonInteger() {
        String code = """
                1 init
                2 push(5)
                3 push(true)
                4 add
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Opérandes non entiers"));
    }

    // ==================== Tests SUB ====================
    @Test
    void testSub() {
        String code = """
                1 init
                2 push(30)
                3 push(10)
                4 sub
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(20, stacks.getValue("result@global"));
    }

    @Test
    void testSubNegativeResult() {
        String code = """
                1 init
                2 push(5)
                3 push(10)
                4 sub
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(-5, stacks.getValue("result@global"));
    }

    @Test
    void testSubOperandVide() {
        String code = """
                1 init
                2 push(10)
                3 sub
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testSubNonInteger() {
        String code = """
                1 init
                2 push(10)
                3 push(false)
                4 sub
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Opérandes non entiers"));
    }

    // ==================== Tests MUL ====================
    @Test
    void testMul() {
        String code = """
                1 init
                2 push(6)
                3 push(7)
                4 mul
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(42, stacks.getValue("result@global"));
    }

    @Test
    void testMulByZero() {
        String code = """
                1 init
                2 push(100)
                3 push(0)
                4 mul
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(0, stacks.getValue("result@global"));
    }

    @Test
    void testMulNegative() {
        String code = """
                1 init
                2 push(3)
                3 neg
                4 push(4)
                5 mul
                6 new(result@global, int, var, 0)
                7 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(-12, stacks.getValue("result@global"));
    }

    @Test
    void testMulOperandVide() {
        String code = """
                1 init
                2 push(5)
                3 mul
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testMulNonInteger() {
        String code = """
                1 init
                2 push(5)
                3 push(false)
                4 mul
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Opérandes non entiers"));
    }

    // ==================== Tests DIV ====================
    @Test
    void testDiv() {
        String code = """
                1 init
                2 push(20)
                3 push(4)
                4 div
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(5, stacks.getValue("result@global"));
    }

    @Test
    void testDivWithRemainder() {
        String code = """
                1 init
                2 push(17)
                3 push(5)
                4 div
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(3, stacks.getValue("result@global"));
    }

    @Test
    void testDivByZero() {
        String code = """
                1 init
                2 push(10)
                3 push(0)
                4 div
                5 new(result@global, int, var, 0)
                6 jcstop
                """;

        DivisionByZeroException exception = assertThrows(DivisionByZeroException.class, () -> executeJajaCode(code));

        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Division par zéro"));
    }

    @Test
    void testDivNonInteger() {
        String code = """
                1 init
                2 push(10)
                3 push(true)
                4 div
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Opérandes non entiers"));
    }

    @Test
    void testDivOperandVide() {
        String code = """
                1 init
                2 push(10)
                3 div
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    // ==================== Tests SUP ====================
    @Test
    void testSup_True() {
        String code = """
                1 init
                2 push(10)
                3 push(5)
                4 sup
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testSup_False() {
        String code = """
                1 init
                2 push(5)
                3 push(10)
                4 sup
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testSup_Equal() {
        String code = """
                1 init
                2 push(5)
                3 push(5)
                4 sup
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testSupOperandVide() {
        String code = """
                1 init
                2 push(5)
                3 sup
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testSupNonInteger() {
        String code = """
                1 init
                2 push(5)
                3 push(true)
                4 sup
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Tentative de comparer des valeurs non entières"));
    }

    // ==================== Tests CMP ====================
    @Test
    void testCmp_Equal() {
        String code = """
                1 init
                2 push(5)
                3 push(5)
                4 cmp
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testCmp_NotEqual() {
        String code = """
                1 init
                2 push(5)
                3 push(10)
                4 cmp
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testCmp_BooleanTrue() {
        String code = """
                1 init
                2 push(true)
                3 push(true)
                4 cmp
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testCmp_BooleanFalse() {
        String code = """
                1 init
                2 push(true)
                3 push(false)
                4 cmp
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testCmp_Nil() {
        String code = """
                1 init
                2 push(waza)
                3 push(waza)
                4 cmp
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testCpm_OperandeVide() {
        String code = """
                1 init
                2 push(5)
                3 cmp
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    // ==================== Tests AND ====================
    @Test
    void testAnd_TrueTrue() {
        String code = """
                1 init
                2 push(true)
                3 push(true)
                4 and
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testAnd_TrueFalse() {
        String code = """
                1 init
                2 push(true)
                3 push(false)
                4 and
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testAnd_FalseFalse() {
        String code = """
                1 init
                2 push(false)
                3 push(false)
                4 and
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testAndOperandVide() {
        String code = """
                1 init
                2 push(true)
                3 and
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testAndNonBoolean() {
        String code = """
                1 init
                2 push(true)
                3 push(5)
                4 and
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Tentative d'opération sur des types non booléens (true && 5)"));
    }

    // ==================== Tests OR ====================
    @Test
    void testOr_TrueTrue() {
        String code = """
                1 init
                2 push(true)
                3 push(true)
                4 or
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testOr_TrueFalse() {
        String code = """
                1 init
                2 push(true)
                3 push(false)
                4 or
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testOr_FalseFalse() {
        String code = """
                1 init
                2 push(false)
                3 push(false)
                4 or
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testOrOperandVide() {
        String code = """
                1 init
                2 push(true)
                3 or
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Besoin de 2 opérandes"));
    }

    @Test
    void testOrNonBoolean() {
        String code = """
                1 init
                2 push(false)
                3 push(10)
                4 or
                5 new(result@global, boolean, var, 0)
                6 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Tentative d'opération sur des types non booléens (false || 10)"));
    }

    // ==================== Tests NOT ====================
    @Test
    void testNot_True() {
        String code = """
                1 init
                2 push(true)
                3 not
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }

    @Test
    void testNot_False() {
        String code = """
                1 init
                2 push(false)
                3 not
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(true, stacks.getValue("result@global"));
    }

    @Test
    void testNotPileVide() {
        String code = """
                1 init
                2 not
                3 new(result@global, boolean, var, 0)
                4 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Pile vide : impossible d'effectuer l'opération"));
    }

    @Test
    void testNotNonBoolean() {
        String code = """
                1 init
                2 push(5)
                3 not
                4 new(result@global, boolean, var, 0)
                5 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("L'opérande doit être booléen"));
    }

    // ==================== Tests INC ====================
    @Test
    void testInc() {
        String code = """
                1 init
                2 push(10)
                3 new(x@global, int, var, 0)
                4 push(1)
                5 inc(x@global)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(11, stacks.getValue("x@global"));
    }

    @Test
    void testIncMultiple() {
        String code = """
                1 init
                2 push(5)
                3 new(x@global, int, var, 0)
                4 push(3)
                5 inc(x@global)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(8, stacks.getValue("x@global"));
    }

    @Test
    void testIncNegative() {
        String code = """
                1 init
                2 push(5)
                3 neg
                4 new(x@global, int, var, 0)
                5 push(10)
                6 inc(x@global)
                7 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(5, stacks.getValue("x@global"));
    }

    @Test
    void testIncInexistantSymbol() {
        String code = """
                1 init
                2 push(5)
                3 inc(y@global)
                4 jcstop
                """;

        UndefinedSymbolException e = assertThrows(UndefinedSymbolException.class, () -> executeJajaCode(code));
        assertEquals(3, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Symbole introuvable"));
    }

    @Test
    void testIncPileVide() {
        String code = """
                1 init
                2 push(10)
                3 new(x@global, int, var, 0)
                4 pop
                5 inc(x@global)
                6 jcstop
                """;

        StackUnderflowException e = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(5, e.getProgramCounter());
        assertTrue(e.getMessage().contains("INC"));
    }

    @Test
    void testIncNonInteger() {
        String code = """
                1 init
                2 push(true)
                3 new(x@global, boolean, var, 0)
                4 inc(x@global)
                5 jcstop
                """;

        TypeMismatchException e = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(4, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Tentative d'incrémenter avec des valeurs non entières"));
    }

    @Test
    void testIncCantAffect() {
        String code = """
                1 init
                2 push(10)
                3 new(CONST@global, int, meth, 0)
                4 push(5)
                5 inc(CONST@global)
                6 jcstop
                """;

        RuntimeException e = assertThrows(RuntimeException.class, () -> executeJajaCode(code));
        assertNotNull(e.getMessage());
    }

    // ==================== Tests NEG ====================

    @Test
    void testNeg() {
        String code = """
                1 init
                2 push(15)
                3 neg
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(-15, stacks.getValue("result@global"));
    }

    @Test
    void testNegNegative() {
        String code = """
                1 init
                2 push(8)
                3 neg
                4 neg
                5 new(result@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(8, stacks.getValue("result@global"));
    }

    @Test
    void testNegNonInteger() {
        String code = """
                1 init
                2 push(true)
                3 neg
                4 new(result@global, int, var, 0)
                5 jcstop
                """;
        TypeMismatchException exception = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(3, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Type invalide pour l'opération NEG"));
    }

    @Test
    void testNegPileVide() {
        String code = """
                1 init
                2 neg
                3 new(result@global, int, var, 0)
                5 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Pile vide"));
    }


    // ==================== Tests POP ====================
    @Test
    void testPop() {
        String code = """
                1 init
                2 push(10)
                3 push(20)
                4 pop
                5 new(x@global, int, var, 0)
                6 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(10, stacks.getValue("x@global"));
    }

    @Test
    void testPopPileVide() {
        String code = """
                1 init
                2 pop
                3 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Rien à dépiler"));
    }

    // ==================== Tests SWAP ====================
    @Test
    void testSwap() {
        String code = """
                1 init
                2 push(0)
                3 swap
                4 pop
                5 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertNotNull(stacks);
    }

    // ==================== Tests GOTO ====================
    @Test
    void testGoto() {
        String code = """
                1 init
                2 goto(5)
                3 push(100)
                4 new(x@global, int, var, 0)
                5 push(50)
                6 new(y@global, int, var, 0)
                7 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertNull(stacks.getValue("x@global"));
        assertEquals(50, stacks.getValue("y@global"));
    }

    @Test
    void testGotoNonValidInteger() {
        String code = """
                1 init
                2 goto(abc)
                3 push(100)
                4 new(x@global, int, var, 0)
                5 push(50)
                6 new(y@global, int, var, 0)
                7 jcstop
                """;
        InvalidAddressException e = assertThrows(InvalidAddressException.class, () -> executeJajaCode(code));
        assertEquals(2, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Adresse invalide : 'abc'"));
    }

    // ==================== Tests IF ====================
    @Test
    void testIf_True() {
        String code = """
                1 init
                2 push(true)
                3 if(6)
                4 push(100)
                5 new(x@global, int, var, 0)
                6 push(50)
                7 new(y@global, int, var, 0)
                8 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertNull(stacks.getValue("x@global"));
        assertEquals(50, stacks.getValue("y@global"));
    }

    @Test
    void testIf_False() {
        String code = """
                1 init
                2 push(false)
                3 if(6)
                4 push(100)
                5 new(x@global, int, var, 0)
                6 push(50)
                7 new(y@global, int, var, 0)
                8 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(100, stacks.getValue("x@global"));
        assertEquals(50, stacks.getValue("y@global"));
    }

    @Test
    void testInvalidAddressInIf() {
        String code = """
                1 init
                2 push(true)
                3 if(abc)
                4 push(100)
                5 new(x@global, int, var, 0)
                6 push(50)
                7 new(y@global, int, var, 0)
                8 jcstop
                """;
        InvalidAddressException e = assertThrows(InvalidAddressException.class, () -> executeJajaCode(code));
        assertEquals(3, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Adresse invalide : 'abc'"));
    }

    @Test
    void testIfPileVide() {
        String code = """
                1 init
                2 if(6)
                3 push(100)
                4 new(x@global, int, var, 0)
                5 pop
                6 push(50)
                7 new(y@global, int, var, 0)
                8 jcstop
                """;
        StackUnderflowException e = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Pile vide : impossible d'effectuer l'opération"));
    }

    @Test
    void testIfValeurNonBooleenne() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(12)
                5 store(x@global)
                6 push(12)
                7 push(0)
                8 sup
                9 if(11)
                10 goto(13)
                11 push(12)
                12 store(x@global)
                13 push(0)
                14 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(12, stacks.getValue("x@global"));
    }

    @Test
    void testIfConditionInvalide() {
        String code = """
                1 init
                2 push(waza)
                3 if(6)
                4 push(100)
                5 new(x@global, int, var, 0)
                6 push(50)
                7 new(y@global, int, var, 0)
                8 jcstop
                """;
        TypeMismatchException e = assertThrows(TypeMismatchException.class, () -> executeJajaCode(code));
        assertEquals(3, e.getProgramCounter());
        assertTrue(e.getMessage().contains("Type de condition invalide"));
    }


    // ==================== Tests INVOKE et RETURN ====================
    @Test
    void testInvokeAndReturn() {
        String code = """
                1 init
                2 push(5)
                3 new(addOne, int, meth, 0)
                4 goto(11)
                5 new(param@addOne, int, var, 1)
                6 load(param@addOne)
                7 push(1)
                8 add
                9 swap
                10 return
                11 push(5)
                12 invoke(addOne)
                13 swap
                14 pop
                15 new(result@global, int, var, 0)
                16 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(6, stacks.getValue("result@global"));
    }

    @Test
    void testInvokeWithMultipleParams() {
        String code = """
                1 init
                2 push(5)
                3 new(multiply, int, meth, 0)
                4 goto(12)
                5 new(b@multiply, int, var, 1)
                6 new(a@multiply, int, var, 2)
                7 load(a@multiply)
                8 load(b@multiply)
                9 mul
                10 swap
                11 return
                12 push(3)
                13 push(4)
                14 invoke(multiply)
                15 swap
                16 pop
                17 swap
                18 pop
                19 new(result@global, int, var, 0)
                20 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(12, stacks.getValue("result@global"));
    }

    // ==================== Tests WRITE ====================
    @Test
    void testWrite() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String code = """
                    1 init
                    2 push(42)
                    3 write
                    4 jcstop
                    """;
            executeJajaCode(code);
            assertTrue(outContent.toString().contains("42"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testWriteValeurInexistante() {
        String code = """
                1 init
                2 write
                3 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Manque la valeur à écrire"));
    }

    @Test
    void testWriteBoolean() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String code = """
                    1 init
                    2 push(true)
                    3 write
                    4 jcstop
                    """;
            executeJajaCode(code);
            assertTrue(outContent.toString().contains("true"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // ==================== Tests WRITELN ====================
    @Test
    void testWriteLn() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            String code = """
                    1 init
                    2 push(123)
                    3 writeln
                    4 jcstop
                    """;
            executeJajaCode(code);
            String output = outContent.toString();
            assertTrue(output.contains("123"));
            assertTrue(output.contains("\n") || output.contains(System.lineSeparator()));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testWriteLNValeurInexistante() {
        String code = """
                1 init
                2 writeln
                3 jcstop
                """;
        StackUnderflowException exception = assertThrows(StackUnderflowException.class, () -> executeJajaCode(code));
        assertEquals(2, exception.getProgramCounter());
        assertTrue(exception.getMessage().contains("Manque la valeur à écrire"));
    }

    // ==================== Tests complexes ====================
    @Test
    void testWhileLoop() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(5)
                5 load(x@global)
                6 sup
                7 not
                8 if(14)
                9 load(x@global)
                10 push(1)
                11 add
                12 store(x@global)
                13 goto(4)
                14 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(5, stacks.getValue("x@global"));
    }

    @Test
    void testIfElse() {
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(12)
                5 store(x@global)
                6 load(x@global)
                7 push(0)
                8 sup
                9 if(13)
                10 push(0)
                11 store(x@global)
                12 goto(15)
                13 push(42)
                14 store(x@global)
                15 push(0)
                16 swap
                17 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(42, stacks.getValue("x@global"));
    }

    @Test
    void testComplexExpression() {
        String code = """
                1 init
                2 push(10)
                3 push(5)
                4 add
                5 push(3)
                6 mul
                7 push(9)
                8 sub
                9 new(result@global, int, var, 0)
                10 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(36, stacks.getValue("result@global"));
    }

    @Test
    void testNestedMethodCalls() {
        String code = """
                1 init
                2 push(5)
                3 new(double, int, meth, 0)
                4 goto(11)
                5 new(x@double, int, var, 1)
                6 load(x@double)
                7 push(2)
                8 mul
                9 swap
                10 return
                11 push(5)
                12 invoke(double)
                13 swap
                14 pop
                15 invoke(double)
                16 swap
                17 pop
                18 new(result@global, int, var, 0)
                19 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(20, stacks.getValue("result@global"));
    }

    @Test
    void testBooleanLogic() {
        String code = """
                1 init
                2 push(true)
                3 push(false)
                4 or
                5 push(true)
                6 and
                7 not
                8 new(result@global, boolean, var, 0)
                9 jcstop
                """;
        Stacks stacks = executeJajaCode(code);
        assertEquals(false, stacks.getValue("result@global"));
    }
}

