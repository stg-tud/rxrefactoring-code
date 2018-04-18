package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

/**
 * Description: Collects uses of Future instances and collections<br>
 * Author: Camila Gonzalez<br>
 * Created: 23/03/2018
 */
public class InstantiationUseWorker implements IWorker<SubclassInstantiationCollector, InstantiationUseWorker> {

	// Maps MethodInvocations and ClassInstanceCreations that return Future or
	// Future subclass instantiations to the latter instance uses.
	Multimap<ASTNode, Use> instantiationUses = HashMultimap.create();
	
	// Stores ClassInstanceCreations and MethodInvocations of instances onto which
	// only supported methods are called.
	public Multiset<ASTNode> toRefactorInstantiations = HashMultiset.create();
	
	// Maps the names attributed to instance creations for which the refactoring
	// is supported to the MethodDeclaration where they are declared.
	public Multimap<String, MethodDeclaration> instantiationNames = HashMultimap.create();
	
	// Maps MethodInvocations that return Future or Future subclass collections to
	// the latter instance uses.
	Multimap<MethodInvocation, Use> collectionCreationsToUses = HashMultimap.create();
	
	Map<ASTNode, UseDef> analysis;
	
	//TODO change to classInfo
	List<String> notSupported = Arrays.asList("cancel", "isCancelled", "isDone");

	public IRewriteCompilationUnit unit;

	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units,
			@Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {

		analysis = input.analysis;

		HashSet<ASTNode> instantiations = new HashSet<ASTNode>();
		instantiations.addAll(input.methodInvReturnClass.values());
		instantiations.addAll(input.subclassInstanceCreations.values());
		instantiations.addAll(input.methodInvReturnSubclass.values());
		HashSet<MethodInvocation> collectionCreations = new HashSet<MethodInvocation>();
		collectionCreations.addAll(input.methodInvReturnCollection.values());
		collectionCreations.addAll(input.methodInvReturnSubclassCollection.values());

		analysis.values().forEach(useDef -> {
			Set<Expression> definitions = useDef.asMap().keySet();
			definitions.forEach(expr -> {
				Set<Use> exprUses = useDef.getUses(expr);
				if (instantiations.contains(expr)) {
					exprUses.stream()
						.forEach(use -> instantiationUses.put(expr, use));
				}
				if (collectionCreations.contains(expr)) {
					exprUses.stream()
						.forEach(use -> collectionCreationsToUses.put((MethodInvocation) expr, use));
				}
			});
		});
		
		toRefactorInstantiations.addAll(instantiationUses.keySet());
		// Callee of not supported method
		instantiationUses.forEach((node, use) -> {
			if (use.getOp() instanceof MethodInvocation) {
				MethodInvocation methodInv = (MethodInvocation) use.getOp();
				if (notSupported.contains(methodInv.getName().toString()))
					toRefactorInstantiations.remove(node);
			}
			});
		
		// Collect identifiers of supported instances
		instantiationUses.forEach((expr, use) -> {
			if (toRefactorInstantiations.contains(expr) && use.getName()!=null) {
				Optional<MethodDeclaration> methodDecl= ASTNodes.findParent(expr, MethodDeclaration.class);
				if (methodDecl.isPresent())
					instantiationNames.put(use.getName().toString(), methodDecl.get());
			}
		});
		
		//TODO Part of assignment of not supported expression
		
		return this;
	}

}
