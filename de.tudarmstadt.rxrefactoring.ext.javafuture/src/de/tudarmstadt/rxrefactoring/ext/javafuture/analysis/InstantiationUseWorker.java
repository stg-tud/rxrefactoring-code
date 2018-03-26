package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

/**
 * Description: Collects uses of Future instances and collections<br>
 * Author: Camila Gonzalez<br>
 * Created: 23/03/2018
 */
public class InstantiationUseWorker implements IWorker<SubclassInstantiationCollector, InstantiationUseWorker>{
	
	// Maps MethodInvocations and ClassInstanceCreations that return Future or Future subclass 
	// instantiations to the latter instance uses.
	Multimap<ASTNode, ASTNode> classInstantiationsToUses = HashMultimap.create();
	// Maps MethodInvocations that return Future or Future subclass collections to the latter instance uses.
	Multimap<MethodInvocation, ASTNode> collectionCreationsToUses = HashMultimap.create();
	
	Map<ASTNode, UseDef> analysis;
	
	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units, @Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary)
			throws Exception {
		
		analysis = input.analysis;
		
		HashSet<ASTNode> instantiations = new HashSet<ASTNode>();
		instantiations.addAll(input.methodInvReturnClass.values());
		instantiations.addAll(input.subclassInstanceCreations.values());
		instantiations.addAll(input.methodInvReturnSubclass.values());
		HashSet<MethodInvocation> collectionCreations = new HashSet<MethodInvocation>();
		collectionCreations.addAll(input.methodInvReturnCollection.values());
		collectionCreations.addAll(input.methodInvReturnSubclassCollection.values());
		
		analysis.forEach((node, useDef) -> 
		{
			Set<Expression> defs = useDef.asMap().keySet();
			
			// Stores Uses of kinds METHOD_INVOCATION, METHOD_PARAMETER and RETURN
			if (node instanceof MethodInvocation || node instanceof ReturnStatement) 
			{
				defs.forEach(expr -> 
				{
					Set<Use> exprUses = useDef.getUses(expr);
					if (instantiations.contains(expr)) 
					{
						if (exprUses.stream().anyMatch(o -> o.getOp().equals(node)))
							classInstantiationsToUses.put(expr, node);
					}
					if (collectionCreations.contains(expr)) 
					{
						if (exprUses.stream().anyMatch(o -> o.getOp().equals(node)))
							collectionCreationsToUses.put((MethodInvocation) expr, node);
					}
				
				});
			}
		});
		return this;
	}
	
	

}
