package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.jajacode.JajaCodeParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.exceptions.JajaCodeRuntimeException;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node.*; // Importez vos axiomes
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JajaCodeInterpreterVisitor extends JajaCodeParserBaseVisitor<Object> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JajaCodeInterpreterVisitor.class);

    // L'état de la machine est maintenant encapsulé ici
    private final MachineContext context;

    // Le registre des commandes (Pattern Command)
    private final Map<String, JajaAxiome> axiomes = new HashMap<>();

    // Le programme (Map adresse -> instruction)
    private final Map<Integer, JajaCodeParser.InstrContext> programme = new HashMap<>();

    public JajaCodeInterpreterVisitor(Stacks stacks) {
        // 1. Initialisation du contexte
        this.context = new MachineContext(stacks);

        // 2. Enregistrement des Axiomes
        initAxiomes();
    }

    private void initAxiomes() {
        // --- Instructions de base ---
        axiomes.put("init", new InitAxiome());
        axiomes.put("jcstop", new JCStopAxiome());

        // --- Mémoire et Pile ---
        axiomes.put("push", new PushAxiome());
        axiomes.put("pop", new PopAxiome());
        axiomes.put("swap", new SwapAxiome());
        axiomes.put("new", new NewAxiome());
        axiomes.put("store", new StoreAxiome());
        axiomes.put("load", new LoadAxiome());
        axiomes.put("invoke", new InvokeAxiome());
        axiomes.put("return", new ReturnAxiome());

        // --- Contrôle de flux ---
        axiomes.put("if", new IfAxiome());
        axiomes.put("goto", new GotoAxiome());

        // --- Arithmétique et Logique ---
        axiomes.put("add", new AddAxiome());
        axiomes.put("sub", new SubAxiome());
        axiomes.put("mul", new MulAxiome());
        axiomes.put("div", new DivAxiome());
        axiomes.put("inc", new IncAxiome());
        axiomes.put("sup", new SupAxiome());
        axiomes.put("cmp", new CmpAxiome());
        axiomes.put("neg", new NegAxiome());
        axiomes.put("not", new NotAxiome());
        axiomes.put("or", new OrAxiome());
        axiomes.put("and", new AndAxiome());

        // --- Entrées / Sorties ---
        axiomes.put("write", new WriteAxiome());
        axiomes.put("writeln", new WriteLnAxiome());

        // --- Opérations sur Tableaux ---
        axiomes.put("newarray", new NewarrayAxiome());
        axiomes.put("aload", new AloadAxiome());
        axiomes.put("astore", new AstoreAxiome());
        axiomes.put("ainc", new AincAxiome());
        axiomes.put("length", new LengthAxiome());
    }

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
    }

    @Override
    public void run() {
        // Reset du contexte
        context.setInstructionCounter(1);

        logger.debug("Interpréteur: Exécution démarrée à l'adresse 1.");

        while (context.isRunning()) {
            int pc = context.getInstructionCounter();
            JajaCodeParser.InstrContext instruction = programme.get(pc);

            if (instruction == null) {
                logger.error("Error : @ {} introuvable !", pc);
                context.stop();
                break;
            }

            logger.debug("PC: {} -> {}", pc, instruction.getText());

            // Le visiteur va dispatcher vers le bon axiome
            visit(instruction);
        }

        logger.info("--- Exécution Terminée ---");
        context.getStacks().printStack();
        context.getStacks().printSymbolTable();
    }

    @Override
    public Object visitInstr(JajaCodeParser.InstrContext ctx) {

        // 1. Instructions sans arguments
        if (ctx.INIT() != null) dispatch("init", null);
        else if (ctx.SWAP() != null) dispatch("swap", null);
        else if (ctx.POP() != null) dispatch("pop", null);
        else if (ctx.JCSTOP() != null) dispatch("jcstop", null);
        else if (ctx.WRITE() != null) dispatch("write", null);
        else if (ctx.WRITELN() != null) dispatch("writeln", null);
        else if (ctx.RETURN() != null) dispatch("return", null);

            // 2. Instructions avec VALEUR
        else if (ctx.PUSH() != null) {
            // On extrait le texte brut ("5", "true", "x")
            String valText = ctx.valeur().getText();
            // Cas spécial pour les chaînes avec guillemets si nécessaire
            if (ctx.valeur().STRING() != null) {
                valText = ctx.valeur().STRING().getText().replace("\"", "");
            }
            dispatch("push", valText);
        }

        // 3. Instructions avec IDENTIFIANT
        else if (ctx.ident() != null) {
            String ident = ctx.ident().getText();
            if (ctx.STORE() != null) dispatch("store", ident);
            else if (ctx.LOAD() != null) dispatch("load", ident);
            else if (ctx.INC() != null) dispatch("inc", ident);
            else if (ctx.INVOKE() != null) dispatch("invoke", ident);
                // NEW est spécial (4 args)
            else if (ctx.NEW() != null) handleNew(ctx);
                // Opérations sur tableaux
            else if (ctx.ALOAD() != null) dispatch("aload", ident);
            else if (ctx.ASTORE() != null) dispatch("astore", ident);
            else if (ctx.AINC() != null) dispatch("ainc", ident);
            else if (ctx.LENGTH() != null) dispatch("length", ident);
                // NEWARRAY est spécial (2 args: ident, type)
            else if (ctx.NEWARRAY() != null) handleNewarray(ctx);
        }

        // 4. Instructions avec ADRESSE
        else if (ctx.adresse() != null) {
            String addr = ctx.adresse().getText();
            if (ctx.IF() != null) dispatch("if", addr);
            else if (ctx.GOTO() != null) dispatch("goto", addr);
        }

        // 5. Opérations (Déléguées aux sous-visiteurs)
        else if (ctx.oper() != null) {
            visitChildren(ctx);
        }

        return null;
    }

    // Helper pour exécuter un axiome
    private void dispatch(String command, String arg) {
        JajaAxiome axiome = axiomes.get(command);
        if (axiome != null) {
            try {
                axiome.execute(context, arg);
            } catch (JajaCodeRuntimeException e) {
                logger.error("\nERREUR D'EXÉCUTION JAJACODE:");
                logger.error(e.getMessage());
                logger.error("");

                context.stop();

                throw e;
            }
        } else {
            logger.error("Axiome non implémenté : {}", command);
            context.incrementPC(); // Pour éviter boucle infinie
        }
    }

    // Gestion spécifique pour NEW (concaténation des args)
    private void handleNew(JajaCodeParser.InstrContext ctx) {
        String ident = ctx.ident().getText();
        String type = (ctx.TYPE() != null) ? ctx.TYPE().getText() : "void";
        String sorte = (ctx.SORTE() != null) ? ctx.SORTE().getText() : "var";
        String depth = (ctx.adresse() != null) ? ctx.adresse().getText() : "0";

        logger.debug("\t\t[DEBUG handleNew] ident={}, type={}, sorte={}, depth={}", ident, type, sorte, depth);

        // On pack les arguments pour l'interface générique (ident,type,sorte,depth)
        String packedArgs = ident + "," + type + "," + sorte + "," + depth;
        dispatch("new", packedArgs);
    }

    // Gestion spécifique pour NEWARRAY (concaténation des args: ident,type)
    private void handleNewarray(JajaCodeParser.InstrContext ctx) {
        String ident = ctx.ident().getText();
        String type = (ctx.TYPE() != null) ? ctx.TYPE().getText() : "int";

        logger.debug("\t\t[DEBUG handleNewarray] ident={}, type={}", ident, type);

        // On pack les arguments pour l'interface générique (ident,type)
        String packedArgs = ident + "," + type;
        dispatch("newarray", packedArgs);
    }

    @Override
    public Object visitOper2(JajaCodeParser.Oper2Context ctx) {
        if (ctx.ADD() != null) dispatch("add", null);
        else if (ctx.SUB() != null) dispatch("sub", null);
        else if (ctx.MUL() != null) dispatch("mul", null);
        else if (ctx.DIV() != null) dispatch("div", null);
        else if (ctx.AND() != null) dispatch("and", null);
        else if (ctx.OR() != null) dispatch("or", null);
        else if (ctx.SUP() != null) dispatch("sup", null);
        else if (ctx.CMP() != null) dispatch("cmp", null);
        return null;
    }

    @Override
    public Object visitOper1(JajaCodeParser.Oper1Context ctx) {
        if (ctx.NEG() != null) dispatch("neg", null);
        else if (ctx.NOT() != null) dispatch("not", null);
        return null;
    }
}