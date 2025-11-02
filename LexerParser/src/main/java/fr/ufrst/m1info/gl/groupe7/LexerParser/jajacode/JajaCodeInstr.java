package fr.ufrst.m1info.gl.groupe7.LexerParser.jajacode;

public enum JajaCodeInstr {
    INIT("init"),
    JCSTOP("jcstop"),
    NEW("new", 4), // new(ident, type, sorte, adr)
    NEWARRAY("newarray", 2), // newarray(ident, type)
    POP("pop"),
    SWAP("swap"),
    PUSH("push", 1), // push(valeur)
    LOAD("load", 1), // load(ident)
    STORE("store", 1), // store(ident)
    ALOAD("aload", 1), // aload(ident)
    ASTORE("astore", 1), // astore(ident)
    LENGTH("length", 1), // length(ident)
    GOTO("goto", 1), // goto(adresse)
    IF("if", 1), // if(adresse)
    INVOKE("invoke", 1), // invoke(ident)
    RETURN("return"),
    ADD("add"),
    SUB("sub"),
    MUL("mul"),
    DIV("div"),
    CMP("cmp"),
    SUP("sup"),
    AND("and"),
    OR("or"),
    NOT("not"),
    NEG("neg"),
    WRITE("write"),
    WRITELN("writeln"),
    INC("inc"),
    AINC("ainc"),
    ;

    private final String text;
    private final int numArgs;

    JajaCodeInstr(String text) { this(text, 0); }
    JajaCodeInstr(String text, int numArgs) { this.text = text; this.numArgs = numArgs; }

    @Override
    public String toString() { return text; }

    public int getNumArgs() { return numArgs; }
}
