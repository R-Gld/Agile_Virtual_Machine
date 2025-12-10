package fr.ufrst.m1info.gl.groupe7.memoire.omega;

/**
 * Omega singleton class representing the special Omega value in the memory model.
 * Used to denote uninitialized value.
 */
public class Omega {

    private static final Omega instance = new Omega();
    private Omega() {}
    public static Omega getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "w";
    }
}
