lexer grammar MiniJajaLexer;

CLASS: 'class';
FINAL : 'final';
VOID : 'void';
TYPE : 'int'|'boolean';
MAIN : 'main';

AND : '&&' ;
OR : '||' ;
NOT : '!' ;
EQQ : '==' ;
EQ : '=' ;
COMMA : ',' ;
SEMI : ';' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '[';
RBRACE : ']';
LCURLY : '{' ;
RCURLY : '}' ;
MINUS : '-' ;
INCREMENT : '++' ;
SOMME : '+=' ;
PLUS : '+' ;
MULT : '*' ;
DIV : '/' ;
SUP : '>' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
RETURN : 'return' ;
WRITE : 'write' ;
WRITELN : 'writeln' ;
LENGTH : 'length' ;


COMMENTAIRE : '/*' .*? '*/' -> skip ;
COMMENTAIRELIGNE : '//' ~[\r\n]* -> skip ;
STRING : '"' ( ~["\\] | '\\' . )* '"' ;
BOOLEAN : 'true' | 'false' ;
NBRE : [0-9]+ ;
IDENT: [a-zA-Z_][a-zA-Z_0-9]* ;
WS: [ \t\n\r\f]+ -> skip ;




