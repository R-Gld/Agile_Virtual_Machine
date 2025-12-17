package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.compiler.MainCompiler;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            App.main(args);
        } else {
            MainCompiler.main(args);
        }
    }
}
