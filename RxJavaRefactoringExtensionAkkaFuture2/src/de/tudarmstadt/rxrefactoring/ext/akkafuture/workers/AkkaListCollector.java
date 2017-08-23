package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;

public class AkkaListCollector implements IWorker<Void, AkkaListCollector> {

	/**
	 * All types CollectionType<Future<...>>
	 */
	public final Multimap<RewriteCompilationUnit, ParameterizedType> collectionTypes = HashMultimap.create();
	
	/**
	 * All invocations to collectionOfFutures.add(...)
	 */
	public final Multimap<RewriteCompilationUnit, MethodInvocation> addInvocation = HashMultimap.create();
	
	@Override
	public AkkaListCollector refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		for (RewriteCompilationUnit unit : units) {
			
			List<CollectorVisitor> visitors =
					Lists.newArrayList(
							new TypeVisitor(unit),
							new AddMethodVisitor(unit)
							);
			
			for (CollectorVisitor v : visitors) {
				unit.accept(v);
			}
		}
		
		return this;
	}
	
	private boolean isCollectionOfFuture(ITypeBinding binding) {
		if (binding == null || binding.getTypeArguments().length != 1 || !FutureTypeWrapper.isAkkaFuture(binding.getTypeArguments()[0]))
			return false;
									
		//TODO: Check for other collection types
		if (Objects.equals(binding.getBinaryName(), "java.util.ArrayList")) {
			return true;
		}
		
		return false;
	}
	
	private abstract class CollectorVisitor extends ASTVisitor {
		final RewriteCompilationUnit unit;
		
		public CollectorVisitor(RewriteCompilationUnit unit) {
			this.unit = unit;
		}		
	}
	
	
	private class TypeVisitor extends CollectorVisitor {
		
		public TypeVisitor(RewriteCompilationUnit unit) {
			super(unit);			
		}		
		
		@Override
		public boolean visit(ParameterizedType node) {
			
			ITypeBinding typeBinding = node.getType().resolveBinding();										
			
			if (isCollectionOfFuture(typeBinding)) {
				collectionTypes.put(unit, node);
			}
						
			return false;
		}
	}
	
	private class AddMethodVisitor extends CollectorVisitor {

		public AddMethodVisitor(RewriteCompilationUnit unit) {
			super(unit);			
		}
		
		public boolean visit(MethodInvocation node) {
			
			
			if (node.getExpression() != null && isCollectionOfFuture(node.getExpression().resolveTypeBinding()) && Objects.equals(node.getName().getIdentifier(), "add")) {
				addInvocation.put(unit, node);
			}
			
			return true;
		}
		
	}
	
	

}
