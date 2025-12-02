package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

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
 * Tests pour vérifier que write et writeln supportent les chaînes avec espaces.
 */
class WriteWithSpacesTest {

    @Test
    void writeln_withStringContainingSpaces_compilesAndExecutes() {
        String program = """
            class Test {
              main {
                writeln("hello world");
              }
            }
            """;

        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(program);
        String jajaCode = compiler.getJajaCodeBuilder().toStringForInterpreter();

        System.out.println("Code JajaCode généré:");
        System.out.println(jajaCode);

        // Vérifier que le code contient la chaîne avec guillemets
        assertTrue(jajaCode.contains("\"hello world\""),
            "Le code doit contenir la chaîne 'hello world' entre guillemets");

        // Interpréter le code
        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbre = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbre);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("hello world"),
            "La sortie doit contenir 'hello world'");
    }

    @Test
    void write_multipleStringsWithSpaces_compilesAndExecutes() {
        String program = """
            class Test {
              main {
                write("Hello ");
                writeln("World!");
              }
            }
            """;

        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(program);
        String jajaCode = compiler.getJajaCodeBuilder().toStringForInterpreter();

        System.out.println("Code JajaCode généré:");
        System.out.println(jajaCode);

        assertTrue(jajaCode.contains("\"Hello \""),
            "Le code doit contenir 'Hello ' avec espace");
        assertTrue(jajaCode.contains("\"World!\""),
            "Le code doit contenir 'World!'");

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbre = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbre);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("Hello "), "La sortie doit contenir 'Hello '");
        assertTrue(output.contains("World!"), "La sortie doit contenir 'World!'");
    }

    @Test
    void directJajaCode_stringWithSpaces_executesCorrectly() {
        String jajaCode = """
            1 init
            2 push("hello world")
            3 writeln
            4 jcstop
            """;

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbre = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbre);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("hello world"),
            "La sortie doit contenir 'hello world'");
    }

    @Test
    void writeln_withLongStringWithMultipleSpaces_works() {
        String program = """
            class Test {
              main {
                writeln("This is a long string with multiple spaces");
              }
            }
            """;

        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(program);
        String jajaCode = compiler.getJajaCodeBuilder().toStringForInterpreter();

        CharStream stream = CharStreams.fromString(jajaCode);
        JajaCodeLexer lexer = new JajaCodeLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JajaCodeParser parser = new JajaCodeParser(tokens);
        JajaCodeParser.ClasseContext arbre = parser.classe();

        Stacks stacks = new Stacks();
        JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
        interpreter.load(arbre);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            interpreter.run();
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("This is a long string with multiple spaces"),
            "La sortie doit contenir la chaîne complète");
    }
}
