package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use.Kind;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;
import de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation.SubclassInstantiationCollector;

/**
 * Description: Checks preconditions, determines which Expressions should be refactored<br>
 * Author: Camila Gonzalez<br>
 * Created: 23/03/2018
 */
public class PreconditionWorker implements IWorker<SubclassInstantiationCollector, PreconditionWorker> {

	// Maps MethodInvocations and ClassInstanceCreations that return Future or
	// Future subclass instantiations to the latter instance uses.
	public Multimap<ASTNode, Use> instantiationUses = HashMultimap.create();
	
	// Maps MethodDeclarations that are to be refactored to their return statements.
	public Multimap<MethodDeclaration, ASTNode> methodDeclarations = HashMultimap.create();
	
	// Maps MethodDeclarations for which parameters should be refactored to these
	// parameters (within the declaration).
	// public Multimap<MethodDeclaration, ASTNode> methodDeclarationParams = HashMultimap.create();
	
	// Maps MethodInvocations and ClassInstanceCreations (new ArrayList<Future<..>>)
	// that return Future or Future subclass Collections to its uses.
	public Multimap<ASTNode, Use> collectionCreationsToUses = HashMultimap.create();
	
	// Maps Collections to its Iterators
	public Multimap<ASTNode, ASTNode> collectionIterators =  HashMultimap.create();
	
	// Maps a Collection creation to invocation of getter methods (these are 
	// initially considered future instantiations)
	public Multimap<ASTNode, MethodInvocation> collectionGetters = HashMultimap.create();
	
	// Maps a Collection creation to initialization of individual items.
	public Multimap<ASTNode, ASTNode> collectionInstantiations = HashMultimap.create();
	
	// Maps MethodDeclarations to their return instantiations
	public Multimap<MethodDeclaration, ASTNode> collectionMethodDeclarations = HashMultimap.create();
	
	// Maps the left side of an Assignment to the expressions it is assigned
	private Multimap<IVariableBinding, ASTNode> assignments = HashMultimap.create();
	private Multimap<IVariableBinding, ASTNode> collectionAssignments = HashMultimap.create();
	
	// Collect constructs for which the refactoring is not possible
	private Set<ASTNode> unsupportedInstantiations =  new HashSet<ASTNode>();
	private Set<ASTNode> unsupportedCollections = new HashSet<ASTNode>();
	
	// Collection to Uses of collection items within an EnhancedForStatement
	// or a LambdaExpression
	private Multimap<ASTNode, Use> collectionItemUses = HashMultimap.create();	
	
	// Bindings attributed to instances for which the refactoring is supported	
	public Set<IVariableBinding> bindings;
	public Set<IVariableBinding> collectionBindings;	
	public Set<IVariableBinding> iteratorBindings;
	
	ClassInfo classInfo;

	public IRewriteCompilationUnit unit;

