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
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.StatementExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.StatementGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.flow.DataFlowAnalysis;

public final class Main {


	
	static IControlFlowGraph<? extends ASTNode> statementExample() {
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
		
		//Apply an analysis		
				DataFlowAnalysis analysis = Analyses.VARIABLE_NAME_ANALYSIS;
				Object result = analysis.apply(g, analysis.mapExecutor());
		
		return g;
	}
	
	static IControlFlowGraph<?> expressionExample() {
		String program =
				"a = b == 1 ? f(g.h(x), y) : z";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(program.toCharArray());
		
		ASTNode node = parser.createAST(null);
		
		ExpressionGraph g = ExpressionGraphUtils.from((Expression) node);
				
		return g;
	}
	
	static IControlFlowGraph<? extends ASTNode> statementExpressionExample() {
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
		
		String program2 =
				"{"
				+ "int n = 0;"
				+ "for(int a; b < c; d++)"
				+ "n = n + i;"
				+ "System.out.println(n);"
				+ "}";
		
		String program3 =				
				"int[][] a = new int[][] {{1, 2}, {3, 4}};"
				+ "int[] b = new int[1];"
				+ "b[0] = a[1][0];";
		
		String program4 =
				"switch (a) {"
				+ "case 0: return 0;"
				+ "case 1: "
				+ "b = a;"
				+ "a++;"
				+ "case 2:"
				+ "c = a;"
				+ "break;"
				+ "default: return a;"
				+ "}"
				+ "return b;";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS9);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(program4.toCharArray());
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		
		ASTNode node = parser.createAST(null);
		
		StatementExpressionGraph g = StatementExpressionGraph.from((Statement) node);
		
		
		//Apply an analysis		
		DataFlowAnalysis analysis = Analyses.VARIABLE_NAME_ANALYSIS;
		Object result = analysis.apply(g, analysis.mapExecutor());
		
		System.out.println(g);
		System.out.println(result);
		
		return g;
		

	}
	

	
	public static void main(String[] args) {
		
//		CallHierarchy callHierarchy = CallHierarchy.getDefault();
//	    IMember[] members = { method };
//	    MethodWrapper[] callers = callHierarchy.getCallerRoots(members);
		
		IControlFlowGraph<? extends ASTNode> graph = statementExample();

		for(IEdge<? extends ASTNode> e : graph.edgeSet()) {
			System.out.println(e.getHead() + "\n-->\n" + e.getTail() + "\n#####");
		}
		
		System.out.println("Entry=" + graph.entryNodes() + ", Exit=" + graph.exitNodes());
		
		
		
	}
	
}
