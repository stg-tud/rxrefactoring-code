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
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;

public class PreconditionWorker implements IWorker<Map<ASTNode, UseDef>, Void>{

	
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable Map<ASTNode, UseDef> input, @NonNull WorkerSummary summary)
			throws Exception {

		
		//Multimap<Expression, FutureMethod> methodUsages = Multimaps.newSetMultimap(Maps.newHashMap(), () -> EnumSet.noneOf(FutureMethod.class));
		
		units.accept(new UnitASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
								
								
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
		
		
		
		return null;
	}
	
	

}
