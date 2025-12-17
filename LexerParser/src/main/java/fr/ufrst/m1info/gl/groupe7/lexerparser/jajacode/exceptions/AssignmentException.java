package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception when an assignment fails in JajaCode.
 */
public class AssignmentException extends JajaCodeRuntimeException {

    private final String identifier;

    public AssignmentException(String identifier, String reason, String axiomeName, int programCounter) {
        super(String.format("Échec de l'affectation pour '%s' : %s", identifier, reason),
              axiomeName, programCounter);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}

