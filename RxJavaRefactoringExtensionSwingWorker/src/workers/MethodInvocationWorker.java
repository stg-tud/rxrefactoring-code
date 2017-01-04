package workers;

import java.util.List;
import java.util.Map;

import domain.SwingWorkerInfo;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class MethodInvocationWorker extends AbstractRefactorWorker<RxCollector>
{
	public MethodInvocationWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<MethodInvocation>> methodInvocationMap = collector.getMethodInvocationsMap();
		int total = methodInvocationMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<MethodInvocation>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<MethodInvocation>> invocationEntry : methodInvocationMap.entrySet() )
		{
			ICompilationUnit icu = invocationEntry.getKey();

			for ( MethodInvocation methodInvocation : invocationEntry.getValue() )
			{
				Expression expression = methodInvocation.getExpression();
				if ( expression instanceof ClassInstanceCreation )
				{
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
					if (!ASTUtil.isSubclassOf(classInstanceCreation, SwingWorkerInfo.getBinaryName(), false))
					{
						// another worker will handle this case
						continue;
					}
				}

				// Get ast and writer
				AST ast = methodInvocation.getAST();
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

				// refactor invocation
				RxLogger.info( this, "METHOD=refactor - refactoring method invocation: "
						+ methodInvocation.getName() + "in " + icu.getElementName() );
				refactorInvocation( ast, icu, singleUnitWriter, methodInvocation );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private void refactorInvocation(
			AST ast,
			ICompilationUnit icu,
			RxSwingWorkerWriter singleUnitWriter,
			MethodInvocation methodInvocation )
	{

		SimpleName methodSimpleName = methodInvocation.getName();
		String newMethodName = RefactoringUtils.getNewMethodName( methodSimpleName.toString() );

		singleUnitWriter.replaceSimpleName( methodSimpleName, newMethodName );

		Expression expression = methodInvocation.getExpression();
		if ( expression instanceof SimpleName )
		{
			SimpleName simpleName = (SimpleName) expression;
			String newName = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
			singleUnitWriter.replaceSimpleName( simpleName, newName );
		}
	}
}
