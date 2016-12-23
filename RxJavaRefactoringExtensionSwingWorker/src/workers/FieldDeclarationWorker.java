package workers;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.ASTNodesCollector;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/22/2016
 */
public class FieldDeclarationWorker extends AbstractRefactorWorker<ASTNodesCollector>
{
	public FieldDeclarationWorker( ASTNodesCollector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<FieldDeclaration>> fieldDeclMap = collector.getFieldDeclarationsMap();
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

				// Create new field declaration statement
				RxLogger.info( this, "METHOD=refactor - Creating new field declaration: " + icu.getElementName() );
				String typeChanged = fieldDeclaration.toString().replace( "SwingWorker", "SWSubscriber" );
				String finalStatementString = RefactoringUtils.cleanSwingWorkerName( typeChanged );
				FieldDeclaration newDecl = ASTNodeFactory.createFieldDeclarationFromText( ast, finalStatementString );

				// Replace declarations
				RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
				singleUnitWriter.addFieldDeclarationBefore( newDecl, fieldDeclaration );
				singleUnitWriter.removeElement( fieldDeclaration );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit(icu);
			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}
}
