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
   
class quicksort{ 
    final int longueur ;
    int tableau[20];
    
    void afficher() {
        int taille = length(tableau);
        int i = 0;
        int a = 0;
        
        while(taille > i) {
           a = tableau[i];
           write(a);
           write(" ");
           i++;
        };
        writeln(" ");
    };
    
    int partition(int deb, int fin) { 
        int compt=deb; 
        int pivot=tableau[deb]; 
        int i=deb+1; 
        int temp; 
    
        while(fin>i || i==fin) { 
            if(pivot>tableau[i]) { 
                compt++; 
                temp=tableau[compt]; 
                tableau[compt]=tableau[i]; 
                tableau[i]=temp; 
            }; 
        i++; 
        }; 
        temp=tableau[compt]; 
        tableau[compt]=tableau[deb]; 
        tableau[deb]=temp; 
        return(compt); 
    }; 
    
    void pause(){
    };
    
    void trirapide(int debut,int fin) { 
        int pivot; 
        if(fin>debut) { 
            pivot=partition(debut,fin); 
            trirapide(debut,pivot-1); 
            trirapide(pivot+1,fin); 
        }; 
    }; 
    
    main {
        longueur = length(tableau);
        tableau[0]=5; 
        tableau[1]=2; 
        tableau[2]=10; 
        tableau[3]=11; 
        tableau[4]=4;
        tableau[5]=52; 
        tableau[6]=13; 
        tableau[7]=12; 
        tableau[8]=5; 
        tableau[9]=1; 
        tableau[10]=62; 
        tableau[11]=32; 
        tableau[12]=14; 	    
        tableau[13]=16; 
        tableau[14]=9; 
        tableau[15]=8; 
        tableau[16]=3; 
        tableau[17]=21; 
        tableau[18]=29; 
        tableau[19]=23; 
        afficher();
        trirapide(0,longueur-1);
        afficher();
        pause();
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
    walker.walk();
    //stacks.printStack();

    } else {
      System.out.println("ERREUR: L'AST est null.");
    }
    System.out.println("==============================================");

    System.out.println("\n✅ Test terminé avec succès !");

  }

}