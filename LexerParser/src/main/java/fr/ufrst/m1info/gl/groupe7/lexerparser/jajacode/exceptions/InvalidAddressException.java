package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception when an invalid address is accessed in JajaCode.
 */
public class InvalidAddressException extends JajaCodeRuntimeException {

    private final String address;

    public InvalidAddressException(String address, String axiomeName, int programCounter) {
        super("Adresse invalide : '" + address + "'", axiomeName, programCounter);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}

