package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;


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
	
	protected abstract Multimap<RewriteCompilationUnit, RefactorType> getNodesMap();
	
	protected abstract void refactorNode(RewriteCompilationUnit unit, RefactorType node);
	
	protected void startRefactorNode(RewriteCompilationUnit unit) {
		
	}
	
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		
	}
	
	@Override
	public Void refactor(ProjectUnits units, CollectorType collector, WorkerSummary summary) throws Exception {
		this.collector = collector;
		this.summary = summary;
		
		Multimap<RewriteCompilationUnit, RefactorType> nodeMap = getNodesMap();
		
		for (RewriteCompilationUnit unit : nodeMap.keySet()) {		
				startRefactorNode(unit);				
				for (RefactorType node : nodeMap.get(unit)) {
					refactorNode(unit, node);
				}
				endRefactorNode(unit);			
		}

		return null;
	}

	protected void addObservableImport(RewriteCompilationUnit unit) {
		unit.addImport("rx.Observable");
	}

	protected void addSubjectImport(RewriteCompilationUnit unit) {
		unit.addImport("rx.subjects.Subject");
		unit.addImport("rx.subjects.ReplaySubject");
	}

	protected void addCallableImport(RewriteCompilationUnit unit) {
		unit.addImport("java.util.concurrent.Callable");		
	}

	protected void addSchedulersImport(RewriteCompilationUnit unit) {
		unit.addImport("rx.schedulers.Schedulers");		
	}

	protected void addAwaitImport(RewriteCompilationUnit unit) {
		unit.addImport("scala.concurrent.Await");		
	}

	protected void addDurationImport(RewriteCompilationUnit unit) {
		unit.addImport("scala.concurrent.duration.Duration");		
	}

	protected void addArraysImport(RewriteCompilationUnit unit) {
		unit.addImport("java.util.Arrays");		
	}

	protected void addCollectorsImport(RewriteCompilationUnit unit) {
		unit.addImport("java.util.stream.Collectors");		
	}

	protected void addFunc1Import(RewriteCompilationUnit unit) {
		unit.addImport("rx.functions.Func1");		
	}

	protected void addAction1Import(RewriteCompilationUnit unit) {
		unit.addImport("rx.functions.Action1");		
	}

	protected void addExecutionContextsImport(RewriteCompilationUnit unit) {
		unit.addImport("akka.dispatch.ExecutionContexts");		
	}

	protected void addFuturesImport(RewriteCompilationUnit unit) {
		unit.addImport("akka.dispatch.Futures");		
	}


}
