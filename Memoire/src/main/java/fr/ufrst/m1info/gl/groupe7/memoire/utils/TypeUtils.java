package fr.ufrst.m1info.gl.groupe7.memoire.utils;

public class TypeUtils {
    public static Type getTypeFromString(String type){
        return switch (type) {
            case "boolean", "BOOLEEN", "BOOLEAN" -> Type.BOOLEEN;
            case "int", "integer", "ENTIER" -> Type.ENTIER;
            case "void" -> Type.VOID;
            default -> throw new RuntimeException("Unknown type: " + type);
        };
    }
}