	@Override
	public @Nullable PreconditionWorker refactor(@NonNull IProjectUnits units,
			@Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {

		SetMultimap<Expression, Use> exprUses = HashMultimap.create();
		input.analysis.values().forEach(useDef -> exprUses.putAll(useDef.asMap()));
		
		classInfo = input.collector.classInfo;
		
		Set<ASTNode> instantiations = new HashSet<ASTNode>();
		instantiations.addAll(input.collector.methodInvReturnClass);
		instantiations.addAll(input.subclassInstanceCreations);
		instantiations.addAll(input.methodInvReturnSubclass);
		
		Set<ASTNode> collectionCreations = new HashSet<ASTNode>();
		collectionCreations.addAll(input.collector.collectionCreations);
		collectionCreations.addAll(input.subclassCollectionCreations);
		
		// Uses of an Iterator
		Multimap<ASTNode, Use> iteratorUses = HashMultimap.create();
	
		
		exprUses.forEach((expr, use)-> {
				
				// Case 1: The expression is an instantiation that should maybe be refactored
				if (instantiations.contains(expr))
					handleInstantiation(expr, use);
				// Case 2: The expression is a collection that should maybe be refactored
				else if (collectionCreations.contains(expr))
						handleCollection(expr, use);
				else if(Types.isTypeOf(expr.resolveTypeBinding(),"java.util.Iterator"))
					iteratorUses.put(expr, use);
		});
		
		
		// TODO: Workaround because use def analysis does not store uses of fields
		Multimap<IVariableBinding, ASTNode> instBindings = getBindings(instantiationUses);
		Multimap<IVariableBinding, ASTNode> instColBindings = getBindings(collectionCreationsToUses);
		exprUses.forEach((expr, use)-> {
			// If the expression is a SimpleName instead of the creation of that instance,
			// and the name corresponds to a used binding, expr refers to a field within
			// a method that did not initialize the field
			// 'expr' could be e.g. a variable within an assignedForStatement
			if (expr instanceof SimpleName) {
				IBinding b = ((SimpleName)expr).resolveBinding();
				if (instColBindings.containsKey(b)){
					instColBindings.get((IVariableBinding)b).forEach(node -> {
						if (node instanceof Expression) {
							Expression fieldExpr = (Expression) node;
							handleCollection(fieldExpr, use);
						}
					});
				}
			// 'expr' could be e.g. an instance created from a MethodInvocation to a field
			// which is later used
			// Note that this does not happen with Future, because only .get() receives method calls,
			// and it is supported and therefore irrelevant
			} else if (expr instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation)expr;
				if (mi.getExpression() instanceof SimpleName) {
				IBinding b = ((SimpleName)mi.getExpression()).resolveBinding();
				if (instBindings.containsKey(b)) {
					Use newUse = new Use(Kind.METHOD_INVOCATION, (SimpleName)mi.getExpression(), mi);
					instBindings.get((IVariableBinding)b).forEach(node -> {
						Expression fieldExpr = (Expression) node;
						handleInstantiation(fieldExpr, newUse);
					});
				} else if (instColBindings.containsKey(b)) {
					Use newUse = new Use(Kind.METHOD_INVOCATION, (SimpleName)mi.getExpression(), mi);
					instColBindings.get((IVariableBinding)b).forEach(node -> {
						Expression fieldExpr = (Expression) node;
						handleCollection(fieldExpr, newUse);
					});
				}
			}
			}
		});		
		
		// A collection should be refactored (or not) as a whole.
		collectionCreationsToUses.forEach((node, use) -> {
			
			if (use.getOp() instanceof MethodInvocation) {
				MethodInvocation methodInv = (MethodInvocation) use.getOp();
				
				// An instantiation is added to the collection (through e.g. an 'add' method) if they 
				// share a use (METHOD_INVOCATION for the collection and METHOD_PARAMETER for the inst).
				instantiationUses.forEach((item, itemUse) -> {
					if (use.getOp() == itemUse.getOp() && use.getKind() == Kind.METHOD_INVOCATION && 
							itemUse.getKind() == Kind.METHOD_PARAMETER)
						collectionInstantiations.put(node, item);
				});
				
				// An instantiation is a getter if a collection Use is the MethodInvocation that created 
				// it with a collection declaring class.
				if (instantiations.contains(methodInv)) {
					ITypeBinding declaringClass = methodInv.resolveMethodBinding().getDeclaringClass();
					if (input.collector.isCollection(declaringClass))
						collectionGetters.put(node, methodInv);
				}
			
				// Gather Iterators created from a Collection
				else if (iteratorUses.keySet().contains(methodInv)){
					collectionIterators.put(node, methodInv);
					
					
					iteratorUses.get(methodInv).forEach(u -> {
						if (instantiations.contains(u.getOp()) && u.getOp() instanceof MethodInvocation)
							collectionGetters.put(node, (MethodInvocation) u.getOp());
					});
				}
				}
			});
		
		// TODO refactor parameters of method declarations if they are internal
		// Currently, no parameters or instances acting as parameters are refactored (except
		// if the method adds the instance to the collection or gets it)
		
		instantiationUses.forEach((expr, use) -> {
			if (use.getKind() == Kind.METHOD_PARAMETER) {
				MethodInvocation mi = (MethodInvocation) use.getOp();
				if (!collectionGetters.values().contains(mi) && !collectionInstantiations.values().contains(expr))
					unsupportedInstantiations.add(expr);
			}
		});
		
		collectionCreationsToUses.forEach((expr, use) -> {
			if (use.getKind() == Kind.METHOD_PARAMETER)
				unsupportedCollections.add(expr);
		});
		
		
		boolean instantiationChanges = true;
		boolean collectionChanges = true;
		while(instantiationChanges) {
			instantiationChanges = false;
			// Updates unsupportedInstantiations using unsupportedInstantiations
			if (handleUnsupportedAssignments(unsupportedInstantiations, assignments, instantiationUses))
				instantiationChanges = true;
			// Filter out those method declarations for which at least one return statement is not to be refactored,
			// and make sure not to refactor other return statements.
			methodDeclarations = filterMethodDeclarations(methodDeclarations, unsupportedInstantiations);
			// Updates unsupportedInstantiations (return statements) using methodDeclarations
			if (handleUnsupportedMethodDecls(unsupportedInstantiations, methodDeclarations, instantiationUses))
				instantiationChanges = true;
			
			// Filter out those method declarations for which at least one parameter is not to be refactored.
			// methodDeclarationParams = filterMethodDeclarations(methodDeclarationParams, unsupportedInstantiations);		
			
			// Updates unsupportedInstantiations using methodDeclarationParams
			// Updates methodDeclarationParams using unsupportedInstantiations
			// This excludes uses as parameter to a method in collectionGetters and collectionInstantiations
			//if (handleUnsupportedParameters(unsupportedInstantiations, methodDeclarationParams, instantiationUses))
			//	instantiationChanges = true;
			
			while(collectionChanges) {
				collectionChanges = false;
				
				// Removes uses of instantiations within an unsupported Collection
				for (ASTNode c : unsupportedCollections) {
					if (unsupportedInstantiations.addAll(collectionGetters.get(c)))
						instantiationChanges = true;
					if (unsupportedInstantiations.addAll(collectionInstantiations.get(c)))
						instantiationChanges = true;
				}
				
				// Do not refactor collections to which an unsupported instance is added 
				if (extendKeySet(collectionInstantiations, unsupportedCollections, unsupportedInstantiations))
					collectionChanges = true;
				
				// Do not refactor collections for which an item cannot be refactored
				if (extendKeySet(collectionGetters, unsupportedCollections, unsupportedInstantiations))
					collectionChanges = true;
				
				if (handleUnsupportedAssignments(unsupportedCollections, collectionAssignments, collectionCreationsToUses))
					collectionChanges = true;
				
				collectionMethodDeclarations = filterMethodDeclarations(collectionMethodDeclarations, unsupportedCollections);

				if (handleUnsupportedMethodDecls(unsupportedCollections, collectionMethodDeclarations, collectionCreationsToUses))
					collectionChanges = true;

				// External MethodInvocations that create collections (or methods that create collections) 
				// but cannot be refactored because of a later use) are not supported at the moment,
				// so if a collection is created in this manner it is not supported.
				for (ASTNode node : collectionCreationsToUses.keySet()) {
					if (node instanceof MethodInvocation) {
						Optional<MethodDeclaration> methodDecl = internalDeclaration((MethodInvocation) node, collectionMethodDeclarations);
						if (!methodDecl.isPresent())
							if (unsupportedCollections.add(node))
								collectionChanges = true;
					}
				}
			}
		}
		
		// Remove unsupported constructs related to a collection
		unsupportedCollections.forEach(c -> {
			collectionCreationsToUses.removeAll(c);
			collectionGetters.removeAll(c);
			collectionInstantiations.removeAll(c);
			collectionIterators.removeAll(c);
			collectionItemUses.removeAll(c);
		});
		
		// Remove unsupported instantiations from instantiationUses
		unsupportedInstantiations.forEach(i -> 	instantiationUses.removeAll(i));
		
		// Collect identifiers of supported instances and collections
		bindings = new HashSet<IVariableBinding>(getBindings(instantiationUses).keySet());
		collectionBindings = getBindings(collectionCreationsToUses).keySet();
		bindings.addAll(getBindings(collectionItemUses).keySet());
		iteratorBindings = getBindings(Multimaps.filterKeys(iteratorUses, k -> collectionIterators.containsValue(k))).keySet();
		
		return this;
	}

