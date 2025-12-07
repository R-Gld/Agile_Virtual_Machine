package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

class TantqueNodeTest {

    @Test
    void interpret_whenConditionTrue_appendsLoopNodeToChildren() {
        Expression cond = Mockito.mock(Expression.class);
        InstructionsNode body = Mockito.mock(InstructionsNode.class);
        AstNode child = Mockito.mock(AstNode.class);

        List<AstNode> bodyChildren = Collections.singletonList(child);
        Mockito.when(cond.evaluate(Mockito.any(Stacks.class))).thenReturn(Boolean.TRUE);
        Mockito.when(body.getChildren()).thenReturn(bodyChildren);

        TantqueNode tq = new TantqueNode(cond, body);
        Stacks stacks = Mockito.mock(Stacks.class);

        tq.interpret(stacks);

        Iterable<AstNode> it = tq.getChildren();
        assertNotNull(it);

        List<AstNode> children = new ArrayList<>();
        for (AstNode n : it) children.add(n);

        // body children plus the appended TantqueNode
        assertEquals(bodyChildren.size() + 1, children.size());
        assertSame(child, children.getFirst());
        assertInstanceOf(TantqueNode.class, children.getLast());

        Mockito.verify(cond).evaluate(Mockito.any(Stacks.class));
        Mockito.verify(body).getChildren();
    }

    @Test
    void interpret_whenConditionFalse_setsChildrenEmpty() {
        Expression cond = Mockito.mock(Expression.class);
        InstructionsNode body = Mockito.mock(InstructionsNode.class);

        Mockito.when(cond.evaluate(Mockito.any(Stacks.class))).thenReturn(Boolean.FALSE);

        TantqueNode tq = new TantqueNode(cond, body);
        Stacks stacks = Mockito.mock(Stacks.class);

        tq.interpret(stacks);

        Iterable<AstNode> it = tq.getChildren();
        assertNotNull(it);

        List<AstNode> children = new ArrayList<>();
        for (AstNode n : it) children.add(n);

        assertEquals(0, children.size());
        Mockito.verify(cond).evaluate(Mockito.any(Stacks.class));
    }
}