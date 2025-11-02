package fr.ufrst.m1info.gl.groupe7.LexerParser.minijaja;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParser;
import fr.ufrst.m1info.gl.groupe7.LexerParser.gen.minijaja.MiniJajaParserBaseListener;


/**
 * A listener that builds a pretty-printed trace of the parse tree as the parser
 * walks it. It records enter/exit events and token text for terminal nodes.
 */
public class MiniJajaListener extends MiniJajaParserBaseListener {


	@Override
	public void enterClasse(MiniJajaParser.ClasseContext ctx) {
		

	}

	@Override
	public void exitClasse(MiniJajaParser.ClasseContext ctx) {
		// fermer scope classe
	

	}

	@Override
	public void enterMethode(MiniJajaParser.MethodeContext ctx) {
	
	}

	@Override
	public void exitMethode(MiniJajaParser.MethodeContext ctx) {
		// fermeture scope méthode

	}

	@Override
	public void enterMethmain(MiniJajaParser.MethmainContext ctx) {
		
	
	}

	@Override
	public void exitMethmain(MiniJajaParser.MethmainContext ctx) {

	}

	@Override
	public void enterVar(MiniJajaParser.VarContext ctx) {

	}

	@Override
	public void exitTypemeth(MiniJajaParser.TypemethContext ctx) {
	}

	
	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	}

	
	@Override
	public void visitTerminal(TerminalNode node) {
	}
}

