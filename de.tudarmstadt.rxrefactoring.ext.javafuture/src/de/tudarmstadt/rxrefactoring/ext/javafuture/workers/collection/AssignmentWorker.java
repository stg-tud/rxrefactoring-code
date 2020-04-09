package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.collection;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureWorker;

public class AssignmentWorker extends AbstractFutureWorker<Assignment> {
	public AssignmentWorker() {
		super("Assignment");
	}

	@Override
	protected Multimap<IRewriteCompilationUnit, Assignment> getNodesMap() {
		return collector.getAssigmentsMap("collection");
	}

	@Override
	protected void endRefactorNode(IRewriteCompilationUnit unit) {
		addObservableImport(unit);
		addFutureObservableImport(unit);

		super.endRefactorNode(unit);
	}

	@Override
	protected void refactorNode(IRewriteCompilationUnit unit, Assignment assignment) {
		Expression rightHand = assignment.getRightHandSide();

		if (collector.isPure(unit, assignment)) {
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "Observable", "from", rightHand);
		} else {
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "FutureObservable", "create", rightHand);
		}

		summary.addCorrect("futureCreation");
	}
}
