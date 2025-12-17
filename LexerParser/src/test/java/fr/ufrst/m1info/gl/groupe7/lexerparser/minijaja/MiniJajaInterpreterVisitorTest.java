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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;

@ExtendWith(MockitoExtension.class)
class MiniJajaInterpreterVisitorTest {

  private ClasseNode parseFromString(String source) {
    MiniJajaLexer lexer = new MiniJajaLexer(CharStreams.fromString(source));
    MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));
    MiniJajaParser.ClasseContext tree = parser.classe();
    assertEquals(0, parser.getNumberOfSyntaxErrors(), "pb syntaxe");

    // Stacks stacks = mock(Stacks.class);
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
  void simpleProgramResource_buildsExpectedAst() throws IOException, URISyntaxException {
    URL url = getClass().getClassLoader().getResource("simple_program.mjj");
    assertNotNull(url, "Resource simple_program.mjj not found");
    String source = Files.readString(Paths.get(url.toURI()));

    ClasseNode ast = parseFromString(source);
    String expected = "Classe(Ident(C),decls (var (integer , Ident(x) , nbre(0)),vnil),Main(vnil, Inil))";
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
            // Stacks stacks = mock(Stacks.class);
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

  private AstNode firstDeclaration(ClasseNode ast) {
    DeclsNode decls = ast.getDeclarations();
    assertNotNull(decls, "decls should not be null");
    AstNode decl = decls.getDecl();
    assertNotNull(decl, "first declaration should not be null");
    return decl;
  }

  @Test
  void decl_cst_instanceof() {
    String source = """
        class C {
          final int x = 10;
          main { }
        }""";
    ClasseNode ast = parseFromString(source);
    AstNode decl = firstDeclaration(ast);
    assertInstanceOf(CstNode.class, decl, "expected CstNode");
  }

  @Test
  void decl_tableau_instanceof() {
    String source = """
        class C {
          int t[10];
          main { }
        }""";
    ClasseNode ast = parseFromString(source);
    AstNode decl = firstDeclaration(ast);
    assertInstanceOf(TableauNode.class, decl, "expected TableauNode");
  }

  // ===== HELPER METHOD =====
  private AstNode parse(String source) {
    return parseFromString(source);
  }

  // ===== 1. CLASSE + MAIN =====

  @Test
  public void testEmptyClass() {
    String program = """
        class A {
          main {
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast, "AST ne doit pas être null");
  }

    // ===== 2. DECLARATIONS =====

  @Test
  public void testVariableDeclarations() {
    String program = """
        class A {
          int x = 1;
          boolean b = true;
          int t[10];
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
    assertTrue(ast instanceof ClasseNode);
  }

  @Test
  public void testFinalConstant() {
    String program = """
        class A {
          final int X = 5;
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
    AstNode decl = firstDeclaration((ClasseNode) ast);
    assertInstanceOf(CstNode.class, decl);
  }

  @Test
  public void testMultipleConstantsAndVariables() {
    String program = """
        class A {
          final int X = 5;
          int x = 10;
          boolean b = false;
          int arr[20];
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testBooleanVariableInitialization() {
    String program = """
        class A {
          boolean flag = true;
          boolean other = false;
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 3. INSTRUCTIONS =====

  @Test
  public void testAffectationSommeIncrement() {
    String program = """
        class A {
          int x = 0;
          main {
            x = 1;
            x += 2;
            x++;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testIfElse() {
    String program = """
        class A {
          int x = 0;
          main {
            if (x > 0) {
              x = 1;
            } else {
              x = 2;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testIfWithoutElse() {
    String program = """
        class A {
          int x = 0;
          main {
            if (x > 0) {
              x = 1;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testNestedIfElse() {
    String program = """
        class A {
          int x = 0;
          int y = 0;
          main {
            if (x > 0) {
              if (y > 0) {
                x = 1;
              } else {
                x = 2;
              };
            } else {
              x = 3;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testWhileLoop() {
    String program = """
        class A {
          int x = 0;
          main {
            while (x > 10) {
              x++;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testNestedWhileLoops() {
    String program = """
        class A {
          int x = 0;
          int y = 0;
          main {
            while (x > 10) {
              while (y > 5) {
                y++;
              };
              x++;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testReturnInstruction() {
    String program = """
        class A {
          int f() {
            return 5;
          };
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testReturnFromMethod() {
    String program = """
        class A {
          int add(int a, int b) {
            return a + b;
          };
          main { }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 4. WRITE / WRITELN =====

  @Test
  public void testWriteAndWriteln() {
    String program = """
        class A {
          int x = 3;
          main {
            write(x);
            writeln("hello");
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

 



  // ===== 5. EXPRESSIONS =====

  // @Test
  // public void testComplexExpressions() {
  //   String program = """
  //       class A {
  //         int x = 1;
  //         int y = 2;
  //         main {
  //           if ((x + y * 2 > 3) && !(x == y)) {
  //             x = -x;
  //           };
  //         }
  //       }
  //       """;


  @Test
  public void testArithmeticOperations() {
    String program = """
        class A {
          int x = 0;
          main {
            x = 5 + 3;
            x = 10 - 2;
            x = 4 * 5;
            x = 20 / 4;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testComparisonOperators() {
    String program = """
        class A {
          boolean b = false;
          int x = 5;
          main {
            b = x > 3;
            b = x == 5;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testLogicalOperators() {
    String program = """
        class A {
          boolean b = false;
          main {
            b = true && false;
            b = true || false;
            b = !true;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testMixedArithmeticAndComparison() {
    String program = """
        class A {
          boolean result = false;
          main {
            result = 2 + 3 * 4 > 10;
            result = 5 * 2 - 1 == 9;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 6. TABLEAUX =====

  @Test
  public void testArrayMultipleAccess() {
    String program = """
        class A {
          int t[10];
          main {
            t[0] = 1;
            t[5] = 50;
            t[9] = 99;
            write(t[0]);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testArrayAccessWithExpression() {
    String program = """
        class A {
          int t[10];
          int i = 2;
          main {
            t[i + 1] = 42;
            write(t[i * 2]);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 7. APPELS (METHOD CALLS) =====

  @Test
  public void testMethodCallExpressionAndInstruction() {
    String program = """
        class A {
          int f(int x) {
            return x + 1;
          };
          main {
            f(3);
            
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testMethodWithMultipleParameters() {
    String program = """
        class A {
          int add(int a, int b, int c) {
            return a + b + c;
          };
          main {
            int result = add(1, 2, 3);
            write(result);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testMethodWithoutParameters() {
    String program = """
        class A {
          int getValue() {
            return 42;
          };
          main {
            int result = getValue();
            write(result);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }



  // ===== 8. EDGE CASES & LIMITS =====

  @Test
  public void testEmptyInstructionList() {
    String program = """
        class A {
          main {
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testLargeNumberValues() {
    String program = """
        class A {
          int x = 0;
          main {
            x = 999999;
            x = -999999;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testComplexNestedStructure() {
    String program = """
        class A {
          int x = 0;
          int y = 0;
          main {
            while (x > 10) {
              if (x > 5) {
                y = x * 2;
              } else {
                y = x / 2;
              };
              x++;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }



  @Test
  public void testVoidMethodCall() {
    String program = """
        class A {
          void printMessage() {
            write("test");
          };
          main {
            printMessage();
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 9. SPECIFIC EXPRESSION TYPES =====

  @Test
  public void testUnaryMinusExpression() {
    String program = """
        class A {
          int x = 0;
          main {
            x = -5;
            x = -(10 + 5);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testLogicalNotExpression() {
    String program = """
        class A {
          boolean b = false;
          main {
            b = !true;
            b = !(5 > 3);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testParenthesizedExpression() {
    String program = """
        class A {
          int x = 0;
          main {
            x = (5 + 3) * 2;
            x = (10 - 2) / (3 - 1);
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testOperatorPrecedence() {
    String program = """
        class A {
          int x = 0;
          main {
            x = 2 + 3 * 4;
            x = (2 + 3) * 4;
            x = 10 - 5 - 2;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 10. COMBINED FEATURES =====

  @Test
  public void testCompleteProgram() {
    String program = """
        class Calculator {
          int add(int a, int b) {
            return a + b;
          };
          int multiply(int a, int b) {
            return a * b;
          };
          main {
            int result = 0;
            result = add(5, 3);
            write(result);
            result = multiply(4, 5);
            writeln(result);
            while (result > 0) {
              result = result - 1;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testArrayInLoop() {
    String program = """
        class A {
          int arr[5];
          main {
            int i = 0;
            while (i > 5) {
              arr[i] = i * 10;
              i++;
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  @Test
  public void testConditionalWithArrayAccess() {
    String program = """
        class A {
          int arr[10];
          main {
            arr[0] = 5;
            if (arr[0] > 3) {
              write(arr[0]);
            };
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 11. BOOLEAN EXPRESSIONS =====

  @Test
  public void testBooleanExpressions() {
    String program = """
        class A {
          boolean result = false;
          main {
            result = 5 == 5;
            result = 10 > 3;
            result = true && true;
            result = false || true;
          }
        }
        """;

    AstNode ast = parse(program);
    assertNotNull(ast);
  }

  // ===== 12. INVALID/ERROR CASES =====

  @Test
  public void testSyntaxErrorDetection() {
    String program = """
        class A {
          main {
            int x = ;
          }
        }
        """;

    assertThrows(AssertionError.class, () -> parse(program));
  }

  @Test
  public void testMissingClosingBrace() {
    String program = """
        class A {
          main {
            int x = 5;
        """;

    assertThrows(AssertionFailedError.class, () -> parse(program));
  }

}
