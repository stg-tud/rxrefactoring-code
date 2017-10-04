package de.tudarmstadt.rxrefactoring.ext.future.workers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.Types;


public class FutureSubmitCollector implements IWorker<Void, Multimap<IRewriteCompilationUnit, MethodInvocation>> {

	final Multimap<IRewriteCompilationUnit, MethodInvocation> submitInvocations = HashMultimap.create();
	
	@Override
	public Multimap<IRewriteCompilationUnit, MethodInvocation> refactor(@NonNull IProjectUnits units, @Nullable Void input, @NonNull WorkerSummary summary) throws Exception {		
		units.accept(new FutureVisitor());			
		return submitInvocations;
	}
	
	
	class FutureVisitor extends UnitASTVisitor {
		
		@Override
		public boolean visit(MethodInvocation node) {
			if (node.getExpression() == null) {
				return true;
			}
			
			ITypeBinding expressionType = node.getExpression().resolveTypeBinding();	
			IMethodBinding methodBinding = node.resolveMethodBinding();
			
			if (Types.hasSignature(expressionType, "java.util.concurrent.ExecutorService") 
					&& Methods.hasSignature(methodBinding, null, "submit", "java.util.concurrent.Callable")) {
				submitInvocations.put(getUnit(), node);
				return true;
			}
			
			return true;
		}
	}
	
	

	

}

