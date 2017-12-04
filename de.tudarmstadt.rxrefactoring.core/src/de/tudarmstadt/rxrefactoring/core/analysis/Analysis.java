package de.tudarmstadt.rxrefactoring.core.analysis;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.ExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.StatementGraph;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;

public final class Analysis {

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
				"f(g.h(x), y)";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(program.toCharArray());
		
		ASTNode node = parser.createAST(null);
		
		ExpressionGraph g = ExpressionGraph.from((Expression) node);
		
		System.out.println(g);
	}
	
	
	public static void main(String[] args) {
		
		expressionExample();
		
	}
	
}
