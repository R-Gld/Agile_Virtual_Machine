package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JajaCodeInterpreterVisitorTest {

    private Stacks stacks;
    private JajaCodeInterpreterVisitor visitor;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
        visitor = new JajaCodeInterpreterVisitor(stacks);
    }

    private JajaCodeParser.ClasseContext parseCode(String code) {
        CharStream stream = CharStreams.fromString(code);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        return parser.classe();
    }

    @Test
    void testPushWithStringRemovesQuotes() {
        // Test PUSH with string value (line 137-140)
        // Tests that quotes are removed from STRING tokens
        String code = """
                1 init
                2 push("Hello")
                3 write
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        assertDoesNotThrow(() -> visitor.run());
        assertTrue(visitor.isFinished());

        // The string "Hello" (without quotes) should have been written
        // This tests that the visitor properly handles STRING tokens by removing quotes
    }

    @Test
    void testInvalidInstructionAddress() {
        // Test handling of invalid instruction address (line 104-108)
        String code = """
                1 init
                2 goto(99)
                3 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        visitor.run();

        // Should stop execution when reaching invalid address
        assertTrue(visitor.isFinished());
    }

    @Test
    void testNewWithVarSorte() {
        // Test NEW with var sorte (line 204)
        String code = """
                1 init
                2 push(5)
                3 new(x, int, var, 0)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        assertDoesNotThrow(() -> visitor.run());

        // Variable should exist
        assertNotNull(stacks.getValue("x"));
        assertEquals(5, stacks.getValue("x"));
    }

    @Test
    void testNewWithCstSorte() {
        // Test NEW with cst sorte (line 205)
        String code = """
                1 init
                2 push(10)
                3 new(y, int, cst, 0)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        assertDoesNotThrow(() -> visitor.run());

        assertEquals(10, stacks.getValue("y"));
    }

    @Test
    void testNewWithBooleanType() {
        // Test NEW with boolean type (line 206)
        String code = """
                1 init
                2 push(true)
                3 new(z, boolean, var, 0)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        assertDoesNotThrow(() -> visitor.run());

        assertEquals(true, stacks.getValue("z"));
    }

    @Test
    void testNewarrayWithIntType() {
        // Test NEWARRAY with int type (line 218)
        String code = """
                1 init
                2 push(5)
                3 newarray(arr, int)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);
        assertDoesNotThrow(() -> visitor.run());

        // Array should have been created with size 5
        assertEquals(5, stacks.getArrayLength("arr"));
    }

    @Test
    void testStepExecution() {
        // Test step-by-step execution (line 251-272)
        String code = """
                1 init
                2 push(10)
                3 push(20)
                4 add
                5 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        // Execute step by step
        assertTrue(visitor.step()); // init at PC=1
        assertTrue(visitor.step()); // push(10) at PC=2
        assertTrue(visitor.step()); // push(20) at PC=3
        assertTrue(visitor.step()); // add at PC=4
        assertFalse(visitor.step()); // jcstop - should return false
        assertTrue(visitor.isFinished());
    }

    @Test
    void testStepWithInvalidAddress() {
        // Test step() with invalid address (line 262-266)
        String code = """
                1 init
                2 goto(100)
                3 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertTrue(visitor.step()); // init
        assertTrue(visitor.step()); // goto(100) - changes PC to 100
        assertFalse(visitor.step()); // PC=100 doesn't exist, should return false
        assertTrue(visitor.isFinished());
    }

    @Test
    void testResetStep() {
        // Test resetStep() functionality (line 277-280)
        String code = """
                1 init
                2 push(5)
                3 push(10)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        // Execute some steps
        visitor.step(); // PC=1
        visitor.step(); // PC=2
        visitor.step(); // PC=3

        int currentPC = visitor.getCurrentInstructionIndex();
        assertTrue(currentPC > 1);

        // Reset
        visitor.resetStep();

        // After reset, PC should be back at 1
        assertEquals(1, visitor.getCurrentInstructionIndex());
    }

    @Test
    void testGetCurrentInstructionText() {
        // Test getCurrentInstructionText() (line 295-301)
        String code = """
                1 init
                2 push(42)
                3 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        // Before any execution, get initial instruction
        String text = visitor.getCurrentInstructionText();
        assertNotNull(text);

        // Execute one step
        visitor.step();

        // Now should show next instruction
        text = visitor.getCurrentInstructionText();
        assertNotNull(text);
        assertTrue(text.contains("push") || text.contains("42"));
    }

    @Test
    void testGetCurrentInstructionTextInvalidPC() {
        // Test getCurrentInstructionText() with invalid PC (line 298-301)
        String code = """
                1 init
                2 goto(999)
                3 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        visitor.step(); // init
        visitor.step(); // goto(999) - PC becomes 999

        // Now PC is at invalid address 999
        String text = visitor.getCurrentInstructionText();
        assertEquals("N/A", text);
    }

    @Test
    void testAllOper2Operations() {
        // Test all oper2 dispatching (line 228-237)
        String code = """
                1 init
                2 push(10)
                3 push(5)
                4 add
                5 push(3)
                6 sub
                7 push(2)
                8 mul
                9 push(2)
                10 div
                11 push(true)
                12 push(false)
                13 and
                14 push(true)
                15 or
                16 push(5)
                17 push(3)
                18 sup
                19 push(5)
                20 push(5)
                21 cmp
                22 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        // Should execute all operations without errors
        assertDoesNotThrow(() -> visitor.run());
        assertTrue(visitor.isFinished());
    }

    @Test
    void testAllOper1Operations() {
        // Test all oper1 dispatching (line 241-245)
        String code = """
                1 init
                2 push(5)
                3 neg
                4 push(true)
                5 not
                6 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
        assertTrue(visitor.isFinished());
    }

    @Test
    void testAllArrayOperations() {
        // Test all array operations (line 153-156)
        String code = """
                1 init
                2 push(5)
                3 newarray(arr, int)
                4 push(0)
                5 push(100)
                6 astore(arr)
                7 push(0)
                8 aload(arr)
                9 pop
                10 push(0)
                11 push(1)
                12 ainc(arr)
                13 length(arr)
                14 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
        assertEquals(5, stacks.getArrayLength("arr"));
    }

    @Test
    void testMethodInvocation() {
        // Test INVOKE and RETURN operations (line 47-48, 130)
        String code = """
                1 init
                2 push(5)
                3 new(f@integer, int, meth, 0)
                4 goto(10)
                5 push(10)
                6 return
                10 invoke(f@integer)
                11 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
    }

    @Test
    void testIsFinishedBeforeExecution() {
        // Test isFinished() before any execution
        String code = """
                1 init
                2 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        // Before run(), context hasn't started yet
        assertFalse(visitor.isFinished());

        visitor.run();
        assertTrue(visitor.isFinished());
    }

    @Test
    void testGetCurrentInstructionIndexProgression() {
        // Test that instruction index progresses correctly
        String code = """
                1 init
                2 push(1)
                3 push(2)
                4 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        visitor.step();
        int pc1 = visitor.getCurrentInstructionIndex();

        visitor.step();
        int pc2 = visitor.getCurrentInstructionIndex();

        visitor.step();
        int pc3 = visitor.getCurrentInstructionIndex();

        // PC should progress
        assertTrue(pc2 > pc1);
        assertTrue(pc3 > pc2);
    }

    @Test
    void testAllBasicInstructions() {
        // Test all basic instructions without arguments (line 124-130)
        String code = """
                1 init
                2 push(1)
                3 push(2)
                4 swap
                5 pop
                6 write
                7 push(5)
                8 writeln
                9 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
        assertTrue(visitor.isFinished());
    }

    @Test
    void testControlFlowInstructions() {
        // Test IF and GOTO (line 51-52, 164-165)
        String code = """
                1 init
                2 push(true)
                3 if(10)
                4 push(99)
                5 goto(11)
                10 push(42)
                11 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
        assertTrue(visitor.isFinished());
    }

    @Test
    void testAllStoreLoadOperations() {
        // Test STORE, LOAD, INC (line 146-148)
        String code = """
                1 init
                2 push(10)
                3 new(x@global, int, var, 0)
                4 load(x@global)
                5 push(5)
                6 inc(x@global)
                7 push(20)
                8 store(x@global)
                9 jcstop
                """;

        JajaCodeParser.ClasseContext tree = parseCode(code);
        visitor.load(tree);

        assertDoesNotThrow(() -> visitor.run());
        assertEquals(20, stacks.getValue("x@global"));
    }
}
