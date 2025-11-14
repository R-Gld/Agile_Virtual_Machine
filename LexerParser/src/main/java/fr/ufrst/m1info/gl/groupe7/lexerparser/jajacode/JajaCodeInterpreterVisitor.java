package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.*;

/**
 * Visiteur et interpréteur pour le langage JajaCode.
 * <p>
 * Cette classe implémente un interpréteur pour le bytecode JajaCode généré par le compilateur MiniJaja.
 * Elle visite l'arbre syntaxique du programme JajaCode et exécute les instructions une par une
 * en utilisant un compteur de programme (PC) et une pile de mémoire.
 * </p>
 *
 * <p><b>Architecture :</b></p>
 * <ul>
 *   <li>Charge le programme JajaCode en mémoire avec {@link #load(JajaCodeParser.ClasseContext)}</li>
 *   <li>Exécute le programme avec {@link #run()}</li>
 *   <li>Utilise un compteur de programme (PC) pour suivre l'instruction courante</li>
 *   <li>Maintient une pile de mémoire via l'objet {@link Stacks}</li>
 * </ul>
 *
 * <p><b>Exemple d'utilisation :</b></p>
 * <pre>{@code
 * Stacks stacks = new Stacks();
 * JajaCodeInterpreterVisitor interpreter = new JajaCodeInterpreterVisitor(stacks);
 * interpreter.load(programmeArbre);
 * interpreter.run();
 * }</pre>
 *
 * @author Groupe 7
 * @version 1.0
 * @see Stacks
 * @see JajaCodeParser
 */
public class JajaCodeInterpreterVisitor extends JajaCodeParserBaseVisitor<Object> implements Runnable {

    /**
     * La pile de mémoire utilisée pour stocker les variables et les valeurs temporaires.
     * Cette pile gère également la table des symboles.
     */
    private final Stacks stacks;

    /**
     * Map contenant toutes les instructions du programme JajaCode indexées par leur adresse.
     * Clé : adresse de l'instruction (Integer)
     * Valeur : contexte de l'instruction (InstrContext)
     */
    private final Map<Integer, JajaCodeParser.InstrContext> programme = new HashMap<>();

    /**
     * Compteur de programme (Program Counter) qui pointe sur l'adresse de l'instruction courante.
     * Initialisé à 1 au début de l'exécution.
     */
    private int instructionCounter;

    /**
     * Indicateur d'état de l'interpréteur.
     * {@code true} si le programme est en cours d'exécution, {@code false} s'il est arrêté.
     */
    private boolean running;

    /**
     * Construit un nouvel interpréteur JajaCode avec une pile de mémoire.
     *
     * @param stacks la pile de mémoire à utiliser pour l'exécution du programme
     */
    public JajaCodeInterpreterVisitor(Stacks stacks) {
        this.stacks = stacks;
    }