	/**
	 * For a Multimap map with K -> V, add to keySet the elements of K
	 * for which at least one element of V is in valueSet. 
	 * @param map
	 * @param keySet
	 * @param valueSet
	 * @return true if at least one elements was added to keySet.
	 */
	private boolean extendKeySet(Multimap<ASTNode, ? extends ASTNode> map, Set<ASTNode> keySet,
			Set<ASTNode> valueSet) {
		boolean changes = false;
		for(Entry<ASTNode, ? extends ASTNode> e :map.entries()) {
			if (valueSet.contains(e.getValue())) {
				if (keySet.add(e.getKey()))
					changes = true;
			}
		}
		return changes;
	}

	/**
	 * Handles cases where an instantiation is not supported because if it used as a
	 * parameter which cannot be refactored, or a parameter cannot be refactored because
	 * one of its arguments cannot be refactored.
	 * @return were there changes in unsupported?
	 */
	private boolean handleUnsupportedParameters(Set<ASTNode> unsupported,
			Multimap<MethodDeclaration, ASTNode> methodDeclParams, Multimap<ASTNode, Use> map) {
		boolean changesInUnsupported = false;
		
		// If an argument passed to a method cannot be refactored, the method which
		// takes it as parameter should not be refactored
		for (Entry<ASTNode, Use> e : map.entries()) {
			if (e.getValue().getKind() == Kind.METHOD_PARAMETER){
				MethodInvocation mi = (MethodInvocation) e.getValue().getOp();
				if (!collectionGetters.values().contains(mi) && !collectionInstantiations.values().contains(e.getKey())) {
					Optional<MethodDeclaration> methodDecl = internalDeclaration(mi, methodDeclParams);
					// MethodDeclaration is external
					if (!methodDecl.isPresent()) {
						if (unsupported.add(e.getKey()))
							changesInUnsupported = true;
					} else if (unsupported.contains(e.getKey())){
							// TODO: Do not refactor parameters within method
							if (unsupported.addAll(methodDeclParams.get(methodDecl.get())))
								changesInUnsupported = true;
							methodDeclParams.removeAll(methodDecl.get());
							}
				}
			}
		}
		return changesInUnsupported;
	}

