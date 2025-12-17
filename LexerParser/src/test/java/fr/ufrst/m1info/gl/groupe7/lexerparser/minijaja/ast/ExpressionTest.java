package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

class ExpressionTest {

    // helper to provide a Stacks instance (mocked) to pass to evaluate
    private final Stacks stacks = Mockito.mock(Stacks.class);

    @Test
    void nbreNode_evaluate_returnsIntegerValue() {
        NbreNode n = new NbreNode(42);
        Object res = n.evaluate(stacks);
        assertInstanceOf(Integer.class, res, "expected Integer result");
        assertEquals(42, ((Integer) res).intValue());
    }

    @Test
    void boolValueNode_evaluate_returnsBoolean() {
        BoolValueNode t = new BoolValueNode(true);
        BoolValueNode f = new BoolValueNode(false);
        Object rt = t.evaluate(stacks);
        Object rf = f.evaluate(stacks);
        assertInstanceOf(Boolean.class, rt);
        assertInstanceOf(Boolean.class, rf);
        assertEquals(Boolean.TRUE, rt);
        assertEquals(Boolean.FALSE, rf);
    }

    @Test
    void binary_arithmetic_nodes_evaluate_correctly() {
        PlusNode plus = new PlusNode(new NbreNode(1), new NbreNode(2));
        MinusNode minus = new MinusNode(new NbreNode(5), new NbreNode(3));
        MultiplicationNode mul = new MultiplicationNode(new NbreNode(2), new NbreNode(4));
        DivisionNode div = new DivisionNode(new NbreNode(8), new NbreNode(2));

        assertEquals(3, ((Integer) plus.evaluate(stacks)).intValue());
        assertEquals(2, ((Integer) minus.evaluate(stacks)).intValue());
        assertEquals(8, ((Integer) mul.evaluate(stacks)).intValue());
        assertEquals(4, ((Integer) div.evaluate(stacks)).intValue());
    }

    @Test
    void unary_minus_evaluate_returnsNegative() {
        UnaryMinusNode u = new UnaryMinusNode(new NbreNode(7));
        Object res = u.evaluate(stacks);
        assertInstanceOf(Integer.class, res);
        assertEquals(-7, ((Integer) res).intValue());
    }

    @Test
    void comparison_and_logic_nodes_evaluate_correctly() {
        EqualsNode eqTrue = new EqualsNode(new NbreNode(1), new NbreNode(1));
        EqualsNode eqFalse = new EqualsNode(new NbreNode(1), new NbreNode(2));
        GreaterThanNode gtTrue = new GreaterThanNode(new NbreNode(5), new NbreNode(2));
        GreaterThanNode gtFalse = new GreaterThanNode(new NbreNode(1), new NbreNode(3));

        AndNode and = new AndNode(new BoolValueNode(true), new BoolValueNode(false));
        OrNode or = new OrNode(new BoolValueNode(false), new BoolValueNode(true));
        NotNode not = new NotNode(new BoolValueNode(true));

        assertEquals(Boolean.TRUE, eqTrue.evaluate(stacks));
        assertEquals(Boolean.FALSE, eqFalse.evaluate(stacks));
        assertEquals(Boolean.TRUE, gtTrue.evaluate(stacks));
        assertEquals(Boolean.FALSE, gtFalse.evaluate(stacks));

        assertEquals(Boolean.FALSE, and.evaluate(stacks));
        assertEquals(Boolean.TRUE, or.evaluate(stacks));
        assertEquals(Boolean.FALSE, not.evaluate(stacks));
    }

    @Test
    void complex_arithmetic_precedence_evaluates_correctly() {
        // 1 + 2 * 3 => 1 + (2*3) = 7
        MultiplicationNode mul = new MultiplicationNode(new NbreNode(2), new NbreNode(3));
        PlusNode expr1 = new PlusNode(new NbreNode(1), mul);
        assertEquals(7, ((Integer) expr1.evaluate(stacks)).intValue());

        // (1 + 2) * 3 => (1+2) * 3 = 9
        PlusNode plus = new PlusNode(new NbreNode(1), new NbreNode(2));
        MultiplicationNode expr2 = new MultiplicationNode(plus, new NbreNode(3));
        assertEquals(9, ((Integer) expr2.evaluate(stacks)).intValue());
    }

    @Test
    void mixed_unary_and_division_and_multiplication() {
        // - (2 + 3) * (10 / 5) => - (5 * 2) = -10
        PlusNode sum = new PlusNode(new NbreNode(2), new NbreNode(3));
        DivisionNode div = new DivisionNode(new NbreNode(10), new NbreNode(5));
        MultiplicationNode mult = new MultiplicationNode(sum, div);
        UnaryMinusNode whole = new UnaryMinusNode(mult);
        assertEquals(-10, ((Integer) whole.evaluate(stacks)).intValue());
    }

    @Test
    void logical_precedence_and_mixed_comparisons() {
        // true || false && false  => true || (false && false) = true
        AndNode and = new AndNode(new BoolValueNode(false), new BoolValueNode(false));
        OrNode or = new OrNode(new BoolValueNode(true), and);
        assertEquals(Boolean.TRUE, or.evaluate(stacks));

        // (1 + 4) > 3 && !(2 == 3) => (5 > 3) && !(false) => true && true => true
        PlusNode plus = new PlusNode(new NbreNode(1), new NbreNode(4));
        GreaterThanNode gt = new GreaterThanNode(plus, new NbreNode(3));
        EqualsNode eq = new EqualsNode(new NbreNode(2), new NbreNode(3));
        NotNode not = new NotNode(eq);
        AndNode combined = new AndNode(gt, not);
        assertEquals(Boolean.TRUE, combined.evaluate(stacks));
    }
}