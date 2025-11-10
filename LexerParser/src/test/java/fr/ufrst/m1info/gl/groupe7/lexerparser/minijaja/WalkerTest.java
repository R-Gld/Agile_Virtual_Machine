package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.Arrays;
import java.util.Collections;

class WalkerTest {
    @Test
    void walk_withNullRoot_doesNotThrow() {
        Stacks stacks = Mockito.mock(Stacks.class);
        Assertions.assertDoesNotThrow(() -> new Walker(null, stacks).walk());
    }

    @Test
    void walk_traverses_and_callsinterpret_on_all_nodes() {
        AstNode parent = Mockito.mock(AstNode.class);
        AstNode child1 = Mockito.mock(AstNode.class);
        AstNode child2 = Mockito.mock(AstNode.class);

        Stacks stacks = Mockito.mock(Stacks.class);

        Mockito.when(parent.getChildren()).thenReturn(Arrays.asList(child1, child2));
        Mockito.when(child1.getChildren()).thenReturn(Collections.emptyList());
        Mockito.when(child2.getChildren()).thenReturn(Collections.emptyList());

        Walker walker = new Walker(parent, stacks);
        walker.walk();

        Mockito.verify(parent, Mockito.times(1)).interpret(stacks);
        Mockito.verify(child1, Mockito.times(1)).interpret(stacks);
        Mockito.verify(child2, Mockito.times(1)).interpret(stacks);
    }

    @Test
    void walk_callsinterpret_in_preorder() {
        AstNode parent = Mockito.mock(AstNode.class);
        AstNode child1 = Mockito.mock(AstNode.class);
        AstNode child2 = Mockito.mock(AstNode.class);

        Stacks stacks = Mockito.mock(Stacks.class);

        Mockito.when(parent.getChildren()).thenReturn(Arrays.asList(child1, child2));
        Mockito.when(child1.getChildren()).thenReturn(Collections.emptyList());
        Mockito.when(child2.getChildren()).thenReturn(Collections.emptyList());

        Walker walker = new Walker(parent, stacks);
        walker.walk();

        InOrder inOrder = Mockito.inOrder(parent, child1, child2);
        inOrder.verify(parent).interpret(stacks);
        inOrder.verify(child1).interpret(stacks);
        inOrder.verify(child2).interpret(stacks);
    }

    @Test
    void walk_handles_null_child_in_children_list() {
        AstNode parent = Mockito.mock(AstNode.class);
        AstNode child1 = Mockito.mock(AstNode.class);
        AstNode child2 = Mockito.mock(AstNode.class);

        Stacks stacks = Mockito.mock(Stacks.class);

        Mockito.when(parent.getChildren()).thenReturn(Arrays.asList(child1, null, child2));
        Mockito.when(child1.getChildren()).thenReturn(Collections.emptyList());
        Mockito.when(child2.getChildren()).thenReturn(Collections.emptyList());

        Walker walker = new Walker(parent, stacks);

        Assertions.assertDoesNotThrow(walker::walk);

        Mockito.verify(parent, Mockito.times(1)).interpret(stacks);
        Mockito.verify(child1, Mockito.times(1)).interpret(stacks);
        Mockito.verify(child2, Mockito.times(1)).interpret(stacks);
    }

    @Test
    void walk_traverses_deep_tree_all_nodes_called_once() {
        AstNode n1 = Mockito.mock(AstNode.class);
        AstNode n2 = Mockito.mock(AstNode.class);
        AstNode n3 = Mockito.mock(AstNode.class);
        AstNode n4 = Mockito.mock(AstNode.class);

        Stacks stacks = Mockito.mock(Stacks.class);

        Mockito.when(n1.getChildren()).thenReturn(Arrays.asList(n2, n3));
        Mockito.when(n2.getChildren()).thenReturn(Collections.singletonList(n4));
        Mockito.when(n3.getChildren()).thenReturn(Collections.emptyList());
        Mockito.when(n4.getChildren()).thenReturn(Collections.emptyList());

        Walker walker = new Walker(n1, stacks);
        walker.walk();

        Mockito.verify(n1, Mockito.times(1)).interpret(stacks);
        Mockito.verify(n2, Mockito.times(1)).interpret(stacks);
        Mockito.verify(n3, Mockito.times(1)).interpret(stacks);
        Mockito.verify(n4, Mockito.times(1)).interpret(stacks);
    }
}