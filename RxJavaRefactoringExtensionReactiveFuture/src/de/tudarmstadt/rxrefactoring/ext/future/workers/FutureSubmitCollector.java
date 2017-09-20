package de.tudarmstadt.rxrefactoring.ext.future.workers;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.UnitASTVisitor;
import de.tudarmstadt.rxrefactoring.core.utils.Methods;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Types;


public class FutureSubmitCollector implements IWorker<Void, Multimap<RewriteCompilationUnit, MethodInvocation>> {

	final Multimap<RewriteCompilationUnit, MethodInvocation> submitInvocations = HashMultimap.create();
	
	@Override
	public Multimap<RewriteCompilationUnit, MethodInvocation> refactor(ProjectUnits units, Void input, WorkerSummary summary) throws Exception {		
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

