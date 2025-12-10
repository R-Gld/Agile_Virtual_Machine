package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions;


import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class TestAppelINode {

    @Test
    void testInterpretThrowException() {
        IdentNode ident = Mockito.mock(IdentNode.class);
        ListExpNode listExp = Mockito.mock(ListExpNode.class);
        Stacks stacks = Mockito.mock(Stacks.class);
        List<Object> listExpReturn = new ArrayList<>();
        listExpReturn.add(10);
        listExpReturn.add(20);
        Mockito.when(listExp.evaluate(stacks)).thenReturn(listExpReturn);

        MethodeNode methodeNode = Mockito.mock(MethodeNode.class);
        Mockito.when(ident.evaluate(stacks)).thenReturn(methodeNode);

        // Defined entete to return with different size so it trigger RuntimeException
        List<EnteteNode> entsToReturn = new ArrayList<>();
        entsToReturn.add(new EnteteNode(new IdentNode("a"), Type.ENTIER));
        EntetesNode entetesNode = Mockito.mock(EntetesNode.class);
        Mockito.when(entetesNode.evaluate(stacks)).thenReturn(entsToReturn);
        Mockito.when(methodeNode.getEntetes()).thenReturn(entetesNode);

        AppelINode node = new AppelINode(ident, listExp);
        Assertions.assertThrows(RuntimeException.class, () -> node.interpret(stacks));

    }
}
