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
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class MethodDeclarationWorker extends AbstractRefactorWorker<RxCollector>
{
	public MethodDeclarationWorker( RxCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<MethodDeclaration>> methodDeclMap = collector.getMethodDeclarationsMap();
		int total = methodDeclMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<MethodDeclaration>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<MethodDeclaration>> methodDeclEntry : methodDeclMap.entrySet() )
		{
			ICompilationUnit icu = methodDeclEntry.getKey();

			for ( MethodDeclaration methodDeclaration : methodDeclEntry.getValue() )
			{
				AST ast = methodDeclaration.getAST();
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

				RxLogger.info( this, "METHOD=refactor - Changing return type: " + icu.getElementName() );
				Type type = methodDeclaration.getReturnType2();
				if ( type instanceof ParameterizedType )
				{
					type = ( (ParameterizedType) type ).getType();
				}
				if ( ASTUtil.isClassOf( type, SwingWorkerInfo.getBinaryName() ) )
				{
					singleUnitWriter.replaceType( (SimpleType) type, "SWSubscriber" );
				}

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}
}
