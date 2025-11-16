package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourcePositionTest {

    @Test
    void testConstructor() {
        String fileName = "TestFile.java";
        int line = 10;
        int column = 5;

        SourcePosition pos = new SourcePosition(fileName, line, column);

        assertEquals(fileName, pos.fileName());
        assertEquals(line, pos.line());
        assertEquals(column, pos.column());
    }
}
