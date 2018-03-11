package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

public class InstantiationUseWorker implements IWorker<SubclassInstantiationCollector, InstantiationUseWorker>{
	
	Multimap<MethodInvocation, ASTNode> methodInvReturnClassUsages = HashMultimap.create();
	Multimap<ClassInstanceCreation, ASTNode> subclassInstanceCreationUsages = HashMultimap.create();
	Map<ASTNode, UseDef> analysis;
	Multimap<IRewriteCompilationUnit, TypeDeclaration> subclassDeclarations;
	
	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units, @Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary)
			throws Exception {
		
		analysis = input.analysis;
		
		Multimap<MethodDeclaration, MethodInvocation> methodInvReturnClass = input.methodInvReturnClass;
		Multimap<MethodDeclaration, ClassInstanceCreation> subclassInstanceCreations = input.subclassInstanceCreations;
		
		analysis.forEach((node, useDef) -> 
		{
			Set<Expression> defs = useDef.asMap().keySet();
			
			// Stores uses of kinds METHOD_INVOCATION, METHOD_PARAMETER and RETURN
			if (node instanceof MethodInvocation || node instanceof ReturnStatement) 
			{
				defs.forEach(expr -> 
				{
					Set exprUses = useDef.getUses(expr);
					if (methodInvReturnClass.values().contains(expr)) 
					{
						if (exprUses.stream().anyMatch(o -> o.toString().contains(node.toString())))
						methodInvReturnClassUsages.put((MethodInvocation) expr, node);
					}
					if (subclassInstanceCreations.values().contains(expr)) 
					{
						if (exprUses.stream().anyMatch(o -> o.toString().contains(node.toString())))
							subclassInstanceCreationUsages.put((ClassInstanceCreation) expr, node);
					}
				
				});
			}
		});
		return this;
	}
	
	

}
