package de.tudarmstadt.rxrefactoring.ext.javafuture.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef.Use;
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
	
	// Maps MethodDeclarations to their return statements
	public Multimap<MethodDeclaration, ASTNode> methodDeclarations = HashMultimap.create();
	
	// Maps MethodInvocations and ClassInstanceCreations (new ArrayList<Future<..>>)
	// that return Future or Future subclass Collections to its uses.
	public Multimap<ASTNode, Use> collectionCreationsToUses = HashMultimap.create();
	
	// Maps Collections to its Iterators
	public Multimap<ASTNode, ASTNode> collectionIterators =  HashMultimap.create();
	
	// Maps a Collection creation to invocation of getter methods (these are 
	// initially considered future instantiations)
	public Multimap<ASTNode, MethodInvocation> collectionGetters = HashMultimap.create();
	
	// Maps a Collection creation to initialization of individual futures.
	public Multimap<ASTNode, ASTNode> collectionInstantiations = HashMultimap.create();
	
	// Maps MethodDeclarations to their return instantiations
	public Multimap<MethodDeclaration, ASTNode> collectionMethodDeclarations = HashMultimap.create();
	
	// Bindings attributed to instances for which the refactoring is supported	
	public Set<IVariableBinding> bindings;
	public Set<IVariableBinding> collectionBindings;	
	public Set<IVariableBinding> iteratorBindings;
	
	ClassInfo classInfo;
	
	public Map<ASTNode, UseDef> anal;

	public IRewriteCompilationUnit unit;

	@Override
	public @Nullable PreconditionWorker refactor(@NonNull IProjectUnits units,
			@Nullable SubclassInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {

		Set<UseDef> useDefs = new HashSet<UseDef>();
		useDefs.addAll(input.analysis.values());
		
		//TODO only to print, remove
		anal = input.analysis;
		
		classInfo = input.collector.classInfo;
		
		Set<ASTNode> instantiations = new HashSet<ASTNode>();
		instantiations.addAll(input.collector.methodInvReturnClass);
		instantiations.addAll(input.subclassInstanceCreations);
		instantiations.addAll(input.methodInvReturnSubclass);
		
		Set<ASTNode> collectionCreations = new HashSet<ASTNode>();
		collectionCreations.addAll(input.collector.collectionCreations);
		collectionCreations.addAll(input.subclassCollectionCreations);
		
		// Maps the left side of an Assignment to the expressions it is assigned
		Multimap<IVariableBinding, ASTNode> assignments = HashMultimap.create();
		
		// Determine whether the refactoring is possible
		Set<ASTNode> unsupportedInstantiations =  new HashSet<ASTNode>();
		Set<ASTNode> unsupportedCollections = new HashSet<ASTNode>();
		Set<MethodDeclaration> unsupportedMethodDecl = new HashSet<MethodDeclaration>();
		Set<MethodDeclaration> unsupportedCollectionMethodDecl = new HashSet<MethodDeclaration>();
		
		Multimap<ASTNode, Use> iteratorUses = HashMultimap.create();
		// Collection to Uses of collection items within an EnhancedForStatement
		// or a LambdaExpression
		Multimap<ASTNode, Use> collectionItemUses = HashMultimap.create();
		
		// Collect instantiationUses and unsupportedInstantiations
		useDefs.forEach(useDef -> {
			Set<Expression> definitions = useDef.asMap().keySet();
			definitions.forEach(expr -> {
				Set<Use> exprUses = useDef.getUses(expr);
				// Case 1: The expression is an instantiation that should maybe be refactored
				if (instantiations.contains(expr))
					exprUses.forEach(use -> {
						instantiationUses.put(expr, use);
						// Callee of not supported method
						if (unsupportedMethodCall(use)) {
							unsupportedInstantiations.add(expr);
						} else if (use.getOp() instanceof ReturnStatement) {
							Optional<MethodDeclaration> maybeParent = ASTNodes.findParent(use.getOp(), MethodDeclaration.class);
							if (maybeParent.isPresent() && 
									expr.resolveTypeBinding().isAssignmentCompatible(maybeParent.get().getReturnType2().resolveBinding())) {
								
								MethodDeclaration parent = maybeParent.get();
								
								methodDeclarations.put(parent, expr);
								
//								for (SingleVariableDeclaration variable : (List<SingleVariableDeclaration>) parent.parameters()) {
//									IVariableBinding binding = variable.resolveBinding();
//									if (binding != null) {
//										bindings.add(binding);
//									}
//								}								
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
				// Case 2: The expression is a collection that should maybe be refactored
				else if (collectionCreations.contains(expr))
					exprUses.forEach(use -> {
						collectionCreationsToUses.put(expr, use);
						
						//Puts collection creations as instantiationUses keys. foreachs are added above
						//instantiationUses.put(expr, use); //TODO: This is very ugly. A foreach statement is viewed as assigning a collection to a future. This is why we refactor it here.
						
						if (use.getOp() instanceof ReturnStatement) {
							Optional<MethodDeclaration> parent = ASTNodes.findParent(use.getOp(), MethodDeclaration.class);
							if (parent.isPresent() && 
									expr.resolveTypeBinding().isAssignmentCompatible(parent.get().getReturnType2().resolveBinding())) {
								collectionMethodDeclarations.put(parent.get(), expr);
							}
						} else if (use.getOp() instanceof EnhancedForStatement) {
							collectionCreationsToUses.remove(expr, use);
						} else if (use.getOp() instanceof MethodInvocation) {
							MethodInvocation mi = (MethodInvocation) use.getOp();
							// The Use is a method call within an EnhancedForStatement or LambdaExpression
							// The method receiver is a collection item, not the collection itself
							if(expr.resolveTypeBinding()!=mi.getExpression().resolveTypeBinding()){
								collectionItemUses.put(expr, use);
								collectionCreationsToUses.remove(expr, use);
								if (unsupportedMethodCall(use)) 
									unsupportedCollections.add(expr);
							}
							// The MethodInvocation is a Lambda expressions on a collection
							if(!mi.arguments().isEmpty()) {
								if (mi.arguments().get(0) instanceof LambdaExpression) {
									collectionCreationsToUses.remove(expr, use);
								}
							};		
						}
					});
				else if(Types.isTypeOf(expr.resolveTypeBinding(),"java.util.Iterator"))
					exprUses.forEach(use -> {
						iteratorUses.put(expr, use);
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
				Optional<MethodDeclaration> methodDecl = internalDecl((MethodInvocation) node, methodDeclarations);
				if (methodDecl.isPresent()) {
					if (unsupportedInstantiations.contains(node))
						unsupportedMethodDecl.add(methodDecl.get());
				}
			}
		});
		
		// Removes uses of the instantiation which is the return statement
		// within an unsupported MethodDeclaration
		unsupportedMethodDecl.forEach(md -> {
			unsupportedInstantiations.addAll(methodDeclarations.get(md));
			methodDeclarations.removeAll(md);
		});

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
			if (use.getOp() instanceof MethodInvocation) {
				MethodInvocation methodInv = (MethodInvocation) use.getOp();
				// An instantiation is a getter if a collection Use is the 
				// MethodInvocation that created it with a collection declaring class.
				if (instantiations.contains(methodInv)) {
					ITypeBinding declaringClass = methodInv.resolveMethodBinding().getDeclaringClass();
					if (input.collector.isCollection(declaringClass)) {
						if (unsupportedInstantiations.contains(methodInv)) {
							unsupportedCollections.add(node);
						} else collectionGetters.put(node, methodInv);
					}
				}
				// Gather Iterators created from a Collection
				else if (iteratorUses.keySet().contains(methodInv)){
					collectionIterators.put(node, methodInv);
				}
			}
		});
		
		
		collectionIterators.forEach((c, i) -> {
			iteratorUses.get(i).forEach(use -> {				
				if (instantiations.contains(use.getOp()) && use.getOp() instanceof MethodInvocation) 
					if (unsupportedInstantiations.contains(use.getOp())) {
						unsupportedCollections.add(c);
					}
					else
						collectionGetters.put(c, (MethodInvocation) use.getOp());
			});
		});
		
		// Single out unsupported MethodDeclarations that return Collections and remove them
		collectionCreationsToUses.keySet().forEach(node -> {
			if(node instanceof MethodInvocation) {
				Optional<MethodDeclaration> methodDecl = internalDecl((MethodInvocation) node, collectionMethodDeclarations);
				if (methodDecl.isPresent()) {
					if (unsupportedCollections.contains(node)) 
						unsupportedCollectionMethodDecl.add(methodDecl.get());
				}
			}
		});	
		unsupportedCollectionMethodDecl.forEach(md -> {
			unsupportedCollections.addAll(collectionMethodDeclarations.get(md));
			collectionMethodDeclarations.removeAll(md);
		});
		
		// Put MethodInvocations not declared in class into unsupportedCollections
		collectionCreationsToUses.keySet().forEach(node -> {
			if (node instanceof MethodInvocation) {
				Optional<MethodDeclaration> methodDecl = internalDecl((MethodInvocation) node, collectionMethodDeclarations);
				if (!methodDecl.isPresent())
					unsupportedCollections.add(node);
			}
		});
		
		// Removes uses of instantiations within an unsupported Collection
		unsupportedCollections.forEach(c -> {
			unsupportedInstantiations.addAll(collectionGetters.get(c));
			unsupportedInstantiations.addAll(collectionInstantiations.get(c));
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
	
	//TODO maybe find better way, the same is done in FutureCollector
	// If only the bindings are compared there there is an error
	// if the MethodDeclaration is in a different class
	/**
	 * For a MethodInvocation it checks whether the method is in methodDecl.
	 * @param methodInv
	 * @param methodDecls
	 * @return The MethodDeclaration or an empty Optional if the method is declared externally.
	 */
	private Optional<MethodDeclaration> internalDecl(MethodInvocation methodInv, Multimap<MethodDeclaration, ASTNode> methodDecls) {
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
	
	private boolean unsupportedMethodCall(Use use) {
		if (use.getOp() instanceof MethodInvocation) {
			MethodInvocation methodInv = (MethodInvocation) use.getOp();
			if (classInfo.getUnsupportedMethods().contains(methodInv.getName().toString()))
				return true;
		}
		return false;
	}
	
	private boolean implementsInterface(ITypeBinding binding, String interfaceBinaryName) {
		if (binding == null) {
			return false;
		}
		
		List<ITypeBinding> bindings = new ArrayList<ITypeBinding>(Arrays.asList(binding.getInterfaces()));
		bindings.add(binding);
		for (ITypeBinding b : bindings) {
			if (b != null && Objects.equals(b.getBinaryName(), interfaceBinaryName))
				return true;
		}
		return false;
	}

}