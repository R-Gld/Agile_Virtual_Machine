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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr.*;

public class MiniJajaCompilerVisitor {

    private final JajaCodeBuilder jjcBuilder;
    private final Stack<String> variablesToPop;
    private String currentScope = "global";
    private final Set<String> mainLocalVariables = new HashSet<>();


    public MiniJajaCompilerVisitor(Stacks stacks) {
        this.variablesToPop = new Stack<>();
        this.jjcBuilder = new JajaCodeBuilder();
    }

    public JajaCodeBuilder getJajaCodeBuilder() {
        return jjcBuilder;
    }

    /**
     * Extrait le nom d'un identifiant depuis un AstNode.
     *
     * @param identNode le nœud contenant l'identifiant
     * @return le nom de l'identifiant
     */
    private String extractIdentifierName(AstNode identNode) {
        if (identNode instanceof IdentNode) {
            return ((IdentNode) identNode).getNom();
        }

        String identStr = identNode.toStringTree();
        if (identStr.startsWith("Ident(") && identStr.endsWith(")")) {
            return identStr.substring(6, identStr.length() - 1);
        }
        return identStr;
    }

    /**
     * Détermine le scope approprié pour une variable.
     *
     * @param variableName le nom de la variable
     * @return "main" si la variable est locale au main, "global" sinon
     */
    private String resolveVariableScope(String variableName) {
        if ("main".equals(currentScope) && mainLocalVariables.contains(variableName)) {
            return "main";
        }
        return "global";
    }

    /**
     * Crée un visiteur temporaire qui hérite du contexte actuel.
     *
     * @return un nouveau visiteur avec le scope et les variables locales propagés
     */
    private MiniJajaCompilerVisitor createChildVisitor() {
        MiniJajaCompilerVisitor childVisitor = new MiniJajaCompilerVisitor(null);
        childVisitor.currentScope = this.currentScope;
        childVisitor.mainLocalVariables.addAll(this.mainLocalVariables);
        return childVisitor;
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
            return "INT";
        }

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
        currentScope = "main";

        if (node.getVars() != null) {
            visit(node.getVars());
        }

        if (node.getInstrs() != null) {
            visit(node.getInstrs());
        }

        jjcBuilder.addInstruction(PUSH, 0);

