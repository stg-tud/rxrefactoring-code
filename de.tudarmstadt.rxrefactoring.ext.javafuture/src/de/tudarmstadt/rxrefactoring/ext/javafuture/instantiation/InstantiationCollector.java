package de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Type;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.ext.javafuture.domain.ClassInfo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.analysis.impl.reachingdefinitions.UseDef;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

/**
 * Description: Collects method invocations that return Futures and Future
 * collections and declarations of Future subclasses<br>
 * Author: Camila Gonzalez<br>
 * Created: 22/02/2018
 */
public class InstantiationCollector implements IWorker<Map<ASTNode, UseDef>, InstantiationCollector> {
	
	// Method invocations that return java.util.concurrent.Future, and for which the
	// returned value is not discarded.
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvReturnClass;

	// Method invocations that return collections of java.util.concurrent.Future,
	// and for which the returned
	// value is not discarded.
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvReturnCollection;

	// Type declarations that implement java.util.concurrent.Future directly and
	// only implement allowed methods, if these are provided.
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> directSubclassDeclarations;

	// Type declarations that implement java.util.concurrent.Future indirectly and
	// only implement allowed methods, if provided.
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> indirectSubclassDeclarations;

	// Only if set to true are subclass declarations and their instantiations
	// collected.
	private boolean collectSubclasses = false;
	
	// If the subclass declares any unsupported methods or additional methods or 
	// fields, or the parameter types are not valid, it is not stored.
	private final ClassInfo classInfo;

	public String binaryName = "java.util.concurrent.Future";

	final Multimap<String, String> declaredClassBindingNames;

	public Map<ASTNode, UseDef> analysis;

	public InstantiationCollector(ClassInfo classInfo, boolean collectSubclasses) {
		this.classInfo = classInfo;
		this.collectSubclasses = collectSubclasses;
		directSubclassDeclarations = HashMultimap.create();
		indirectSubclassDeclarations = HashMultimap.create();
		methodInvReturnClass = HashMultimap.create();
		declaredClassBindingNames = HashMultimap.create();
		methodInvReturnCollection = HashMultimap.create();
	}

	public InstantiationCollector(ClassInfo classInfo) {
		this.classInfo = classInfo;
		directSubclassDeclarations = HashMultimap.create();
		indirectSubclassDeclarations = HashMultimap.create();
		methodInvReturnClass = HashMultimap.create();
		declaredClassBindingNames = HashMultimap.create();
		methodInvReturnCollection = HashMultimap.create();
	}

