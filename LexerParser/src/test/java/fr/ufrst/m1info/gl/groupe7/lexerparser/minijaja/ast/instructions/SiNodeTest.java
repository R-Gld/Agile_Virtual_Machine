package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

public class SiNodeTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = new Stacks();
    }

    // ========================================
    // Constructor Tests
    // ========================================

    @Test
    void testConstructor_WithoutElseBranch() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        
        assertNotNull(siNode);
        assertEquals(condition, siNode.getExpressionNode());
        assertEquals(thenBlock, siNode.getInstructionsNode());
        assertNull(siNode.getInstructionsNode2());
    }

    @Test
    void testConstructor_WithElseBranch() {
        Expression condition = new BoolValueNode(false);
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        
        assertNotNull(siNode);
        assertEquals(condition, siNode.getExpressionNode());
        assertEquals(thenBlock, siNode.getInstructionsNode());
        assertEquals(elseBlock, siNode.getInstructionsNode2());
    }

    // ========================================
    // Getter Tests
    // ========================================

    @Test
    void testGetExpressionNode() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        SiNode siNode = new SiNode(condition, thenBlock);
        
        assertEquals(condition, siNode.getExpressionNode());
    }

    @Test
    void testGetInstructionsNode() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        SiNode siNode = new SiNode(condition, thenBlock);
        
        assertEquals(thenBlock, siNode.getInstructionsNode());
    }

    @Test
    void testGetInstructionsNode2_WithElse() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        
        assertEquals(elseBlock, siNode.getInstructionsNode2());
    }

    @Test
    void testGetInstructionsNode2_WithoutElse() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        SiNode siNode = new SiNode(condition, thenBlock);
        
        assertNull(siNode.getInstructionsNode2());
    }

    // ========================================
    // Children Management Tests
    // ========================================

    @Test
    void testSetChildren_WithNonNullIterable() {
        SiNode siNode = new SiNode(new BoolValueNode(true), new InstructionsNode());
        List<AstNode> children = List.of(new InstructionsNode());
        
        siNode.setchildren(children);
        
        assertEquals(children, siNode.getChildren());
    }

    @Test
    void testSetChildren_WithNull() {
        SiNode siNode = new SiNode(new BoolValueNode(true), new InstructionsNode());
        
        siNode.setchildren(null);
        
        assertNull(siNode.getChildren());
    }

    @Test
    void testGetChildren_InitiallyNull() {
        SiNode siNode = new SiNode(new BoolValueNode(true), new InstructionsNode());
        
        assertNull(siNode.getChildren());
    }

    // ========================================
    // toStringTree Tests
    // ========================================

    @Test
    void testToStringTree_WithElseBranch() {
        Expression condition = new BoolValueNode(true);
        AffectationNode thenInstr = new AffectationNode(new IdentNode("x"), new NbreNode(10));
        AffectationNode elseInstr = new AffectationNode(new IdentNode("y"), new NbreNode(20));
        InstructionsNode thenBlock = new InstructionsNode(thenInstr, new InstructionsNode());
        InstructionsNode elseBlock = new InstructionsNode(elseInstr, new InstructionsNode());
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        String tree = siNode.toStringTree();
        
        assertTrue(tree.startsWith("si ("));
        assertTrue(tree.contains("true"));
        assertTrue(tree.contains("Instrs("));
    }

    @Test
    void testToStringTree_WithoutElseBranch() {
        Expression condition = new BoolValueNode(false);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        String tree = siNode.toStringTree();
        
        assertTrue(tree.startsWith("si ("));
        assertTrue(tree.contains("false"));
        assertTrue(tree.contains("inil"));
    }

    @Test
    void testToStringTree_WithComplexCondition() {
        Expression condition = new EqualsNode(new NbreNode(5), new NbreNode(5));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        String tree = siNode.toStringTree();
        
        assertTrue(tree.contains("=="));
        assertTrue(tree.contains("nbre(5)"));
    }

    // ========================================
    // Interpret Tests - Condition True
    // ========================================

    @Test
    void testInterpret_ConditionTrue_ExecutesThenBlock() {
        Expression condition = new BoolValueNode(true);
        AffectationNode thenInstr = new AffectationNode(new IdentNode("x"), new NbreNode(10));
        InstructionsNode thenBlock = new InstructionsNode(thenInstr, new InstructionsNode());
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        assertEquals(thenBlock.getChildren(), children);
    }

    @Test
    void testInterpret_ConditionTrue_WithElse_ExecutesThenBlock() {
        Expression condition = new BoolValueNode(true);
        AffectationNode thenInstr = new AffectationNode(new IdentNode("x"), new NbreNode(10));
        AffectationNode elseInstr = new AffectationNode(new IdentNode("y"), new NbreNode(20));
        InstructionsNode thenBlock = new InstructionsNode(thenInstr, new InstructionsNode());
        InstructionsNode elseBlock = new InstructionsNode(elseInstr, new InstructionsNode());
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        assertEquals(thenBlock.getChildren(), children);
    }

    @Test
    void testInterpret_TrueCondition_WithMockedExpression() {
        Expression mockCondition = Mockito.mock(Expression.class);
        Mockito.when(mockCondition.evaluate(any(Stacks.class))).thenReturn(true);
        
        AstNode mockChild = Mockito.mock(AstNode.class);
        InstructionsNode thenBlock = Mockito.mock(InstructionsNode.class);
        Mockito.when(thenBlock.getChildren()).thenReturn(List.of(mockChild));
        
        SiNode siNode = new SiNode(mockCondition, thenBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        
        List<AstNode> childList = new ArrayList<>();
        children.forEach(childList::add);
        assertEquals(1, childList.size());
        assertEquals(mockChild, childList.getFirst());
    }

    // ========================================
    // Interpret Tests - Condition False
    // ========================================

    @Test
    void testInterpret_ConditionFalse_NoElse_ChildrenNull() {
        Expression condition = new BoolValueNode(false);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertEquals(List.of(), siNode.getChildren());
    }

    @Test
    void testInterpret_ConditionFalse_WithElse_ExecutesElseBlock() {
        Expression condition = new BoolValueNode(false);
        AffectationNode thenInstr = new AffectationNode(new IdentNode("x"), new NbreNode(10));
        AffectationNode elseInstr = new AffectationNode(new IdentNode("y"), new NbreNode(20));
        InstructionsNode thenBlock = new InstructionsNode(thenInstr, new InstructionsNode());
        InstructionsNode elseBlock = new InstructionsNode(elseInstr, new InstructionsNode());
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        assertEquals(elseBlock.getChildren(), children);
    }

    @Test
    void testInterpret_FalseCondition_WithMockedExpression() {
        Expression mockCondition = Mockito.mock(Expression.class);
        Mockito.when(mockCondition.evaluate(any(Stacks.class))).thenReturn(false);
        
        AstNode mockElseChild = Mockito.mock(AstNode.class);
        InstructionsNode thenBlock = Mockito.mock(InstructionsNode.class);
        InstructionsNode elseBlock = Mockito.mock(InstructionsNode.class);
        Mockito.when(elseBlock.getChildren()).thenReturn(List.of(mockElseChild));
        
        SiNode siNode = new SiNode(mockCondition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        
        List<AstNode> childList = new ArrayList<>();
        children.forEach(childList::add);
        assertEquals(1, childList.size());
        assertEquals(mockElseChild, childList.getFirst());
    }

    // ========================================
    // Complex Condition Tests
    // ========================================

    @Test
    void testInterpret_EqualityConditionTrue() {
        Expression condition = new EqualsNode(new NbreNode(5), new NbreNode(5));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_EqualityConditionFalse() {
        Expression condition = new EqualsNode(new NbreNode(5), new NbreNode(3));
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    @Test
    void testInterpret_GreaterThanConditionTrue() {
        Expression condition = new GreaterThanNode(new NbreNode(10), new NbreNode(5));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_GreaterThanConditionFalse() {
        Expression condition = new GreaterThanNode(new NbreNode(3), new NbreNode(5));
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    @Test
    void testInterpret_AndConditionTrue() {
        Expression condition = new AndNode(new BoolValueNode(true), new BoolValueNode(true));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_AndConditionFalse() {
        Expression condition = new AndNode(new BoolValueNode(true), new BoolValueNode(false));
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    @Test
    void testInterpret_OrConditionTrue() {
        Expression condition = new OrNode(new BoolValueNode(false), new BoolValueNode(true));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_OrConditionFalse() {
        Expression condition = new OrNode(new BoolValueNode(false), new BoolValueNode(false));
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    @Test
    void testInterpret_NotConditionTrue() {
        Expression condition = new NotNode(new BoolValueNode(false));
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_NotConditionFalse() {
        Expression condition = new NotNode(new BoolValueNode(true));
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    @Test
    void testInterpret_ComplexNestedCondition() {
        // (5 > 3) && (10 == 10)
        Expression gt = new GreaterThanNode(new NbreNode(5), new NbreNode(3));
        Expression eq = new EqualsNode(new NbreNode(10), new NbreNode(10));
        Expression condition = new AndNode(gt, eq);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_ComplexNestedConditionFalse() {
        // (3 > 5) || (10 == 5)
        Expression gt = new GreaterThanNode(new NbreNode(3), new NbreNode(5));
        Expression eq = new EqualsNode(new NbreNode(10), new NbreNode(5));
        Expression condition = new OrNode(gt, eq);
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode elseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        assertEquals(elseBlock.getChildren(), siNode.getChildren());
    }

    // ========================================
    // Edge Cases and Error Handling
    // ========================================

    @Test
    void testInterpret_NonBooleanExpression_ThrowsClassCastException() {
        // Create expression that returns Integer instead of Boolean
        Expression invalidCondition = new NbreNode(1);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(invalidCondition, thenBlock);
        
        assertThrows(ClassCastException.class, () -> {
            siNode.interpret(stacks);
        });
    }

    @Test
    void testInterpret_EmptyThenBlock() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode emptyThenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, emptyThenBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_EmptyElseBlock() {
        Expression condition = new BoolValueNode(false);
        InstructionsNode thenBlock = new InstructionsNode();
        InstructionsNode emptyElseBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock, emptyElseBlock);
        siNode.interpret(stacks);
        
        assertNotNull(siNode.getChildren());
    }

    @Test
    void testInterpret_MultipleInstructionsInThenBlock() {
        Expression condition = new BoolValueNode(true);
        AffectationNode instr1 = new AffectationNode(new IdentNode("x"), new NbreNode(10));
        AffectationNode instr2 = new AffectationNode(new IdentNode("y"), new NbreNode(20));
        InstructionsNode instrs2 = new InstructionsNode(instr2, new InstructionsNode());
        InstructionsNode thenBlock = new InstructionsNode(instr1, instrs2);
        
        SiNode siNode = new SiNode(condition, thenBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        
        List<AstNode> childList = new ArrayList<>();
        children.forEach(childList::add);
        assertTrue(!childList.isEmpty());
    }

    @Test
    void testInterpret_MultipleInstructionsInElseBlock() {
        Expression condition = new BoolValueNode(false);
        InstructionsNode thenBlock = new InstructionsNode();
        AffectationNode instr1 = new AffectationNode(new IdentNode("a"), new NbreNode(1));
        AffectationNode instr2 = new AffectationNode(new IdentNode("b"), new NbreNode(2));
        InstructionsNode instrs2 = new InstructionsNode(instr2, new InstructionsNode());
        InstructionsNode elseBlock = new InstructionsNode(instr1, instrs2);
        
        SiNode siNode = new SiNode(condition, thenBlock, elseBlock);
        siNode.interpret(stacks);
        
        Iterable<AstNode> children = siNode.getChildren();
        assertNotNull(children);
        
        List<AstNode> childList = new ArrayList<>();
        children.forEach(childList::add);
        assertTrue(!childList.isEmpty());
    }

    // ========================================
    // Integration Tests
    // ========================================

    @Test
    void testInterpret_NestedSiNodes() {
        // Outer: if (true)
        // Inner: if (false) else ...
        Expression outerCondition = new BoolValueNode(true);
        Expression innerCondition = new BoolValueNode(false);
        
        InstructionsNode innerElse = new InstructionsNode();
        SiNode innerSi = new SiNode(innerCondition, new InstructionsNode(), innerElse);
        InstructionsNode outerThen = new InstructionsNode(innerSi, new InstructionsNode());
        
        SiNode outerSi = new SiNode(outerCondition, outerThen);
        outerSi.interpret(stacks);
        
        assertNotNull(outerSi.getChildren());
    }

    @Test
    void testInterpret_ConditionEvaluationOrder() {
        Expression mockCondition = Mockito.mock(Expression.class);
        Mockito.when(mockCondition.evaluate(any(Stacks.class))).thenReturn(true);
        
        InstructionsNode thenBlock = new InstructionsNode();
        SiNode siNode = new SiNode(mockCondition, thenBlock);
        
        siNode.interpret(stacks);
        
        Mockito.verify(mockCondition, Mockito.times(1)).evaluate(stacks);
    }

    @Test
    void testInterpret_ChildrenNotModifiedIfNotInterpreted() {
        Expression condition = new BoolValueNode(true);
        InstructionsNode thenBlock = new InstructionsNode();
        
        SiNode siNode = new SiNode(condition, thenBlock);
        
        // Before interpret
        assertNull(siNode.getChildren());
        
        // After interpret
        siNode.interpret(stacks);
        assertNotNull(siNode.getChildren());
    }


}
