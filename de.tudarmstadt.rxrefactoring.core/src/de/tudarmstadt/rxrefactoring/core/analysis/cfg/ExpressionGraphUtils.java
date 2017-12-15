package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;



public class ExpressionGraphUtils {
	public static ExpressionGraph from(Expression expr) {
		ExpressionGraph graph = new ExpressionGraph();		
		Result result = new ExpressionGraphBuilder(graph).from(expr);

		return graph;
	}
	
	
	
	public static Result addTo(IControlFlowGraph<? super Expression> graph, Expression expr) {
		ExpressionGraphBuilder utils = new ExpressionGraphBuilder(graph);
		Result result = utils.from(expr);
				
		return result;				
	}
	
	public static class Result {
		final Expression entry;
		final Expression exit;
					
		public Result(Expression entry, Expression exit) {
			super();
			this.entry = entry;
			this.exit = exit;
		}		
		
		@Override
		public String toString() {
			return "Result(entry=" + entry + ", exit=" + exit + ")";
		}
	}
	
	
	static class ExpressionGraphBuilder {
		
		private final IControlFlowGraph<? super Expression> graph;
		
		public ExpressionGraphBuilder(IControlFlowGraph<? super Expression> graph) {
			this.graph = graph;
		}		
						
		public Result from(Expression currentExpression) {
			if (currentExpression instanceof MethodInvocation) {				
				MethodInvocation e = (MethodInvocation) currentExpression;
				Queue<Result> expressionQueue = new LinkedList<>();
				
				Expression expression = e.getExpression();				
				if (expression != null) {
					expressionQueue.add(from(expression));
				}				
				
				for(Object o : e.arguments()) {
					Expression e0 = (Expression) o;
					expressionQueue.add(from(e0));
				}
				
								
				Result first = expressionQueue.peek();
				
				Result previousExpression = null;
				while (!expressionQueue.isEmpty()) {
					Result e0 =  expressionQueue.poll();
					
					if (previousExpression != null) {
						addEdge(previousExpression.exit, e0.entry);
					}
					
					previousExpression = e0;					
				}
				
				if (previousExpression != null) {
					addEdge(previousExpression.exit, e);
				}
				
				return new Result(first.entry, e);
				
			} else if (currentExpression instanceof Assignment) {				
				Assignment e = (Assignment) currentExpression;
			
				Result lhs = from(e.getLeftHandSide());
				Result rhs = from(e.getRightHandSide());
				
				addEdge(rhs.exit, lhs.entry);
				addEdge(lhs.exit, e);
				
				return new Result(rhs.entry, e);				
			} else if (currentExpression instanceof CastExpression) {				
				CastExpression e = (CastExpression) currentExpression;
						
				Result res = from(e.getExpression());
				
				addEdge(res.exit, e);
				
				return new Result(res.entry, e);		
			} else if (currentExpression instanceof ConditionalExpression) {				
				ConditionalExpression e = (ConditionalExpression) currentExpression;
						
				Result cond = from(e.getExpression()); ;
				Result thenBranch = from(e.getThenExpression()); 
				Result elseBranch = from(e.getElseExpression());
				
				
				addEdge(cond.exit, thenBranch.entry);
				addEdge(cond.exit, elseBranch.entry);
				addEdge(thenBranch.exit, e);
				addEdge(elseBranch.exit, e);				
				
				return new Result(cond.entry, e);				
			} else if (currentExpression instanceof InfixExpression) {
				//TODO: Add shortcut semantics e.g. for &&				
				InfixExpression e = (InfixExpression) currentExpression;
					
				Result lhs = from(e.getLeftOperand());
				Result rhs = from(e.getRightOperand());
				
				addEdge(lhs.exit, rhs.entry);
				addEdge(rhs.exit, e);			
				
				return new Result(lhs.entry, e);				
			} else if (currentExpression instanceof FieldAccess) {
				FieldAccess e = (FieldAccess) currentExpression;
				
				Result res = from(e.getExpression());
				
				addEdge(res.exit, e);
				
				return new Result(res.entry, e);	
			} else { // If there is no control flow inside the expression
				/* 
				 * BooleanLiteral, CharacterLiteral, Name, ThisExpression, TypeLiteral, NullLiteral, NumberLiteral, CreationReference, VariableDeclarationExpression, 
				 * LambdaExpression, SuperFieldAccess
				 */
				return new Result(currentExpression, currentExpression);
			}	
			
			/*
			 * Expression:
				 *    {@link Annotation},
				 *    {@link ArrayAccess},
				 *    {@link ArrayCreation},
				 *    {@link ArrayInitializer},
				 *    {@link ClassInstanceCreation},
				 *    {@link ExpressionMethodReference},
				 *    {@link FieldAccess},
				 *    {@link InstanceofExpression},
				 *    {@link MethodReference},
				 *    {@link ParenthesizedExpression},
				 *    {@link PostfixExpression},
				 *    {@link PrefixExpression},
				 *    {@link StringLiteral},
				 *    {@link SuperMethodInvocation},
				 *    {@link SuperMethodReference},
				 *    {@link TypeMethodReference},
				 */    
		}
		
		private IEdge<? super Expression> addEdge(Expression sourceVertex, Expression targetVertex) {
			graph.addVertex(sourceVertex);
			graph.addVertex(targetVertex);
			
			return graph.addEdge(sourceVertex, targetVertex);
		}
		
	}
}
