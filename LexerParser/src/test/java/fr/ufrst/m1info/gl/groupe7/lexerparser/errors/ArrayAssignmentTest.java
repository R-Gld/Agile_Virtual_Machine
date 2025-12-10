package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayAssignmentTest {

    @Test
    void arrayToArrayAssignment_sameType_shouldBeValid() {
        String code = """
                class synonymie {
                    int t[4];
                    int taille = 4;

                    void f(int x) {
                        int t1[10];
                        int i = 0;

                        while(10 > i) {
                            t1[i] = x;
                            i++;
                        };
                        t = t1;
                        t[x] = taille;
                    };

                    main {
                        int i = 0;

                        while (4 > i) {
                            t[i] = taille - i;
                            i++;
                        };
                        f(3);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();

        CharStream charStream = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("test_synonymie.mjj");
        analyser.analyse(classe);

        // Array to array assignment of the same type should be valid
        assertFalse(collector.hasErrors(), "Array to array assignment of same type should be valid");
    }

    @Test
    void scalarToArrayAssignment_shouldReportSemanticError() {
        String code = """
                class Test {
                    int t[4];
                    int x = 5;

                    main {
                        t = x;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();

        CharStream charStream = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("test.mjj");
        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should detect scalar to array assignment error");
    }

    @Test
    void arrayElementAssignment_shouldBeValid() {
        String code = """
                class Test {
                    int t[4];

                    main {
                        t[0] = 5;
                        t[1] = t[0];
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();

        CharStream charStream = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniJajaParser parser = new MiniJajaParser(tokens);

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        ClasseNode classe = (ClasseNode) visitor.visit(parser.classe());

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("test.mjj");
        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "Array element assignment should be valid");
    }
}
