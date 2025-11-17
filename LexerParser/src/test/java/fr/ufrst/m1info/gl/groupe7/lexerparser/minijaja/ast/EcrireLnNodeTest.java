package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireLnNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EcrireLnNodeTest {

    @Test
    public void toStringTree_withString_returnsStringWrapped() {
        EcrireLnNode node = new EcrireLnNode("HelloLn");

        String tree = node.toStringTree();

        assertEquals("ecrireln (HelloLn)", tree);
    }

    @Test
    public void interpret_withExpression_printsEvaluatedValueAndNewline() {
        Expression expr = mock(Expression.class);
        when(expr.evaluate(any(Stacks.class))).thenReturn(321);
        EcrireLnNode node = new EcrireLnNode(expr);
        Stacks stacks = mock(Stacks.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            node.interpret(stacks);
        } finally {
            System.setOut(original);
        }

        assertEquals("321\n", out.toString());
    }

    @Test
    public void interpret_withString_printsStringAndNewline() {
        EcrireLnNode node = new EcrireLnNode("abcLn");
        Stacks stacks = mock(Stacks.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            node.interpret(stacks);
        } finally {
            System.setOut(original);
        }

        assertEquals("abcLn\n", out.toString());
    }
}
