package de.tudarmstadt.rxrefactoring.ext.akkafuture.workers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;


public abstract class AbstractGeneralWorker<CollectorType, RefactorType> implements IWorker<CollectorType, Void> {

	protected final String nodeName;
	protected CollectorType collector;
	protected WorkerSummary summary;
	
	public AbstractGeneralWorker(String nodeName) {
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


}
