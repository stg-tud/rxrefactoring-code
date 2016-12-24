package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxSubscriberDto;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class AssignmentWorker extends GeneralWorker
{
	public AssignmentWorker( RxCollector rxCollector )
	{
		super( rxCollector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<Assignment>> varDeclMap = collector.getAssigmentsMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<Assignment>> assignmentEntry : varDeclMap.entrySet() )
		{
			ICompilationUnit icu = assignmentEntry.getKey();

			for ( Assignment assignment : assignmentEntry.getValue() )
			{
				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				AST ast = assignment.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				Expression rightHandSide = assignment.getRightHandSide();
				if ( rightHandSide instanceof ClassInstanceCreation )
				{
					RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
					assignment.accept( refactoringVisitor );

					RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
					refactorAssignment( icu, singleUnitWriter, refactoringVisitor, assignment );
				}
				else
				{
					Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
					String cleanedStatement = RefactoringUtils.cleanSwingWorkerName( referenceStatement.toString() );
					Statement newStatement = ASTNodeFactory.createSingleStatementFromText( ast, cleanedStatement );

					RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
					singleUnitWriter.addStatementBefore( newStatement, referenceStatement );
					singleUnitWriter.removeStatement( assignment );
				}

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit( icu );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private void refactorAssignment(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		removeSuperInvocations( refactoringVisitor );
		updateImports( singleUnitWriter );

		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = assignment.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
		singleUnitWriter.addStatementBefore( observableStatement, referenceStatement );

		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.toString() );
		RxSubscriberDto subscriberDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		subscriberDto.setVariableDecl( false );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addStatementBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( assignment );
	}
}
