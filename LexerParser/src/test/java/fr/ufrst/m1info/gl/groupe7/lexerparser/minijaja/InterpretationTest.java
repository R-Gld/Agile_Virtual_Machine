package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterpretationTest {

    private Stacks stacks;

    @BeforeEach
    void setUp() {
        stacks = Mockito.mock(Stacks.class);
    }

    @Test
    void testInterpretCstNodeWithValue() {
        // Arrange
        String varName = "x";
        Type type = Type.ENTIER;
        int value = 10;

        IdentNode ident = new IdentNode(varName);
        Expression expression = Mockito.mock(Expression.class);
        when(expression.evaluate(stacks)).thenReturn(value);

        CstNode cstNode = new CstNode(type, ident, expression);

        // Act
        cstNode.interpret(stacks);

        // Assert
        verify(stacks).declareCst(varName, value, type);
    }

    @Test
    void testInterpretCstNodeWithoutValue() {
        // Arrange
        String varName = "y";
        Type type = Type.BOOLEEN;

        IdentNode ident = new IdentNode(varName);
        
        CstNode cstNode = new CstNode(type, ident);

        // Act
        cstNode.interpret(stacks);

        // Assert
        verify(stacks).declareCst(varName, type);
    }
}
