package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception levée lors d'une division par zéro
 */
public class DivisionByZeroException extends JajaCodeRuntimeException {

    public DivisionByZeroException(String axiomeName, int programCounter) {
        super("Division par zéro", axiomeName, programCounter);
    }
}

