package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception de base pour les erreurs d'exécution du code JajaCode
 */
public class JajaCodeRuntimeException extends RuntimeException {

    private final int programCounter;
    private final String axiomeName;

    public JajaCodeRuntimeException(String message, String axiomeName, int programCounter) {
        super(String.format("[PC=%d, Axiome=%s] %s", programCounter, axiomeName, message));
        this.axiomeName = axiomeName;
        this.programCounter = programCounter;
    }

    public JajaCodeRuntimeException(String message, String axiomeName, int programCounter, Throwable cause) {
        super(String.format("[PC=%d, Axiome=%s] %s", programCounter, axiomeName, message), cause);
        this.axiomeName = axiomeName;
        this.programCounter = programCounter;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public String getAxiomeName() {
        return axiomeName;
    }
}

