parser grammar JajaCodeParser;
options { tokenVocab=JajaCodeLexer; }

program
    : classe EOF
    ;

classe
    : adresse instr SEMI classe
    |
    ;

instr
    : INIT
    | SWAP
    | NEW LPAREN ident COMA TYPE COMA SORTE COMA adresse RPAREN
    | NEWARRAY LPAREN ident COMA TYPE RPAREN
    | INVOKE LPAREN ident RPAREN
    | LENGTH LPAREN ident RPAREN
    | RETURN
    | WRITE
    | WRITELN
    | PUSH LPAREN valeur RPAREN
    | POP
    | LOAD LPAREN ident RPAREN
    | ALOAD LPAREN ident RPAREN
    | STORE LPAREN ident RPAREN
    | ASTORE LPAREN ident RPAREN
    | IF LPAREN adresse RPAREN
    | GOTO LPAREN adresse RPAREN
    | INC LPAREN ident RPAREN
    | AINC LPAREN ident RPAREN
    | oper
    | NOP
    | JCSTOP
    ;

// ident ::= IDENTIFIER
ident
    : IDENTIFIER
    ;

valeur
    : NOMBRE
    | TRUE
    | FALSE
    | STRING
    | VIDE
    ;

adresse
    : NOMBRE
    ;

oper
    : oper2
    | oper1
    ;

oper1
    : NEG
    | NOT
    ;

oper2
    : ADD
    | SUB
    | MUL
    | DIV
    | CMP
    | SUP
    | OR
    | AND
    ;