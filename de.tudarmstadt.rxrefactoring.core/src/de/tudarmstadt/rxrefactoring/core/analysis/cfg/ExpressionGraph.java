package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.AbstractBaseGraph;

public class ExpressionGraph extends AbstractBaseGraph<Expression, Edge<Expression>>
implements ControlFlowGraph<Expression> {
	
	protected ExpressionGraph() {
		super((v1, v2) -> new ExpressionEdge(v1, v2), false, true);
	}

	public static ExpressionGraph from(Expression expr) {
		ExpressionGraph result = new ExpressionGraph();		
		ExpressionUtils.from(result, expr);
		return result;
	}
	
	private static class ExpressionUtils {
		
				
		public static Expression from(ExpressionGraph graph, Expression currentExpression) {
			if (currentExpression instanceof MethodInvocation) {				
				MethodInvocation e = (MethodInvocation) currentExpression;
				Queue<Expression> expressionQueue = new LinkedList<Expression>();
				
				Expression expression = e.getExpression();				
				if (expression != null) {
					expressionQueue.add(from(graph, expression));
				}				
				
				for(Object o : e.arguments()) {
					Expression e0 = (Expression) o;
					expressionQueue.add(from(graph, e0));
				}
				
				expressionQueue.add(e);
				
				Expression previousExpression = null;
				while (!expressionQueue.isEmpty()) {
					Expression e0 =  expressionQueue.poll();
					
					if (previousExpression != null) {
						addEdge(graph, previousExpression, e0);
					}
					
					previousExpression = e0;					
				}
				
				return previousExpression;
			} else if (currentExpression instanceof Assignment) {				
				Assignment e = (Assignment) currentExpression;
			
				Expression e1 = from(graph, e.getLeftHandSide());
				Expression e0 = from(graph, e.getRightHandSide());
				
				addEdge(graph, e1, e0);
				addEdge(graph, e0, e);
				
				return e;				
			} else { // If there is no control flow inside the expression
				return currentExpression;
			}	
			
			/*
			 * Expression:
				 *    {@link Annotation},
				 *    {@link ArrayAccess},
				 *    {@link ArrayCreation},
				 *    {@link ArrayInitializer},
				 *    {@link BooleanLiteral},
				 *    {@link CastExpression},
				 *    {@link CharacterLiteral},
				 *    {@link ClassInstanceCreation},
				 *    {@link ConditionalExpression},
				 *    {@link CreationReference},
				 *    {@link ExpressionMethodReference},
				 *    {@link FieldAccess},
				 *    {@link InfixExpression},
				 *    {@link InstanceofExpression},
				 *    {@link LambdaExpression},
				 *    {@link MethodReference},
				 *    {@link Name},
				 *    {@link NullLiteral},
				 *    {@link NumberLiteral},
				 *    {@link ParenthesizedExpression},
				 *    {@link PostfixExpression},
				 *    {@link PrefixExpression},
				 *    {@link StringLiteral},
				 *    {@link SuperFieldAccess},
				 *    {@link SuperMethodInvocation},
				 *    {@link SuperMethodReference},
				 *    {@link ThisExpression},
				 *    {@link TypeLiteral},
				 *    {@link TypeMethodReference},
				 *    {@link VariableDeclarationExpression}
				 */    
		}
		
		private static Edge<Expression> addEdge(ExpressionGraph graph, Expression sourceVertex, Expression targetVertex) {
			graph.addVertex(sourceVertex);
			graph.addVertex(targetVertex);
			
			return graph.addEdge(sourceVertex, targetVertex);
		}
		
	}

}
