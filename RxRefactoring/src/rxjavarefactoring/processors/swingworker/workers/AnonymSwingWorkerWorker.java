package rxjavarefactoring.processors.swingworker.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import rxjavarefactoring.framework.builders.RxObservableStringBuilder;
import rxjavarefactoring.framework.builders.RxSubscriberStringBuilder;
import rxjavarefactoring.framework.constants.SchedulerType;
import rxjavarefactoring.framework.refactoring.AbstractRefactorWorker;
import rxjavarefactoring.framework.utils.ASTUtil;
import rxjavarefactoring.framework.utils.CodeFactory;
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

	private static final String RIGHT_REC_BRACE = "]";
	private static final String LEFT_REC_BRACE = "[";
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
				RxLogger.info( this, "METHOD=refactor - Gathering information from SwingWorker: " + icu.getElementName() );
				SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
				swingWorkerDeclaration.accept( swingWorkerVisitor );
				AST ast = swingWorkerDeclaration.getAST();

				RxSingleChangeWriter singleChangeWriter = new RxSingleChangeWriter( icu, ast, getClass().getSimpleName() );
				RxLogger.info( this, "METHOD=refactor - Creating rx.Observable object: " + icu.getElementName() );
				createLocalObservable( singleChangeWriter, swingWorkerDeclaration, ast );

				if ( swingWorkerVisitor.getProcessBlock() != null )
				{
					RxLogger.info( this, "METHOD=refactor - Creating getRxUpdateSubscriber method: " + icu.getElementName() );
					String newMethodString = createNewMethod(swingWorkerVisitor);
					MethodDeclaration newMethod = CodeFactory.createMethodFromText( ast, newMethodString );
					singleChangeWriter.addMethodAfter( newMethod, swingWorkerDeclaration );
				}

				singleChangeWriter.removeStatement( swingWorkerDeclaration );

				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleChangeWriter.addChange( icu, singleChangeWriter );

			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private String createNewMethod(SwingWorkerVisitor swingWorkerVisitor)
	{
		String newSubscriber = RxSubscriberStringBuilder.newSubscriber(
                swingWorkerVisitor.getProgressUpdateTypeName(),
                swingWorkerVisitor.getProcessBlock(),
                swingWorkerVisitor.getProgressUpdateVariableName() );

		// TODO getRxUpdateSubscriber should be numbered in case that there is already one, gif 2016-11-19
		return "private Subscriber<" +
                swingWorkerVisitor.getProgressUpdateTypeName() +
                "> getRxUpdateSubscriber() { return "
                + newSubscriber + "}";
	}

	private void createLocalObservable( RxSingleChangeWriter rewriter, AnonymousClassDeclaration swingWorkerObject, AST ast )
	{
		SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
		swingWorkerObject.accept( swingWorkerVisitor );

		String observableStatement = createObservable( swingWorkerVisitor );
		Statement newStatement = CodeFactory.createSingleStatementFromTest( ast, observableStatement );

		Statement referenceStatement = ASTUtil.findParent( swingWorkerObject, Statement.class );

		if ( swingWorkerVisitor.getProcessBlock() != null )
		{
			Statement getSubscriberStatement = createUpdateSubscriberInstance( swingWorkerVisitor, newStatement );
			rewriter.addStatementBefore( getSubscriberStatement, referenceStatement );
		}
		rewriter.addStatementBefore( newStatement, referenceStatement );

		updateImports( rewriter, swingWorkerVisitor );
	}

	private Statement createUpdateSubscriberInstance( SwingWorkerVisitor swingWorkerVisitor, Statement newStatement )
	{
		// TODO rxUpdateSubscriber and getRxUpdateSubscriber should be numbered in case that there is already one, gif 2016-11-19
		String type = swingWorkerVisitor.getProgressUpdateTypeName();
		String subscriberDecl = "final Subscriber<" + type + "> rxUpdateSubscriber = getRxUpdateSubscriber()";
		return CodeFactory.createSingleStatementFromTest( newStatement.getAST(), subscriberDecl );
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
	}

	private String createObservable( SwingWorkerVisitor swingWorkerVisitor )
	{
		Block doInBackgroundBlock = swingWorkerVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = swingWorkerVisitor.getDoneBlock();
		String type = swingWorkerVisitor.getResultType().toString();
		String resultVariableName = swingWorkerVisitor.getResultVariableName();
		List<String> timeOutArguments = swingWorkerVisitor.getTimeoutArguments();
		Block timeoutCatchBlock = swingWorkerVisitor.getTimeoutCatchBlock();

		// replaces publish(x, y, ...) by rxUpdateSubscriber.onNext(Arrays.asList(x, y, ...))
		replacePublishInvocations( swingWorkerVisitor );

		// changes all get() / get(long, TimeUnit) invocation by a variable name
		removeGetInvocations( swingWorkerVisitor );

		// get() and get(long, TimeUnit) throw exceptions.
		// Since they were just replaced by a variable name, the catch clauses
		// must be removed
		ASTUtil.removeUnnecessaryCatchClauses( doOnCompletedBlock );

		String observableStatement = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnCompletedBlock, resultVariableName )
				.addTimeout( timeOutArguments, timeoutCatchBlock )
				.addSubscribe()
				.build();

		return observableStatement;
	}

	private void replacePublishInvocations( SwingWorkerVisitor swingWorkerVisitor )
	{
		if ( !swingWorkerVisitor.getPublishInvocations().isEmpty() )
		{
			for ( MethodInvocation publishInvocation : swingWorkerVisitor.getPublishInvocations() )
			{
				List argumentList = publishInvocation.arguments();
				String argumentsString = argumentList.toString().replace( RIGHT_REC_BRACE, EMPTY ).replace( LEFT_REC_BRACE, EMPTY );
				// TODO rxUpdateSubscriber should be numbered in case that there is already one, gif 2016-11-19
				String newInvocation = "rxUpdateSubscriber.onNext(Arrays.asList(" + argumentsString + "))";
				Statement newStatement = CodeFactory.createSingleStatementFromTest( publishInvocation.getAST(), newInvocation );
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