	/**
	 * Updates set of MethodDeclarations to remove those for which a returned instance 
	 * cannot be refactored or an unsupported instantiations acts as the method's
	 * return statement.
	 * @param unsupported: set of unsupported expressions
	 * @param methodDeclarations
	 * @param map: map of uses
	 * @return were there changes in unsupported?
	 */
	private boolean handleUnsupportedMethodDecls(Set<ASTNode> unsupported,
			Multimap<MethodDeclaration, ASTNode> methodDeclarations,
			Multimap<ASTNode, Use> map) {
		boolean changesInUnsupported = false;		
		// If an instance created through this method cannot be refactored,
		// the method declaration should not be refactored
		for (ASTNode node : map.keySet()) {
			if(node instanceof MethodInvocation) {
				Optional<MethodDeclaration> methodDecl = internalDeclaration((MethodInvocation) node, methodDeclarations);
				if (methodDecl.isPresent()) {
					if (unsupported.contains(node)) {
						// Do not refactor return statements within method
						if (unsupported.addAll(methodDeclarations.get(methodDecl.get())))
							changesInUnsupported = true;
						// Remove from MethodDeclarations
						methodDeclarations.removeAll(methodDecl.get());
					}
				}
			}
		}
		return changesInUnsupported;
	}
	
	/**
	 * Filter out entries with keys for which at least one value is unsupported, 
	 * and add the remaining associated values to the list of unsupported.
	 */
	private Multimap<MethodDeclaration, ASTNode> filterMethodDeclarations(
			Multimap<MethodDeclaration, ASTNode> unfiltered, Set<ASTNode> unsupported) {
		Set<MethodDeclaration> filterOut = new HashSet<MethodDeclaration>();
		unfiltered.keySet().forEach(k -> {
			if(unfiltered.get(k).stream().anyMatch(value -> unsupported.contains(value))) {
				unsupported.addAll(unfiltered.get(k));
				filterOut.add(k);
			}
		});
		return Multimaps.filterKeys(unfiltered, k -> !filterOut.contains(k));
	}

