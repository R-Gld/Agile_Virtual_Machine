package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class MachineContext {
    private Stacks stacks;
    private int instructionCounter;
    private boolean running;

    public MachineContext(Stacks stacks) {
        this.stacks = stacks;
        this.instructionCounter = 0;
        this.running = true;
    }

    public void incrementPC() {
        this.instructionCounter++;
    }

    public void stop() {
        this.running = false;
    }

    public Stacks getStacks() {
        return stacks;
    }

    public void setStacks(Stacks stacks) {
        this.stacks = stacks;
    }

    public int getInstructionCounter() {
        return instructionCounter;
    }

    public void setInstructionCounter(int adresse) {
        this.instructionCounter = adresse;
    }

    public boolean isRunning() {
        return running;
    }
}