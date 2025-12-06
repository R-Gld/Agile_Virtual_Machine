package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception levée lors d'une incompatibilité de types
 */
public class TypeMismatchException extends JajaCodeRuntimeException {

    private final String expectedType;
    private final String actualType;

    public TypeMismatchException(String expectedType, String actualType, String axiomeName, int programCounter) {
        super(String.format("Incompatibilité de types : attendu=%s, reçu=%s", expectedType, actualType),
              axiomeName, programCounter);
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public TypeMismatchException(String message, String axiomeName, int programCounter) {
        super(message, axiomeName, programCounter);
        this.expectedType = null;
        this.actualType = null;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public String getActualType() {
        return actualType;
    }
}

