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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireLnNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
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
    private final Set<String> globalVariables = new HashSet<>();
    private final Set<String> mainLocalVariables = new HashSet<>();
    private final Set<String> currentScopeVariables = new HashSet<>();


    public MiniJajaCompilerVisitor(DiagnosticCollector collector) {
        this.variablesToPop = new Stack<>();
        this.jjcBuilder = new JajaCodeBuilder();
        this.collector = collector;
    }

    public JajaCodeBuilder getJajaCodeBuilder() {
        return jjcBuilder;
    }

    /**
     * Extracts the identifier name from an AstNode.
     *
     * @param identNode the node containing the identifier
     * @return the identifier name
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
     * Determines the appropriate scope for a variable.
     * Searches first in the current scope (method or main), then in global.
     *
     * @param variableName the variable name
     * @return the appropriate scope (method signature, "main" or "global")
     */
    private String resolveVariableScope(String variableName) {
        // First, search in the current scope (can be a method signature like "test@int")
        if (currentScopeVariables.contains(variableName)) {
            return currentScope;
        }

        // Then search in main
        if ("main".equals(currentScope) && mainLocalVariables.contains(variableName)) {
            return "main";
        }

        return "global";
    }

    /**
     * Creates a temporary visitor that inherits the current context.
     *
     * @return a new visitor with the current scope and local variables propagated
     */
    private MiniJajaCompilerVisitor createChildVisitor() {
        MiniJajaCompilerVisitor childVisitor = new MiniJajaCompilerVisitor(collector);
        childVisitor.currentScope = this.currentScope;
        childVisitor.globalVariables.addAll(this.globalVariables);
        childVisitor.mainLocalVariables.addAll(this.mainLocalVariables);
        childVisitor.currentScopeVariables.addAll(this.currentScopeVariables);
        return childVisitor;
    }

    /**
     * Compiles a class according to rule [cclasse]:
     * n ⊢ classe(ident(i), dss, mma) ⇒ {init ⊕G (pdss ⊕ pmma ⊕ prdss) ⊕D pop ⊕D jcstop, ...}
     */
    public void visit(ClasseNode node) {
        jjcBuilder.addInstruction(INIT);

        // pdss: process global declarations
        if (node.getDeclarations() != null) {
            visit(node.getDeclarations());
        }

        // pmma: process the main (without removing local variables here)
        if (node.getMethodeMain() != null) {
            visit((MainNode) node.getMethodeMain());
        }

        // prdss: removal of global declarations (swap/pop for each)
        while (!variablesToPop.isEmpty()) {
            variablesToPop.pop();
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        // pop: remove the 0 from main
        jjcBuilder.addInstruction(POP);

        jjcBuilder.addInstruction(JCSTOP);
    }

    /**
     * Compiles the main according to rule [cmain]:
     * n ⊢ main(dvs, iss) ⇒ {pdvs ⊕ piss ⊕D push(0) ⊕ prdvs, ndvs + niss + nrdvs + 1}
     * <p>
     * Note: The main does not declare variables in this simplified example,
     * so prdvs is empty.
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
        } else if (instrNode instanceof IncrementNode incrementNode) {
            visitIncrement(incrementNode);
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
     * Compiles a conditional instruction (if/else) into JajaCode.
     *
     * <p>This method generates the code for an {@code if} or {@code if/else} instruction following
     * this structure:
     * <pre>
     *   [code to evaluate the condition]
     *   if(else_address)              // jumps to else if the condition is false (0)
     *   [then block code]
     *   goto(end_address)             // jumps after the else (only if else is present)
     *   [else block code]           // at else_address
     *   [rest of the program]          // at end_address
     * </pre>
     *
     * <p><b>Example of generated code:</b>
     * <p>For the following MiniJaja code:
     * <pre>
     * if(12 > 1) {
     *     x = true;
     * } else {
     *     x = false;
     * }
     * </pre>
     * The generated JajaCode will be:
     * <pre>
     * push(12)
     * push(1)
     * sup
     * if(11)                  // else address
     * push(true)              // then block
     * store(x@global)
     * goto(13)                // end address
     * push(false)             // else block (address 11)
     * store(x@global)
     * [suite...]              // address 13
     * </pre>
     *
     * <p><b>Address calculation:</b>
     * <ul>
     *   <li>{@code else_address = current_address + 1 + then_block_size + (1 if else is present, 0 otherwise)}</li>
     *   <li>{@code end_address = else_address + else_block_size}</li>
     * </ul>
     *
     * <p><b>Semantics of the {@code if(address)} instruction:</b>
     * <p>The {@code if(address)} instruction pops a value from the stack:
     * <ul>
     *   <li>If the value is {@code 0} (false), it jumps to {@code address}</li>
     *   <li>If the value is different from {@code 0} (true), it continues to the next instruction</li>
     * </ul>
     *
     * <p><b>Implementation note:</b>
     * <p>This method creates temporary visitors to compile the then and else blocks in isolation,
     * in order to calculate their sizes before generating the jump instructions.
     * The temporary builders are then merged with the main builder in the correct order.
     *
     * @param node the SiNode representing the if/else instruction to compile
     * @throws NullPointerException if node is null
     */
    private void visitSi(SiNode node) {
        // Rule [csi]: Compile if-then-else statement
        // Structure when no else: condition, NOT, IF(afterThen), [then]
        // Structure with else:    condition, NOT, IF(elseStart), [then], GOTO(end), [else]
        //
        // Logic:
        // - If condition is TRUE:  NOT(TRUE)=FALSE, IF does not jump, execute then
        // - If condition is FALSE: NOT(FALSE)=TRUE, IF jumps to else (or after then)

        // Evaluate the condition
        if (node.getExpressionNode() != null) {
            visitExpression(node.getExpressionNode());
        }

        // Compile the then block in a temporary builder to calculate its size
        MiniJajaCompilerVisitor thenVisitor = createChildVisitor();
        if (node.getInstructionsNode() != null) {
            thenVisitor.visit(node.getInstructionsNode());
        }
        int thenSize = thenVisitor.getJajaCodeBuilder().getInstructionsAsList().size();

        // Compile the else block (if present) in a temporary builder
        int elseSize = 0;
        boolean hasElse = node.getInstructionsNode2() != null;
        if (hasElse) {
            MiniJajaCompilerVisitor elseVisitor = createChildVisitor();
            elseVisitor.visit(node.getInstructionsNode2());
            elseSize = elseVisitor.getJajaCodeBuilder().getInstructionsAsList().size();
        }

        // Get the current address AFTER the condition (before NOT)
        int addrAfterCondition = jjcBuilder.getCurrentAddress();

        if (hasElse) {
            // With else: condition, NOT, IF(elseStart), [then], GOTO(end), [else]
            // Calculate addresses:
            // - NOT is at addrAfterCondition
            // - IF is at addrAfterCondition + 1
            // - then starts at addrAfterCondition + 2
            // - GOTO is at addrAfterCondition + 2 + thenSize
            // - else starts at addrAfterCondition + 2 + thenSize + 1
            // - end is at addrAfterCondition + 2 + thenSize + 1 + elseSize
            int thenStartAddr = addrAfterCondition + 2;
            int gotoAddr = thenStartAddr + thenSize;
            int elseStartAddr = gotoAddr + 1;
            int endAddr = elseStartAddr + elseSize;

            // Generate: NOT, IF(elseStart)
            jjcBuilder.addInstruction(NOT);
            jjcBuilder.addInstruction(IF, elseStartAddr);

            // Compile the THEN block
            if (node.getInstructionsNode() != null) {
                visit(node.getInstructionsNode());
            }

            // Generate GOTO(end)
            jjcBuilder.addInstruction(GOTO, endAddr);

            // Compile the ELSE block
            visit(node.getInstructionsNode2());
        } else {
            // No else: condition, NOT, IF(afterThen), [then]
            // Calculate addresses:
            // - NOT is at addrAfterCondition
            // - IF is at addrAfterCondition + 1
            // - then starts at addrAfterCondition + 2
            // - afterThen is at addrAfterCondition + 2 + thenSize
            int thenStartAddr = addrAfterCondition + 2;
            int afterThenAddr = thenStartAddr + thenSize;

            // Generate: NOT, IF(afterThen)
            jjcBuilder.addInstruction(NOT);
            jjcBuilder.addInstruction(IF, afterThenAddr);

            // Compile the THEN block
            if (node.getInstructionsNode() != null) {
                visit(node.getInstructionsNode());
            }
        }
    }

    /**
     * Compiles a while loop (tantque) into JajaCode.
     *
     * <p>This method generates the code for a {@code while} loop following
     * this structure:
     * <pre>
     *   start_label:
     *   [code to evaluate the condition]
     *   not                           // invert because if jumps if false
     *   if(end_address)               // if false, exit the loop
     *   [loop body code]
     *   goto(start_address)           // return to the beginning
     *   end_label:
     *   [rest of the program]
     * </pre>
     *
     * <p><b>Example of generated code:</b>
     * <p>For the following MiniJaja code:
     * <pre>
     * while(i > 0) {
     *     i += -1;
     * }
     * </pre>
     * The generated JajaCode will be:
     * <pre>
     * load(i@main)        // address 8 (start)
     * push(0)
     * sup
     * not
     * if(17)              // end address
     * push(1)             // loop body
     * neg
     * inc(i@main)
     * goto(8)             // return to the beginning
     *                     // address 17 (end)
     * </pre>
     *
     * <p><b>Address calculation:</b>
     * <ul>
     *   <li>{@code start_address = current_address}</li>
     *   <li>{@code end_address = start_address + condition_size + 2 (not + if) + body_size + 1 (goto)}</li>
     * </ul>
     *
     * <p><b>Semantics:</b>
     * <p>The condition is evaluated at each iteration. If it is true (≠ 0),
     * the body is executed and we return to the beginning. If it is false (= 0),
     * we exit the loop.
     *
     * <p><b>Implementation note:</b>
     * <p>A temporary visitor is created to compile the loop body in isolation,
     * in order to calculate its size before generating the jump instructions.
     *
     * @param node the TantqueNode representing the while loop to compile
     * @throws NullPointerException if node is null
     */
    private void visitTantque(TantqueNode node) {
        int loopStartAddr = jjcBuilder.getCurrentAddress();

        // Evaluate the condition and invert it
        if (node.getExpressionNode() != null) {
            visitExpression(node.getExpressionNode());
        }
        jjcBuilder.addInstruction(NOT);

        // Compile the loop body in a temporary visitor to calculate the size
        MiniJajaCompilerVisitor bodyVisitor = createChildVisitor();
        if (node.getInstructionsNode() != null) {
            bodyVisitor.visit(node.getInstructionsNode());
        }
        int bodySize = bodyVisitor.getJajaCodeBuilder().getInstructionsAsList().size();

        // Calculate addresses
        int currentAddr = jjcBuilder.getCurrentAddress();
        int loopEndAddr = currentAddr + 1 + bodySize + 1; // +1 for IF, +1 for GOTO

        // Generate IF
        jjcBuilder.addInstruction(IF, loopEndAddr);

        // Compile the body directly in the main builder
        if (node.getInstructionsNode() != null) {
            visit(node.getInstructionsNode());
        }

        // Generate GOTO to return to the beginning
        jjcBuilder.addInstruction(GOTO, loopStartAddr);
    }

    /**
     * Compiles a sum instruction (+=) into JajaCode.
     *
     * <p>This method generates the code for a {@code variable += expression} instruction
     * using the {@code inc(variable)} instruction, which increments the variable
     * with the value at the top of the stack.</p>
     *
     * <p><b>Example of generated code:</b>
     * <p>For the following MiniJaja code:
     * <pre>
     * y += 2 * x;
     * </pre>
     * The generated JajaCode will be:
     * <pre>
     * push(2)
     * load(x@global)
     * mul
     * inc(y@global)
     * </pre>
     *
     * @param node the SommeNode representing the += instruction to compile
     */
    private void visitSomme(SommeNode node) {
        AstNode target = node.getIdent1Node();

        if (target instanceof IdentNode identNode) {
            // Simple variable: compile increment, then inc
            if (node.getExpressionNode() != null) {
                visitExpression(node.getExpressionNode());
            }

            String ident = extractIdentifierName(identNode);
            String scopeAddress = resolveVariableScope(ident);
            jjcBuilder.addInstruction(INC, ident + "@" + scopeAddress);

        } else if (target instanceof TabNode tabNode) {
            // Array element: compile index, then increment, then ainc
            // Rule [csommeT]: pe1 ⊕ pe ⊕D ainc(i)
            visitExpression(tabNode.getIndex());  // pe1 - index

            if (node.getExpressionNode() != null) {
                visitExpression(node.getExpressionNode());  // pe - increment value
            }

            String arrayName = extractIdentifierName(tabNode.getIdent());
            String scopeAddress = resolveVariableScope(arrayName);
            jjcBuilder.addInstruction(AINC, arrayName + "@" + scopeAddress);
        }
    }

    /**
     * Compiles an increment instruction according to the [cincrément] and [cincrémentT] rules:
     * - [cincrément]: n ⊢ incrément(ident(i)) ⇒ {jcnil ⊕D push(1) ⊕D inc(i), 2}
     * - [cincrémentT]: n ⊢ incrément(tab(ident(i), e) ⇒ {pe ⊕D push(1) ⊕D ainc(i), ne + 2}
     * <p>
     * Examples:
     * - x++; -> push(1), inc(x@global)
     * - arr[5]++; -> push(5), push(1), ainc(arr@global)
     *
     * @param node the IncrementNode representing the ++ instruction to compile
     */
    private void visitIncrement(IncrementNode node) {
        AstNode target = node.getIdent1();

        if (target instanceof IdentNode identNode) {
            // Simple variable: push(1), then inc
            // Rule [cincrément]: jcnil ⊕D push(1) ⊕D inc(i)
            jjcBuilder.addInstruction(PUSH, 1);

            String ident = extractIdentifierName(identNode);
            String scopeAddress = resolveVariableScope(ident);
            jjcBuilder.addInstruction(INC, ident + "@" + scopeAddress);

        } else if (target instanceof TabNode tabNode) {
            // Array element: compile index, then push(1), then ainc
            // Rule [cincrémentT]: pe ⊕D push(1) ⊕D ainc(i)
            visitExpression(tabNode.getIndex());  // pe - index

            jjcBuilder.addInstruction(PUSH, 1);

            String arrayName = extractIdentifierName(tabNode.getIdent());
            String scopeAddress = resolveVariableScope(arrayName);
            jjcBuilder.addInstruction(AINC, arrayName + "@" + scopeAddress);
        }
    }

    public void visit(AffectationNode node) {
        AstNode target = node.getIdent1Node();

        if (target instanceof IdentNode identNode) {
            // Simple variable: compile value, then store
            if (node.getExpression() != null) {
                visitExpression(node.getExpression());
            }

            String ident = extractIdentifierName(identNode);
            String scopeAddress = resolveVariableScope(ident);
            jjcBuilder.addInstruction(STORE, ident + "@" + scopeAddress);

        } else if (target instanceof TabNode tabNode) {
            // Array element: compile index, then value, then astore
            // Rule [caffecteT]: pe1 ⊕ pe ⊕D astore(i)
            visitExpression(tabNode.getIndex());  // pe1 - index

            if (node.getExpression() != null) {
                visitExpression(node.getExpression());  // pe - value
            }

            String arrayName = extractIdentifierName(tabNode.getIdent());
            String scopeAddress = resolveVariableScope(arrayName);
            jjcBuilder.addInstruction(ASTORE, arrayName + "@" + scopeAddress);
        } else if (target != null) {
            // Default case: original behavior for test compatibility
            if (node.getExpression() != null) {
                visitExpression(node.getExpression());
            }

            String ident = extractIdentifierName(target);
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
        } else if (node.getDecl() instanceof CstNode cstNode) {
            visit(cstNode);
        } else if (node.getDecl() instanceof TableauNode tableauNode) {
            visit(tableauNode);
        }

        if (node.getDecls() != null) {
            visit(node.getDecls());
        }
    }

    public void visit(VarsNode node) {
        if (node == null || node.getVar() == null) return;

        AstNode varNode = node.getVar();
        if (varNode instanceof VarNode vn) {
            visit(vn);
        } else if (varNode instanceof CstNode cn) {
            visit(cn);
        } else if (varNode instanceof TableauNode tn) {
            visit(tn);
        }

        if (node.getVars() != null) {
            visit(node.getVars());
        }
    }

    public void visit(VarNode node) {
        compileDeclaration(node.getExp(), node.getType(), node.getIdent().getNom(), "var");
    }

    /**
     * Compiles a constant declaration into JajaCode.
     * According to the [ccst] rule:
     * n ⊢ cst(t, ident(i), e) ⇒ {pe ⊕D new(i, t, cst, 0), ne + 1}
     *
     * @param node the CstNode to compile
     */
    public void visit(CstNode node) {
        compileDeclaration(node.getExp(), node.getType(), node.getIdent().getNom(), "cst");
    }

    /**
     * Compiles an array declaration according to the [ctableau] rule:
     * n ⊢ tableau(t, ident(i), e) ⇒ {pe ⊕D newarray(i, t), ne + 1}
     * Example: int tableau[20]; -> push(20), newarray(tableau@global, int)
     *
     * @param node the TableauNode to compile
     */
    public void visit(TableauNode node) {
        // Compile the size expression (pe)
        if (node.getExp() != null) {
            visitExpression(node.getExp());
        } else {
            jjcBuilder.addInstruction(PUSH, 0);
        }

        // Get name, type, and scope
        String ident = node.getIdent().getNom();
        String type = typeToJajaCode(node.getType());
        String scopeAddress = currentScope;

        // ⊕D newarray(i, t)
        jjcBuilder.addInstruction(NEWARRAY, ident + "@" + scopeAddress, type);

        // Track for cleanup (swap + pop)
        variablesToPop.push(ident + "@" + scopeAddress);

        // Register in the scope
        if ("global".equals(currentScope)) {
            globalVariables.add(ident);
        } else if ("main".equals(currentScope)) {
            mainLocalVariables.add(ident);
        } else {
            currentScopeVariables.add(ident);
        }
    }

    /**
     * Common method to compile a variable or constant declaration.
     *
     * @param vexpWrapper the initialization expression (can be null)
     * @param type        the declaration type
     * @param ident       the identifier name
     * @param kind        the declaration kind ("var" ou "cst")
     */
    private void compileDeclaration(Vexp vexpWrapper, Type type, String ident, String kind) {
        Expression vexp = vexpWrapper != null ? vexpWrapper.getVexp() : null;

        if (vexp != null) {
            visitExpression(vexp);
        } else {
            // If no initialization expression, push w (omega) for all variables/constants
            // Omega represents an uninitialized value, accepted by the type system
            jjcBuilder.addInstruction(PUSH, "w");
        }

        String scopeAddress = currentScope;

        jjcBuilder.addInstruction(NEW, ident + "@" + scopeAddress, typeToJajaCode(type), kind, 0);
        variablesToPop.push(ident + "@" + scopeAddress);

        if ("global".equals(currentScope)) {
            globalVariables.add(ident);
        } else if ("main".equals(currentScope)) {
            mainLocalVariables.add(ident);
        } else {
            // We are in a method
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
        Set<String> previousScopeVariables = new HashSet<>(currentScopeVariables);
        currentScope = methodSignature;
        currentScopeVariables.clear();

        // ========== PHASE 1: Calculer les tailles avec un visiteur temporaire ==========
        MiniJajaCompilerVisitor tempVisitor = new MiniJajaCompilerVisitor(collector);
        tempVisitor.currentScope = methodSignature;
        tempVisitor.globalVariables.addAll(this.globalVariables);

        // Calculer la taille des entêtes
        if (node.getEntetes() != null) {
            visitEntetesReverse(node.getEntetes(), tempVisitor, methodSignature);
        }

        // Calculer la taille des variables locales
        int varCountBefore = tempVisitor.variablesToPop.size();
        if (node.getVars() != null) {
            tempVisitor.visit(node.getVars());
        }
        int localVarCount = tempVisitor.variablesToPop.size() - varCountBefore;

        // Calculer la taille des instructions
        if (node.getInstrs() != null) {
            tempVisitor.visit(node.getInstrs());
        }
        int totalBodySize = tempVisitor.getJajaCodeBuilder().getInstructionsAsList().size();

        // Copier les variables du scope depuis le visiteur temporaire
        currentScopeVariables.addAll(tempVisitor.currentScopeVariables);

        // Calculer le nombre d'instructions pour le retrait des variables locales
        int retraitVarsCount = localVarCount * 2; // vars
        int push0Count = isVoidMethod ? 1 : 0;
        // swap + return = 2 instructions (pour void et non-void)
        int swapReturnCount = 2;

        // ========== PHASE 2: Générer le code avec les bonnes adresses ==========
        int currentAddr = jjcBuilder.getCurrentAddress();
        int methodStartAddr = currentAddr + 3; // après push + new + goto
        int methodEndAddr = methodStartAddr + totalBodySize + push0Count + retraitVarsCount + swapReturnCount;

        // Générer le code de déclaration de la méthode
        jjcBuilder.addInstruction(PUSH, methodStartAddr);
        jjcBuilder.addInstruction(NEW, methodName, methodType, "meth", 0);
        jjcBuilder.addInstruction(GOTO, methodEndAddr);

        // Ajouter la méthode à la liste des déclarations à retirer
        variablesToPop.push(methodName);

        // ========== PHASE 3: Compiler le corps directement dans le builder principal ==========

        // Sauvegarder la taille de variablesToPop avant de compiler le corps de la méthode
        int varStackSizeBefore = variablesToPop.size();

        // Compiler les entêtes (paramètres) directement
        if (node.getEntetes() != null) {
            visitEntetesDirectly(node.getEntetes(), methodSignature);
        }

        // Compiler les variables locales directement
        if (node.getVars() != null) {
            visit(node.getVars());
        }

        // Compiler les instructions directement
        if (node.getInstrs() != null) {
            visit(node.getInstrs());
        }

        // Selon [cméthodeRien] : piss ⊕ (push(0) ⊕G prdvs) ⊕D swap ⊕D return
        // Pour les méthodes void : push(0) AVANT le retrait des variables locales
        if (isVoidMethod) {
            jjcBuilder.addInstruction(PUSH, 0);
        }

        // prdvs : Retrait des variables locales uniquement
        for (int i = 0; i < localVarCount; i++) {
            jjcBuilder.addInstruction(SWAP);
            jjcBuilder.addInstruction(POP);
        }

        // Nettoyer variablesToPop : retirer les variables locales de la méthode qui ont été ajoutées
        // (elles sont gérées par les SWAP/POP générés ci-dessus, pas par le nettoyage global)
        while (variablesToPop.size() > varStackSizeBefore) {
            variablesToPop.pop();
        }


        // swap + return
        jjcBuilder.addInstruction(SWAP);
        jjcBuilder.addInstruction(RETURN);

        // Restaurer le scope
        currentScope = previousScope;
        currentScopeVariables.clear();
        currentScopeVariables.addAll(previousScopeVariables);
    }

    /**
     * Visite les entêtes directement dans le builder principal (pas dans un visitor séparé).
     */
    private void visitEntetesDirectly(EntetesNode entetes, String methodSignature) {
        if (entetes == null || entetes.getEntete() == null) {
            return;
        }

        // D'abord traiter le reste de la liste
        if (entetes.getEntetes() != null) {
            visitEntetesDirectly(entetes.getEntetes(), methodSignature);
        }

        // Ensuite traiter l'entête actuelle
        EnteteNode entete = entetes.getEntete();
        String paramName = entete.getIdent().getNom();
        String paramType = typeToJajaCode(entete.getType());

        // Calculer la profondeur
        int currentDepth = countParams(entetes);

        jjcBuilder.addInstruction(NEW, paramName + "@" + methodSignature, paramType, "var", currentDepth);
        currentScopeVariables.add(paramName);
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
        } else if (expression instanceof TabNode tabNode) {
            visitTab(tabNode);
        } else if (expression instanceof LengthNode lengthNode) {
            visitLength(lengthNode);
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

    /**
     * Compile un accès tableau en lecture selon la règle [ctab]:
     * n ⊢ tab(ident(i), e) ⇒ {pe ⊕D aload(i), ne + 1}
     * Exemple: x = tableau[5]; -> push(5), aload(tableau@global)
     *
     * @param node le nœud TabNode représentant l'accès au tableau
     */
    private void visitTab(TabNode node) {
        // Compiler l'expression d'index (pe)
        visitExpression(node.getIndex());

        // Résoudre le nom et scope du tableau
        String arrayName = extractIdentifierName(node.getIdent());
        String scopeAddress = resolveVariableScope(arrayName);

        // ⊕D aload(i)
        jjcBuilder.addInstruction(ALOAD, arrayName + "@" + scopeAddress);
    }

    /**
     * Compile l'opération length selon la règle [clongueur]:
     * n ⊢ longueur(ident(i)) ⇒ {jcnil ⊕D length(i), 1}
     * Exemple: int n = length(tableau); -> length(tableau@global)
     *
     * @param node le nœud LengthNode représentant l'opération length
     */
    private void visitLength(LengthNode node) {
        String arrayName = node.getId().getNom();
        String scopeAddress = resolveVariableScope(arrayName);

        // ⊕D length(i)
        jjcBuilder.addInstruction(LENGTH, arrayName + "@" + scopeAddress);
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
     * Note: Pour que les arguments correspondent aux paramètres avec les bonnes depths,
     * on compile l'expression courante (pe) AVANT le reste de la liste (plexp).
     * Cela permet que le premier argument soit à la profondeur la plus grande (depth=n)
     * et le dernier argument à depth=1, correspondant à l'ordre des paramètres.
     */
    private void visitListExp(ListExpNode node) {
        if (node == null) return;

        // Si la liste est vide (exnil)
        if (node.getExp() == null && node.getListExp() == null) {
            return;
        }

        // D'abord compiler l'expression courante (pe) - le premier argument
        if (node.getExp() != null) {
            visitExpression(node.getExp());
        }

        // Ensuite compiler le reste de la liste (plexp) - les arguments suivants
        if (node.getListExp() != null) {
            visitListExp(node.getListExp());
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
