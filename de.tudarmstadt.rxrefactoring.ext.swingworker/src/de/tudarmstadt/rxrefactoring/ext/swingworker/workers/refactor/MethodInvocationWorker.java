package de.tudarmstadt.rxrefactoring.ext.swingworker.workers.refactor;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import de.tudarmstadt.rxrefactoring.core.IProjectUnits;
import de.tudarmstadt.rxrefactoring.core.IRewriteCompilationUnit;
import de.tudarmstadt.rxrefactoring.core.IWorker;
import de.tudarmstadt.rxrefactoring.core.RefactorSummary.WorkerSummary;
import de.tudarmstadt.rxrefactoring.core.utils.RefactorScope;
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
public class MethodInvocationWorker implements IWorker<TypeOutput, Boolean> {

	private MethodDeclaration isSameMethod;
	
	@Override
	public Boolean refactor(@NonNull IProjectUnits units, @Nullable TypeOutput input,
			@NonNull WorkerSummary summary) throws Exception {

		RefactorInfo info = input.info;

		for (Map.Entry<IRewriteCompilationUnit, MethodInvocation> invocationEntry : input.collector
				.getMethodInvocationsMap().entries()) {
			IRewriteCompilationUnit unit = invocationEntry.getKey();
			MethodInvocation methodInvocation = invocationEntry.getValue();

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
			
			/*if(expression != null) {
				MethodDeclaration method = ASTNodes.findParent(expression, MethodDeclaration.class).get();
				if(!method.equals(isSameMethod) && isSameMethod != null) {
					ASTNode newNode = unit.copyNode(methodInvocation.getParent());
					RewriteCompilationUnit newUnit = new RewriteCompilationUnit(unit.getPrimary(), newNode);
					unit = (IRewriteCompilationUnit) newUnit;
				}
				isSameMethod = method;
			}*/

			boolean noChanges = refactorInvocation(ast, unit, methodInvocation);

			if(noChanges) {
				summary.addSkipped("methodInvocations");
				continue;
				}
			summary.addCorrect("methodInvocations");

		}
	
		
		return null;
	}

	private boolean refactorInvocation(AST ast, IRewriteCompilationUnit unit, MethodInvocation methodInvocation) {

		SimpleName methodSimpleName = methodInvocation.getName();
		String newMethodName = RefactoringUtils.getNewMethodName(methodSimpleName.toString());
		boolean noChanges = methodSimpleName.getIdentifier().equals(newMethodName);
		

		if(!noChanges) {	
			synchronized (unit) {
				unit.replace(methodSimpleName, SwingWorkerASTUtils.newSimpleName(ast, newMethodName));
			}
		}

		Expression expression = methodInvocation.getExpression();
		if (expression instanceof SimpleName) {
			SimpleName simpleName = (SimpleName) expression;
			String newName = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
			noChanges = simpleName.getIdentifier().equals(newName);
			if(!noChanges) {
				synchronized (unit) {
		//			unit.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newName)); // THA no simple name change -> simple names worker does that
				}
			}
		}
		
		return noChanges;

	}
}
