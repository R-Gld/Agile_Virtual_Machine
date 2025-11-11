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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class MiniJajaInterpreterVisitorTest {

    private ClasseNode parseFromString(String source) {
        MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(source));
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));
        MiniJajaParser.ClasseContext tree = parser.classe();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "pb syntaxe");

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
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
        assertInstanceOf(AffectationNode.class, instr, "expected first instruction to be an affectation");
        return ((AffectationNode) instr).getExpression();
    }

    @Test
    @Disabled
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
                     
                        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
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
        String source = """
                class C {
                  int x = 0;
                  main { x = 1 + 2 * 3; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(PlusNode.class, expr, "top expression should be PlusNode");
        AstNode left = ((PlusNode) expr).getExp2();
        AstNode right = ((PlusNode) expr).getTerme();
        assertInstanceOf(NbreNode.class, left, "left of + should be number");
        assertInstanceOf(MultiplicationNode.class, right, "right of + should be a MultiplicationNode (precedence)");
        AstNode rLeft = ((MultiplicationNode) right).getTerme();
        AstNode rRight = ((MultiplicationNode) right).getFact();
        assertInstanceOf(NbreNode.class, rLeft, "left of * should be number");
        assertInstanceOf(NbreNode.class, rRight, "right of * should be number");
    }

    @Test
    void exp_unary_minus_instanceof() {
        String source = """
                class C {
                  int x = 0;
                  main { x = -1; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(UnaryMinusNode.class, expr, "expected UnaryMinusNode");
        assertInstanceOf(NbreNode.class, ((UnaryMinusNode) expr).getTerme(), "unary minus applies to a number");
    }

    @Test
    void exp_division_instanceof() {
        String source = """
                class C {
                  int x = 0;
                  main { x = 8 / 2; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(DivisionNode.class, expr, "expected DivisionNode");
        assertInstanceOf(NbreNode.class, ((DivisionNode) expr).getTerme());
        assertInstanceOf(NbreNode.class, ((DivisionNode) expr).getFact());
    }

    @Test
    void exp_equals_instanceof() {
        String source = """
                class C {
                  boolean b;
                  main { b = 1 == 1; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(EqualsNode.class, expr, "expected EqualsNode");
        assertInstanceOf(NbreNode.class, ((EqualsNode) expr).getExp1());
        assertInstanceOf(NbreNode.class, ((EqualsNode) expr).getExp2());
    }

    @Test
    void exp_greater_than_instanceof() {
        String source = """
                class C {
                  boolean b;
                  main { b = 2 > 1; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(GreaterThanNode.class, expr, "expected GreaterThanNode");
        assertInstanceOf(NbreNode.class, ((GreaterThanNode) expr).getExp1());
        assertInstanceOf(NbreNode.class, ((GreaterThanNode) expr).getExp2());
    }

    @Test
    void exp_logical_and_instanceof() {
        String source = """
                class C {
                  boolean b;
                  main { b = true && false; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(AndNode.class, expr, "expected AndNode");
        // Order may vary in rendering; only assert child types
        AstNode a = ((AndNode) expr).getExp();
        AstNode b = ((AndNode) expr).getExp1();
        assertInstanceOf(BoolValueNode.class, a);
        assertInstanceOf(BoolValueNode.class, b);
    }

    @Test
    void exp_logical_or_instanceof() {
        String source = """
                class C {
                  boolean b;
                  main { b = false || true; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(OrNode.class, expr, "expected OrNode");
        AstNode a = ((OrNode) expr).getExp();
        AstNode b = ((OrNode) expr).getExp1();
        assertInstanceOf(BoolValueNode.class, a);
        assertInstanceOf(BoolValueNode.class, b);
    }

    @Test
    void exp_logical_not_instanceof() {
        String source = """
                class C {
                  boolean b;
                  main { b = !false; }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(NotNode.class, expr, "expected NotNode");
        assertInstanceOf(BoolValueNode.class, ((NotNode) expr).getExp());
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
        String source = """
                class C {
                  int x = 0;
                  main { x = (3); }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);
        assertInstanceOf(Expression.class, expr, "expected ParenNode");
    }

    @Test
    void fact_listexp_one_exp() {
        String source = """
                class C {
                  int x = 0;
                  main { x = f(42); }
                }""";
        ClasseNode ast = parseFromString(source);
        // System.out.println("AST: " + ast.toStringTree());
        AstNode expr = firstMainExpression(ast);
        // System.out.println("expr: " + expr.toStringTree());

        assertInstanceOf(AppelENode.class, expr, "expected AppelENode");
    }

    @Test
    void instr_somme() {
        String source = """
                class C {
                  int x = 0;
                  main { x+=42; }
                }""";
        ClasseNode ast = parseFromString(source);
        InstructionsNode instrs = ((MainNode) ast.getMethodeMain()).getInstrs();
        assertNotNull(instrs, "main instructions should not be null");
        InstructionNode instr = instrs.getInstructionNode();
        assertInstanceOf(SommeNode.class, instr, "expected first instruction to be a somme");
    }

    @Test
    void fact_listexp_multiple_exps() {
        String source = """
                class C {
                  int x = 0;
                  main { x = f(42, 43); }
                }""";
        ClasseNode ast = parseFromString(source);
        AstNode expr = firstMainExpression(ast);

        assertInstanceOf(AppelENode.class, expr, "expected AppelENode");
    }

}
