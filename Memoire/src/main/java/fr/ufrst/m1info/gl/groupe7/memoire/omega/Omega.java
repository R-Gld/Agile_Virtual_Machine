package fr.ufrst.m1info.gl.groupe7.memoire.omega;

public class Omega {
    private static final Omega instance = new Omega();

    private Omega() {
    }

    public static Omega getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "w";
    }
}
