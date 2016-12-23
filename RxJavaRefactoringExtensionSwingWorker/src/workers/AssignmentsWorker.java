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
import visitors.Collector;
import visitors.RefactoringVisitor;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class AssignmentsWorker extends AbstractRefactorWorker<Collector>
{
	public AssignmentsWorker(Collector collector )
	{
		super( collector );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<Assignment>> varDeclMap = collector.getAssigmentsMap();
		int numUnits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numUnits );
		RxLogger.info( this, "METHOD=refactor - Total number of compilation units: " + numUnits );

		for ( Map.Entry<ICompilationUnit, List<Assignment>> assigmentEntry : varDeclMap.entrySet() )
		{
			ICompilationUnit icu = assigmentEntry.getKey();

			for ( Assignment assignment : assigmentEntry.getValue() )
			{
				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				AST ast = assignment.getAST();
				RefactoringVisitor refactoringVisitor = new RefactoringVisitor();
				assignment.accept(refactoringVisitor);

				// Register changes in the single unit writer
				RxLogger.info( this, "METHOD=refactor - Copying changes to the single unit writer: " + icu.getElementName() );
				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );
				refactorAssignment( icu, singleUnitWriter, refactoringVisitor, assignment );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Add changes to multiple units writer: " + icu.getElementName() );
				rxMultipleUnitsWriter.addCompilationUnit(icu);
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
		removeSuperInvocations(refactoringVisitor);
		updateImports( singleUnitWriter );

		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, refactoringVisitor);

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

		Map<String, Object> observerData = new HashMap<>();
		observerData.put( "dto", subscriberDto );
		String observerTemplate = "observer.ftl";

		String observerString = TemplateUtils.processTemplate( observerTemplate, observerData );
		Statement observerStatement = ASTNodeFactory.createSingleStatementFromText( ast, observerString );
		singleUnitWriter.addStatementBefore( observerStatement, referenceStatement );

		singleUnitWriter.removeStatement( assignment );
	}

	private void updateImports( RxSingleUnitWriter singleUnitWriter )
	{
		singleUnitWriter.addImport( "rx.Observable" );
		singleUnitWriter.addImport( "rx.Emitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWEmitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWDto" );
	}

	private RxObservableDto createObservableDto( String icuName, RefactoringVisitor refactoringVisitor)
	{
		RxObservableDto observableDto = new RxObservableDto( icuName );
		observableDto.setResultType( refactoringVisitor.getResultType().toString() );
		observableDto.setProcessType( refactoringVisitor.getProcessType().toString() );
		observableDto.setDoInBackgroundBlock( refactoringVisitor.getDoInBackgroundBlock().toString() );
		return observableDto;
	}

	private RxSubscriberDto createObserverDto(String observerName, RefactoringVisitor refactoringVisitor, RxObservableDto observableDto )
	{
		RxSubscriberDto subscriberDto = new RxSubscriberDto();
		subscriberDto.setObserverName( observerName );
		subscriberDto.setResultType( refactoringVisitor.getResultType().toString() );
		subscriberDto.setProcessType( refactoringVisitor.getProcessType().toString() );
		subscriberDto.setObservableName( observableDto.getVarName() );
		subscriberDto.setChunksName( refactoringVisitor.getProcessVariableName() );
		Block processBlock = refactoringVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			subscriberDto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = refactoringVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			subscriberDto.setDoneBlock( doneBlock.toString() );
		}
		return subscriberDto;
	}

	private void removeSuperInvocations( RefactoringVisitor refactoringVisitor)
	{
		for ( SuperMethodInvocation methodInvocation : refactoringVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
