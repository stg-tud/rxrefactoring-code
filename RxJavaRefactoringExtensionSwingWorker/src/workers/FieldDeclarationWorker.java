package workers;

import java.util.List;
import java.util.Map;

import domain.SwingWorkerInfo;
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
 * Created: 12/22/2016
 */
public class FieldDeclarationWorker extends AbstractRefactorWorker<RxCollector>
{
	public FieldDeclarationWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<FieldDeclaration>> fieldDeclMap = collector.getFieldDeclMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<FieldDeclaration>> fieldDeclEntry : fieldDeclMap.entrySet() )
		{
			ICompilationUnit icu = fieldDeclEntry.getKey();

			for ( FieldDeclaration fieldDeclaration : fieldDeclEntry.getValue() )
			{
				// Get ast and writer
				AST ast = fieldDeclaration.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				updateImports( singleUnitWriter );

				RxLogger.info( this, "METHOD=refactor - Changing type: " + icu.getElementName() );
				Type type = fieldDeclaration.getType();
				if (type instanceof ParameterizedType)
				{
					type = ((ParameterizedType) type).getType();
					if ( ASTUtil.isClassOf(type, SwingWorkerInfo.getBinaryName()))
					{
						singleUnitWriter.replaceType((SimpleType) type, "SWSubscriber");
					}
				}

				RxLogger.info( this, "METHOD=refactor - Changing field name: " + icu.getElementName() );
				VariableDeclarationFragment varDecl = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
				String oldIdentifier = varDecl.getName().getIdentifier();
				singleUnitWriter.replaceSimpleName(varDecl.getName(), RefactoringUtils.cleanSwingWorkerName(oldIdentifier));

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private void updateImports( RxSingleUnitWriter singleUnitWriter )
	{
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
	}
}
