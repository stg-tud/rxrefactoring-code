package de.tudarmstadt.rxrefactoring.ext.asynctask.instantiation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

/**
 * Description: Collects declarations of AsyncTask (the target class) subclasses, method invocations that 
 * return it and instance creations that create it (anonymously).<br>
 * Author: Camila Gonzalez<br>
 * Created: 01/03/2018
 */
public class InstantiationCollector implements IWorker<Void, InstantiationCollector> {
	
	// Direct subclasses of the target class that only implement allowed methods.
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> directSubclassDeclarations;
	
	// All indirect subclasses of the target class that do not implement any method that is not 
	// allowed or inherit from a class that does.
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> indirectSubclassDeclarations;
	
	// Instance creations of the target class. Creation of anonymous classes come here.
	public final Multimap<MethodDeclaration, ClassInstanceCreation> classInstanceCreations;
	
	// Method invocations that return an instance of the target class, and for which the returned value is
	// not discarded (is used within a variable assignment, method invocation, class instance creation or return statement)
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvReturnClass;
	
	// List of method names (e.g. "doInBackground") which, if set, define which methods can be implemented 
	// by a subclass for that subclass to be stored in subclassDeclarations. If the subclass declares any 
	// additional methods or fields, or the parameter types are not valid, it is not stored.
	private Optional<List<String>> implementableSubclassMethods = Optional.empty();
	
	// Name of class for which instantiations are searched.
	public String binaryName = "android.os.AsyncTask";
	
	final Multimap<String, String> declaredClassBindingNames;
	
	public InstantiationCollector() 
	{
		directSubclassDeclarations = HashMultimap.create();
		indirectSubclassDeclarations = HashMultimap.create();
		classInstanceCreations = HashMultimap.create();
		methodInvReturnClass = HashMultimap.create();
		declaredClassBindingNames = HashMultimap.create();
	}
	
	public InstantiationCollector(List<String> implementableSubclassMethods) 
	{
		this.implementableSubclassMethods = Optional.ofNullable(implementableSubclassMethods);
		directSubclassDeclarations = HashMultimap.create();
		indirectSubclassDeclarations = HashMultimap.create();
		classInstanceCreations = HashMultimap.create();
		methodInvReturnClass = HashMultimap.create();
		declaredClassBindingNames = HashMultimap.create();
	}
	
