package workers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import domain.RxObservableModel;
import domain.RxObserverModel;
import domain.SWSubscriberModel;
import domain.SwingWorkerInfo;
import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.RefactoringVisitor;
import visitors.RxCollector;
import writer.RxSwingWorkerWriter;

/**
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
		int total = varDeclMap.values().size();
		monitor.beginTask( getClass().getSimpleName(), total );
		RxLogger.info( this, "METHOD=refactor - Total number of <<Assignment>>: " + total );

		for ( Map.Entry<ICompilationUnit, List<Assignment>> assignmentEntry : varDeclMap.entrySet() )
		{
			ICompilationUnit icu = assignmentEntry.getKey();

			for ( Assignment assignment : assignmentEntry.getValue() )
			{
				AST ast = assignment.getAST();
				RxSwingWorkerWriter rxSwingWorkerWriter = new RxSwingWorkerWriter(icu, ast, getClass().getSimpleName());
				RxSwingWorkerWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, rxSwingWorkerWriter );

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
			RxSwingWorkerWriter singleUnitWriter,
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
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		String icuName = icu.getElementName();
		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		SWSubscriberModel subscriberDto = createSWSubscriberDto( rxObserverName, icuName, refactoringVisitor );

		Map<String, Object> subscriberData = new HashMap<>();
		subscriberData.put( "model", subscriberDto );
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
			RxSwingWorkerWriter singleUnitWriter,
			RefactoringVisitor refactoringVisitor,
			Assignment assignment )
	{
		String icuName = icu.getElementName();
		RxObservableModel observableDto = createObservableDto( icuName, refactoringVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "model", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = assignment.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
		singleUnitWriter.addBefore( observableStatement, referenceStatement );

		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.cleanSwingWorkerName( swingWorkerName.getIdentifier() );
		RxObserverModel observerDto = createObserverDto( rxObserverName, refactoringVisitor, observableDto );
		observerDto.setVariableDecl( false );

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "model", observerDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( assignment );
	}
}
