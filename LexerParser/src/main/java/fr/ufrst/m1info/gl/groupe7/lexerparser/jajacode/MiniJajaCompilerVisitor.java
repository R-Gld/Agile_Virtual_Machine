package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.Stack;

import static fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr.*;

public class MiniJajaCompilerVisitor {

    private final JajaCodeBuilder jjcBuilder;
    private final Stack<String> variablesToPop;


    public MiniJajaCompilerVisitor(Stacks stacks) {
        this.variablesToPop = new Stack<>();
        this.jjcBuilder = new JajaCodeBuilder();
    }

    public JajaCodeBuilder getJajaCodeBuilder() {
        return jjcBuilder;
    }

    /**
     * Normalise un type MiniJaja en type JajaCode.
     * Convertit "boolean" ou "bool" en "BOOLEAN", "int" en "INT", etc.
     *
     * @param miniJajaType le type en MiniJaja (peut être "bool", "boolean", "int", etc.)
     * @return le type normalisé en JajaCode (BOOLEAN, INT, etc.)
     */
    private String normalizeType(String miniJajaType) {
        if (miniJajaType == null || miniJajaType.trim().isEmpty()) {
            System.out.println("Coucou je suis dans normalizeType avec un type null ou vide, je retourne INT par défaut.");
            return "INT";
        }

        System.out.println("Coucou je suis dans normalizeType avec le type : " + miniJajaType);

        String lowerType = miniJajaType.trim().toLowerCase();
        return switch (lowerType) {
            case "bool", "boolean" -> "BOOLEAN";
            case "int", "integer" -> "INT";
            default -> miniJajaType.trim().toUpperCase();
        };
    }

