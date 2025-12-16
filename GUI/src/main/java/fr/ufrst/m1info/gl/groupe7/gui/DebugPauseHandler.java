package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import javafx.collections.ObservableList;

public class DebugPauseHandler {
    private Status status;
    private final Debug debugWalker;
    private ObservableList<StackModel> memoryList;

    public DebugPauseHandler(Debug debugWalker,  ObservableList<StackModel> memoryList) {
        this.status = Status.WAITING;
        this.debugWalker = debugWalker;
        this.memoryList = memoryList;
    }

    public boolean handlePause(int line, AstNode node, Stacks stacks) {
        updateStack(stacks);
        while (debugWalker.isPaused()) {
            switch (status) {
                case WAITING:
                    continue;
                case NEXT_STEP:
                    debugWalker.continueStepByStep();
                    return true;
                case NEXT_BREAKPOINT:
                    debugWalker.continueToNextBreakpoint();
                    return true;
                case STOP:
                    debugWalker.stop();
                    return false;
            }
        }
        return true;
    }

    public void updateStack(Stacks stacks) {
        stacks.printStack();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
