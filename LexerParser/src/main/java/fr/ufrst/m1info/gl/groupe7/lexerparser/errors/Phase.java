package fr.ufrst.m1info.gl.groupe7.lexerparser.errors;

/**
 * Represents the various phases in which a diagnostic can occur during the processing
 * of a software component or program. These phases include: <br> <br>
 * - <code>LEXICAL</code>: Refers to the lexical analysis phase, which involves breaking down
 *   the input into tokens.<br>
 * - <code>SYNTAX</code>: Refers to the syntax analysis phase, which involves checking the syntactic
 *   structure of the input against the grammar of the language. <br>
 * - <code>SEMANTIC</code>: Refers to the semantic analysis phase, which involves verifying
 *   that the input satisfies semantic rules, such as type checking. <br>
 * - <code>RUNTIME</code>: Refers to the runtime phase, where issues may arise during the
 *   execution of the program. <br>
 */
public enum Phase {
    LEXICAL,
    SYNTAX,
    SEMANTIC,
    RUNTIME
}
