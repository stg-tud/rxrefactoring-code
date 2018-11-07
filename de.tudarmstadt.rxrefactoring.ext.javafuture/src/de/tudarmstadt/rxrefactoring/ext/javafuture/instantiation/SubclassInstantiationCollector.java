package de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation;

import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

/**
 * Description: Collects class instantiations, method invocations and class
 * declarations for subclasses declared in the package.<br>
 * Author: Camila Gonzalez<br>
 * Created: 24/02/2018
 */
public class SubclassInstantiationCollector implements IWorker<InstantiationCollector, SubclassInstantiationCollector> {

	// TypeDeclarations of classes and interfaces that inherit directly or
	// indirectly from binaryName and implement only 
	// allowed methods. If excludeExternal is set, only classes for which 
	// the entire inheritance chain is in the package (except for 
	// binaryName and java.lang.Object) are included.
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> subclassDeclarations;

	// ClassInstanceCreations of classes in subclassDeclarations if the result is not discarded.
	public final Set<ClassInstanceCreation> subclassInstanceCreations;

	// MethodInvocations in declaration that return instances of classes 
	// in subclassDeclarations and do not discard the result.
	public final Set<MethodInvocation> methodInvReturnSubclass;

	// MethodInvocations or ClassInstanceCreations 
	// that return a java.util.Collection or ArrayCreations of a class in subclassDeclarations 
	// and do not discard the result.
	public final Set<ASTNode> subclassCollectionCreations;

	// Is set to true, only classes are included in subclassDeclarations that do not
	// inherit from any class outside the package except for the target class and 
	// java.lang.Object.
	private boolean excludeExternal = true;
	private Multimap<String, String> declaredClassBindingNames;
	private String binaryName;
	public InstantiationCollector collector;
	public Map<ASTNode, UseDef> analysis;

	public SubclassInstantiationCollector() {
		subclassDeclarations = HashMultimap.create();
		subclassInstanceCreations = new HashSet<ClassInstanceCreation>();
		methodInvReturnSubclass = new HashSet<MethodInvocation>();
		subclassCollectionCreations = new HashSet<ASTNode>();
	}

	public SubclassInstantiationCollector(boolean excludeExternal) {
		subclassDeclarations = HashMultimap.create();
		subclassInstanceCreations = new HashSet<ClassInstanceCreation>();
		methodInvReturnSubclass = new HashSet<MethodInvocation>();
		subclassCollectionCreations = new HashSet<ASTNode>();
		this.excludeExternal = excludeExternal;
	}

	@Override
	public @Nullable SubclassInstantiationCollector refactor(@NonNull IProjectUnits units,
			@Nullable InstantiationCollector input, @NonNull WorkerSummary summary) throws Exception {
		analysis = input.analysis;
		binaryName = input.binaryName;
		declaredClassBindingNames = input.declaredClassBindingNames;
		
		collector = input;

		for (Entry<IRewriteCompilationUnit, TypeDeclaration> e : input.directSubclassDeclarations.entries())
			addToSubset(e);
		for (Entry<IRewriteCompilationUnit, TypeDeclaration> e : input.indirectSubclassDeclarations.entries())
			addToSubset(e);
		
		if (!subclassDeclarations.isEmpty()) {
			InstantiationVisitor visitor = new InstantiationVisitor();
			units.accept(visitor);
		}

		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	/**
	 * Adds type declarations of subclasses to
	 * {@link SubclassInstantiationCollector#subclassDeclarations}.
	 */
	private void addToSubset(Entry<IRewriteCompilationUnit, TypeDeclaration> e) {
		String name = e.getValue().resolveBinding().getBinaryName();
		if (excludeExternal) {
			if (name != null && declaredClassBindingNames.get(name).stream().allMatch(s -> s.equals(binaryName)
					|| s.equals("java.lang.Object") || declaredClassBindingNames.keySet().contains(s))) {
				subclassDeclarations.put(e.getKey(), e.getValue());
			}
		} else
			subclassDeclarations.put(e.getKey(), e.getValue());
	}
	
	/**
	 * Collects instance creations for {@link InstantiationCollector#binaryName}
	 * subclasses.
	 */
	public class InstantiationVisitor extends UnitASTVisitor {
		/**
		 * Collects instance creations for classes in
		 * {@link SubclassInstantiationCollector#subclassDeclarations} if they are not
		 * discarded.
		 */
		@Override
		public boolean visit(ClassInstanceCreation node) {
			ITypeBinding type = node.resolveTypeBinding();
			//Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (type == null)
				return true;
			if (collector.returnedValueUsed(node) && subclassDeclarations.values().stream()
					.anyMatch(c -> c.resolveBinding().getBinaryName().equals(type.getBinaryName()))) {
				subclassInstanceCreations.add(node);
			}
			if (collector.isCollection(type) && collector.returnedValueUsed(node)
					&& type.getTypeArguments().length > 0
					&& subclassDeclarations.values().stream().anyMatch(d -> d.resolveBinding().getBinaryName()
							.equals(type.getTypeArguments()[0].getBinaryName()))) {
				subclassCollectionCreations.add(node);
			}
			return true;
		}
		
		/**
		 * Collects array creations of {@link InstantiationCollector#binaryName} subclasses.
		 */
		@Override
		public boolean visit(ArrayCreation node) {
			//Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (collector.returnedValueUsed(node) &&
					subclassDeclarations.values().stream().anyMatch(d -> d.resolveBinding().getBinaryName()
							.equals(node.resolveTypeBinding().getElementType().getBinaryName())))
				subclassCollectionCreations.add(node);
			return true;
		}

		/**
		 * Collects method invocations that return instances of classes in
		 * {@link SubclassInstantiationCollector#subclassDeclarations} if they are not
		 * discarded.
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			if (node.resolveMethodBinding() == null)
				return false;
			ITypeBinding returnType = node.resolveMethodBinding().getReturnType();
			//Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (returnType == null)
				return true;
			if (collector.returnedValueUsed(node) && subclassDeclarations.values().stream()
					.anyMatch(c -> c.resolveBinding().getBinaryName().equals(returnType.getBinaryName()))) {
				methodInvReturnSubclass.add(node);
			} else {
				if (collector.isCollection(returnType) && collector.returnedValueUsed(node)
						&& returnType.getTypeArguments().length > 0
						&& subclassDeclarations.values().stream().anyMatch(d -> d.resolveBinding().getBinaryName()
								.equals(returnType.getTypeArguments()[0].getBinaryName()))) {
					subclassCollectionCreations.add(node);
				}
			}
			return true;
		}

	}

}
