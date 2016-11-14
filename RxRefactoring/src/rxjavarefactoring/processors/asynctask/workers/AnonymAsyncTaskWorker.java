package rxjavarefactoring.processors.asynctask.workers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import rxjavarefactoring.framework.AbstractRefactorWorker;
import rxjavarefactoring.framework.RxLogger;
import rxjavarefactoring.framework.RxMultipleChangeWriter;
import rxjavarefactoring.framework.RxSingleChangeWriter;
import rxjavarefactoring.processors.CuCollector;
import rxjavarefactoring.processors.WorkerStatus;
import rxjavarefactoring.processors.asynctask.visitors.AsyncTaskVisitor;
import rxjavarefactoring.utils.ASTUtil;
import rxjavarefactoring.utils.CodeFactory;

/**
 * Description: This worker is in charge of refactoring anonymous AsyncTasks
 * that are not assigned to a variable.<br>
 * Example: new AsyncTask(){...}.execute();<br>
 * Author: Grebiel Jose Ifill Brito<br>
 * Created: 11/12/2016
 */
// TODO: still in progress. execute(params) not yet considered!
public class AnonymAsyncTaskWorker extends AbstractRefactorWorker
{

	public AnonymAsyncTaskWorker( CuCollector collector, IProgressMonitor monitor, RxMultipleChangeWriter rxMultipleChangeWriter )
	{
		super( collector, monitor, rxMultipleChangeWriter );
	}

	@Override
	public WorkerStatus call() throws Exception
	{
		try
		{
			RxLogger.info( this, "METHOD=call - Starting worker in thread: " + Thread.currentThread().getName() );
			return refactorAnonymClasses();
		}
		catch ( Exception e )
		{
			RxLogger.error(this, "METHOD=refactorAnonymClasses" , e);
			return WorkerStatus.ERROR;
		}
	}

	// ### Private Methods ###

	private WorkerStatus refactorAnonymClasses()
	{
		Map<ICompilationUnit, List<AnonymousClassDeclaration>> cuAnonymousClassesMap = collector.getCuAnonymousClassesMap();
		int numCunits = collector.getNumberOfCompilationUnits();
		monitor.beginTask( getClass().getSimpleName(), numCunits);
		RxLogger.info( this, "METHOD=refactorAnonymClasses - Number of compilation units: " + numCunits);
		for ( ICompilationUnit icu : cuAnonymousClassesMap.keySet() )
		{
			List<AnonymousClassDeclaration> declarations = cuAnonymousClassesMap.get( icu );
			for ( AnonymousClassDeclaration asyncTaskDeclaration : declarations )
			{
				// TODO: AsyncTaskVisitor is incomplete
				RxLogger.info( this, "METHOD=refactorAnonymClasses - Extract Information from AsyncTask: " + icu.getElementName() );
				AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
				asyncTaskDeclaration.accept( asyncTaskVisitor );
				AST ast = asyncTaskDeclaration.getAST();

				RxSingleChangeWriter singleChangeWriter = new RxSingleChangeWriter( icu, ast, getClass().getSimpleName() );
				RxLogger.info( this, "METHOD=refactorAnonymClasses - Updating imports: " + icu.getElementName() );
				updateImports( singleChangeWriter );
				createLocalObservable( singleChangeWriter, asyncTaskDeclaration, ast );
				singleChangeWriter.removeStatement( asyncTaskDeclaration );

				RxLogger.info( this, "METHOD=refactorAnonymClasses - Refactoring class: " + icu.getElementName() );
				rxMultipleChangeWriter.addChange( icu.getElementName(), icu, singleChangeWriter );

			}
			monitor.worked( 1 );
		}
		return WorkerStatus.OK;
	}

	private void updateImports( RxSingleChangeWriter rewriter )
	{
		rewriter.addImport( "rx.Observable" );
		rewriter.addImport( "rx.schedulers.Schedulers" );
		rewriter.addImport( "rx.functions.Action1" );
		rewriter.addImport( "rx.android.schedulers.AndroidSchedulers" );
		rewriter.addImport( "java.util.concurrent.Callable" );
		rewriter.removeImport( "android.os.AsyncTask" );
	}

	// TODO: Use a helper class to create the observable!
	private void createLocalObservable( RxSingleChangeWriter rewriter, AnonymousClassDeclaration taskObject, AST ast )
	{
		AsyncTaskVisitor asyncTaskVisitor = new AsyncTaskVisitor();
		taskObject.accept( asyncTaskVisitor );

		String observableCall = createObservable( asyncTaskVisitor );
		Block observableStatement = CodeFactory.getStatementsBlockFromText( ast, observableCall );

		Statement referenceStatement = ASTUtil.getStmtParent( taskObject );
		Statement newStatement = (Statement) observableStatement.statements().get( 0 );
		rewriter.addStatementBefore( newStatement, referenceStatement );
	}

	private String createObservable( AsyncTaskVisitor asyncTaskVisitor )
	{
		String doInBackgroundStatements = asyncTaskVisitor.getDoInBackgroundBlock().toString();
		String doOnCompletedStatements = asyncTaskVisitor.getOnPostExecuteBlock().toString();
		String returnedType = asyncTaskVisitor.getReturnedType().toString();
		String resultVariableName = asyncTaskVisitor.getResultVariableName();

		String observable = "Observable.fromCallable(new Callable<" + returnedType + ">() {" +
				"	@Override" +
				"	public " + returnedType + " call() throws Exception " + doInBackgroundStatements +
				"})" +
				".subscribeOn(Schedulers.computation())" +
				".observeOn(AndroidSchedulers.mainThread())" +
				".doOnNext(new Action1<" + returnedType + ">() {" +
				"	@Override" +
				"	public void call(" + returnedType + " " + resultVariableName + ") " + doOnCompletedStatements +
				"})" +
				".subscribe();";

		return observable;
	}
}
