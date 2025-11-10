package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.*;

public class JajaCodeInterpreterVisitor extends JajaCodeParserBaseVisitor<Object> implements Runnable {


    private final Stacks stacks;

    private final Map<Integer, JajaCodeParser.InstrContext> programme = new HashMap<>();

    // Compteur pour les instructions
    private int instructionCounter;
    private boolean running;

    public JajaCodeInterpreterVisitor(Stacks stacks) {
        this.stacks = stacks;
    }

    /**
     * Charge le programme JajaCode à partir de l'arbre syntaxique.
     *
     * @param arbre L'arbre syntaxique du programme JajaCode.
     */
    public void load(JajaCodeParser.ClasseContext arbre) {
        new JajaCodeParserBaseVisitor<Void>() {
            @Override
            public Void visitClasse(JajaCodeParser.ClasseContext ctx) {
                if (ctx.instr() != null) {
                    int adresse = Integer.parseInt(ctx.adresse().getText());
                    programme.put(adresse, ctx.instr());
                    visit(ctx.classe());
                }
                return null;
            }
        }.visit(arbre);
        System.out.println("Interpréteur: Programme chargé, " + programme.size() + " instructions.");
    }

    /**
     * Exécute le programme JajaCode chargé. L'on va avoir un compteur de programme (pc) qui pointe sur l'instruction courante.
     * Tant que le programme est en cours d'exécution, on récupère l'instruction à l'adresse pc,
     * on l'exécute, puis on met à jour pc en fonction de l'instruction exécutée
     */
    @Override
    public void run() {
        instructionCounter = 1;
        running = true;

        System.out.println("Interpréteur: Exécution démarrée à l'adresse 1.");

        while (running) {
            JajaCodeParser.InstrContext instruction = programme.get(instructionCounter);

            if (instruction == null) {
                System.err.println("Error : @ " + instructionCounter + " introuvable !");
                running = false;
                break;
            }

            System.out.println("PC: " + instructionCounter + " → Exécute: " + instruction.getText() + " | Pile: " + stacks);

            visit(instruction);
        }

        System.out.println("--- Exécution Terminée ---");
        System.out.println("Pile finale: " + stacks);
        System.out.println("Mémoire finale: " + stacks);
    }

    /**
     * Visite et exécute une instruction JajaCode.
     *
     * @param ctx Le contexte de l'instruction à exécuter.
     * @return null
     */

    @Override
    public Object visitInstr(JajaCodeParser.InstrContext ctx) {
        if (ctx.INIT() != null) {
            axiomeInit();
        } else if (ctx.PUSH() != null) {
            axiomePush(ctx.valeur());
        } else if (ctx.NEW() != null) {
            axiomeNew(ctx.ident().getText(), ctx.TYPE().getText(), ctx.SORTE().getText());
        } else if (ctx.STORE() != null) {
            axiomeStore(ctx.ident().getText());
        } else if (ctx.SWAP() != null) {
            axiomeSwap();
        } else if (ctx.POP() != null) {
            axiomePop();
        } else if (ctx.JCSTOP() != null) {
            axiomeJcstop();
        } else {
            System.err.println("Instruction non implémentée: " + ctx.getText());
            instructionCounter++;
        }
        return null;
    }

    // Implémentation des axiomes JajaCode

    private void axiomeInit() {
        System.out.println("\t\tAxiome INIT exécuté.");
        instructionCounter++;
    }

    private void axiomePush(JajaCodeParser.ValeurContext valeurCtx) {
        int valeur = Integer.parseInt(valeurCtx.getText());
        stacks.push(new Stacks.Quad("%TMP%", valeur, "%TMP%", "int"));
        System.out.println("\t\tAxiome PUSH exécuté: " + valeur + " poussé sur la pile.");
        instructionCounter++;
    }

    private void axiomeNew(String ident, String type, String kind) {
        Stacks.Quad valeur = stacks.pop();

        if (valeur == null) {
            System.err.println("Erreur dans axiomeNew : pile vide.");
            running = false;
            return;
        }

        switch (kind.toLowerCase()) {
            case "var":
                stacks.declareVar(ident, valeur.value, type);
                break;
            case "cst":
            case "meth":
                stacks.declareCst(ident, valeur.value, type);
                break;
            case "tab":
                if (valeur.value instanceof Integer) {
                    stacks.declareTab(ident, (Integer) valeur.value, type);
                } else {
                    System.err.println("Erreur dans axiomeNew : taille de tableau invalide.");
                    running = false;
                    return;
                }
                break;
            default:
                System.err.println("Erreur dans axiomeNew : type de symbole inconnu: " + kind);
                running = false;
                return;
        }

        System.out.println("\t\tAxiome NEW exécuté: " + ident + " de type " + type + " et sorte " + kind + " créé avec valeur " + valeur.value + ".");
        instructionCounter++;
    }


    /**
     * Axiome STORE : met à jour la valeur d'un symbole existant dans la mémoire.
     * Comment ça marche :
     * 1. On dépile la valeur à stocker.
     * 2. On cherche le symbole dans la mémoire.
     * 3. Si le symbole n'existe pas, on affiche une erreur et on arrête l'interpréteur.
     * 4. Si le symbole existe, on met à jour sa valeur.
     * 5. On incrémente le compteur de programme.
     *
     * @param ident the identifier of the symbol to store
     */
    private void axiomeStore(String ident) {
        Stacks.Quad valeur = stacks.pop();

        if (valeur == null) {
            System.err.println("Erreur fatale dans axiomeStore : pile vide.");
            running = false;
            return;
        }

        // Utiliser AffecterVal pour mettre à jour la valeur
        boolean success = stacks.AffecterVal(ident, valeur.value);

        if (!success) {
            System.err.println("Erreur fatale dans axiomeStore : impossible d'affecter la valeur à '" + ident + "'.");
            running = false;
            return;
        }

        System.out.println("\t\tAxiome STORE exécuté: " + ident + " mis à jour avec valeur " + valeur.value + ".");
        instructionCounter++;
    }

    private void axiomeSwap() {
        stacks.swap();
        System.out.println("\t\tAxiome SWAP exécuté: Les deux éléments du sommet de la pile ont été échangés.");
        instructionCounter++;
    }

    private void axiomePop() {
        Stacks.Quad popped = stacks.pop();
        System.out.println("\t\tAxiome POP exécuté: " + popped + " retiré de la pile.");
        instructionCounter++;
    }

    private void axiomeJcstop() {
        System.out.println("\t\tAxiome JCSTOP exécuté: Arrêt de l'exécution.");
        running = false;
    }

    @Override
    public Object visitValeur(JajaCodeParser.ValeurContext ctx) {
        if (ctx.NOMBRE() != null) {
            return Integer.parseInt(ctx.NOMBRE().getText());
        }
        if (ctx.TRUE() != null) {
            return true;
        }
        if (ctx.FALSE() != null) {
            return false;
        }
        if (ctx.VIDE() != null) {
            return null;
        }
        throw new UnsupportedOperationException("Value not supported: " + ctx.getText());
    }


}