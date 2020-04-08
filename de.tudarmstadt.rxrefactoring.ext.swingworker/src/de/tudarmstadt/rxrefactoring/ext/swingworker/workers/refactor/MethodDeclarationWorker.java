package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class MethodDeclarationWorker implements IWorker<TypeOutput, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, MethodDeclaration> methodDeclEntry : input.collector
				.getMethodDeclarationsMap().entries()) {
			IRewriteCompilationUnit unit = methodDeclEntry.getKey();
			MethodDeclaration methodDeclaration = methodDeclEntry.getValue();
			boolean isReturnTypeToRefactor = shouldNotBeSkippedBecauseOfReturnType(methodDeclaration);

			if (!info.shouldBeRefactored(methodDeclaration.resolveBinding().getDeclaringClass())
					&& !isReturnTypeToRefactor) {
				summary.addSkipped("methodDeclarations");
				continue;
			}
			
			if (isReturnTypeToRefactor)
				refactorReturnType(methodDeclaration, unit);

			summary.addCorrect("methodDeclarations");
		}

		return null;
	}
	

	private boolean shouldNotBeSkippedBecauseOfReturnType(MethodDeclaration decl) {
		if (Types.isTypeOf(decl.resolveBinding().getReturnType(), SwingWorkerInfo.getBinaryName()))
			return true;

		return false;

	}


	private void refactorReturnType(MethodDeclaration methodDeclaration, IRewriteCompilationUnit unit) {

		AST ast = methodDeclaration.getAST();

		Type type = methodDeclaration.getReturnType2();
		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getType();
		}
		ITypeBinding binding = type.resolveBinding();
		if (Types.isExactTypeOf(binding, SwingWorkerInfo.getBinaryName())) {
			SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
			synchronized (unit) {
				unit.replace(type, newType);
			}
		}

	}
}
