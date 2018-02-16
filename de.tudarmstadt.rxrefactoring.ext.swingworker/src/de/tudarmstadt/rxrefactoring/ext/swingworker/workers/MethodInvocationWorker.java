package de.tudarmstadt.rxrefactoring.ext.swingworker.workers;

import java.util.Map;

import de.tudarmstadt.rxrefactoring.ext.swingworker.domain.SwingWorkerInfo;

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
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.RefactoringUtils;
import de.tudarmstadt.rxrefactoring.ext.swingworker.utils.SwingWorkerASTUtils;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016<br>
 * Adapted to new core by Camila Gonzalez on 24/01/2018
 */
public class MethodInvocationWorker implements IWorker<RxCollector, Void>
{

	@Override
	public @Nullable Void refactor(@NonNull IProjectUnits units, @Nullable RxCollector input,
			@NonNull WorkerSummary summary) throws Exception
	{
		Multimap<IRewriteCompilationUnit, MethodInvocation> methodInvocationMap = input.getMethodInvocationsMap();
		int total = methodInvocationMap.values().size();
		Log.info( getClass(), "METHOD=refactor - Total number of <<MethodInvocation>>: " + total );

		for (Map.Entry<IRewriteCompilationUnit, MethodInvocation> invocationEntry : methodInvocationMap.entries())
		{
		IRewriteCompilationUnit icu = invocationEntry.getKey();
		MethodInvocation methodInvocation = invocationEntry.getValue();

		Expression expression = methodInvocation.getExpression();
		if ( expression instanceof ClassInstanceCreation )
		{
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
			if (!ASTUtils.isSubclassOf(classInstanceCreation, SwingWorkerInfo.getBinaryName(), false))
			{
				// Another worker will handle this case
				continue;
			}
		}

		AST ast = methodInvocation.getAST();

		// Refactor invocation
		Log.info( getClass(), "METHOD=refactor - refactoring method invocation: "
				+ methodInvocation.getName() + "in " + icu.getElementName() );
		refactorInvocation( ast, icu, methodInvocation );

		// Add changes to the multiple compilation units write object
		Log.info( getClass(), "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			
		summary.addCorrect("MethodInvocations");

		}
		return null;
	}

	private void refactorInvocation(
			AST ast,
			IRewriteCompilationUnit icu,
			MethodInvocation methodInvocation )
	{

		SimpleName methodSimpleName = methodInvocation.getName();
		String newMethodName = RefactoringUtils.getNewMethodName( methodSimpleName.toString() );
		
		SimpleName newMethod = SwingWorkerASTUtils.newSimpleName(ast, newMethodName);
		
		synchronized(icu) 
		{
			icu.replace(methodSimpleName, newMethod);
		}

		Expression expression = methodInvocation.getExpression();
		if ( expression instanceof SimpleName )
		{
			SimpleName simpleName = (SimpleName) expression;
			String newName = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
			synchronized(icu) 
			{
				icu.replace(simpleName, SwingWorkerASTUtils.newSimpleName(ast, newName));
			}
		}
	}

}
