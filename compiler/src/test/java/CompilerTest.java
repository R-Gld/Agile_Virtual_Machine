import fr.ufrst.m1info.gl.groupe7.compiler.Compiler;
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
}
