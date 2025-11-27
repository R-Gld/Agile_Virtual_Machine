package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireLnNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour les instructions WRITE et WRITELN.
 */
class WriteInstructionTest {

    @Test
    void ecrireNode_withExpression_generatesWriteInstruction() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        InstructionNode ecrire = new EcrireNode(new NbreNode(42));

        visitor.visit(ecrire);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(42)"), "Should push value 42");
        assertTrue(code.contains("write"), "Should contain write instruction");
    }

    @Test
    void ecrireNode_withIdentifier_generatesWriteInstruction() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        InstructionNode ecrire = new EcrireNode(new IdentNode("x"));

        visitor.visit(ecrire);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"), "Should load variable x");
        assertTrue(code.contains("write"), "Should contain write instruction");
    }

    @Test
    void ecrireNode_withString_generatesWriteInstruction() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        InstructionNode ecrire = new EcrireNode("Hello");

        visitor.visit(ecrire);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(\"Hello\")"), "Should push string Hello");
        assertTrue(code.contains("write"), "Should contain write instruction");
    }

    @Test
    void ecrireLnNode_withExpression_generatesWritelnInstruction() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        InstructionNode ecrireln = new EcrireLnNode(new NbreNode(100));

        visitor.visit(ecrireln);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(100)"), "Should push value 100");
        assertTrue(code.contains("writeln"), "Should contain writeln instruction");
    }

    @Test
    void ecrireLnNode_withString_generatesWritelnInstruction() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        InstructionNode ecrireln = new EcrireLnNode("World");

        visitor.visit(ecrireln);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(\"World\")"), "Should push string World");
        assertTrue(code.contains("writeln"), "Should contain writeln instruction");
    }

    @Test
    void fullProgram_withWriteAndWriteln_compilesSuccessfully() {
        String program = """
            class Test {
              int x = 6;
              main {
                write(x);
                writeln("ok");
              }
            }
            """;

        MiniJajaCompilerVisitor visitor = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(program);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@"), "Should load variable x");
        assertTrue(code.contains("write"), "Should contain write instruction");
        assertTrue(code.contains("push(\"ok\")"), "Should push string ok");
        assertTrue(code.contains("writeln"), "Should contain writeln instruction");
    }
}
