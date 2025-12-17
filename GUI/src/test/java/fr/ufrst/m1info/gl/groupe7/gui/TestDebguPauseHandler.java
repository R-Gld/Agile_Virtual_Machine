package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

public class TestDebguPauseHandler {
    @Test
    public void testHandlePauseSetStateToStop() {
        Debug debug = Mockito.mock(Debug.class);
        Mockito.when(debug.isPaused()).thenReturn(true);
        AstNode node = Mockito.mock(AstNode.class);

        DebugPauseHandler pauseHandler = new DebugPauseHandler(debug, FXCollections.observableArrayList());

        Stacks stacks = Mockito.mock(Stacks.class);
        Mockito.when(stacks.getStackFromTopToBottom()).thenReturn(new ArrayList<>());


        Thread thread = new Thread(() -> {
            Assertions.assertFalse(pauseHandler.handlePause(1, node, stacks));
        });

        thread.start();
        pauseHandler.updateStatus(Status.STOP);


    }
}
