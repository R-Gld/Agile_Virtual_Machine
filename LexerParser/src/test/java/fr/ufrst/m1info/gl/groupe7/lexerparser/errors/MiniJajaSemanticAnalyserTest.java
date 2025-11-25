package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.memoire.SymbolTable;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class MiniJajaSemanticAnalyserTest {

    @Test
    void constructor_shouldExposeDependencies() {
        DiagnosticCollector collector = mock(DiagnosticCollector.class);
        SymbolTable symbolTable = mock(SymbolTable.class);

        MiniJajaSemanticAnalyser analyser = new MiniJajaSemanticAnalyser(collector, symbolTable);

        assertSame(collector, analyser.collector());
        assertSame(symbolTable, analyser.symbolTable());
    }

    @Test
    void analyse_shouldNotReportErrors_whenEmptyClass() {
        DiagnosticCollector collector = mock(DiagnosticCollector.class);
        SymbolTable symbolTable = mock(SymbolTable.class);
        MiniJajaSemanticAnalyser analyser = new MiniJajaSemanticAnalyser(collector, symbolTable);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);

        MainNode mainNode = mock(MainNode.class);
        when(classe.getMethodeMain()).thenReturn(mainNode);
        when(mainNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(null);

        analyser.analyse(classe);

        verifyNoInteractions(collector);
    }

    @Test
    void analyse_shouldReportError_onDuplicateGlobalVarDeclaration() {
        DiagnosticCollector collector = mock(DiagnosticCollector.class);
        SymbolTable symbolTable = mock(SymbolTable.class);
        MiniJajaSemanticAnalyser analyser = new MiniJajaSemanticAnalyser(collector, symbolTable);

        String varName = "x";

        IdentNode ident1 = mock(IdentNode.class);
        when(ident1.getNom()).thenReturn(varName);
        VarNode varNode1 = mock(VarNode.class);
        when(varNode1.getIdent()).thenReturn(ident1);
        when(varNode1.getType()).thenReturn(Type.ENTIER);

        IdentNode ident2 = mock(IdentNode.class);
        when(ident2.getNom()).thenReturn(varName);
        VarNode varNode2 = mock(VarNode.class);
        when(varNode2.getIdent()).thenReturn(ident2);
        when(varNode2.getType()).thenReturn(Type.ENTIER);

        DeclsNode decls2 = mock(DeclsNode.class);
        when(decls2.getDecl()).thenReturn(varNode2);
        when(decls2.getDecls()).thenReturn(null);

        DeclsNode decls1 = mock(DeclsNode.class);
        when(decls1.getDecl()).thenReturn(varNode1);
        when(decls1.getDecls()).thenReturn(decls2);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(decls1);

        MainNode mainNode = mock(MainNode.class);
        when(classe.getMethodeMain()).thenReturn(mainNode);
        when(mainNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(null);

        analyser.analyse(classe);

        verify(collector, atLeastOnce()).report(
                eq(Severity.ERROR),
                eq(Phase.SEMANTIC),
                any(SourcePosition.class),
                contains("Duplicate variable declaration")
        );
    }

    @Test
    void analyse_shouldReportError_onUndeclaredVariableInAffectation() {
        DiagnosticCollector collector = mock(DiagnosticCollector.class);
        SymbolTable symbolTable = mock(SymbolTable.class);
        MiniJajaSemanticAnalyser analyser = new MiniJajaSemanticAnalyser(collector, symbolTable);

        ClasseNode classe = mock(ClasseNode.class);
        when(classe.getDeclarations()).thenReturn(null);

        MainNode mainNode = mock(MainNode.class);
        when(classe.getMethodeMain()).thenReturn(mainNode);

        InstructionsNode instrs = mock(InstructionsNode.class);
        when(mainNode.getVars()).thenReturn(null);
        when(mainNode.getInstrs()).thenReturn(instrs);

        AffectationNode affectation = mock(AffectationNode.class);
        IdentNode ident = mock(IdentNode.class);
        when(ident.getNom()).thenReturn("x");
        when(affectation.getIdent1Node()).thenReturn(ident);

        Expression expr = mock(Expression.class);
        when(affectation.getExpression()).thenReturn(expr);

        when(instrs.getChildren()).thenReturn(List.of(affectation));

        analyser.analyse(classe);

        verify(collector, atLeastOnce()).report(
                eq(Severity.ERROR),
                eq(Phase.SEMANTIC),
                any(SourcePosition.class),
                contains("Undeclared variable")
        );
    }
}