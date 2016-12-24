package workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.SwingWorkerInfo;
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
public class SingleVariableDeclWorker extends AbstractRefactorWorker<RxCollector>
{
	public SingleVariableDeclWorker( RxCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<SingleVariableDeclaration>> singleVarDeclMap = collector.getSingleVarDeclMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<SingleVariableDeclaration>> singVarDeclEntry : singleVarDeclMap.entrySet() )
		{
			ICompilationUnit icu = singVarDeclEntry.getKey();

			for ( SingleVariableDeclaration singleVarDecl : singVarDeclEntry.getValue() )
			{
				// Get ast and writer
				AST ast = singleVarDecl.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				SimpleTypeVisitor visitor = new SimpleTypeVisitor();
				singleVarDecl.accept( visitor );

				for ( SimpleName simpleName : visitor.simpleNames )
				{
					if ( "SwingWorker".equals( simpleName.toString() ) )
					{
						singleUnitWriter.replaceSimpleName( simpleName, "SWSubscriber" );
					}
					else
					{
						singleUnitWriter.replaceSimpleName( simpleName, RefactoringUtils.cleanSwingWorkerName( simpleName.toString() ) );
					}
				}

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private class SimpleTypeVisitor extends ASTVisitor
	{
		private List<SimpleName> simpleNames = new ArrayList<>();

		@Override
		public boolean visit( SimpleName node )
		{
			ITypeBinding type = node.resolveTypeBinding();
			if ( ASTUtil.isTypeOf( type, SwingWorkerInfo.getBinaryName() ) )
			{
				simpleNames.add( node );
			}
			return true;
		}
	}
}
