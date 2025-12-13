package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParserBaseListener;
import javafx.scene.control.IndexRange;

import java.util.*;

/**
 * ANTLR listener to find symbols (declared methods and their calls, unused variables) in a MiniJaja parse tree.
 * This enables semantic highlighting for user-defined functions and unused variables.
 */
public class MiniJajaSymbolListener extends MiniJajaParserBaseListener {

    private final List<IndexRange> functionRanges = new ArrayList<>();
    private final Set<String> declaredMethodNames = new HashSet<>();

    // Inner classes for tracking variables and scopes
    private static class Variable {
        final IndexRange declarationRange;
        boolean used = false;

        Variable(IndexRange declarationRange) {
            this.declarationRange = declarationRange;
        }
    }

    private static class Scope {
        final Map<String, Variable> variables = new HashMap<>();
    }

    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final List<IndexRange> unusedVariableRanges = new ArrayList<>();


    public List<IndexRange> getFunctionStyleRanges() {
        return functionRanges;
    }

    public List<IndexRange> getUnusedVariableRanges() {
        return unusedVariableRanges;
    }


    private void enterScope() {
        scopes.push(new Scope());
    }

    private void exitScope() {
        if (scopes.isEmpty()) return;
        Scope scope = scopes.pop();
        for (Variable var : scope.variables.values()) {
            if (!var.used) {
                unusedVariableRanges.add(var.declarationRange);
            }
        }
    }

    private void addVariable(String name, org.antlr.v4.runtime.Token token) {
        if (scopes.isEmpty() || name == null || token == null) return;
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1;
        scopes.peek().variables.put(name, new Variable(new IndexRange(start, stop)));
    }

    private void markVariableAsUsed(String name) {
        if (name == null) return;
        for (Scope scope : scopes) {
            if (scope.variables.containsKey(name)) {
                scope.variables.get(name).used = true;
                return;
            }
        }
    }


    /**
     * Called when the walker enters a method declaration rule.
     * Captures the name of the declared method and styles its declaration.
     *
     * @param ctx the parse tree context.
     */
    @Override
    public void enterMethode(MiniJajaParser.MethodeContext ctx) {
        if (ctx.IDENT() != null) {
            String methodName = ctx.IDENT().getText();
            declaredMethodNames.add(methodName);

            // Add the declaration site to the list of ranges to style
            org.antlr.v4.runtime.Token token = ctx.IDENT().getSymbol();
            int start = token.getStartIndex();
            int stop = token.getStopIndex() + 1;
            functionRanges.add(new IndexRange(start, stop));
        }
        enterScope();
    }

    @Override
    public void exitMethode(MiniJajaParser.MethodeContext ctx) {
        exitScope();
    }

    @Override
    public void enterClasse(MiniJajaParser.ClasseContext ctx) {
        enterScope();
    }

    @Override
    public void exitClasse(MiniJajaParser.ClasseContext ctx) {
        exitScope();
    }

    @Override
    public void enterMethmain(MiniJajaParser.MethmainContext ctx) {
        enterScope();
    }

    @Override
    public void exitMethmain(MiniJajaParser.MethmainContext ctx) {
        exitScope();
    }

    @Override
    public void enterVar(MiniJajaParser.VarContext ctx) {
        if (ctx.IDENT() != null) {
            addVariable(ctx.IDENT().getText(), ctx.IDENT().getSymbol());
        }
    }

    @Override
    public void enterEntete(MiniJajaParser.EnteteContext ctx) {
        if (ctx.IDENT() != null) {
            addVariable(ctx.IDENT().getText(), ctx.IDENT().getSymbol());
        }
    }

    @Override
    public void enterInstr(MiniJajaParser.InstrContext ctx) {
        // Handle assignment as usage
        if (ctx.ident1() != null && ctx.EQ() != null) {
             markVariableAsUsed(ctx.ident1().IDENT().getText());
        }
        // Handle increment as usage
        if (ctx.ident1() != null && ctx.INCREMENT() != null) {
            markVariableAsUsed(ctx.ident1().IDENT().getText());
        }
        // Handle sum as usage
        if (ctx.ident1() != null && ctx.SOMME() != null) {
            markVariableAsUsed(ctx.ident1().IDENT().getText());
        }

        if (ctx.IDENT() != null && ctx.LPAREN() != null) {
            String methodName = ctx.IDENT().getText();
            if (declaredMethodNames.contains(methodName)) {
                org.antlr.v4.runtime.Token token = ctx.IDENT().getSymbol();
                int start = token.getStartIndex();
                int stop = token.getStopIndex() + 1;
                functionRanges.add(new IndexRange(start, stop));
            }
        }
    }

    /**
     * Called when the walker enters a factor rule, which might be a method call or variable usage.
     * Checks if the identifier corresponds to a user-declared method and styles it.
     * Or if it's a variable, mark it as used.
     *
     * @param ctx the parse tree context.
     */
    @Override
    public void enterFact(MiniJajaParser.FactContext ctx) {
        // A 'fact' is a method call if it has an IDENT followed by a parenthesis.
        if (ctx.IDENT() != null && ctx.LPAREN() != null) {
            String methodName = ctx.IDENT().getText();
            // Check if it's one of the user-declared methods.
            if (declaredMethodNames.contains(methodName)) {
                org.antlr.v4.runtime.Token token = ctx.IDENT().getSymbol();
                int start = token.getStartIndex();
                int stop = token.getStopIndex() + 1;
                functionRanges.add(new IndexRange(start, stop));
            }
        }
        // A 'fact' can be a variable usage
        if (ctx.ident1() != null) {
            markVariableAsUsed(ctx.ident1().IDENT().getText());
        }
    }

    @Override
    public void enterIdent1(MiniJajaParser.Ident1Context ctx) {
        if (ctx.getParent() instanceof MiniJajaParser.FactContext) {
            // Already handled in enterFact to distinguish from method calls
            return;
        }

        if (ctx.IDENT() != null) {
            markVariableAsUsed(ctx.IDENT().getText());
        }
    }
}