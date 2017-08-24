package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;

public abstract class AbstractAkkaWorker<CollectorType, RefactorType> extends AbstractGeneralWorker<CollectorType, RefactorType> {

	public AbstractAkkaWorker(String nodeName) {
		super(nodeName);
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
		unit.addImport("akka.dispatch.Await");		
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
