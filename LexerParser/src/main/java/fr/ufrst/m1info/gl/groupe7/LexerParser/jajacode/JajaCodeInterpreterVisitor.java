package fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.jajacode.JajaCodeParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.Memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.Memoire.Symbol;
import fr.ufrst.m1info.gl.groupe7.Memoire.SymbolTable;

import java.util.*;

public class JajaCodeInterpreterVisitor extends JajaCodeParserBaseVisitor<Object> implements Runnable {

//    private final Stack<Object> pile = new Stack<>();
    private final ManageSTForInterp.JJCStack stack = new ManageSTForInterp.JJCStack();

    private final SymbolTable symbolTable;

    private final Map<Integer, JajaCodeParser.InstrContext> programme = new HashMap<>();

    // Compteur pour les instructions
    private int instructionCounter;
    private boolean running;

    public JajaCodeInterpreterVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
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

            System.out.println("PC: " + instructionCounter + " → Exécute: " + instruction.getText() + " | Pile: " + stack);

            visit(instruction);
        }

        System.out.println("--- Exécution Terminée ---");
        System.out.println("Pile finale: " + stack);
        System.out.println("Mémoire finale: " + symbolTable);
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
        stack.push(new ManageSTForInterp.JJCQuad(valeur, ManageSTForInterp.Type.INT));
        System.out.println("\t\tAxiome PUSH exécuté: " + valeur + " poussé sur la pile.");
        instructionCounter++;
    }

    private void axiomeNew(String ident, String type, String kind) {
        ManageSTForInterp.JJCQuad valeur = stack.pop();
        stack.push(valeur); // Counter the pop ^ TODO check if its good.

        switch(ManageSTForInterp.Kind.fromString(kind)) {
            case VAR:
                symbolTable.declareVar(ident, valeur.value, type);
                break;
            case METH:
                symbolTable.declareCst(ident, valeur.value, type);
                break;
            default:
                System.err.println("Erreur dans axiomeNew : type de symbole inconnu.");
                running = false;
                return;
        }

        System.out.println("\t\tAxiome NEW exécuté: " + ident + " de type " + type + " et sorte " + kind + " créé avec valeur " + valeur + ".");
        instructionCounter++;
    }


    /**
     * Axiome STORE : met à jour la valeur d'un symbole existant dans la mémoire.
     * Comment ça marche :
     * 1. On dépile la valeur à stocker.
     * 2. On cherche le symbole dans la mémoire.
     * 3. Si le symbole n'existe pas, on affiche une erreur et on arrête l'interpréteur.
     * 4. Si le symbole existe, on le retire de la mémoire.
     * 5. On crée un nouveau symbole avec la même identité, type et sorte, mais avec la nouvelle valeur.
     * 6. On ajoute le nouveau symbole à la mémoire.
     * 7. On incrémente le compteur de programme.
     *
     * @param ident the identifier of the symbol to store
     */
    private void axiomeStore(String ident) {
        ManageSTForInterp.JJCQuad valeur = stack.pop();

        Symbol oldSymbol = symbolTable.findSymbol(ident);

        if (oldSymbol == null) {
            System.err.println("Erreur fatale dans axiomeStore : symbole '" + ident + "' non trouvé.");
            running = false;
            return;
        }

        symbolTable.assign(ident, valeur.value);

        System.out.println("\t\tAxiome STORE exécuté: " + ident + " mis à jour avec valeur " + valeur + ".");
        instructionCounter++;
    }

    private void axiomeSwap() {
        stack.swap();
        System.out.println("\t\tAxiome SWAP exécuté: Les deux éléments du sommet de la pile ont été échangés.");
        instructionCounter++;
    }

    private void axiomePop() {
        Stacks.Quad popped = stack.pop();
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

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ManageSTForInterp.JJCStack getStack() {
        return stack;
    }

    private static class ManageSTForInterp {

        private enum Kind {
            VAR, METH;

            private static final Map<String, Kind> LOOKUP = new HashMap<>();

            static {
                for (Kind k : Kind.values() ) {
                    LOOKUP.put(k.toString(), k);
                }
            }

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ROOT);
            }

            public static Kind fromString(String kind) {
                if (kind == null) throw new UnsupportedOperationException("Type isn't supported: null");
                Kind k = LOOKUP.get(kind.toLowerCase(Locale.ROOT));
                if (k == null) throw new UnsupportedOperationException("Type isn't supported: " + kind);
                return k;
            }
        }

        private enum Type {
            INT, BOOL, VOID;

            private static final Map<String, Type> LOOKUP = new HashMap<>();
            static {
                for (Type t : values()) {
                    LOOKUP.put(t.toString(), t);
                }
            }

            @Override
            public String toString() {
                return name().toLowerCase(Locale.ROOT);
            }

            public static Type fromString(String type) {
                if (type == null) throw new UnsupportedOperationException("Type isn't supported: null");
                Type t = LOOKUP.get(type.toLowerCase(Locale.ROOT));
                if (t == null) throw new UnsupportedOperationException("Type isn't supported: " + type);
                return t;
            }
        }

        public static class JJCQuad extends Stacks.Quad {
            public JJCQuad(Object value, Type type) {
                super("%TMP%", value, "%TMP%", type.toString());
            }

            @Override
            public int hashCode() {
                return value.hashCode() * 31 + type.hashCode();
            }

            @Override
            public String toString() {
                return "<" + value + ", " + type + ">";
            }
        }

        public static class JJCStack extends Stacks {

            public JJCStack() {
                super();
            }

            @Override
            public void push(Quad q) {
                super.stack.push(q);
                System.err.println("Pushed: " + q);
            }

            @Override
            public JJCQuad pop() {
                if (!stack.isEmpty()) {
                    JJCQuad q = (JJCQuad) stack.pop();
                    System.out.println("Popped: " + q);
                    return q;
                } else {
                    System.out.println("Stack is empty. Nothing to pop!");
                    return null;
                }
            }

            @Override
            public void swap() {
                if (stack.size() >= 2) {
                    JJCQuad q1 = (JJCQuad) stack.pop();
                    JJCQuad q2 = (JJCQuad) stack.pop();
                    stack.push(q1);
                    stack.push(q2);
                    System.out.println("Swapped top elements: " + q1.ident + " and " + q2.ident);
                } else {
                    System.out.println("Cannot swap: not enough elements in the stack.");
                }
            }

            @Override
            public JJCQuad getTop() {
                return stack.isEmpty() ? null : (JJCQuad) stack.peek();
            }

            @Override
            public void declareCst(String ident, Object value, String type) {
                JJCQuad q = new JJCQuad(value, Type.fromString(type));
                push(q);
            }
            @Override
            public void declareTab(String ident, int size, String type) {
                throw new UnsupportedOperationException("Cannot declare an array in JJCStack.");
            }
            @Override
            public void declareVar(String ident, Object value, String type) {
                JJCQuad q = new JJCQuad(value, Type.fromString(type));
                push(q);
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder("Stack(size: " + stack.size() + ")[top -> ");
                stack.elements().asIterator().forEachRemaining(e -> sb.append(e).append(", "));
                sb.append("]");
                return sb.toString();
            }

            public boolean isEmpty(){
                return stack.isEmpty();
            }
        }
    }

}