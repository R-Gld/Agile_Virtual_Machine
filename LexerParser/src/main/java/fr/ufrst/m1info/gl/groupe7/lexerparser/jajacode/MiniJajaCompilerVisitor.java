package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.DiagnosticCollector;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireLnNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.JajaCodeInstr.*;

public class MiniJajaCompilerVisitor {

    private final JajaCodeBuilder jjcBuilder;
    private final Stack<String> variablesToPop;
    private final DiagnosticCollector collector;
    private String currentScope = "global";
    private final Set<String> mainLocalVariables = new HashSet<>();
    private final Set<String> currentScopeVariables = new HashSet<>();


    // TODO why does stacks param is unused here ? Lucas, Ahmed, Léo ?
    // TODO UP ??
    public MiniJajaCompilerVisitor(Stacks stacks, DiagnosticCollector collector) {
        this.variablesToPop = new Stack<>();
        this.jjcBuilder = new JajaCodeBuilder();
        this.collector = collector;
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
        if (identNode instanceof IdentNode identNodeTyped) {
            return identNodeTyped.getNom();
        }

        String identStr = identNode.toStringTree();
        if (identStr.startsWith("Ident(") && identStr.endsWith(")")) {
            return identStr.substring(6, identStr.length() - 1);
        }
        return identStr;
    }

    /**
     * Détermine le scope approprié pour une variable.
     * Cherche d'abord dans le scope actuel (méthode ou main), puis dans global.
     *
     * @param variableName le nom de la variable
     * @return le scope approprié (signature de méthode, "main" ou "global")
     */
    private String resolveVariableScope(String variableName) {
        // D'abord chercher dans le scope actuel (peut être une signature de méthode comme "test@int")
        if (currentScopeVariables.contains(variableName)) {
            return currentScope;
        }

        // Ensuite chercher dans le main
        if ("main".equals(currentScope) && mainLocalVariables.contains(variableName)) {
            return "main";
        }

        // Par défaut, scope global
        return "global";
    }

    /**
     * Crée un visiteur temporaire qui hérite du contexte actuel.
     *
     * @return un nouveau visiteur avec le scope et les variables locales propagés
     */
    private MiniJajaCompilerVisitor createChildVisitor() {
        MiniJajaCompilerVisitor childVisitor = new MiniJajaCompilerVisitor(null, collector);
        childVisitor.currentScope = this.currentScope;
        childVisitor.mainLocalVariables.addAll(this.mainLocalVariables);
        childVisitor.currentScopeVariables.addAll(this.currentScopeVariables);
        return childVisitor;
    }

