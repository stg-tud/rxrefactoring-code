package de.tudarmstadt.rxrefactoring.ext.javafuture.instantiation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;

/**
 * Description: Collects Future class instantiations, method invocations that return Futures and 
 * declarations of Future subclasses<br>
 * Author: Camila Gonzalez<br>
 * Created: 22/02/2018
 */
public class FutureInstantiationCollector implements IWorker<Void, FutureInstantiationCollector> {
	
	// Type declarations that implement java.util.concurrent.Future directly
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> subclassesFuture;
	// Type declarations that implement java.util.concurrent.Future indirectly
	public final Multimap<IRewriteCompilationUnit, TypeDeclaration> indirectSubclassesFuture;
	// Method invocations that return java.util.concurrent.Future
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvocationsReturnFuture;
	// Method invocations that return collections of java.util.concurrent.Future
	public final Multimap<MethodDeclaration, MethodInvocation> methodInvocationsReturnFutureColl;
	// Method declarations that create java.util.concurrent.Future
	public final Multimap<IRewriteCompilationUnit, MethodDeclaration> futureCreators;
	
	final Multimap<String, String> declaredClassBindingNames;
	
	static String futureBinaryName = "java.util.concurrent.Future";
	
	public FutureInstantiationCollector() 
	{
		subclassesFuture = HashMultimap.create();
		indirectSubclassesFuture = HashMultimap.create();
		futureCreators = HashMultimap.create();
		methodInvocationsReturnFuture = HashMultimap.create();
		declaredClassBindingNames = HashMultimap.create();
		methodInvocationsReturnFutureColl = HashMultimap.create();
	}
	
	@Override
	public FutureInstantiationCollector refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception 
	{
		FutureInstantiationVisitor visitor = new FutureInstantiationVisitor();
		units.accept(visitor);
		
		summary.setCorrect("numberOfCompilationUnits", units.size());

		return this;
	}

	public class FutureInstantiationVisitor extends UnitASTVisitor{
		
		/**
		 * Collects class definitions in package that implement the {@link java.util.concurrent.Future} interface
		 * directly in {@link FutureInstantiationCollector#subclassesFuture} and indirectly in 
		 * {@link FutureInstantiationCollector#indirectSubclassesFuture}. The maps are disjoint.
		 */
		@Override
		public boolean visit(TypeDeclaration node) 
		{
			String nodeBindingName = node.resolveBinding().getBinaryName();
			LinkedList<ITypeBinding> superTypeBindings = new LinkedList<ITypeBinding>();
			declaredClassBindingNames.put(nodeBindingName, nodeBindingName);
			// Add superclass binding, if present
			Type superType = node.getSuperclassType();
			boolean directSubclass = false;
			if (superType != null && superType.resolveBinding() != null) superTypeBindings.add(superType.resolveBinding());
			// Add super interface bindings
			for(Object o : node.superInterfaceTypes())
			{
				Type type = (Type) o;
				ITypeBinding typeBinding = type.resolveBinding();
				if (typeBinding!=null) superTypeBindings.add(typeBinding);
				// If it implements future, add to subclassesFuture
				if (futureBinaryName.equals(typeBinding.getBinaryName())) 
				{
					subclassesFuture.put(getUnit(), node);
					directSubclass = true;
				}
			}
			while (!superTypeBindings.isEmpty()) {
				ITypeBinding b = superTypeBindings.removeFirst();
				if (b.getBinaryName()!= null) 
				{
					declaredClassBindingNames.put(nodeBindingName, b.getBinaryName());
					Collections.addAll(superTypeBindings, b.getInterfaces());
					ITypeBinding sb = b.getSuperclass();
					if (sb != null) superTypeBindings.add(sb);
				}
			}
			if (!directSubclass && declaredClassBindingNames.get(nodeBindingName).contains(futureBinaryName))
			{
				indirectSubclassesFuture.put(getUnit(), node);
			}
			return true;
		}
		
		/**
		 * Collects method invocations that return {@link java.util.concurrent.Future}s in 
		 * {@link FutureInstantiationCollector#methodInvocationsReturnFuture} and invocations that return collections of them
		 * (implement {@link java.util.Collection}) in {@link FutureInstantiationCollector#methodInvocationsReturnFutureColl}
		 */
		@Override
		public boolean visit(MethodInvocation node) 
		{	
			if (node.resolveMethodBinding() == null) return false;
			ITypeBinding returnType = node.resolveMethodBinding().getReturnType();
			Optional<MethodDeclaration> parent = ASTNodes.findParent(node, MethodDeclaration.class);
			if (!parent.isPresent()) return false;
			if (futureBinaryName.equals(returnType.getBinaryName())) 
			{
				methodInvocationsReturnFuture.put(parent.get(), node);
			} else {
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
						futureBinaryName.equals(returnType.getTypeArguments()[0].getBinaryName()))
				{
					methodInvocationsReturnFutureColl.put(parent.get(), node);
				}
			}			
			return true;
		}	
		//Finds interface upwards in the hierarchy of the current class
		boolean findInterface(ITypeBinding binding, String interfaceName) 
		{
			LinkedList<ITypeBinding> interfaces = new LinkedList<ITypeBinding>();
			Collections.addAll(interfaces, binding.getInterfaces());
			while (!interfaces.isEmpty()) {
				ITypeBinding b = interfaces.removeFirst();
				if (interfaceName.equals(b.getBinaryName())) return true;
				Collections.addAll(interfaces, b.getInterfaces());
			}
			return false;
		}
		
		/**
		 * Collects method declarations that return {@link java.util.concurrent.Future}s.
		 */
		@Override
		public boolean visit(MethodDeclaration node)
		{
			// If not a constructor
			if (node.getReturnType2() != null)
			{
				ITypeBinding type = node.getReturnType2().resolveBinding();
				if (type != null && futureBinaryName.equals(type.getBinaryName()))
				{
					futureCreators.put(getUnit(), node);
				}
			}
			return true;
		}
		
	}

}

