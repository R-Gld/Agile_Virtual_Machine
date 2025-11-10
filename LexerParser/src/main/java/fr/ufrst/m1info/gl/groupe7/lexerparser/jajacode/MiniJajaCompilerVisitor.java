package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
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

    public MiniJajaCompilerVisitor() {
        this(new Stacks());
    }

    public JajaCodeBuilder getJajaCodeBuilder() {
        return jjcBuilder;
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
        }
        // TODO: Ajouter d'autres types d'instructions au fur et à mesure
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

            int scopeAddress = 1;
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

    public void visit(VarNode node) {
        jjcBuilder.addInstruction(PUSH, 0);

        String ident = node.getIdent().getNom();
        String type = node.getType();
        int scopeAddress = 1;

        // TODO: Déterminer le 'kind' correctement (var ou cst)
        String kind = "var";
        jjcBuilder.addInstruction(NEW, ident + "@" + scopeAddress, type, kind, 0);

        variablesToPop.push(ident + "@" + scopeAddress);
    }

    public void visit(NbreNode node) {
        jjcBuilder.addInstruction(PUSH, node.value);
    }

    private void visitExpression(AstNode expression) {
        if (expression instanceof NbreNode) {
            visit((NbreNode) expression);
        }
        // TODO: Ajouter d'autres types d'expressions au fur et à mesure
    }
}
