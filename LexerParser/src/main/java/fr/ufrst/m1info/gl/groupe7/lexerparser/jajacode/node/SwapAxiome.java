package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.MachineContext;

public class SwapAxiome implements JajaAxiome {

    @Override
    public void execute(MachineContext ctx, String arg) {
        ctx.getStacks().swap();
        ctx.incrementPC();
    }
}
