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
    private final Set<String> declaredVariableNames = new HashSet<>();

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
        int start;
        int end = -1;
        boolean seenInstruction = false;
        boolean isMethodOrMainScope = false;

        Scope(int start) {
            this.start = start;
        }
    }

    private static class PersistedScope {
        final int start;
        final int end;
        final Set<String> variables;
        final boolean hadInstruction;

        PersistedScope(int start, int end, Set<String> variables, boolean hadInstruction) {
            this.start = start;
            this.end = end;
            this.variables = variables;
            this.hadInstruction = hadInstruction;
        }
    }

    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final List<IndexRange> unusedVariableRanges = new ArrayList<>();
    private final List<IndexRange> declarationAfterInstructionRanges = new ArrayList<>();
    private final List<PersistedScope> persistedScopes = new ArrayList<>();


    public List<IndexRange> getFunctionStyleRanges() {
        return functionRanges;
    }

    public List<IndexRange> getUnusedVariableRanges() {
        return unusedVariableRanges;
    }

    public List<IndexRange> getDeclarationAfterInstructionRanges() {
        return declarationAfterInstructionRanges;
    }


    private void enterScope(int start, boolean isMethodOrMain) {
        Scope scope = new Scope(start);
        scope.isMethodOrMainScope = isMethodOrMain;
        scopes.push(scope);
    }

    private void exitScope(int end) {
        if (scopes.isEmpty()) return;
        Scope scope = scopes.pop();
        scope.end = end;
        for (Variable var : scope.variables.values()) {
            if (!var.used) {
                unusedVariableRanges.add(var.declarationRange);
            }
        }
        Set<String> vars = new HashSet<>(scope.variables.keySet());
        persistedScopes.add(new PersistedScope(scope.start, scope.end, vars, scope.seenInstruction));
    }

    private void addVariable(String name, org.antlr.v4.runtime.Token token) {
        if (scopes.isEmpty() || name == null || token == null) return;
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1;
        Objects.requireNonNull(scopes.peek()).variables.put(name, new Variable(new IndexRange(start, stop)));
        // record declared variable for autocomplete suggestions
        declaredVariableNames.add(name);
    }

    /**
     * Returns a list of method names declared in the parsed source.
     */
    public List<String> getDeclaredMethods() {
        return new ArrayList<>(declaredMethodNames);
    }

    /**
     * Returns a list of variable names declared in the parsed source.
     */
    public List<String> getDeclaredVariables() {
        return new ArrayList<>(declaredVariableNames);
    }

    public boolean isOffsetInScopeWithInstruction(int offset) {
        for (PersistedScope ps : persistedScopes) {
            if (ps.start <= offset && offset < ps.end) {
                if (ps.hadInstruction) return true;
            }
        }
        return false;
    }

    /**
     * Returns the variables visible at a given text offset (caret position).
     * This respects scope: only variables declared in scopes that include the
     * offset are returned.
     */
    public List<String> getVisibleVariables(int offset) {
        Set<String> visible = new HashSet<>();
        for (PersistedScope ps : persistedScopes) {
            if (ps.start <= offset && offset < ps.end) {
                visible.addAll(ps.variables);
            }
        }
        return new ArrayList<>(visible);
    }

    /**
     * Returns variables visible at the given offset ordered by scope proximity
     * (inner scopes first). Removes duplicates while preserving ordering.
     */
    public List<String> getVisibleVariablesOrdered(int offset) {
        // collect scopes that include offset
        List<PersistedScope> enclosing = new ArrayList<>();
        for (PersistedScope ps : persistedScopes) {
            if (ps.start <= offset && offset < ps.end) {
                enclosing.add(ps);
            }
        }
        // sort by scope size ascending (smaller = inner)
        enclosing.sort(Comparator.comparingInt(ps -> (ps.end - ps.start)));

        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (PersistedScope ps : enclosing) {
            ordered.addAll(ps.variables);
        }
        return new ArrayList<>(ordered);
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
        enterScope(ctx.getStart().getStartIndex(), true);
    }

    @Override
    public void exitMethode(MiniJajaParser.MethodeContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterClasse(MiniJajaParser.ClasseContext ctx) {
        enterScope(ctx.getStart().getStartIndex(), false);
    }

    @Override
    public void exitClasse(MiniJajaParser.ClasseContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterMethmain(MiniJajaParser.MethmainContext ctx) {
        enterScope(ctx.getStart().getStartIndex(), true);
    }

    @Override
    public void exitMethmain(MiniJajaParser.MethmainContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterVar(MiniJajaParser.VarContext ctx) {
        if (ctx.IDENT() != null) {
            if (!scopes.isEmpty()) {
                Scope current = scopes.peek();
                if (current.isMethodOrMainScope && current.seenInstruction) {
                    if (ctx.TYPE() != null && ctx.TYPE().getSymbol() != null) {
                        org.antlr.v4.runtime.Token typeToken = ctx.TYPE().getSymbol();
                        int startType = typeToken.getStartIndex();
                        int stopType = typeToken.getStopIndex() + 1;
                        declarationAfterInstructionRanges.add(new IndexRange(startType, stopType));
                    }
                }
            }
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
        if (!scopes.isEmpty()) {
            scopes.peek().seenInstruction = true;
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