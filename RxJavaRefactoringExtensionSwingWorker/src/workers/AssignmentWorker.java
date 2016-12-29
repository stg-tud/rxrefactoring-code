package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.RxObservableDto;
import domain.RxObserverDto;
import domain.SWSubscriberDto;
import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
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
				AST ast = assignment.getAST();
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				Expression rightHandSide = assignment.getRightHandSide();
				if ( rightHandSide instanceof ClassInstanceCreation )
				{
					ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightHandSide;

					if ( ASTUtil.isClassOf( classInstanceCreation, SwingWorkerInfo.getBinaryName() ) )
					{
						RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
						RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
						assignment.accept( refactoringVisitor );

						RxLogger.info( this, "METHOD=refactor - Refactoring assignment in: " + icu.getElementName() );
						refactorAssignment( icu, singleUnitWriter, refactoringVisitor, assignment );
					}
				}
				else if ( rightHandSide instanceof SimpleName )
				{
					RxLogger.info( this, "METHOD=refactor - Refactoring right variable name: " + icu.getElementName() );
					SimpleName simpleName = (SimpleName) rightHandSide;
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
					singleUnitWriter.replaceSimpleName( simpleName, newIdentifier );
				}

				Expression leftHandSide = assignment.getLeftHandSide();
				if ( leftHandSide instanceof SimpleName )
				{
					RxLogger.info( this, "METHOD=refactor - Refactoring left variable name: " + icu.getElementName() );
					SimpleName simpleName = (SimpleName) leftHandSide;
					String newIdentifier = RefactoringUtils.cleanSwingWorkerName( simpleName.getIdentifier() );
					singleUnitWriter.replaceSimpleName( simpleName, newIdentifier );
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

		if ( refactoringVisitor.hasAdditionalFieldsOrMethods() )
		{
			refactorStatefulSwingWorker( icu, singleUnitWriter, refactoringVisitor, assignment );
		}
		else
		{
			refactorStatelessSwingWorker( icu, singleUnitWriter, refactoringVisitor, assignment );
		}
	}

	private void refactorStatefulSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		String icuName = icu.getElementName();
		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberDto subscriberDto = createSWSubscriberDto( rxObserverName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "dto", subscriberDto );
		String subscriberTemplate = "subscriber.ftl";

		String subscriberString = TemplateUtils.processTemplate( subscriberTemplate, subscriberData );
		AST ast = assignment.getAST();
		TypeDeclaration typeDeclaration = ASTNodeFactory.createTypeDeclarationFromText( ast, subscriberString );

		Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
		singleUnitWriter.addBefore( typeDeclaration, referenceStatement );

		String newAssignmentString = subscriberDto.getSubscriberName() + " = new " + subscriberDto.getClassName() + "()";
		Statement newAssignment = ASTNodeFactory.createSingleStatementFromText( ast, newAssignmentString );
		singleUnitWriter.addBefore( newAssignment, referenceStatement );

		singleUnitWriter.removeStatement( assignment );

	}

	private void refactorStatelessSwingWorker(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = assignment.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
		singleUnitWriter.addBefore( observableStatement, referenceStatement );

		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		RxObserverDto observerDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		observerDto.setVariableDecl( false );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", observerDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( assignment );
	}
}