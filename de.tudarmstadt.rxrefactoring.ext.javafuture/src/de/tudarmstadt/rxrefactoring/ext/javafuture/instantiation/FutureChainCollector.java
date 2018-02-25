package de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

/**
 * Description: Collects class instantiations, method invocations and class declarations for Future 
 * subclasses, distinguishing whether these inherit from classes or interfaces outside the package,
 * such as ScheduledFuture<br>
 * Author: Camila Gonzalez<br>
 * Created: 24/02/2018
 */
public class FutureChainCollector implements IWorker<FutureInstantiationCollector, FutureChainCollector> {

	// TypeDeclarations of classes and interfaces that inherit directly or indirectly from 
	// java.util.concurrent.Future (and java.lang.Object) and optionally from other classes or interfaces 
	// in the package, but not from external ones
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> futureSubclassDeclarationsInternal;
	
	// Map MethodDeclarations to ClassInstanceCreations of Future subclasses
	public final Multimap<MethodDeclaration, ClassInstanceCreation> futureDescInstanceCreationsInternal;
	public final Multimap<MethodDeclaration, ClassInstanceCreation> futureDescInstanceCreations;
	
	// Map MethodDeclarations to MethodInvocations in declaration that return instances of Future subclasses
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvFutureDescInternal;
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvFutureDesc;
	
	// Map MethodDeclarations to MethodInvocations that return a java.util.Collection of a Future subclass
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvDescInternalColl;
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvDescColl;
	
	public Multimap<IRewriteCompilationUnit, TypeDeclaration> subclassesFuture;
	public Multimap<IRewriteCompilationUnit, TypeDeclaration> indirectSubclassesFuture;
	public Multimap<MethodDeclaration, MethodInvocation> methodInvocationsReturnFuture;
	public Multimap<MethodDeclaration, MethodInvocation> methodInvocationsReturnFutureColl;
	public Multimap<String, String> declaredClassBindingNames;
	
	static String futureBinaryName = "java.util.concurrent.Future";
	
	public FutureChainCollector() 
	{
		futureSubclassDeclarationsInternal = HashMultimap.create();
		futureDescInstanceCreationsInternal = HashMultimap.create();
		futureDescInstanceCreations = HashMultimap.create();
		methodInvFutureDescInternal = HashMultimap.create();
		methodInvFutureDesc = HashMultimap.create();
		methodInvDescInternalColl = HashMultimap.create();
		methodInvDescColl = HashMultimap.create();
	}
	
	@Override
	public @Nullable FutureChainCollector refactor(@NonNull IProjectUnits units,
			@Nullable FutureInstantiationCollector input, @NonNull WorkerSummary summary) throws Exception 
	{
		subclassesFuture = input.subclassesFuture;
		indirectSubclassesFuture = input.indirectSubclassesFuture;
		methodInvocationsReturnFuture = input.methodInvocationsReturnFuture;
		methodInvocationsReturnFutureColl = input.methodInvocationsReturnFutureColl;
		declaredClassBindingNames = input.declaredClassBindingNames;
		
		for(Entry<IRewriteCompilationUnit, TypeDeclaration> e : input.subclassesFuture.entries()) addToSubset(e);
		for(Entry<IRewriteCompilationUnit, TypeDeclaration> e : input.indirectSubclassesFuture.entries()) addToSubset(e);
		
		FutureInstantiationVisitor visitor = new FutureInstantiationVisitor();
		units.accept(visitor);
		
		summary.setCorrect("numberOfCompilationUnits", units.size());
		
		return this;
	}
	
	/**
	 * Adds type declaration to {@link FutureChainCollector#futureSubclassDeclarationsInternal} if it inherits from 
	 * {@link java.util.concurrent.Future} (and {@link java.lang.Object}) directly or indirectly, and optionally from
	 * other classes or interfaces defined in the package, but not from external ones.
	 */
	private void addToSubset(Entry<IRewriteCompilationUnit, TypeDeclaration> e)
	{
		String name = e.getValue().resolveBinding().getBinaryName();
		if (name != null && declaredClassBindingNames.get(name).stream().allMatch(s -> 
				s.equals(futureBinaryName) || s.equals("java.lang.Object") || declaredClassBindingNames.keySet().contains(s)
			))
		{
			futureSubclassDeclarationsInternal.put(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * Collects instance creations for {@link java.util.concurrent.Future} subclasses.
	 */
	public class FutureInstantiationVisitor extends UnitASTVisitor
	{
		@Override
		public boolean visit(ClassInstanceCreation node) 
		{
			ITypeBinding type = node.resolveTypeBinding();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent() || type == null || type.getBinaryName() == null) return true;
			if (futureSubclassDeclarationsInternal.values().stream().anyMatch(c -> 
				c.resolveBinding().getBinaryName().equals(type.getBinaryName())
			))
			{
				futureDescInstanceCreationsInternal.put(parent.get(), node);
			}
			else if (implementsFutureIndirec(type))
			{
				futureDescInstanceCreations.put(parent.get(), node);
			}
			return true;
		}	
		
		/**
		 * 
		 * @param t ITypeBinding
		 * @return true if there is a {@link java.util.concurrent.Future} in the inheritance chain
		 */
		private boolean implementsFutureIndirec(ITypeBinding t) 
		{
			LinkedList<ITypeBinding> superInterfaces = new LinkedList<ITypeBinding>();
			superInterfaces.add(t);
			while (!superInterfaces.isEmpty()) {
				ITypeBinding b = superInterfaces.removeFirst();
				if (b != null)
				{
					if (futureBinaryName.equals(b.getBinaryName())) return true;
					Collections.addAll(superInterfaces, b.getInterfaces());
				}
			}
			return false;
		}
		
		/**
		 * Collects method invocations for {@link java.util.concurrent.Future} subclasses.
		 */
		@Override
		public boolean visit(MethodInvocation node) 
		{
			if (node.resolveMethodBinding() == null) return false;
			ITypeBinding returnType = node.resolveMethodBinding().getReturnType();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent() || returnType.getBinaryName()==null) return false;
			if (futureSubclassDeclarationsInternal.values().stream().anyMatch(d -> 
				d.resolveBinding().getBinaryName().equals(returnType.getBinaryName())))
			{
				methodInvFutureDescInternal.put(parent.get(), node);
			}
			else if (implementsFutureIndirec(returnType) && !methodInvocationsReturnFuture.containsEntry(parent.get(), node))
			{
				methodInvFutureDesc.put(parent.get(), node);
			}
			else 
			{
				LinkedList<ITypeBinding> interfaces = new LinkedList<ITypeBinding>();
				Collections.addAll(interfaces, returnType.getInterfaces());
				boolean extendsCollection = false;
				while (!interfaces.isEmpty()) 
				{
					ITypeBinding b = interfaces.removeFirst();
					if ("java.util.Collection".equals(b.getBinaryName()))
						{
						extendsCollection = true;
						break;
						}
					Collections.addAll(interfaces, b.getInterfaces());
				}				
				if (extendsCollection && returnType.getTypeArguments().length>0 && 
						futureSubclassDeclarationsInternal.values().stream().anyMatch(d -> 
						d.resolveBinding().getBinaryName().equals(returnType.getTypeArguments()[0].getBinaryName())))
				{
					methodInvDescInternalColl.put(parent.get(), node);
				}
				else if (extendsCollection && returnType.getTypeArguments().length>0 &&
						implementsFutureIndirec(returnType.getTypeArguments()[0]) && !methodInvocationsReturnFutureColl.containsEntry(parent.get(), node))
				{
					methodInvDescColl.put(parent.get(), node);
				}
			}
			return true;
		}
		
	}

}

