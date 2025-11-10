package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

class SiNodeTest {

    @Test
    void interpret_whenConditionTrue_setsChildrenToThenBranch() {
        Expression cond = Mockito.mock(Expression.class);
        InstructionsNode thenNode = Mockito.mock(InstructionsNode.class);
        AstNode child = Mockito.mock(AstNode.class);
        Iterable<AstNode> thenChildren = Collections.singletonList(child);

        Mockito.when(cond.evaluate(Mockito.any(Stacks.class))).thenReturn(Boolean.TRUE);
        Mockito.when(thenNode.getChildren()).thenReturn(thenChildren);

        SiNode si = new SiNode(cond, thenNode);
        Stacks stacks = Mockito.mock(Stacks.class);

        si.interpret(stacks);

        assertSame(thenChildren, si.getChildren());
        Mockito.verify(cond).evaluate(Mockito.any(Stacks.class));
        Mockito.verify(thenNode).getChildren();
    }

    @Test
    void interpret_whenConditionFalseWithElse_setsChildrenToElseBranch() {
        Expression cond = Mockito.mock(Expression.class);
        InstructionsNode thenNode = Mockito.mock(InstructionsNode.class);
        InstructionsNode elseNode = Mockito.mock(InstructionsNode.class);
        AstNode elseChild = Mockito.mock(AstNode.class);
        Iterable<AstNode> elseChildren = Collections.singletonList(elseChild);

        Mockito.when(cond.evaluate(Mockito.any(Stacks.class))).thenReturn(Boolean.FALSE);
        Mockito.when(elseNode.getChildren()).thenReturn(elseChildren);

        SiNode si = new SiNode(cond, thenNode, elseNode);
        Stacks stacks = Mockito.mock(Stacks.class);

        si.interpret(stacks);

        assertSame(elseChildren, si.getChildren());
        Mockito.verify(cond).evaluate(Mockito.any(Stacks.class));
        Mockito.verify(elseNode).getChildren();
    }

    @Test
    void interpret_whenConditionFalseWithoutElse_leavesChildrenNull() {
        Expression cond = Mockito.mock(Expression.class);
        InstructionsNode thenNode = Mockito.mock(InstructionsNode.class);

        Mockito.when(cond.evaluate(Mockito.any(Stacks.class))).thenReturn(Boolean.FALSE);

        SiNode si = new SiNode(cond, thenNode);
        Stacks stacks = Mockito.mock(Stacks.class);

        si.interpret(stacks);

        assertNull(si.getChildren());
        Mockito.verify(cond).evaluate(Mockito.any(Stacks.class));
    }
}