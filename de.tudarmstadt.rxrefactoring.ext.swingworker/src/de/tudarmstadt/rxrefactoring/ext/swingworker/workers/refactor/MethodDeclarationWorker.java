package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.IWorkerV1;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
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
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, MethodDeclaration> methodDeclEntry : input.collector
				.getMethodDeclarationsMap().entries()) {
			IRewriteCompilationUnit unit = methodDeclEntry.getKey();
			MethodDeclaration methodDeclaration = methodDeclEntry.getValue();

			if (!info.shouldBeRefactored(methodDeclaration.resolveBinding().getDeclaringClass())
					&& !Types.isExactTypeOf(methodDeclaration.resolveBinding().getReturnType(), SwingWorkerInfo.getBinaryName())) {
				summary.addSkipped("methodDeclarations");
				continue;
			}

			AST ast = methodDeclaration.getAST();

			Type type = methodDeclaration.getReturnType2();
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getType();
			}
			if (Types.isTypeOf(type.resolveBinding(), SwingWorkerInfo.getBinaryName())) {
				SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
				synchronized (unit) {
					unit.replace(type, newType);
				}
			}

			summary.addCorrect("methodDeclarations");
		}

		return null;
	}

}
