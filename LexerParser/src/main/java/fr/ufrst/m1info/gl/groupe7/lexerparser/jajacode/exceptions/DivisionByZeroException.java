package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception when a division by zero occurs in JajaCode.
 */
public class DivisionByZeroException extends JajaCodeRuntimeException {

    public DivisionByZeroException(String axiomeName, int programCounter) {
        super("Division par zéro", axiomeName, programCounter);
    }
}