    /**
     * Compile une classe selon la règle [cclasse] :
     * n ⊢ classe(ident(i), dss, mma) ⇒ {init ⊕G (pdss ⊕ pmma ⊕ prdss) ⊕D pop ⊕D jcstop, ...}
     */
    public void visit(ClasseNode node) {
        jjcBuilder.addInstruction(INIT);

        // pdss : Traiter les déclarations globales
        if (node.getDeclarations() != null) {
            visit(node.getDeclarations());
        }

        // pmma : Traiter le main (sans le retrait des variables locales ici)
        if (node.getMethodeMain() != null) {
            visit((MainNode) node.getMethodeMain());
        }

        // prdss : Retrait des déclarations globales (swap/pop pour chaque)
        while (!variablesToPop.isEmpty()) {
            variablesToPop.pop();
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        // pop : Retrait du 0 du main
        jjcBuilder.addInstruction(POP);

        jjcBuilder.addInstruction(JCSTOP);
    }

    /**
     * Compile le main selon la règle [cmain] :
     * n ⊢ main(dvs, iss) ⇒ {pdvs ⊕ piss ⊕D push(0) ⊕ prdvs, ndvs + niss + nrdvs + 1}
     * <p>
     * Note: Le main ne déclare pas de variables dans cet exemple simplifié,
     * donc prdvs est vide.
     */
    public void visit(MainNode node) {
        currentScope = "main";

        // Sauvegarder le nombre de variables globales avant le main
        int globalVarCount = variablesToPop.size();

        // pdvs : Compiler les déclarations de variables locales
        if (node.getVars() != null) {
            visit(node.getVars());
        }

        // Calculer le nombre de variables locales du main
        int localVarCount = variablesToPop.size() - globalVarCount;

        // piss : Compiler les instructions
        if (node.getInstrs() != null) {
            visit(node.getInstrs());
        }

        // push(0) : Valeur de retour du main (méthode void)
        jjcBuilder.addInstruction(PUSH, 0);

        // prdvs : Retrait des variables locales du main (swap/pop pour chaque)
        for (int i = 0; i < localVarCount; i++) {
            variablesToPop.pop();
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

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
        if (instrNode instanceof AffectationNode affectationNode) {
            visit(affectationNode);
        } else if (instrNode instanceof SiNode siNode) {
            visitSi(siNode);
        } else if (instrNode instanceof TantqueNode tantqueNode) {
            visitTantque(tantqueNode);
        } else if (instrNode instanceof SommeNode sommeNode) {
            visitSomme(sommeNode);
        } else if (instrNode instanceof EcrireLnNode ecrireLnNode) {
            visitEcrireLn(ecrireLnNode);
        } else if (instrNode instanceof EcrireNode ecrireNode) {
            visitEcrire(ecrireNode);
        } else if (instrNode instanceof RetourNode retourNode) {
            visitRetour(retourNode);
        } else if (instrNode instanceof AppelINode appelINode) {
            visitAppelI(appelINode);
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

        // Générer l'instruction INC
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

        if (node.getDecl() instanceof VarNode varNode) {
            visit(varNode);
        } else if (node.getDecl() instanceof MethodeNode methodeNode) {
            visit(methodeNode);
        }

        if (node.getDecls() != null) {
            visit(node.getDecls());
        }
    }

    public void visit(VarsNode node) {
        if (node == null || node.getVar() == null) return;

        visit(((VarNode) node.getVar()));

        if (node.getVars() != null) {
            visit(node.getVars());
        }
    }

    public void visit(VarNode node) {
        Expression vexp = node.getExp() != null ? node.getExp().getVexp() : null;

        if (vexp != null) {
            visitExpression(vexp);
        } else {
            // Si pas d'expression d'initialisation, on push une valeur par défaut selon le type
            if ("BOOLEEN".equals(node.getType().name())) {
                jjcBuilder.addInstruction(PUSH, false);
            } else if ("ENTIER".equals(node.getType().name())) {
                jjcBuilder.addInstruction(PUSH, 0);
            }
        }

        String ident = node.getIdent().getNom();
        String scopeAddress = currentScope;
        String kind = "var";

        jjcBuilder.addInstruction(NEW, ident + "@" + scopeAddress, typeToJajaCode(node.getType()), kind, 0);
        variablesToPop.push(ident + "@" + scopeAddress);

        if ("main".equals(currentScope)) {
            mainLocalVariables.add(ident);
        } else if (!"global".equals(currentScope)) {
            // On est dans une méthode
            currentScopeVariables.add(ident);
        }
    }

    /**
     * Compile une déclaration de méthode en code JajaCode.
     * <p>
     * Selon les règles [cméthode] et [cméthodeRien] :
     * - [cméthode] pour les méthodes avec retour :
     * n ⊢ méthode(t, ident(i), ens, dvs, iss) ⇒
     * {jcnil ⊕D push(n+3) ⊕D new(i, t, meth, 0) ⊕D goto(...)
     * ⊕ pens ⊕ pdvs ⊕ piss ⊕ prdvs ⊕D swap ⊕D return, ...}
     * <p>
     * Note: Le retrait des paramètres (pens) se fait côté appelant (après invoke),
     * pas dans le corps de la méthode. Seules les variables locales (dvs) sont retirées ici.
     *
     * @param node le nœud MethodeNode à compiler
     */
    public void visit(MethodeNode node) {
        String methodName = node.getIdent().getNom();
        String methodType = typeToJajaCode(node.getTypeMeth());
        String methodSignature = methodName + "@" + methodType;
        boolean isVoidMethod = node.getTypeMeth() == Type.VOID;

        // Sauvegarder le scope actuel
        String previousScope = currentScope;
        currentScope = methodSignature;

        // Compiler les entêtes (paramètres) - pens
        MiniJajaCompilerVisitor methodBodyVisitor = new MiniJajaCompilerVisitor(null, collector);
        methodBodyVisitor.currentScope = methodSignature;

        if (node.getEntetes() != null) {
            visitEntetesReverse(node.getEntetes(), methodBodyVisitor, methodSignature);
        }

        // Compiler les variables locales - pdvs
        int varCountBefore = methodBodyVisitor.variablesToPop.size();
        if (node.getVars() != null) {
            methodBodyVisitor.visit(node.getVars());
        }
        int localVarCount = methodBodyVisitor.variablesToPop.size() - varCountBefore;

        // Compiler les instructions - piss
        if (node.getInstrs() != null) {
            methodBodyVisitor.visit(node.getInstrs());
        }

        JajaCodeBuilder methodBody = methodBodyVisitor.getJajaCodeBuilder();
        int bodySize = methodBody.getInstructionsAsList().size();

        // Calculer le nombre d'instructions pour le retrait des variables locales uniquement
        // Les paramètres sont retirés côté appelant
        int retraitVarsCount = localVarCount * 2;

        // Pour les méthodes void, on ajoute push(0) avant le retrait
        int push0Count = isVoidMethod ? 1 : 0;

        // Calculer les adresses
        int currentAddr = jjcBuilder.getCurrentAddress();
        int methodStartAddr = currentAddr + 3; // après push + new + goto

        // L'adresse de fin est après : corps + push(0)? + retrait vars + swap + return
        int methodEndAddr = methodStartAddr + bodySize + push0Count + retraitVarsCount + 2;

        // Générer le code de déclaration de la méthode
        jjcBuilder.addInstruction(PUSH, methodStartAddr);
        jjcBuilder.addInstruction(NEW, methodName, methodType, "meth", 0);
        jjcBuilder.addInstruction(GOTO, methodEndAddr);

        // Ajouter la méthode à la liste des déclarations à retirer selon [crméthode]
        variablesToPop.push(methodName);

        // Insérer le corps de la méthode (pens + pdvs + piss)
        jjcBuilder.merge(methodBody);

        // Pour les méthodes void : push(0) avant le retrait
        if (isVoidMethod) {
            jjcBuilder.addInstruction(PUSH, 0);
        }

        // prdvs : Retrait des variables locales uniquement (pas les paramètres)
        for (int i = 0; i < localVarCount; i++) {
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        // ⊕D swap ⊕D return
        jjcBuilder.addInstruction(SWAP);
        jjcBuilder.addInstruction(RETURN);

        // Restaurer le scope
        currentScope = previousScope;
    }

    /**
     * Compte le nombre de paramètres dans une structure EntetesNode récursive.
     */
    private int countParams(EntetesNode entetes) {
        if (entetes == null || entetes.getEntete() == null) {
            return 0;
        }
        return 1 + countParams(entetes.getEntetes());
    }

    /**
     * Visite les entêtes dans l'ordre inverse pour générer les instructions NEW
     * avec les bonnes profondeurs (depth).
     * <p>
     * Selon [centête] : n ⊢ entête(t, ident(i)) ⇒ {new(i, t, var, k), 1}
     */
    private void visitEntetesReverse(EntetesNode entetes, MiniJajaCompilerVisitor visitor, String methodSignature) {

        if (entetes == null || entetes.getEntete() == null) {
            return;
        }

        // D'abord traiter le reste de la liste
        if (entetes.getEntetes() != null) {
            visitEntetesReverse(entetes.getEntetes(), visitor, methodSignature);
        }

        // Ensuite traiter l'entête actuelle
        EnteteNode entete = entetes.getEntete();
        String paramName = entete.getIdent().getNom();
        String paramType = typeToJajaCode(entete.getType());

        // Calculer la profondeur : les paramètres sont numérotés de 1 à n
        int currentDepth = countParams(entetes);

        visitor.jjcBuilder.addInstruction(NEW, paramName + "@" + methodSignature, paramType, "var", currentDepth);


        // Enregistrer le paramètre dans le scope actuel du visiteur
        visitor.currentScopeVariables.add(paramName);
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
        if (expression instanceof NbreNode nbreNode) {
            visit(nbreNode);
        } else if (expression instanceof BoolValueNode boolValueNode) {
            visit(boolValueNode);
        } else if (expression instanceof IdentNode identNode) {
            visit(identNode);
        } else if (expression instanceof AppelENode appelENode) {
            visitAppelE(appelENode);
        } else if (expression instanceof PlusNode plusNode) {
            visitPlus(plusNode);
        } else if (expression instanceof UnaryMinusNode unaryMinusNode) {
            visitUnaryMinus(unaryMinusNode);
        } else if (expression instanceof MinusNode minusNode) {
            visitMoins(minusNode);
        } else if (expression instanceof AndNode andNode) {
            visitAnd(andNode);
        } else if (expression instanceof OrNode orNode) {
            visitOr(orNode);
        } else if (expression instanceof NotNode notNode) {
            visitNot(notNode);
        } else if (expression instanceof MultiplicationNode multiplicationNode) {
            visitMultiplication(multiplicationNode);
        } else if (expression instanceof DivisionNode divisionNode) {
            visitDivision(divisionNode);
        } else if (expression instanceof GreaterThanNode greaterThanNode) {
            visitGreaterThan(greaterThanNode);
        } else if (expression instanceof EqualsNode equalsNode) {
            visitEquals(equalsNode);
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

    /**
     * Compile une instruction writeln en code JajaCode.
     * Évalue l'expression ou la chaîne à afficher, puis génère l'instruction WRITELN.
     *
     * @param node le nœud EcrireLnNode représentant l'instruction writeln à compiler
     */
    private void visitEcrireLn(EcrireLnNode node) {
        Object ident1Node = node.getIdent1Node();

        if (ident1Node instanceof Expression expression) {
            visitExpression(expression);
        } else if (ident1Node instanceof String str) {
            // Ajouter des guillemets autour de la chaîne pour le JajaCode
            jjcBuilder.addInstruction(PUSH, "\"" + str + "\"");
        }

        jjcBuilder.addInstruction(WRITELN);
    }

    /**
     * Compile une instruction write en code JajaCode.
     * Évalue l'expression ou la chaîne à afficher, puis génère l'instruction WRITE.
     *
     * @param node le nœud EcrireNode représentant l'instruction write à compiler
     */
    private void visitEcrire(EcrireNode node) {
        Object ident1Node = node.getIdent1Node();

        if (ident1Node instanceof Expression expression) {
            visitExpression(expression);
        } else if (ident1Node instanceof String str) {
            // Ajouter des guillemets autour de la chaîne pour le JajaCode
            jjcBuilder.addInstruction(PUSH, "\"" + str + "\"");
        }

        jjcBuilder.addInstruction(WRITE);
    }

    /**
     * Compile une instruction de retour selon la règle [cretour] :
     * n ⊢ retour(e) ⇒ {pe, ne}
     * <p>
     * Note: Le return compile simplement l'expression. Le swap/return final
     * est géré par visit(MethodeNode) après le retrait des variables locales.
     */
    private void visitRetour(RetourNode node) {
        if (node.getExp() != null) {
            visitExpression(node.getExp());
        }
    }

    /**
     * Compile un appel de méthode en instruction selon la règle [cappelI] :
     * n ⊢ appelI(ident(i), lexp) ⇒ {plexp ⊕D invoke(i) ⊕ prlexp ⊕D pop, nlexp + nrlexp + 2}
     * <p>
     * prlexp selon [crlexp] : n ⊢retrait listexp(e, lexp) ⇒ {swap ⊕G (pop ⊕G prlexp), nrlexp + 2}
     */
    private void visitAppelI(AppelINode node) {
        String methodName = node.getIdent().getNom();

        // Compter le nombre d'arguments
        int argCount = countListExp(node.getListExp());

        // plexp : Compiler les arguments (listexp)
        if (node.getListExp() != null) {
            visitListExp(node.getListExp());
        }

        // ⊕D invoke(i) - utiliser juste le nom de la méthode
        jjcBuilder.addInstruction(INVOKE, methodName);

        // prlexp : Retrait des arguments selon [crlexp]
        // Pour chaque argument : swap ⊕G pop
        for (int i = 0; i < argCount; i++) {
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        // ⊕D pop : On retire le résultat (inutile pour une instruction)
        jjcBuilder.addInstruction(POP);
    }

    /**
     * Compile un appel de méthode en expression selon la règle [cappelE] :
     * n ⊢ appelE(ident(i), lexp) ⇒ {plexp ⊕D invoke(i) ⊕ prlexp, nlexp + nrlexp + 1}
     * <p>
     * prlexp selon [crlexp] : n ⊢retrait listexp(e, lexp) ⇒ {swap ⊕G (pop ⊕G prlexp), nrlexp + 2}
     */
    private void visitAppelE(AppelENode node) {
        String methodName = node.getIdent().getNom();

        // Compter le nombre d'arguments
        int argCount = 0;
        if (node.getExp() instanceof ListExpNode listExp) {
            argCount = countListExp(listExp);
        }

        // plexp : Compiler les arguments (listexp)
        if (node.getExp() != null) {
            visitListExp((ListExpNode) node.getExp());
        }

        // ⊕D invoke(i) - utiliser juste le nom de la méthode
        jjcBuilder.addInstruction(INVOKE, methodName);

        // prlexp : Retrait des arguments selon [crlexp]
        // Pour chaque argument : swap ⊕G pop
        for (int i = 0; i < argCount; i++) {
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }
        // Note: On ne fait PAS pop du résultat car c'est une expression (le résultat reste sur la pile)
    }

    /**
     * Compte le nombre d'expressions dans une ListExpNode.
     */
    private int countListExp(ListExpNode node) {
        if (node == null || node.getExp() == null) {
            return 0;
        }
        return 1 + countListExp(node.getListExp());
    }

    /**
     * Compile une liste d'expressions selon la règle [clistexp] :
     * n ⊢ listexp(e, lexp) ⇒ {plexp ⊕ pe, ne + nlexp}
     * <p>
     * Note: Le reste de la liste (plexp) est compilé AVANT l'expression courante (pe).
     */
    private void visitListExp(ListExpNode node) {
        if (node == null) return;

        // Si la liste est vide (exnil)
        if (node.getExp() == null && node.getListExp() == null) {
            return;
        }

        // D'abord compiler le reste de la liste (plexp)
        if (node.getListExp() != null) {
            visitListExp(node.getListExp());
        }

        // Ensuite compiler l'expression courante (pe)
        if (node.getExp() != null) {
            visitExpression(node.getExp());
        }
    }

    private String typeToJajaCode(Type type) {
        return switch (type) {
            case ENTIER -> "int";
            case BOOLEEN -> "boolean";
            case VOID -> "void";
            default -> throw new IllegalArgumentException("Type non supporté pour JajaCode: " + type);
        };
    }
}
