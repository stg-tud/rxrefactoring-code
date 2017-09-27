package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class AssignmentWorker extends AbstractFutureWorker<Assignment> {
	public AssignmentWorker() {
		super("Assignment");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<Assignment>> getNodesMap() {
		return collector.getAssigmentsMap("collection");
	}

	@Override
	protected void endRefactorNode(RewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);
		
		super.endRefactorNode(unit);
	}
	
	@Override
	protected void refactorNode(RewriteCompilationUnit unit, Assignment assignment) {
		Expression rightHand = assignment.getRightHandSide();
		
		if(collector.isPure(unit, assignment)) {
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "from", rightHand);
		} else {
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "create", rightHand);
		}
		
		summary.addCorrect("futureCreation");
	}
}

