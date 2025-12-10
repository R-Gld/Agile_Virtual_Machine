package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
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
 * MiniJajaVisitor builds the Abstract Syntax Tree (AST) from the ANTLR parse
 * tree.
 * Each visit method creates corresponding AST nodes for the MiniJaja language.
 */
public class MiniJajaInterpreterVisitor extends MiniJajaParserBaseVisitor<AstNode> {

	private static final Logger logger = LoggerFactory.getLogger(MiniJajaInterpreterVisitor.class);

	// ======== CLASS ========

	@Override
	public AstNode visitClasse(MiniJajaParser.ClasseContext ctx) {
		logger.debug("enter visitClasse: text='{}'", ctx.getText());
		String className = ctx.IDENT().getText();
		logger.debug("visitClasse: className='{}'", className);
		IdentNode ident = new IdentNode(className);

		DeclsNode decls = (DeclsNode) visit(ctx.decls());
		MainNode main = (MainNode) visit(ctx.methmain());

		logger.debug("exit visitClasse: built ClasseNode for '{}'", className);
		return new ClasseNode(ident, decls, main);
	}

	// ======== DECLARATIONS ========

	@Override
	public AstNode visitDecls(MiniJajaParser.DeclsContext ctx) {
		logger.debug("enter visitDecls: text='{}'", ctx.getText());
		// Base case: no more declarations
		if (ctx.decl() == null) {
			logger.debug("visitDecls: no declarations (vnil)");
			return new DeclsNode(); // empty declaration list
		}

		AstNode firstDecl = visit(ctx.decl());
		DeclsNode nextDecls = (DeclsNode) visit(ctx.decls());
		logger.debug("exit visitDecls: created DeclsNode");
		return new DeclsNode(firstDecl, nextDecls);
	}

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

	@Override
	public AstNode visitMethmain(MiniJajaParser.MethmainContext ctx) {
        logger.debug("enter visitMethmain: text='{}'", ctx.getText());
		VarsNode vars = (VarsNode) visit(ctx.vars());
		InstructionsNode instructions = (InstructionsNode) visit(ctx.instrs());
		logger.debug("exit visitMethmain: built MainNode");
		return new MainNode(vars, instructions);
	}

