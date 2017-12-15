package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ExpressionGraphUtils;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.StatementExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.StatementGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.example.UseDefAnalysis;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

public final class Main {

	static void statementExample() {
		String program =
				"{"
				+ "G g = new G();"
				+ "int a = 2;"
				+ "int b = g.f(2, a);"
				+ "if (x(a, b)) {"
				+ "return a;"
				+ "}"
				+ "int r = t(g.h(x), y);"
				+ "return r;"
				+ "}";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(program.toCharArray());
		
		ASTNode node = parser.createAST(null);
		
		StatementGraph g = StatementGraph.from((Statement) node);
		
		System.out.println(g);
	}
	
	static void expressionExample() {
		String program =
				"a = b == 1 ? f(g.h(x), y) : z";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(program.toCharArray());
		
		ASTNode node = parser.createAST(null);
		
		ExpressionGraph g = ExpressionGraphUtils.from((Expression) node);
		
		
		System.out.println("Entry=" + g.entryNodes() + ", Exit=" + g.exitNodes());
		System.out.println(g);
	}
	
	static void futureExample() {
		String program =
				"{"
				+ "Future<Integer> f1 = ask(\"Hello\");"
				+ "Future<Integer> f2 = ask(\"World\");"
				+ "if (b) {"
				+ "f1 = f2;"
				+ "f1.cancel();"
				+ "}"
				+ "f2.cancel();"
				+ "}";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(program.toCharArray());
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		
		ASTNode node = parser.createAST(null);
		
		StatementExpressionGraph g = StatementExpressionGraph.from((Statement) node);
		
		for(IEdge<ASTNode> e : g.edgeSet()) {
			System.out.println(e.getHead() + "\n-->\n" + e.getTail() + "\n#####");
		}
		
		//Apply an analysis		
		Map<ASTNode, Multimap<Expression, ASTNode>> result = new UseDefAnalysis().apply(g);
		
		System.out.println(g);
	}
	
	
	public static void main(String[] args) {
		
		futureExample();
		
		
		
	}
	
}
