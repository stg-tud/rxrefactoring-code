package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.ReachingDefinition;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;

public class PreconditionWorker implements IWorker<Map<ASTNode, ReachingDefinition>, Void>{

	enum FutureMethod {
		GET, GET_TIMEOUT, CANCEL, IS_CANCELLED, IS_DONE
	}
	
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Map<ASTNode, ReachingDefinition> input, @NonNull WorkerSummary summary)
			throws Exception {

		
		Multimap<Expression, FutureMethod> methodUsages = Multimaps.newSetMultimap(Maps.newHashMap(), () -> EnumSet.noneOf(FutureMethod.class));
		
		units.accept(new UnitASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				
				if (Methods.hasSignature(node.resolveMethodBinding(), null, "get")) {
					Collection<Expression> defs = input.get(node).getDefinitionOf(node.getExpression());
					defs.forEach(expr -> methodUsages.put(expr, FutureMethod.GET));					
				} else if (Methods.hasSignature(node.resolveMethodBinding(), null, "get", "long", "java.util.concurrent.TimeUnit")) {
					Collection<Expression> defs = input.get(node).getDefinitionOf(node.getExpression());
					defs.forEach(expr -> methodUsages.put(expr, FutureMethod.GET_TIMEOUT));
				} else if (Methods.hasSignature(node.resolveMethodBinding(), null, "cancel", "boolean")) {
					Collection<Expression> defs = input.get(node).getDefinitionOf(node.getExpression());
					defs.forEach(expr -> methodUsages.put(expr, FutureMethod.CANCEL));
				} else if (Methods.hasSignature(node.resolveMethodBinding(), null, "isCancelled")) {
					Collection<Expression> defs = input.get(node).getDefinitionOf(node.getExpression());
					defs.forEach(expr -> methodUsages.put(expr, FutureMethod.IS_CANCELLED));
				} else if (Methods.hasSignature(node.resolveMethodBinding(), null, "isDone")) {
					Collection<Expression> defs = input.get(node).getDefinitionOf(node.getExpression());
					defs.forEach(expr -> methodUsages.put(expr, FutureMethod.IS_DONE));
				}
								
				return true;
			}
		});
		
//		units.forEach(unit -> {
//			ASTNode root = unit.getRoot();
//			
//			System.out.println(root);
//			
//			ASTVisitor v = new ASTVisitor() {
//				
//				@Override
//				public void preVisit(ASTNode node) {
//					System.out.println("#######");
//					System.out.println("node:");
//					System.out.println(node);
//					System.out.println("property:");
//					System.out.println(input.get(node));
//					System.out.println("#######");
//				}
//			};
//			
//			unit.accept(v);				
//			
//		});
		
		Log.info(getClass(), methodUsages);
		
		
		return null;
	}
	
	

}
