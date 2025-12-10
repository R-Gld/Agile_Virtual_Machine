package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaLexer;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode; // Assurez-vous d'importer AstNode
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.walker.Debug;
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
        class ConstTest {
          final int GLOBAL_CST = 100;
          final boolean FLAG = true;
          int x = 0;
          
          int useConst(int n) {
            final int LOCAL_CST = 50;
            final int DERIVED = LOCAL_CST + n;
            int result;
            result = GLOBAL_CST + LOCAL_CST + DERIVED + n;
            return result;
          };
          
          int multiConst(int a) {
            final int C1 = 10;
            final int C2 = 20;
            final int C3 = C1 + C2;
            final int C4 = C3 * 2;
            int res;
            res = a + C1 + C2 + C3 + C4;
            return res;
          };
          
          boolean constBool(boolean input) {
            final boolean TRUE_CST = true;
            final boolean FALSE_CST = false;
            boolean result;
            if (TRUE_CST && input) {
              result = TRUE_CST;
            } else {
              result = FALSE_CST;
            }
            return result;
          };
          
          int constInExpr(int val) {
            final int A = 5;
            final int B = 10;
            final int C = 15;
            int r;
            r = val * A + val * B - val / C + A * B * C;
            return r;
          };
          
          int constInCondition(int n) {
            final int THRESHOLD = 50;
            final int LOW = 10;
            final int HIGH = 100;
            int result;
            if (n > THRESHOLD) {
              result = HIGH;
            } else {
              if (n > LOW) {
                result = THRESHOLD;
              } else {
                result = LOW;
              }
            }
            return result;
          };
          
          int constInLoop(int count) {
            final int STEP = 2;
            final int MAX = 20;
            int sum;
            int i;
            sum = 0;
            i = 0;
            while (count > i) {
              sum = sum + STEP;
              i = i + 1;
            }
            return sum + MAX;
          };
          
          main {
            final int MAIN_CST = 42;
            final int ANOTHER = MAIN_CST + GLOBAL_CST;
            final boolean MAIN_FLAG = true;
            int r1;
            int r2;
            int r3;
            int r4;
            int r5;
            int r6;
            boolean b1;
            
            x = GLOBAL_CST;
            
            r1 = useConst(5);
            r2 = multiConst(MAIN_CST);
            b1 = constBool(MAIN_FLAG);
            r3 = constInExpr(MAIN_CST);
            r4 = constInCondition(ANOTHER);
            r5 = constInLoop(10);
            
           r6 = GLOBAL_CST + MAIN_CST + ANOTHER + r1 + r2 + r3 + r4 + r5;
            if (FLAG && MAIN_FLAG) {
             r6 = r6 + 1;
           }
            writeln(r1);
            writeln(r2);
            writeln(r3);
            writeln(r4);
            writeln(r5);
            writeln(r6);
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
    Debug debug = new Debug(Debug.Mode.DISABLED);
    debug.addBreakPoint(60); // Exemple de breakpoint
    System.out.println(astRoot.toStringTree());
    Walker walker = new Walker(astRoot, stacks, debug);
    walker.walk();
    //stacks.printStack();

    } else {
      System.out.println("ERREUR: L'AST est null.");
    }
    System.out.println("==============================================");

    System.out.println("\n✅ Test terminé avec succès !");

  }
  
  /**
   * Main method to run the debugger interactively from the terminal.
   * Run with: mvn exec:java -pl LexerParser -Dexec.mainClass="fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.DebugeParsingETC"
   */
  public static void main(String[] args) {
    DebugeParsingETC test = new DebugeParsingETC();
    test.DebugeParsing();
  }

}