    /**
     * Charge le programme JajaCode à partir de l'arbre syntaxique.
     * <p>
     * Cette méthode parcourt l'arbre syntaxique du programme JajaCode et indexe
     * chaque instruction par son adresse dans la map {@link #programme}.
     * Cela permet un accès direct aux instructions pendant l'exécution.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Parcourt récursivement l'arbre syntaxique</li>
     *   <li>Pour chaque instruction, extrait son adresse</li>
     *   <li>Stocke l'instruction dans la map avec son adresse comme clé</li>
     * </ol>
     *
     * @param arbre l'arbre syntaxique du programme JajaCode à charger
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
     * Exécute le programme JajaCode chargé.
     * <p>
     * Cette méthode implémente la boucle principale d'exécution de l'interpréteur.
     * Elle initialise le compteur de programme à 1 et exécute les instructions
     * séquentiellement jusqu'à ce que le programme se termine ou qu'une erreur survienne.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Initialise le compteur de programme ({@link #instructionCounter}) à 1</li>
     *   <li>Active le flag {@link #running}</li>
     *   <li>Tant que {@code running} est vrai :
     *     <ul>
     *       <li>Récupère l'instruction à l'adresse courante</li>
     *       <li>Affiche l'état de la pile et l'instruction</li>
     *       <li>Exécute l'instruction via {@link #visitInstr(JajaCodeParser.InstrContext)}</li>
     *       <li>Le compteur de programme est incrémenté par chaque axiome</li>
     *     </ul>
     *   </li>
     *   <li>Affiche l'état final de la pile et de la mémoire</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Si une instruction n'existe pas à l'adresse courante, affiche une erreur et arrête</li>
     *   <li>Chaque axiome peut arrêter l'exécution en cas d'erreur critique</li>
     * </ul>
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

            System.out.println("PC: " + instructionCounter + " → Exécute: " + instruction.getText() + " | Pile: ");
            stacks.printStack();

            visit(instruction);
        }

        System.out.println("--- Exécution Terminée ---");
        System.out.println("Pile finale:");
        stacks.printStack();
        System.out.println("Mémoire finale: ");
        stacks.printSymbolTable();
    }

    /**
     * Visite et exécute une instruction JajaCode.
     * <p>
     * Cette méthode analyse le type d'instruction et délègue son exécution
     * à l'axiome approprié. C'est le point central de dispatch de l'interpréteur.
     * </p>
     *
     * <p><b>Instructions supportées :</b></p>
     * <ul>
     *   <li>{@code INIT} - Initialisation du programme → {@link #axiomeInit()}</li>
     *   <li>{@code PUSH} - Empiler une valeur → {@link #axiomePush(JajaCodeParser.ValeurContext)}</li>
     *   <li>{@code NEW} - Déclarer un symbole → {@link #axiomeNew(String, String, String)}</li>
     *   <li>{@code STORE} - Stocker une valeur → {@link #axiomeStore(String)}</li>
     *   <li>{@code LOAD} - Charger une valeur → {@link #axiomeLoad(String)}</li>
     *   <li>{@code SWAP} - Échanger → {@link #axiomeSwap()}</li>
     *   <li>{@code POP} - Dépiler → {@link #axiomePop()}</li>
     *   <li>{@code IF} - Saut conditionnel → {@link #axiomeIF(int)}</li>
     *   <li>{@code GOTO} - Saut inconditionnel → {@link #axiomeGOTO(int)}</li>
     *   <li>{@code INC} - Incrémentation → {@link #axiomeINC(String)}</li>
     *   <li>{@code JCSTOP} - Arrêt du programme → {@link #axiomeJcstop()}</li>
     * </ul>
     *
     * @param ctx le contexte de l'instruction à exécuter
     * @return null (la valeur de retour n'est pas utilisée)
     */
    @Override
    public Object visitInstr(JajaCodeParser.InstrContext ctx) {
        if (ctx.INIT() != null) {
            axiomeInit();
        } else if (ctx.PUSH() != null && ctx.valeur() != null) {
            axiomePush(ctx.valeur());
        } else if (ctx.NEW() != null) {
            // L'instruction NEW peut avoir TYPE et SORTE comme tokens ou comme enfants de l'arbre
            String ident = null;
            String type = null;
            String sorte = null;

            if (ctx.ident() != null) {
                ident = ctx.ident().getText();
            }

            if (ctx.TYPE() != null) {
                type = ctx.TYPE().getText();
            }

            if (ctx.SORTE() != null) {
                sorte = ctx.SORTE().getText();
            }

            // Si TYPE ou SORTE sont null, on essaie d'accéder aux enfants de l'arbre
            if (type == null || sorte == null) {
                System.out.println("\t\t[DEBUG] TYPE ou SORTE null, extraction depuis les enfants");
                System.out.println("\t\t[DEBUG] Nombre d'enfants: " + ctx.getChildCount());

                // Parcourir tous les enfants pour trouver TYPE et SORTE
                for (int i = 0; i < ctx.getChildCount(); i++) {
                    String childText = ctx.getChild(i).getText();
                    System.out.println("\t\t[DEBUG] Enfant " + i + ": " + childText);

                    // Détecter le type (INT, BOOLEAN, etc.)
                    if (type == null && (childText.equals("INT") || childText.equals("BOOLEAN") || childText.equals("BOOL") || childText.matches("[A-Z]+"))) {
                        type = childText;
                        System.out.println("\t\t[DEBUG] Type trouvé: " + type);
                    }

                    // Détecter la sorte (VARIABLE, CST, TAB, etc.)
                    if (sorte == null && (childText.equals("VARIABLE") || childText.equals("VAR") || childText.equals("CST") || childText.equals("TAB") || childText.equals("METH"))) {
                        sorte = childText;
                        System.out.println("\t\t[DEBUG] Sorte trouvée: " + sorte);
                    }
                }
            }

            if (ident != null && type != null && sorte != null) {
                System.out.println("\t\t[DEBUG] Exécution de axiomeNew avec: ident=" + ident + ", type=" + type + ", sorte=" + sorte);
                axiomeNew(ident, type, sorte);
            } else {
                System.err.println("Erreur : Instruction NEW incomplète !");
                System.err.println("  ident: " + ident);
                System.err.println("  TYPE: " + type);
                System.err.println("  SORTE: " + sorte);
                System.err.println("  Texte complet: " + ctx.getText());
                System.err.println("  Nombre d'enfants: " + ctx.getChildCount());
                running = false;
            }
        } else if (ctx.STORE() != null && ctx.ident() != null) {
            axiomeStore(ctx.ident().getText());
        } else if (ctx.LOAD() != null && ctx.ident() != null) {
            axiomeLoad(ctx.ident().getText());
        } else if (ctx.SWAP() != null) {
            axiomeSwap();
        } else if (ctx.POP() != null) {
            axiomePop();
        } else if (ctx.IF() != null && ctx.adresse() != null) {
            axiomeIF(Integer.parseInt(ctx.adresse().getText()));
        } else if (ctx.GOTO() != null && ctx.adresse() != null) {
            axiomeGOTO(Integer.parseInt(ctx.adresse().getText()));
        } else if (ctx.INC() != null && ctx.ident() != null) {
            axiomeINC(ctx.ident().getText());
        } else if (ctx.JCSTOP() != null) {
            axiomeJcstop();
        } else {
            // Pour les autres instructions (ADD, SUB, MUL, DIV, NEG, NOT, AND, OR, CMP, SUP, INF),
            // on laisse le visiteur par défaut les gérer via visitOper2, visitOper1, etc.
            System.out.println("\t\t[DEBUG] Délégation à visitChildren pour: " + ctx.getText());
            visitChildren(ctx);
        }
        return null;
    }

