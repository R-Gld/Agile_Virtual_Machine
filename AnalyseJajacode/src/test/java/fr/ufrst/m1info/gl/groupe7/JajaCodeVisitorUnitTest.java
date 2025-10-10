package fr.ufrst.m1info.gl.groupe7;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JajaCodeVisitorUnitTest {

    @Spy
    @InjectMocks
    private JajaCodeVisitor jajaCodeVisitor;

    @Mock
    private JajaCodeParser.InstrContext instrContext;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    private Stack<Object> getStack() throws NoSuchFieldException, IllegalAccessException {
        Field stackField = JajaCodeVisitor.class.getDeclaredField("stack");
        stackField.setAccessible(true);
        return (Stack<Object>) stackField.get(jajaCodeVisitor);
    }

    @Test
    public void testVisitInstr_withPush() throws Exception {
        JajaCodeParser.ValeurContext valeurContext = mock(JajaCodeParser.ValeurContext.class);
        when(instrContext.PUSH()).thenReturn(mock(TerminalNode.class));
        when(instrContext.valeur()).thenReturn(valeurContext);
        doReturn(42).when(jajaCodeVisitor).visit(valeurContext);

        jajaCodeVisitor.visitInstr(instrContext);

        Stack<Object> stack = getStack();
        assertFalse(stack.isEmpty());
        assertEquals(42, stack.peek());
        verify(jajaCodeVisitor).visit(valeurContext);
    }

    @Test
    public void testVisitInstr_withLoad() throws Exception {
        JajaCodeParser.IdentContext identContext = mock(JajaCodeParser.IdentContext.class);
        when(instrContext.LOAD()).thenReturn(mock(TerminalNode.class));
        when(instrContext.ident()).thenReturn(identContext);
        when(identContext.getText()).thenReturn("wazaaaaaaaaa");

        jajaCodeVisitor.visitInstr(instrContext);

        Stack<Object> stack = getStack();
        assertFalse(stack.isEmpty());
        assertEquals("wazaaaaaaaaa", stack.peek());
    }

    @Test
    public void testVisitInstr_withOper() {
        JajaCodeParser.OperContext operContext = mock(JajaCodeParser.OperContext.class);
        when(instrContext.oper()).thenReturn(operContext);
        doReturn(null).when(jajaCodeVisitor).visit(operContext);

        jajaCodeVisitor.visitInstr(instrContext);

        verify(jajaCodeVisitor).visit(operContext);
    }
}