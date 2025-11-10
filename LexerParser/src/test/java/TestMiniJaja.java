import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.MiniJajaInterpreterVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

public class TestMiniJaja {
  public static void main(String[] args) {
    Stacks stacks = new Stacks();

    String program = """
        class MathOps {
          // === Declarations globales ==
          int x = 12+9-4;

         main{
          x=12;

          }
        }
        """;

    System.out.println("=====  PROGRAMME SOURCE  =====");
    System.out.println(program);
    System.out.println("==================================");

    // 1) Initialisation
    CharStream cs = CharStreams.fromString(program);
    MiniJajaLexer lexer = new MiniJajaLexer(cs);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    MiniJajaParser parser = new MiniJajaParser(tokens);

    // Créez le Visitor en lui passant la table
    MiniJajaInterpreterVisitor visitor = new MiniJajaInterpreterVisitor();

    // 2) Lancement du parsing pour obtenir l'arbre d'analyse (ParseTree)
    ParseTree tree = parser.classe();
    stacks.printStack();
    // 3) Construction de l'AST en appelant visitor.visit(tree)
    AstNode astRoot = visitor.visit(tree);

    // 4) Affichage de l'AST
    System.out.println("\n=====  ARBRE SYNTAXIQUE ABSTRAIT (AST)  =====");
    if (astRoot != null) {
      // Utilise la méthode toStringTree() corrigée
      System.out.println(astRoot.toStringTree());
    } else {
      System.out.println("ERREUR: L'AST est null.");
    }
    System.out.println("==============================================");

    stacks.printStack();

    System.out.println("\n Test terminé avec succès !");
  }

}
