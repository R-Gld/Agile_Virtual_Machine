package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SourcePositionTest {

    @Test
    void testConstructor() {
        String fileName = null;
        int line = 10;
        int column = 5;

        SourcePosition pos = new SourcePosition(fileName, line, column);

        assertEquals(fileName, pos.fileName());
        assertEquals(line, pos.line());
        assertEquals(column, pos.column());
    }

    @Test
    void testConstructor_WithNullFileName_DoesNotThrow() {
        assertDoesNotThrow(() -> new SourcePosition(null, 1, 1));
    }

    @Test
    void testConstructor_WithNonExistentFile_ThrowsException() {
        String nonExistentFile = "/this/file/does/not/exist/test.java";
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new SourcePosition(nonExistentFile, 1, 1)
        );
        assertTrue(exception.getMessage().contains("File does not exist"));
    }

    @Test
    void testConstructor_WithExistingFile_DoesNotThrow(@TempDir Path tempDir) throws IOException {
        Path tempFile = tempDir.resolve("test.java");
        Files.writeString(tempFile, "// test file");

        assertDoesNotThrow(() -> new SourcePosition(tempFile.toString(), 1, 1));
    }
}
