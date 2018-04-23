package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

/**
 * Description: Collects uses of Future instances and collections<br>
 * Author: Camila Gonzalez<br>
 * Created: 23/03/2018
 */
public class InstantiationUseWorker implements IWorker<SubclassInstantiationCollector, InstantiationUseWorker> {

	// Maps MethodInvocations and ClassInstanceCreations that return Future or
	// Future subclass instantiations to the latter instance uses.
	public Multimap<ASTNode, Use> instantiationUses = HashMultimap.create();
	
	public Multiset<IVariableBinding> bindings = HashMultiset.create();
	
	public Multiset<IVariableBinding> collectionBindings = HashMultiset.create();
	
	// Maps the names attributed to collection creations for which the refactoring
	// is supported to the MethodDeclaration where they are declared.
	public Multimap<String, MethodDeclaration> collectionNames = HashMultimap.create();
	
	// Maps MethodInvocations and ClassInstanceCreations (new ArrayList<Future<..>>)
	// that return Future or Future subclass collections to its uses.
	public Multimap<ASTNode, Use> collectionCreationsToUses = HashMultimap.create();
	
	// Maps a collection creation to invocation of getter methods (these are 
	// initially considered future instantiations)
	public Multimap<ASTNode, MethodInvocation> collectionGetters = HashMultimap.create();
	
	// Maps a collection creation to initialization of individual futures.
	public Multimap<ASTNode, ASTNode> collectionInstantiations = HashMultimap.create();
	
	ClassInfo classInfo;
	
	public Map<ASTNode, UseDef> anal;

	public IRewriteCompilationUnit unit;
	

	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units,
			@Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {

		Multiset<UseDef> useDefs = HashMultiset.create();
		useDefs.addAll(input.analysis.values());
		
		anal = input.analysis;
		
		classInfo = input.collector.classInfo;
		
		Multiset<ASTNode> instantiations = HashMultiset.create();
		instantiations.addAll(input.collector.methodInvReturnClass.values());
		instantiations.addAll(input.subclassInstanceCreations.values());
		instantiations.addAll(input.methodInvReturnSubclass.values());
		
		Multiset<ASTNode> collectionCreations = HashMultiset.create();
		collectionCreations.addAll(input.collector.collectionCreations.values());
		collectionCreations.addAll(input.subclassCollectionCreations.values());
		
		// Determine whether the refactoring is possible
		Multiset<ASTNode> unsupportedInstantiations = HashMultiset.create();
		Multiset<ASTNode> unsupportedCollections = HashMultiset.create();
		
		useDefs.forEach(useDef -> {
			Set<Expression> definitions = useDef.asMap().keySet();
			definitions.forEach(expr -> {
				Set<Use> exprUses = useDef.getUses(expr);
				if (instantiations.contains(expr))
					exprUses.forEach(use -> {
						// Callee of not supported method
						if (unsupportedMethodCall(use)) 
							unsupportedInstantiations.add(expr);
						else instantiationUses.put(expr, use);
					});
				if (collectionCreations.contains(expr))
					exprUses.forEach(use -> {
						collectionCreationsToUses.put(expr, use);
					});				
			});
		});
		
		unsupportedInstantiations.forEach(i -> 	instantiationUses.removeAll(i));
		

		// Collection cases are handled separately, because the collection should be 
		// refactored (or not) as a whole
		
		// TODO can method instantiations that return a List<Future> be refactored?
		
		// Gather MethodInvocations of Futures that are getters of a collection or 
		// initialization of futures in the collection, as well as their uses.
		collectionCreationsToUses.forEach((node, use) -> {
			
			// A future is initialized and added to the collection if the future and
			// the collection share a use (METHOD_INVOCATION for the collection and 
			// METHOD_PARAMETER for the use).
			List<ASTNode> items = new ArrayList<ASTNode>();
			instantiationUses.forEach((item, itemUse) -> {
				if (use.getOp() == itemUse.getOp())
					items.add(item);
			});
			items.forEach(item -> {				
				if (unsupportedInstantiations.contains(item)) {
					unsupportedCollections.add(node);
					//instantiationUses.removeAll(item);
				} else collectionInstantiations.put(node, item);
			});
			// A future is returned through a getter if a collection Use is the 
			// MethodInvocation that created it with a collection declaring class
			if (use.getOp() instanceof MethodInvocation) {
				MethodInvocation methodInv = (MethodInvocation) use.getOp();
				if (instantiations.contains(methodInv)) {
					ITypeBinding declaringClass = methodInv.resolveMethodBinding().getDeclaringClass();
					if (input.collector.isCollection(declaringClass)) {
						if (unsupportedInstantiations.contains(methodInv)) {
						//if (instantiationUses.get(methodInv).stream().anyMatch(u -> unsupportedMethodCall(u))) {
							unsupportedCollections.add(node);
						//	instantiationUses.removeAll(methodInv);
						} else collectionGetters.put(node, methodInv);
					}
				}
			}
		});
		
		//TODO dont refactor colllections because workers are disabled
		unsupportedCollections.addAll(collectionCreationsToUses.keySet());
		unsupportedCollections.forEach(c -> {
			collectionCreationsToUses.removeAll(c);
			collectionGetters.get(c).forEach(g -> instantiationUses.removeAll(g));
			collectionInstantiations.get(c).forEach(i -> instantiationUses.removeAll(i));
			collectionGetters.removeAll(c);
			collectionInstantiations.removeAll(c);
		});
		
		//TODO Part of assignment of not supported expression
		
		// Collect identifiers of supported instances
		instantiationUses.forEach((expr, use) -> {
			if (use.getName()!=null) {
				IBinding binding = use.getName().resolveBinding();
				if (binding instanceof IVariableBinding) {
					bindings.add((IVariableBinding) binding);
				}
			}
		});
		
		collectionCreationsToUses.forEach((expr, use) -> {
			if (use.getName()!=null) {
				IBinding binding = use.getName().resolveBinding();
				if (binding instanceof IVariableBinding) {
					collectionBindings.add((IVariableBinding) binding);
				}
			}
		});
				
		return this;
	}
	
	private boolean unsupportedMethodCall(Use use) {
		if (use.getOp() instanceof MethodInvocation) {
			MethodInvocation methodInv = (MethodInvocation) use.getOp();
			if (classInfo.getUnsupportedMethods().contains(methodInv.getName().toString()))
				return true;
		}
		return false;
	}

}
