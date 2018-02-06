package de.tudarmstadt.rxrefactoring.core.analysis.cfg.exception;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class ExceptionExits {
	
	private final Multimap<ExceptionIdentifier, Expression> exceptions;
	
	private ExceptionExits() {
		this(Multimaps.newListMultimap(Maps.newHashMap(), () -> Lists.newLinkedList()));
	}
	
	private ExceptionExits(Multimap<ExceptionIdentifier, Expression> exceptions) {
		this.exceptions = exceptions;
	}
	
	public static ExceptionExits createFrom(Expression expr) {
		final Multimap<ExceptionIdentifier, Expression> exceptions = Multimaps.newListMultimap(Maps.newHashMap(), () -> Lists.newLinkedList());
		
		ASTVisitor v = new ASTVisitor() {
						
			public boolean visit(ArrayAccess node) {
				return true;
			}
			
			public boolean visit(ArrayCreation node) {
				if (node.getInitializer() == null) {
					exceptions.put(ExceptionIdentifier.NEGATIVE_ARRAY_SIZE_EXCEPTION, node);
				}
				return true;
			}
			
			public boolean visit(ArrayInitializer node) {
				return true;
			}

			public boolean visit(Assignment node) {
				return true;
			}

			public boolean visit(BooleanLiteral node) {
				return true;
			}
		
			public boolean visit(CastExpression node) {
				return true;
			}
		
			public boolean visit(CharacterLiteral node) {
				return true;
			}

			public boolean visit(ClassInstanceCreation node) {
				return true;
			}
		
			public boolean visit(ConditionalExpression node) {
				return true;
			}
			
			public boolean visit(CreationReference node) {
				return true;
			}

			public boolean visit(ExpressionMethodReference node) {
				return true;
			}
	
			public boolean visit(FieldAccess node) {
				
				if (node.getExpression() != null) {
					exceptions.put(ExceptionIdentifier.NULL_POINTER_EXCEPTION, node);
				}
				
				return true;
			}
	
			public boolean visit(InfixExpression node) {
				if (node.getOperator().equals(Operator.DIVIDE)) {
					//TODO Check whether division is integer division
					exceptions.put(ExceptionIdentifier.ARITHMETIC_EXCEPTION, node);
				}
				
				return true;
			}
	
			public boolean visit(InstanceofExpression node) {
				return true;
			}
	
			public boolean visit(LambdaExpression node) {
				return true;
			}
		
			public boolean visit(MethodInvocation node) {
				IMethodBinding m = node.resolveMethodBinding();
				
				if (m != null) {
					for (ITypeBinding t : m.getExceptionTypes()) {
						exceptions.put(ExceptionIdentifier.createFrom(t), node);
					}
				} else {
					exceptions.put(ExceptionIdentifier.ANY_EXCEPTION, node);
				}
				
				if (node.getExpression() != null) {
					exceptions.put(ExceptionIdentifier.NULL_POINTER_EXCEPTION, node);
				}
				
				
				return true;
			}

			public boolean visit(NullLiteral node) {
				return true;
			}

			public boolean visit(NumberLiteral node) {
				return true;
			}

			public boolean visit(ParenthesizedExpression node) {
				return true;
			}

			public boolean visit(PostfixExpression node) {
				return true;
			}

			public boolean visit(PrefixExpression node) {
				return true;
			}
			
			public boolean visit(QualifiedName node) {
				return true;
			}

			public boolean visit(SimpleName node) {
				return true;
			}

			public boolean visit(StringLiteral node) {
				return true;
			}

			public boolean visit(SuperFieldAccess node) {
				return true;
			}

			public boolean visit(SuperMethodInvocation node) {
				IMethodBinding m = node.resolveMethodBinding();
				
				if (m != null) {
					for (ITypeBinding t : m.getExceptionTypes()) {
						exceptions.put(ExceptionIdentifier.createFrom(t), node);
					}
				} else {
					exceptions.put(ExceptionIdentifier.ANY_EXCEPTION, node);
				}
								
				return true;				
			}

			public boolean visit(SuperMethodReference node) {
				return true;
			}
		
			public boolean visit(ThisExpression node) {
				return true;
			}
			
			public boolean visit(TypeLiteral node) {
				return true;
			}
		
			public boolean visit(TypeMethodReference node) {
				return true;
			}
		
			public boolean visit(VariableDeclarationExpression node) {
				return true;
			}			
		};
		
		expr.accept(v);
		return new ExceptionExits(exceptions);
	}
	
	
	public String toString() {
		return "ExceptionExits\n" + exceptions.toString();
	}

}
