package de.tudarmstadt.rxrefactoring.ext.javafuture.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.ASTNode;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;

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

	protected abstract Map<IRewriteCompilationUnit, List<NodeType>> getNodesMap();

	protected abstract void refactorNode(IRewriteCompilationUnit unit, NodeType node);

	protected void startRefactorNode(IRewriteCompilationUnit unit) {

	}

	protected void endRefactorNode(IRewriteCompilationUnit unit) {

	}

	@Override
	public Void refactor(@NonNull IProjectUnits units, @Nullable FutureCollector collector,
			@NonNull WorkerSummary summary) throws Exception {
		this.collector = collector;
		this.summary = summary;

		Map<IRewriteCompilationUnit, List<NodeType>> nodeMap = getNodesMap();

		for (Map.Entry<IRewriteCompilationUnit, List<NodeType>> nodeEntry : nodeMap.entrySet()) {

			IRewriteCompilationUnit unit = nodeEntry.getKey();

			for (NodeType node : nodeEntry.getValue()) {

				startRefactorNode(unit);
				refactorNode(unit, node);
				endRefactorNode(unit);

			}
		}

		return null;
	}
	
	@Override
	public Void refactor(@NonNull IProjectUnits units, @Nullable FutureCollector collector,
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {
		
		return null;
	}

	// @Override
	// protected WorkerStatus refactor() {
	//
	// Map<IRewriteCompilationUnit, List<NodeType>> nodeMap = getNodesMap();
	// int total = nodeMap.values().size();
	//
	// monitor.beginTask(getClass().getSimpleName(), total);
	// RxLogger.info(this, "METHOD=refactor - Total number of <<" + nodeName + ">>:
	// " + total);
	//
	// for (Map.Entry<IRewriteCompilationUnit, List<NodeType>> nodeEntry :
	// nodeMap.entrySet())
	// {
	// IRewriteCompilationUnit icu = nodeEntry.getKey();
	//
	// for (NodeType node : nodeEntry.getValue())
	// {
	// // Get ast and writer
	// AST ast = node.getAST();
	// IRewriteCompilationUnit rxSwingWorkerWriter = new
	// IRewriteCompilationUnit(icu, ast, getClass().getSimpleName());
	// IRewriteCompilationUnit unit =
	// RxRewriteCompilationUnitMapHolder.getRewriteCompilationUnit(icu,
	// rxSwingWorkerWriter);
	//
	// beginRefactorNode(unit);
	// refactorNode(unit, node);
	// endRefactorNode(unit);
	//
	// // Add changes to the multiple compilation units write object
	// rxMultipleUnitsWriter.addCompilationUnit(icu);
	// }
	// monitor.worked(1);
	// }
	//
	// return WorkerStatus.OK;
	// }
}
