package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import visitors.RxCollector;

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
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<MethodInvocation>> invocationEntry : methodInvocationMap.entrySet() )
		{
			ICompilationUnit icu = invocationEntry.getKey();

			for ( MethodInvocation methodInvocation : invocationEntry.getValue() )
			{
				Expression expression = methodInvocation.getExpression();
				if ( expression instanceof ClassInstanceCreation )
				{
					// another worker will handle this case
					continue;
				}

				// Get ast and writer
				AST ast = methodInvocation.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

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
			RxSingleUnitWriter singleUnitWriter,
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
