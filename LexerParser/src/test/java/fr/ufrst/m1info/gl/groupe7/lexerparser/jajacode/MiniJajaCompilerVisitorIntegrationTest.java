package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour le compilateur MiniJaja.
 * Ces tests vérifient la génération complète de code JajaCode sans utiliser de mocks.
 */
class MiniJajaCompilerVisitorIntegrationTest {

    private IdentNode ident(String name) {
        return new IdentNode(name);
    }

    private InstructionsNode singleInstruction(InstructionNode instruction) {
        return new InstructionsNode(instruction, null);
    }

    // ===== Tests pour SommeNode (+=) =====

    @Test
    void sommeNode_simpleIncrement() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        SommeNode somme = new SommeNode(ident("x"), new NbreNode(5));

        visitor.visit(somme);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("inc(x@global)"));
    }

    @Test
    void sommeNode_withMultiplication() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        MultiplicationNode mult = new MultiplicationNode(new NbreNode(2), ident("x"));
        SommeNode somme = new SommeNode(ident("y"), mult);

        visitor.visit(somme);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(2)"));
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("inc(y@global)"));
    }

    @Test
    void sommeNode_negativeIncrement() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());
        SommeNode somme = new SommeNode(ident("i"), new UnaryMinusNode(new NbreNode(1)));

        visitor.visit(somme);
        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("neg"));
        assertTrue(code.contains("inc(i@global)"));
    }

    // ===== Tests pour SiNode (if/else) =====

    @Test
    void siNode_simpleIfWithoutElse() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x > 5) { y = 10; }
        GreaterThanNode condition = new GreaterThanNode(ident("x"), new NbreNode(5));
        AffectationNode assignment = new AffectationNode(ident("y"), new NbreNode(10));
        InstructionsNode thenBlock = singleInstruction(assignment);

        SiNode siNode = new SiNode(condition, thenBlock, null);
        visitor.visit(siNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(10)"));
        assertTrue(code.contains("store(y@global)"));
    }

    @Test
    void siNode_ifWithElse() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x == 5) { y = 1; } else { y = 0; }
        EqualsNode condition = new EqualsNode(ident("x"), new NbreNode(5));
        AffectationNode thenAssignment = new AffectationNode(ident("y"), new NbreNode(1));
        AffectationNode elseAssignment = new AffectationNode(ident("y"), new NbreNode(0));
        InstructionsNode thenBlock = singleInstruction(thenAssignment);
        InstructionsNode elseBlock = singleInstruction(elseAssignment);

        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        visitor.visit(siNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("cmp"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("goto("));
        assertTrue(code.contains("store(y@global)"));
    }

    @Test
    void siNode_nestedIf() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x > 0) { if(y > 0) { z = 1; } }
        GreaterThanNode outerCondition = new GreaterThanNode(ident("x"), new NbreNode(0));
        GreaterThanNode innerCondition = new GreaterThanNode(ident("y"), new NbreNode(0));
        AffectationNode innerAssignment = new AffectationNode(ident("z"), new NbreNode(1));
        InstructionsNode innerThenBlock = singleInstruction(innerAssignment);
        SiNode innerSi = new SiNode(innerCondition, innerThenBlock, null);
        InstructionsNode outerThenBlock = singleInstruction(innerSi);

        SiNode outerSi = new SiNode(outerCondition, outerThenBlock, null);
        visitor.visit(outerSi);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier la structure imbriquée
        int xLoadCount = code.split("load\\(x@global\\)").length - 1;
        int yLoadCount = code.split("load\\(y@global\\)").length - 1;
        assertEquals(1, xLoadCount, "Should load x once");
        assertEquals(1, yLoadCount, "Should load y once");
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("store(z@global)"));
    }

    @Test
    void siNode_withBooleanCondition() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(true) { x = 5; }
        BoolValueNode condition = new BoolValueNode(true);
        AffectationNode assignment = new AffectationNode(ident("x"), new NbreNode(5));
        InstructionsNode thenBlock = singleInstruction(assignment);

        SiNode siNode = new SiNode(condition, thenBlock, null);
        visitor.visit(siNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(true)"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("store(x@global)"));
    }

    // ===== Tests pour TantqueNode (while) =====

    @Test
    void tantqueNode_simpleWhile() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x > 0) { x = x - 1; }
        GreaterThanNode condition = new GreaterThanNode(ident("x"), new NbreNode(0));
        MinusNode subtraction = new MinusNode(ident("x"), new NbreNode(1));
        AffectationNode assignment = new AffectationNode(ident("x"), subtraction);
        InstructionsNode body = singleInstruction(assignment);

        TantqueNode whileNode = new TantqueNode(condition, body);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("not"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("sub"));
        assertTrue(code.contains("store(x@global)"));
        assertTrue(code.contains("goto("));
    }

    @Test
    void tantqueNode_withIncrement() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(i > 0) { i += -1; }
        GreaterThanNode condition = new GreaterThanNode(ident("i"), new NbreNode(0));
        SommeNode increment = new SommeNode(ident("i"), new UnaryMinusNode(new NbreNode(1)));
        InstructionsNode body = singleInstruction(increment);

        TantqueNode whileNode = new TantqueNode(condition, body);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(i@global)"));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("not"));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("neg"));
        assertTrue(code.contains("inc(i@global)"));
        assertTrue(code.contains("goto("));
    }

    @Test
    void tantqueNode_withMultipleInstructions() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x > 3) { y += 2 * x; x = x - 1; }
        GreaterThanNode condition = new GreaterThanNode(ident("x"), new NbreNode(3));

        MultiplicationNode mult = new MultiplicationNode(new NbreNode(2), ident("x"));
        SommeNode somme = new SommeNode(ident("y"), mult);

        MinusNode subtraction = new MinusNode(ident("x"), new NbreNode(1));
        AffectationNode assignment = new AffectationNode(ident("x"), subtraction);

        InstructionsNode instruction2 = singleInstruction(assignment);
        InstructionsNode instruction1 = new InstructionsNode(somme, instruction2);

        TantqueNode whileNode = new TantqueNode(condition, instruction1);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier la présence de toutes les instructions
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(3)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("not"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(2)"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("inc(y@global)"));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("sub"));
        assertTrue(code.contains("store(x@global)"));
        assertTrue(code.contains("goto("));
    }

    @Test
    void tantqueNode_withEqualityCondition() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x == 5) { x = 0; }
        EqualsNode condition = new EqualsNode(ident("x"), new NbreNode(5));
        AffectationNode assignment = new AffectationNode(ident("x"), new NbreNode(0));
        InstructionsNode body = singleInstruction(assignment);

        TantqueNode whileNode = new TantqueNode(condition, body);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("cmp"));
        assertTrue(code.contains("not"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("store(x@global)"));
        assertTrue(code.contains("goto("));
    }

    @Test
    void tantqueNode_emptyBody() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x > 0) { }
        GreaterThanNode condition = new GreaterThanNode(ident("x"), new NbreNode(0));
        InstructionsNode emptyBody = new InstructionsNode(null, null);

        TantqueNode whileNode = new TantqueNode(condition, emptyBody);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("not"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("goto("));
    }

    // ===== Tests de robustesse et cas limites =====

    @Test
    void complexNesting_whileInsideIfInsideWhile() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x > 0) { if(y > 0) { while(z > 0) { z = z - 1; } } x = x - 1; }
        // While externe
        GreaterThanNode outerCondition = new GreaterThanNode(ident("x"), new NbreNode(0));

        // If interne
        GreaterThanNode ifCondition = new GreaterThanNode(ident("y"), new NbreNode(0));

        // While le plus interne
        GreaterThanNode innerCondition = new GreaterThanNode(ident("z"), new NbreNode(0));
        MinusNode innerSub = new MinusNode(ident("z"), new NbreNode(1));
        AffectationNode innerAssignment = new AffectationNode(ident("z"), innerSub);
        InstructionsNode innerWhileBody = singleInstruction(innerAssignment);
        TantqueNode innerWhile = new TantqueNode(innerCondition, innerWhileBody);

        InstructionsNode ifBody = singleInstruction(innerWhile);
        SiNode siNode = new SiNode(ifCondition, ifBody, null);

        // Affectation après le if
        MinusNode outerSub = new MinusNode(ident("x"), new NbreNode(1));
        AffectationNode outerAssignment = new AffectationNode(ident("x"), outerSub);

        InstructionsNode siInstruction = singleInstruction(siNode);
        InstructionsNode outerWhileBody = new InstructionsNode(siNode, singleInstruction(outerAssignment));

        TantqueNode outerWhile = new TantqueNode(outerCondition, outerWhileBody);
        visitor.visit(outerWhile);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier que toutes les structures sont présentes
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("load(y@global)"));
        assertTrue(code.contains("load(z@global)"));
        int gotoCount = code.split("goto\\(").length - 1;
        int ifCount = code.split("if\\(").length - 1;
        assertTrue(gotoCount >= 2, "Should have at least 2 goto (for 2 while loops)");
        assertTrue(ifCount >= 3, "Should have at least 3 if (2 for while + 1 for if)");
    }

    @Test
    void multipleSequentialWhileLoops() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(x > 0) { x = x - 1; } while(y > 0) { y = y - 1; } while(z > 0) { z = z - 1; }
        GreaterThanNode cond1 = new GreaterThanNode(ident("x"), new NbreNode(0));
        MinusNode sub1 = new MinusNode(ident("x"), new NbreNode(1));
        AffectationNode assign1 = new AffectationNode(ident("x"), sub1);
        TantqueNode while1 = new TantqueNode(cond1, singleInstruction(assign1));

        GreaterThanNode cond2 = new GreaterThanNode(ident("y"), new NbreNode(0));
        MinusNode sub2 = new MinusNode(ident("y"), new NbreNode(1));
        AffectationNode assign2 = new AffectationNode(ident("y"), sub2);
        TantqueNode while2 = new TantqueNode(cond2, singleInstruction(assign2));

        GreaterThanNode cond3 = new GreaterThanNode(ident("z"), new NbreNode(0));
        MinusNode sub3 = new MinusNode(ident("z"), new NbreNode(1));
        AffectationNode assign3 = new AffectationNode(ident("z"), sub3);
        TantqueNode while3 = new TantqueNode(cond3, singleInstruction(assign3));

        InstructionsNode instrs3 = singleInstruction(while3);
        InstructionsNode instrs2 = new InstructionsNode(while2, instrs3);
        InstructionsNode instrs1 = new InstructionsNode(while1, instrs2);

        visitor.visit(instrs1);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier que les 3 boucles sont présentes
        int gotoCount = code.split("goto\\(").length - 1;
        assertEquals(3, gotoCount, "Should have 3 goto instructions (one per while)");

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("load(y@global)"));
        assertTrue(code.contains("load(z@global)"));
        assertTrue(code.contains("store(x@global)"));
        assertTrue(code.contains("store(y@global)"));
        assertTrue(code.contains("store(z@global)"));
    }

    @Test
    void complexArithmeticInConditions() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while((x + y) * 2 > z - 1) { x += 1; }
        PlusNode addition = new PlusNode(ident("x"), ident("y"));
        MultiplicationNode mult = new MultiplicationNode(addition, new NbreNode(2));
        MinusNode subtraction = new MinusNode(ident("z"), new NbreNode(1));
        GreaterThanNode condition = new GreaterThanNode(mult, subtraction);

        SommeNode increment = new SommeNode(ident("x"), new NbreNode(1));
        InstructionsNode body = singleInstruction(increment);

        TantqueNode whileNode = new TantqueNode(condition, body);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier que toutes les opérations sont présentes
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("load(y@global)"));
        assertTrue(code.contains("load(z@global)"));
        assertTrue(code.contains("add"));
        assertTrue(code.contains("push(2)"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("sub"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("inc(x@global)"));
    }

    @Test
    void ifElseWithComplexAssignments() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x == y) { result = x * 2 + y; } else { result = x - y * 3; }
        EqualsNode condition = new EqualsNode(ident("x"), ident("y"));

        // Then: result = x * 2 + y
        MultiplicationNode mult1 = new MultiplicationNode(ident("x"), new NbreNode(2));
        PlusNode plus1 = new PlusNode(mult1, ident("y"));
        AffectationNode thenAssign = new AffectationNode(ident("result"), plus1);

        // Else: result = x - y * 3
        MultiplicationNode mult2 = new MultiplicationNode(ident("y"), new NbreNode(3));
        MinusNode minus1 = new MinusNode(ident("x"), mult2);
        AffectationNode elseAssign = new AffectationNode(ident("result"), minus1);

        SiNode siNode = new SiNode(condition, singleInstruction(thenAssign), singleInstruction(elseAssign));
        visitor.visit(siNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("load(y@global)"));
        assertTrue(code.contains("cmp"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("add"));
        assertTrue(code.contains("sub"));
        assertTrue(code.contains("store(result@global)"));
        assertTrue(code.contains("goto("));
    }

    @Test
    void cascadingIfElse() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x > 10) { y = 3; } else { if(x > 5) { y = 2; } else { y = 1; } }
        GreaterThanNode outerCondition = new GreaterThanNode(ident("x"), new NbreNode(10));
        AffectationNode outerThen = new AffectationNode(ident("y"), new NbreNode(3));

        GreaterThanNode innerCondition = new GreaterThanNode(ident("x"), new NbreNode(5));
        AffectationNode innerThen = new AffectationNode(ident("y"), new NbreNode(2));
        AffectationNode innerElse = new AffectationNode(ident("y"), new NbreNode(1));

        SiNode innerSi = new SiNode(innerCondition, singleInstruction(innerThen), singleInstruction(innerElse));
        SiNode outerSi = new SiNode(outerCondition, singleInstruction(outerThen), singleInstruction(innerSi));

        visitor.visit(outerSi);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier la structure en cascade
        int ifCount = code.split("if\\(").length - 1;
        int gotoCount = code.split("goto\\(").length - 1;
        assertEquals(2, ifCount, "Should have 2 if instructions");
        assertTrue(gotoCount >= 2, "Should have at least 2 goto instructions");

        assertTrue(code.contains("push(10)"));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("push(3)"));
        assertTrue(code.contains("push(2)"));
        assertTrue(code.contains("push(1)"));
    }

    @Test
    void multipleIncrementOperations() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // x += 1; y += 2; z += 3;
        SommeNode inc1 = new SommeNode(ident("x"), new NbreNode(1));
        SommeNode inc2 = new SommeNode(ident("y"), new NbreNode(2));
        SommeNode inc3 = new SommeNode(ident("z"), new NbreNode(3));

        InstructionsNode instrs3 = singleInstruction(inc3);
        InstructionsNode instrs2 = new InstructionsNode(inc2, instrs3);
        InstructionsNode instrs1 = new InstructionsNode(inc1, instrs2);

        visitor.visit(instrs1);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("inc(x@global)"));
        assertTrue(code.contains("push(2)"));
        assertTrue(code.contains("inc(y@global)"));
        assertTrue(code.contains("push(3)"));
        assertTrue(code.contains("inc(z@global)"));

        // Vérifier l'ordre d'exécution
        int posX = code.indexOf("inc(x@global)");
        int posY = code.indexOf("inc(y@global)");
        int posZ = code.indexOf("inc(z@global)");
        assertTrue(posX < posY && posY < posZ, "Increments should be in order x, y, z");
    }

    @Test
    void whileWithComplexBodyAndMultipleStatements() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // while(counter > 0) { sum += counter; counter -= 1; if(sum > 100) { overflow = true; } }
        GreaterThanNode condition = new GreaterThanNode(ident("counter"), new NbreNode(0));

        SommeNode addToSum = new SommeNode(ident("sum"), ident("counter"));
        SommeNode decrementCounter = new SommeNode(ident("counter"), new UnaryMinusNode(new NbreNode(1)));

        GreaterThanNode ifCondition = new GreaterThanNode(ident("sum"), new NbreNode(100));
        AffectationNode setOverflow = new AffectationNode(ident("overflow"), new BoolValueNode(true));
        SiNode ifNode = new SiNode(ifCondition, singleInstruction(setOverflow), null);

        InstructionsNode instr3 = singleInstruction(ifNode);
        InstructionsNode instr2 = new InstructionsNode(decrementCounter, instr3);
        InstructionsNode instr1 = new InstructionsNode(addToSum, instr2);

        TantqueNode whileNode = new TantqueNode(condition, instr1);
        visitor.visit(whileNode);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("load(counter@global)"));
        assertTrue(code.contains("load(sum@global)"));
        assertTrue(code.contains("inc(sum@global)"));
        assertTrue(code.contains("inc(counter@global)"));
        assertTrue(code.contains("push(100)"));
        assertTrue(code.contains("push(true)"));
        assertTrue(code.contains("store(overflow@global)"));
    }

    @Test
    void extremeNesting_fiveLevels() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(a) { if(b) { if(c) { if(d) { if(e) { x = 1; } } } } }
        AffectationNode deepestAssignment = new AffectationNode(ident("x"), new NbreNode(1));

        SiNode level5 = new SiNode(ident("e"), singleInstruction(deepestAssignment), null);
        SiNode level4 = new SiNode(ident("d"), singleInstruction(level5), null);
        SiNode level3 = new SiNode(ident("c"), singleInstruction(level4), null);
        SiNode level2 = new SiNode(ident("b"), singleInstruction(level3), null);
        SiNode level1 = new SiNode(ident("a"), singleInstruction(level2), null);

        visitor.visit(level1);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier que toutes les variables sont chargées
        assertTrue(code.contains("load(a@global)"));
        assertTrue(code.contains("load(b@global)"));
        assertTrue(code.contains("load(c@global)"));
        assertTrue(code.contains("load(d@global)"));
        assertTrue(code.contains("load(e@global)"));
        assertTrue(code.contains("push(1)"));
        assertTrue(code.contains("store(x@global)"));

        // Vérifier le nombre de if
        int ifCount = code.split("if\\(").length - 1;
        assertEquals(5, ifCount, "Should have 5 nested if instructions");
    }

    @Test
    void mixedIncrementAndAssignment() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // x = 10; x += 5; x = x * 2; x += -3;
        AffectationNode assign1 = new AffectationNode(ident("x"), new NbreNode(10));
        SommeNode increment1 = new SommeNode(ident("x"), new NbreNode(5));
        MultiplicationNode mult = new MultiplicationNode(ident("x"), new NbreNode(2));
        AffectationNode assign2 = new AffectationNode(ident("x"), mult);
        SommeNode increment2 = new SommeNode(ident("x"), new UnaryMinusNode(new NbreNode(3)));

        InstructionsNode instr4 = singleInstruction(increment2);
        InstructionsNode instr3 = new InstructionsNode(assign2, instr4);
        InstructionsNode instr2 = new InstructionsNode(increment1, instr3);
        InstructionsNode instr1 = new InstructionsNode(assign1, instr2);

        visitor.visit(instr1);

        String code = visitor.getJajaCodeBuilder().toString();

        // Vérifier la séquence d'opérations
        assertTrue(code.contains("push(10)"));
        assertTrue(code.contains("store(x@global)"));
        assertTrue(code.contains("push(5)"));
        assertTrue(code.contains("inc(x@global)"));
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("neg"));
    }

    @Test
    void emptyIfBlocks() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(x > 0) { } else { }
        GreaterThanNode condition = new GreaterThanNode(ident("x"), new NbreNode(0));
        InstructionsNode emptyThen = new InstructionsNode(null, null);
        InstructionsNode emptyElse = new InstructionsNode(null, null);

        SiNode siNode = new SiNode(condition, emptyThen, emptyElse);
        visitor.visit(siNode);

        String code = visitor.getJajaCodeBuilder().toString();

        // Même avec des blocs vides, la structure doit être présente
        assertTrue(code.contains("load(x@global)"));
        assertTrue(code.contains("push(0)"));
        assertTrue(code.contains("sup"));
        assertTrue(code.contains("if("));
        assertTrue(code.contains("goto("));
    }

    @Test
    void largeNumbersInOperations() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // x = 999999; y = 1000000; z = x + y;
        AffectationNode assign1 = new AffectationNode(ident("x"), new NbreNode(999999));
        AffectationNode assign2 = new AffectationNode(ident("y"), new NbreNode(1000000));
        PlusNode addition = new PlusNode(ident("x"), ident("y"));
        AffectationNode assign3 = new AffectationNode(ident("z"), addition);

        InstructionsNode instr3 = singleInstruction(assign3);
        InstructionsNode instr2 = new InstructionsNode(assign2, instr3);
        InstructionsNode instr1 = new InstructionsNode(assign1, instr2);

        visitor.visit(instr1);

        String code = visitor.getJajaCodeBuilder().toString();

        assertTrue(code.contains("push(999999)"));
        assertTrue(code.contains("push(1000000)"));
        assertTrue(code.contains("add"));
        assertTrue(code.contains("store(z@global)"));
    }

    @Test
    void consecutiveConditions() {
        MiniJajaCompilerVisitor visitor = new MiniJajaCompilerVisitor(null, new DiagnosticCollector());

        // if(a) { x = 1; } if(b) { x = 2; } if(c) { x = 3; }
        SiNode if1 = new SiNode(ident("a"), singleInstruction(new AffectationNode(ident("x"), new NbreNode(1))), null);
        SiNode if2 = new SiNode(ident("b"), singleInstruction(new AffectationNode(ident("x"), new NbreNode(2))), null);
        SiNode if3 = new SiNode(ident("c"), singleInstruction(new AffectationNode(ident("x"), new NbreNode(3))), null);

        InstructionsNode instr3 = singleInstruction(if3);
        InstructionsNode instr2 = new InstructionsNode(if2, instr3);
        InstructionsNode instr1 = new InstructionsNode(if1, instr2);

        visitor.visit(instr1);

        String code = visitor.getJajaCodeBuilder().toString();

        int ifCount = code.split("if\\(").length - 1;
        assertEquals(3, ifCount, "Should have 3 separate if instructions");

        assertTrue(code.contains("load(a@global)"));
        assertTrue(code.contains("load(b@global)"));
        assertTrue(code.contains("load(c@global)"));
    }

    // ===== Tests pour les Méthodes (INVOKE et RETURN) =====
    @Test
    void methodInvocation_simpleMethodReturningConstant() {
        String miniJajaCode = """
                class Test {
                    int getValue() {
                        return 42;
                    };
                    main {
                        int x = 0;
                        x = getValue();
                        writeln(x);
                    }
                }
                """;

        // Compiler le code MiniJaja vers JajaCode
        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(miniJajaCode);
        String jajaCode = compiler.getJajaCodeBuilder().toString();

        System.out.println("=== JajaCode généré ===");
        System.out.println(jajaCode);
        System.out.println("=======================");

        // Vérifications de compilation
        assertTrue(jajaCode.contains("new(getValue"), "Should declare getValue method");
        assertTrue(jajaCode.contains("invoke(getValue"), "Should invoke getValue method");
        assertTrue(jajaCode.contains("return"), "Should have return instruction");
        assertTrue(jajaCode.contains("push(42)"), "Should push 42");
        assertTrue(jajaCode.contains("writeln"), "Should have writeln");

        // Vérifier que le code JajaCode est bien formé avec les adresses
        assertTrue(jajaCode.contains("goto("), "Should have goto to skip method body");
        assertTrue(jajaCode.contains("meth"), "Method should be declared with kind 'meth'");
    }

    /**
     * Test de compilation d'une méthode avec paramètre.
     */
    @Test
    void methodInvocation_methodWithParameter() {
        String miniJajaCode = """
                class Test {
                    int double(int n) {
                        return n;
                    };
                    main {
                        int result = 0;
                        result = double(21);
                    }
                }
                """;

        MiniJajaCompilerVisitor compiler = MiniJajaCompiler.getMiniJajaCompilerVisitorFromString(miniJajaCode);
        String jajaCode = compiler.getJajaCodeBuilder().toString();

        // Vérifications
        assertTrue(jajaCode.contains("new(double"), "Should declare double method");
        assertTrue(jajaCode.contains("push(21)"), "Should push argument 21");
        assertTrue(jajaCode.contains("invoke(double"), "Should invoke double method");
        assertTrue(jajaCode.contains("return"), "Should have return instruction");
        assertTrue(jajaCode.contains("store(result@main)"), "Should store result");
    }
}