    /**
     * Visite un nœud d'opération binaire et exécute l'axiome correspondant.
     * <p>
     * Cette méthode gère les opérations arithmétiques binaires :
     * <ul>
     *   <li>ADD (+)</li>
     *   <li>SUB (-)</li>
     *   <li>MUL (*)</li>
     *   <li>DIV (/)</li>
     * </ul>
     * </p>
     *
     * @param ctx le contexte de l'opération binaire
     * @return null
     */
    @Override
    public Object visitOper2(JajaCodeParser.Oper2Context ctx) {
        if (ctx.ADD() != null) {
            axiomeAdd();
        } else if (ctx.SUB() != null) {
            axiomeSub();
        } else if (ctx.MUL() != null) {
            axiomeMul();
        } else if (ctx.DIV() != null) {
            axiomeDiv();
        } else if (ctx.AND() != null) {
            axiomeAnd();
        } else if (ctx.OR() != null) {
            axiomeOr();
        } else if (ctx.SUP() != null) {
            axiomeSup();
        } else if (ctx.CMP() != null) {
            axiomeCMP();
        } else {
            System.err.println("Opération binaire non implémentée: " + ctx.getText());
            instructionCounter++;
        }
        return null;
    }

    /**
     * Visite un nœud d'opération unaire et exécute l'axiome correspondant.
     * <p>
     * Cette méthode gère les opérations unaires :
     * <ul>
     *   <li>NEG (négation arithmétique : -x)</li>
     *   <li>NOT (négation booléenne : !x)</li>
     * </ul>
     * </p>
     *
     * @param ctx le contexte de l'opération unaire
     * @return null
     */
    @Override
    public Object visitOper1(JajaCodeParser.Oper1Context ctx) {
        if (ctx.NEG() != null) {
            axiomeUnaryMinus();
        } else if (ctx.NOT() != null) {
            axiomeNot();
        } else {
            System.err.println("Opération unaire non implémentée: " + ctx.getText());
            instructionCounter++;
        }
        return null;
    }

