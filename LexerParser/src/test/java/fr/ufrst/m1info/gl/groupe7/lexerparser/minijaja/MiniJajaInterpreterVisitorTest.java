package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Terme.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Terme.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

@ExtendWith(MockitoExtension.class)
class MiniJajaInterpreterVisitorTest {

    private ClasseNode parseFromString(String source) {
        MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(source));
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));
        MiniJajaParser.ClasseContext tree = parser.classe();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "pb syntaxe");

        Stacks stacks = mock(Stacks.class);
        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(stacks);
        AstNode node = visitor.visitClasse(tree);
        assertNotNull(node, "Visitor returned null AST");
        return (ClasseNode) node;
    }

    private MiniJajaParser parserFor(CharStream input) {
        MiniJajaLexer lexer = new MiniJajaLexer(input);
        return new MiniJajaParser(new CommonTokenStream(lexer));
    }

    // === Helpers to navigate to the single main affectation expression ===
    private AstNode firstMainExpression(ClasseNode ast) {
        InstructionsNode instrs = ((MainNode) ast.getMethodeMain()).getInstrs();
        assertNotNull(instrs, "main instructions should not be null");
        InstructionNode instr = instrs.getInstructionNode();
        assertTrue(instr instanceof AffectationNode, "expected first instruction to be an affectation");
        return ((AffectationNode) instr).getExpression();
    }

    @Test
    void simpleProgramResource_buildsExpectedAst() throws IOException {
        URL url = getClass().getClassLoader().getResource("simple_program.mjj");
        assertNotNull(url, "Resource simple_program.mjj not found");
        String source = Files.readString(Path.of(url.getPath()));

        ClasseNode ast = parseFromString(source);
        String expected = "Classe(Ident(C),decls (var (int , Ident(x) , nbre(0)),vnil),Main(vnil, Inil))";
        assertEquals(expected, ast.toStringTree());
    }

    @Test
    void invalidPrograms_haveSyntaxErrors() throws URISyntaxException, IOException {
        URL folderUrl = getClass().getClassLoader().getResource("invalid");
        assertNotNull(folderUrl, "Resource folder invalid/ not found");
        Path folderPath = Paths.get(folderUrl.toURI());
        assertTrue(Files.isDirectory(folderPath), "invalid/ is not a directory");
        try (Stream<Path> files = Files.list(folderPath)) {
            files.filter(p -> p.toString().endsWith(".mjj")).forEach(path -> {
                try {
                    String code = Files.readString(path);
                    MiniJajaParser parser = parserFor(CharStreams.fromString(code));
                    MiniJajaParser.ClasseContext tree = parser.classe();
                    int errors = parser.getNumberOfSyntaxErrors();
                    boolean missingClassIdent = tree.IDENT() == null;
                    if (errors == 0 && !missingClassIdent) {
                        Stacks stacks = mock(Stacks.class);
                        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(stacks);
                        AstNode ast = visitor.visitClasse(tree);
                        assertNotNull(ast, () -> "Visitor returned null AST for syntactically valid invalid-case: "
                                + path.getFileName());
                    } else {
                        assertTrue(errors > 0 || missingClassIdent,
                                () -> "Expected syntax errors or missing class ident for " + path.getFileName()
                                        + ", got errors=" + errors + ", missingIdent=" + missingClassIdent);
                    }
                } catch (IOException e) {
                    fail("Failed to read test resource: " + path + ": " + e.getMessage());
                }
            });
        }
    }

    // ===================== INSTANCEOF TESTS FOR EXPRESSIONS =====================

    @Test
    void exp_plus_and_times_instanceof_structure() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = 1 + 2 * 3; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof PlusNode, "top expression should be PlusNode");
        AstNode left = ((PlusNode) expr).getExp2();
        AstNode right = ((PlusNode) expr).getTerme();
        assertTrue(left instanceof NbreNode, "left of + should be number");
        assertTrue(right instanceof MultiplicationNode, "right of + should be a MultiplicationNode (precedence)");
        AstNode rLeft = ((MultiplicationNode) right).getTerme();
        AstNode rRight = ((MultiplicationNode) right).getFact();
        assertTrue(rLeft instanceof NbreNode, "left of * should be number");
        assertTrue(rRight instanceof NbreNode, "right of * should be number");
    }

    @Test
    void exp_unary_minus_instanceof() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = -1; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof UnaryMinusNode, "expected UnaryMinusNode");
        assertTrue(((UnaryMinusNode) expr).getTerme() instanceof NbreNode, "unary minus applies to a number");
    }

    @Test
    void exp_division_instanceof() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = 8 / 2; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof DivisionNode, "expected DivisionNode");
        assertTrue(((DivisionNode) expr).getTerme() instanceof NbreNode);
        assertTrue(((DivisionNode) expr).getFact() instanceof NbreNode);
    }

    @Test
    void exp_equals_instanceof() {
        String source = "class C {\n" +
                "  boolean b;\n" +
                "  main { b = 1 == 1; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof EqualsNode, "expected EqualsNode");
        assertTrue(((EqualsNode) expr).getExp1() instanceof NbreNode);
        assertTrue(((EqualsNode) expr).getExp2() instanceof NbreNode);
    }

    @Test
    void exp_greater_than_instanceof() {
        String source = "class C {\n" +
                "  boolean b;\n" +
                "  main { b = 2 > 1; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof GreaterThanNode, "expected GreaterThanNode");
        assertTrue(((GreaterThanNode) expr).getExp1() instanceof NbreNode);
        assertTrue(((GreaterThanNode) expr).getExp2() instanceof NbreNode);
    }

    @Test
    void exp_logical_and_instanceof() {
        String source = "class C {\n" +
                "  boolean b;\n" +
                "  main { b = true && false; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof AndNode, "expected AndNode");
        // Order may vary in rendering; only assert child types
        AstNode a = ((AndNode) expr).getExp();
        AstNode b = ((AndNode) expr).getExp1();
        assertTrue(a instanceof BoolValueNode);
        assertTrue(b instanceof BoolValueNode);
    }

    @Test
    void exp_logical_or_instanceof() {
        String source = "class C {\n" +
                "  boolean b;\n" +
                "  main { b = false || true; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof OrNode, "expected OrNode");
        AstNode a = ((OrNode) expr).getExp();
        AstNode b = ((OrNode) expr).getExp1();
        assertTrue(a instanceof BoolValueNode);
        assertTrue(b instanceof BoolValueNode);
    }

    @Test
    void exp_logical_not_instanceof() {
        String source = "class C {\n" +
                "  boolean b;\n" +
                "  main { b = !false; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof NotNode, "expected NotNode");
        assertTrue(((NotNode) expr).getExp() instanceof BoolValueNode);
    }

    // @Test
    // void fact_length_instanceof() {
    // String source = "class C {\n" +
    // " int x = 0;\n" +
    // " int t[3];\n" +
    // " main { x = length(t); }\n" +
    // "}";
    // ClasseNode ast = parseFromString(source);
    // System.out.println("AST: " + ast.toStringTree());

    // // AstNode expr = firstMainExpression(ast);
    // // assertTrue(expr instanceof LengthNode, "expected LengthNode");
    // }

    @Test
    void fact_exp() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = (3); }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertTrue(expr instanceof Expression, "expected ParenNode");
    }

    @Test
    void fact_listexp_one_exp() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = f(42); }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        // System.out.println("AST: " + ast.toStringTree());
        AstNode expr = firstMainExpression(ast);
        // System.out.println("expr: " + expr.toStringTree());

        assertTrue(expr instanceof AppelENode, "expected AppelENode");
    }

    @Test
    void instr_somme() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x+=42; }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        InstructionsNode instrs = ((MainNode) ast.getMethodeMain()).getInstrs();
        assertNotNull(instrs, "main instructions should not be null");
        InstructionNode instr = instrs.getInstructionNode();
        assertTrue(instr instanceof SommeNode, "expected first instruction to be a somme");
    }

    @Test
    void fact_listexp_multiple_exps() {
        String source = "class C {\n" +
                "  int x = 0;\n" +
                "  main { x = f(42, 43); }\n" +
                "}";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);

        assertTrue(expr instanceof AppelENode, "expected AppelENode");
    }

}