	@Override
	public InstantiationCollector refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception 
	{
		FutureInstantiationVisitor visitor = new FutureInstantiationVisitor();
		units.accept(visitor);
		
		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	public class FutureInstantiationVisitor extends UnitASTVisitor{
		
		/**
		 * Collects class definitions in package that inherits from {@link InstantiationCollector#binaryName} directly 
		 * in {@link InstantiationCollector#directSubclassDeclarations} and indirectly in 
		 * {@link InstantiationCollector#indirectSubclassDeclarations}. The maps are disjoint.
		 * If {@link InstantiationCollector#implementableSubclassMethods} is set, only methods from the target class
		 * can be defined and these must match the parameter types of the superclass, otherwise the class declaration is ignored.
		 */
		@Override
		public boolean visit(TypeDeclaration node) 
		{
			String nodeBindingName = node.resolveBinding().getBinaryName();
			LinkedList<ITypeBinding> superTypeBindings = new LinkedList<ITypeBinding>();
			LinkedList<ITypeBinding> checkForMethodValidity = new LinkedList<ITypeBinding>();
			declaredClassBindingNames.put(nodeBindingName, nodeBindingName);
			ITypeBinding targetClass = null;
			boolean directSubclass = false;
			// Add superclass binding, if present
			Type superType = node.getSuperclassType();
			if (superType != null && superType.resolveBinding() != null)
			{
				superTypeBindings.add(superType.resolveBinding());
				declaredClassBindingNames.put(nodeBindingName, superType.resolveBinding().getBinaryName());
				if (binaryName.equals(superType.resolveBinding().getBinaryName()))
					{
						targetClass = superType.resolveBinding();
						directSubclass = true;
					}
			}
			// Add super interface bindings
			for(Object o : node.superInterfaceTypes())
			{
				Type type = (Type) o;
				ITypeBinding typeBinding = type.resolveBinding();
				if (typeBinding!=null)
				{
					superTypeBindings.add(typeBinding);
					declaredClassBindingNames.put(nodeBindingName, typeBinding.getBinaryName());
					if (binaryName.equals(typeBinding.getBinaryName()))
						{
						targetClass = typeBinding;
						directSubclass = true;
						}
				}
			}
			while (!superTypeBindings.isEmpty()) {
				ITypeBinding b = superTypeBindings.removeFirst();
				checkForMethodValidity.add(b);
				if (b.getBinaryName()!= null) 
				{
					if (binaryName.equals(b.getBinaryName())) targetClass = b;
					declaredClassBindingNames.put(nodeBindingName, b.getBinaryName());
					Collections.addAll(superTypeBindings, b.getInterfaces());
					ITypeBinding sb = b.getSuperclass();
					if (sb != null) superTypeBindings.add(sb);
				}
			}
			if (targetClass!=null && onlyAllowedMethods(node.resolveBinding(), targetClass))
			{
				ITypeBinding targetClassStatic = targetClass;
				if (directSubclass) directSubclassDeclarations.put(getUnit(), node);
				else if (checkForMethodValidity.stream().allMatch(x -> {
					String name = x.getBinaryName();
					return name==binaryName || name=="java.lang.Object" || onlyAllowedMethods(x, targetClassStatic);
					})) indirectSubclassDeclarations.put(getUnit(), node);
			}
			return true;
		}
		
		/**
		 * Collects method invocations that return instances of {@link InstantiationCollector#binaryName} in 
		 * {@link InstantiationCollector#methodInvocationsReturnClass} if the result is not discarded (which is to
		 * say if it is used within a variable assignment, method invocation, class instance creation or return statement).
		 */
		@Override
		public boolean visit(MethodInvocation node) 
		{	
			if (node.resolveMethodBinding() == null) return false;
			ITypeBinding returnType = node.resolveMethodBinding().getReturnType();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent() || returnType == null) return true;
			if (binaryName.equals(returnType.getBinaryName()) && returnedValueUsed(node))
				methodInvReturnClass.put(parent.get(), node);
			return true;
		}
		
		/**
		 * Collects instance creations of {@link InstantiationCollector#binaryName} in if the result is not discarded. 
		 */
		@Override
		public boolean visit(ClassInstanceCreation node) 
		{	
			ITypeBinding type = node.getType().resolveBinding();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent() || type == null) return true;
			if (binaryName.equals(type.getBinaryName()) && returnedValueUsed(node))
				classInstanceCreations.put(parent.get(), node);
			return true;
		}
	}
	
	/**
	 * Checks if a class instance creation of method invocation is a strict child (is not one itself) of an 
	 * assignment or return statement, a method invocation (as callee or argument) or a class instance creation.
	 */
	public boolean returnedValueUsed(ASTNode node)
	{
		if (node.getParent() == null) return false;
		if (ASTNodes.findParent(node.getParent(), MethodInvocation.class).isPresent()) return true;
		if (ASTNodes.findParent(node.getParent(), ClassInstanceCreation.class).isPresent()) return true;
		if (ASTNodes.findParent(node, Assignment.class).isPresent()) return true;
		if (ASTNodes.findParent(node, VariableDeclarationStatement.class).isPresent()) return true;
		if (ASTNodes.findParent(node, ReturnStatement.class).isPresent()) return true;
		return false;
	}
	
	/**
	 * Checks if the class only implements the allowed methods and declares no additional fields. The names
	 * of the methods must be in {@link InstantiationCollector#implementableSubclassMethods}, and the parameter 
	 * types must match those of a superclass declaration.
	 */
	private boolean onlyAllowedMethods(ITypeBinding subclass, ITypeBinding superclass) 
	{
		if (implementableSubclassMethods.isPresent())
		{
			if (subclass.getDeclaredFields().length>0) return false;
			IMethodBinding[] superMethodDecl = superclass.getDeclaredMethods();
			IMethodBinding[] methodDecl = subclass.getDeclaredMethods();
			for (IMethodBinding md : methodDecl)
			{
				// Every method declaration name must be in the list of accepted method names
				if (implementableSubclassMethods.get().contains(md.getName()))
				{					
					// There must be at least one method in the superclass with the same name and same parameter types
					Stream<IMethodBinding> smd = Arrays.stream(superMethodDecl).filter(x -> md.getName().equals(x.getName()));
					if (smd.noneMatch(x -> {
						ITypeBinding[] superParams = x.getParameterTypes();
						ITypeBinding[] params = md.getParameterTypes();
						if (superParams.length != params.length) return false;
						for (int i = 0; i<params.length;i++) 
						{
							if (!params[i].equals(superParams[i]))return false;
						}
						return true;
					})) return false;
				} else if (!md.isConstructor())	return false;
			}
		}
		return true;
	}

	@Override
	public InstantiationCollector refactor(IProjectUnits units, Void input, WorkerSummary summary, RefactorScope scope)
			throws Exception {
		// TODO Auto-generated method stub
		// Only needed if RefactorScope is implemented in this extension
		return null;
	}
}

