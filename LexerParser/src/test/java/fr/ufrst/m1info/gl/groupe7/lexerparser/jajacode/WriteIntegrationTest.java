package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration complet pour write et writeln :
 * MiniJaja -> Compilation -> JajaCode -> Interprétation
 */
class WriteIntegrationTest {

    @Test
    void fullPipeline_writeAndWriteln_executesCorrectly() {
        // 1. Programme MiniJaja source
        String miniJajaProgram = """
            class Test {
              int x = 6;
              main {
                write(x);
                writeln("ok");
              }
            }
            """;

        // 2. Compilation MiniJaja -> JajaCode
        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(miniJajaProgram);
        String jajaCode = compiler.getJajaCodeBuilder().toStringForInterpreter();

        System.out.println("=== Code JajaCode généré ===");
        System.out.println(jajaCode);

        // Vérifications de la compilation
        assertTrue(jajaCode.contains("write"), "Le code compilé doit contenir write");
        assertTrue(jajaCode.contains("writeln"), "Le code compilé doit contenir writeln");

        // 3. Parsing du code JajaCode
        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        assertNotNull(arbreJajaCode, "Le parsing JajaCode doit réussir");

        // 4. Interprétation JajaCode
        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbreJajaCode);

        // Capturer la sortie standard
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        System.out.println("=== Sortie du programme ===");
        System.out.println(output);

        // Vérifications de la sortie
        // On s'attend à "6ok\n" mais il y a aussi les logs de debug
        assertTrue(output.contains("6"), "La sortie doit contenir la valeur 6");
        assertTrue(output.contains("ok"), "La sortie doit contenir 'ok'");
    }

    @Test
    void fullPipeline_multipleWrites_executesCorrectly() {
        String miniJajaProgram = """
            class Test {
              int a = 1;
              int b = 2;
              main {
                write(a);
                write(b);
                writeln("done");
              }
            }
            """;

        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(miniJajaProgram);
        String jajaCode = compiler.getJajaCodeBuilder().toStringForInterpreter();

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbreJajaCode);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("1"), "La sortie doit contenir 1");
        assertTrue(output.contains("2"), "La sortie doit contenir 2");
        assertTrue(output.contains("done"), "La sortie doit contenir 'done'");
    }

    @Test
    void directJajaCode_write_executesCorrectly() {
        String jajaCode = """
            1 init
            2 push(42)
            3 write
            4 push(100)
            5 writeln
            6 jcstop
            """;

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbreJajaCode = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbreJajaCode);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("42"), "La sortie doit contenir 42");
        assertTrue(output.contains("100"), "La sortie doit contenir 100");
    }
}
