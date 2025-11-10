package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

public class JajaCodeInstructionBuilder {

    public static String build(JajaCodeInstr instr, Object... args) {
        String base = instr.toString();
        if (args.length != instr.getNumArgs()) {
            throw new IllegalArgumentException("Amount of argument given to '" + instr + "' is incorrect (expected " + instr.getNumArgs() + ", given " + args.length + ").");
        }

        if (instr.getNumArgs() == 0) return base;

        StringBuilder params = new StringBuilder();
        params.append("(");
        for (int i = 0; i < args.length; i++) {
            if (i != 0) params.append(", ");
            params.append(args[i]);
        }

        params.append(")");
        return base + params;
    }

}