    // ========================================================================
    // Implémentation des axiomes JajaCode
    // ========================================================================

    /**
     * Axiome INIT : Initialise le programme JajaCode.
     * <p>
     * Cette instruction marque le début de l'exécution du programme.
     * Elle ne modifie pas l'état de la pile ni de la mémoire.
     * </p>
     *
     * <p><b>Effet :</b></p>
     * <ul>
     *   <li>Incrémente le compteur de programme</li>
     *   <li>Affiche un message de confirmation</li>
     * </ul>
     */
    private void axiomeInit() {
        System.out.println("\t\tAxiome INIT exécuté.");
        instructionCounter++;
    }

    /**
     * Axiome PUSH : Empile une valeur sur la pile.
     * <p>
     * Cette instruction crée un quad temporaire contenant la valeur
     * et le place au sommet de la pile.
     * </p>
     *
     * <p><b>Effet :</b></p>
     * <ul>
     *   <li>Crée un quad temporaire {@code <%TMP%, valeur, %TMP%, type>}</li>
     *   <li>Empile ce quad sur la pile</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ul>
     *
     * @param valeurCtx le contexte contenant la valeur à empiler
     */
    private void axiomePush(JajaCodeParser.ValeurContext valeurCtx) {
        Object valeur = visitValeur(valeurCtx);
        String type;

        if (valeur instanceof Integer) {
            type = "int";
        } else if (valeur instanceof Boolean) {
            type = "bool";
        } else {
            type = "void";
        }

        stacks.push(new Stacks.Quad("%TMP%", valeur, "%TMP%", type));
        System.out.println("\t\tAxiome PUSH exécuté: " + valeur + " poussé sur la pile.");
        instructionCounter++;
    }

