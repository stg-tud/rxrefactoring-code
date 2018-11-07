package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
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
//	public final Multimap<IRewriteCompilationUnit, VariableDeclarationFragment> variableDeclarations = HashMultimap.create();
	
	/**
	 * All invocations of Await.result(...)
	 */
	public final Multimap<IRewriteCompilationUnit, AwaitBinding> awaits = HashMultimap.create();
	
	/**
	 * All future creations, e.g., Patterns.ask(...)
	 */
	public final Multimap<IRewriteCompilationUnit, FutureCreationWrapper> futureCreations = HashMultimap.create();
	
	/**
	 * All future method usages, e.g., future.map
	 */
	public final Multimap<IRewriteCompilationUnit, FutureMethodWrapper> futureUsages = HashMultimap.create();
	
	/**
	 * All future references that can not be refactored, e.g., pipe(future, ...)
	 */
	public final Multimap<IRewriteCompilationUnit, Expression> unrefactorableFutureReferences = HashMultimap.create();
	
	
	/**
	 * All types CollectionType<Future<...>>
	 */
	public final Multimap<IRewriteCompilationUnit, ParameterizedType> collectionTypes = HashMultimap.create();
	
	/**
	 * All invocations to collectionOfFutures.add(...)
	 */
//	public final Multimap<IRewriteCompilationUnit, FutureCollectionAccessWrapper> collectionAccess = HashMultimap.create();
	
	
	/**
	 * All variable declarations that should be changed to Observable
	 */
	public final Multimap<IRewriteCompilationUnit, VariableDeclarationFragment> variableDeclarationToObservable = HashMultimap.create();
	/**
	 * All variable declarations that should be changed to Subject.
	 */
	public final Multimap<IRewriteCompilationUnit, VariableDeclarationFragment> variableDeclarationToSubject = HashMultimap.create();
	
	
	@Override
	public AkkaFutureCollector refactor(IProjectUnits units, Void input, WorkerSummary summary) throws Exception {
		
		for (IRewriteCompilationUnit unit : units) {
			
			List<CollectorVisitor> visitors =
					Lists.newArrayList(
							new MethodInvocationVisitor(unit),
							new TypeVisitor(unit),
							new ReturnVisitor(unit),
							new ClassInstanceCreationVisitor(unit)
							);
			
			for (CollectorVisitor v : visitors) {
				unit.accept(v);
			}
		}
		
		summary.setCorrect("numberOfCompilationUnits", units.size());
		
		return this;
	}
	
	private abstract class CollectorVisitor extends ASTVisitor {
		final IRewriteCompilationUnit unit;
		
		public CollectorVisitor(IRewriteCompilationUnit unit) {
			this.unit = unit;
		}
		
	}
	


	
	private class TypeVisitor extends CollectorVisitor {
		
		public TypeVisitor(IRewriteCompilationUnit unit) {
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
	
	private class ReturnVisitor extends CollectorVisitor {

		public ReturnVisitor(IRewriteCompilationUnit unit) {
			super(unit);
		}
		
		public boolean visit(ReturnStatement node) {
			Expression expr = node.getExpression();
			
			
			if (expr != null && FutureTypeWrapper.isAkkaFuture(expr.resolveTypeBinding())) {
				unrefactorableFutureReferences.put(unit, expr);
			}
				
			return true;
		}
		
	}
	
	private class ClassInstanceCreationVisitor extends CollectorVisitor {

		public ClassInstanceCreationVisitor(IRewriteCompilationUnit unit) {
			super(unit);
		}
		
		public boolean visit(ClassInstanceCreation node) {
			unrefactorableFutureReferences.putAll(unit, AkkaFutureASTUtils.futureReferencesInClassInstanceCreation(node));
			return true;
		}
		
	}
	
	private class MethodInvocationVisitor extends CollectorVisitor {

		public MethodInvocationVisitor(IRewriteCompilationUnit unit) {
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
