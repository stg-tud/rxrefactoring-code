package rxjavarefactoring.processors.swingworker.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.codegenerators.ASTNodeFactory;
import rxjavarefactoring.framework.codegenerators.ComplexRxObservableBuilder;
import rxjavarefactoring.framework.codegenerators.RxObservableStringBuilder;
import rxjavarefactoring.framework.codegenerators.RxSubscriberHolder;
import rxjavarefactoring.framework.constants.SchedulerType;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.RxLogger;
import rxjavarefactoring.framework.writers.RxMultipleChangeWriter;
import rxjavarefactoring.framework.writers.RxSingleChangeWriter;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.WorkerStatus;
import rxjavarefactoring.processors.swingworker.visitors.SwingWorkerVisitor;

/**
 * Description: This worker is in charge of refactoring anonymous SwingWorkers
 * that are not assigned to a variable<br>
 * Example: new SwingWorker(){...}.execute();
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/16/2016
 */
public class AnonymSwingWorkerWorker extends AbstractRefactorWorker<CuCollector>
{
	private static final String EMPTY = "";

	public AnonymSwingWorkerWorker( CuCollector collector, IProgressMonitor monitor, RxMultipleChangeWriter rxMultipleChangeWriter )
	{
		super( collector, monitor, rxMultipleChangeWriter );
	}

	@Override
	protected WorkerStatus refactor()
	{
		Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap = collector.getCuAnonymousClassesMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits );
		RxLogger.info( this, "METHOD=refactor - Number of compilation units: " + numCunits );
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			List<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			for ( AnonymousClassDeclaration swingWorkerDeclaration : declarations )
			{
				// Collect details about the SwingWorker
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
				swingWorkerDeclaration.accept( swingWorkerVisitor );
				AST ast = swingWorkerDeclaration.getAST();

				RxSingleChangeWriter singleChangeWriter = new RxSingleChangeWriter( icu, ast, getClass().getSimpleName() );

				// Create rx.Observable using the Subscriber if necessary
				RxLogger.info( this, "METHOD=refactor - Creating rx.Observable object: " + icu.getElementName() );
				Statement referenceStatement = ASTUtil.findParent( swingWorkerDeclaration, Statement.class );
				addRxObservable( icu, singleChangeWriter, referenceStatement, swingWorkerVisitor, swingWorkerDeclaration );

				// remove existing SwingWorker
				singleChangeWriter.removeStatement( swingWorkerDeclaration );

				// Add changes to the multiple compilation units write object
				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleChangeWriter.addChange( icu, singleChangeWriter );

			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private void addRxObservable(
			ICompilationUnit icu,
			RxSingleChangeWriter rewriter,
			Statement referenceStatement,
			SwingWorkerVisitor swingWorkerVisitor,
			AnonymousClassDeclaration swingWorkerDeclaration )
	{
		boolean complexRxObservableClassNeeded = swingWorkerVisitor.hasAdditionalFieldsOrMethods();
		boolean processBlockExists = swingWorkerVisitor.getProcessBlock() != null;

		RxSubscriberHolder subscriberHolder = null;
		if ( processBlockExists )
		{
			subscriberHolder = new RxSubscriberHolder(
					icu.getElementName(),
					swingWorkerVisitor.getProgressUpdateTypeName(),
					swingWorkerVisitor.getProcessBlock(),
					swingWorkerVisitor.getProgressUpdateVariableName() );
		}

		AST ast = referenceStatement.getAST();
		if ( !complexRxObservableClassNeeded )
		{
			String subscribedObservable = createObservable( swingWorkerVisitor, subscriberHolder )
					.addSubscribe()
					.build();
			if ( processBlockExists )
			{
				String newMethodString = subscriberHolder.getGetMethodDeclaration();
				MethodDeclaration newMethod = ASTNodeFactory.createMethodFromText( ast, newMethodString );
				rewriter.addMethodAfter( newMethod, swingWorkerDeclaration );

				String subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				Statement getSubscriberStatement = ASTNodeFactory.createSingleStatementFromTest( ast, subscriberDecl );
				rewriter.addStatementBefore( getSubscriberStatement, referenceStatement );
			}
			Statement newStatement = ASTNodeFactory.createSingleStatementFromTest( ast, subscribedObservable );
			rewriter.addStatementBefore( newStatement, referenceStatement );
		}
		else
		{
			List<FieldDeclaration> fieldDeclarations = swingWorkerVisitor.getFieldDeclarations();
			String subscriberDecl = EMPTY;
			String subscriberGetRxUpdateMethod = EMPTY;
			if ( processBlockExists )
			{
				subscriberDecl = subscriberHolder.getSubscriberDeclaration();
				subscriberGetRxUpdateMethod = subscriberHolder.getGetMethodDeclaration();
			}

			String observableStatement = createObservable( swingWorkerVisitor, subscriberHolder )
					.buildReturnStatement();
			String observableType = swingWorkerVisitor.getResultType().toString();

			String complexRxObservableClass = ComplexRxObservableBuilder.newComplexRxObservable( icu.getElementName() )
					.withFields( fieldDeclarations )
					.withGetAsyncObservable( observableType, subscriberDecl, observableStatement )
					.withMethod( subscriberGetRxUpdateMethod )
					.withMethods( swingWorkerVisitor.getAdditionalMethodDeclarations() ).build();

			TypeDeclaration complexRxObservableDecl = ASTNodeFactory.createTypeDeclarationFromText( ast, complexRxObservableClass );
			rewriter.addInnerClassAfter( complexRxObservableDecl, referenceStatement );

			String newStatementString = "new ComplexRxObservable().getAsyncObservable().subscribe();";
			Statement newSatement = ASTNodeFactory.createSingleStatementFromTest( ast, newStatementString );
			rewriter.addStatementBefore( newSatement, referenceStatement );

		}

		updateImports( rewriter, swingWorkerVisitor );
	}

	private void updateImports( RxSingleChangeWriter rewriter, SwingWorkerVisitor swingWorkerVisitor )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		rewriter.removeImport( "javax.swing.SwingWorker" );
		if ( swingWorkerVisitor.getDoneBlock() != null )
		{
			rewriter.addImport( "rx.functions.Action1" );
			if ( !swingWorkerVisitor.getTimeoutArguments().isEmpty() )
			{
				rewriter.addImport( "rx.functions.Func1" );
				rewriter.addImport( "java.util.concurrent.TimeUnit" );
			}
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
			rewriter.removeImport( "java.util.concurrent.TimeoutException" );
		}
		if ( swingWorkerVisitor.getProcessBlock() != null )
		{
			rewriter.addImport( "rx.Subscriber" );
			rewriter.addImport( "java.util.Arrays" );
		}
	}

