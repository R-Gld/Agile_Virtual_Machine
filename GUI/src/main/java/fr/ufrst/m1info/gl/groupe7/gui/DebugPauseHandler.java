package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import javafx.collections.ObservableList;

import java.util.List;

public class DebugPauseHandler {
    private Status status;
    private final Debug debugWalker;
    private final ObservableList<StackModel> memoryList;

    public DebugPauseHandler(Debug debugWalker,  ObservableList<StackModel> memoryList) {
        this.status = Status.WAITING;
        this.debugWalker = debugWalker;
        this.memoryList = memoryList;
    }

    public boolean handlePause(int line, AstNode node, Stacks stacks) {
        updateStack(stacks);
        updateStatus(Status.WAITING);
        while (debugWalker.isPaused()) {
            switch (status) {
                case WAITING:
                    continue;
                case NEXT_STEP:
                    debugWalker.step();
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
        memoryList.clear();
        List<Stacks.Quad> stack = stacks.getStackFromTopToBottom();
        for (int i = 0; i < stack.size(); i++) {
            memoryList.add(new StackModel(stack.get(i), stack.size()-i));
        }
        stacks.printStack();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
