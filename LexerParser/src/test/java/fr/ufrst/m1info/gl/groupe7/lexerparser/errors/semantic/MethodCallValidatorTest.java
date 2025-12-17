package fr.ufrst.m1info.gl.groupe7.lexerparser.errors.semantic;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Phase;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.Severity;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MethodCallValidatorTest {

    private MethodCallValidator validator;
    private SemanticContext context;
    private ScopeResolver scopeResolver;
    private DeclarationCollector declarationCollector;
    private DiagnosticCollector diagnosticCollector;
    private Stacks stacks;

    @BeforeEach
    void setUp() {
        diagnosticCollector = mock(DiagnosticCollector.class);
        stacks = new Stacks();
        context = mock(SemanticContext.class);
        scopeResolver = mock(ScopeResolver.class);
        declarationCollector = mock(DeclarationCollector.class);

        when(context.getCollector()).thenReturn(diagnosticCollector);
        when(context.getStacks()).thenReturn(stacks);

        validator = new MethodCallValidator(context, scopeResolver, declarationCollector);
    }

    @Test
    void testValidateMethodCall_validAppelINode() {
        // Given - Create real AppelI node
        IdentNode ident = new IdentNode("testMethod");
        ListExpNode args = new ListExpNode(new NbreNode(42), null);
        AppelINode callNode = new AppelINode(ident, args);

        // Setup method declaration
        String methodSignature = "testMethod@integer";
        EnteteNode param = new EnteteNode(new IdentNode("param"), Type.ENTIER);
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, new EntetesNode(param, new EntetesNode()));
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("testMethod")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(Collections.singletonList(Type.ENTIER));
        when(declarationCollector.collectArgumentTypes(args)).thenReturn(Collections.singletonList(Type.ENTIER));
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertEquals(Type.ENTIER, result);
        verify(diagnosticCollector, never()).report(any(), any(), any(), anyString());
    }

    @Test
    void testValidateMethodCall_validAppelENode() {
        // Given - Create real AppelE node
        IdentNode ident = new IdentNode("calculate");
        ListExpNode args = new ListExpNode(new NbreNode(10), null);
        AppelENode callNode = new AppelENode(ident, args);

        // Setup method declaration
        String methodSignature = "calculate@integer";
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, new EntetesNode());
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("calculate")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(Collections.emptyList());
        when(declarationCollector.collectArgumentTypes(any())).thenReturn(Collections.emptyList());
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertEquals(Type.ENTIER, result);
        verify(diagnosticCollector, never()).report(any(), any(), any(), anyString());
    }

    @Test
    void testValidateMethodCall_invalidNodeType() {
        // Given - Create invalid node type
        AstNode invalidNode = new NbreNode(42); // Not an AppelI or AppelE

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validator.validateMethodCall(invalidNode));
        assertTrue(exception.getMessage().contains("validateMethodCall expects an AppelINode or AppelENode"));
    }

    @Test
    void testValidateMethodCall_nullNode() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> validator.validateMethodCall(null));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void testValidateMethodCall_undeclaredMethod() {
        // Given
        IdentNode ident = new IdentNode("unknownMethod");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        when(scopeResolver.findMethodSignature("unknownMethod")).thenReturn(null);
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertNull(result);
        verify(diagnosticCollector).report(
            eq(Severity.ERROR),
            eq(Phase.SEMANTIC),
            any(SourcePosition.class),
            argThat(msg -> msg.contains("Undeclared method") && msg.contains("unknownMethod"))
        );
    }

    @Test
    void testValidateMethodCall_methodValueNotMethodeNode() {
        // Given
        IdentNode ident = new IdentNode("badMethod");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        String methodSignature = "badMethod@integer";
        // Store a String instead of MethodeNode
        stacks.declareVar(methodSignature, "not a MethodeNode", Type.ENTIER);

        when(scopeResolver.findMethodSignature("badMethod")).thenReturn(methodSignature);
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertNull(result);
    }

    @Test
    void testValidateMethodCall_parameterCountMismatch() {
        // Given
        IdentNode ident = new IdentNode("myMethod");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        String methodSignature = "myMethod@integer";
        // Method expects 2 parameters
        EnteteNode param1 = new EnteteNode(new IdentNode("a"), Type.ENTIER);
        EnteteNode param2 = new EnteteNode(new IdentNode("b"), Type.BOOLEEN);
        EntetesNode entetes = new EntetesNode(param1, new EntetesNode(param2, new EntetesNode()));
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, entetes);
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("myMethod")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(Arrays.asList(Type.ENTIER, Type.BOOLEEN));
        when(declarationCollector.collectArgumentTypes(args)).thenReturn(Collections.singletonList(Type.ENTIER));
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertNull(result);
        verify(diagnosticCollector).report(
            eq(Severity.ERROR),
            eq(Phase.SEMANTIC),
            any(SourcePosition.class),
            argThat(msg -> msg.contains("expects 2 parameter(s), but got 1"))
        );
    }

    @Test
    void testValidateMethodCall_parameterTypeMismatch() {
        // Given
        IdentNode ident = new IdentNode("process");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        String methodSignature = "process@void";
        EnteteNode param = new EnteteNode(new IdentNode("x"), Type.ENTIER);
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, new EntetesNode(param, new EntetesNode()));
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("process")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(Collections.singletonList(Type.ENTIER));
        when(declarationCollector.collectArgumentTypes(args)).thenReturn(Collections.singletonList(Type.BOOLEEN));
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then
        assertEquals(Type.ENTIER, result); // Still returns method type even with parameter error
        verify(diagnosticCollector).report(
            eq(Severity.ERROR),
            eq(Phase.SEMANTIC),
            any(SourcePosition.class),
            argThat(msg -> msg.contains("parameter 1") && msg.contains("expects type 'integer', but got 'boolean'"))
        );
    }

    @Test
    void testValidateMethodCall_nullActualParameterType() {
        // Given - one actual parameter type is null (should not report error)
        IdentNode ident = new IdentNode("testNull");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        String methodSignature = "testNull@integer";
        EnteteNode param = new EnteteNode(new IdentNode("x"), Type.ENTIER);
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, new EntetesNode(param, new EntetesNode()));
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("testNull")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(Collections.singletonList(Type.ENTIER));
        when(declarationCollector.collectArgumentTypes(args)).thenReturn(Collections.singletonList(null));
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then - should succeed because null actual type is not checked (line 111)
        assertEquals(Type.ENTIER, result);
        verify(diagnosticCollector, never()).report(any(), any(), any(), anyString());
    }

    @Test
    void testValidateMethodCall_multipleParameterTypeMismatch() {
        // Given - multiple parameters with type mismatches
        IdentNode ident = new IdentNode("calc");
        ListExpNode args = new ListExpNode(new NbreNode(1), null);
        AppelINode callNode = new AppelINode(ident, args);

        String methodSignature = "calc@integer";
        EnteteNode param1 = new EnteteNode(new IdentNode("a"), Type.ENTIER);
        EnteteNode param2 = new EnteteNode(new IdentNode("b"), Type.BOOLEEN);
        EnteteNode param3 = new EnteteNode(new IdentNode("c"), Type.ENTIER);
        EntetesNode entetes = new EntetesNode(param1,
            new EntetesNode(param2,
                new EntetesNode(param3, new EntetesNode())));
        MethodeNode methodeNode = createMethodNode(Type.ENTIER, entetes);
        stacks.declareMeth(methodSignature, methodeNode, Type.ENTIER);

        when(scopeResolver.findMethodSignature("calc")).thenReturn(methodSignature);
        when(declarationCollector.collectParameterTypes(any())).thenReturn(
            Arrays.asList(Type.ENTIER, Type.BOOLEEN, Type.ENTIER));
        when(declarationCollector.collectArgumentTypes(args)).thenReturn(
            Arrays.asList(Type.BOOLEEN, Type.ENTIER, Type.BOOLEEN));
        when(context.createPosition(callNode)).thenReturn(new SourcePosition("test.jj", 1, 0));

        // When
        Type result = validator.validateMethodCall(callNode);

        // Then - should report 3 errors (one for each mismatch)
        assertEquals(Type.ENTIER, result);
        verify(diagnosticCollector, times(3)).report(
            eq(Severity.ERROR),
            eq(Phase.SEMANTIC),
            any(SourcePosition.class),
            anyString()
        );
    }

    // Helper method to create MethodeNode
    private MethodeNode createMethodNode(Type returnType, AstNode entetes) {
        IdentNode methodIdent = new IdentNode("testMethod");
        VarsNode vars = new VarsNode();
        InstructionsNode instructions = new InstructionsNode();

        return new MethodeNode(
            returnType,
            methodIdent,
            (EntetesNode) entetes,
            vars,
            instructions
        );
    }
}