        currentScope = "global";
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
        } else if (instrNode instanceof TantqueNode) {
            visitTantque((TantqueNode) instrNode);
        } else if (instrNode instanceof SommeNode) {
            visitSomme((SommeNode) instrNode);
        }
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

        // Compiler le bloc then
        JajaCodeBuilder originalBuilder = this.jjcBuilder;
        MiniJajaCompilerVisitor thenVisitor = createChildVisitor();

        if (node.getInstructionsNode() != null) {
            thenVisitor.visit(node.getInstructionsNode());
        }
        JajaCodeBuilder thenBuilder = thenVisitor.getJajaCodeBuilder();

        // Compiler le bloc else (si présent)
        JajaCodeBuilder elseBuilder = null;
        boolean hasElse = node.getInstructionsNode2() != null;
        if (hasElse) {
            MiniJajaCompilerVisitor elseVisitor = createChildVisitor();
            elseVisitor.visit(node.getInstructionsNode2());
            elseBuilder = elseVisitor.getJajaCodeBuilder();
        }

        // Calculer les adresses
        int currentAddr = originalBuilder.getCurrentAddress();
        int thenSize = thenBuilder.getInstructionsAsList().size();
        int elseSize = hasElse ? elseBuilder.getInstructionsAsList().size() : 0;

        int elseAddr = currentAddr + 1 + thenSize + (hasElse ? 1 : 0);
        int endAddr = elseAddr + elseSize;

        // Générer les instructions
        originalBuilder.addInstruction(IF, elseAddr);
        originalBuilder.merge(thenBuilder);

        if (hasElse) {
            originalBuilder.addInstruction(GOTO, endAddr);
            originalBuilder.merge(elseBuilder);
        }
    }

    /**
     * Compile une boucle while (tantque) en code JajaCode.
     *
     * <p>Cette méthode génère le code pour une boucle {@code while} en suivant
     * la structure suivante :
     * <pre>
     *   label_debut:
     *   [code pour évaluer la condition]
     *   not                           // inverser car if saute si faux
     *   if(adresse_fin)               // si faux, sortir de la boucle
     *   [code du corps de la boucle]
     *   goto(adresse_debut)           // retour au début
     *   label_fin:
     *   [suite du programme]
     * </pre>
     *
     * <p><b>Exemple de code généré :</b>
     * <p>Pour le code MiniJaja suivant :
     * <pre>
     * while(i &gt; 0) {
     *     i += -1;
     * }
     * </pre>
     * Le code JajaCode généré sera :
     * <pre>
     * load(i@main)        // adresse 8 (début)
     * push(0)
     * sup
     * not
     * if(17)              // adresse de fin
     * push(1)             // corps de la boucle
     * neg
     * inc(i@main)
     * goto(8)             // retour au début
     *                     // adresse 17 (fin)
     * </pre>
     *
     * <p><b>Calcul des adresses :</b>
     * <ul>
     *   <li>{@code adresse_debut = adresse_courante}</li>
     *   <li>{@code adresse_fin = adresse_debut + taille_condition + 2 (not + if) + taille_corps + 1 (goto)}</li>
     * </ul>
     *
     * <p><b>Sémantique :</b>
     * <p>La condition est évaluée à chaque itération. Si elle est vraie (≠ 0),
     * le corps est exécuté et on retourne au début. Si elle est fausse (= 0),
     * on sort de la boucle.
     *
     * <p><b>Note d'implémentation :</b>
     * <p>Un visiteur temporaire est créé pour compiler le corps de la boucle de manière
     * isolée, afin de pouvoir calculer sa taille avant de générer les instructions de saut.
     *
     * @param node le nœud TantqueNode représentant la boucle while à compiler
     * @throws NullPointerException si node est null
     */
    private void visitTantque(TantqueNode node) {
        int loopStartAddr = jjcBuilder.getCurrentAddress();

        // Évaluer la condition et l'inverser
        if (node.getExpressionNode() != null) {
            visitExpression(node.getExpressionNode());
        }
        jjcBuilder.addInstruction(NOT);

        // Compiler le corps de la boucle
        MiniJajaCompilerVisitor bodyVisitor = createChildVisitor();
        if (node.getInstructionsNode() != null) {
            bodyVisitor.visit(node.getInstructionsNode());
        }
        JajaCodeBuilder bodyBuilder = bodyVisitor.getJajaCodeBuilder();

        // Calculer les adresses
        int currentAddr = jjcBuilder.getCurrentAddress();
        int bodySize = bodyBuilder.getInstructionsAsList().size();
        int loopEndAddr = currentAddr + 1 + bodySize + 1;

        // Générer les instructions
        jjcBuilder.addInstruction(IF, loopEndAddr);
        jjcBuilder.merge(bodyBuilder);
        jjcBuilder.addInstruction(GOTO, loopStartAddr);
    }

    /**
     * Compile une instruction de somme (+=) en code JajaCode.
     *
     * <p>Cette méthode génère le code pour une instruction {@code variable += expression}
     * en utilisant l'instruction {@code inc(variable)} qui incrémente la variable
     * avec la valeur au sommet de la pile.</p>
     *
     * <p><b>Exemple de code généré :</b>
     * <p>Pour le code MiniJaja suivant :
     * <pre>
     * y += 2 * x;
     * </pre>
     * Le code JajaCode généré sera :
     * <pre>
     * push(2)
     * load(x@global)
     * mul
     * inc(y@global)
     * </pre>
     *
     * @param node le nœud SommeNode représentant l'instruction += à compiler
     */
    private void visitSomme(SommeNode node) {
        // Évaluer l'expression à ajouter
        if (node.getExpressionNode() != null) {
            visitExpression(node.getExpressionNode());
        }

        // Générer l'instruction INC avec le bon scope
        if (node.getIdent1Node() != null) {
            String ident = extractIdentifierName(node.getIdent1Node());
            String scopeAddress = resolveVariableScope(ident);
            jjcBuilder.addInstruction(INC, ident + "@" + scopeAddress);
        }
    }

    public void visit(AffectationNode node) {
        if (node.getExpression() != null) {
            visitExpression(node.getExpression());
        }

        if (node.getIdent1Node() != null) {
            String ident = extractIdentifierName(node.getIdent1Node());
            String scopeAddress = resolveVariableScope(ident);
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

    public void visit(VarsNode node) {
        if (node == null || node.getVar() == null) return;

        visit(node.getVar());

        if (node.getVars() != null) {
            visit(node.getVars());
        }
    }

    // TODO Corriger la gestion des types si boolean x; -> x est initialisé à 0 au lieu de false et c'est un INT
    // Par contre boolean x = true; -> x est bien un BOOLEAN
    public void visit(VarNode node) {
        Expression vexp = node.getExp() != null ? node.getExp().getVexp() : null;

        // Si on a une valeur d'initialisation, on peut déduire le type réel
        String actualType;
        if (vexp instanceof BoolValueNode) {
            actualType = "BOOLEAN";
        } else if (vexp instanceof NbreNode) {
            actualType = "INT";
        } else {
            actualType = normalizeType(node.getType());
        }

        if (vexp != null) {
            visitExpression(vexp);
        } else {
            if ("BOOLEAN".equals(actualType)) {
                jjcBuilder.addInstruction(PUSH, false);
            } else {
                jjcBuilder.addInstruction(PUSH, 0);
            }
        }

        String ident = node.getIdent().getNom();
        String scopeAddress = currentScope;
        String kind = "VARIABLE";

        jjcBuilder.addInstruction(NEW, ident + "@" + scopeAddress, actualType, kind, 0);
        variablesToPop.push(ident + "@" + scopeAddress);

        if ("main".equals(currentScope)) {
            mainLocalVariables.add(ident);
        }
    }


    public void visit(NbreNode node) {
        jjcBuilder.addInstruction(PUSH, node.value);
    }

    public void visit(BoolValueNode node) {
        jjcBuilder.addInstruction(PUSH, node.value);
    }

    public void visit(IdentNode node) {
        String varName = node.getNom();
        String scopeAddress = resolveVariableScope(varName);
        jjcBuilder.addInstruction(LOAD, varName + "@" + scopeAddress);
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

    private void visitWriteLn() {
        jjcBuilder.addInstruction(WRITELN);
    }

    public void visitWrite() {
        jjcBuilder.addInstruction(WRITE);
    }
}
