package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestInstructionsJJC {

    private void executeJajaCode(String code, Stacks stacks) {
        CharStream stream = CharStreams.fromString(code);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(parser.classe());
        interpreter.run();
    }

    @Test
    void testInitAndStop() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 jcstop
                """;
        executeJajaCode(code, stacks);
        assertTrue(stacks.getStackFromTopToBottom().isEmpty());
    }

    @Test
    void testPushPopSwap() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(10)
                3 push(20)
                4 swap
                5 pop
                6 pop
                7 jcstop
                """;
        executeJajaCode(code, stacks);
        assertEquals(0, stacks.getStackFromTopToBottom().size());
    }

    @Test
    void testNewStoreLoad() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(42)
                5 store(x@global)
                6 load(x@global)
                7 pop
                8 jcstop
                """;
        executeJajaCode(code, stacks);
        assertEquals(42, stacks.getValue("x@global"));
    }

    @Test
    void testIfGoto() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(1)
                3 if(6)
                4 push(99)
                5 goto(7)
                6 push(42)
                7 jcstop
                """;
        executeJajaCode(code, stacks);
        // Si if saute, 42 est empilé, sinon 99
        assertEquals(42, stacks.getTop().value);
    }

    @Test
    void testAddSubMulDiv() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(10)
                3 push(5)
                4 add
                5 push(2)
                6 mul
                7 push(4)
                8 sub
                9 push(2)
                10 div
                11 jcstop
                """;
        executeJajaCode(code, stacks);
        assertEquals(13, stacks.getTop().value); // (((10+5)*2)-4)/2 = 13
    }

    @Test
    void testLogicAndOrNotSupCmp() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(true)
                3 push(false)
                4 or
                5 push(true)
                6 and
                7 not
                8 push(2)
                9 push(0)
                10 sup
                11 push(2)
                12 push(2)
                13 cmp
                14 jcstop
                """;
        executeJajaCode(code, stacks);
        // Après not, la pile contient false; après sup, true; après cmp, true
        assertTrue((Boolean) stacks.getTop().value);
    }

    @Test
    void testInc() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(0)
                3 new(y@global, int, VARIABLE, 0)
                4 push(5)
                5 inc(y@global)
                6 load(y@global)
                7 jcstop
                """;
        executeJajaCode(code, stacks);
        assertEquals(5, stacks.getValue("y@global"));
    }

    @Test
    void testWriteln() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(123)
                3 writeln
                4 jcstop
                """;
        executeJajaCode(code, stacks);
        // Pas d'exception = succès
    }

    @Test
    void testWhileLoop() {
        Stacks stacks = new Stacks();
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
        executeJajaCode(code, stacks);
        assertEquals(5, stacks.getValue("x@global"));
    }

    @Test
    void testIfElse() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(1)
                5 if(9)
                6 push(99)
                7 store(x@global)
                8 goto(11)
                9 push(42)
                10 store(x@global)
                11 jcstop
                """;
        executeJajaCode(code, stacks);
        assertEquals(42, stacks.getValue("x@global"));
    }

    @Test
    void testFonctions() {
        Stacks stacks = new Stacks();
        String code = """
                1 init
                2 push(0)
                3 new(x@global, int, var, 0)
                4 push(7)
                5 new(f, int, meth, 0)
                6 goto(11)
                7 new(p@f@int, int, var, 1)
                8 load(p@f@int)
                9 swap
                10 return
                11 push(3)
                12 store(x@global)
                13 load(x@global)
                14 invoke(f)
                15 swap
                16 pop
                17 inc(x@global)
                18 push(0)
                19 swap
                20 pop
                21 swap
                22 pop
                23 pop
                24 jcstop
                """;
        executeJajaCode(code, stacks);
    }
}

