package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
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
public class SimpleNameWorker extends AbstractRefactorWorker<RxCollector>
{
	public SimpleNameWorker( RxCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<SimpleName>> simpleNamesMap = collector.getSimpleNamesMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<SimpleName>> simpleNameEntry : simpleNamesMap.entrySet() )
		{
			ICompilationUnit icu = simpleNameEntry.getKey();

			for ( SimpleName simpleName : simpleNameEntry.getValue() )
			{
				// Get ast and writer
				AST ast = simpleName.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				MethodInvocation methodInvocation = ASTUtil.findParent( simpleName, MethodInvocation.class );
				if ( methodInvocation != null )
				{
					ITypeBinding declaringClass = methodInvocation.resolveMethodBinding().getDeclaringClass();
					boolean executor = ASTUtil.isTypeOf( declaringClass, "java.util.concurrent.ExecutorService" );
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName(simpleName.getIdentifier());
					if ( executor && "submit".equals( methodInvocation.getName().toString() ) )
					{
						String executeObservableString = newIdentifier + ".executeObservable()";
						Statement executeObservableStatement = ASTNodeFactory.createSingleStatementFromText(ast, executeObservableString);
						Statement referenceStatement = ASTUtil.findParent(simpleName, Statement.class);
						singleUnitWriter.addBefore(executeObservableStatement, referenceStatement);
						singleUnitWriter.removeStatement(simpleName);
					}
					else
					{
						RxLogger.info( this, "METHOD=refactor - Refactoring simple name in: " + icu.getElementName() );
						singleUnitWriter.replaceSimpleName( simpleName, newIdentifier);
					}

					// Add changes to the multiple compilation units write object
					RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
					rxMultipleUnitsWriter.addCompilationUnit( icu );
				}
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}
}