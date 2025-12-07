package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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
        analyser.setFileName("Test.mjj");

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
        analyser.setFileName("ValidExpr.mjj");

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
        analyser.setFileName("DuplicateGlobal.mjj");

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
        analyser.setFileName("DuplicateLocal.mjj");

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
        analyser.setFileName("UndeclaredAssign.mjj");

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
        analyser.setFileName("TypeMismatch.mjj");

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
        analyser.setFileName("TabAssign.mjj");

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
        analyser.setFileName("UndeclaredExpr.mjj");

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
        analyser.setFileName("PlusOk.mjj");

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
        analyser.setFileName("PlusLeftBad.mjj");

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
        analyser.setFileName("PlusRightBad.mjj");

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
        analyser.setFileName("UnaryMinusBad.mjj");

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
        analyser.setFileName("AndOk.mjj");

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
        analyser.setFileName("AndLeftBad.mjj");

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
        analyser.setFileName("AndRightBad.mjj");

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
        analyser.setFileName("NotBad.mjj");

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
        analyser.setFileName("EqualsBad.mjj");

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
        analyser.setFileName("EqualsOk.mjj");

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
        analyser.setFileName("GreaterOk.mjj");

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
        analyser.setFileName("GreaterBad.mjj");

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

    @Test
    void inferType_nullExpression_shouldReturnNull_viaReflection() throws Exception {
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);

        Method m = MiniJajaSemanticAnalyzer.class
                .getDeclaredMethod("inferType", fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression.class);
        m.setAccessible(true);

        Object result = m.invoke(analyser, new Object[]{null});
        assertNull(result, "inferType(null) doit retourner null");
    }

    @Test
    void checkNode_nullNode_shouldReturnImmediately_viaReflection() throws Exception {
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);

        Method m = MiniJajaSemanticAnalyzer.class
                .getDeclaredMethod("checkNode", AstNode.class);
        m.setAccessible(true);

        // Doit simplement ne rien faire (pas d'exception, pas de diagnostic)
        m.invoke(analyser, new Object[]{null});

        assertTrue(collector.getDiagnostics().isEmpty());
    }

    @Test
    void checkBinaryOperation_unknownOperation_shouldHitDefaultOperationName() throws Exception {
        DiagnosticCollector collector = new DiagnosticCollector();

        MiniJajaSemanticAnalyzer analyser = new MiniJajaSemanticAnalyzer(collector);
        analyser.setFileName("OpUnknown.mjj");

        Method m = MiniJajaSemanticAnalyzer.class.getDeclaredMethod(
                "checkBinaryOperation", Type.class, Type.class, Type.class, String.class);
        m.setAccessible(true);

        // leftType != expectedType -> déclenche le message avec getOperationName("^^") => "operation"
        m.invoke(analyser, Type.ENTIER, Type.ENTIER, Type.BOOLEEN, "^^");

        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("operation"), "Le nom générique 'operation' doit apparaître");
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
        analyser.setFileName("MissingReturn.mjj");

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
        analyser.setFileName("UndeclaredInInit.mjj");

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
        analyser.setFileName("TypeMismatchInit.mjj");

        analyser.analyse(classe);

        assertTrue(collector.hasErrors(), "Should report type mismatch error");
        Diagnostic diag = firstSemanticError(collector);
        assertNotNull(diag);
        assertTrue(diag.message().contains("Type mismatch") && diag.message().contains("initialization"),
                "Error message should mention type mismatch in initialization");
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