package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.domain.ClassInfos;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.utils.AkkaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.AwaitBinding;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCollectionAccessWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureCreationWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureMethodWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureTypeWrapper;

public class AkkaFutureCollector implements IWorker<Void, AkkaFutureCollector> {

	
	/**
	 * All variable declarations Future<...> futureVaribale = ...
	 */
//	public final Multimap<RewriteCompilationUnit, VariableDeclarationFragment> variableDeclarations = HashMultimap.create();
	
	/**
	 * All invocations of Await.result(...)
	 */
	public final Multimap<RewriteCompilationUnit, AwaitBinding> awaits = HashMultimap.create();
	
	/**
	 * All future creations, e.g., Patterns.ask(...)
	 */
	public final Multimap<RewriteCompilationUnit, FutureCreationWrapper> futureCreations = HashMultimap.create();
	
	/**
	 * All future method usages, e.g., future.map
	 */
	public final Multimap<RewriteCompilationUnit, FutureMethodWrapper> futureUsages = HashMultimap.create();
	
	/**
	 * All future references that can not be refactored, e.g., pipe(future, ...)
	 */
	public final Multimap<RewriteCompilationUnit, Expression> unrefactorableFutureReferences = HashMultimap.create();
	
	
	/**
	 * All types CollectionType<Future<...>>
	 */
	public final Multimap<RewriteCompilationUnit, ParameterizedType> collectionTypes = HashMultimap.create();
	
	/**
	 * All invocations to collectionOfFutures.add(...)
	 */
//	public final Multimap<RewriteCompilationUnit, FutureCollectionAccessWrapper> collectionAccess = HashMultimap.create();
	
	
	/**
	 * All variable declarations that should be changed to Observable
	 */
	public final Multimap<RewriteCompilationUnit, VariableDeclarationFragment> variableDeclarationToObservable = HashMultimap.create();
	/**
	 * All variable declarations that should be changed to Subject.
	 */
	public final Multimap<RewriteCompilationUnit, VariableDeclarationFragment> variableDeclarationToSubject = HashMultimap.create();
	
	
	@Override
	public AkkaFutureCollector refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		for (RewriteCompilationUnit unit : units) {
			
			List<CollectorVisitor> visitors =
					Lists.newArrayList(
							new MethodInvocationVisitor(unit),
							new TypeVisitor(unit)
							);
			
			for (CollectorVisitor v : visitors) {
				unit.accept(v);
			}
		}
		
		summary.setCorrect("numberOfCompilationUnits", units.size());
		
		return this;
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
			
			if (AkkaFutureASTUtils.isCollectionOfFuture(typeBinding)) {
				collectionTypes.put(unit, node);
			}
						
			return false;
		}
	}
	
	private class MethodInvocationVisitor extends CollectorVisitor {

		public MethodInvocationVisitor(RewriteCompilationUnit unit) {
			super(unit);			
		}
		
		public boolean visit(MethodInvocation node) {
			
			AwaitBinding await = AwaitBinding.create(node);
			if (await != null) {
				awaits.put(unit, await);
			}
			
			VariableDeclarationFragment parent = node.getParent() instanceof VariableDeclarationFragment ? (VariableDeclarationFragment) node.getParent() : null;
				
			if (FutureMethodWrapper.isFutureMethod(node)) {
				futureUsages.put(unit, FutureMethodWrapper.createFromExpression(node));
				
				if (parent != null) {
					variableDeclarationToObservable.put(unit, parent);
				}
//			
			} else if (FutureCreationWrapper.isFutureCreation(node)) {
				futureCreations.put(unit, FutureCreationWrapper.create(node));
				
				if (parent != null) {
					variableDeclarationToSubject.put(unit, parent);
				}
				
			} else if (await == null && !FutureCollectionAccessWrapper.isCollectionAccess(node)) {				
				unrefactorableFutureReferences.putAll(unit, AkkaFutureASTUtils.futureReferencesInMethodInvocation(node));				
			}	
			
			return true;
		}
		
	}

}
