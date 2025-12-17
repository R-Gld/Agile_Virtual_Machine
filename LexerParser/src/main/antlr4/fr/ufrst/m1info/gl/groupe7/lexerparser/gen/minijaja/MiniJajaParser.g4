parser grammar MiniJajaParser;

options { tokenVocab=MiniJajaLexer; }

classe: CLASS IDENT LCURLY decls methmain RCURLY | EOF;

decls: decl SEMI decls
    |
    ;

decl: var | methode;

vars: var SEMI vars
    |
    ;

var:  TYPE IDENT vexp
    | TYPE IDENT LBRACE exp RBRACE
    | FINAL TYPE IDENT vexp
    ;

vexp: EQ exp
    |
    ;

methode : typemeth IDENT LPAREN entetes RPAREN LCURLY vars instrs RCURLY
    ;


methmain: MAIN LCURLY vars instrs RCURLY
;

entetes: entete COMMA entetes
    | entete
    |
    ;

entete: TYPE IDENT;

instrs: instr SEMI instrs
    |
    ;

instr: ident1 EQ exp 
    | ident1 SOMME exp
    | ident1 INCREMENT
    | IDENT LPAREN listexp RPAREN
    | RETURN exp
    | WRITE LPAREN (ident1|STRING) RPAREN
    | WRITELN LPAREN (ident1|STRING) RPAREN
    | IF LPAREN exp RPAREN LCURLY instrs RCURLY (ELSE LCURLY instrs RCURLY)?
    | WHILE LPAREN exp RPAREN LCURLY instrs RCURLY
    ;
listexp: exp COMMA listexp
    | exp
    | ;



exp: NOT exp1
    | exp AND exp1
    | exp OR exp1
    | exp1
    ;


exp1: exp1 EQQ exp2
    | exp1 SUP exp2
    | exp2
    ;


exp2: exp2 PLUS terme
    | exp2 MINUS terme
    | MINUS terme
    | terme
    ;


terme: terme MULT fact
    | terme DIV fact
    | fact
    ;

fact: ident1
    | NBRE
    | BOOLEAN
    | LPAREN exp RPAREN
    | LENGTH LPAREN IDENT RPAREN
    | IDENT LPAREN listexp RPAREN
    ;

ident1: IDENT | IDENT LBRACE exp RBRACE;

typemeth: VOID
    | TYPE;

