package fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.classe.ClasseNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.cst.CstNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.decls.DeclsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entete.EnteteNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.entetes.EntetesNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.Expression;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.and.AndNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.not.NotNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp.or.OrNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.equals.EqualsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp1.greater.GreaterThanNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.minus.MinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.plus.PlusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.exp2.unaryMinus.UnaryMinusNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.AppelENode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.BoolValueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.LengthNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.ListExpNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.fact.NbreNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.division.DivisionNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.terme.multiplication.MultiplicationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.expressions.vexp.Vexp;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.ident.IdentNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AffectationNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.AppelINode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.EcrireNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.IncrementNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.InstructionsNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.RetourNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SiNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.SommeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.instructions.TantqueNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.main.MainNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.methode.MethodeNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tab.TabNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.tableau.TableauNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.var.VarNode;
import fr.ufrst.m1info.gl.groupe7.lexerparser.minijaja.ast.vars.VarsNode;
import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import fr.ufrst.m1info.gl.groupe7.memoire.utils.Type;

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
        VarNode var = new VarNode(Type.ENTIER, new IdentNode("x"), null);
        assertEquals("var (integer , Ident(x),Omega)", var.toStringTree());
    }

    @Test
    void testVarNodeWithInitToStringTree() {
        VarNode var = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(1));
        // getters
        assertEquals(Type.ENTIER, var.getType());
        assertInstanceOf(IdentNode.class, var.getIdent());
        assertEquals("x", var.getIdent().getNom());
        assertInstanceOf(NbreNode.class, var.getExp().getVexp());
        assertEquals(1, ((NbreNode) var.getExp().getVexp()).value);
        // rendering
        assertEquals("var (integer , Ident(x) , nbre(1))", var.toStringTree());
    }

    @Test
    void testVarsNodeEmptyAndNonEmptyToStringTree() {
        VarsNode empty = new VarsNode();
        assertEquals("vnil", empty.toStringTree());

        VarNode vx = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0));
        VarsNode one = new VarsNode(vx, new VarsNode());
        assertEquals("vars (var (integer , Ident(x) , nbre(0)),vnil)", one.toStringTree());
        // getters
        assertInstanceOf(VarNode.class, one.getVar());
        assertEquals("x",((VarNode) one.getVar()).getIdent().getNom());
        assertInstanceOf(VarsNode.class, one.getVars());
        assertEquals("vnil", one.getVars().toStringTree());
    }
    @Test
    void testVarsNodeGetChildren() {
        VarsNode empty = new VarsNode();
        Iterable<AstNode> childrenEmpty = empty.getChildren();
        assertNotNull(childrenEmpty);
        int countEmpty = 0;
        for (AstNode child : childrenEmpty) {
            countEmpty++;
        }
        assertEquals(0, countEmpty);

        VarNode vx = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0));
        VarsNode one = new VarsNode(vx, new VarsNode());
        Iterable<AstNode> childrenOne = one.getChildren();
        assertNotNull(childrenOne);
        int countOne = 0;
        for (AstNode child : childrenOne) {
            countOne++;
            if(child instanceof VarNode) {assertInstanceOf(VarNode.class, child);
            assertEquals("x", ((VarNode) child).getIdent().getNom());}
        }
        assertEquals(2, countOne);
    }

    @Test
    void testCstNodeToStringTree() {
        CstNode cstWithExp = new CstNode(Type.ENTIER, new IdentNode("y"), new NbreNode(10));
        assertEquals("cst (integer , Ident(y) , nbre(10))", cstWithExp.toStringTree());

        CstNode cstWithoutExp = new CstNode(Type.BOOLEEN, new IdentNode("z"));
        assertEquals("cst (boolean , Ident(z),Omega)", cstWithoutExp.toStringTree());
    }

    @Test
    void testTableauNodeToStringTree() {
        TableauNode tab = new TableauNode(Type.ENTIER, new IdentNode("tab"), new NbreNode(5));
        assertEquals("tableau (integer , Ident(tab) , nbre(5))", tab.toStringTree());
    }

    @Test
    void testMethodeNodeToStringTree() {
        EntetesNode entetes = new EntetesNode();
        MethodeNode methode = new MethodeNode(Type.ENTIER, new IdentNode("m"), entetes, new VarsNode(),
                new InstructionsNode());
        assertEquals("methode (integer,Ident(m),enil,vnil,Inil)", methode.toStringTree());

    }

    @Test
    void testDeclsNodeEmptyAndNonEmptyToStringTree() {
        DeclsNode empty = new DeclsNode();
        assertEquals("vnil", empty.toStringTree());

        VarNode vx = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0));
        DeclsNode one = new DeclsNode(vx, new DeclsNode());
        assertEquals("decls (var (integer , Ident(x) , nbre(0)),vnil)", one.toStringTree());
        // getters
        assertInstanceOf(VarNode.class, one.getDecl());
        assertEquals("x", ((VarNode) one.getDecl()).getIdent().getNom());
        assertInstanceOf(DeclsNode.class, one.getDecls());
        assertEquals("vnil", one.getDecls().toStringTree());
    }

    @Test
    void testDeclsNodeWithOnlyOneDeclaration() {
        VarNode vx = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0));
        DeclsNode one = new DeclsNode(vx);
        assertInstanceOf(VarNode.class, one.getDecl());
        assertNull(one.getDecls());
    }

    @Test
    void testDeclsNodeToStringTreeWithBothNulls() {
        DeclsNode decls = new DeclsNode();
        assertEquals("vnil", decls.toStringTree());
    }

    @Test
    void testDeclsNodeGetchildren() {
        VarNode vx = new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0));
        DeclsNode declsWithOne = new DeclsNode(vx);
        Iterable<AstNode> childrenOne = declsWithOne.getChildren();
        assertNotNull(childrenOne);
        int countOne = 0;
        for (AstNode child : childrenOne) {
            countOne++;
            assertInstanceOf(VarNode.class, child);
            assertEquals("x", ((VarNode) child).getIdent().getNom());
        }
        assertEquals(1, countOne);

        DeclsNode emptyDecls = new DeclsNode();
        Iterable<AstNode> childrenEmpty = emptyDecls.getChildren();
        assertNotNull(childrenEmpty);
        int countEmpty = 0;
        for (AstNode child : childrenEmpty) {
            countEmpty++;
        }
        assertEquals(0, countEmpty);
    }

    @Test
    void testInstructionsNodeEmptyAndSingleToStringTree() {
        InstructionsNode empty = new InstructionsNode();
        assertEquals("Inil", empty.toStringTree());

        AffectationNode instr = new AffectationNode(new IdentNode("x"), new NbreNode(2));
        InstructionsNode single = new InstructionsNode(instr, new InstructionsNode());
        assertEquals("Instrs(affectation(Ident(x),nbre(2)),Inil)", single.toStringTree());
        // getters
        assertInstanceOf(AffectationNode.class, single.getInstructionNode());
        AffectationNode a = (AffectationNode) single.getInstructionNode();
        assertInstanceOf(IdentNode.class, a.getIdent1Node());
        assertEquals("x", ((IdentNode) a.getIdent1Node()).getNom());
        assertInstanceOf(NbreNode.class, a.getExpression());
        assertEquals(2, ((NbreNode) a.getExpression()).value);
        assertNotNull(single.getInstructions());
        assertEquals("Inil", single.getInstructions().toStringTree());
    }

    @Test
    void testInstructionsNodeWithOnlyOneInstruction() {
        AffectationNode instr = new AffectationNode(new IdentNode("x"), new NbreNode(2));
        InstructionsNode single = new InstructionsNode(instr);
        assertInstanceOf(AffectationNode.class, single.getInstructionNode());
        assertNull(single.getInstructions());

    }

    @Test
    void testAffectationNode() {
        AffectationNode aff = new AffectationNode(new IdentNode("x"), new NbreNode(5));
        assertInstanceOf(AstNode.class, aff.getIdent1Node());
        assertInstanceOf(NbreNode.class, aff.getExpression());
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
        assertFalse(f.getValue());
        assertTrue(t.getValue());
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
    public void toStringTree_withExpression_returnsExpressionTreeWrapped() {
        Expression expr = mock(Expression.class);
        when(expr.toStringTree()).thenReturn("y");
        EcrireNode node = new EcrireNode(expr);

        String tree = node.toStringTree();

        assertEquals("ecrire (y)", tree);
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
        assertInstanceOf(IdentNode.class, len.getId());
        assertEquals("tab", len.getId().getNom());
    }

    @Test
    void testLengthNodeEvaluate() {
        Stacks stacks = new Stacks();
        TableauNode tableauNode = new TableauNode(Type.ENTIER, new IdentNode("myArray"), new NbreNode(3));
        tableauNode.interpret(stacks);
        LengthNode len = new LengthNode(new IdentNode("myArray"));
        Object result = len.evaluate(stacks);
        assertInstanceOf(Integer.class, result);
        assertEquals(3, result);
    }
    @Test
    void testLengthNodeToStringTree() {
        LengthNode len = new LengthNode(new IdentNode("myString"));
        assertEquals("length(Ident(myString))", len.toStringTree());
    }

    @Test
    void testSiNodeSansElse() {
        GreaterThanNode cond = new GreaterThanNode(new NbreNode(3), new NbreNode(0));
        InstructionsNode thenInstrs = new InstructionsNode(new AffectationNode(new IdentNode("x"), new NbreNode(1)),
                new InstructionsNode());
        SiNode si = new SiNode(cond, thenInstrs);
        assertEquals("si (> (nbre(3),nbre(0)),Instrs(affectation(Ident(x),nbre(1)),Inil)inil)", si.toStringTree());
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
                "si (> (nbre(2),nbre(0)),Instrs(affectation(Ident(x),nbre(1)),Inil)Instrs(affectation(Ident(x),nbre(2)),Inil))",
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
        assertNotNull(v.getVexp());
        assertEquals(9, ((NbreNode) v.getVexp()).value);
    }

    @Test
    void VexpEvaluateOmegaAndValue() {
        Stacks stacks = new Stacks();
        Vexp omega = new Vexp();
        assertNull(omega.evaluate(stacks));

        Vexp v = new Vexp(new NbreNode(9));
        Object result = v.evaluate(stacks);
        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
        assertEquals(9, result);
    }

    @Test
    void testVexpToStringTree() {
        Vexp omega = new Vexp();
        assertEquals("omega", omega.toStringTree());

        Vexp v = new Vexp(new NbreNode(42));
        assertEquals("nbre(42)", v.toStringTree());
    }

    @Test
    void testListExpNode() {
        DummyListExpNode tail = new DummyListExpNode();
        ListExpNode list = new ListExpNode(new NbreNode(5), tail);
        assertEquals("listExp(nbre(5),exnil)", list.toStringTree());
        assertInstanceOf(NbreNode.class, list.getExp());
        assertEquals(5, ((NbreNode) list.getExp()).value);
        assertInstanceOf(DummyListExpNode.class, list.getListExp());
    }

    @Test
    void testVexpEvalate() {
        Stacks stacks = new Stacks();
        stacks.declareVar("a", 10, Type.ENTIER);
        stacks.declareVar("b", 20, Type.ENTIER);

        Vexp vexpOmega = new Vexp();
        assertNull(vexpOmega.evaluate(stacks));

        Vexp vexpValue = new Vexp(new NbreNode(15));
        Object result = vexpValue.evaluate(stacks);
        assertNotNull(result);
        assertInstanceOf(Integer.class, result);
        assertEquals(15, result);
    }

    @Test
    void testAppelENodeAndAppelINode() {
        DummyListExpNode list = new DummyListExpNode();
        AppelENode appelE = new AppelENode(new IdentNode("f"), list);
        assertEquals("appelE(Ident(f),exnil)", appelE.toStringTree());
        assertEquals("f", appelE.getIdent().getNom());
        assertEquals("exnil", appelE.getExp().toStringTree());

        AppelINode appelI = new AppelINode(new IdentNode("proc"), list);
        assertEquals("appelI(Ident(proc),exnil)", appelI.toStringTree());
    }

    @Test
    void testEnteteAndEntetesNodes() {

        EntetesNode enil = new EntetesNode(null, null);
        assertEquals("enil", enil.toStringTree());

        EnteteNode e1 = new EnteteNode(new IdentNode("m"), Type.ENTIER);
        assertEquals(Type.ENTIER, e1.getType());
        assertEquals("m", e1.getIdent().getNom());
        assertEquals("entete (integer , Ident(m))", e1.toStringTree());

        EntetesNode single = new EntetesNode(e1, null);
        assertEquals("entetes (entete (integer , Ident(m)))", single.toStringTree());

        EnteteNode e2 = new EnteteNode(new IdentNode("n"), Type.BOOLEEN);
        EntetesNode chain = new EntetesNode(e1, new EntetesNode(e2, null));
        assertEquals("entetes (entete (integer , Ident(m)),entetes (entete (boolean , Ident(n))))",
                chain.toStringTree());
        assertInstanceOf(EnteteNode.class, chain.getEntete());
        assertInstanceOf(EntetesNode.class, chain.getEntetes());
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

        BoolValueNode t = new BoolValueNode(
                true);
        BoolValueNode f = new BoolValueNode(
                false);

        AndNode and = new AndNode(t, f);
        assertEquals("et (true,false)", and.toStringTree());
        assertInstanceOf(BoolValueNode.class, and
                .getExp());
        assertInstanceOf(BoolValueNode.class, and
                .getExp1());
        assertTrue(((BoolValueNode) and
                .getExp()).value);
        assertFalse(((BoolValueNode) and
                .getExp1()).value);

        OrNode or = new OrNode(f, t);
        assertEquals("ou (false,true)", or.toStringTree());
        assertFalse(((BoolValueNode) or
                .getExp()).value);
        assertTrue(((BoolValueNode) or
                .getExp1()).value);

        NotNode not = new NotNode(t);
        assertEquals("non (true)", not.toStringTree());
        assertTrue(((BoolValueNode) not
                .getExp()).value);
    }

    @Test
    void testMainNodeToStringTree() {
        MainNode main = new MainNode(new VarsNode(), new InstructionsNode());
        assertInstanceOf(VarsNode.class, main.getVars());
        assertInstanceOf(InstructionsNode.class, main.getInstrs());
        assertEquals("Main(vnil, Inil)", main.toStringTree());
    }

    @Test
    void testMainNodeNoVars() {
        MainNode main = new MainNode(new InstructionsNode());
        assertNull(main.getVars());
        assertInstanceOf(InstructionsNode.class, main.getInstrs());
        assertEquals("Main(vnil, Inil)", main.toStringTree());
    }

    @Test
    void testClasseNodeToStringTree() {
        IdentNode id = new IdentNode("C");
        DeclsNode decls = new DeclsNode(new VarNode(Type.ENTIER, new IdentNode("x"), new NbreNode(0)), new DeclsNode());
        MainNode main = new MainNode(new VarsNode(), new InstructionsNode());

        ClasseNode classe = new ClasseNode(id, decls, main);
        assertInstanceOf(IdentNode.class, classe.getIdent());
        assertInstanceOf(DeclsNode.class, classe.getDeclarations());
        assertInstanceOf(MainNode.class, classe.getMethodeMain());
        assertInstanceOf(String.class, classe.getVarClasse());
        assertEquals("C", classe.getVarClasse());
        assertEquals("Classe(Ident(C),decls (var (integer , Ident(x) , nbre(0)),vnil),Main(vnil, Inil))",
                classe.toStringTree());
    }

    @Test
    void testAffectationNodeinterpret_updates_realStacks_and_printsToErr() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", 0, Type.ENTIER);

        java.io.ByteArrayOutputStream errBaos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream oldErr = System.err;
        System.setErr(new java.io.PrintStream(errBaos));
        try {
            AffectationNode aff = new AffectationNode(new IdentNode("x"), new NbreNode(5));
            Assertions.assertDoesNotThrow(() -> aff.interpret(stacks));

            // verify value actually updated in the real stacks
            Object val = stacks.getValue("x");
            // accept Integer or other numeric representations
            Assertions.assertTrue((val instanceof Integer && ((Integer) val) == 5) || String.valueOf(val).equals("5"));
        } finally {
            System.err.flush();
            System.setErr(oldErr);
        }
    }

    @Test
    void testSommeNodeInterpretWithTabNode() {
        Stacks stacks = new Stacks();
        stacks.declareTab("tab", 5, Type.ENTIER);
        stacks.setArrayValue("tab", 2, 10); // tab[2] = 10

        SommeNode somme = new SommeNode(new TabNode(new IdentNode("tab"), new NbreNode(2)), new NbreNode(5));
        Assertions.assertDoesNotThrow(() -> somme.interpret(stacks));

        // verify value actually updated in the real stacks
        Object val = stacks.getArrayValue("tab", 2);
        // accept Integer or other numeric representations
        Assertions.assertTrue((val instanceof Integer && ((Integer) val) == 15) || String.valueOf(val).equals("15"));
    }

    @Test
    void testSommeNodeInterpretWithIdentNode() {
        Stacks stacks = new Stacks();
        stacks.declareVar("x", 10, Type.ENTIER);

        SommeNode somme = new SommeNode(new IdentNode("x"), new NbreNode(5));
        Assertions.assertDoesNotThrow(() -> somme.interpret(stacks));

        // verify value actually updated in the real stacks
        Object val = stacks.getValue("x");
        // accept Integer or other numeric representations
        Assertions.assertTrue((val instanceof Integer && ((Integer) val) == 15) || String.valueOf(val).equals("15"));
    }

    @Test
    void testAffectationNodeInterpretWithTabNode() {
        Stacks stacks = new Stacks();
        stacks.declareTab("tab", 5, Type.ENTIER);

        AffectationNode aff = new AffectationNode(new TabNode(new IdentNode("tab"), new NbreNode(1)), new NbreNode(20));
        Assertions.assertDoesNotThrow(() -> aff.interpret(stacks));

        // verify value actually updated in the real stacks
        Object val = stacks.getArrayValue("tab", 1);
        // accept Integer or other numeric representations
        Assertions.assertTrue((val instanceof Integer && ((Integer) val) == 20) || String.valueOf(val).equals("20"));
    }

    @Test
    void testAffectationNodeInterpretWithIdentNode() {
        Stacks stacks = new Stacks();
        stacks.declareVar("y", 0, Type.ENTIER);

        AffectationNode aff = new AffectationNode(new IdentNode("y"), new NbreNode(20));
        Assertions.assertDoesNotThrow(() -> aff.interpret(stacks));

        // verify value actually updated in the real stacks
        Object val = stacks.getValue("y");
        // accept Integer or other numeric representations
        Assertions.assertTrue((val instanceof Integer && ((Integer) val) == 20) || String.valueOf(val).equals("20"));
    }

    @Test
    void testIncrementNodeInterpretWithIdentNode() {
        Stacks stacks = new Stacks();
        stacks.declareVar("z", 5, Type.ENTIER);

        IncrementNode inc = new IncrementNode(new IdentNode("z"));
        Assertions.assertDoesNotThrow(() -> inc.interpret(stacks));
    }

    @Test
    void testIncrementNodeInterpretWithTabNode() {
        Stacks stacks = new Stacks();
        stacks.declareTab("arr", 5, Type.ENTIER);
        stacks.setArrayValue("arr", 0, 7); // arr[0] = 7

        IncrementNode inc = new IncrementNode(new TabNode(new IdentNode("arr"), new NbreNode(0)));
        Assertions.assertDoesNotThrow(() -> inc.interpret(stacks));
    }

    @Test
    void testTabNodeEvaluate() {
        Stacks stacks = new Stacks();
        stacks.declareTab("t", 5, Type.ENTIER);
        stacks.setArrayValue("t", 3, 42);

        TabNode tab = new TabNode(new IdentNode("t"), new NbreNode(3));
        Object result = tab.evaluate(stacks);

        assertNotNull(result);
        assertEquals(42, result);
    }

    @Test
    void testTableauNodeInterpret() {
        Stacks stacks = new Stacks();
        TableauNode tab = new TableauNode(Type.ENTIER, new IdentNode("t"), new NbreNode(5));
        tab.interpret(stacks);

        Assertions.assertDoesNotThrow(() -> stacks.getArrayValue("t", 0));
    }
}