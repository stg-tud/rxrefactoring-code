package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.workers.RxCollector;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.*;

import com.google.common.collect.Multimap;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.legacy.ASTUtils;
import de.tudarmstadt.rxrefactoring.core.utils.Log;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class MethodDeclarationWorker implements IWorker<RxCollector, Void> {

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception {
		Multimap<IRewriteCompilationUnit, MethodDeclaration> methodDeclMap = input.getMethodDeclarationsMap();
		int total = methodDeclMap.values().size();
		Log.info(getClass(), "METHOD=refactor - Total number of <<MethodDeclaration>>: " + total);

		for (Map.Entry<IRewriteCompilationUnit, MethodDeclaration> methodDeclEntry : methodDeclMap.entries()) {
			IRewriteCompilationUnit icu = methodDeclEntry.getKey();
			MethodDeclaration methodDeclaration = methodDeclEntry.getValue();

			AST ast = methodDeclaration.getAST();

			Log.info(getClass(), "METHOD=refactor - Changing return type: " + icu.getElementName());
			Type type = methodDeclaration.getReturnType2();
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getType();
			}
			if (ASTUtils.isClassOf(type, SwingWorkerInfo.getBinaryName())) {
				SimpleType newType = SwingWorkerASTUtils.newSimpleType(ast, "SWSubscriber");
				synchronized (icu) {
					icu.replace(type, newType);
				}
			}

			// Add changes to the multiple compilation units write object
			Log.info(getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName());

			summary.addCorrect("MethodDeclarations");
		}

		return null;
	}

}