	@Override
	public InstantiationCollector refactor(IProjectUnits units, Map<ASTNode, UseDef> input, WorkerSummary summary)
			throws Exception {
		FutureInstantiationVisitor visitor = new FutureInstantiationVisitor();
		analysis = input;

		units.accept(visitor);

		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	public class FutureInstantiationVisitor extends UnitASTVisitor {

		/**
		 * Collects class definitions in package that implement the
		 * {@link java.util.concurrent.Future} interface directly in
		 * {@link InstantiationCollector#directSubclassDeclarations} and indirectly in
		 * {@link InstantiationCollector#indirectSubclassDeclarations}. The maps are
		 * disjoint.
		 */
		@Override
		public boolean visit(TypeDeclaration node) {
			if (collectSubclasses) {
				String nodeBindingName = node.resolveBinding().getBinaryName();
				LinkedList<ITypeBinding> superTypeBindings = new LinkedList<ITypeBinding>();
				LinkedList<ITypeBinding> checkForMethodValidity = new LinkedList<ITypeBinding>();
				declaredClassBindingNames.put(nodeBindingName, nodeBindingName);
				ITypeBinding targetClass = null;
				boolean directSubclass = false;
				// Add superclass binding, if present
				Type superType = node.getSuperclassType();
				if (superType != null && superType.resolveBinding() != null) {
					superTypeBindings.add(superType.resolveBinding());
					declaredClassBindingNames.put(nodeBindingName, superType.resolveBinding().getBinaryName());
					if (binaryName.equals(superType.resolveBinding().getBinaryName())) {
						targetClass = superType.resolveBinding();
						directSubclass = true;
					}
				}
				// Add super interface bindings
				for (Object o : node.superInterfaceTypes()) {
					Type type = (Type) o;
					ITypeBinding typeBinding = type.resolveBinding();
					if (typeBinding != null) {
						superTypeBindings.add(typeBinding);
						declaredClassBindingNames.put(nodeBindingName, typeBinding.getBinaryName());
						if (binaryName.equals(typeBinding.getBinaryName())) {
							targetClass = typeBinding;
							directSubclass = true;
						}
					}
				}
				while (!superTypeBindings.isEmpty()) {
					ITypeBinding b = superTypeBindings.removeFirst();
					checkForMethodValidity.add(b);
					if (b.getBinaryName() != null) {
						if (binaryName.equals(b.getBinaryName()))
							targetClass = b;
						declaredClassBindingNames.put(nodeBindingName, b.getBinaryName());
						Collections.addAll(superTypeBindings, b.getInterfaces());
						ITypeBinding sb = b.getSuperclass();
						if (sb != null)
							superTypeBindings.add(sb);
					}
				}
				if (targetClass != null && onlyAllowedMethods(node.resolveBinding(), targetClass)) {
					ITypeBinding targetClassStatic = targetClass;
					if (directSubclass)
						directSubclassDeclarations.put(getUnit(), node);
					else if (checkForMethodValidity.stream().allMatch(x -> {
						String name = x.getBinaryName();
						return name == binaryName || name == "java.lang.Object" || onlyAllowedMethods(x, targetClassStatic);
					}))
						indirectSubclassDeclarations.put(getUnit(), node);
				}
			}
			return true;
		}

		/**
		 * Collects method invocations that return {@link java.util.concurrent.Future}s
		 * (which are later used) in
		 * {@link InstantiationCollector#methodInvReturnClass} and invocations
		 * that return collections of them (implement {@link java.util.Collection}) in
		 * {@link InstantiationCollector#methodInvReturnCollection}
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			if (node.resolveMethodBinding() == null)
				return false;
			ITypeBinding returnType = node.resolveMethodBinding().getReturnType();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent() || returnType == null)
				return true;
			if (binaryName.equals(returnType.getBinaryName()) && returnedValueUsed(node))
				methodInvReturnClass.put(parent.get(), node);
			else {
				if (isCollectionOfClass(returnType) && returnedValueUsed(node)
						&& returnType.getTypeArguments().length > 0
						&& binaryName.equals(returnType.getTypeArguments()[0].getBinaryName())) {
					methodInvReturnCollection.put(parent.get(), node);
				}
			}
			return true;
		}
	}

	/**
	 * @return true if the type is a collection.
	 */
	public boolean isCollectionOfClass(ITypeBinding type) {
		LinkedList<ITypeBinding> interfaces = new LinkedList<ITypeBinding>();
		Collections.addAll(interfaces, type.getInterfaces());
		while (!interfaces.isEmpty()) {
			ITypeBinding b = interfaces.removeFirst();
			if ("java.util.Collection".equals(b.getBinaryName())) {
				return true;
			}
			Collections.addAll(interfaces, b.getInterfaces());
		}
		return false;
	}

	/**
	 * Checks if a class instance creation of method invocation is a strict child
	 * (is not one itself) of an assignment or return statement, a method invocation
	 * (as callee or argument) or a class instance creation.
	 */
	public boolean returnedValueUsed(ASTNode node) {
		if (node.getParent() == null)
			return false;
		if (ASTNodes.findParent(node.getParent(), MethodInvocation.class).isPresent())
			return true;
		if (ASTNodes.findParent(node.getParent(), ClassInstanceCreation.class).isPresent())
			return true;
		if (ASTNodes.findParent(node, Assignment.class).isPresent())
			return true;
		if (ASTNodes.findParent(node, VariableDeclarationStatement.class).isPresent())
			return true;
		if (ASTNodes.findParent(node, ReturnStatement.class).isPresent())
			return true;
		return false;
	}

	/**
	 * Checks if the class only implements the allowed methods (public methods) 
	 * that are not unsupported and declares no additional fields.
	 */
	private boolean onlyAllowedMethods(ITypeBinding subclass, ITypeBinding superclass) {

		if (subclass.getDeclaredFields().length > 0)
			return false;
		IMethodBinding[] superMethodDecl = superclass.getDeclaredMethods();
		IMethodBinding[] methodDecl = subclass.getDeclaredMethods();
		for (IMethodBinding md : methodDecl) {
			// Every method declaration name must be a supported Future public method
			if (classInfo.getPublicMethods().contains(md.getName()) &&
					!classInfo.getUnsupportedMethods().contains(md.getName())) {
				// There must be at least one method in the superclass with the same name and
				// same parameter types
				Stream<IMethodBinding> smd = Arrays.stream(superMethodDecl)
						.filter(x -> md.getName().equals(x.getName()));
				if (smd.noneMatch(x -> {
					ITypeBinding[] superParams = x.getParameterTypes();
					ITypeBinding[] params = md.getParameterTypes();
					if (superParams.length != params.length)
						return false;
					for (int i = 0; i < params.length; i++) {
						if (!params[i].equals(superParams[i]))
							return false;
					}
					return true;
				}))
					return false;
			} else if (!md.isConstructor())
				return false;
		}	
		return true;
	}

}
