package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja;

import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.lexerparser.gen.minijaja.MiniJajaParserBaseVisitor;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.AstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.IncrementNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.LengthNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Terme.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Terme.MultiplicationNode;
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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;

/**
 * MiniJajaVisitor builds the Abstract Syntax Tree (AST) from the ANTLR parse
 * tree.
 * Each visit method creates corresponding AST nodes for the MiniJaja language.
 */
public class MiniJajaInterpreterVisitor extends MiniJajaParserBaseVisitor<AstNode> {

	private final Stacks stacks;

	public MiniJajaInterpreterVisitor(Stacks stacks) {
		this.stacks = stacks;
	}
	// ======== CLASS ========

	@Override
	public AstNode visitClasse(MiniJajaParser.ClasseContext ctx) {
		String className = ctx.IDENT().getText();
		IdentNode ident = new IdentNode(className);

		DeclsNode decls = (DeclsNode) visit(ctx.decls());
		MainNode main = (MainNode) visit(ctx.methmain());

		return new ClasseNode(ident, decls, main);
	}

	// ======== DECLARATIONS ========

	@Override
	public AstNode visitDecls(MiniJajaParser.DeclsContext ctx) {
		// Base case: no more declarations
		if (ctx.decl() == null) {
			return new DeclsNode(); // empty declaration list
		}

		VarNode firstDecl = (VarNode) visit(ctx.decl());
		DeclsNode nextDecls = (DeclsNode) visit(ctx.decls());
		return new DeclsNode(firstDecl, nextDecls);
	}

	@Override
	public AstNode visitDecl(MiniJajaParser.DeclContext ctx) {
		return visit(ctx.var());
	}

	@Override
	public AstNode visitVexp(MiniJajaParser.VexpContext ctx) {
		return ctx.exp() == null ? null : visit(ctx.exp());
	}

	// ======== MAIN METHOD ========

	@Override
	public AstNode visitMethmain(MiniJajaParser.MethmainContext ctx) {
		VarsNode vars = (VarsNode) visit(ctx.vars());
		InstructionsNode instructions = (InstructionsNode) visit(ctx.instrs());
		return new MainNode(vars, instructions);
	}

	// ======== VARIABLES ========

	@Override
	public AstNode visitVar(MiniJajaParser.VarContext ctx) {
		String type = "int";// TODO: FIX TYPEMETH
		IdentNode ident = new IdentNode(ctx.IDENT().getText());
		Expression vexp = (Expression) visit(ctx.vexp());
		if (vexp == null) {
			stacks.declareVar(ident.getNom(), 0, "int");
			return new VarNode(type, ident, null);
		}

		stacks.declareVar(ident.getNom(), vexp.getValue(), type);

		return new VarNode(type, ident, vexp);

		// TODO : AJOUTER LES AUTRES DECLARATIONS DE VARIABLE }
	}

	@Override
	public AstNode visitVars(MiniJajaParser.VarsContext ctx) {
		if (ctx.children == null)
			return new VarsNode();
		VarNode firstVar = (VarNode) visit(ctx.var());
		VarsNode nextVars = (VarsNode) visit(ctx.vars());
		return new VarsNode(firstVar, nextVars);
	}

	// ======== INSTRUCTIONS ========

	@Override
	public AstNode visitInstrs(MiniJajaParser.InstrsContext ctx) {

		if (ctx.instr() == null) {
			return new InstructionsNode(); // Empty list (Inil)
		}

		InstructionNode firstInstr = (InstructionNode) visit(ctx.instr());

		// ========= Interpretation de l'affectation ================
		if (firstInstr instanceof AffectationNode affectation) {
			IdentNode IdentNode = (IdentNode) affectation.getIdent1Node();
			String variableName = IdentNode.getNom();
			Object value = affectation.getExpression().getValue();
			stacks.AffecterVal(variableName, value);
		}
		// ==========================================================

		InstructionsNode next = (InstructionsNode) visit(ctx.instrs());

		return new InstructionsNode(firstInstr, next);
	}

