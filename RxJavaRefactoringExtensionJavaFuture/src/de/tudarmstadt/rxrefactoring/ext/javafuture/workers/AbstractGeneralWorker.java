package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.ProjectUnits;
import de.tudarmstadt.rxrefactoring.core.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.workers.IWorker;


public abstract class AbstractGeneralWorker<NodeType extends ASTNode> implements IWorker<FutureCollector, Void> {

	protected final String nodeName;
	protected FutureCollector collector;
	protected WorkerSummary summary;
	
	public AbstractGeneralWorker(String nodeName) {
		this.nodeName = nodeName;
	}
	
	protected String getNodeName() {
		return nodeName;
	}
	
	protected abstract Map<RewriteCompilationUnit, List<NodeType>> getNodesMap();
	
	protected abstract void refactorNode(RewriteCompilationUnit unit, NodeType node);
	
	protected void startRefactorNode(RewriteCompilationUnit unit) {
		
	}
	
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		
	}
	
	@Override
	public Void refactor(ProjectUnits units, FutureCollector collector, WorkerSummary summary) throws Exception {
		this.collector = collector;
		this.summary = summary;
		
		Map<RewriteCompilationUnit, List<NodeType>> nodeMap = getNodesMap();
		
		for (Map.Entry<RewriteCompilationUnit, List<NodeType>> nodeEntry : nodeMap.entrySet()) {
			
			RewriteCompilationUnit unit = nodeEntry.getKey();

			for (NodeType node : nodeEntry.getValue()) {
			
				startRefactorNode(unit);
				refactorNode(unit, node);
				endRefactorNode(unit);
				
			}
		}

		return null;
	}
	
//	@Override
//	protected WorkerStatus refactor() {
//
//		Map<RewriteCompilationUnit, List<NodeType>> nodeMap = getNodesMap();
//		int total = nodeMap.values().size();
//
//		monitor.beginTask(getClass().getSimpleName(), total);
//		RxLogger.info(this, "METHOD=refactor - Total number of <<" + nodeName + ">>: " + total);
//
//		for (Map.Entry<RewriteCompilationUnit, List<NodeType>> nodeEntry : nodeMap.entrySet())
//		{
//			RewriteCompilationUnit icu = nodeEntry.getKey();
//
//			for (NodeType node : nodeEntry.getValue())
//			{
//				// Get ast and writer
//				AST ast = node.getAST();
//				RewriteCompilationUnit rxSwingWorkerWriter = new RewriteCompilationUnit(icu, ast, getClass().getSimpleName());
//				RewriteCompilationUnit unit = RxRewriteCompilationUnitMapHolder.getRewriteCompilationUnit(icu, rxSwingWorkerWriter);
//
//				beginRefactorNode(unit);	
//				refactorNode(unit, node);
//				endRefactorNode(unit);
//
//				// Add changes to the multiple compilation units write object
//				rxMultipleUnitsWriter.addCompilationUnit(icu);
//			}
//			monitor.worked(1);
//		}
//
//		return WorkerStatus.OK;
//	}
}
