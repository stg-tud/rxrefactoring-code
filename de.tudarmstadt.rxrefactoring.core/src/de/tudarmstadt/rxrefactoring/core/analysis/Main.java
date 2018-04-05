package de.tudarmstadt.rxrefactoring.core.analysis;

import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression.ExpressionGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.statement.ProgramGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.dataflow.DataFlowAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.VariableNameAnalysis;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinition;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinitionsAnalysis;

public final class Main {


	
	
	
	static IControlFlowGraph<Expression> expressionExample() {
		String program =
				"a = b == 1 ? f(g.h(x), y) : z";
				
		final ASTParser parser = ASTParser.newParser(AST.JLS10);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(program.toCharArray());
		
		ASTNode node = parser.createAST(null);
		
		ExpressionGraph g = ExpressionGraph.createFrom((Expression) node);
		
		System.out.println(ExpressionGraph.createAccess((Expression) node));
				
		return g;
	}
	
	static IControlFlowGraph<? extends ASTNode> programExample() {
		String program =
				  "{"
				+ 	"Future<Integer> f1 = ask(\"Hello\");"
				+ 	"Future<Integer> f2 = ask(\"World\");"
				+ 	"if (b) {"
				+ 		"f1 = f2;"
				+ 		"f1.cancel();"
				+ 	"}"
				+ 		"f2.cancel();"
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
		
		
		String program5 =
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
		
		String program6 = 
				"int[] l1 = new int[] {2};"
				+ "for(int i : l)"
				+ "println(i);"
				+ "return 0;";
		
		String program7 = 
				"if (o.g())"
				+ "return 0;"
				+ "return 1;";
		
		String program8 = 
			"try {"
			+	"if (1 / 0) return 1;"
			+ "} catch (java.lang.ArithmeticException e) {"
			+ 	"return 0;"
			+ "}";
		
		String program9 = 
				"try {"
				+	"throw new IllegalArgumentException();"
				+ "} catch (java.lang.IllegalArgumentException e) {"
				+ 	"return 0;"
				+ "}"
				+ "throw new NullPointerException();";
		
						
		final ASTParser parser = ASTParser.newParser(AST.JLS10);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(program.toCharArray());
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setResolveBindings(true);
		
		ASTNode node = parser.createAST(null);
		
		ProgramGraph g = ProgramGraph.createFrom((Statement) node);
		
		
		//Apply an analysis		
		DataFlowAnalysis analysis = ReachingDefinitionsAnalysis.create();
		Object result = analysis.apply(g, analysis.mapExecutor());
		
		System.out.println(g);
		System.out.println(result);
		
		return g;
		

	}
	

	
	public static void main(String[] args) {
		
//		CallHierarchy callHierarchy = CallHierarchy.getDefault();
//	    IMember[] members = { method };
//	    MethodWrapper[] callers = callHierarchy.getCallerRoots(members);
		
		IControlFlowGraph<? extends ASTNode> graph = programExample();

		for(IEdge<? extends ASTNode> e : graph.edgeSet()) {
			System.out.println(e.getHead() + "\n-->\n" + e.getTail() + "\n#####");
		}
		
		System.out.println("Entry=" + graph.entryNodes() + ", Exit=" + graph.exitNodes());
		
		
		
	}
	
}
