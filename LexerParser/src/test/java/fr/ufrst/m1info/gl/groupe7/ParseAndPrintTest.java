package fr.ufrst.m1info.gl.groupe7;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja.ast.AstNode; // Assurez-vous d'importer AstNode

/**
 * Test qui parse un programme, construit l'AST via le Visitor,
 * affiche l'AST et le dump de la table des symboles.
 */
public class ParseAndPrintTest {

  @Test
  public void testParseAndPrintASTAndSymbolTable() {
    Stacks stacks = new Stacks();
    SymbolTable SymbolTable = new SymbolTable();
    String program = """
        class MathOps {
          // === Declarations globales ==
          int x = 0;
         main{
          x=12;

          }
        }
        """;

    System.out.println("===== 💬 PROGRAMME SOURCE 💬 =====");
    System.out.println(program);
    System.out.println("==================================");

    // 1) Initialisation
    CharStream cs = CharStreams.fromString(program);
    MiniJajaLexer lexer = new MiniJajaLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    MiniJajaParser parser = new MiniJajaParser(tokens);

    // Créez le Visitor en lui passant la table
    MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor(stacks);

    // 2) Lancement du parsing pour obtenir l'arbre d'analyse (ParseTree)
    ParseTree tree = parser.classe();

    // 3) Construction de l'AST en appelant visitor.visit(tree)
    AstNode astRoot = visitor.visit(tree);
    System.out.println("stack");
    stacks.printStack();
    System.out.println("SymbolTable");
    stacks.printSymbolTable();
    System.out.println("after");

    // 4) Affichage de l'AST
    System.out.println("\n===== 🌳 ARBRE SYNTAXIQUE ABSTRAIT (AST) 🌳 =====");
    if (astRoot != null) {
      // Utilise la méthode toStringTree() corrigée
      System.out.println(astRoot.toStringTree());
    } else {
      System.out.println("ERREUR: L'AST est null.");
    }
    System.out.println("==============================================");

    System.out.println("\n✅ Test terminé avec succès !");

  }

}