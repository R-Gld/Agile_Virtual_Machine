package fr.ufrst.m1info.gl.groupe7;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode.JajaCodeInterpreterVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JajaCodeInterpreterUnitTest {

    private JajaCodeInterpreterVisitor runJajaCode(String code) {
        SymbolTable memory = new SymbolTable();
        CharStream stream = CharStreams.fromString(code);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext tree = parser.classe();

        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(memory);
        interpreter.load(tree);
        interpreter.run();
        return interpreter;
    }

    @Test
    @Disabled
    void testProgrammePrincipal_Affectation() {
        String code = """
                    1 init
                    2 push(0)
                    3 new(x, int, var, 0)
                    4 push(5)
                    5 store(x)
                    6 push(0)
                    7 swap
                    8 pop
                    9 pop
                    10 jcstop
                    """;

        JajaCodeInterpreterVisitor finalMachine = runJajaCode(code);
        SymbolTable finalMemory = finalMachine.getSymbolTable();

        Symbol symbolX = finalMemory.findSymbol("x");
        assertNotNull(symbolX, "La variable 'x' devrait exister.");
        assertEquals(5, symbolX.getValue(), "La variable 'x' devrait avoir la valeur 5.");
        assertTrue(finalMachine.getStack().isEmpty(), "La pile devrait être vide à la fin.");
    }
}
