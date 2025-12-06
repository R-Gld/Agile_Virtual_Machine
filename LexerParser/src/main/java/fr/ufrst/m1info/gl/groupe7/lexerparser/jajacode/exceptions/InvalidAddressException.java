package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions;

/**
 * Exception levée lorsqu'une adresse est invalide
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

