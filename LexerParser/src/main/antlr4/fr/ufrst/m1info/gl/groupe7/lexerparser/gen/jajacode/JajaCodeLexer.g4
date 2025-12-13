lexer grammar JajaCodeLexer;

INIT: 'init' ;
SWAP: 'swap' ;
NEW: 'new' ;
NEWARRAY: 'newarray' ;
INVOKE: 'invoke' ;
LENGTH: 'length' ;
RETURN: 'return' ;
WRITE: 'write' ;
WRITELN: 'writeln' ;
PUSH: 'push' ;
POP: 'pop' ;
LOAD: 'load' ;
ALOAD: 'aload' ;
STORE: 'store' ;
ASTORE: 'astore' ;
IF: 'if' ;
GOTO: 'goto' ;
INC: 'inc' ;
AINC: 'ainc' ;
OPER: 'oper' ;
NOP: 'nop' ;
JCSTOP: 'jcstop' ;
TRUE: 'true' ;
FALSE: 'false' ;
VIDE: 'vide' ;

// oper1
NEG: 'neg' ;
NOT: 'not' ;
// oper2
ADD: 'add' ;
SUB: 'sub' ;
MUL: 'mul' ;
DIV: 'div' ;
CMP: 'cmp' ;
SUP: 'sup' ;
OR: 'or' ;
AND: 'and' ;

// Séparateurs et Symboles
LPAREN : '(' ;
RPAREN : ')' ;
COMA : ',' ;

// Types de données
TYPE : 'int' | 'boolean' | 'void';
SORTE : 'var' | 'meth' | 'cst' ;

// Identifiant et Valeur
IDENTIFIER: [a-zA-Z_][a-zA-Z_0-9@]* ;
NOMBRE: [0-9]+ ;
STRING: '"' (~["\r\n])* '"' ;  // Chaînes entre guillemets doubles

WS: [ \t\n\r\f]+ -> skip ;