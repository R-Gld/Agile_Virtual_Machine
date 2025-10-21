package fr.ufrst.m1info.gl.groupe7;

import java.util.Stack;

public class JajaCodeListener extends JajaCodeParserBaseListener {
    private final Stack<String> stack = new Stack<>();
    private boolean jcstopDetected = false;

    @Override
    public void enterInstr(JajaCodeParser.InstrContext ctx) {
        if (ctx.PUSH() != null) {
            stack.push(ctx.valeur().getText());
        } else if (ctx.LOAD() != null) {
            stack.push(ctx.ident().getText());
        } else if (ctx.JCSTOP() != null) {
            jcstopDetected = true;
        }
    }

    @Override
    public void enterOper2(JajaCodeParser.Oper2Context ctx) {
        try {
            String b = stack.pop();
            String a = stack.pop();

            if (ctx.ADD() != null) {
                stack.push(a + " + " + b);
            } else if (ctx.SUB() != null) {
                stack.push(a + " - " + b);
            } else if (ctx.MUL() != null) {
                stack.push(a + " * " + b);
            } else if (ctx.DIV() != null) {
                stack.push("(" + a + ") / " + b);
            }
        } catch (Exception e) {
            System.err.println("Erreur pendant le traitement: " + e.getMessage());
        }
    }

    @Override
    public void exitInstr(JajaCodeParser.InstrContext ctx) {
        if (jcstopDetected && !stack.isEmpty()) {
            System.out.println("Expression: " + stack.peek());
            jcstopDetected = false;
        }
    }
}