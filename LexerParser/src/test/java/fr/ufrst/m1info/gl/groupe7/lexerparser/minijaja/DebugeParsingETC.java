package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode; // Assurez-vous d'importer AstNode
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Walker;

/**
 * Test qui parse un programme, construit l'AST via le Visitor,
 * affiche l'AST et le dump de la table des symboles.
 */
public class DebugeParsingETC {

  @Test
  public void DebugeParsing() {
    Stacks stacks = new Stacks();
    String program = """
      class C {
      int x = 0 ;
      int t[4] ;
      int fct(int max) {
        int y = 5 ;
        while (max > 0) {
        y += max ;
        max = max - 1 ;
        } ;
        return y ;
      } ;
      main {
        while (4 > x) {
        t[x] = x-1 ;
        x++ ;
        } ;
      }
      }
      """;

    //System.out.println("===== 💬 PROGRAMME SOURCE 💬 =====");
    //System.out.println(program);
    //System.out.println("==================================");

    // 1) Initialisation
    CharStream cs = CharStreams.fromString(program);
    MiniJajaLexer lexer = new MiniJajaLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    MiniJajaParser parser = new MiniJajaParser(tokens);

    // Créez le Visitor en lui passant la table
    MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();

    // 2) Lancement du parsing pour obtenir l'arbre d'analyse (ParseTree)
    ParseTree tree = parser.classe();

    // 3) Construction de l'AST en appelant visitor.visit(tree)
    AstNode astRoot = visitor.visit(tree);



    // 4) Affichage de l'AST
    System.out.println("\n===== 🌳 ARBRE SYNTAXIQUE ABSTRAIT (AST) 🌳 =====");
    if (astRoot != null) {
      // Utilise la méthode toStringTree() corrigée
      // 4) Affichage de l'AST
    System.out.println(astRoot.toStringTree());
    Walker walker = new Walker(astRoot, stacks);
    //walker.walk();
    //stacks.printStack();

    } else {
      System.out.println("ERREUR: L'AST est null.");
    }
    System.out.println("==============================================");

    System.out.println("\n✅ Test terminé avec succès !");

  }

}