	/**
	 * Update the list of unsupported instantiation by adding those which are part of an 
	 * assignment with an an expression which is already in the list.
	 * @param unsupported: set of unsupported expressions
	 * @param assignments: map of assignments
	 * @param map: map of uses, from which nodes are obtained for each binding
	 * @return were there changes in unsupported?
	 */
	private boolean handleUnsupportedAssignments(Set<ASTNode> unsupported, Multimap<IVariableBinding, 
			ASTNode> assignments, Multimap<ASTNode, Use> map) {
		boolean changesInUnsupported = false;
		Multimap<IVariableBinding, ASTNode> currentBindings = getBindings(map);
		Set<IVariableBinding> toRemoveBindings = new HashSet<IVariableBinding>();
		// Variable binding to expression it is associated to in that assignment
		for (Entry<IVariableBinding, ASTNode> e :assignments.entries()) {
			// Nodes that are associated with the left side binding
			for(ASTNode n : currentBindings.get(e.getKey())){
				// The left side binding is unsupported
				if (unsupported.contains(n)) {
					toRemoveBindings.add(e.getKey());
					if (unsupported.add(e.getValue())) 
						changesInUnsupported = true;
				// The right side is unsupported
				} else if (unsupported.contains(e.getValue())) { 
					toRemoveBindings.add(e.getKey());
					if (unsupported.add(n))
						changesInUnsupported = true;
				}
						
			}
		}
		toRemoveBindings.forEach(b -> assignments.removeAll(b));
		return changesInUnsupported;
	}

	/**
	 * Associates bindings to expressions they are binded to.
	 * @param expr: the expression
	 * @param assignment: the use
	 * @param assignments: the map where the nodes should be added
	 */
	private void collectAssignments(Expression expr, Assignment assignment,	Multimap<IVariableBinding, 
			ASTNode> assignments) {
		Expression leftSide = assignment.getLeftHandSide();
		if (leftSide instanceof SimpleName) {
			SimpleName name = (SimpleName) leftSide;
			if (name.resolveBinding() instanceof IVariableBinding)
				assignments.put((IVariableBinding)name.resolveBinding(), expr);
		}
	}
	
	
	/**
	 * Handle an instantiations that may have to be refactored. 
	 */
	private void handleInstantiation(Expression expr, Use use) {
		instantiationUses.put(expr, use);
		if (unsupportedUse(use)) {
			unsupportedInstantiations.add(expr);
		} else if (use.getOp() instanceof ReturnStatement) {
			collectMethodDeclarations(expr, (ReturnStatement) use.getOp(), methodDeclarations);
		} else if (use.getOp() instanceof Assignment) {
			collectAssignments(expr, ((Assignment)use.getOp()), assignments);
		};
	}
	
