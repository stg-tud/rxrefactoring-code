package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.Log;
import de.tudarmstadt.rxrefactoring.core.utils.Statements;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.visitors.TemplateVisitor;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class SimpleNameWorker implements IWorker<RxCollector, Void> {
	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception {
		Multimap<IRewriteCompilationUnit, SimpleName> simpleNamesMap = input.getSimpleNamesMap();
		int total = simpleNamesMap.values().size();
		Log.info(getClass(), "METHOD=refactor - Total number of <<SimpleName>>: " + total);

		for (Map.Entry<IRewriteCompilationUnit, SimpleName> simpleNameEntry : simpleNamesMap.entries()) {
			IRewriteCompilationUnit icu = simpleNameEntry.getKey();
			SimpleName simpleName = simpleNameEntry.getValue();

			AST ast = simpleName.getAST();

			String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
			Optional<MethodInvocation> methodInvocation = ASTNodes.findParent(simpleName, MethodInvocation.class);
			if (methodInvocation.isPresent()) {
				ITypeBinding declaringClass = methodInvocation.get().resolveMethodBinding().getDeclaringClass();
				boolean executor = ASTUtils.isTypeOf(declaringClass, "java.util.concurrent.ExecutorService");

				if (executor && "submit".equals(methodInvocation.get().getName().toString())) {
					String executeObservableString = newIdentifier + ".executeObservable()";
					Statement executeObservableStatement = TemplateVisitor.createSingleStatementFromText(ast,
							executeObservableString);
					Statement referenceStatement = ASTNodes.findParent(simpleName, Statement.class).get();
					Statements.addStatementBefore(icu, executeObservableStatement, referenceStatement);
					SwingWorkerASTUtils.removeStatement(icu, simpleName);
				}
			}
			Log.info(getClass(), "METHOD=refactor - Refactoring simple name in: " + icu.getElementName());
			SimpleName newName = SwingWorkerASTUtils.newSimpleName(ast, newIdentifier);
			synchronized (icu) {
				icu.replace(simpleName, newName);
			}

			// Add changes to the multiple compilation units write object
			Log.info(getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName());

			summary.addCorrect("SimpleNames");
		}

		return null;
	}

}
