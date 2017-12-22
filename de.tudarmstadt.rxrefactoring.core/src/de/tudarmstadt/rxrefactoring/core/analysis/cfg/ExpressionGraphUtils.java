package de.tudarmstadt.rxrefactoring.core.analysis.cfg;

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;



public class ExpressionGraphUtils {
	public static ExpressionGraph from(Expression expr) {
		ExpressionGraph graph = new ExpressionGraph();		
		new ExpressionGraphBuilder(graph).from(expr);
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
						
		/*
		 * Result contains the first and last (sub-)expression of an expression evaluation.
		 */
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
				
			} else if (currentExpression instanceof SuperMethodInvocation) {				
				SuperMethodInvocation e = (SuperMethodInvocation) currentExpression;
				Queue<Result> expressionQueue = new LinkedList<>();
								
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
				
			} else if (currentExpression instanceof ClassInstanceCreation) {				
				ClassInstanceCreation e = (ClassInstanceCreation) currentExpression;
			
				
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
				InfixExpression e = (InfixExpression) currentExpression;
									
				Result lhs = from(e.getLeftOperand());
				Result rhs = from(e.getRightOperand());
												
				addEdge(lhs.exit, rhs.entry);
				addEdge(rhs.exit, e);	
				
				if (e.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND) || e.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR)) {
					addEdge(lhs.exit, e);
				}
				
				return new Result(lhs.entry, e);	
				
			} else if (currentExpression instanceof PostfixExpression) {
				PostfixExpression e = (PostfixExpression) currentExpression;
					
				Result operand = from(e.getOperand());								
				addEdge(operand.exit, e);								
				return new Result(operand.entry, e);	
				
			} else if (currentExpression instanceof PrefixExpression) {
				PrefixExpression e = (PrefixExpression) currentExpression;
					
				Result operand = from(e.getOperand());								
				addEdge(operand.exit, e);								
				return new Result(operand.entry, e);	
				
			} else if (currentExpression instanceof FieldAccess) {
				FieldAccess e = (FieldAccess) currentExpression;
				
				Result res = from(e.getExpression());
				
				addEdge(res.exit, e);
				
				return new Result(res.entry, e);	
			} else if (currentExpression instanceof InstanceofExpression) {
				InstanceofExpression e = (InstanceofExpression) currentExpression;
								
				Result res = from(e.getLeftOperand());				
				addEdge(res.exit, e);
				
				return new Result(res.entry, e);	
			} else if (currentExpression instanceof ArrayAccess) {
				ArrayAccess e = (ArrayAccess) currentExpression;
				
				Result arrayRes = from(e.getArray());
				Result indexRes = from(e.getIndex());
		
				
				addEdge(arrayRes.exit, indexRes.entry);
				addEdge(indexRes.exit, e);
				
				return new Result(arrayRes.entry, e);
				
			} else if (currentExpression instanceof ArrayCreation) {
				ArrayCreation e = (ArrayCreation) currentExpression;
				
				ArrayInitializer initializer = e.getInitializer();				
				
				if (initializer == null) {
					Expression firstNode = null;
					Expression previousNode = null; 
					for (Object o : e.dimensions()) {
						Expression initExpr = (Expression) o;						
						
						Result exprResult = from(initExpr);
						if (previousNode != null) {
							addEdge(previousNode, exprResult.entry);							
						} else {
							firstNode = exprResult.entry;
						}						
						previousNode = exprResult.exit;					
					}
					
					if (previousNode != null) //Should not happen under normal conditions, because either dimensions or an initializer have to be provided.
						addEdge(previousNode, e);
					
					return new Result(firstNode, e);
				} else {					
					Result initializerResult = from(e.getInitializer());
					
					addEdge(initializerResult.exit, e);
					return new Result(initializerResult.entry, e);					
				}				
			} else if (currentExpression instanceof ArrayInitializer) {
				ArrayInitializer e = (ArrayInitializer) currentExpression;
				
				Expression firstNode = e;
				Expression previousNode = null; 
				
				for (Object o : e.expressions()) {
					Expression expr = (Expression) o;						
					
					Result exprResult = from(expr);
					if (previousNode != null) {
						addEdge(previousNode, exprResult.entry);							
					} else {
						firstNode = exprResult.entry;
					}						
					previousNode = exprResult.exit;					
				}
				
				if (previousNode != null)
					addEdge(previousNode, e);
				
				return new Result(firstNode, e);
				
			} else if (currentExpression instanceof ParenthesizedExpression) {
				ParenthesizedExpression e = (ParenthesizedExpression) currentExpression;
									
					Result res = from(e.getExpression());				
					addEdge(res.exit, e);
					
					return new Result(res.entry, e);	
			} else { // If there is no control flow inside the expression
				/* 
				 * BooleanLiteral, CharacterLiteral, Name, ThisExpression, TypeLiteral, NullLiteral, NumberLiteral, CreationReference, VariableDeclarationExpression, 
				 * LambdaExpression, SuperFieldAccess, StringLiteral, Annotation
				 */
				return new Result(currentExpression, currentExpression);
				
				
				
			}	
			
			/*
			 * Expression:
				 *    {@link ExpressionMethodReference},
				 *    {@link MethodReference},
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