	@Override
	public AstNode visitInstr(MiniJajaParser.InstrContext ctx) {
		// IF statement
		if (ctx.IF() != null) {
			AstNode condition = visit(ctx.exp());
			InstructionsNode thenBlock = (InstructionsNode) visit(ctx.instrs(0));
			InstructionsNode elseBlock = ctx.ELSE() != null ? (InstructionsNode) visit(ctx.instrs(1)) : null;
			return new SiNode(condition, thenBlock, elseBlock);
		}

		// WHILE loop
		if (ctx.WHILE() != null) {
			AstNode condition = visit(ctx.exp());
			InstructionsNode loopBody = (InstructionsNode) visit(ctx.instrs(0));
			return new TantqueNode(condition, loopBody);
		}

		// RETURN statement
		if (ctx.RETURN() != null) {
			return new RetourNode(visit(ctx.exp()));
		}

		// Affectation, increment, or addition on identifiers
		if (ctx.ident1() != null) {
			AstNode ident = visit(ctx.ident1());
			if (ctx.EQ() != null)
				return new AffectationNode(ident, (Expression) visit(ctx.exp()));
			if (ctx.SOMME() != null)
				return new SommeNode(ident, visit(ctx.exp()));
			if (ctx.INCREMENT() != null)
				return new IncrementNode(ident);
		}

		throw new IllegalStateException("Unhandled instruction: " + ctx.getText());
	}

	// ======== EXPRESSIONS ========

	@Override
	public AstNode visitExp(MiniJajaParser.ExpContext ctx) {
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

		System.err.println("[visitExp] Unexpected expression: " + ctx.getText());
		return null;
	}

	@Override
	public AstNode visitExp1(MiniJajaParser.Exp1Context ctx) {
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
		System.err.println("[visitExp1] Unexpected expression: " + ctx.getText());
		return null;
	}

	@Override
	public AstNode visitExp2(MiniJajaParser.Exp2Context ctx) {
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
		System.err.println("[visitExp2] Unexpected expression: " + ctx.getText());
		return null;
	}

	@Override
	public AstNode visitTerme(MiniJajaParser.TermeContext ctx) {
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
		System.err.println("[visitTerme] Unexpected term: " + ctx.getText());
		return null;
	}

	// ======== FACTORS & IDENTIFIERS ========

	@Override
	public AstNode visitFact(MiniJajaParser.FactContext ctx) {
		if (ctx.ident1() != null)
			return visit(ctx.ident1());

		if (ctx.IDENT() != null) {
			IdentNode ident = new IdentNode(ctx.IDENT().getText());
			if (ctx.LENGTH() != null)
				return new LengthNode(ident);
			if (ctx.listexp() != null)
				return new AppelENode(ident, visit(ctx.listexp()));
		}

		if (ctx.exp() != null)
			return visit(ctx.exp());
		if (ctx.NBRE() != null)
			return new NbreNode(Integer.parseInt(ctx.NBRE().getText()));
		if (ctx.BOOLEAN() != null)
			return new BoolValueNode(Boolean.parseBoolean(ctx.BOOLEAN().getText()));

		System.err.println("[visitFact] Unexpected fact node: " + ctx.getText());
		return null;
	}

	@Override
	public AstNode visitIdent1(MiniJajaParser.Ident1Context ctx) {
		IdentNode id = new IdentNode(ctx.IDENT().getText());
		if (ctx.exp() != null)
			return new TabNode(id, visit(ctx.exp()));
		return id;
	}

	@Override
	public AstNode visitListexp(MiniJajaParser.ListexpContext ctx) {

		if (ctx.exp() == null) {
			return new ListExpNode(null, null); // Nœud "exnil"
		}

		AstNode exp = visit(ctx.exp());

		if (ctx.listexp() != null) {
			ListExpNode next = (ListExpNode) visit(ctx.listexp());
			return new ListExpNode(exp, next);
		}
		return new ListExpNode(exp, null);

	}

	// ======== TYPE METHODS ========

	// @Override
	// public AstNode visitTypemeth(MiniJajaParser.TypemethContext ctx) {
	// if (ctx.TYPE() != null) {
	// return ctx.TYPE().getText();
	// } else {
	// return "void";
	// }
	//
	// }

	/**
	 * Résultat par défaut si une méthode 'visit' n'est pas implémentée.
	 */
	@Override
	protected AstNode defaultResult() {
		return null;
	}
}
