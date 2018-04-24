package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
 * Description: Checks preconditions, determines which Expressions should be refactored<br>
 * Author: Camila Gonzalez<br>
 * Created: 23/03/2018
 */
public class InstantiationUseWorker implements IWorker<SubclassInstantiationCollector, InstantiationUseWorker> {

	// Maps MethodInvocations and ClassInstanceCreations that return Future or
	// Future subclass instantiations to the latter instance uses.
	public Multimap<ASTNode, Use> instantiationUses = HashMultimap.create();
	
	public Set<IVariableBinding> bindings;
	
	public Set<IVariableBinding> collectionBindings;
	
	// Maps the names attributed to Collection creations for which the refactoring
	// is supported to the MethodDeclaration where they are declared.
	public Multimap<String, MethodDeclaration> collectionNames = HashMultimap.create();
	
	// Maps MethodInvocations and ClassInstanceCreations (new ArrayList<Future<..>>)
	// that return Future or Future subclass Collections to its uses.
	public Multimap<ASTNode, Use> collectionCreationsToUses = HashMultimap.create();
	
	// Maps a Collection creation to invocation of getter methods (these are 
	// initially considered future instantiations)
	public Multimap<ASTNode, MethodInvocation> collectionGetters = HashMultimap.create();
	
	// Maps a Collection creation to initialization of individual futures.
	public Multimap<ASTNode, ASTNode> collectionInstantiations = HashMultimap.create();
	
	// Maps MethodDeclarations to their return instantiations
	public Multimap<MethodDeclaration, ASTNode> methodDeclarations = HashMultimap.create();
	
	ClassInfo classInfo;
	
	public Map<ASTNode, UseDef> anal;

	public IRewriteCompilationUnit unit;
	

