package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SemanticException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.exceptions.SyntaxException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MiniJajaSemanticAnalyzerTest {

    // ============================================================
    //  Constructeur / accès aux dépendances
    // ============================================================

    @Test
    void constructor_and_accessors_shouldExposeDependencies() {
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);

        assertSame(collector, analyser.collector());
        assertNotNull(analyser.symbolTable(), "SymbolTable should be created internally");
        assertNotNull(analyser.stacks(), "Stacks should be created internally");
    }

    // ============================================================
    //  Cas "heureux" simples via vrai code MiniJaja
    // ============================================================

    @Test
    void validProgram_emptyMain_shouldProduceNoDiagnostics() {
        String code = """
                class Test {
                    main {
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "Aucune erreur sémantique attendue");
        assertTrue(collector.getDiagnostics().isEmpty(), "Aucun diagnostic attendu");
    }

    @Test
    void validArithmeticAndBooleanExpressions_shouldBeAccepted() {
        String code = """
                class Test {
                    int x;
                    boolean b;
                    main {
                        x = 1 + 2 * 3 / 4 - 5;
                        b = true && false;
                        b = !false;
                        b = (1 == 2);
                        b = (1 > 0);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "Aucune erreur sémantique attendue");
    }

    // ============================================================
    //  Déclarations globales / locales (collectDeclarations / collectVars)
    // ============================================================

    @Test
    void duplicateGlobalVar_shouldReportSemanticError() {
        String code = """
                class Test {
                    int x;
                    int x;
                    main {
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertEquals(Severity.ERROR, diag.severity());
        assertEquals(Phase.SEMANTIC, diag.phase());
        assertTrue(diag.message().contains("Duplicate variable declaration"));
    }

    @Test
    void duplicateLocalVarInMain_shouldReportSemanticError() {
        String code = """
                class Test {
                    main {
                        int x;
                        int x;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertEquals(Severity.ERROR, diag.severity());
        assertTrue(diag.message().contains("Duplicate local variable"));
    }

    // ============================================================
    //  Affectations et variables non déclarées / mismatch de types
    // ============================================================

    @Test
    void undeclaredVariableInAffectation_shouldReportError() {
        String code = """
                class Test {
                    main {
                        int y;
                        x = 1;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Undeclared variable: 'x'"));
    }

    @Test
    void typeMismatchInAssignment_shouldReportError() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = true;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type mismatch in assignment"));
    }

    @Test
    void assignmentToArrayElement_shouldNotTriggerIdentNodeBranch() {
        String code = """
                class Test {
                    int t[10];
                    main {
                        t[0] = 1;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        // L'objectif est juste de couvrir le cas identNode instanceof TabNode (pas IdentNode)
        assertFalse(collector.hasErrors());
    }

    @Test
    void assignmentUsingUndeclaredExpressionVariable_shouldNotReportTypeMismatch() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = y;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing ne doit pas produire d'erreur");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        List<Diagnostic> diags = collector.getDiagnostics();
        assertTrue(diags.stream().anyMatch(d -> d.message().contains("Undeclared variable: 'y'")),
                "Erreur sur variable utilisée non déclarée attendue");
        assertTrue(diags.stream().noneMatch(d -> d.message().contains("Type mismatch in assignment")),
                "Pas d'erreur de mismatch de type attendue dans ce cas");
    }

    // ============================================================
    //  Opérations arithmétiques (Plus / Minus / Mult / Div / UnaryMinus)
    // ============================================================

    @Test
    void arithmeticPlus_validOperands_shouldNotReportErrors() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = 1 + 2;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors());
    }

    @Test
    void arithmeticPlus_leftOperandWrongType_shouldReportError() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = true + 1;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in arithmetic operation"));
        assertTrue(diag.message().contains("left operand of '+'"));
    }

    @Test
    void arithmeticPlus_rightOperandWrongType_shouldReportError() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = 1 + true;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in arithmetic operation"));
        assertTrue(diag.message().contains("right operand of '+'"));
    }

    @Test
    void unaryMinus_wrongOperandType_shouldReportError() {
        String code = """
                class Test {
                    int x;
                    main {
                        x = -true;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("unary minus operator '-' requires an integer operand"));
    }

    // ============================================================
    //  Opérations booléennes (And / Or / Not)
    // ============================================================

    @Test
    void logicalAnd_validOperands_shouldNotReportErrors() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = true && false;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors());
    }

    @Test
    void logicalAnd_rightOperandWrongType_shouldReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = 1 && true;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in logical operation"));
        assertTrue(diag.message().contains("right operand of 'and'"));
    }

    @Test
    void logicalAnd_leftOperandWrongType_shouldReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = true && 1;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in logical operation"));
        assertTrue(diag.message().contains("left operand of 'and'"));
    }

    @Test
    void logicalNot_wrongOperandType_shouldReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = !1;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("logical NOT operator 'non'"));
    }

    // ============================================================
    //  Comparaisons (Equals / GreaterThan)
    // ============================================================

    @Test
    void equalsComparison_typeMismatch_shouldReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = (1 == true);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in equality comparison"));
    }

    @Test
    void equalsComparison_sameType_shouldNotReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = (1 == 2);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors());
    }

    @Test
    void greaterThanComparison_validTypes_shouldNotReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = (1 > 0);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors());
    }

    @Test
    void greaterThanComparison_wrongTypes_shouldReportError() {
        String code = """
                class Test {
                    boolean b;
                    main {
                        b = (true > 0);
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type error in comparison operation"));
    }

    // ============================================================
    //  Cas particuliers : checkType avec MethodeNode, null-safety, getOperationName default
    // ============================================================

    @Test
    void nullVarsAndNullInstrs_shouldHitNullGuards() {
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);

        ClasseNode classe = mock(ClasseNode.class);
        MainNode mainNode = mock(MainNode.class);

        // Pas de déclarations globales
        when(classe.getDeclarations()).thenReturn(null);
        // main renvoyé par getMethodeMain
        when(classe.getMethodeMain()).thenReturn(mainNode);
        // vars et instrs à null pour couvrir les if(vars == null) et if(instrs == null)
        when(mainNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(null);

        analyser.analyse(classe);

        assertFalse(collector.hasErrors());
    }

    // ============================================================
    //  Tests for missing return statement
    // ============================================================

    @Test
    void methodWithoutReturn_shouldReportError() {
        String code = """
                class C {
                    int f(int z) {
                        int x = 5;
                    };
                    main {
                        int y = 0;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report missing return error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Missing return statement") || diag.message().contains("must return"),
                "Error message should mention missing return");
    }

    // ============================================================
    //  Tests for undeclared variable in initialization
    // ============================================================

    @Test
    void undeclaredVariableInInitialization_shouldReportError() {
        String code = """
                class C {
                    int f(int z) {
                        boolean t = a;
                        return 0;
                    };
                    main {
                        int x = 0;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report undeclared variable error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Undeclared variable") && diag.message().contains("'a'"),
                "Error message should mention undeclared variable 'a'");
    }

    // ============================================================
    //  Tests for type mismatch in initialization
    // ============================================================

    @Test
    void typeMismatchInInitialization_shouldReportError() {
        String code = """
                class C {
                    int f(int z) {
                        boolean o = z;
                        return 0;
                    };
                    main {
                        int x = 0;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName(null);

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report type mismatch error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type mismatch") && diag.message().contains("initialization"),
                "Error message should mention type mismatch in initialization");
    }

    @Test
    void uninitializedFinalConstant_canBeInitializedOnce() {
        String code = """
                class Test {
                    final int longueur;
                    main {
                        longueur = 10;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("ConstantInit.mjj");

        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "First initialization of uninitialized final constant should be allowed");
    }

    @Test
    void initializedFinalConstant_cannotBeReassigned() {
        String code = """
                class Test {
                    final int x = 5;
                    main {
                        x = 10;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("ConstantReassignment.mjj");

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Reassigning initialized final constant should report error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Cannot reassign constant") || diag.message().contains("final"),
                "Error message should mention constant reassignment");
    }

    @Test
    void uninitializedFinalConstant_cannotBeReassignedAfterFirstInit() {
        String code = """
                class Test {
                    final int longueur;
                    main {
                        longueur = 10;
                        longueur = 20;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("ConstantDoubleInit.mjj");

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Second assignment to constant should report error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Cannot reassign constant") || diag.message().contains("final"),
                "Error message should mention constant reassignment");
    }

    @Test
    void complexQuicksortExample_withUninitializedFinalConstant_shouldWork() {
        String code = """
                class quicksort{
                    final int longueur;
                    int tableau[longueur];

                    main {
                        longueur = length(tableau);
                        tableau[0]=5;
                        tableau[1]=2;
                    }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("Quicksort.mjj");

        analyser.analyse(classe);

        assertFalse(collector.hasErrors(), "Quicksort example should not produce semantic errors");
    }

    @Test
    void globalConstant_initializedInMain_thenAssignedInMethod_shouldReportErrorInMethod() {
        String code = """
                class C {
                  int x = 0;
                  final int can;
                  int fice(int x) {
                    can = x;
                    return can;
                  };

                  main {
                    can = 10;
                    write(can);
                  }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("ConstOrder.mjj");

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report error for assigning global constant in method");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Cannot assign to global constant") ||
                   diag.message().contains("inside a method") ||
                   diag.message().contains("Cannot reassign constant") ||
                   diag.message().contains("final"),
                "Error message should mention global constant or reassignment, got: " + diag.message());
        // The error should be reported in method fice (can = x), not in main (can = 10)
    }

    @Test
    void globalConstant_cannotBeAssignedInMethod() {
        String code = """
                class C {
                  int x = 0;
                  final int can;
                  int fice(int x) {
                    can = x;
                    return can;
                  };

                  main {
                    fice(10);
                    write(can);
                  }
                }
                """;

        DiagnosticCollector collector = new DiagnosticCollector();
        ClasseNode classe = parseClasse(code, collector);
        assertFalse(collector.hasErrors(), "Parsing should not produce errors");

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("ConstInMethod.mjj");

        analyser.analyse(classe);

        // STRICT RULE: Global constants cannot be assigned in methods, even on first initialization
        // This prevents issues with multiple calls and ensures constants are truly constant
        assertTrue(collector.hasErrors(), "Should report error for assigning global constant in method");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Cannot assign to global constant") ||
                   diag.message().contains("inside a method"),
                "Error message should mention global constant assignment in method, got: " + diag.message());
    }

    // ============================================================
    //  Helpers communs
    // ============================================================

    private ClasseNode parseClasse(String code, DiagnosticCollector collector) {
        CharStream cs = CharStreams.fromString(code);
        MiniJajaLexer lexer = new MiniJajaLexer(cs);
        MiniJajaParser parser = new MiniJajaParser(new CommonTokenStream(lexer));

        SyntaxErrorListener sel = new SyntaxErrorListener(collector, "Test.mjj");
        sel.register(lexer);
        sel.register(parser);

        ParseTree tree = parser.classe();

        if (collector.hasErrors()) {
            throw new SyntaxException(collector);
        }

        MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();
        AstNode astRoot = visitor.visit(tree);

        assertNotNull(astRoot, "L'AST ne doit pas être null");
        assertInstanceOf(ClasseNode.class, astRoot, "La racine doit être une ClasseNode");

        return (ClasseNode) astRoot;
    }

    private Diagnostic firstSemanticError(DiagnosticCollector collector) {
        return collector.getDiagnostics().stream()
                .filter(d -> d.severity() == Severity.ERROR && d.phase() == Phase.SEMANTIC)
                .findFirst()
                .orElse(null);
    }
}