    /**
     * Axiome NEW : Déclare un nouveau symbole dans la mémoire.
     * <p>
     * Cette instruction dépile une valeur et crée un symbole (variable, constante,
     * tableau ou méthode) avec cette valeur initiale.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Dépile la valeur initiale du sommet de la pile</li>
     *   <li>Crée un symbole selon le type ({@code kind}) :
     *     <ul>
     *       <li>{@code var} → Variable modifiable</li>
     *       <li>{@code cst} → Constante non modifiable</li>
     *       <li>{@code meth} → Méthode (traitée comme constante)</li>
     *       <li>{@code tab} → Tableau de taille donnée</li>
     *     </ul>
     *   </li>
     *   <li>Incrémente le compteur de programme</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si le type de symbole est inconnu</li>
     *   <li>Arrête l'exécution si la taille du tableau n'est pas un entier</li>
     * </ul>
     *
     * @param ident le nom du symbole à créer
     * @param type  le type du symbole (int, bool, etc.)
     * @param kind  la sorte du symbole (var, cst, tab, meth)
     */
    private void axiomeNew(String ident, String type, String kind) {
        System.out.println("\t\t[DEBUG] axiomeNew appelé: ident=" + ident + ", type=" + type + ", kind=" + kind);

        Stacks.Quad valeur = stacks.pop();

        if (valeur == null) {
            System.err.println("Erreur dans axiomeNew : pile vide.");
            running = false;
            return;
        }

        System.out.println("\t\t[DEBUG] Valeur dépilée: " + valeur.value);

        switch (kind.toLowerCase()) {
            case "var":
            case "variable":
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
     * Axiome STORE : Stocke une valeur dans un symbole existant.
     * <p>
     * Cette instruction dépile une valeur et l'affecte à un symbole existant
     * dans la table des symboles.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Dépile la valeur à stocker</li>
     *   <li>Recherche le symbole dans la mémoire</li>
     *   <li>Met à jour la valeur du symbole via {@link Stacks#AffecterVal(String, Object)}</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si le symbole n'existe pas ou est une constante</li>
     * </ul>
     *
     * @param ident le nom du symbole à mettre à jour
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

    /**
     * Axiome LOAD : Charge la valeur d'un symbole et l'empile.
     * <p>
     * Cette instruction recherche un symbole dans la table des symboles,
     * récupère sa valeur et l'empile sous forme de quad temporaire.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Recherche le symbole par son identifiant</li>
     *   <li>Récupère la valeur du symbole</li>
     *   <li>Crée un quad temporaire avec cette valeur</li>
     *   <li>Empile le quad</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si le symbole n'existe pas</li>
     * </ul>
     *
     * @param ident le nom du symbole dont la valeur doit être chargée
     */
    private void axiomeLoad(String ident) {
        Object valeur = stacks.getValue(ident);

        if (valeur == null && !stacks.getSymbolTable().contains(ident)) {
            System.err.println("Erreur fatale dans axiomeLoad : symbole '" + ident + "' introuvable.");
            running = false;
            return;
        }

        stacks.push(new Stacks.Quad("%TMP%", valeur, "%TMP%", stacks.getDataType(ident)));
        System.out.println("\t\tAxiome LOAD exécuté: " + ident + " = " + valeur + " chargé sur la pile.");
        instructionCounter++;
    }

    /**
     * Axiome SWAP : Échange les deux éléments au sommet de la pile.
     * <p>
     * Cette instruction échange la position des deux quads au sommet de la pile.
     * </p>
     *
     * <p><b>Effet :</b></p>
     * <ul>
     *   <li>Appelle {@link Stacks#swap()}</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ul>
     */
    private void axiomeSwap() {
        stacks.swap();
        System.out.println("\t\tAxiome SWAP exécuté: Les deux éléments du sommet de la pile ont été échangés.");
        instructionCounter++;
    }

    /**
     * Axiome POP : Dépile l'élément au sommet de la pile.
     * <p>
     * Cette instruction retire le quad au sommet de la pile.
     * </p>
     *
     * <p><b>Effet :</b></p>
     * <ul>
     *   <li>Appelle {@link Stacks#pop()}</li>
     *   <li>Affiche l'élément retiré</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ul>
     */
    private void axiomePop() {
        Stacks.Quad popped = stacks.pop();
        System.out.println("\t\tAxiome POP exécuté: " + popped + " retiré de la pile.");
        instructionCounter++;
    }

    /**
     * Axiome ADD : Addition de deux entiers.
     * <p>
     * Cette instruction dépile deux valeurs, les additionne et empile le résultat.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Dépile op2 (deuxième opérande)</li>
     *   <li>Dépile op1 (premier opérande)</li>
     *   <li>Calcule result = op1 + op2</li>
     *   <li>Empile result</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si les opérandes ne sont pas des entiers</li>
     * </ul>
     */
    private void axiomeAdd() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeAdd : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeAdd : opérandes non entiers.");
            running = false;
            return;
        }

        int result = (Integer) op1.value + (Integer) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "int"));