    public void visit(ClasseNode node) {
        jjcBuilder.addInstruction(INIT);

        // Traiter les déclarations
        if (node.getDeclarations() != null) {
            visit(node.getDeclarations());
        }

        // Traiter le main
        if (node.getMethodeMain() != null) {
            visit((MainNode) node.getMethodeMain());
        }

        // Dépiler les variables déclarées
        while (!variablesToPop.isEmpty()) {
            variablesToPop.pop();
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        jjcBuilder.addInstruction(POP);
        jjcBuilder.addInstruction(JCSTOP);
    }

    public void visit(MainNode node) {
        if (node.getInstrs() != null) {
            visit(node.getInstrs());
        }
        jjcBuilder.addInstruction(PUSH, 0);
    }

    public void visit(InstructionsNode node) {
        if (node == null || node.getInstructionNode() == null) return;

        visit(node.getInstructionNode());

        if (node.getInstructions() != null) {
            visit(node.getInstructions());
        }
    }

    public void visit(InstructionNode instrNode) {
        if (instrNode instanceof AffectationNode) {
            visit((AffectationNode) instrNode);
        } else if (instrNode instanceof SiNode) {
            visitSi((SiNode) instrNode);
        }
        // TODO: Ajouter d'autres types d'instructions au fur et à mesure
    }

    /**
     * Compile une instruction conditionnelle (if/else) en code JajaCode.
     *
     * <p>Cette méthode génère le code pour une instruction {@code if} ou {@code if/else} en suivant
     * la structure suivante :
     * <pre>
     *   [code pour évaluer la condition]
     *   if(adresse_else)              // saute au else si la condition est fausse (0)
     *   [code du bloc then]
     *   goto(adresse_fin)             // saute après le else (seulement si else présent)
     *   [code du bloc else]           // à l'adresse_else
     *   [suite du programme]          // à l'adresse_fin
     * </pre>
     *
     * <p><b>Exemple de code généré :</b>
     * <p>Pour le code MiniJaja suivant :
     * <pre>
     * if(12 &gt; 1) {
     *     x = true;
     * } else {
     *     x = false;
     * }
     * </pre>
     * Le code JajaCode généré sera :
     * <pre>
     * push(12)
     * push(1)
     * sup
     * if(11)                  // adresse du else
     * push(true)              // bloc then
     * store(x@global)
     * goto(13)                // adresse de fin
     * push(false)             // bloc else (adresse 11)
     * store(x@global)
     * [suite...]              // adresse 13
     * </pre>
     *
     * <p><b>Calcul des adresses :</b>
     * <ul>
     *   <li>{@code adresse_else = adresse_courante + 1 + taille_bloc_then + (1 si else présent, 0 sinon)}</li>
     *   <li>{@code adresse_fin = adresse_else + taille_bloc_else}</li>
     * </ul>
     *
     * <p><b>Sémantique de l'instruction {@code if(adresse)} :</b>
     * <p>L'instruction {@code if(adresse)} dépile une valeur de la pile :
     * <ul>
     *   <li>Si la valeur est {@code 0} (faux), saute à {@code adresse}</li>
     *   <li>Si la valeur est différente de {@code 0} (vrai), continue à l'instruction suivante</li>
     * </ul>
     *
     * <p><b>Note d'implémentation :</b>
     * <p>Cette méthode crée des visiteurs temporaires pour compiler les blocs then et else de manière
     * isolée, afin de pouvoir calculer leurs tailles avant de générer les instructions de saut.
     * Les builders temporaires sont ensuite fusionnés avec le builder principal dans le bon ordre.
     *
     * @param node le nœud SiNode représentant l'instruction if/else à compiler
     * @throws NullPointerException si node est null
     */
    private void visitSi(SiNode node) {
        // Évaluer la condition
        if (node.getExpressionNode() != null) {
            visitExpression(node.getExpressionNode());
        }

        // Sauvegarder le builder courant et créer un nouveau pour le bloc then pour le GOTO, c'est un checkpoint en gros
        JajaCodeBuilder originalBuilder = this.jjcBuilder;
        MiniJajaCompilerVisitor thenVisitor = new MiniJajaCompilerVisitor(null);

        if (node.getInstructionsNode() != null) {
            thenVisitor.visit(node.getInstructionsNode());
        }
        JajaCodeBuilder thenBuilder = thenVisitor.getJajaCodeBuilder();

        // Créer un builder pour le bloc else (si présent)
        JajaCodeBuilder elseBuilder = null;
        boolean hasElse = node.getInstructionsNode2() != null;
        if (hasElse) {
            MiniJajaCompilerVisitor elseVisitor = new MiniJajaCompilerVisitor(null);
            elseVisitor.visit(node.getInstructionsNode2());
            elseBuilder = elseVisitor.getJajaCodeBuilder();
        }

        // Calculer les adresses
        int currentAddr = originalBuilder.getCurrentAddress();
        int thenSize = thenBuilder.getInstructionsAsList().size();
        int elseSize = hasElse ? elseBuilder.getInstructionsAsList().size() : 0;

        // L'adresse du else est après : if(addr) + code_then + goto(addr)
        int elseAddr = currentAddr + 1 + thenSize + (hasElse ? 1 : 0);
        // L'adresse de fin est après le code else
        int endAddr = elseAddr + elseSize;

        // Générer l'instruction if(label_else)
        originalBuilder.addInstruction(IF, elseAddr);

        // Ajouter le code du bloc then
        originalBuilder.merge(thenBuilder);

        // Si il y a un else, ajouter goto(label_end) puis le code du else
        if (hasElse) {
            originalBuilder.addInstruction(GOTO, endAddr);
            originalBuilder.merge(elseBuilder);
        }
    }


    public void visit(AffectationNode node) {

        if (node.getExpression() != null) {
            visitExpression(node.getExpression());
        }

        if (node.getIdent1Node() != null) {
            String ident;
            if (node.getIdent1Node() instanceof IdentNode) {
                ident = ((IdentNode) node.getIdent1Node()).getNom();
            } else {
                String identStr = node.getIdent1Node().toStringTree();
                if (identStr.startsWith("Ident(") && identStr.endsWith(")")) {
                    ident = identStr.substring(6, identStr.length() - 1);
                } else {
                    ident = identStr;
                }
            }

            String scopeAddress = "global";
            jjcBuilder.addInstruction(STORE, ident + "@" + scopeAddress);
        }
    }

    public void visit(DeclsNode node) {
        if (node == null || node.getDecl() == null) return;

        visit(node.getDecl());

        if (node.getDecls() != null) {
            visit(node.getDecls());
        }
    }

    // TODO Corriger la gestion des types si boolean x; -> x est initialisé à 0 au lieu de false et c'est un INT
    // Par contre boolean x = true; -> x est bien un BOOLEAN
    public void visit(VarNode node) {
        Expression vexp = node.getExp() != null ? node.getExp().getVexp() : null;
        System.out.println("Vexp dans visit VarNode : " + vexp);

        // Utiliser le type déclaré du nœud et le normaliser
        String rawType = node.getType();
        System.out.println("DEBUG: node.getType() retourne : '" + rawType + "' pour la variable " + node.getIdent().getNom());

        // Si on a une valeur d'initialisation, on peut déduire le type réel
        String actualType;
        if (vexp instanceof BoolValueNode) {
            actualType = "BOOLEAN";
            System.out.println("DEBUG: Type déduit depuis BoolValueNode : BOOLEAN");
        } else if (vexp instanceof NbreNode) {
            actualType = "INT";
            System.out.println("DEBUG: Type déduit depuis NbreNode : INT");
        } else {
            // Sinon, utiliser le type déclaré et le normaliser
            actualType = normalizeType(rawType);
            System.out.println("DEBUG: Type normalisé depuis node.getType() : " + actualType);
        }

        System.out.println("DEBUG: Type final : '" + actualType + "'");

        // Gérer la valeur d'initialisation
        if (vexp != null) {
            visitExpression(vexp);
        } else {
            // Valeur par défaut selon le type
            if ("BOOLEAN".equals(actualType)) {
                System.out.println("DEBUG: Génération de PUSH false pour type BOOLEAN");
                jjcBuilder.addInstruction(PUSH, false);
            } else {
                System.out.println("DEBUG: Génération de PUSH 0 pour type " + actualType);
                jjcBuilder.addInstruction(PUSH, 0);
            }
        }

        String ident = node.getIdent().getNom();
        String scopeAddress = "global";
        String kind = "VARIABLE";

        System.out.println("DEBUG: Génération de NEW avec type : " + actualType);
        jjcBuilder.addInstruction(NEW, ident + "@" + scopeAddress, actualType, kind, 0);
        variablesToPop.push(ident + "@" + scopeAddress);
    }


    public void visit(NbreNode node) {
        jjcBuilder.addInstruction(PUSH, node.value);
    }

    public void visit(BoolValueNode node) {
        jjcBuilder.addInstruction(PUSH, node.value);
    }

    public void visit(IdentNode node) {
        String scopeAddress = "global";
        jjcBuilder.addInstruction(LOAD, node.getNom() + "@" + scopeAddress);
    }

    private void visitExpression(AstNode expression) {
        if (expression instanceof NbreNode) {
            visit((NbreNode) expression);
        } else if (expression instanceof BoolValueNode) {
            visit((BoolValueNode) expression);
        } else if (expression instanceof IdentNode) {
            visit((IdentNode) expression);
        } else if (expression instanceof PlusNode) {
            visitPlus((PlusNode) expression);
        } else if (expression instanceof UnaryMinusNode) {
            visitUnaryMinus((UnaryMinusNode) expression);
        } else if (expression instanceof MinusNode) {
            visitMoins((MinusNode) expression);
        } else if (expression instanceof AndNode) {
            visitAnd((AndNode) expression);
        } else if (expression instanceof OrNode) {
            visitOr((OrNode) expression);
        } else if (expression instanceof NotNode) {
            visitNot((NotNode) expression);
        } else if (expression instanceof MultiplicationNode) {
            visitMultiplication((MultiplicationNode) expression);
        } else if (expression instanceof DivisionNode) {
            visitDivision((DivisionNode) expression);
        } else if (expression instanceof GreaterThanNode) {
            visitGreaterThan((GreaterThanNode) expression);
        } else if (expression instanceof EqualsNode) {
            visitEquals((EqualsNode) expression);
        }
    }

    private void visitPlus(PlusNode node) {
        visitExpression(node.getExp2());
        visitExpression(node.getTerme());
        jjcBuilder.addInstruction(ADD);
    }

    private void visitMoins(MinusNode node) {
        visitExpression(node.getExp2());
        visitExpression(node.getTerme());
        jjcBuilder.addInstruction(SUB);
    }

    private void visitUnaryMinus(UnaryMinusNode node) {
        visitExpression(node.getTerme());
        jjcBuilder.addInstruction(NEG);
    }

    private void visitDivision(DivisionNode node) {
        visitExpression(node.getTerme());
        visitExpression(node.getFact());
        jjcBuilder.addInstruction(DIV);
    }

    private void visitMultiplication(MultiplicationNode node) {
        visitExpression(node.getTerme());
        visitExpression(node.getFact());
        jjcBuilder.addInstruction(MUL);
    }

    private void visitAnd(AndNode node) {
        visitExpression(node.getExp());
        visitExpression(node.getExp1());
        jjcBuilder.addInstruction(AND);
    }

    private void visitOr(OrNode node) {
        visitExpression(node.getExp());
        visitExpression(node.getExp1());
        jjcBuilder.addInstruction(OR);
    }

    private void visitNot(NotNode node) {
        visitExpression(node.getExp());
        jjcBuilder.addInstruction(NOT);
    }

    private void visitGreaterThan(GreaterThanNode node) {
        visitExpression(node.getExp1());
        visitExpression(node.getExp2());
        jjcBuilder.addInstruction(SUP);
    }

    private void visitEquals(EqualsNode node) {
        visitExpression(node.getExp1());
        visitExpression(node.getExp2());
        jjcBuilder.addInstruction(CMP);
    }
}
