package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
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
import de.tudarmstadt.rxrefactoring.core.utils.WorkerIdentifier;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.NamingUtils;
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

			if ((!info.shouldBeRefactored(type) && !Types.isExactTypeOf(type.getErasure(), "javax.swing.SwingWorker"))
					|| ASTNodes.findParentInStatement(simpleName, FieldDeclaration.class).isPresent()) {
				summary.addSkipped("simpleNames");
				continue;
			}

			/*
			 * Optional<MethodDeclaration> nameInMethod = ASTNodes.findParent(simpleName,
			 * MethodDeclaration.class); if (nameInMethod.isPresent()) { MethodDeclaration
			 * method = nameInMethod.get(); if (shouldBeSaved(nameInMethod, list)) {
			 * list.put(nameInMethod.get(), new ArrayList<SimpleName>(
			 * Arrays.asList(simpleName))); counter++; }
			 * 
			 * if(!list.get(nameInMethod.get()).stream().anyMatch(x ->
			 * x.getIdentifier().equals(simpleName.getIdentifier()))) { List<SimpleName>
			 * subMap = list.get(method); subMap.add(simpleName); counter++; }
			 */

			// icu.setWorker(icu.getWorker() +
			// RenamingUtils.getRightWorkerName(nameInMethod.get(), simpleName));
			// }

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
			if (!simpleName.getIdentifier().equals(newName.getIdentifier())) {
				synchronized (icu) {
					icu.replace(simpleName, newName);
				}
				
				Optional<MethodDeclaration> nameInMethod = ASTNodes.findParent(simpleName, MethodDeclaration.class);
				Optional<Assignment> assignment = ASTNodes.findParent(simpleName, Assignment.class);

				if (nameInMethod.isPresent() && !assignment.isPresent()
						&& input.collector.scope.equals(RefactorScope.SEPARATE_OCCURENCES)) {
						String identifier = icu.getWorkerIdentifier().getName() + NamingUtils.getRightWorkerName(nameInMethod.get(), simpleName);
						icu.setWorkerIdentifier(new WorkerIdentifier(identifier));
				}
			}

			summary.addCorrect("simpleNames");
		}

		return null;
	}

	/*
	 * private String getRightWorkerName(MethodDeclaration m, SimpleName simpleName,
	 * Map<MethodDeclaration, List<SimpleName>> list) { int counter = 1;
	 * 
	 * for(Entry<MethodDeclaration, List<SimpleName>> entry : list.entrySet()) {
	 * for(SimpleName name: entry.getValue()) {
	 * if(name.getIdentifier().equals(simpleName.getIdentifier()) &&
	 * entry.getKey().equals(m)) return Integer.toString(counter); counter++; }
	 * counter++; }
	 * 
	 * 
	 * return "";
	 * 
	 * }
	 */

}
