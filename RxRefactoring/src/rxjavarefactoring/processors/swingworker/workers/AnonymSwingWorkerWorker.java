package rxjavarefactoring.processors.swingworker.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import rxjavarefactoring.framework.builders.RxObservableStringBuilder;
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
				RxLogger.info( this, "METHOD=refactor - Extract Information from SwingWorker: " + icu.getElementName() );
				SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
				swingWorkerDeclaration.accept( swingWorkerVisitor );
				AST ast = swingWorkerDeclaration.getAST();

				RxSingleChangeWriter singleChangeWriter = new RxSingleChangeWriter( icu, ast, getClass().getSimpleName() );
				RxLogger.info( this, "METHOD=refactor - Updating imports: " + icu.getElementName() );
				createLocalObservable( singleChangeWriter, swingWorkerDeclaration, ast );
				singleChangeWriter.removeStatement( swingWorkerDeclaration );

				RxLogger.info( this, "METHOD=refactor - Refactoring class: " + icu.getElementName() );
				rxMultipleChangeWriter.addChange( icu, singleChangeWriter );

			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private void createLocalObservable( RxSingleChangeWriter rewriter, AnonymousClassDeclaration swingWorkerObject, AST ast )
	{
		SwingWorkerVisitor swingWorkerVisitor = new SwingWorkerVisitor();
		swingWorkerObject.accept( swingWorkerVisitor );

		String observableStatement = createObservable( swingWorkerVisitor );
		Block observableBlock = CodeFactory.getStatementsBlockFromText( ast, observableStatement );

		Statement referenceStatement = ASTUtil.getStmtParent( swingWorkerObject );
		List statements = observableBlock.statements();
		Statement newStatement = (Statement) statements.get( 0 );
		rewriter.addStatementBefore( newStatement, referenceStatement );

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
			if ( swingWorkerVisitor.getDoneBlock().toString().contains( "TimeUnit" ) )
			{
				rewriter.addImport( "java.util.concurrent.TimeUnit" );
			}
			rewriter.removeImport( "java.util.concurrent.ExecutionException" );
		}
	}

	private String createObservable( SwingWorkerVisitor swingWorkerVisitor )
	{
		Block doInBackgroundBlock = swingWorkerVisitor.getDoInBackgroundBlock();
		Block doOnCompletedBlock = swingWorkerVisitor.getDoneBlock();
		String type = swingWorkerVisitor.getResultType().toString();
		String resultVariableName = swingWorkerVisitor.getResultVariableName();
		List<String> timeOutArguments = swingWorkerVisitor.getTimeoutArguments();

		String observableStatement = RxObservableStringBuilder
				.newObservable( type, doInBackgroundBlock, SchedulerType.JAVA_MAIN_THREAD )
				.addDoOnNext( doOnCompletedBlock, resultVariableName )
				.addTimeout( timeOutArguments )
				.addSubscribe()
				.build();

		if ( swingWorkerVisitor.isMethodGetPresent() )
		{
			observableStatement = observableStatement.replaceFirst( "get\\(.*\\)", resultVariableName );
		}

		return observableStatement;
	}
}