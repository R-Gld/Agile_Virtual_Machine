package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;


import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EcrireLnNodeTest {

    @Test
    public void toStringTree_withString_returnsStringWrapped() {
        EcrireLnNode node = new EcrireLnNode("HelloLn");

        String tree = node.toStringTree();

        assertEquals("ecrireln (HelloLn)", tree);
    }


    @Test
    public void interpret_withString_printsStringAndNewline() {
        EcrireLnNode node = new EcrireLnNode("abcLn");
        Stacks stacks = new Stacks();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            node.interpret(stacks);
        } finally {
            System.setOut(original);
        }

        String expected = "abcLn" + System.lineSeparator();
        assertEquals(expected, out.toString());
    }
}

