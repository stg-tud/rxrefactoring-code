package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;


public abstract class AbstractAkkaFutureWorker<CollectorType, RefactorType> implements IWorker<CollectorType, Void> {

	protected final String nodeName;
	protected CollectorType collector;
	protected WorkerSummary summary;
	
	public AbstractAkkaFutureWorker(String nodeName) {
		this.nodeName = nodeName;
	}
	
	protected String getNodeName() {
		return nodeName;
	}
	
	protected abstract Multimap<IRewriteCompilationUnit, RefactorType> getNodesMap();
	
	protected abstract void refactorNode(IRewriteCompilationUnit unit, RefactorType node);
	
	protected void startRefactorNode(IRewriteCompilationUnit unit) {
		
	}
	
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		
	}
	
	@Override
	public Void refactor(IProjectUnits units, CollectorType collector, WorkerSummary summary) throws Exception {
		this.collector = collector;
		this.summary = summary;
		
		Multimap<IRewriteCompilationUnit, RefactorType> nodeMap = getNodesMap();
		
		for (IRewriteCompilationUnit unit : nodeMap.keySet()) {		
				startRefactorNode(unit);				
				for (RefactorType node : nodeMap.get(unit)) {
					refactorNode(unit, node);
				}
				endRefactorNode(unit);			
		}

		return null;
	}

	protected void addObservableImport(IRewriteCompilationUnit unit) {
		unit.addImport("rx.Observable");
	}

	protected void addSubjectImport(IRewriteCompilationUnit unit) {
		unit.addImport("rx.subjects.Subject");
		unit.addImport("rx.subjects.ReplaySubject");
	}

	protected void addCallableImport(IRewriteCompilationUnit unit) {
		unit.addImport("java.util.concurrent.Callable");		
	}

	protected void addSchedulersImport(IRewriteCompilationUnit unit) {
		unit.addImport("rx.schedulers.Schedulers");		
	}

	protected void addAwaitImport(IRewriteCompilationUnit unit) {
		unit.addImport("scala.concurrent.Await");		
	}

	protected void addDurationImport(IRewriteCompilationUnit unit) {
		unit.addImport("scala.concurrent.duration.Duration");		
	}

	protected void addArraysImport(IRewriteCompilationUnit unit) {
		unit.addImport("java.util.Arrays");		
	}

	protected void addCollectorsImport(IRewriteCompilationUnit unit) {
		unit.addImport("java.util.stream.Collectors");		
	}

	protected void addFunc1Import(IRewriteCompilationUnit unit) {
		unit.addImport("rx.functions.Func1");		
	}

	protected void addAction1Import(IRewriteCompilationUnit unit) {
		unit.addImport("rx.functions.Action1");		
	}

	protected void addExecutionContextsImport(IRewriteCompilationUnit unit) {
		unit.addImport("akka.dispatch.ExecutionContexts");		
	}

	protected void addFuturesImport(IRewriteCompilationUnit unit) {
		unit.addImport("akka.dispatch.Futures");		
	}


}
