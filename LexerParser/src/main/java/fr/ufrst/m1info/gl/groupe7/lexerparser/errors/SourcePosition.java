package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Source position.
 * @param fileName the file name
 * @param line the line number
 * @param column the column number
 */
public record SourcePosition(String fileName, int line, int column) {
    public SourcePosition {
        if (fileName != null && !Files.exists(Path.of(fileName))) {
            throw new IllegalArgumentException("File does not exist: " + fileName);
        }
    }
}
