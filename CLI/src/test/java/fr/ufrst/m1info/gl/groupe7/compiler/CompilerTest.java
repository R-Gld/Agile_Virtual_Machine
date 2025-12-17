package fr.ufrst.m1info.gl.groupe7.compiler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {

    /**
     * Minimal valid MiniJaja program for your grammar.
     * Adjust it if necessary (e.g., add a main, declarations, etc.).
     */
    private static String minimalValidMiniJaja() {
        try {
            return Files.readString(MainCompilerCompilerTest.resourceAsPath("inputs/minimal_ok_release1.mjj"));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------
    //   CONSTRUCTORS
    // ----------------------

    @Test
    void constructor_fromPath_andOutput_doesNotThrow(@TempDir Path tmp) throws IOException {
        Path in = tmp.resolve("in.mjj");
        Path out = tmp.resolve("out.jjc");
        Files.writeString(in, minimalValidMiniJaja(), StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> new Compiler(in, Compiler.Destination.FILE, out));
    }

    @Test
    void constructor_fromPath_only_doesNotThrow(@TempDir Path tmp) throws IOException {
        Path in = tmp.resolve("in.mjj");
        Files.writeString(in, minimalValidMiniJaja(), StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> new Compiler(in, Compiler.Destination.SYSOUT));
    }

    @Test
    void constructor_fromString_only_doesNotThrow() {
        assertDoesNotThrow(() -> new Compiler(minimalValidMiniJaja(), Compiler.Destination.STRING, null));
    }

    @Test
    void constructor_fromString_andOutput_doesNotThrow(@TempDir Path tmp) {
        Path out = tmp.resolve("out.jjc");
        assertDoesNotThrow(() -> new Compiler(minimalValidMiniJaja(), Compiler.Destination.FILE, out));
    }

    @Test
    void run_writesToFile_whenOutputFileProvided(@TempDir Path tmp) throws IOException {
        Path out = tmp.resolve("compiled.jjc");
        Compiler compiler = new Compiler(minimalValidMiniJaja(), Compiler.Destination.FILE, out);

        assertDoesNotThrow(compiler::run);
        assertTrue(Files.exists(out), "Output file should be created");
        assertTrue(Files.isRegularFile(out), "Output path should be a regular file");
        String fileContent = Files.readString(out, StandardCharsets.UTF_8);
        assertEquals(compiler.getLastOutput(), fileContent, "File content must match lastOutput");
    }

    @Test
    void run_writesToStdout_whenNoOutputFileProvided() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));

        try {
            Compiler compiler = new Compiler(minimalValidMiniJaja(), Compiler.Destination.SYSOUT, null);
            assertDoesNotThrow(compiler::run);
            String outStr = capture.toString(StandardCharsets.UTF_8);
            assertFalse(outStr.isEmpty(), "stdout redirection should have captured the execution");
            assertEquals(compiler.getLastOutput(), outStr, "stdout content must equal lastOutput");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void run_destinationSTRING_setsLastOutput_andDoesNotWriteStdout() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));

        try {
            Compiler compiler = new Compiler(minimalValidMiniJaja(), Compiler.Destination.STRING, null);
            assertDoesNotThrow(compiler::run);
            assertTrue(compiler.getLastOutput() != null && !compiler.getLastOutput().isEmpty(),
                    "lastOutput should be set in STRING mode");
            assertEquals(0, capture.size(), "Destination.STRING must not write to stdout");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void run_FILE_withoutOutputPath_throws() {
        Compiler compiler = new Compiler(minimalValidMiniJaja(), Compiler.Destination.FILE, null);
        assertThrows(IllegalStateException.class, compiler::run);
    }

    @Test
    void compileToString_hasNoSideEffects_onStdout_andDoesNotTouchLastOutput() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));

        try {
            Compiler compiler = new Compiler(minimalValidMiniJaja(), Compiler.Destination.SYSOUT, null);
            String s = compiler.compileToString();
            assertNotNull(s);
            assertFalse(s.isEmpty());
            assertEquals(0, capture.size(), "compileToString() must not write to stdout");
            assertEquals("", compiler.getLastOutput(), "compileToString() must not update lastOutput");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Test de bout en bout : compilation d'un programme MiniJaja avec une méthode et writeln.
     */
    @Test
    void compile_programWithMethodAndWriteln_generatesCorrectJajaCode() {
        String miniJajaCode = """
            class TestMethod {
                int double(int n) {
                    return n;
                };
                main {
                    int result = 0;
                    result = double(42);
                    writeln(result);
                }
            }
            """;

        // Compiler le code
        Compiler compiler = new Compiler(miniJajaCode, Compiler.Destination.STRING, null);
        assertDoesNotThrow(compiler::run, "Compilation should succeed without errors");

        String jajaCode = compiler.getLastOutput();

        // Vérifications sur le code JajaCode généré
        assertNotNull(jajaCode, "JajaCode output should not be null");
        assertFalse(jajaCode.isEmpty(), "JajaCode output should not be empty");

        // Vérifier la présence des instructions clés pour la déclaration de méthode
        assertTrue(jajaCode.contains("push("),
            "Should contain push instruction for method address");
        assertTrue(jajaCode.contains("new(double"),
            "Should declare method 'double'");
        assertTrue(jajaCode.contains("meth"),
            "Method should be declared with 'meth' kind");
        assertTrue(jajaCode.contains("goto("),
            "Should contain goto to skip method body");

        // Vérifier la présence de l'instruction return dans le corps de la méthode
        assertTrue(jajaCode.contains("return"),
            "Method body should end with return instruction");

        // Vérifier la présence de l'appel de méthode
        assertTrue(jajaCode.contains("invoke(double"),
            "Should contain invoke instruction to call the method");

        // Vérifier la présence de writeln
        assertTrue(jajaCode.contains("writeln"),
            "Should contain writeln instruction");

        // Vérifier que la valeur 42 est présente (argument de la méthode)
        assertTrue(jajaCode.contains("push(42)"),
            "Should push the argument value 42");

        // Afficher le code généré pour debug (optionnel)
        System.out.println("=== Generated JajaCode ===");
        System.out.println(jajaCode);
        System.out.println("==========================");
    }

    /**
     * Test de bout en bout : compilation d'un programme avec une méthode qui utilise ses paramètres.
     */
    @Test
    void compile_methodWithParameter_compilesSuccessfully() {
        String miniJajaCode = """
            class TestParam {
                int add(int a) {
                    int result = 0;
                    result = a;
                    return result;
                };
                main {
                    int x = 0;
                    x = add(10);
                    writeln(x);
                }
            }
            """;

        Compiler compiler = new Compiler(miniJajaCode, Compiler.Destination.STRING, null);
        assertDoesNotThrow(compiler::run, "Compilation with method parameters should succeed");

        String jajaCode = compiler.getLastOutput();

        // Vérifications de base
        assertNotNull(jajaCode);
        assertFalse(jajaCode.isEmpty());

        // Vérifier les éléments spécifiques
        assertTrue(jajaCode.contains("new(add"), "Should declare method 'add'");
        assertTrue(jajaCode.contains("invoke(add"), "Should invoke method 'add'");
        assertTrue(jajaCode.contains("push(10)"), "Should push argument 10");
        assertTrue(jajaCode.contains("writeln"), "Should contain writeln");
    }

    /**
     * Test de bout en bout : compilation d'un programme avec appel de méthode dans une expression.
     */
    @Test
    void compile_methodCallInExpression_compilesSuccessfully() {
        String miniJajaCode = """
            class TestExpr {
                int getValue() {
                    return 5;
                };
                main {
                    int result = 0;
                    result = getValue();
                    writeln(result);
                }
            }
            """;

        Compiler compiler = new Compiler(miniJajaCode, Compiler.Destination.STRING, null);
        assertDoesNotThrow(compiler::run, "Method call in expression should compile");

        String jajaCode = compiler.getLastOutput();
        assertTrue(jajaCode.contains("invoke(getValue"),
            "Should invoke getValue method");
        assertTrue(jajaCode.contains("store(result@main)"),
            "Should store result in main scope");
    }
}
