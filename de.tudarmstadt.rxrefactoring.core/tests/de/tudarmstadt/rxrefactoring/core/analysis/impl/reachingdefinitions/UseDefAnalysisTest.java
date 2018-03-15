package de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.tudarmstadt.rxrefactoring.core.RxRefactoringTest;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use.Kind;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;

public final class UseDefAnalysisTest extends RxRefactoringTest {

	private Map<ASTNode, UseDef> analyse(Statement statement) {
		UseDefAnalysis analysis = UseDefAnalysis.create();
		return analysis.apply(ProgramGraph.createFrom(statement), analysis.mapExecutor());
	}

	@Test
	public void directMethodInvocation() {
		Block block = parse("\"foo\".toString();");
		Expression expression = getExpression(block, 0);
		MethodInvocation op = assertHasType(expression, MethodInvocation.class);
		Expression foo = op.getExpression();

		UseDef uses = analyse(block).get(op);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.METHOD_INVOCATION, use.getKind());
		assertNull(use.getName());
		assertEquals(op, use.getOp());
	}

	@Test
	public void namedMethodInvocation() {
		Block block = parse("String foo = \"foo\";\n"
				+ "foo.toString();");
		Expression expression = getExpression(block, 1);
		MethodInvocation op = assertHasType(expression, MethodInvocation.class);
		Expression foo = getVarInit((ASTNode) block.statements().get(0));

		UseDef uses = analyse(block).get(op);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.METHOD_INVOCATION, use.getKind());
		Name name = use.getName();
		assertNotNull(name);
		assertEquals("foo", name.getFullyQualifiedName());
		assertEquals(op, use.getOp());
	}

	@Test
	public void directMethodParam() {
		Block block = parse("String.valueOf(\"foo\");");
		Expression expression = getExpression(block, 0);
		MethodInvocation op = assertHasType(expression, MethodInvocation.class);
		Expression foo = (Expression) op.arguments().get(0);

		UseDef uses = analyse(block).get(op);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.METHOD_PARAMETER, use.getKind());
		assertNull(use.getName());
		assertEquals(op, use.getOp());
	}

	@Test
	public void namedMethodParam() {
		Block block = parse("String foo = \"foo\";\n"
				+ "String.valueOf(foo);");
		Expression expression = getExpression(block, 1);
		MethodInvocation op = assertHasType(expression, MethodInvocation.class);
		Expression foo = getVarInit((ASTNode) block.statements().get(0));

		UseDef uses = analyse(block).get(op);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.METHOD_PARAMETER, use.getKind());
		Name name = use.getName();
		assertNotNull(name);
		assertEquals("foo", name.getFullyQualifiedName());
		assertEquals(op, use.getOp());
	}

	@Test
	public void directReturn() {
		Block block = parse("return \"foo\";");
		ReturnStatement op = assertHasType(block.statements().get(0), ReturnStatement.class);
		Expression foo = op.getExpression();

		UseDef uses = analyse(block).get(foo);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.RETURN, use.getKind());
		assertNull(use.getName());
		assertEquals(op, use.getOp());
	}

	@Test
	public void namedReturn() {
		Block block = parse("String foo = \"foo\";\n"
				+ "return foo;");
		ReturnStatement op = assertHasType(block.statements().get(1), ReturnStatement.class);
		Expression foo = getVarInit((ASTNode) block.statements().get(0));

		UseDef uses = analyse(block).get(op);
		Set<Use> fooUses = uses.getUses(foo);
		assertEquals(1, fooUses.size());
		Use use = fooUses.iterator().next();
		assertEquals(Kind.RETURN, use.getKind());
		Name name = use.getName();
		assertNotNull(name);
		assertEquals("foo", name.getFullyQualifiedName());
		assertEquals(op, use.getOp());
	}
}
