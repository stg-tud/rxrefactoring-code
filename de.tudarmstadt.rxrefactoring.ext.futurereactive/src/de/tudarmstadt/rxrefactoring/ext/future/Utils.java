package de.tudarmstadt.rxrefactoring.ext.future;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.collect.Lists;

import de.tudarmstadt.rxrefactoring.core.NodeSupplier;
import de.tudarmstadt.rxrefactoring.core.ir.util.ConsumerBuilder;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;

public class Utils {

	public static NodeSupplier<Expression> callableToConsumer(@NonNull Expression expr, @NonNull NodeSupplier<SimpleName> name, @NonNull NodeSupplier<? extends Type> type) {
		if (expr instanceof LambdaExpression) {
			return unit -> unit.copyNode(expr);
		} else if (expr instanceof ClassInstanceCreation && ((ClassInstanceCreation) expr).getAnonymousClassDeclaration() != null) {
			return unit -> {
				ClassInstanceCreation newCallable = (ClassInstanceCreation) expr;
				
				AnonymousClassDeclaration anonClass = newCallable.getAnonymousClassDeclaration();
				
				
				
				MethodDeclaration callMethod = null;
				for (Object element : anonClass.bodyDeclarations()) {
					if (element instanceof MethodDeclaration) {
						MethodDeclaration method = (MethodDeclaration) element;					 
						
						if (Methods.hasSignature(method.resolveBinding(), null, "call")) {
							callMethod = method;
							break;
						}
					}					
				}
				
				if (callMethod == null) {
					throw new IllegalArgumentException("The given callable contains no call.");
				}	
								
				if (callMethod.parameters().size() != 0) {
					throw new IllegalArgumentException("The given callable contains no call with the correct signature.");
				}
				
				
				final MethodDeclaration callMethodFinal = callMethod;
		//		SingleVariableDeclaration variable = (SingleVariableDeclaration) callMethod.parameters().get(0);				
				@SuppressWarnings("null")
				ConsumerBuilder builder = new ConsumerBuilder(
						type, 
						name, 
						u -> (Block) ASTNode.copySubtree(u.getAST(), callMethodFinal.getBody()));
				
				return (Expression) builder.supplyClassInstanceCreation().apply(unit);
			};			
		}
		
		throw new UnsupportedOperationException("IMPLEMENT OTHER CASES");
	}
	
	
	
	public static @NonNull List<ReturnStatement> findReturnStatements(@NonNull Block body) {
		@SuppressWarnings("null")
		@NonNull List<ReturnStatement> returns = Lists.newLinkedList();
		
		class BlockVisitor extends ASTVisitor {
			
			@Override
			public boolean preVisit2(ASTNode node) {
				//Do not visit expressions
				if (node instanceof Expression) {
					return false;
				}
				
				return true;
			}
			
			public boolean visit(ReturnStatement node) {
				returns.add(node);
				return true;
			}
		}		
		
		body.accept(new BlockVisitor());
		
		return returns;
	}
}