	/**
	 * Handle an instantiations of a collection that may have to be refactored. 
	 */
	private void handleCollection(Expression expr, Use use) {
		collectionCreationsToUses.put(expr, use);
		if (use.getOp() instanceof ReturnStatement) {
			collectMethodDeclarations(expr, (ReturnStatement) use.getOp(), collectionMethodDeclarations);
		} else if (use.getOp() instanceof EnhancedForStatement) {
			// EnhancedForStatement should not be treated as collection uses
			collectionCreationsToUses.remove(expr, use);
		} else if (use.getOp() instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) use.getOp();
			// The Use is a method call within an EnhancedForStatement or LambdaExpression
			// The method receiver is a collection item, not the collection itself
			if(mi.getExpression() != null && 
					!expr.resolveTypeBinding().isAssignmentCompatible(mi.getExpression().resolveTypeBinding())){
				collectionItemUses.put(expr, use);
				collectionCreationsToUses.remove(expr, use);
				if (unsupportedUse(use)) 
					unsupportedCollections.add(expr);
			}
			// The MethodInvocation is a Lambda expressions on a collection
			if(!mi.arguments().isEmpty()) {
				if (mi.arguments().get(0) instanceof LambdaExpression) {
					collectionCreationsToUses.remove(expr, use);
				}
			};		
		} else if (use.getOp() instanceof Assignment) {
			collectAssignments(expr, ((Assignment)use.getOp()), collectionAssignments);
		}
	}
	

	/**
	 * Associates for MethodDeclarations the instantiation of the expression
	 * that they return.
	 * @param expr: the expression that is returned by the method declaration
	 * @param rs: the use
	 * @param methodDeclMap: the map where the nodes should be added
	 */
	private void collectMethodDeclarations(Expression expr, ReturnStatement rs, 
			Multimap<MethodDeclaration, ASTNode> methodDeclMap) {
		Optional<MethodDeclaration> parent = ASTNodes.findParent(rs, MethodDeclaration.class);
		if (parent.isPresent() && 
		expr.resolveTypeBinding().isAssignmentCompatible(parent.get().getReturnType2().resolveBinding())) 
			methodDeclMap.put(parent.get(), expr);
	}


	//TODO maybe find better way, the same is done in FutureCollector
	// If only the bindings are compared there there is an error
	// if the MethodDeclaration is in a different class
	/**
	 * For a MethodInvocation it checks whether the method is in methodDecls.
	 * @param methodInv
	 * @param methodDecls
	 * @return The MethodDeclaration or an empty Optional if the method is declared externally.
	 */
	private Optional<MethodDeclaration> internalDeclaration(MethodInvocation methodInv, 
			Multimap<MethodDeclaration, ASTNode> methodDecls) {
		IMethodBinding binding = methodInv.resolveMethodBinding().getMethodDeclaration();
		for(MethodDeclaration md : methodDecls.keySet()) {
			IMethodBinding decBinding = md.resolveBinding();
			if (binding.toString().equals(decBinding.toString()) &&
					binding.getDeclaringClass().getBinaryName().equals(decBinding.getDeclaringClass().getBinaryName())) 
				return Optional.of(md);
		}
		return Optional.empty();
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
	
	/**
	 * Determines whether a definition corresponds to a parameter in the MethodDeclaration.
	 * @param expr
	 * @return The MethodDeclaration it is parameter to, or None if it is to none.
	 */
	private Optional<MethodDeclaration> methodParameter(Expression expr) {
		if (expr instanceof SimpleName &&
				expr.resolveTypeBinding().getBinaryName().equals(classInfo.getBinaryName())){
			Optional<MethodDeclaration> md = ASTNodes.findParent(expr, MethodDeclaration.class);
			if (md.isPresent()) {
				List params = md.get().parameters();
				for (Object p : params) {
					if (p instanceof SingleVariableDeclaration) {
						SingleVariableDeclaration v = (SingleVariableDeclaration) p;
						if (v.getName().resolveBinding().equals(((SimpleName) expr).resolveBinding()))
							return md;
					}
				}
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Determines whether a Use is not supported.
	 * @param use
	 * @return Whether it is a supported use (true) or not (false).
	 */
	private boolean unsupportedUse(Use use) {
		if (use.getOp() instanceof MethodInvocation) {
			MethodInvocation methodInv = (MethodInvocation) use.getOp();
			if (classInfo.getUnsupportedMethods().contains(methodInv.getName().toString()))
				return true;
		}
		return false;
	}
	

}