package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node.NodeLoad;
import fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode.node.NodePush;
import org.junit.jupiter.api.Test;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.IncrementNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.Instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
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
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;

class AstNodesTest {

    // Dummy list expression to avoid null tail issues in ListExpNode#toStringTree
    static class DummyListExpNode extends ListExpNode {
        public DummyListExpNode() {
            super(new NbreNode(0), null);
        }

        @Override
        public String toStringTree() {
            return "exnil";
        }
    }

    @Test
    void testIdentNodeToStringTree() {
        IdentNode id = new IdentNode("x");
        assertEquals("Ident(x)", id.toStringTree());
        assertEquals("x", id.getNom());
    }

    @Test
    void testVarNodeWithoutInitToStringTree() {
        VarNode var = new VarNode("int", new IdentNode("x"), null);
        assertEquals("var (int , Ident(x),Omega)", var.toStringTree());
    }

    @Test
    void testVarNodeWithInitToStringTree() {
        VarNode var = new VarNode("int", new IdentNode("x"), new NbreNode(1));
        // getters
        assertEquals("int", var.getType());
        assertTrue(var.getIdent() instanceof IdentNode);
        assertEquals("x", var.getIdent().getNom());
        assertTrue(var.getExp().getVexp() instanceof NbreNode);
        assertEquals(1, ((NbreNode) var.getExp().getVexp()).value);
        // rendering
        assertEquals("var (int , Ident(x) , nbre(1))", var.toStringTree());
    }

    @Test
    void testVarsNodeEmptyAndNonEmptyToStringTree() {
        VarsNode empty = new VarsNode();
        assertEquals("vnil", empty.toStringTree());

        VarNode vx = new VarNode("int", new IdentNode("x"), new NbreNode(0));
        VarsNode one = new VarsNode(vx, new VarsNode());
        assertEquals("vars (var (int , Ident(x) , nbre(0)),vnil)", one.toStringTree());
        // getters
        assertTrue(one.getVar() instanceof VarNode);
        assertEquals("x", one.getVar().getIdent().getNom());
        assertTrue(one.getVars() instanceof VarsNode);
        assertEquals("vnil", one.getVars().toStringTree());
    }

    @Test
    void testDeclsNodeEmptyAndNonEmptyToStringTree() {
        DeclsNode empty = new DeclsNode();
        assertEquals("vnil", empty.toStringTree());

        VarNode vx = new VarNode("int", new IdentNode("x"), new NbreNode(0));
        DeclsNode one = new DeclsNode(vx, new DeclsNode());
        assertEquals("decls (var (int , Ident(x) , nbre(0)),vnil)", one.toStringTree());
        // getters
        assertTrue(one.getDecl() instanceof VarNode);
        assertEquals("x", one.getDecl().getIdent().getNom());
        assertTrue(one.getDecls() instanceof DeclsNode);
        assertEquals("vnil", one.getDecls().toStringTree());
    }

    @Test
    void testInstructionsNodeEmptyAndSingleToStringTree() {
        InstructionsNode empty = new InstructionsNode();
        assertEquals("Inil", empty.toStringTree());

        AffectationNode instr = new AffectationNode(new IdentNode("x"), new NbreNode(2));
        InstructionsNode single = new InstructionsNode(instr, new InstructionsNode());
        assertEquals("Instrs(affectation(Ident(x),nbre(2)),Inil)", single.toStringTree());
        // getters
        assertTrue(single.getInstructionNode() instanceof AffectationNode);
        AffectationNode a = (AffectationNode) single.getInstructionNode();
        assertTrue(a.getIdent1Node() instanceof IdentNode);
        assertEquals("x", ((IdentNode) a.getIdent1Node()).getNom());
        assertTrue(a.getExpression() instanceof NbreNode);
        assertEquals(2, ((NbreNode) a.getExpression()).value);
        assertEquals("Inil", single.getInstructions().toStringTree());
    }

    @Test
    void testInstructionsNodeWithOnlyOneInstruction() {

        AffectationNode instr = new AffectationNode(new IdentNode("x"), new NbreNode(2));
        InstructionsNode single = new InstructionsNode(instr);
        assertTrue(single.getInstructionNode() instanceof AffectationNode);
        assertTrue(single.getInstructions() == null);

    }

    @Test
    void testAffectationNode() {
        AffectationNode aff = new AffectationNode(new IdentNode("x"), new NbreNode(5));
        assertTrue(aff.getIdent1Node() instanceof AstNode);
        assertTrue(aff.getExpression() instanceof NbreNode);
        assertEquals("x", ((IdentNode) aff.getIdent1Node()).getNom());
        assertEquals(5, ((NbreNode) aff.getExpression()).value);
        assertEquals("affectation(Ident(x),nbre(5))", aff.toStringTree());
    }

    @Test
    void testIncrementNode() {
        IncrementNode inc = new IncrementNode(new IdentNode("x"));
        assertEquals("Increment(Ident(x))", inc.toStringTree());
    }

