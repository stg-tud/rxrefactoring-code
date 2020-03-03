package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.ASTNodes;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
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
			@NonNull WorkerSummary summary, RefactorScope scope) throws Exception {

		RefactorInfo info = input.info;

		Map<MethodDeclaration, Map.Entry<IRewriteCompilationUnit, SimpleName>> methods = new HashMap<MethodDeclaration, Map.Entry<IRewriteCompilationUnit, SimpleName>>();
		Map<MethodDeclaration, String> list = new HashMap<MethodDeclaration, String>();
		int counter = 1;
		for (Map.Entry<IRewriteCompilationUnit, SimpleName> simpleNameEntry : input.collector.getSimpleNamesMap()
				.entries()) {
			IRewriteCompilationUnit icu = simpleNameEntry.getKey();
			SimpleName simpleName = simpleNameEntry.getValue();

			ITypeBinding type = simpleName.resolveTypeBinding();

			if (!info.shouldBeRefactored(type) && !Types.isExactTypeOf(type.getErasure(), "javax.swing.SwingWorker")) {
				summary.addSkipped("simpleNames");
				continue;
			}

			Optional<MethodDeclaration> nameInMethod = ASTNodes.findParent(simpleName, MethodDeclaration.class);
			if (nameInMethod.isPresent()) {
				if (!list.keySet().contains(nameInMethod.get()) && nameInMethod.get() != null) {
					list.put(nameInMethod.get(), Integer.toString(counter));
					counter++;

				}
				icu.setWorker(icu.getWorker() + list.get(nameInMethod.get()));
			}

			/*
			 * if(methods.keySet().contains(nameInMethod)) {
			 * Map.Entry<IRewriteCompilationUnit, SimpleName> entry =
			 * methods.get(nameInMethod); IRewriteCompilationUnit unit = entry.getKey();
			 * if(simpleName.getIdentifier().equals(entry.getValue().getIdentifier()))
			 * icu.setWorker(icu.getWorker() + nameInMethod.getName().getIdentifier());
			 * }else { methods.put(nameInMethod, simpleNameEntry);
			 * 
			 * }
			 */

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
			if (!simpleName.equals(newName)) {
				synchronized (icu) {
					icu.replace(simpleName, newName);
				}
			}

			summary.addCorrect("simpleNames");
		}

		return null;
	}

	@Override
	public @Nullable Void refactor(IProjectUnits units, TypeOutput input, WorkerSummary summary) throws Exception {
		// TODO Auto-generated method stub
		// only needed if RefactorScope is not implemented
		return null;
	}

}
