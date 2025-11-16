package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that parse MiniJaja source and execute it using the real
 * AST interpretation methods and a real `Stacks` instance (no Mockito).
 */
public class RuntimeIntegrationTest {

    private ClasseNode parse(String src) {
        MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(src));
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));
        MiniJajaParser.ClasseContext tree = parser.classe();
        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        return (ClasseNode) visitor.visitClasse(tree);
    }

    private void executeClasse(ClasseNode classe, Stacks stacks) {
        // execute declarations
        DeclsNode decls = classe.getDeclarations();
        while (decls != null && decls.getDecl() != null) {
            VarNode v = decls.getDecl();
            v.interpret(stacks);
            decls = decls.getDecls();
        }

        // execute main
        MainNode main = (MainNode) classe.getMethodeMain();

        // declare main vars
        VarsNode vars = main.getVars();
        while (vars != null && vars.getVar() != null) {
            vars.getVar().interpret(stacks);
            vars = vars.getVars();
        }

        // execute instructions
        executeInstructionsNode(main.getInstrs(), stacks);
    }

    private void executeInstructionsNode(InstructionsNode instrs, Stacks stacks) {
        if (instrs == null) return;
        InstructionsNode cur = instrs;
        while (cur != null && cur.getInstructionNode() != null) {
            executeInstruction(cur.getInstructionNode(), stacks);
            cur = cur.getInstructions();
        }
    }

    private void executeInstruction(InstructionNode node, Stacks stacks) {
        if (node == null) return;

        if (node instanceof SiNode si) {
            si.interpret(stacks);
            Iterable<AstNode> children = si.getChildren();
            if (children != null) {
                for (var child : children) {
                    if (child instanceof InstructionNode in) executeInstruction(in, stacks);
                    else if (child instanceof InstructionsNode ins)
                        executeInstructionsNode(ins, stacks);
                }
            }
            return;
        }

        if (node instanceof TantqueNode tq) {
            // interpret sets children to body + next loop or empty list
            tq.interpret(stacks);
            Iterable<AstNode> children = tq.getChildren();
            if (children != null) {
                for (var child : children) {
                    if (child instanceof InstructionNode in) executeInstruction(in, stacks);
                    else if (child instanceof InstructionsNode ins)
                        executeInstructionsNode(ins, stacks);
                }
            }
            return;
        }

        // default: instruction can interpret itself (affectation, somme, ecrire, ...)
        node.interpret(stacks);
    }

    @Test
    public void while_loop_and_arithmetic_execution() {
        String src = """
                class C {
                  int x = 0;
                  main {
                    while (3 > x) { x+=1; };
                  }
                }""";

        ClasseNode ast = parse(src);
        Stacks stacks = new Stacks();
        executeClasse(ast, stacks);

        assertEquals(3, stacks.getValue("x"));
    }

    @Test
    public void nested_expressions_and_prints_execute_without_mock() {
        String src = """
                class C {
                  int x = 2;
                  main {
                    x = (1 + 2) * x;
                    write(x);
                    writeln("ok");
                  }
                }""";

        ClasseNode ast = parse(src);
        Stacks stacks = new Stacks();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            executeClasse(ast, stacks);
        } finally {
            System.setOut(original);
        }

        // printed value: (1+2)*2 = 6 followed by newline from ecrireln
        String printed = out.toString();
        assertTrue(printed.contains("6"));
        assertTrue(printed.contains("ok"));
    }
}
