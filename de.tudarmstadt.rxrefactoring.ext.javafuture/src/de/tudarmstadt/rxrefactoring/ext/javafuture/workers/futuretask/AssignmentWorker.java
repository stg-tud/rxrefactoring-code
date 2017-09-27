package de.tudarmstadt.rxrefactoring.ext.javafuture.workers.futuretask;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.JavaFutureASTUtils;
import de.tudarmstadt.rxrefactoring.ext.javafuture.utils.visitors.helper.MethodInvocationVisitor;
import de.tudarmstadt.rxrefactoring.ext.javafuture.workers.AbstractFutureTaskWorker;

public class AssignmentWorker extends AbstractFutureTaskWorker<Assignment> {
	public AssignmentWorker() {
		super("Assignment");
	}

	@Override
	protected Map<RewriteCompilationUnit, List<Assignment>> getNodesMap() {
		return collector.getAssigmentsMap("futuretask");
	}

	@Override
	protected void refactorNode(RewriteCompilationUnit unit, Assignment assignment) {
		Expression rightHand = assignment.getRightHandSide();
		
		refactorRightHand(unit, rightHand);
		
	}
	
	/**
	 * Replaces x = someMethod with x = Observable.from(someMethod)
	 * But only if we didn't refactor the method ourselves before.
	 * @param unit
	 * @param fragment
	 */
	private void refactorRightHand(RewriteCompilationUnit unit, Expression rightHand) {
		
		// look for a methodinvocation here
		MethodInvocationVisitor visitor = new MethodInvocationVisitor(collector, "future");

		rightHand.accept(visitor);

		if(visitor.isExternalMethod().orElse(false)) {
			// move the initializer expression inside an "Observable.from(rightHand)"
			
			JavaFutureASTUtils.moveInsideMethodInvocation(unit, "SimpleFutureTaskObservable", "create", rightHand);
			summary.addCorrect("futureTaskCreation");
		}
	}
}

