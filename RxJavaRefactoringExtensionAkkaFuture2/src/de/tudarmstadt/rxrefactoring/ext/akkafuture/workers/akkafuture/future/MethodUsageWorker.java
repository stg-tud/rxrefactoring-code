package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.akkafuture.future;

import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AbstractAkkaWorker;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.workers.AkkaFutureCollector;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureMethodWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FutureOnFailureWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesMapWrapper;
import de.tudarmstadt.rxrefactoring.ext.akkafuture.wrapper.FuturesSequenceWrapper;

public class MethodUsageWorker extends AbstractAkkaWorker<AkkaFutureCollector, FutureMethodWrapper> {
	public MethodUsageWorker() {
		super("Assignment");
	}

	@Override
	protected Multimap<RewriteCompilationUnit, FutureMethodWrapper> getNodesMap() {
		return collector.futureUsages;
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addSubjectImport(unit);
		addCallableImport(unit);
		addSchedulersImport(unit);
		addAwaitImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, FutureMethodWrapper wrapper) {
		
		if (wrapper instanceof FuturesSequenceWrapper) {
			//Update imports
			addArraysImport(unit);
			addCollectorsImport(unit);
			
			//Update future usage
			FuturesSequenceWrapper w = (FuturesSequenceWrapper) wrapper;		
			MethodInvocation m = w.createZipExpression(unit);			
			unit.replace(w.getExpression(), m);			
		} else if (wrapper instanceof FuturesMapWrapper) {
			//Update imports
			addFunc1Import(unit);
			
			//Update future usage
			FuturesMapWrapper w = (FuturesMapWrapper) wrapper;			
			MethodInvocation m = w.createMapExpression(unit);			
			unit.replace(w.getExpression(), m);			
		} else if (wrapper instanceof FutureOnFailureWrapper) {
			//Update imports
			addAction1Import(unit);
			
			//Update future usage
			FutureOnFailureWrapper w = (FutureOnFailureWrapper) wrapper;			
			MethodInvocation m = w.createOnErrorExpression(unit);			
			unit.replace(w.getExpression(), m);			
		}
	}	
}