	private RxObservableStringBuilder createObservable( SwingWorkerVisitor swingWorkerVisitor, RxSubscriberHolder subscriberHolder )
	{
		Block doInBackgroundBlock = swingWorkerVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = swingWorkerVisitor.getDoneBlock();
		String type = swingWorkerVisitor.getResultType().toString();
		String resultVariableName = swingWorkerVisitor.getResultVariableName();
		List<String> timeOutArguments = swingWorkerVisitor.getTimeoutArguments();
		Block timeoutCatchBlock = swingWorkerVisitor.getTimeoutCatchBlock();

		// replaces publish(x, y, ...) by rxUpdateSubscriber.onNext(Arrays.asList(x, y, ...))
		replacePublishInvocations( swingWorkerVisitor, subscriberHolder );

		// changes all get() / get(long, TimeUnit) invocation by a variable name
		removeGetInvocations( swingWorkerVisitor );

		// get() and get(long, TimeUnit) throw exceptions.
		// Since they were just replaced by a variable name, the catch clauses
		// must be removed
		ASTUtil.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		return RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnCompletedBlock, resultVariableName )
				.addTimeout( timeOutArguments, timeoutCatchBlock );
	}

	private void replacePublishInvocations( SwingWorkerVisitor swingWorkerVisitor, RxSubscriberHolder subscriberHolder )
	{
		if ( !swingWorkerVisitor.getPublishInvocations().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : swingWorkerVisitor.getPublishInvocations() )
			{
				List argumentList = publishInvocation.arguments();
				String newInvocation = subscriberHolder.getOnNextInvocation( argumentList );
				Statement newStatement = ASTNodeFactory.createSingleStatementFromTest( publishInvocation.getAST(), newInvocation );
				ASTUtil.replaceInStatement( publishInvocation, newStatement );
			}
		}
	}

	private void removeGetInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		if ( swingWorkerVisitor.getDoneBlock() != null )
		{
			for ( MethodInvocation methodInvocation : swingWorkerVisitor.getMethodInvocationsGet() )
			{
				String resultVariableName = swingWorkerVisitor.getResultVariableName();
				SimpleName variableName = methodInvocation.getAST().newSimpleName( resultVariableName );
				ASTUtil.replaceInStatement( methodInvocation, variableName );
			}
		}
	}
}
