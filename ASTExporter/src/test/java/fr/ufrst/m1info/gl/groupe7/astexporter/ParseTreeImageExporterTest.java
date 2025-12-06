package fr.ufrst.m1info.gl.groupe7.astexporter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ParseTreeImageExporterTest {

    @Test
    public void testImageGeneration() throws IOException {
        String code = "class Test { main { int x = 1; write(x); } }";
        Path tempInput = Files.createTempFile("test_input", ".mjj");
        Files.writeString(tempInput, code);

        Path tempOutput = Files.createTempFile("test_output", ".png");
        Files.delete(tempOutput);

        String[] args = { "@" + tempInput.toAbsolutePath(), tempOutput.toAbsolutePath().toString() };
        int exitCode = ParseTreeImageExporter.run(args);

        assertEquals(0, exitCode, "Exit code should be 0");
        assertTrue(Files.exists(tempOutput), "Output file should exist");
        assertTrue(Files.isRegularFile(tempOutput), "Output file should be a file");
        assertTrue(Files.size(tempOutput) > 0, "Output file should not be empty");

        BufferedImage image = ImageIO.read(tempOutput.toFile());
        assertNotNull(image, "Should be able to read the generated image");
        assertTrue(image.getWidth() > 0, "Image width should be > 0");
        assertTrue(image.getHeight() > 0, "Image height should be > 0");

        Files.deleteIfExists(tempInput);
        Files.deleteIfExists(tempOutput);
    }

    @Test
    public void testMissingArguments() {
        int exitCode = ParseTreeImageExporter.run(new String[] {});
        assertEquals(1, exitCode, "Should return 1 when no arguments provided");
    }

    @Test
    public void testFileNotFound() {
        int exitCode = ParseTreeImageExporter.run(new String[] { "@nonexistent_file_12345.mjj" });
        assertEquals(2, exitCode, "Should return 2 when file not found");
    }

    @Test
    public void testEmptySource() {
        int exitCode = ParseTreeImageExporter.run(new String[] { "   " });
        assertEquals(3, exitCode, "Should return 3 when source is empty or blank");
    }

    @Test
    public void testSyntaxError() {
        int exitCode = ParseTreeImageExporter.run(new String[] { "class { invalid syntax }" });
        assertEquals(4, exitCode, "Should return 4 when syntax errors occur");
    }

    @Test
    public void testDirectCodeInput() throws IOException {
        Path tempOutput = Files.createTempFile("test_direct", ".png");
        Files.delete(tempOutput);

        int exitCode = ParseTreeImageExporter
                .run(new String[] { "class A { main { } }", tempOutput.toAbsolutePath().toString() });

        assertEquals(0, exitCode, "Should return 0 for valid direct code input");
        assertTrue(Files.exists(tempOutput), "Output file should be created");

        Files.deleteIfExists(tempOutput);
    }

    @Test
    public void testDefaultOutputPath() throws IOException {
        String code = "class Default { main { } }";
        Path tempInput = Files.createTempFile("test_default", ".mjj");
        Files.writeString(tempInput, code);

        Path defaultOutput = Path.of("target/ast-images/parse_tree.png");
        Files.deleteIfExists(defaultOutput);

        int exitCode = ParseTreeImageExporter.run(new String[] { "@" + tempInput.toAbsolutePath()});

        assertEquals(0, exitCode, "Should return 0 when using default output path");
        assertTrue(Files.exists(defaultOutput), "Default output file should be created");

        Files.deleteIfExists(tempInput);
    }

    // @Test
    // public void testAstConstructionFailure() throws IOException {

    //     String code = "class Fail { main { f(); } }";
    //     Path tempInput = Files.createTempFile("test_fail", ".mjj");
    //     Files.writeString(tempInput, code);

    //     Path tempOutput = Files.createTempFile("test_fail_output", ".png");
    //     Files.delete(tempOutput);

    //     java.io.PrintStream originalOut = System.out;
    //     java.io.PrintStream originalErr = System.err;
    //     java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
    //     java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
    //     System.setOut(new java.io.PrintStream(outContent));
    //     System.setErr(new java.io.PrintStream(errContent));

    //     try {
    //         int exitCode = ParseTreeImageExporter.run(new String[] { "@" + tempInput.toAbsolutePath(),
    //                 tempOutput.toAbsolutePath().toString() });


    //         assertEquals(0, exitCode, "Should return 0 even if AST fails (fallback to CST)");
    //         assertTrue(Files.exists(tempOutput), "Output file should be created (CST)");

    //         String errOutput = errContent.toString();
    //         String outOutput = outContent.toString();

    //         boolean fallbackTriggered = errOutput.contains("Erreur lors de la construction de l'AST")
    //                 || outOutput.contains("Arbre CST exporté");

    //         assertTrue(fallbackTriggered,
    //                 "Should have triggered fallback to CST export. Logs:\nOut: " + outOutput + "\nErr: " + errOutput);

    //     } finally {
    //         System.setOut(originalOut);
    //         System.setErr(originalErr);
    //         Files.deleteIfExists(tempInput);
    //         Files.deleteIfExists(tempOutput);
    //     }
    // }
}
