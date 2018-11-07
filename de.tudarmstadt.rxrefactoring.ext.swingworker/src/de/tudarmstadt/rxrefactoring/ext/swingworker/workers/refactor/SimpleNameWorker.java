package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.Types;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactorInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.types.TypeOutput;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class SimpleNameWorker implements IWorker<TypeOutput, Void> {
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, SimpleName> simpleNameEntry : input.collector.getSimpleNamesMap()
				.entries()) {
			IRewriteCompilationUnit icu = simpleNameEntry.getKey();
			SimpleName simpleName = simpleNameEntry.getValue();

			ITypeBinding type = simpleName.resolveTypeBinding();

			if (!info.shouldBeRefactored(type) && !Types.isExactTypeOf(type.getErasure(), "javax.swing.SwingWorker")) {
				summary.addSkipped("simpleNames");
				continue;
			}

			AST ast = simpleName.getAST();

			String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
			// Optional<MethodInvocation> methodInvocation = ASTNodes.findParent(simpleName,
			// MethodInvocation.class);
			// if (methodInvocation.isPresent()) {
			// ITypeBinding declaringClass =
			// methodInvocation.get().resolveMethodBinding().getDeclaringClass();
			// boolean executor = ASTUtils.isTypeOf(declaringClass,
			// "java.util.concurrent.ExecutorService");
			//
			// if (executor && "submit".equals(methodInvocation.get().getName().toString()))
			// {
			// String executeObservableString = newIdentifier + ".executeObservable()";
			// Statement executeObservableStatement =
			// TemplateVisitor.createSingleStatementFromText(ast,
			// executeObservableString);
			// Statement referenceStatement = ASTNodes.findParent(simpleName,
			// Statement.class).get();
			// Statements.addStatementBefore(icu, executeObservableStatement,
			// referenceStatement);
			// SwingWorkerASTUtils.removeStatement(icu, simpleName);
			// }
			// }
			SimpleName newName = SwingWorkerASTUtils.newSimpleName(ast, newIdentifier);
			synchronized (icu) {
				icu.replace(simpleName, newName);
			}

			summary.addCorrect("simpleNames");
		}

		return null;
	}

}
