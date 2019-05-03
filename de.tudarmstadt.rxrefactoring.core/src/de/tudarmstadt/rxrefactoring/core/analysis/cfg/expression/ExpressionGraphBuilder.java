package de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression;

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
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import com.google.common.collect.ImmutableMultimap;

import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IControlFlowGraph;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.IEdge;
import de.tudarmstadt.rxrefactoring.core.analysis.cfg.expression.ExpressionGraph.ExprAccess;


class ExpressionGraphBuilder {
	
	private final IControlFlowGraph<? super Expression> graph;
	
	public ExpressionGraphBuilder(IControlFlowGraph<? super Expression> graph) {
		this.graph = graph;
	}		
					
	/*
	 * Result contains the first and last (sub-)expression of an expression evaluation.
	 */
	public ExprAccess from(Expression currentExpression) {
		if (currentExpression instanceof MethodInvocation) {				
			MethodInvocation e = (MethodInvocation) currentExpression;
			Queue<ExprAccess> expressionQueue = new LinkedList<>();
			
			//Traverse the expression
			Expression expression = e.getExpression();				
			if (expression != null) {
				expressionQueue.add(from(expression));
			}				
			
			//Traverse the arguments
			for(Object o : e.arguments()) {
				Expression e0 = (Expression) o;
				expressionQueue.add(from(e0));
			}				
						
			//Build the edges
			ExprAccess first = expressionQueue.peek();				
			ExprAccess previousExpression = null;
			ImmutableMultimap.Builder<ExceptionIdentifier, Expression> exceptions = ImmutableMultimap.builder(); //Collect exceptions from childs
			
			while (!expressionQueue.isEmpty()) {
				ExprAccess e0 =  expressionQueue.poll();
				exceptions.putAll(e0.getExceptions());
				
				if (previousExpression != null) {
					addEdge(previousExpression.exit, e0.entry);
				}					
				previousExpression = e0;					
			}				
			
			if (previousExpression != null) {
				addEdge(previousExpression.exit, e);
			}
			
			//Deal with exceptions
			IMethodBinding m = e.resolveMethodBinding();
			
			
			if (m != null) {
				for (ITypeBinding t : m.getExceptionTypes()) {
					exceptions.put(ExceptionIdentifier.createFrom(t), e);
				}
			} else {
				exceptions.put(ExceptionIdentifier.ANY_EXCEPTION, e);
			}
			
			if (e.getExpression() != null) {
				exceptions.put(ExceptionIdentifier.NULL_POINTER_EXCEPTION, e);
			}
			
			
			//Return corresponding expr access			
			if (first == null)
				return ExprAccess.create(e, e, exceptions.build());
			else
				return ExprAccess.create(first.entry, e, exceptions.build());				
			
		} else if (currentExpression instanceof SuperMethodInvocation) {				
			SuperMethodInvocation e = (SuperMethodInvocation) currentExpression;
			Queue<ExprAccess> expressionQueue = new LinkedList<>();
			
			//Traverse arguments
			for(Object o : e.arguments()) {
				Expression e0 = (Expression) o;
				expressionQueue.add(from(e0));
			}				
					
			//Build the edges
			ExprAccess first = expressionQueue.peek();				
			ExprAccess previousExpression = null;
			ImmutableMultimap.Builder<ExceptionIdentifier, Expression> exceptions = ImmutableMultimap.builder(); //Collect exceptions from childs

			while (!expressionQueue.isEmpty()) {
				ExprAccess e0 =  expressionQueue.poll();
				exceptions.putAll(e0.getExceptions());
				
				if (previousExpression != null) {
					addEdge(previousExpression.exit, e0.entry);
				}					
				previousExpression = e0;					
			}				
			
			if (previousExpression != null) {
				addEdge(previousExpression.exit, e);
			}
			
			//Deal with exceptions
			IMethodBinding m = e.resolveMethodBinding();
						
			if (m != null) {
				for (ITypeBinding t : m.getExceptionTypes()) {
					exceptions.put(ExceptionIdentifier.createFrom(t), e);
				}
			} else {
				exceptions.put(ExceptionIdentifier.ANY_EXCEPTION, e);
			}
					
			
			//Return corresponding expr access
			if (first == null)
				return ExprAccess.create(e, e, exceptions.build());
			else
				return ExprAccess.create(first.entry, e, exceptions.build());				
			
		} else if (currentExpression instanceof ClassInstanceCreation) {				
			ClassInstanceCreation e = (ClassInstanceCreation) currentExpression;
			Queue<ExprAccess> expressionQueue = new LinkedList<>();
			
			Expression expression = e.getExpression();				
			if (expression != null) {
				expressionQueue.add(from(expression));
			}				
			
			for(Object o : e.arguments()) {
				Expression e0 = (Expression) o;
				expressionQueue.add(from(e0));
			}				
							
			ExprAccess first = expressionQueue.peek();				
			ExprAccess previousExpression = null;			
			ImmutableMultimap.Builder<ExceptionIdentifier, Expression> exceptions = ImmutableMultimap.builder(); //Collect exceptions from childs

			while (!expressionQueue.isEmpty()) {
				ExprAccess e0 =  expressionQueue.poll();
				exceptions.putAll(e0.getExceptions());
				
				if (previousExpression != null) {
					addEdge(previousExpression.exit, e0.entry);
				}
				
				previousExpression = e0;					
			}
						
			
			if (previousExpression != null) {
				addEdge(previousExpression.exit, e);
			}
			
			//Deal with exceptions
			IMethodBinding m = e.resolveConstructorBinding();
						
			if (m != null) {
				for (ITypeBinding t : m.getExceptionTypes()) {
					exceptions.put(ExceptionIdentifier.createFrom(t), e);
				}
			} else {
				exceptions.put(ExceptionIdentifier.ANY_EXCEPTION, e);
			}
			
			//Return corresponding expr access
			if (first == null)
				return ExprAccess.create(e, e, exceptions.build());
			else
				return ExprAccess.create(first.entry, e, exceptions.build());
			
		} else if (currentExpression instanceof Assignment) {				
			Assignment e = (Assignment) currentExpression;
		
			ExprAccess lhs = from(e.getLeftHandSide());
			ExprAccess rhs = from(e.getRightHandSide());
			
			addEdge(rhs.exit, lhs.entry);
			addEdge(lhs.exit, e);
			
			return ExprAccess.create(rhs.entry, e, lhs.getExceptions(), rhs.getExceptions());				
		} else if (currentExpression instanceof CastExpression) {				
			CastExpression e = (CastExpression) currentExpression;
					
			ExprAccess res = from(e.getExpression());
			
			addEdge(res.exit, e);
			
			return ExprAccess.create(res.entry, e, res.getExceptions(), ImmutableMultimap.of(ExceptionIdentifier.CLASS_CAST_EXCEPTION, e));		
		} else if (currentExpression instanceof ConditionalExpression) {				
			ConditionalExpression e = (ConditionalExpression) currentExpression;
					
			ExprAccess cond = from(e.getExpression()); ;
			ExprAccess thenBranch = from(e.getThenExpression()); 
			ExprAccess elseBranch = from(e.getElseExpression());
			
			
			addEdge(cond.exit, thenBranch.entry);
			addEdge(cond.exit, elseBranch.entry);
			addEdge(thenBranch.exit, e);
			addEdge(elseBranch.exit, e);				
			
			return ExprAccess.create(cond.entry, e, cond.getExceptions(), thenBranch.getExceptions(), elseBranch.getExceptions());				
		} else if (currentExpression instanceof InfixExpression) {
			InfixExpression e = (InfixExpression) currentExpression;
								
			ExprAccess lhs = from(e.getLeftOperand());
			ExprAccess rhs = from(e.getRightOperand());
											
			addEdge(lhs.exit, rhs.entry);
			addEdge(rhs.exit, e);	
			
			if (e.getOperator().equals(InfixExpression.Operator.CONDITIONAL_AND) || e.getOperator().equals(InfixExpression.Operator.CONDITIONAL_OR)) {
				addEdge(lhs.exit, e);
			}
			
			//TODO Check whether arguments are ints
			if (e.getOperator().equals(InfixExpression.Operator.DIVIDE))			
				return ExprAccess.create(lhs.entry, e, lhs.getExceptions(), rhs.getExceptions(), ImmutableMultimap.of(ExceptionIdentifier.ARITHMETIC_EXCEPTION, e));
			else
				return ExprAccess.create(lhs.entry, e, lhs.getExceptions(), rhs.getExceptions());
			
		} else if (currentExpression instanceof PostfixExpression) {
			PostfixExpression e = (PostfixExpression) currentExpression;
				
			ExprAccess operand = from(e.getOperand());								
			addEdge(operand.exit, e);								
			return ExprAccess.create(operand.entry, e, operand.getExceptions());	
			
		} else if (currentExpression instanceof PrefixExpression) {
			PrefixExpression e = (PrefixExpression) currentExpression;
				
			ExprAccess operand = from(e.getOperand());								
			addEdge(operand.exit, e);								
			return ExprAccess.create(operand.entry, e, operand.getExceptions());	
			
		} else if (currentExpression instanceof FieldAccess) {
			FieldAccess e = (FieldAccess) currentExpression;
			
			ExprAccess res = from(e.getExpression());
			
			addEdge(res.exit, e);
			
			return ExprAccess.create(res.entry, e, res.getExceptions());	
			
		} else if (currentExpression instanceof InstanceofExpression) {
			InstanceofExpression e = (InstanceofExpression) currentExpression;
							
			ExprAccess res = from(e.getLeftOperand());				
			addEdge(res.exit, e);
			
			return ExprAccess.create(res.entry, e, res.getExceptions());	
		} else if (currentExpression instanceof ArrayAccess) {
			ArrayAccess e = (ArrayAccess) currentExpression;
			
			ExprAccess arrayRes = from(e.getArray());
			ExprAccess indexRes = from(e.getIndex());
	
			
			addEdge(arrayRes.exit, indexRes.entry);
			addEdge(indexRes.exit, e);
			
			return ExprAccess.create(arrayRes.entry, e, 
					arrayRes.getExceptions(), 
					indexRes.getExceptions(), 
					ImmutableMultimap.of(ExceptionIdentifier.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, e));
			
		} else if (currentExpression instanceof ArrayCreation) {
			ArrayCreation e = (ArrayCreation) currentExpression;
			
			ArrayInitializer initializer = e.getInitializer();				
			
			if (initializer == null) {
				Expression firstNode = null;
				Expression previousNode = null; 
				ImmutableMultimap.Builder<ExceptionIdentifier, Expression> builder = ImmutableMultimap.builder();
				
				for (Object o : e.dimensions()) {
					Expression initExpr = (Expression) o;						
					
					ExprAccess exprResult = from(initExpr);
					builder.putAll(exprResult.getExceptions());
					
					if (previousNode != null) {
						addEdge(previousNode, exprResult.entry);							
					} else {
						firstNode = exprResult.entry;
					}						
					previousNode = exprResult.exit;					
				}
				
				if (previousNode != null) //Should not happen under normal conditions, because either dimensions or an initializer have to be provided.
					addEdge(previousNode, e);
				
				return ExprAccess.create(firstNode, e, builder.build(), ImmutableMultimap.of(ExceptionIdentifier.NEGATIVE_ARRAY_SIZE_EXCEPTION, e));
			} else {					
				ExprAccess initializerResult = from(e.getInitializer());
				
				addEdge(initializerResult.exit, e);
				return ExprAccess.create(initializerResult.entry, e, initializerResult.getExceptions());					
			}				
		} else if (currentExpression instanceof ArrayInitializer) {
			ArrayInitializer e = (ArrayInitializer) currentExpression;
			
			Expression firstNode = e;
			Expression previousNode = null; 
			ImmutableMultimap.Builder<ExceptionIdentifier, Expression> builder = ImmutableMultimap.builder();
			
			for (Object o : e.expressions()) {
				Expression expr = (Expression) o;						
				
				ExprAccess exprResult = from(expr);
				builder.putAll(exprResult.getExceptions());
				
				if (previousNode != null) {
					addEdge(previousNode, exprResult.entry);							
				} else {
					firstNode = exprResult.entry;
				}						
				previousNode = exprResult.exit;					
			}
			
			if (previousNode != null)
				addEdge(previousNode, e);
			
			return ExprAccess.create(firstNode, e, builder.build());
			
		} else if (currentExpression instanceof ParenthesizedExpression) {
			ParenthesizedExpression e = (ParenthesizedExpression) currentExpression;
								
			ExprAccess res = from(e.getExpression());				
			addEdge(res.exit, e);
				
			return ExprAccess.create(res.entry, e, res.getExceptions());	
		} else if (currentExpression instanceof ExpressionMethodReference) {
			ExpressionMethodReference e = (ExpressionMethodReference) currentExpression;
			
			ExprAccess res = from(e.getExpression());
			
			return ExprAccess.create(res.entry, e, res.getExceptions());			
			
		} else { // If there is no control flow inside the expression
			/* 
			 * BooleanLiteral, CharacterLiteral, Name, ThisExpression, TypeLiteral, NullLiteral, NumberLiteral, CreationReference, VariableDeclarationExpression, 
			 * LambdaExpression, SuperFieldAccess, StringLiteral, Annotation, CreationReference, SuperMethodReference, TypeMethodReference
			 */
			return ExprAccess.create(currentExpression, currentExpression);			
		}		  
	}
	
	
	private IEdge<? super Expression> addEdge(Expression sourceVertex, Expression targetVertex) {
		graph.addVertex(sourceVertex);
		graph.addVertex(targetVertex);
		
		return graph.addEdge(sourceVertex, targetVertex);
	}

}