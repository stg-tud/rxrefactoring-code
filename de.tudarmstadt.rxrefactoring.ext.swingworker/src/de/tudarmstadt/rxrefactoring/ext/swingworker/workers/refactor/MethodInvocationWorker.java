package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import com.google.common.collect.Sets;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.internal.execution.RewriteCompilationUnitFactory;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class MethodInvocationWorker implements IWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, MethodInvocation> invocationEntry : input.collector
				.getMethodInvocationsMap().entries()) {
			// RewriteCompilationUnitFactory factory = new RewriteCompilationUnitFactory();
			IRewriteCompilationUnit unit = invocationEntry.getKey();
			MethodInvocation methodInvocation = invocationEntry.getValue();
			// ICompilationUnit testUnit = unit.getPrimary();
			// RewriteCompilationUnit unitOfSmallChange = factory.from(testUnit);
			// unitOfSmallChange.copyNode(unit.getRoot());

			Expression expression = methodInvocation.getExpression();

			boolean receiverIsRefactored = expression == null
					|| info.shouldBeRefactored(expression.resolveTypeBinding())
					|| Types.isExactTypeOf(expression.resolveTypeBinding().getErasure(), "javax.swing.SwingWorker");

			boolean methodDeclaredBySwingWorker = Types
					.isTypeOf(methodInvocation.resolveMethodBinding().getDeclaringClass(), "javax.swing.SwingWorker");

			if (!receiverIsRefactored) {
				summary.addSkipped("methodInvocations");
				continue;
			}

			if (expression instanceof ClassInstanceCreation) {
				ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
				if (!Types.isTypeOf(classInstanceCreation.resolveTypeBinding(), SwingWorkerInfo.getBinaryName())) {
					// Another worker will handle this case
					continue;
				}
			}

			AST ast = methodInvocation.getAST();

			refactorInvocation(ast, unit, methodInvocation);

			summary.addCorrect("methodInvocations");

		}
		return null;
	}

	private void refactorInvocation(AST ast, IRewriteCompilationUnit unit, MethodInvocation methodInvocation) {

		SimpleName methodSimpleName = methodInvocation.getName();
		String newMethodName = RefactoringUtils.getNewMethodName(methodSimpleName.toString());

		SimpleName newMethod = SwingWorkerASTUtils.newSimpleName(ast, newMethodName);

		synchronized (unit) {
			unit.replace(methodSimpleName, newMethod);
		}

		Expression expression = methodInvocation.getExpression();
		if (expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) expression;
			String newName = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
			synchronized (unit) {
				unit.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newName));
			}
		}

	}

}
