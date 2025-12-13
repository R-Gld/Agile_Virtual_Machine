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
        int start = -1;
        int end = -1;
        boolean sawInstruction = false;

        Scope(int start) {
            this.start = start;
        }
    }

    private static class PersistedScope {
        final int start;
        final int end;
        final Set<String> variables;

        PersistedScope(int start, int end, Set<String> variables) {
            this.start = start;
            this.end = end;
            this.variables = variables;
        }
    }

    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final List<IndexRange> unusedVariableRanges = new ArrayList<>();
    private final List<PersistedScope> persistedScopes = new ArrayList<>();
    private final List<IndexRange> declAfterInstrRanges = new ArrayList<>();


    public List<IndexRange> getFunctionStyleRanges() {
        return functionRanges;
    }

    public List<IndexRange> getUnusedVariableRanges() {
        return unusedVariableRanges;
    }

    /**
     * Returns a list of ranges where variables were declared after instructions (invalid).
     */
    public List<IndexRange> getDeclAfterInstrRanges() {
        return declAfterInstrRanges;
    }


    private void enterScope(int start) {
        scopes.push(new Scope(start));
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
        persistedScopes.add(new PersistedScope(scope.start, scope.end, vars));
    }

    private void addVariable(String name, org.antlr.v4.runtime.Token token) {
        if (scopes.isEmpty() || name == null || token == null) return;
        int start = token.getStartIndex();
        int stop = token.getStopIndex() + 1;
        scopes.peek().variables.put(name, new Variable(new IndexRange(start, stop)));
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
            for (String v : ps.variables) {
                ordered.add(v);
            }
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

    private void markInstructionSeen() {
        if (!scopes.isEmpty()) {
            scopes.peek().sawInstruction = true;
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
        enterScope(ctx.getStart().getStartIndex());
    }

    @Override
    public void exitMethode(MiniJajaParser.MethodeContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterClasse(MiniJajaParser.ClasseContext ctx) {
        enterScope(ctx.getStart().getStartIndex());
    }

    @Override
    public void exitClasse(MiniJajaParser.ClasseContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterMethmain(MiniJajaParser.MethmainContext ctx) {
        enterScope(ctx.getStart().getStartIndex());
    }

    @Override
    public void exitMethmain(MiniJajaParser.MethmainContext ctx) {
        exitScope(ctx.getStop().getStopIndex() + 1);
    }

    @Override
    public void enterVar(MiniJajaParser.VarContext ctx) {
        if (ctx.IDENT() != null) {
            // If this var is declared after an instruction in the current scope,
            // record the type token range as a semantic error.
            if (!scopes.isEmpty() && scopes.peek().sawInstruction && ctx.TYPE() != null) {
                org.antlr.v4.runtime.Token typeToken = ctx.TYPE().getSymbol();
                int start = typeToken.getStartIndex();
                int stop = typeToken.getStopIndex() + 1;
                declAfterInstrRanges.add(new IndexRange(start, stop));
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
        // Mark that we've seen an instruction in the current scope
        markInstructionSeen();

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