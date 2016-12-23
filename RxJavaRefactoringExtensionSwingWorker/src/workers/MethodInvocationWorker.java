package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.ASTNodesCollector;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class MethodInvocationWorker extends AbstractRefactorWorker<ASTNodesCollector>
{
	public MethodInvocationWorker( ASTNodesCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<MethodInvocation>> methodInvocationMap = collector.getMethodInvocationMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<MethodInvocation>> invocationEntry : methodInvocationMap.entrySet() )
		{
			ICompilationUnit icu = invocationEntry.getKey();

			for ( MethodInvocation methodInvocation : invocationEntry.getValue() )
			{
				// Get ast and writer
				AST ast = methodInvocation.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				// refactor invocation
				RxLogger.info( this, "METHOD=refactor - refactoring method invocation: "
						+ methodInvocation.getName() + "in " + icu.getElementName() );
				refactorInvocation( ast, icu, singleUnitWriter, methodInvocation );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private void refactorInvocation( AST ast, ICompilationUnit icu, RxSingleUnitWriter singleUnitWriter, MethodInvocation methodInvocation )
	{
		Statement referenceStatement = ASTUtil.findParent( methodInvocation, Statement.class );
		String statement = referenceStatement.toString();

		String methodName = methodInvocation.getName().toString();
		String newMethodName = getNewMethodName( methodName );

		String target = methodName + "(";
		String replacement = newMethodName + "(";
		int targetIndex = statement.indexOf( target );

		if ( targetIndex >= 0 && isMethodNameStart( statement, targetIndex ) )
		{
			// Create new statement containing the method invocation
			String invocationUpdated = statement.replace( target, replacement );
			String updatedStatement = RefactoringUtils.cleanSwingWorkerName( invocationUpdated );

			Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, updatedStatement );

			singleUnitWriter.addStatementBefore( newStatement, referenceStatement );
			singleUnitWriter.removeStatement( methodInvocation );

			// Add changes to the multiple compilation units write object
			RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
			rxMultipleUnitsWriter.addCompilationUnit( icu );
		}
	}

	private String getNewMethodName( String methodName )
	{
		String newMethodName = SwingWorkerInfo.getPublicMethodsMap().get( methodName );

		if ( newMethodName == null )
		{
			newMethodName = methodName;
		}
		return newMethodName;
	}

	private boolean isMethodNameStart( String methodName, int targetIndex )
	{
		return targetIndex == 0 || methodName.charAt( targetIndex - 1 ) <= 'a' || methodName.charAt( targetIndex - 1 ) >= 'Z';
	}
}
