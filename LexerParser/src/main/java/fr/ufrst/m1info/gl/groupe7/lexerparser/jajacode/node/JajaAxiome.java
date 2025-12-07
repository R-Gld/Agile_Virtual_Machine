package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;


// Ici ça va servir pour JajaCodeInterpreter
public interface JajaAxiome {
    void execute(MachineContext ctx, String arg);
}