        System.out.println("\t\tAxiome ADD exécuté: " + op1.value + " + " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome SUB : Soustraction de deux entiers.
     * <p>
     * Cette instruction dépile deux valeurs, calcule la différence et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 - op2 et empile le résultat</p>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si les opérandes ne sont pas des entiers</li>
     * </ul>
     */
    private void axiomeSub() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeSub : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeSub : opérandes non entiers.");
            running = false;
            return;
        }

        int result = (Integer) op1.value - (Integer) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "int"));

        System.out.println("\t\tAxiome SUB exécuté: " + op1.value + " - " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome MUL : Multiplication de deux entiers.
     * <p>
     * Cette instruction dépile deux valeurs, les multiplie et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 * op2 et empile le résultat</p>
     */
    private void axiomeMul() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeMul : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeMul : opérandes non entiers.");
            running = false;
            return;
        }

        int result = (Integer) op1.value * (Integer) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "int"));

        System.out.println("\t\tAxiome MUL exécuté: " + op1.value + " * " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome DIV : Division entière de deux entiers.
     * <p>
     * Cette instruction dépile deux valeurs, effectue la division entière et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 / op2 et empile le résultat</p>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si division par zéro</li>
     * </ul>
     */
    private void axiomeDiv() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeDiv : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeDiv : opérandes non entiers.");
            running = false;
            return;
        }

        if ((Integer) op2.value == 0) {
            System.err.println("Erreur dans axiomeDiv : division par zéro.");
            running = false;
            return;
        }

        int result = (Integer) op1.value / (Integer) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "int"));

        System.out.println("\t\tAxiome DIV exécuté: " + op1.value + " / " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome NEG : Négation unaire (opposé d'un entier).
     * <p>
     * Cette instruction dépile un entier, calcule son opposé et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule -op et empile le résultat</p>
     */
    private void axiomeUnaryMinus() {
        Stacks.Quad op = stacks.pop();

        if (op == null) {
            System.err.println("Erreur dans axiomeUnaryMinus : pile vide.");
            running = false;
            return;
        }

        if (!(op.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeUnaryMinus : opérande non entier.");
            running = false;
            return;
        }

        int result = -(Integer) op.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "int"));

        System.out.println("\t\tAxiome UNARY MINUS exécuté: -" + op.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome NOT : Négation booléenne (NON logique).
     * <p>
     * Cette instruction dépile un booléen, calcule sa négation et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule !op et empile le résultat</p>
     */
    private void axiomeNot() {
        Stacks.Quad op = stacks.pop();

        if (op == null) {
            System.err.println("Erreur dans axiomeNot : pile vide.");
            running = false;
            return;
        }

        if (!(op.value instanceof Boolean)) {
            System.err.println("Erreur dans axiomeNot : opérande non booléen.");
            running = false;
            return;
        }

        boolean result = !(Boolean) op.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome NOT exécuté: !" + op.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome AND : ET logique entre deux booléens.
     * <p>
     * Cette instruction dépile deux booléens, effectue un ET logique et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 AND op2 et empile le résultat</p>
     */
    private void axiomeAnd() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeAnd : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Boolean) || !(op2.value instanceof Boolean)) {
            System.err.println("Erreur dans axiomeAnd : opérandes non booléens.");
            running = false;
            return;
        }

        boolean result = (Boolean) op1.value && (Boolean) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome AND exécuté: " + op1.value + " && " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome OR : OU logique entre deux booléens.
     * <p>
     * Cette instruction dépile deux booléens, effectue un OU logique et empile le résultat.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 OR op2 et empile le résultat</p>
     */
    private void axiomeOr() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeOr : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Boolean) || !(op2.value instanceof Boolean)) {
            System.err.println("Erreur dans axiomeOr : opérandes non booléens.");
            running = false;
            return;
        }

        boolean result = (Boolean) op1.value || (Boolean) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome OR exécuté: " + op1.value + " || " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome CMP : Comparaison d'égalité.
     * <p>
     * Cette instruction dépile deux valeurs, les compare et empile le résultat booléen.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 == op2 et empile le résultat</p>
     */
    private void axiomeCMP() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeEq : pile vide.");
            running = false;
            return;
        }

        boolean result = Objects.equals(op1.value, op2.value);
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome EQ exécuté: " + op1.value + " == " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome SUP : Comparaison de supériorité stricte.
     * <p>
     * Cette instruction dépile deux entiers et empile vrai si le premier est strictement supérieur au second.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 &gt; op2 et empile le résultat</p>
     */
    private void axiomeSup() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeSUP : pile vide.");
            running = false;
            return;
        }

        if (!(op1.value instanceof Integer) || !(op2.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeSUP : opérandes non entiers.");
            running = false;
            return;
        }

        boolean result = (Integer) op1.value > (Integer) op2.value;
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome SUP exécuté: " + op1.value + " > " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome CMP : Comparaison d'égalité.
     * <p>
     * Cette instruction dépile deux valeurs, les compare et empile le résultat booléen.
     * </p>
     *
     * <p><b>Effet :</b> Calcule op1 == op2 et empile le résultat</p>
     */
    private void axiomeCmp() {
        Stacks.Quad op2 = stacks.pop();
        Stacks.Quad op1 = stacks.pop();

        if (op1 == null || op2 == null) {
            System.err.println("Erreur dans axiomeCMP : pile vide.");
            running = false;
            return;
        }

        boolean result = Objects.equals(op1.value, op2.value);
        stacks.push(new Stacks.Quad("%TMP%", result, "%TMP%", "bool"));

        System.out.println("\t\tAxiome CMP exécuté: " + op1.value + " == " + op2.value + " = " + result);
        instructionCounter++;
    }

    /**
     * Axiome IF : Saut conditionnel.
     * <p>
     * Cette instruction dépile un booléen et saute à l'adresse donnée si la valeur est vraie.
     * Si la valeur est fausse, l'exécution continue séquentiellement.
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Dépile la condition</li>
     *   <li>Si condition == true (ou != 0) → saute à l'adresse donnée</li>
     *   <li>Si condition == false (ou == 0) → continue à l'instruction suivante</li>
     * </ol>
     *
     * <p><b>Utilisation typique avec while :</b></p>
     * <pre>
     * // while(condition) { ... }
     * [début]:
     *   évaluer condition
     *   not              // Inverser la condition
     *   if([fin])        // Sauter à la fin si (not condition) est vrai
     *   ... corps ...
     *   goto([début])
     * [fin]:
     * </pre>
     *
     * @param adresse l'adresse de saut si la condition est vraie
     */
    private void axiomeIF(int adresse) {
        Stacks.Quad condition = stacks.pop();

        if (condition == null) {
            System.err.println("Erreur dans axiomeIF : pile vide.");
            running = false;
            return;
        }

        boolean conditionValue;
        if (condition.value instanceof Boolean) {
            conditionValue = (Boolean) condition.value;
        } else if (condition.value instanceof Integer) {
            conditionValue = (Integer) condition.value != 0;
        } else {
            System.err.println("Erreur dans axiomeIF : condition invalide.");
            running = false;
            return;
        }

        // IF saute à l'adresse donnée si la condition est VRAIE (sémantique standard JajaCode)
        if (conditionValue) {
            instructionCounter = adresse;
            System.out.println("\t\tAxiome IF exécuté: condition vraie, saut à l'adresse " + adresse + ".");
        } else {
            instructionCounter++;
            System.out.println("\t\tAxiome IF exécuté: condition fausse, poursuite de l'exécution séquentielle.");
        }
    }

    /**
     * Axiome GOTO : Saut inconditionnel.
     * <p>
     * Cette instruction effectue un saut inconditionnel à l'adresse donnée.
     * </p>
     *
     * <p><b>Effet :</b> Le compteur de programme est mis à jour avec l'adresse cible</p>
     *
     * @param adresse l'adresse de destination du saut
     */
    private void axiomeGOTO(int adresse) {
        instructionCounter = adresse;
        System.out.println("\t\tAxiome GOTO exécuté: saut à l'adresse " + adresse + ".");
    }

    /**
     * Axiome INC : Incrémentation d'une variable.
     * <p>
     * Cette instruction dépile une valeur et l'ajoute à la variable spécifiée.
     * Équivalent à : variable = variable + valeur_dépilée
     * </p>
     *
     * <p><b>Fonctionnement :</b></p>
     * <ol>
     *   <li>Dépile la valeur à ajouter</li>
     *   <li>Récupère la valeur actuelle de la variable</li>
     *   <li>Calcule nouvelle_valeur = valeur_actuelle + valeur_dépilée</li>
     *   <li>Met à jour la variable avec la nouvelle valeur</li>
     *   <li>Incrémente le compteur de programme</li>
     * </ol>
     *
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Arrête l'exécution si la pile est vide</li>
     *   <li>Arrête l'exécution si la variable n'existe pas</li>
     *   <li>Arrête l'exécution si les valeurs ne sont pas des entiers</li>
     * </ul>
     *
     * @param ident le nom de la variable à incrémenter
     */
    private void axiomeINC(String ident) {
        Stacks.Quad increment = stacks.pop();

        if (increment == null) {
            System.err.println("Erreur dans axiomeINC : pile vide.");
            running = false;
            return;
        }

        Object currentValue = stacks.getValue(ident);

        if (currentValue == null && !stacks.getSymbolTable().contains(ident)) {
            System.err.println("Erreur dans axiomeINC : symbole '" + ident + "' introuvable.");
            running = false;
            return;
        }

        if (!(currentValue instanceof Integer) || !(increment.value instanceof Integer)) {
            System.err.println("Erreur dans axiomeINC : valeurs non entières.");
            running = false;
            return;
        }

        int newValue = (Integer) currentValue + (Integer) increment.value;
        boolean success = stacks.AffecterVal(ident, newValue);

        if (!success) {
            System.err.println("Erreur dans axiomeINC : impossible d'incrémenter '" + ident + "'.");
            running = false;
            return;
        }

        System.out.println("\t\tAxiome INC exécuté: " + ident + " incrémenté de " + increment.value + " → nouvelle valeur: " + newValue);
        instructionCounter++;
    }

    /**
     * Axiome JCSTOP : Arrête l'exécution du programme.
     * <p>
     * Cette instruction termine l'exécution du programme JajaCode.
     * </p>
     *
     * <p><b>Effet :</b> Met {@link #running} à false pour terminer la boucle d'exécution</p>
     */
    private void axiomeJcstop() {
        System.out.println("\t\tAxiome JCSTOP exécuté: Arrêt de l'exécution.");
        running = false;
    }

    /**
     * Visite un nœud valeur et retourne sa représentation Java.
     * <p>
     * Cette méthode convertit les valeurs littérales du code JajaCode
     * en objets Java correspondants qui seront empilés sur la pile.
     * </p>
     *
     * <p><b>Valeurs supportées :</b></p>
     * <ul>
     *   <li>{@code NOMBRE} → {@link Integer} (ex: 42, -5, 0)</li>
     *   <li>{@code TRUE} → {@link Boolean} true</li>
     *   <li>{@code FALSE} → {@link Boolean} false</li>
     *   <li>{@code VIDE} → null (valeur vide/non initialisée)</li>
     * </ul>
     *
     * <p><b>Exemple :</b></p>
     * <pre>
     * push(42)    → visitValeur retourne Integer(42)
     * push(true)  → visitValeur retourne Boolean(true)
     * push(vide)  → visitValeur retourne null
     * </pre>
     *
     * @param ctx le contexte de la valeur à convertir
     * @return la valeur Java correspondante (Integer, Boolean ou null)
     * @throws UnsupportedOperationException si le type de valeur n'est pas supporté
     */
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