	@Override
	public @Nullable InstantiationUseWorker refactor(@NonNull IProjectUnits units,
			@Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {

		Set<UseDef> useDefs = new HashSet<UseDef>();
		useDefs.addAll(input.analysis.values());
		
		//TODO only to print, remove
		anal = input.analysis;
		
		classInfo = input.collector.classInfo;
		
		Set<ASTNode> instantiations = new HashSet<ASTNode>();
		instantiations.addAll(input.collector.methodInvReturnClass.values());
		instantiations.addAll(input.subclassInstanceCreations.values());
		instantiations.addAll(input.methodInvReturnSubclass.values());
		
		Set<ASTNode> collectionCreations = new HashSet<ASTNode>();
		collectionCreations.addAll(input.collector.collectionCreations.values());
		collectionCreations.addAll(input.subclassCollectionCreations.values());
		
		// Maps the left side of an Assignment to the expressions it is assigned
		Multimap<IVariableBinding, ASTNode> assignments = HashMultimap.create();
		
		// Determine whether the refactoring is possible
		Set<ASTNode> unsupportedInstantiations =  new HashSet<ASTNode>();
		Set<ASTNode> unsupportedCollections = new HashSet<ASTNode>();
		Set<MethodDeclaration> unsupportedMethodDecl = new HashSet<MethodDeclaration>();
		
		
		// Collect instantiationUses and unsupportedInstantiations
		useDefs.forEach(useDef -> {
			Set<Expression> definitions = useDef.asMap().keySet();
			definitions.forEach(expr -> {
				Set<Use> exprUses = useDef.getUses(expr);
				if (instantiations.contains(expr))
					exprUses.forEach(use -> {
						instantiationUses.put(expr, use);
						// Callee of not supported method
						if (unsupportedMethodCall(use)) {
							unsupportedInstantiations.add(expr);
						} else if (use.getOp() instanceof ReturnStatement) {
							Optional<MethodDeclaration> parent = ASTNodes.findParent(use.getOp(), MethodDeclaration.class);
							if (parent.isPresent() && 
									parent.get().getReturnType2().resolveBinding() ==
									expr.resolveTypeBinding()) {
								methodDeclarations.put(parent.get(), expr);
							}
						} else if (use.getOp() instanceof Assignment) {
							
							Expression leftSide = ((Assignment)use.getOp()).getLeftHandSide();
							if (leftSide instanceof SimpleName) {
								SimpleName name = (SimpleName) leftSide;
								if (name.resolveBinding() instanceof IVariableBinding)
									assignments.put((IVariableBinding)name.resolveBinding(), expr);
							}
						};
					});
				// Collect collectionCreationsToUses
				if (collectionCreations.contains(expr))
					exprUses.forEach(use -> {
						collectionCreationsToUses.put(expr, use);
					});				
			});
		});
		
		// Mark instantiations which are part of an Assignment with an unsupported
		// one as unsupported as well
		Multimap<IVariableBinding, ASTNode> currentBindings = getBindings(instantiationUses);
		assignments.forEach((leftVarBinding, expr) -> {
			currentBindings.get(leftVarBinding).forEach(node -> {
				if (unsupportedInstantiations.contains(node)) {
					unsupportedInstantiations.add(expr);
				}
				else if (unsupportedInstantiations.contains(expr)) {
					unsupportedInstantiations.add(node);
				}
			});
		});
	
		// Link instantiations to MethodDeclarations and collect
		// unsupportedMethodDeclarations. 
		// If a MethodDeclaration cannot be refactored, supported 
		// instances will still be refactored, but the MethodInvocation 
		// will be treaded as if the MethodDeclaration were external.
		instantiationUses.keySet().forEach(node -> {
			if(node instanceof MethodInvocation) {
				IMethodBinding binding = ((MethodInvocation) node).resolveMethodBinding();
				for(MethodDeclaration md : methodDeclarations.keySet()) {
					IMethodBinding decBinding = md.resolveBinding();
					//TODO maybe find better way, the same is done in FutureCollector
					// If only the bindings are compared there there is an error
					// if the MethodDeclaration is in a different class
					if (binding.toString().equals(decBinding.toString()) &&
							binding.getDeclaringClass().getBinaryName().equals(decBinding.getDeclaringClass().getBinaryName())
							) {
						if (unsupportedInstantiations.contains(node))
							unsupportedMethodDecl.add(md);
					}
				}
			}
		});
		
		// Removes uses of the instantiation which is the return statement
		// within an unsupported MethodDeclaration
		unsupportedMethodDecl.forEach(md -> {
			unsupportedInstantiations.addAll(methodDeclarations.get(md));
			methodDeclarations.removeAll(md);
		});
		
		
		// TODO can method instantiations that return a List<Future> be refactored?

		// A collection should be refactored (or not) as a whole.
		// Gather MethodInvocations instantiations that are getters of a 
		// collection or other initializations of collector items.
		collectionCreationsToUses.forEach((node, use) -> {
			// An instantiation is added to the collection if it shares a use 
			// with it (METHOD_INVOCATION for the collection and 
			// METHOD_PARAMETER for the instantiation).
			List<ASTNode> items = new ArrayList<ASTNode>();
			instantiationUses.forEach((item, itemUse) -> {
				if (use.getOp() == itemUse.getOp())
					items.add(item);
			});
			items.forEach(item -> {				
				if (unsupportedInstantiations.contains(item)) {
					unsupportedCollections.add(node);
				} else collectionInstantiations.put(node, item);
			});
			// An instantiation is a getter if a collection Use is the 
			// MethodInvocation that created it with a collection declaring class.
			if (use.getOp() instanceof MethodInvocation) {
				MethodInvocation methodInv = (MethodInvocation) use.getOp();
				if (instantiations.contains(methodInv)) {
					ITypeBinding declaringClass = methodInv.resolveMethodBinding().getDeclaringClass();
					if (input.collector.isCollection(declaringClass)) {
						if (unsupportedInstantiations.contains(methodInv)) {
							unsupportedCollections.add(node);
						} else collectionGetters.put(node, methodInv);
					}
				}
			}
		});
		
		//TODO dont refactor collections because workers are disabled
		unsupportedCollections.addAll(collectionCreationsToUses.keySet());
		// Removes uses of instantiations within an unsupported Collection
		unsupportedCollections.forEach(c -> {
			unsupportedInstantiations.addAll(collectionGetters.get(c));
			unsupportedInstantiations.addAll(collectionInstantiations.get(c));
			collectionCreationsToUses.removeAll(c);
			collectionGetters.removeAll(c);
			collectionInstantiations.removeAll(c);
		});
		
		// Remove unsupported instantiations from instantiationUses
		unsupportedInstantiations.forEach(i -> 	instantiationUses.removeAll(i));
		
		// Collect identifiers of supported instances and collections
		bindings = getBindings(instantiationUses).keySet();
		collectionBindings = getBindings(collectionCreationsToUses).keySet();
		
		return this;
	}
	
	private Multimap<IVariableBinding, ASTNode> getBindings(Multimap<ASTNode, Use> map) {
		Multimap<IVariableBinding, ASTNode> bindings = HashMultimap.create();
		map.forEach((expr, use) -> {
			if (use.getName()!=null) {
				IBinding binding = use.getName().resolveBinding();
				if (binding instanceof IVariableBinding) {
					bindings.put((IVariableBinding) binding, expr);
				}
			}
		});
		return bindings;
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
