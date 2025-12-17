package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

/**
 * Source position.
 * @param fileName the file name
 * @param line the line number
 * @param column the column number
 */
public record SourcePosition(String fileName, int line, int column) {}
