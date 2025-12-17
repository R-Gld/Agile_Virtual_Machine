package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception when a stack underflow occurs in JajaCode.
 */
public class StackUnderflowException extends JajaCodeRuntimeException {

    public StackUnderflowException(String axiomeName, int programCounter) {
        super("Pile vide : impossible d'effectuer l'opération", axiomeName, programCounter);
    }

    public StackUnderflowException(String message, String axiomeName, int programCounter) {
        super(message, axiomeName, programCounter);
    }
}