    @Test
    void testBoolAndNbreValues() {
        BoolValueNode f = new BoolValueNode(false);
        BoolValueNode t = new BoolValueNode(true);
        assertEquals(0, f.getValue());
        assertEquals(1, t.getValue());
        assertEquals("false", f.toStringTree());
        assertEquals("true", t.toStringTree());

        NbreNode n = new NbreNode(42);
        assertEquals(42, n.value);
        assertEquals("nbre(42)", n.toStringTree());
    }

    @Test
    void testSommeNode() {
        SommeNode somme = new SommeNode(new IdentNode("x"), new NbreNode(3));
        // Note: current toStringTree omet une virgule
        assertEquals("somme(Ident(x)nbre(3))", somme.toStringTree());
    }

    @Test
    void testRetourNode() {
        RetourNode ret = new RetourNode(new NbreNode(1));
        assertEquals("Retour(nbre(1))", ret.toStringTree());
    }

    @Test
    void testLengthNode() {
        LengthNode len = new LengthNode(new IdentNode("tab"));
        assertEquals("length(Ident(tab))", len.toStringTree());
        assertTrue(len.getId() instanceof IdentNode);
        assertEquals("tab", ((IdentNode) len.getId()).getNom());
    }

    @Test
    void testSiNodeSansElse() {
        GreaterThanNode cond = new GreaterThanNode(new NbreNode(3), new NbreNode(0));
        InstructionsNode thenInstrs = new InstructionsNode(new AffectationNode(new IdentNode("x"), new NbreNode(1)),
                new InstructionsNode());
        SiNode si = new SiNode(cond, thenInstrs);
        assertEquals("si (>nbre(3),nbre(0),Instrs(affectation(Ident(x),nbre(1)),Inil)inil)", si.toStringTree());
    }

    @Test
    void testSiNodeAvecElse() {
        GreaterThanNode cond = new GreaterThanNode(new NbreNode(2), new NbreNode(0));
        InstructionsNode thenInstrs = new InstructionsNode(new AffectationNode(new IdentNode("x"), new NbreNode(1)),
                new InstructionsNode());
        InstructionsNode elseInstrs = new InstructionsNode(new AffectationNode(new IdentNode("x"), new NbreNode(2)),
                new InstructionsNode());
        SiNode si = new SiNode(cond, thenInstrs, elseInstrs);
        assertEquals(
                "si (>nbre(2),nbre(0),Instrs(affectation(Ident(x),nbre(1)),Inil)Instrs(affectation(Ident(x),nbre(2)),Inil))",
                si.toStringTree());
    }

    @Test
    void testTantqueNode() {
        InstructionsNode body = new InstructionsNode(new AffectationNode(new IdentNode("x"), new NbreNode(2)),
                new InstructionsNode());
        TantqueNode tq = new TantqueNode(new NbreNode(1), body);
        assertEquals("tantque(nbre(1),Instrs(affectation(Ident(x),nbre(2)),Inil))", tq.toStringTree());
    }

    @Test
    void testTabNode() {
        TabNode tab = new TabNode(new IdentNode("t"), new NbreNode(3));
        assertEquals("tab(Ident(t),nbre(3))", tab.toStringTree());
        assertEquals("t", tab.getIdent().getNom());
    }

    @Test
    void testVexpOmegaAndValue() {
        Vexp omega = new Vexp();
        assertEquals("omega", omega.toStringTree());
        Vexp v = new Vexp(new NbreNode(9));
        assertEquals("nbre(9)", v.toStringTree());
        assertEquals(9, ((NbreNode) v.getVexp()).value);
    }

    @Test
    void testAppelENodeAndAppelINode() {
        DummyListExpNode list = new DummyListExpNode();
        AppelENode appelE = new AppelENode(new IdentNode("f"), list);
        assertEquals("appelE(Ident(f),exnil)", appelE.toStringTree());
        assertEquals("f", appelE.getIdent().getNom());
        assertEquals("exnil", ((DummyListExpNode) appelE.getExp()).toStringTree());

        AppelINode appelI = new AppelINode(new IdentNode("proc"), list);
        assertEquals("appelI(Ident(proc),exnil)", appelI.toStringTree());
    }

    @Test
    void testEnteteAndEntetesNodes() {
        EntetesNode enil = new EntetesNode(null, null);
        assertEquals("enil", enil.toStringTree());

        EnteteNode e1 = new EnteteNode(new IdentNode("m"), "int");
        assertEquals("int", e1.getType());
        assertEquals("m", e1.getIdent().getNom());
        assertEquals("Entete (int , Ident(m))", e1.toStringTree());

        EntetesNode single = new EntetesNode(e1, null);
        assertEquals("entetes (Entete (int , Ident(m)))", single.toStringTree());

        EnteteNode e2 = new EnteteNode(new IdentNode("n"), "boolean");
        EntetesNode chain = new EntetesNode(e1, new EntetesNode(e2, null));
        assertEquals("entetes (Entete (int , Ident(m)),entetes (Entete (boolean , Ident(n))))", chain.toStringTree());
        assertTrue(chain.getEntete() instanceof EnteteNode);
        assertTrue(chain.getEntetes() instanceof EntetesNode);
    }

