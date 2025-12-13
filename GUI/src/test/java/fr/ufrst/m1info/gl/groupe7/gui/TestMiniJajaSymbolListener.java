package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMiniJajaSymbolListener {

    private MiniJajaSymbolListener walkSource(String code) {
        MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(code));
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));
        MiniJajaParser.ClasseContext tree = parser.classe();
        MiniJajaSymbolListener listener = new MiniJajaSymbolListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);
        return listener;
    }

    @Test
    void testFunctionDeclarationAndCallAreMarked() {
        String code = "class C {\n" +
            "  int x = 0;\n" +
            "  int foo() {\n" +
            "    int z = 0;\n" +
            "  };\n" +
            "  main {\n" +
            "    int y = 0;\n" +
                "    y = 2;\n" +
                "    y = foo();\n" +
            "  }\n" +
            "}";

        MiniJajaSymbolListener listener = walkSource(code);

        List<String> functions = listener.getFunctionStyleRanges().stream()
            .map(r -> code.substring(r.getStart(), r.getEnd()))
            .collect(Collectors.toList());

        // 'foo' should be present for declaration and call
        long fooCount = functions.stream().filter(s -> s.equals("foo")).count();
        assertEquals(2, fooCount, "Expected two occurrences of 'foo' to be marked (declaration and call)");
    }

    @Test
    void testUnusedVariablesAreReported() {
        String code = "class C {\n" +
            "  int x = 0;\n" +
            "  int foo() {\n" +
            "    int z = 0;\n" +
            "  };\n" +
            "  main {\n" +
            "    int y = 0;\n" +
            "    y = 2;\n" +
            "  }\n" +
            "}";

        MiniJajaSymbolListener listener = walkSource(code);

        List<String> unusedVars = listener.getUnusedVariableRanges().stream()
            .map(r -> code.substring(r.getStart(), r.getEnd()))
            .collect(Collectors.toList());

        // Expect x and z to be unused, but not y
        assertTrue(unusedVars.contains("x"), "Field 'x' should be reported as unused");
        assertTrue(unusedVars.contains("z"), "Local 'z' should be reported as unused");
        assertTrue(!unusedVars.contains("y"), "Local 'y' should not be reported as unused");
    }

    @Test
    void testVoidMethodesCallAreReported() {
        String code = "class C {\n" +
            "  void foo() {\n" +
            "    write(\"42\");\n" +
            "  };\n" +
            "  main {\n" +
            "    foo();\n" +
            "  }\n" +
            "}";

        MiniJajaSymbolListener listener = walkSource(code);

        List<String> functions = listener.getFunctionStyleRanges().stream()
            .map(r -> code.substring(r.getStart(), r.getEnd()))
            .collect(Collectors.toList());

        // 'foo' should be present for declaration and call
        long fooCount = functions.stream().filter(s -> s.equals("foo")).count();
        assertEquals(2, fooCount, "Expected two occurrences of 'foo' to be marked (declaration and call)");
    }

            // A 'declaration after instruction' should be caught by the GUI error highlighter
            // since the grammar does not allow a var declaration after instructions inside a method/main.
}
