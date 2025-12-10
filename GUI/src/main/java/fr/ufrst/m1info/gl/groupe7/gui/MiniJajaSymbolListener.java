package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParserBaseListener;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import javafx.scene.control.IndexRange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ANTLR listener to find symbols (declared methods and their calls) in a MiniJaja parse tree.
 * This enables semantic highlighting for user-defined functions.
 */
public class MiniJajaSymbolListener extends MiniJajaParserBaseListener {

    private final List<IndexRange> functionRanges = new ArrayList<>();
    private final Set<String> declaredMethodNames = new HashSet<>();

    public List<IndexRange> getFunctionStyleRanges() {
        return functionRanges;
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
    }

    /**
     * Called when the walker enters a factor rule, which might be a method call.
     * Checks if the identifier corresponds to a user-declared method and styles it.
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
    }
}