	// ======== VARIABLES ========

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
			case "void": // contrôle des types : interdire ça
				type = Type.VOID;
				break;
			default:
				return null;
		}
		

		IdentNode ident = new IdentNode(ctx.IDENT() != null ? ctx.IDENT().getText() : "");
        logger.debug("visitVar: ident='{}', type='{}'", ident.getNom(), typeText);
		Expression vexp = null;

		if(ctx.vexp() != null) {	
		// Si une expression d'initialisation est présente
		vexp =(Expression) visit(ctx.vexp()) ;

		}


		// Cas 1 : constante finale
		if (ctx.FINAL() != null) {
				return (vexp != null)
				? new CstNode(type, ident, vexp)
				: new CstNode(type, ident);
		}

		// Cas 2 : tableau (déclaré avec une taille exp)
		if (ctx.exp() != null) {
			Type tableauType = switch (typeText) {
				case "int" -> Type.ENTIER;
				case "boolean" -> Type.BOOLEEN;
				default -> null;
			};
			return new TableauNode(tableauType, ident, (Expression) visit(ctx.exp()));
		}

		// Cas 3 : variable simple
        logger.debug("exit visitVar: created variable node for '{}', type='{}'", ident.getNom(), type);
		return (vexp != null)
				? new VarNode(type, ident , vexp)
				: new VarNode(type, ident);
	}


	@Override
	public AstNode visitVars(MiniJajaParser.VarsContext ctx) {
        logger.debug("enter visitVars: text='{}'", ctx.getText());
		if (ctx.children == null)
			return new VarsNode();
		AstNode firstVar =  visit(ctx.var());
		VarsNode nextVars = (VarsNode) visit(ctx.vars());
		logger.debug("exit visitVars: created VarsNode");
		return new VarsNode(firstVar, nextVars);
	}

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
		return new MethodeNode(typeMeth, ident, entetes, vars, instrs);
	}

	@Override
	public AstNode visitEntetes(MiniJajaParser.EntetesContext ctx) {
        logger.debug("enter visitEntetes: text='{}'", ctx.getText());
		// Base case: no more parameters
		if (ctx.entete() == null) {
			logger.debug("visitEntetes: empty entetes");
			return new EntetesNode(); // empty parameter list
		}

		EnteteNode firstEntete = ctx.entete() != null ? (EnteteNode) visit(ctx.entete()) : null;
		EntetesNode nextEntetes = ctx.entetes() != null ? (EntetesNode) visit(ctx.entetes()) : new EntetesNode();
		logger.debug("exit visitEntetes: created EntetesNode");
		return new EntetesNode(firstEntete, nextEntetes);
	}
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
		return new EnteteNode(ident, type);
	}







	// ======== INSTRUCTIONS ========

	@Override
	public AstNode visitInstrs(MiniJajaParser.InstrsContext ctx) {
        logger.debug("enter visitInstrs: text='{}'", ctx.getText());

		if (ctx.instr() == null) {
			return new InstructionsNode(); // Empty list (Inil)
		}

		InstructionNode firstInstr = (InstructionNode) visit(ctx.instr());

		InstructionsNode next = (InstructionsNode) visit(ctx.instrs());

		return new InstructionsNode(firstInstr, next);
	}

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
			return new SiNode(condition, thenBlock, elseBlock);
		}

		// WHILE loop
		if (ctx.WHILE() != null) {
			Expression condition = (Expression) visit(ctx.exp());
			InstructionsNode loopBody = (InstructionsNode) visit(ctx.instrs(0));
			logger.debug("visitInstr: WHILE -> TantqueNode");
			return new TantqueNode(condition, loopBody);
		}

		// RETURN statement
		if (ctx.RETURN() != null) {
			Expression returned = (Expression) visit(ctx.exp());
			logger.debug("visitInstr: RETURN -> RetourNode");
			return new RetourNode(returned);
		}



        //APPELI relou
        if (ctx.listexp() != null) {
            ListExpNode listExp = (ListExpNode) visit(ctx.listexp());
            IdentNode ident =  new IdentNode(ctx.IDENT().getText());
            logger.debug("visitInstr: IDENT -> AppelINode");

            return new AppelINode(ident, listExp);

        }


        // --- 1) WRITE / WRITELN ---
        if (ctx.WRITE() != null || ctx.WRITELN() != null) {
            boolean writeln = ctx.WRITELN() != null;

            // Cas 1 : ident1
            if (ctx.ident1() != null) {
                Expression expr = (Expression) visit(ctx.ident1());
                return writeln ? new EcrireLnNode(expr) : new EcrireNode(expr);
            }

            // Cas 2 : string literal
            if (ctx.STRING() != null) {
                String raw = ctx.STRING().getText();
                String content = raw.substring(1, raw.length() - 1);
                return writeln ? new EcrireLnNode(content) : new EcrireNode(content);
            }

            throw new RuntimeException("WRITE sans ident1 ni STRING");
        }

		// Assignment, addition, or increment on an identifier (e.g. a = ..., a += ..., a++)
		if (ctx.ident1() != null) {
			AstNode ident1 = visit(ctx.ident1());
			if (ctx.EQ() != null) {
				logger.debug("visitInstr: IDENT + EQ -> AffectationNode");
				return new AffectationNode(ident1, (Expression) visit(ctx.exp()));
			}
			if (ctx.SOMME() != null) {
				logger.debug("visitInstr: IDENT + SOMME -> SommeNode");
				return new SommeNode(ident1, (Expression) visit(ctx.exp()));
			}
			if (ctx.INCREMENT() != null) {
				logger.debug("visitInstr: IDENT + INCREMENT -> IncrementNode");
				return new IncrementNode(ident1);
			}

		}





		throw new IllegalStateException("Unhandled instruction: " + ctx.getText());
	}

	// ======== EXPRESSIONS ========

	@Override
	public AstNode visitExp(MiniJajaParser.ExpContext ctx) {
        logger.debug("enter visitExp: text='{}'", ctx.getText());
		if (ctx.exp1() != null && ctx.exp() != null) {
			Expression exp1 = (Expression) visit(ctx.exp1());
			Expression exp = (Expression) visit(ctx.exp());
			if (ctx.AND() != null)
				return new AndNode(exp1, exp);
			if (ctx.OR() != null)
				return new OrNode(exp1, exp);
		}
		if (ctx.exp1() != null && ctx.NOT() != null)
			return new NotNode((Expression) visit(ctx.exp1()));
		if (ctx.exp1() != null)
			return visit(ctx.exp1());

        logger.warn("[visitExp] Unexpected expression: {}", ctx.getText());
		return null;
	}

	@Override
	public AstNode visitExp1(MiniJajaParser.Exp1Context ctx) {
        logger.debug("enter visitExp1: text='{}'", ctx.getText());
		if (ctx.exp1() != null && ctx.exp2() != null) {
			Expression exp1 = (Expression) visit(ctx.exp1());
			Expression exp2 = (Expression) visit(ctx.exp2());
			if (ctx.EQQ() != null)
				return new EqualsNode(exp1, exp2);
			if (ctx.SUP() != null)
				return new GreaterThanNode(exp1, exp2);
		}
		if (ctx.exp2() != null)
			return visit(ctx.exp2());
        logger.warn("[visitExp1] Unexpected expression: {}", ctx.getText());
		return null;
	}

	@Override
	public AstNode visitExp2(MiniJajaParser.Exp2Context ctx) {
        logger.debug("enter visitExp2: text='{}'", ctx.getText());
		if (ctx.exp2() != null && ctx.terme() != null) {
			Expression exp2 = (Expression) visit(ctx.exp2());
			Expression terme = (Expression) visit(ctx.terme());
			if (ctx.PLUS() != null)
				return new PlusNode(exp2, terme);
			if (ctx.MINUS() != null)
				return new MinusNode(exp2, terme);
		}
		if (ctx.terme() != null && ctx.MINUS() != null)
			return new UnaryMinusNode((Expression) visit(ctx.terme()));
		if (ctx.terme() != null)
			return visit(ctx.terme());
        logger.warn("[visitExp2] Unexpected expression: {}", ctx.getText());
		return null;
	}

	@Override
	public AstNode visitTerme(MiniJajaParser.TermeContext ctx) {
        logger.debug("enter visitTerme: text='{}'", ctx.getText());
		if (ctx.fact() != null && ctx.terme() != null) {
			Expression fact = (Expression) visit(ctx.fact());
			Expression terme = (Expression) visit(ctx.terme());
			if (ctx.MULT() != null)
				return new MultiplicationNode(terme, fact);
			if (ctx.DIV() != null)
				return new DivisionNode(terme, fact);
		}
		if (ctx.fact() != null)
			return visit(ctx.fact());
        logger.warn("[visitTerme] Unexpected term: {}", ctx.getText());
		return null;
	}

	// ======== FACTORS & IDENTIFIERS ========

	@Override
	public AstNode visitFact(MiniJajaParser.FactContext ctx) {
        logger.debug("enter visitFact: text='{}'", ctx.getText());
		if (ctx.ident1() != null)
			return visit(ctx.ident1());

		if (ctx.IDENT() != null) {
			IdentNode ident = new IdentNode(ctx.IDENT().getText());
			if (ctx.LENGTH() != null)
				return new LengthNode(ident);
			if (ctx.listexp() != null)
				return new AppelENode(ident, (ListExpNode) visit(ctx.listexp()));
		}

		if (ctx.exp() != null)
			return visit(ctx.exp());
		if (ctx.NBRE() != null)
			return new NbreNode(Integer.parseInt(ctx.NBRE().getText()));
		if (ctx.BOOLEAN() != null)
			return new BoolValueNode(Boolean.parseBoolean(ctx.BOOLEAN().getText()));

        logger.warn("[visitFact] Unexpected fact node: {}", ctx.getText());
		return null;
	}

	@Override
	public AstNode visitIdent1(MiniJajaParser.Ident1Context ctx) {
        logger.debug("enter visitIdent1: text='{}'", ctx.getText());
		IdentNode ident = new IdentNode(ctx.IDENT().getText());
		if (ctx.exp() != null) {
			Expression index = (Expression) visit(ctx.exp());
			return new TabNode(ident, index);
		}
		return ident;
	}

	@Override
	public AstNode visitListexp(MiniJajaParser.ListexpContext ctx) {
        logger.debug("enter visitListexp: text='{}'", ctx.getText());

		if (ctx.exp() == null) {
			return new ListExpNode(null, null); // Nœud "exnil"
		}

		Expression exp = (Expression) visit(ctx.exp());

		if (ctx.listexp() != null) {
			ListExpNode next = (ListExpNode) visit(ctx.listexp());
			return new ListExpNode(exp, next);
		}
		return new ListExpNode(exp, null);

	}


	/**
	 * Résultat par défaut si une méthode 'visit' n'est pas implémentée.
	 */
	@Override
	protected AstNode defaultResult() {
		logger.debug("defaultResult called");
		throw new UnsupportedOperationException("Visite non implémentée pour ce nœud.");
		
	}
}
