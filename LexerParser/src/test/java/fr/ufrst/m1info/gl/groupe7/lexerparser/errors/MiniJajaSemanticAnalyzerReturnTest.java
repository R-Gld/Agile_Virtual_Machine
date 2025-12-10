package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MiniJajaSemanticAnalyzerReturnTest {

    @Test
    void returnTypeMismatch_shouldReportError() {
        String code = """
                class C {
                    int x;

                    boolean f(boolean a) {
                        return 15;
                    };

                    main {
                        x = 12;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);
        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report error for return type mismatch");

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Return type mismatch"));
        assertTrue(diag.message().contains("boolean"));
        assertTrue(diag.message().contains("integer"));
    }

    @Test
    void correctReturnType_shouldNotReportError() {
        String code = """
                class C {
                    int x;

                    int f(boolean a) {
                        return 15;
                    };

                    main {
                        x = 12;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);
        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "No error expected for correct return type");
    }

    @Test
    void returnInMain_shouldReportError() {
        String code = """
                class C {
                    main {
                        return 5;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);
        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report error for return in main");

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("can only be used inside a method"));
    }

    // Helper methods
    private ClasseNode parseClasse(String code, DiagnosticCollector collector) {
        CharStream cs = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, "Test.mjj");
        sel.register(lexer);
        sel.register(parser);

        ParseTree tree = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        AstNode astRoot = visitor.visit(tree);

        assertNotNull(astRoot, "L'AST ne doit pas être null");
        assertInstanceOf(ClasseNode.class, astRoot, "La racine doit être une ClasseNode");

        return (ClasseNode) astRoot;
    }

    private Diagnostic firstSemanticError(DiagnosticCollector collector) {
        return collector.getDiagnostics().stream()
                .filter(d -> d.severity() == Severity.ERROR && d.phase() == Phase.SEMANTIC)
                .findFirst()
                .orElse(null);
    }
}
