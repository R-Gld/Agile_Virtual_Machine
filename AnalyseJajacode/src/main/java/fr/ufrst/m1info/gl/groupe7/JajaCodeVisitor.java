package fr.ufrst.m1info.gl.groupe7;

import java.util.Stack;

public class JajaCodeVisitor extends JajaCodeParserBaseVisitor<Object> {
    private final Stack<Object> stack = new Stack<>();

    @Override
    public Object visitClasse(JajaCodeParser.ClasseContext ctx) {
        return super.visitClasse(ctx);
    }

    @Override
    public Object visitInstr(JajaCodeParser.InstrContext ctx) {
        if (ctx.PUSH() != null) {
            Object val = visit(ctx.valeur());
            stack.push(val);
        } else if (ctx.LOAD() != null) {
            stack.push(ctx.ident().getText());
        } else if (ctx.oper() != null) {
            visit(ctx.oper());
        } else if (ctx.WRITE() != null) {
            System.out.print(stack.pop());
        } else if (ctx.WRITELN() != null) {
            System.out.println(stack.pop());
        }
        return null;
    }

    @Override
    public Object visitOper(JajaCodeParser.OperContext ctx) {
        if (ctx.oper2() != null) {
            return visitOper2(ctx.oper2());
        } else if (ctx.oper1() != null) {
            return visitOper1(ctx.oper1());
        }
        return null;
    }

    @Override
    public Object visitOper2(JajaCodeParser.Oper2Context ctx) {
        Object right = stack.pop();
        Object left = stack.pop();
        String op = ctx.getText();
        switch (op) {
            case "add" -> stack.push((Integer) left + (Integer) right);
            case "sub" -> stack.push((Integer) left - (Integer) right);
            case "mul" -> stack.push((Integer) left * (Integer) right);
            case "div" -> stack.push((Integer) left / (Integer) right);
        }
        return null;
    }

    @Override
    public Object visitOper1(JajaCodeParser.Oper1Context ctx) {
        Object val = stack.pop();
        String op = ctx.getText();
        if (op.equals("neg")) {
            stack.push(-(Integer) val);
        } else if (op.equals("not")) {
            stack.push(!(Boolean) val);
        }
        return null;
    }

    @Override
    public Object visitValeur(JajaCodeParser.ValeurContext ctx) {
        if (ctx.NOMBRE() != null) {
            return Integer.parseInt(ctx.NOMBRE().getText());
        } else if (ctx.TRUE() != null) {
            return true;
        }
        return null;
    }
}