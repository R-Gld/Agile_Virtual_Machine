package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.errors.SourcePosition;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.*;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * MiniJajaInterpreterVisitor builds the Abstract Syntax Tree (AST) from the ANTLR parse
 * tree.
 * Each visit method creates corresponding AST nodes for the MiniJaja language.
 */
public class MiniJajaInterpreterVisitor extends MiniJajaParserBaseVisitor<AstNode> {

	private static final Logger logger = LoggerFactory.getLogger(MiniJajaInterpreterVisitor.class);

	private final String fileName;

	/**
	 * Constructor with file name for source position tracking.
	 * @param fileName the source file name (may be null)
	 */
	public MiniJajaInterpreterVisitor(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Default constructor (for backward compatibility).
	 */
	public MiniJajaInterpreterVisitor() {
		this(null);
	}

	/**
	 * Extract source position from ANTLR parser rule context.
	 * @param ctx the parser rule context
	 * @return the source position
	 */
	private SourcePosition extractPosition(ParserRuleContext ctx) {
		if (ctx == null || ctx.getStart() == null) {
			return new SourcePosition(fileName, 0, 0);
		}
		Token startToken = ctx.getStart();
		return new SourcePosition(
			fileName,
			startToken.getLine(),
			startToken.getCharPositionInLine()
		);
	}

	/**
	 * Attach source position to an AST node.
	 * @param node the AST node
	 * @param ctx the parser rule context
	 * @return the node with position attached
	 */
	private <T extends AstNode> T withPosition(T node, ParserRuleContext ctx) {
		if (node != null) {
			node.setSourcePosition(extractPosition(ctx));
		}
		return node;
	}

	// ======== CLASS ========

    /**
     * Visits the class declaration.
     *
     * @param ctx the parse tree context for the class
     * @return a {@link ClasseNode} representing the class
     */
    @Override
    public AstNode visitClasse(MiniJajaParser.ClasseContext ctx) {
        logger.debug("enter visitClasse: text='{}'", ctx.getText());
        String className = ctx.IDENT().getText();
        logger.debug("visitClasse: className='{}'", className);
        IdentNode ident = new IdentNode(className);

		DeclsNode decls = (DeclsNode) visit(ctx.decls());
		MainNode main = (MainNode) visit(ctx.methmain());

		logger.debug("exit visitClasse: built ClasseNode for '{}'", className);
		return withPosition(new ClasseNode(ident, decls, main), ctx);
	}

	// ======== DECLARATIONS ========

    /**
     * Visits the declarations (variables, methods, constants).
     * This is a recursive rule.
     *
     * @param ctx the parse tree context for declarations
     * @return a {@link DeclsNode} representing a list of declarations
     */
    @Override
    public AstNode visitDecls(MiniJajaParser.DeclsContext ctx) {
        logger.debug("enter visitDecls: text='{}'", ctx.getText());
        // Base case: no more declarations
        if (ctx.decl() == null) {
            logger.debug("visitDecls: no declarations (vnil)");
            return withPosition(new DeclsNode(), ctx); // empty declaration list
        }

		AstNode firstDecl = visit(ctx.decl());
		DeclsNode nextDecls = (DeclsNode) visit(ctx.decls());
		logger.debug("exit visitDecls: created DeclsNode");
		return withPosition(new DeclsNode(firstDecl, nextDecls), ctx);
	}

    /**
     * Visits a single declaration.
     *
     * @param ctx the parse tree context for a declaration
     * @return either a {@link VarNode} (or subclass) or a {@link MethodeNode}
     */
    @Override
    public AstNode visitDecl(MiniJajaParser.DeclContext ctx) {
        logger.debug("enter visitDecl: text='{}'", ctx.getText());
        if (ctx.var() != null) {
            logger.debug("visitDecl: delegating to visitVar");
            return visit(ctx.var());
        } else if (ctx.methode() != null) {
            logger.debug("visitDecl: delegating to visitMethode");
            return visit(ctx.methode());
        }
        logger.debug("visitDecl: unexpected decl type");
        return null; // Should not reach here
    }

    /**
     * Visits a variable expression wrapper (Vexp).
     *
     * @param ctx the parse tree context for Vexp
     * @return an {@link Expression} or null
     */
    @Override
    public AstNode visitVexp(MiniJajaParser.VexpContext ctx) {
        logger.debug("enter visitVexp: text='{}'", (ctx.exp() != null ? ctx.exp().getText() : "<null>"));
        if (ctx.children == null) {
            logger.debug("visitVexp: no expression (null)");
            return null;
        }
        return visit(ctx.exp());
    }

	// ======== MAIN METHOD ========

    /**
     * Visits the main method definition.
     *
     * @param ctx the parse tree context for the main method
     * @return a {@link MainNode}
     */
    @Override
    public AstNode visitMethmain(MiniJajaParser.MethmainContext ctx) {
        logger.debug("enter visitMethmain: text='{}'", ctx.getText());
		VarsNode vars = (VarsNode) visit(ctx.vars());
		InstructionsNode instructions = (InstructionsNode) visit(ctx.instrs());
		logger.debug("exit visitMethmain: built MainNode");
		return withPosition(new MainNode(vars, instructions), ctx);
	}

	// ======== VARIABLES ========

    /**
     * Visits a variable declaration.
     * Handles simple variables ({@link VarNode}), constants ({@link CstNode}),
     * and arrays ({@link TableauNode}).
     *
     * @param ctx the parse tree context for a variable declaration
     * @return a {@link VarNode}, {@link CstNode}, or {@link TableauNode}
     */
    @Override
    public AstNode visitVar(MiniJajaParser.VarContext ctx) {
        String typeText = ctx.TYPE().getText();
        Type type;
        switch (typeText) {
            case "int":
                type = Type.ENTIER;
                break;
            case "boolean":
                type = Type.BOOLEEN;
                break;
            
            default:
                return null;
        }

		IdentNode ident = new IdentNode(ctx.IDENT() != null ? ctx.IDENT().getText() : "");
        logger.debug("visitVar: ident='{}', type='{}'", ident.getNom(), typeText);
		Expression vexp = null;

        if(ctx.vexp() != null) {
            // If an initialization expression is present
            vexp = (Expression) visit(ctx.vexp());

		}

        // Case 1: final constant
        if (ctx.FINAL() != null) {
                return withPosition((vexp != null)
                ? new CstNode(type, ident, vexp)
                : new CstNode(type, ident), ctx);
        }

        // Case 2: array (declared with a size expression)
        if (ctx.exp() != null) {
            Type tableauType = switch (typeText) {
                case "int" -> Type.ENTIER;
                case "boolean" -> Type.BOOLEEN;
                default -> null;
            };
            return withPosition(new TableauNode(tableauType, ident, (Expression) visit(ctx.exp())), ctx);
        }

        // Case 3: simple variable
        logger.debug("exit visitVar: created variable node for '{}', type='{}'", ident.getNom(), type);
        return withPosition((vexp != null)
                ? new VarNode(type, ident , vexp)
                : new VarNode(type, ident), ctx);
    }

    /**
     * Visits a list of variable declarations (recursive).
     *
     * @param ctx the parse tree context for variable list
     * @return a {@link VarsNode}
     */
    @Override
    public AstNode visitVars(MiniJajaParser.VarsContext ctx) {
        logger.debug("enter visitVars: text='{}'", ctx.getText());
		if (ctx.children == null)
			return withPosition(new VarsNode(), ctx);
		AstNode firstVar =  visit(ctx.var());
		VarsNode nextVars = (VarsNode) visit(ctx.vars());
		logger.debug("exit visitVars: created VarsNode");
		return withPosition(new VarsNode(firstVar, nextVars), ctx);
	}

    /**
     * Visits a method declaration.
     *
     * @param ctx the parse tree context for a method
     * @return a {@link MethodeNode}
     */
    @Override
    public AstNode visitMethode(MiniJajaParser.MethodeContext ctx) {
        logger.debug("enter visitMethode: text='{}'", ctx.getText());
		String typeText = ctx.typemeth().getText();
		Type typeMeth;
		switch (typeText) {
			case "int":
				typeMeth = Type.ENTIER;
				break;
			case "boolean":
				typeMeth = Type.BOOLEEN;
				break;
			case "void":
				typeMeth = Type.VOID;
				break;
			default:
				return null;
		}
		IdentNode ident = new IdentNode(ctx.IDENT().getText());
		EntetesNode entetes = (EntetesNode) visit(ctx.entetes());
		VarsNode vars = (VarsNode) visit(ctx.vars());
		InstructionsNode instrs = (InstructionsNode) visit(ctx.instrs());
        logger.debug("exit visitMethode: methode='{}' type='{}'", ident.getNom(), typeText);
		return withPosition(new MethodeNode(typeMeth, ident, entetes, vars, instrs), ctx);
	}

    /**
     * Visits the list of method headers/parameters (recursive).
     *
     * @param ctx the parse tree context for headers
     * @return an {@link EntetesNode}
     */
    @Override
    public AstNode visitEntetes(MiniJajaParser.EntetesContext ctx) {
        logger.debug("enter visitEntetes: text='{}'", ctx.getText());
		// Base case: no more parameters
		if (ctx.entete() == null) {
			logger.debug("visitEntetes: empty entetes");
			return withPosition(new EntetesNode(), ctx); // empty parameter list
		}

        EnteteNode firstEntete = ctx.entete() != null ? (EnteteNode) visit(ctx.entete()) : null;
        EntetesNode nextEntetes = ctx.entetes() != null ? (EntetesNode) visit(ctx.entetes()) : new EntetesNode();
        logger.debug("exit visitEntetes: created EntetesNode");
        return withPosition(new EntetesNode(firstEntete, nextEntetes), ctx);
    }

    /**
     * Visits a single method header/parameter.
     *
     * @param ctx the parse tree context for a header
     * @return an {@link EnteteNode}
     */
    @Override
    public AstNode visitEntete(MiniJajaParser.EnteteContext ctx) {
        logger.debug("enter visitEntete: text='{}'", ctx.getText());
		IdentNode ident = new IdentNode(ctx.IDENT().getText());
		String typeText = ctx.TYPE().getText();
		Type type;
		switch (typeText) {
			case "int":
				type = Type.ENTIER;
				break;
			case "boolean":
				type = Type.BOOLEEN;
				break;
			default:
				return null;
		}

        logger.debug("exit visitEntete: ident='{}' type='{}'", ident.getNom(), type);
        return withPosition(new EnteteNode(ident, type), ctx);
    }

	// ======== INSTRUCTIONS ========

    /**
     * Visits a list of instructions (recursive).
     *
     * @param ctx the parse tree context for instructions
     * @return an {@link InstructionsNode}
     */
    @Override
    public AstNode visitInstrs(MiniJajaParser.InstrsContext ctx) {
        logger.debug("enter visitInstrs: text='{}'", ctx.getText());

		if (ctx.instr() == null) {
			return withPosition(new InstructionsNode(), ctx); // Empty list (Inil)
		}

		InstructionNode firstInstr = (InstructionNode) visit(ctx.instr());

		InstructionsNode next = (InstructionsNode) visit(ctx.instrs());

		return withPosition(new InstructionsNode(firstInstr, next), ctx);
	}

    /**
     * Visits a single instruction.
     * Handles control flow (if, while, return), calls, I/O, assignments, and increments.
     *
     * @param ctx the parse tree context for an instruction
     * @return an {@link InstructionNode} subtype
     */
    @Override
    public AstNode visitInstr(MiniJajaParser.InstrContext ctx) {
        logger.debug("enter visitInstr: text='{}'", ctx.getText());

		// IF statement
		if (ctx.IF() != null) {
			Expression condition = (Expression) visit(ctx.exp());
			InstructionsNode thenBlock = (InstructionsNode) visit(ctx.instrs(0));
			InstructionsNode elseBlock = (ctx.ELSE() != null && ctx.instrs().size() > 1)
					? (InstructionsNode) visit(ctx.instrs(1))
					: null;
			logger.debug("visitInstr: IF -> SiNode");
			return withPosition(new SiNode(condition, thenBlock, elseBlock), ctx);
		}

		// WHILE loop
		if (ctx.WHILE() != null) {
			Expression condition = (Expression) visit(ctx.exp());
			InstructionsNode loopBody = (InstructionsNode) visit(ctx.instrs(0));
			logger.debug("visitInstr: WHILE -> TantqueNode");
			return withPosition(new TantqueNode(condition, loopBody), ctx);
		}

		// RETURN statement
		if (ctx.RETURN() != null) {
			Expression returned = (Expression) visit(ctx.exp());
			logger.debug("visitInstr: RETURN -> RetourNode");
			return withPosition(new RetourNode(returned), ctx);
		}

        // APPELI (Method call instruction)
        if (ctx.listexp() != null) {
            ListExpNode listExp = (ListExpNode) visit(ctx.listexp());
            IdentNode ident =  new IdentNode(ctx.IDENT().getText());
            logger.debug("visitInstr: IDENT -> AppelINode");

            return withPosition(new AppelINode(ident, listExp), ctx);
        }

        // --- 1) WRITE / WRITELN ---
        if (ctx.WRITE() != null || ctx.WRITELN() != null) {
            boolean writeln = ctx.WRITELN() != null;

            // Cas 1 : ident1
            if (ctx.ident1() != null) {
                Expression expr = (Expression) visit(ctx.ident1());
                return withPosition(writeln ? new EcrireLnNode(expr) : new EcrireNode(expr), ctx);
            }

            // Cas 2 : string literal
            if (ctx.STRING() != null) {
                String raw = ctx.STRING().getText();
                String content = raw.substring(1, raw.length() - 1);
                return withPosition(writeln ? new EcrireLnNode(content) : new EcrireNode(content), ctx);
            }

            throw new RuntimeException("WRITE sans ident1 ni STRING");
        }

		// Assignment, addition, or increment on an identifier (e.g. a = ..., a += ..., a++)
		if (ctx.ident1() != null) {
			AstNode ident1 = visit(ctx.ident1());
			if (ctx.EQ() != null) {
				logger.debug("visitInstr: IDENT + EQ -> AffectationNode");
				return withPosition(new AffectationNode(ident1, (Expression) visit(ctx.exp())), ctx);
			}
			if (ctx.SOMME() != null) {
				logger.debug("visitInstr: IDENT + SOMME -> SommeNode");
				return withPosition(new SommeNode(ident1, (Expression) visit(ctx.exp())), ctx);
			}
			if (ctx.INCREMENT() != null) {
				logger.debug("visitInstr: IDENT + INCREMENT -> IncrementNode");
				return withPosition(new IncrementNode(ident1), ctx);
			}

		}





		throw new IllegalStateException("Unhandled instruction: " + ctx.getText());
	}

	// ======== EXPRESSIONS ========

    /**
     * Visits general expressions (AND, OR, NOT).
     *
     * @param ctx the parse tree context for expression
     * @return an {@link Expression}
     */
    @Override
    public AstNode visitExp(MiniJajaParser.ExpContext ctx) {
        logger.debug("enter visitExp: text='{}'", ctx.getText());
		if (ctx.exp1() != null && ctx.exp() != null) {
			Expression exp1 = (Expression) visit(ctx.exp1());
			Expression exp = (Expression) visit(ctx.exp());
			if (ctx.AND() != null)
				return withPosition(new AndNode(exp1, exp), ctx);
			if (ctx.OR() != null)
				return withPosition(new OrNode(exp1, exp), ctx);
		}
		if (ctx.exp1() != null && ctx.NOT() != null)
			return withPosition(new NotNode((Expression) visit(ctx.exp1())), ctx);
		if (ctx.exp1() != null)
			return visit(ctx.exp1());

        logger.warn("[visitExp] Unexpected expression: {}", ctx.getText());
		return null;
	}

    /**
     * Visits level 1 expressions (Equals, GreaterThan).
     *
     * @param ctx the parse tree context
     * @return an {@link Expression}
     */
    @Override
    public AstNode visitExp1(MiniJajaParser.Exp1Context ctx) {
        logger.debug("enter visitExp1: text='{}'", ctx.getText());
		if (ctx.exp1() != null && ctx.exp2() != null) {
			Expression exp1 = (Expression) visit(ctx.exp1());
			Expression exp2 = (Expression) visit(ctx.exp2());
			if (ctx.EQQ() != null)
				return withPosition(new EqualsNode(exp1, exp2), ctx);
			if (ctx.SUP() != null)
				return withPosition(new GreaterThanNode(exp1, exp2), ctx);
		}
		if (ctx.exp2() != null)
			return visit(ctx.exp2());
        logger.warn("[visitExp1] Unexpected expression: {}", ctx.getText());
		return null;
	}

    /**
     * Visits level 2 expressions (Plus, Minus, UnaryMinus).
     *
     * @param ctx the parse tree context
     * @return an {@link Expression}
     */
    @Override
    public AstNode visitExp2(MiniJajaParser.Exp2Context ctx) {
        logger.debug("enter visitExp2: text='{}'", ctx.getText());
		if (ctx.exp2() != null && ctx.terme() != null) {
			Expression exp2 = (Expression) visit(ctx.exp2());
			Expression terme = (Expression) visit(ctx.terme());
			if (ctx.PLUS() != null)
				return withPosition(new PlusNode(exp2, terme), ctx);
			if (ctx.MINUS() != null)
				return withPosition(new MinusNode(exp2, terme), ctx);
		}
		if (ctx.terme() != null && ctx.MINUS() != null)
			return withPosition(new UnaryMinusNode((Expression) visit(ctx.terme())), ctx);
		if (ctx.terme() != null)
			return visit(ctx.terme());
        logger.warn("[visitExp2] Unexpected expression: {}", ctx.getText());
		return null;
	}

    /**
     * Visits term expressions (Multiplication, Division).
     *
     * @param ctx the parse tree context
     * @return an {@link Expression}
     */
    @Override
    public AstNode visitTerme(MiniJajaParser.TermeContext ctx) {
        logger.debug("enter visitTerme: text='{}'", ctx.getText());
		if (ctx.fact() != null && ctx.terme() != null) {
			Expression fact = (Expression) visit(ctx.fact());
			Expression terme = (Expression) visit(ctx.terme());
			if (ctx.MULT() != null)
				return withPosition(new MultiplicationNode(terme, fact), ctx);
			if (ctx.DIV() != null)
				return withPosition(new DivisionNode(terme, fact), ctx);
		}
		if (ctx.fact() != null)
			return visit(ctx.fact());
        logger.warn("[visitTerme] Unexpected term: {}", ctx.getText());
		return null;
	}

	// ======== FACTORS & IDENTIFIERS ========

    /**
     * Visits a factor node (identifiers, literals, calls, etc.).
     *
     * @param ctx the parse tree context for a factor
     * @return an {@link Expression} or {@link IdentNode} depending on content
     */
    @Override
    public AstNode visitFact(MiniJajaParser.FactContext ctx) {
        logger.debug("enter visitFact: text='{}'", ctx.getText());
		if (ctx.ident1() != null)
			return visit(ctx.ident1());

		if (ctx.IDENT() != null) {
			IdentNode ident = new IdentNode(ctx.IDENT().getText());
			if (ctx.LENGTH() != null)
				return withPosition(new LengthNode(ident), ctx);
			if (ctx.listexp() != null)
				return withPosition(new AppelENode(ident, (ListExpNode) visit(ctx.listexp())), ctx);
		}

		if (ctx.exp() != null)
			return visit(ctx.exp());
		if (ctx.NBRE() != null)
			return withPosition(new NbreNode(Integer.parseInt(ctx.NBRE().getText())), ctx);
		if (ctx.BOOLEAN() != null)
			return withPosition(new BoolValueNode(Boolean.parseBoolean(ctx.BOOLEAN().getText())), ctx);

        logger.warn("[visitFact] Unexpected fact node: {}", ctx.getText());
		return null;
	}

    /**
     * Visits a level 1 identifier (Variable access or Array access).
     *
     * @param ctx the parse tree context
     * @return an {@link IdentNode} or {@link TabNode}
     */
    @Override
    public AstNode visitIdent1(MiniJajaParser.Ident1Context ctx) {
        logger.debug("enter visitIdent1: text='{}'", ctx.getText());
		IdentNode ident = new IdentNode(ctx.IDENT().getText());
		if (ctx.exp() != null) {
			Expression index = (Expression) visit(ctx.exp());
			return withPosition(new TabNode(ident, index), ctx);
		}
		return withPosition(ident, ctx);
	}

    /**
     * Visits a list of expressions (e.g., arguments in a function call).
     *
     * @param ctx the parse tree context for expression list
     * @return a {@link ListExpNode}
     */
    @Override
    public AstNode visitListexp(MiniJajaParser.ListexpContext ctx) {
        logger.debug("enter visitListexp: text='{}'", ctx.getText());

		if (ctx.exp() == null) {
			return withPosition(new ListExpNode(null, null), ctx); // node"exnil"
		}

		Expression exp = (Expression) visit(ctx.exp());

		if (ctx.listexp() != null) {
			ListExpNode next = (ListExpNode) visit(ctx.listexp());
			return withPosition(new ListExpNode(exp, next), ctx);
		}
		return withPosition(new ListExpNode(exp, null), ctx);

	}


    /**
     * Default result when a 'visit' method is not implemented.
     */
    @Override
    protected AstNode defaultResult() {
        logger.debug("defaultResult called");
        throw new UnsupportedOperationException("Visit not implemented for this node.");
        
    }
}