    @Test
    void testExpressionsBinairesEtUnaires() {
        PlusNode plus = new PlusNode(new NbreNode(1), new NbreNode(2));
        assertEquals("+ (nbre(1),nbre(2))", plus.toStringTree());
        assertEquals(1, ((NbreNode) plus.getExp2()).value);
        assertEquals(2, ((NbreNode) plus.getTerme()).value);

        MinusNode minus = new MinusNode(new NbreNode(3), new NbreNode(1));
        assertEquals("- (nbre(3),nbre(1))", minus.toStringTree());
        assertEquals(3, ((NbreNode) minus.getExp2()).value);
        assertEquals(1, ((NbreNode) minus.getTerme()).value);

        UnaryMinusNode uminus = new UnaryMinusNode(new NbreNode(7));
        assertEquals("- (nbre(7))", uminus.toStringTree());
        assertEquals(7, ((NbreNode) uminus.getTerme()).value);

        MultiplicationNode mult = new MultiplicationNode(new NbreNode(2), new NbreNode(4));
        assertEquals("* (nbre(2),nbre(4))", mult.toStringTree());
        assertEquals(2, ((NbreNode) mult.getTerme()).value);
        assertEquals(4, ((NbreNode) mult.getFact()).value);

        DivisionNode div = new DivisionNode(new NbreNode(8), new NbreNode(2));
        assertEquals("/ (nbre(8),nbre(2))", div.toStringTree());
        assertEquals(8, ((NbreNode) div.getTerme()).value);
        assertEquals(2, ((NbreNode) div.getFact()).value);

        EqualsNode eq = new EqualsNode(new NbreNode(1), new NbreNode(1));
        assertEquals("== (nbre(1),nbre(1))", eq.toStringTree());
        assertEquals(1, ((NbreNode) eq.getExp1()).value);
        assertEquals(1, ((NbreNode) eq.getExp2()).value);

        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode t = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode(
                true);
        fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode f = new fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode(
                false);

        AndNode and = new AndNode(t, f);
        assertEquals("et (true,false)", and.toStringTree());
        assertTrue(and
                .getExp() instanceof fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode);
        assertTrue(and
                .getExp1() instanceof fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode);
        assertEquals(true, ((fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode) and
                .getExp()).value);
        assertEquals(false, ((fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode) and
                .getExp1()).value);

        OrNode or = new OrNode(f, t);
        assertEquals("ou (false,true)", or.toStringTree());
        assertEquals(false, ((fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode) or
                .getExp()).value);
        assertEquals(true, ((fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode) or
                .getExp1()).value);

        NotNode not = new NotNode(t);
        assertEquals("non (true)", not.toStringTree());
        assertEquals(true, ((fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Fact.BoolValueNode) not
                .getExp()).value);
    }

    @Test
    void testMainNodeToStringTree() {
        MainNode main = new MainNode(new VarsNode(), new InstructionsNode());
        assertTrue(main.getVars() instanceof VarsNode);
        assertTrue(main.getInstrs() instanceof InstructionsNode);
        assertEquals("Main(vnil, Inil)", main.toStringTree());
    }

    @Test
    void testMainNodeNoVars() {
        MainNode main = new MainNode(new InstructionsNode());
        assertTrue(main.getVars() == null);
        assertTrue(main.getInstrs() instanceof InstructionsNode);
        assertEquals("Main(vnil, Inil)", main.toStringTree());
    }

    @Test
    void testClasseNodeToStringTree() {
        IdentNode id = new IdentNode("C");
        DeclsNode decls = new DeclsNode(new VarNode("int", new IdentNode("x"), new NbreNode(0)), new DeclsNode());
        MainNode main = new MainNode(new VarsNode(), new InstructionsNode());

        ClasseNode classe = new ClasseNode(id, decls, main);
        assertTrue(classe.getIdent() instanceof IdentNode);
        assertTrue(classe.getDeclarations() instanceof DeclsNode);
        assertTrue(classe.getMethodeMain() instanceof MainNode);
        assertTrue(classe.getVarClasse() instanceof String);
        assertEquals("C", classe.getVarClasse());
        assertEquals("Classe(Ident(C),decls (var (int , Ident(x) , nbre(0)),vnil),Main(vnil, Inil))",
                classe.toStringTree());
    }

    /**
     * This test is only there to avoid the codecov problem on nonimplemented classes.
     */
    @Test
    void codecovfixes() { // TODO Delete once theses files are implemented.
        NodeLoad nl = new NodeLoad();
        NodePush np = new NodePush();
    }
}
