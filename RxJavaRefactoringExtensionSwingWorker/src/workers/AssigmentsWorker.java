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
import rxjavarefactoring.framework.utils.StringUtils;
import rxjavarefactoring.framework.writers.RxSingleUnitWriter;
import rxjavarefactoring.framework.writers.RxSingleUnitWriterMapHolder;
import rxjavarefactoring.processor.ASTNodesCollector;
import rxjavarefactoring.processor.WorkerStatus;
import utils.RefactoringUtils;
import utils.TemplateUtils;
import visitors.SwingWorkerVisitor;

/**
 * Description: <br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 12/21/2016
 */
public class AssigmentsWorker extends AbstractRefactorWorker<ASTNodesCollector>
{
	public AssigmentsWorker( ASTNodesCollector collector )
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
				SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
				assignment.accept( swingWorkerVisitor );

				RxSingleUnitWriter singleUnitWriter = RxSingleUnitWriterMapHolder.getSingleUnitWriter( icu, ast, getClass().getSimpleName() );

				// Register changes in the single unit writer
				refactorAssignment( icu, singleUnitWriter, swingWorkerVisitor, assignment );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleUnitsWriter.addChange( icu, singleUnitWriter );
			}
			monitor.worked( 1 );
		}

		return WorkerStatus.OK;
	}

	private void refactorAssignment(
			ICompilationUnit icu,
			RxSingleUnitWriter singleUnitWriter,
			SwingWorkerVisitor swingWorkerVisitor,
			Assignment assignment )
	{
		removeSuperInvocations( swingWorkerVisitor );

		// changes all get() / get(long, TimeUnit) invocation by a variable name
		removeGetInvocations( swingWorkerVisitor );

		// get() and get(long, TimeUnit) throw exceptions.
		// Since they were just replaced by a variable name, the catch clauses
		// must be removed
		ASTUtil.removeUnnecessaryCatchClauses( swingWorkerVisitor.getDoneBlock() );

		updateImports( singleUnitWriter );

		String icuName = icu.getElementName();
		RxObservableDto observableDto = createObservableDto( icuName, swingWorkerVisitor );

		Map<String, Object> observableData = new HashMap<>();
		observableData.put( "dto", observableDto );
		String observableTemplate = "observable.ftl";

		String observableString = TemplateUtils.processTemplate( observableTemplate, observableData );

		AST ast = assignment.getAST();
		Statement observableStatement = ASTNodeFactory.createSingleStatementFromText( ast, observableString );

		Statement referenceStatement = ASTUtil.findParent( assignment, Statement.class );
		singleUnitWriter.addStatementBefore( observableStatement, referenceStatement );

		SimpleName swingWorkerName = (SimpleName) assignment.getLeftHandSide();
		String rxObserverName = RefactoringUtils.getNewVarName( swingWorkerName.toString() );
		RxSubscriberDto subscriberDto = createObserverDto( rxObserverName, swingWorkerVisitor, observableDto );

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
		singleUnitWriter.addImport( "rx.exceptions.Exceptions" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWEmitter" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWSubscriber" );
		singleUnitWriter.addImport( "de.tudarmstadt.stg.rx.swingworker.SWDto" );
		singleUnitWriter.removeImport( "java.util.concurrent.TimeoutException" );
	}

	private RxObservableDto createObservableDto( String icuName, SwingWorkerVisitor swingWorkerVisitor )
	{
		RxObservableDto observableDto = new RxObservableDto( icuName );
		observableDto.setResultType( swingWorkerVisitor.getResultType().toString() );
		observableDto.setProcessType( swingWorkerVisitor.getProcessType().toString() );
		observableDto.setDoInBackgroundBlock( swingWorkerVisitor.getDoInBackgroundBlock().toString() );
		return observableDto;
	}

	private RxSubscriberDto createObserverDto( String observerName, SwingWorkerVisitor swingWorkerVisitor, RxObservableDto observableDto )
	{
		RxSubscriberDto subscriberDto = new RxSubscriberDto();
		subscriberDto.setObserverName( observerName );
		subscriberDto.setResultType( swingWorkerVisitor.getResultType().toString() );
		subscriberDto.setProcessType( swingWorkerVisitor.getProcessType().toString() );
		subscriberDto.setObservableName( observableDto.getVarName() );
		subscriberDto.setChunksName( swingWorkerVisitor.getProcessVariableName() );
		subscriberDto.setAsyncResultVarName( swingWorkerVisitor.getAsyncResultVarName() );
		Block processBlock = swingWorkerVisitor.getProcessBlock();
		if ( processBlock != null )
		{
			subscriberDto.setProcessBlock( processBlock.toString() );
		}
		Block doneBlock = swingWorkerVisitor.getDoneBlock();
		if ( doneBlock != null )
		{
			subscriberDto.setDoneBlock( doneBlock.toString() );
		}

		String interruptedExceptionName = swingWorkerVisitor.getInterruptedExceptionName();
		String timeoutExceptionName = swingWorkerVisitor.getTimeoutExceptionName();
		if ( interruptedExceptionName != null )
		{
			subscriberDto.setThrowableName( interruptedExceptionName );
		}
		else if ( timeoutExceptionName != null )
		{
			subscriberDto.setThrowableName( timeoutExceptionName );
		}
		else
		{
			subscriberDto.setThrowableName( "throwable" );
		}
		subscriberDto.setOnErrorBlockEnabled( true );
		Block timeoutCatchBlock = swingWorkerVisitor.getTimeoutCatchBlock();
		if ( timeoutCatchBlock != null )
		{
			subscriberDto.setTimeoutExceptionBlock( StringUtils.removeBlockBraces( timeoutCatchBlock ) );
		}
		Block interruptedCatchBlock = swingWorkerVisitor.getInterruptedCatchBlock();
		if ( interruptedCatchBlock != null )
		{
			subscriberDto.setInterruptedExceptionBlock( StringUtils.removeBlockBraces( interruptedCatchBlock ) );
		}
		return subscriberDto;
	}

	private void removeGetInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		if ( swingWorkerVisitor.getDoneBlock() != null )
		{
			for ( MethodInvocation methodInvocation : swingWorkerVisitor.getMethodInvocationsGet() )
			{
				replaceGetInvocation( swingWorkerVisitor, methodInvocation );
			}
			for ( SuperMethodInvocation methodInvocation : swingWorkerVisitor.getSuperMethodInvocationsGet() )
			{
				replaceGetInvocation( swingWorkerVisitor, methodInvocation );
			}
		}
	}

	private <T extends ASTNode> void replaceGetInvocation( SwingWorkerVisitor swingWorkerVisitor, T methodInvocation )
	{
		String resultVariableName = swingWorkerVisitor.getAsyncResultVarName();
		SimpleName variableName = methodInvocation.getAST().newSimpleName( resultVariableName );
		ASTUtil.replaceInStatement( methodInvocation, variableName );
	}

	private void removeSuperInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		for ( SuperMethodInvocation methodInvocation : swingWorkerVisitor.getSuperMethodInvocationsToRemove() )
		{
			Statement statement = ASTUtil.findParent( methodInvocation, Statement.class );
			statement.delete();
		}
	}
}
